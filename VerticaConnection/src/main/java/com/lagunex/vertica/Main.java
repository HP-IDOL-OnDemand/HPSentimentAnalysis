package com.lagunex.vertica;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.logging.Level;
import java.util.logging.Logger;

// external dependencies used to parse the command line arguments
import org.apache.commons.cli.BasicParser;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

/**
 * CLI application to insert records into Vertica
 *
 * Run with -h or --help for details
 * The input consist of a number of lines, each line represents a row with the format:
 * 
 * column1|column2|...
 * 
 * "null" columns will be added as NULL and any other "|" character that is not a split must be escaped.
 * 
 * The output will indicate the number of rows inserted
 * 
 * @author Carlos A. Henríquez Q. <carlos.henriquez@lagunex.com>
 */
public class Main {
    private static final Logger LOGGER = Logger.getLogger(Main.class.getName());

    private enum Table {tweet, sentiment};
    private final Table table;
    private final BufferedReader input;

    /**
     * Entry poing of the application
     * 
     * @param args arguments from the command line. Run with -h or --help for details 
     */
    public static void main(String[] args) {
        Main main = new Main(args);
        main.insertRecords();
    }

    public Main(String[] args) {
        Options opt = getCliOptions();
        CommandLine cli = parseOptions(opt,args);

        input = getBufferReader(cli);
        table = getTableName(cli.getArgs()[0]);
        if (input == null || table == null) printHelpAndExit(opt, -1);
    }

    private Options getCliOptions() {
        Options options = new Options();
        options.addOption("i", "input", true, "Input file with records (default stdin)");
        options.addOption("h", "help", false, "Prints this message");
        return options;
    }

    private CommandLine parseOptions(Options cliOptions, String[] args) {
        CommandLineParser parser = new BasicParser();
        CommandLine cli = null;
        try {
            cli = parser.parse(cliOptions, args);
            if (cli.hasOption("h") || cli.getArgs().length != 1) {
                printHelpAndExit(cliOptions,0);
            }
        } catch (ParseException ex) {
            System.err.println(ex.getMessage());
            LOGGER.info(ex.getMessage());
            printHelpAndExit(cliOptions,1);
        }
        return cli;
    }

    private void printHelpAndExit(Options options, int exitCode) {
        HelpFormatter formatter = new HelpFormatter();
        PrintWriter err = new PrintWriter(System.err);
        formatter.printHelp(
                err,HelpFormatter.DEFAULT_WIDTH,
                "java com.lagunex.nlp.Main [options] tableName","",
                options,
                HelpFormatter.DEFAULT_LEFT_PAD,HelpFormatter.DEFAULT_DESC_PAD,
                "tableName must be either tweet or sentiment"
        );
        err.flush();
        err.close();
        System.exit(exitCode);
    }

    /**
     * Returns a BufferedReader from stdin (default) or a filename if specified
     * with -i option
     * @param cli
     * @return 
     */
    private BufferedReader getBufferReader(CommandLine cli) {
        BufferedReader br = null;
        if (cli.hasOption('i')) {
            try {
                br = new BufferedReader(new FileReader(cli.getOptionValue('i')));
            } catch (FileNotFoundException ex) {
                System.err.println(ex.getMessage());
                LOGGER.severe(ex.getMessage());
            }
        } else {
           br = new BufferedReader(new InputStreamReader(System.in));
        }
        return br;
    }

    // forces tableName to be a valid Table or null
    private Table getTableName(String tableName) {
        Table table = null;
        try {
            table = Table.valueOf(tableName);
        } catch(IllegalArgumentException e) {
            LOGGER.log(Level.INFO, "Illegal table provided: {0}", tableName);
        }
        return table;
    }

    private void insertRecords() {
        Vertica vertica = Vertica.getInstance();
        String line = getNextLine();
        int total = 0;
        while(line != null) {
            if (table.equals(Table.tweet)) {
                total += vertica.insertTweetRecord(line);
            } else {
                total += vertica.insertSentimentRecord(line);
            }
            line = getNextLine();
            System.err.print(".");
        }
        System.out.println(total);
    }

    // Encapsulates the reading process to handle Exception
    private String getNextLine() {
        String line = null;
        try {
            line = input.readLine();
        } catch(IOException e) {
            LOGGER.severe(e.getMessage());
        }
        return line;
    }
}
