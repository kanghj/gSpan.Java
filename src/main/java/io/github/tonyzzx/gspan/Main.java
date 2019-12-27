package io.github.tonyzzx.gspan;

import org.apache.commons.cli.*;

import smu.hongjin.CountingUtils;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Map;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) throws IOException {
        Arguments arguments = Arguments.getInstance(args);

        File inFile = new File(arguments.inFilePath);
        File outFile = new File(arguments.outFilePath);
        try (FileReader reader = new FileReader(inFile)) {
            try (FileWriter writer = new FileWriter(outFile)) {
                gSpan gSpan = new gSpan();
                System.out.println("gSpan is mining...");
                gSpan.run(reader, writer, arguments.minSup, arguments.maxNodeNum, arguments.minNodeNum);
                System.out.println("It's done! The result is in  " + arguments.outFilePath + " .");
                
                try (BufferedWriter selectedSubGraphWriter = new BufferedWriter(new FileWriter(arguments.outFilePath + "_best_subgraphs.txt"))) {
        			for (Map.Entry<Long, Double> subgraphFeature : gSpan.selectedSubgraphFeatures.entrySet()) {
        				selectedSubGraphWriter.write(subgraphFeature.getKey() + "," + subgraphFeature.getValue());
        				selectedSubGraphWriter.write("\n");
        			}
        		}
                System.out.println("The identified discriminative subgraphs are in  " + arguments.outFilePath + "_best_subgraphs.txt");
            	try(BufferedWriter featuresWriter = new BufferedWriter(new FileWriter(arguments.outFilePath + "_features.txt" ))) {
        			CountingUtils.writeGraphFeatures(gSpan, gSpan.coverage, featuresWriter);
        		}
            	System.out.println("The feature vectors of labeled graphs are in " + arguments.outFilePath + "_features.txt");
            }
        }
		
    }

    private static class Arguments {
        private static Arguments arguments;

        private String[] args;

        String inFilePath;
        long minSup;
        long minNodeNum = 0;
        long maxNodeNum = Long.MAX_VALUE;
        String outFilePath;

        private Arguments(String[] args) {
            this.args = args;
        }

        static Arguments getInstance(String[] args) {
            arguments = new Arguments(args);
            if (args.length > 0) {
                arguments.initFromCmd();
            } else {
                arguments.initFromRun();
            }
            return arguments;
        }

        /***
         * User inputs args.
         */
        private void initFromCmd() {
            Options options = new Options();
            options.addRequiredOption("d", "data", true, "(Required) File path of data set");
            options.addRequiredOption("s", "sup", true, "(Required) Minimum support");
            options.addOption("i", "min-node", true, "Minimum number of nodes for each sub-graph");
            options.addOption("a", "max-node", true, "Maximum number of nodes for each sub-graph");
            options.addOption("r", "result", true, "File path of result");
            options.addOption("h", "help", false, "Help");

            CommandLineParser parser = new DefaultParser();
            HelpFormatter formatter = new HelpFormatter();
            CommandLine cmd = null;
            try {
                cmd = parser.parse(options, args);
                if (cmd.hasOption("h")) {
                    formatter.printHelp("gSpan", options);
                    System.exit(0);
                }
            } catch (ParseException e) {
                formatter.printHelp("gSpan", options);
                System.exit(1);
            }

            inFilePath = cmd.getOptionValue("d");
            minSup = Long.parseLong(cmd.getOptionValue("s"));
            minNodeNum = Long.parseLong(cmd.getOptionValue("i", "0"));
            maxNodeNum = Long.parseLong(cmd.getOptionValue("a", String.valueOf(Long.MAX_VALUE)));
            outFilePath = cmd.getOptionValue("r", inFilePath + "_result");
        }

        /***
         * User runs it directly.
         */
        private void initFromRun() {
            try (Scanner sc = new Scanner(System.in)) {
                System.out.println("Please input the file path of data set: ");
                inFilePath = sc.nextLine();
                System.out.println("Please set the minimum support: ");
                minSup = sc.nextLong();
                outFilePath = inFilePath + "_result";
                
                maxNodeNum = 6;
            }
        }
    }
}
