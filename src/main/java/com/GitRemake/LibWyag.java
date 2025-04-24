package com.GitRemake;

import org.apache.commons.cli.*;
import org.apache.commons.cli.Options;
import org.ini4j.*;

public class LibWyag {
  public static void main(String[] args) {
    // create basic components
    Options options = new Options();
    // parser for handling cli arguments
    CommandLineParser parser = new DefaultParser();

    try {
      CommandLine cmd = parser.parse(options, args, true);
      String[] remainingArgs = cmd.getArgs();

      // check if theres any args
      if (remainingArgs.length == 0) {
        System.err.println("Error: Enter a command");
        System.exit(1);
      }
      // get the first command
      String command = remainingArgs[0];

      // preparing the other arguments to be process
      String[] commandArgs = new String[remainingArgs.length - 1];
      System.arraycopy(remainingArgs, 1, commandArgs, 0, remainingArgs.length - 1);

      // check with a list of commands
      switch (command) {
        case "add" -> cmdAdd(commandArgs);
        case "cat-file" -> cmdCatFile(commandArgs);
        case "check-ignore" -> cmdCheckIgnore(commandArgs);
        case "checkout" -> cmdCheckout(commandArgs);
        case "commit" -> cmdCommit(commandArgs);
        case "hash-object" -> cmdHashObject(commandArgs);
        case "init" -> cmdInit(commandArgs);
        case "log" -> cmdLog(commandArgs);
        case "ls-files" -> cmdLsFiles(commandArgs);
        case "ls-tree" -> cmdLsTree(commandArgs);
        case "rev-parse" -> cmdRevParse(commandArgs);
        case "rm" -> cmdRm(commandArgs);
        case "show-ref" -> cmdShowRef(commandArgs);
        case "status" -> cmdStatus(commandArgs);
        case "tag" -> cmdTag(commandArgs);
        default -> System.out.println("Bad command.");
      }
    } catch (ParseException e) {
      System.err.println("Error parsing: " + e.getMessage());
      System.exit(1);
    }
  }
}
