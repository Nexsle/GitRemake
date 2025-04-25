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
        case "checkout" -> cmdCheckIgnore(commandArgs);
        case "commit" -> cmdInit(commandArgs);
        case "hash-object" -> cmdHashObject(commandArgs);
        case "init" -> cmdInit(commandArgs);
        case "log" -> cmdLog(commandArgs);
        case "ls-files" -> cmdLsFiles(commandArgs);
        case "ls-tree" -> cmdLsTree(commandArgs);
        case "rev-parse" -> cmdRevParse(commandArgs);
        case "rm" -> cmdAdd(commandArgs);
        case "show-ref" -> cmdShowRef(commandArgs);
        case "status" -> cmdStatus(commandArgs);
        case "tag" -> cmdLog(commandArgs);
        default -> System.out.println("Bad command.");
      }
    } catch (ParseException e) {
      System.err.println("Error parsing: " + e.getMessage());
      System.exit(1);
    }
  }

  private static void cmdInit(String[] commandArgs) {
    Options options = new Options();

    CommandLineParser parser = new DefaultParser();
    HelpFormatter helper = new HelpFormatter();

    try {
      CommandLine cmd = parser.parse(options, commandArgs);
      // default path is here
      String path = ".";
      if (cmd.getArgs().length > 0) {
        path = cmd.getArgs()[0];
      }

      GitRepository repo = GitRepository.repoCreate(path);
      System.out.println("Initialized empty repository in " + repo.getGitDir());

    } catch (ParseException e) {
      helper.printHelp("wyag init [directory]", options);
      System.exit(1);
    } catch (Exception e) {
      System.err.println("Error :" + e.getMessage());
      System.exit(1);
    }
  }

  private static void cmdStatus(String[] commandArgs) {
    // TODO Auto-generated method stub
    throw new UnsupportedOperationException("Unimplemented method 'cmdStatus'");
  }

  private static void cmdShowRef(String[] commandArgs) {
    // TODO Auto-generated method stub
    throw new UnsupportedOperationException("Unimplemented method 'cmdShowRef'");
  }

  private static void cmdRevParse(String[] commandArgs) {
    // TODO Auto-generated method stub
    throw new UnsupportedOperationException("Unimplemented method 'cmdRevParse'");
  }

  private static void cmdLsTree(String[] commandArgs) {
    // TODO Auto-generated method stub
    throw new UnsupportedOperationException("Unimplemented method 'cmdLsTree'");
  }

  private static void cmdLsFiles(String[] commandArgs) {
    // TODO Auto-generated method stub
    throw new UnsupportedOperationException("Unimplemented method 'cmdLsFiles'");
  }

  private static void cmdLog(String[] commandArgs) {
    // TODO Auto-generated method stub
    throw new UnsupportedOperationException("Unimplemented method 'cmdLog'");
  }

  private static void cmdHashObject(String[] commandArgs) {
    // TODO Auto-generated method stub
    throw new UnsupportedOperationException("Unimplemented method 'cmdHashvoid'");
  }

  private static void cmdCheckIgnore(String[] commandArgs) {
    // TODO Auto-generated method stub
    throw new UnsupportedOperationException("Unimplemented method 'cmdCheckIgnore'");
  }

  private static void cmdCatFile(String[] commandArgs) {
    // TODO Auto-generated method stub
    throw new UnsupportedOperationException("Unimplemented method 'cmdCatFile'");
  }

  private static void cmdAdd(String[] commandArgs) {
    // TODO Auto-generated method stub
    throw new UnsupportedOperationException("Unimplemented method 'cmdAdd'");
  }
}
