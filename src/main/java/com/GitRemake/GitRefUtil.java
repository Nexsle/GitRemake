package com.GitRemake;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.TreeMap;

public class GitRefUtil {
  // Resolves the reference to its actuall SHA-1 hash value
  public static String refResolve(GitRepository repo, String reference) throws IOException {
    String pathStr = repo.repoFile(reference);
    Path path = Paths.get(pathStr);

    if (!Files.exists(path)) {
      return null;
    }

    String data = Files.readString(path, StandardCharsets.UTF_8).trim();

    // ex: ref: refs/heads/main
    if (data.startsWith("ref: ")) {
      return refResolve(repo, data.substring(5));
    } else {
      // direct reference (SHA-1)
      return data;
    }
  }

  /* The main form
     * {
    "heads": {
      "main": "6071c08...",
      "dev": "3fb74ec..."
    },
    "tags": {
      "v1.0": "d85a8f7..."
    }
  }
      */
  public static Map<String, Object> refList(GitRepository repo, String path) throws IOException {
    String refString;
    if (path == null) {
      refString = repo.repoDir(false, "refs");
    } else {
      refString = repo.repoFile(path);
    }

    Path refsPath = Paths.get(refString);

    Map<String, Object> refs = new TreeMap<>();

    // ensure we dont call it on file
    if (!Files.exists(refsPath) || !Files.isDirectory(refsPath)) {
      return refs;
    }
    // iterate through all the files
    Files.list(refsPath)
        .forEach(
            p -> {
              String name = p.getFileName().toString();

              try {
                if (Files.isDirectory(p)) {
                  String subPath = path == null ? "ref/" + name : path + "/" + name;
                  refs.put(name, refList(repo, subPath));
                } else {
                  String refPath = path == null ? "ref/" + name : path + "/" + name;
                  refs.put(name, refResolve(repo, refPath));
                }

              } catch (IOException e) {
                System.err.println("Error processing reference: " + p + " - " + e.getMessage());
              }
            });
    return refs;
  }

  public static void refCreate(GitRepository repo, String refName, String sha) throws IOException {
    String pathString = repo.repoFile("refs/" + refName);
    Path refFile = Paths.get(pathString);

    // make sure all the parent directory is created
    Files.createDirectories(refFile.getParent());

    // write the hash reference inside the file
    Files.writeString(refFile, sha + "\n", StandardCharsets.UTF_8);
  }
}
