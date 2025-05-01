package com.GitRemake;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

public class GitCheckout {
  public static void checkout(GitRepository repo, String commitOrTreeSha, String path)
      throws IOException {
    GitObject obj = GitObjectUtil.objectRead(repo, commitOrTreeSha);

    if (obj instanceof GitCommit) {
      GitCommit commit = (GitCommit) obj;
      String treeSha = commit.getTree();
      obj = GitObjectUtil.objectRead(repo, treeSha);
    }

    if (!(obj instanceof GitTree)) {
      throw new IllegalArgumentException("Not a tree object: " + commitOrTreeSha);
    }

    // Verify that the directory is empty
    File directory = new File(path);
    if (directory.exists()) {
      if (!directory.isDirectory()) {
        throw new IllegalArgumentException("Not a directory: " + path);
      }

      // check the directory is empty
      String[] files = directory.list();
      if (files.length > 0 && files != null) {
        throw new IllegalArgumentException("Directory not empty: " + path);
      }
    } else {
      // create directory if doesnt exist
      if (!directory.mkdirs()) {
        throw new IOException("Error creating directory: " + path);
      }
    }
    treeCheckout(repo, (GitTree) obj, Paths.get(path).toAbsolutePath());
  }

  private static void treeCheckout(GitRepository repo, GitTree tree, Path absolutePath) {
    List<GitTreeEntry> entries = tree.getEntries();

    for (GitTreeEntry entry : entries) {
      Path entryPath = absolutePath.resolve(entry.getPath());

      try {
        if (entry.isTree()) {
          Files.createDirectories(entryPath);

          GitObject obj = GitObjectUtil.objectRead(repo, entry.getSha());
          if (!(obj instanceof GitTree)) {
            throw new IllegalArgumentException("Not a tree object: " + entry.getSha());
          }

          treeCheckout(repo, (GitTree) obj, entryPath);
          // blob then write the content
        } else if (entry.isFile()) {
          GitObject obj = GitObjectUtil.objectRead(repo, entry.getSha());

          if (!(obj instanceof GitBlob)) {
            throw new IllegalArgumentException("Not a blob object: " + entry.getSha());
          }

          GitBlob blob = (GitBlob) obj;
          byte[] content = blob.getBlobData();

          Files.createDirectories(entryPath.getParent());

          Files.write(entryPath, content);
        } else if (entry.isSymLink()) {
          GitObject obj = GitObjectUtil.objectRead(repo, entry.getSha());

          if (!(obj instanceof GitBlob)) {
            throw new IllegalArgumentException("Not a blob object for symlink: " + entry.getSha());
          }

          GitBlob blob = (GitBlob) obj;
          String target = new String(blob.getBlobData());

          Files.createDirectories(entryPath.getParent());

          Files.createSymbolicLink(entryPath, Paths.get(target));
        }

      } catch (IOException e) {
        System.err.println("Error creating directory: " + entryPath.toString());
      }
    }
  }
}
