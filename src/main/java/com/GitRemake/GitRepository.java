package com.GitRemake;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import org.ini4j.Ini;

public class GitRepository {
  private String worktree;
  private String gitdir;
  private Ini conf;

  public GitRepository(String worktree, boolean force) {
    this.worktree = worktree;
    this.gitdir = worktree + "/.git";

    // check if .git exists or force is true to see if we
    // are working in an invalid dir
    if (!(force || new File(gitdir).isDirectory())) {
      throw new IllegalArgumentException("Not a Git repository: " + worktree);
    }
    // storing the configuration
    conf = new Ini();
    String configPath = repoFile("config");

    if (configPath != null && new File(configPath).exists()) {
      try {
        conf.load(new FileReader(configPath));
      } catch (IOException e) {
        throw new RuntimeException("Error reading configuration" + e);
      }
      // if theres no config but force is false
      // meaning currently opening a repo, this is an error
    } else if (!force) {
      throw new IllegalArgumentException("Configuration file missing!!");
    }

    if (!force) {
      try {
        int version = Integer.parseInt(conf.get("core", "repositoryformatversion"));
        if (version != 0) {
          throw new IllegalStateException("Unsuported repositoryformatverison: " + version);
        }
      } catch (Exception e) {
        throw new IllegalStateException("Invalid configuration", e);
      }
    }
  }

  public String getWorkTree() {
    return worktree;
  }

  public String getGitDir() {
    return gitdir;
  }

  // Make a path to the repository's .git directory
  private String repoPath(String... paths) {
    // String resultPath = this.gitdir;
    Path result = Paths.get(this.gitdir);
    for (String path : paths) {
      result = result.resolve(path);
    }
    return result.toString();
  }

  public String repoDir(boolean mkdir, String... paths) {
    String path = repoPath(paths);

    File dirPath = new File(path);
    // checking if the path is a directory/exists
    if (dirPath.exists()) {
      if (dirPath.isDirectory()) {
        return path;
      } else {
        throw new IllegalStateException("This is not a valid directory path: " + path);
      }
    }

    // create the dir
    if (mkdir) {
      dirPath.mkdirs();
      return path;
    } else {
      return null;
    }
  }

  // simple method for when repoFile("config");
  public String repoFile(String... paths) {
    return repoFile(false, paths);
  }

  public String repoFile(boolean mkdir, String... paths) {
    if (paths.length == 0) {
      return null;
    }

    // Example: path is ["/refs", "/remote", "/origin", "/HEAD"]
    // then the last path will be a file instead of a dir
    String[] filePath = new String[paths.length - 1];
    System.arraycopy(paths, 0, filePath, 0, paths.length - 1);

    // check if the path exists
    if (repoDir(mkdir, filePath) == null) {
      return null;
    }

    return repoPath(paths);
  }

  /**
   * Creates a new Git repository at the specified path
   *
   * @param path The directory where the repository will be created
   * @return The newly created repository
   * @throws IllegalArgumentException If the path is invalid or not empty
   * @throws RuntimeException If repository files cannot be created
   */
  public static GitRepository repoCreate(String path) {
    GitRepository repo = new GitRepository(path, true);

    // Make sure the path actually exists or is an empty dir
    File worktree = new File(repo.getWorkTree());
    File gitdir = new File(repo.getGitDir());
    if (worktree.exists()) {
      if (!worktree.isDirectory()) {
        throw new IllegalArgumentException("Is not a directory: " + path);
      }
      if (gitdir.exists() && gitdir.list().length > 0) {
        throw new IllegalArgumentException("Directory is not empty: " + path);
      }
    } else {
      worktree.mkdirs();
    }
    repo.repoDir(true, "branches");
    repo.repoDir(true, "objects");
    repo.repoDir(true, "refs", "tags");
    repo.repoDir(true, "refs", "heads");

    try (FileWriter writer = new FileWriter(repo.repoFile(true, "description"))) {
      writer.write("Unnamed repository; edit this file 'description' to name the repository.\n");
    } catch (IOException e) {
      System.err.println("Error: " + e.getMessage());
      e.printStackTrace();
      throw new RuntimeException("Failed to create description file", e);
    }

    try (FileWriter writer = new FileWriter(repo.repoFile(true, "HEAD"))) {
      writer.write("ref: refs/heads/master\n");
    } catch (IOException e) {
      throw new RuntimeException("Failed to create HEAD file", e);
    }

    try (FileWriter writer = new FileWriter(repo.repoFile(true, "config"))) {
      Ini config = repoDefaultConfig();
      config.store(writer);
    } catch (IOException e) {
      throw new RuntimeException("Failed to create config file", e);
    }

    return repo;
  }

  private static Ini repoDefaultConfig() {
    Ini config = new Ini();
    config.put("core", "repositoryformatversion", "0");
    config.put("core", "filemode", "false");
    config.put("core", "bare", "false");

    return config;
  }

  public static GitRepository repoFind() {
    return repoFind(".");
  }

  public static GitRepository repoFind(String path) {
    return repoFind(path, true);
  }

  public static GitRepository repoFind(String path, boolean required) {
    Path searchPath = Paths.get(path);
    // check if we have reached the .git
    if (Files.isDirectory(searchPath.resolve(".git"))) {
      return new GitRepository(path, false);
    }

    Path parent = searchPath.getParent();

    if (parent == null) {
      // means theres no more parent
      // for this path
      if (required) {
        throw new IllegalArgumentException("No git directory.");
      } else {
        return null;
      }
    }
    return repoFind(parent.toString(), required);
  }
}
