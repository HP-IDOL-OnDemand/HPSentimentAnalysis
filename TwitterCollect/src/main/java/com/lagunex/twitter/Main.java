package com.lagunex.twitter;

import com.lagunex.util.StringUtils;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.function.Consumer;

// external dependencies to process command line arguments
import org.apache.commons.cli.BasicParser;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

/**
 * CLI Application to search for tweets.
 * Run with -h or --help for details
 * 
 * @author Carlos A. Henríquez Q. <carlos.henriquez@lagunex.com>
 */
public class Main {
    private final String DEFAULT_QUERY = "$HPQ";
    private final int DEFAULT_NUMBER = 100;

    private final String query;
    private final int number;
    private final PrintWriter output;
    private final TwitterClient client;
    private final long sinceId;
    private final long maxId;
    
    /**
     * Entry poing of the application
     * 
     * @param args arguments from the command line. Run with -h or --help for details 
     */
    public static void main(String[] args) {
        Main main = new Main(args);
        main.queryTwitter();
        main.closeResources();
    }

    public Main(String[] args) {
        CommandLine cli = parseOptions(getCliOptions(),args);
        sinceId = cli.hasOption('s') ? Long.parseLong(cli.getOptionValue('s')) : 0L;
        maxId = cli.hasOption('u') ? Long.parseLong(cli.getOptionValue('u')) : 0L;

        number = cli.hasOption('n') ? Integer.parseInt(cli.getOptionValue('n')) : DEFAULT_NUMBER;
        query = (cli.getArgs().length == 1) ? cli.getArgs()[cli.getArgs().length-1] : DEFAULT_QUERY;
        output = getPrintWriter(cli);

        client = TwitterClient.getInstance();
        client.setMaxResults(number);
    }

    private CommandLine parseOptions(Options cliOptions, String[] args) {
        CommandLineParser parser = new BasicParser();
        CommandLine cli = null;
        try {
            cli = parser.parse(cliOptions, args);
            if (cli.hasOption("h") || cli.getArgs().length > 1) {
                printHelpAndExit(cliOptions,0);
            } else if (cli.hasOption('s') && cli.hasOption('u')) {
                System.err.println("Cannot specified both --since and --until");
                printHelpAndExit(cliOptions, -1);
            }
        } catch (ParseException ex) {
            System.err.println(ex.getMessage());
            printHelpAndExit(cliOptions,-1);
        }
        return cli;
    }

    private void printHelpAndExit(Options options, int exitCode) {
        HelpFormatter formatter = new HelpFormatter();
        PrintWriter err = new PrintWriter(System.err);
        formatter.printHelp(err,
                HelpFormatter.DEFAULT_WIDTH,
                "java com.lagunex.twitter.Main [options] query","",
                options,
                HelpFormatter.DEFAULT_LEFT_PAD,HelpFormatter.DEFAULT_DESC_PAD,
                ""
        );
        err.flush();
        err.close();
        System.exit(exitCode);
    }
    
    private Options getCliOptions() {
        Options options = new Options();
        options.addOption("o", "output", true, "Output file (default stdout)");
        options.addOption("n", "numberOfTweets", true, "Maximum number of Tweets to retrieve");
		options.addOption("s", "since", true, "Search results since given tweet");
        options.addOption("u", "until", true, "Search results until given tweet");

        options.addOption("h", "help", false, "Prints this message");
        return options;
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
                throw new RuntimeException(ex);
            }
        } else {
            pw = new PrintWriter(System.out);
        }
        return pw;
    }

    private void queryTwitter() {
        Consumer<Tweet> printTweetToOutput = tweet -> {
            String singleLineMessage = StringUtils.collapseLines(tweet.getMessage());
            output.println(
                String.join(StringUtils.SEPARATOR, 
                    String.valueOf(tweet.getId()),
                    StringUtils.escape(singleLineMessage),
                    tweet.getLanguage(),
                    StringUtils.formatDateTime(tweet.getCreatedAt())
                )
            );
        };
        
		if (sinceId > 0) {
			client.searchSince(query, sinceId, printTweetToOutput);
		} else if (maxId > 0) {
			client.searchUntil(query, maxId, printTweetToOutput);
		} else {
			client.search(query, printTweetToOutput);
		}
    }

    private void closeResources() {
        output.flush();
        output.close();
    }
}
