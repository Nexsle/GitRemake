package com.GitRemake;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
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
    String startCommit = commandArgs.length > 0 ? commandArgs[0] : "HEAD";

    GitRepository repo = GitRepository.repoFind();

    // Start the Graphviz output
    System.out.println("digraph wyaglog{");
    System.out.println(" node[shape=rect]");

    // Track which commit we've seen
    Set<String> seen = new HashSet<>();

    logGraphviz(repo, GitObjectUtil.objectFind(repo, startCommit), seen);

    System.out.println("}");
  }

  private static void cmdHashObject(String[] commandArgs) {
    Options options = new Options();
    options.addOption(
        Option.builder("t")
            .longOpt("type")
            .hasArgs()
            .desc("Specify the object type (blob, commit, tag, tree)")
            .required(true)
            .build());

    options.addOption(
        Option.builder("w")
            .longOpt("write")
            .desc("Actually write the object into the repository")
            .required(true)
            .build());

    CommandLineParser parser = new DefaultParser();
    HelpFormatter helper = new HelpFormatter();

    try {
      CommandLine cmd = parser.parse(options, commandArgs);
      // Get the file name behind the options
      String[] remainingArgs = cmd.getArgs();
      // make sure that user enter a file name
      if (remainingArgs.length == 0) {
        System.err.println("Error: No file name specified");
        helper.printHelp("wyag hash-object [-w] [-t TYPE] FILE", options);
        System.exit(1);
      }
      // get the path and type
      String filePath = remainingArgs[0];
      String type = cmd.getOptionValue("t", "blob");

      // read file
      byte[] fileContents;
      try {
        fileContents = Files.readAllBytes(Paths.get(filePath));
      } catch (IOException e) {
        System.err.println("Error reading file: " + filePath);
        System.exit(1);
        return;
      }

      GitObject gitObject;
      try {
        gitObject = GitObjectUtil.createObject(type, fileContents);
      } catch (IllegalArgumentException e) {
        System.err.println("Error: " + e.getMessage());
        System.exit(1);
        return;
      }

      GitRepository repo = null;
      if (cmd.hasOption("w")) {
        repo = GitRepository.repoFind();
      }

      String hash = GitObjectUtil.objectWrite(repo, gitObject);

      System.out.println(hash);

    } catch (ParseException e) {
      System.err.println("Error parsing arguments: " + e.getMessage());
      helper.printHelp("wyag hash-object [-w] [-t TYPE] FILE", options);
      System.exit(1);
    } catch (Exception e) {
      System.err.println("Error: " + e.getMessage());
      e.printStackTrace();
      System.exit(1);
    }
  }

  private static void cmdCheckIgnore(String[] commandArgs) {
    // TODO Auto-generated method stub
    throw new UnsupportedOperationException("Unimplemented method 'cmdCheckIgnore'");
  }

  private static void cmdCatFile(String[] commandArgs) {
    Options options = new Options();
    // add the type option
    options.addOption(
        Option.builder("t")
            .longOpt("type")
            .hasArgs()
            .desc("Specify the object type (blob, commit, tag, tree)")
            .required(true)
            .build());

    CommandLineParser parser = new DefaultParser();
    HelpFormatter helper = new HelpFormatter();

    try {
      CommandLine cmd = parser.parse(options, commandArgs);
      // Get the input behind the -type option
      String[] remainingArgs = cmd.getArgs();
      // check if user enter any thing
      if (remainingArgs.length == 0) {
        System.err.println("Error: no object type specified");
        helper.printHelp("wyag cat-file -t TYPE OBJECT", options);
        System.exit(1);
      }
      // get the value of the option
      String type = cmd.getOptionValue("t");
      // get the objectId which is also the hashed value
      String objectId = remainingArgs[0];

      GitRepository repo = GitRepository.repoFind();

      GitObject object = GitObjectUtil.objectRead(repo, objectId);
      if (object == null) {
        System.err.println("Error object not found: " + objectId);
        System.exit(1);
      }

      if (!object.getType().equals(type)) {
        System.err.println("Error: object " + objectId + " is not of the same type " + type);
        System.exit(1);
      }

      if (object instanceof GitBlob) {
        System.out.write(((GitBlob) object).getBlobData());
      } else {
        System.out.write(object.serialize());
      }
    } catch (ParseException e) {
      System.err.println("Error parsing arguments: " + e.getMessage());
      helper.printHelp("wyag cat-file -t TYPE OBJECT", options);
      System.exit(1);
    } catch (Exception e) {
      System.err.println("Error: " + e.getMessage());
      e.printStackTrace();
      System.exit(1);
    }
  }

  private static void cmdAdd(String[] commandArgs) {
    // TODO Auto-generated method stub
    throw new UnsupportedOperationException("Unimplemented method 'cmdAdd'");
  }

  private static void logGraphviz(GitRepository repo, String sha, Set<String> seen) {
    if (seen.contains(sha)) {
      return;
    }

    seen.add(sha);

    GitObject object = GitObjectUtil.objectRead(repo, sha);
    if (!(object instanceof GitCommit)) {
      throw new RuntimeException("Error: " + sha + " is not a commit!!");
    }

    GitCommit commit = (GitCommit) object;
    String message = commit.getMessage().trim();

    if (message.contains("\n")) {
      message = message.substring(0, message.indexOf("\n"));
    }

    message = message.replace("\\", "\\\\").replace("\"", "\\\"");

    System.out.println(" c_" + sha + "[label=\"" + sha.substring(0, 7) + ": " + message + "\"]");

    if (!commit.getType().equals("commit")) {
      throw new RuntimeException("Error expected commit, got " + commit.getType());
    }

    List<String> parents = commit.getParent();

    if (parents.isEmpty()) {
      return;
    }

    for (String parent : parents) {
      System.out.println(" c_" + sha + " -> c_" + parent + ";");
      logGraphviz(repo, parent, seen);
    }
  }
}
