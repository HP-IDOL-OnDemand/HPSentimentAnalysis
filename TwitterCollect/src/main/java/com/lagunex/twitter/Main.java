/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.lagunex.twitter;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.function.Consumer;
import org.apache.commons.cli.BasicParser;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

/**
 *
 * @author carloshq
 */
public class Main {
    private static final String DEFAULT_QUERY = "$HPQ";
    private static final int DEFAULT_NUMBER = 100;
    
    public static void main(String[] args) {
        CommandLine cli = parseOptions(getCliOptions(),args);
        
        int number = cli.hasOption('n') ? Integer.parseInt(cli.getOptionValue('n')) : DEFAULT_NUMBER;
        String query = (cli.getArgs().length == 1) ? cli.getArgs()[cli.getArgs().length-1] : DEFAULT_QUERY;
        PrintWriter pw = getPrintWriter(cli);
        
        TwitterClient client = TwitterClient.getInstance();
        client.setMaxResults(number);
		
		Consumer<Tweet> consumer = tweet -> pw.println(tweet.toTblString());
		if (cli.hasOption('s')) {
			client.searchSince(query, Long.parseLong(cli.getOptionValue('s')), consumer);
		} else if (cli.hasOption('u')) {
			client.searchUntil(query, Long.parseLong(cli.getOptionValue('u')), consumer);
		} else {
			client.search(query, consumer);
		}
        
        pw.flush();
        pw.close();
    }
    
    private static Options getCliOptions() {
        Options options = new Options();
        options.addOption("f", "file", true, "File to output the query (defaults stdout)");
        options.addOption("n", "numberOfTweets", true, "Maximum number of Tweets to retrieve");
		options.addOption("s", "since", true, "Search results since given tweet");
        options.addOption("u", "until", true, "Search results until given tweet");

        options.addOption("h", "help", false, "Prints this message");
        return options;
    }
    
    private static CommandLine parseOptions(Options cliOptions, String[] args) {
        CommandLineParser parser = new BasicParser();
        CommandLine cli = null;
        try {
            cli = parser.parse(cliOptions, args);
            if (cli.hasOption("h") || cli.getArgs().length > 1) {
                printHelpAndExit(cliOptions,0);
            }
        } catch (ParseException ex) {
            printHelpAndExit(cliOptions,1);
        }
        return cli;
    }
    
    private static void printHelpAndExit(Options options, int exitCode) {
        HelpFormatter formatter = new HelpFormatter();
        formatter.printHelp( "java com.lagunex.twitter.Main [options] query", options );
        System.exit(exitCode);
    }

    private static PrintWriter getPrintWriter(CommandLine cli) {
        PrintWriter pw = null;
        if (cli.hasOption('f')) {
            try {
                pw = new PrintWriter(cli.getOptionValue('f'));
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
        } else {
            pw = new PrintWriter(System.out);
        }
        return pw;
    }
}
