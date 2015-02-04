package com.lagunex.nlp;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.function.Consumer;
import java.util.logging.Logger;

// external dependencies used to parse the command line arguments
import org.apache.commons.cli.BasicParser;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

/**
 * CLI application to perform sentiment analysis over lines of input.
 *
 * Run with -h or --help for details
 * The input consist of a number of lines, each line has the format:
 * 
 * id|text|lang
 * 
 * For instance,
 * 
 * 4983759487|This is a good valid line|en
 * 4857234985|Esta es una línea en español|es|2015-01-01 14:00:00|additional data...
 * 
 * The line is splitted by '|' and only the first three tokens are required
 * 
 * The output consiste of one or more lines per line of input.
 * The first line can be null if there was an error analysin the input or it can have the format
 * 
 * id|aggregate|score
 * 
 * For instance,
 * 
 * 4983759487|positive|0.6
 * 
 * The following lines describe the sentiments found in that sentence (if any) with the following format:
 * 
 * id|sentiment|topic|score
 * 
 * For instance,
 * 
 * 4983759487|good|valid line|0.6
 * 
 * @author Carlos A. Henríquez Q. <carlos.henriquez@lagunex.com>
 */
public class Main {
    private static final Logger LOGGER = Logger.getLogger(Main.class.getName());

    private final String SEPARATOR_REGEX="\\|";
    private final char DEFAULT_SEPARATOR = '|';

    private final BufferedReader input;
    private final PrintWriter output;

    /**
     * Entry poing of the application
     * 
     * @param args arguments from the command line. Run with -h or --help for details 
     */
    public static void main(String[] args) {
        Main main = new Main(args);
        main.analyseInput();
        main.flushAndCloseResources();
    }

    public Main(String[] args) {
        Options opt = getCliOptions();
        CommandLine cli = parseOptions(opt,args);

        input = getBufferReader(cli);
        output = getPrintWriter(cli);
        if (input == null || output == null) printHelpAndExit(opt, -1);
    }

    private Options getCliOptions() {
        Options options = new Options();
        options.addOption("i", "input", true, "Input file with opinions (default stdin)");
        options.addOption("o", "output", true, "Output file (default stdout)");

        options.addOption("h", "help", false, "Prints this message");
        return options;
    }

    private CommandLine parseOptions(Options cliOptions, String[] args) {
        CommandLineParser parser = new BasicParser();
        CommandLine cli = null;
        try {
            cli = parser.parse(cliOptions, args);
            if (cli.hasOption("h") || cli.getArgs().length > 1) {
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
                "java com.lagunex.twitter.Main [options] query","",
                options,
                HelpFormatter.DEFAULT_LEFT_PAD,HelpFormatter.DEFAULT_DESC_PAD,
                ""
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

    /**
     * Returns a PrintWriter from stdout (default) or a filename if specified
     * with -o option
     * @param cli
     * @return 
     */
    private PrintWriter getPrintWriter(CommandLine cli) {
        PrintWriter pw = null;
        if (cli.hasOption('o')) {
            try {
                pw = new PrintWriter(cli.getOptionValue('o'));
            } catch (IOException ex) {
                System.err.println(ex.getMessage());
                LOGGER.severe(ex.getMessage());
            }
        } else {
            pw = new PrintWriter(System.out);
        }
        return pw;
    }

    private void analyseInput() {
        SentimentAnalysis client = SentimentAnalysis.getInstance();
        String line = getNextLine();
        while(line != null) {
            String[] tokens = line.split(SEPARATOR_REGEX); // format: "id|text|lang" e.g. "8245245|text to analyze|en"
            SentimentResult result = client.analyse(
                tokens[1],
                SentimentAnalysis.Language.getLanguage(tokens[2])
            );
            printResult(tokens[0],result);
            line = getNextLine();
        }
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

    private void printResult(String id, SentimentResult result) {
        Consumer<Object> print = sentiment -> output.printf("%s%c%s%n", id, DEFAULT_SEPARATOR, sentiment);
        if (result != null) {
            print.accept(result.getAggregate());
            result.getPositive().forEach(print);
            result.getNegative().forEach(print);
        } else {
            output.println();
        }
        System.err.print('.');
    }

    private void flushAndCloseResources() {
        try { input.close(); } catch (Exception e) { LOGGER.warning(e.getMessage()); }
        output.flush();
        output.close();
    }
}
