package com.GitRemake;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.zip.DataFormatException;
import java.util.zip.Deflater;
import java.util.zip.Inflater;

public class GitObjectUtil {

  // reads the object from inside the repository by its SHA-1 hash.
  public static GitObject objectRead(GitRepository repo, String sha) {
    // get the path to the object "file" inside the objects dir
    String path = repo.repoFile("objects", sha.substring(0, 2), sha.substring(2));
    // check if valid path
    if (path == null || !new File(path).isFile()) {
      return null;
    }
    try {
      byte[] rawData = readAndDecompress(path);

      ObjectInfo info = parseHeader(rawData);

      return createObject(info.type, info.content);
    } catch (IOException e) {
      throw new RuntimeException("Error reading object " + sha, e);
    }
  }

  public static String objectWrite(GitRepository repo, GitObject object) {
    byte[] data = object.serialize();

    // Format: <type> <size>/0<content>
    String header = object.getType() + " " + data.length;
    byte[] headerBytes = header.getBytes();

    byte[] result = new byte[headerBytes.length + 1 + data.length];
    // we now add the header part: <type> <size>
    System.arraycopy(headerBytes, 0, result, 0, headerBytes.length);
    result[headerBytes.length] = 0; // now we add the null "/0" byte
    // and finally the content
    System.arraycopy(data, 0, result, headerBytes.length + 1, data.length);

    // compute the hash after we have the header + content
    String sha = computeSha1(result);

    if (repo != null) {
      // Create the file path to the object
      // Example the path to e673d1b7eaa0aa01b5bc2442d570a765bdaae751 is
      // .git/objects/e6/73d1b7eaa0aa01b5bc2442d570a765bdaae751
      String path = repo.repoFile(true, "objects", sha.substring(0, 2), sha.substring(2));

      File objectFile = new File(path);
      // only write if file does not exist aka the hash is new
      // because there is no modify in Git
      // when the content change the hash changes also resulting in a new file
      if (!objectFile.exists()) {
        try {
          byte[] compressed = compress(result);
          Files.write(objectFile.toPath(), compressed);
        } catch (IOException e) {
          throw new RuntimeException("Error writing object " + sha, e);
        }
      }
    }
    return sha;
  }

  // create the hashing for Git's objects
  private static String computeSha1(byte[] data) {
    try {
      MessageDigest md = MessageDigest.getInstance("SHA-1");
      byte[] hashedBytes = md.digest(data);

      StringBuilder hexString = new StringBuilder();
      for (byte b : hashedBytes) {
        // converting the bytes into hexidecimal and shortening it with the AND 0xff
        String hex = Integer.toHexString(0xff & b);
        // if it was the first char in a string like "5" then we make it into "05" to get a valid
        // hexidecimal string
        if (hex.length() == 1) {
          hexString.append("0");
        }
        hexString.append(hex);
      }
      return hexString.toString();
    } catch (NoSuchAlgorithmException e) {
      throw new RuntimeException("SHA-1 algorithm could not be access ", e);
    }
  }

  private static byte[] compress(byte[] data) {
    // deflater to compress the data
    Deflater deflater = new Deflater();
    deflater.setInput(data);
    deflater.finish();

    ByteArrayOutputStream output = new ByteArrayOutputStream(data.length);
    // create a buffer to store
    byte[] buffer = new byte[1024];
    while (!deflater.finished()) {
      // compress as many bytes as possible and the write it to the output
      int count = deflater.deflate(buffer);
      output.write(buffer, 0, count);
    }
    try {
      output.close();
    } catch (IOException e) {
      throw new RuntimeException("Error closing output stream", e);
    }
    return output.toByteArray();
  }

  private static byte[] readAndDecompress(String path) throws IOException {
    // Verify the file exist and read
    File objectFile = new File(path);
    byte[] compressed = Files.readAllBytes(objectFile.toPath());

    Inflater inflater = new Inflater();
    inflater.setInput(compressed);

    // Create a buffer to hold the decomp data
    byte[] buffer = new byte[8192]; // Give 8KB
    int dataLength = 0;

    try {
      // start decompressing the data
      dataLength = inflater.inflate(buffer);
    } catch (DataFormatException e) {
      throw new IOException("Error decompressing object: " + e.getMessage());
    }
    // return only the part that has data instead of the whole buffer
    return Arrays.copyOfRange(buffer, 0, dataLength);
  }

  private static ObjectInfo parseHeader(byte[] rawData) {
    // find the white space between the type and size
    int whiteSpacePos = findNextByte(rawData, (byte) ' ', 0);
    // while (rawData[whiteSpacePos] != (byte) ' ' && whiteSpacePos < rawData.length) {
    //   whiteSpacePos++;
    // }

    if (whiteSpacePos >= rawData.length) {
      throw new RuntimeException("Malformed object: no separator found");
    }

    // Extract the type
    String type = new String(Arrays.copyOfRange(rawData, 0, whiteSpacePos));

    // Find the size between type and null char
    // int nullPos = whiteSpacePos + 1;
    // while (rawData[nullPos] != 0 && nullPos < rawData.length) {
    //   nullPos++;
    // }
    int nullPos = findNextByte(rawData, (byte) 0, whiteSpacePos + 1);

    if (nullPos >= rawData.length) {
      throw new IllegalArgumentException("Malformed object: no null terminator found");
    }

    // Extract the size
    String sizeStr = new String(Arrays.copyOfRange(rawData, whiteSpacePos + 1, nullPos));
    int size = Integer.parseInt(sizeStr);

    // Extract the content (everything behind the size)
    byte[] content = Arrays.copyOfRange(rawData, nullPos + 1, rawData.length);

    // Validate that content size matches the specify size
    if (content.length != size) {
      throw new IllegalStateException(
          "Object size mismatch: Expected " + content.length + " but got " + size);
    }
    return new ObjectInfo(type, content);
  }

  public static GitObject createObject(String type, byte[] data) {
    switch (type) {
      case "blob":
        return new GitBlob(data);
      case "commit":
        return new GitCommit(data);
      case "tree":
        return new GitTree(data);
      case "tag":
        return new GitTag(data);
      default:
        throw new IllegalArgumentException("Unknown object type: " + type);
    }
  }

  // TODO: implement the rest of the method
  public static String objectFind(GitRepository repo, String name, String type) {
    if (name.equals("HEAD")) {
      try {
        return GitRefUtil.refResolve(repo, "HEAD");
      } catch (IOException e) {
        return null;
      }
    }

    // check if name is a full hash
    // if so just return name
    if (name.matches("[0-9a-f]{40}")) return name;

    // check if abbrerviated hash (atleast 4 hex char)
    if (name.matches("[0-9a-f]{4,39}")) {
      // try to find the object with the prefix
      String prefix = name.substring(0, 2);
      String subfix = name.substring(2);

      try {
        String dirPath = repo.repoDir(false, "objects/" + prefix);
        Path objectDir = Paths.get(dirPath);
        if (Files.exists(objectDir)) {
          try (Stream<Path> path = Files.list(objectDir)) {
            List<String> matches =
                path.map(p -> p.getFileName().toString())
                    .filter(f -> f.startsWith(subfix))
                    .map(f -> prefix + f)
                    .collect(Collectors.toList());

            if (matches.size() == 1) {
              return matches.get(0);
            } else if (matches.size() > 1) {
              throw new IOException("Ambiguous has prefix: " + name);
            }
          }
        }

      } catch (IOException e) {
        return null;
      }
    }
    // THIS IS FOR WHEN USE ENTER A STRING (reach when not a hash)
    // Ex: git show main
    // Check if it's a tag
    try {
      String tagRef = GitRefUtil.refResolve(repo, "refs/tags/" + name);
      if (tagRef != null) {
        return tagRef;
      }
    } catch (IOException e) {
      // Ignore and continue
    }

    // Check if it's a branch
    try {
      String branchRef = GitRefUtil.refResolve(repo, "refs/heads/" + name);
      if (branchRef != null) {
        return branchRef;
      }
    } catch (IOException e) {
      // Ignore and continue
    }

    // Check if it's a remote branch
    try {
      String remoteRef = GitRefUtil.refResolve(repo, "refs/remotes/" + name);
      if (remoteRef != null) {
        return remoteRef;
      }
    } catch (IOException e) {
      // Ignore and continue
    }

    // If we get here, we couldn't find the object
    return null;
  }

  private static int findNextByte(byte[] raw, byte target, int position) {
    for (int i = position; i < raw.length; i++) {
      if (raw[i] == target) {
        return i;
      }
    }
    return -1;
  }
}
