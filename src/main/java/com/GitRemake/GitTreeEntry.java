package com.GitRemake;


public class GitTreeEntry {
  private final byte[] mode; // file mode and permission
  private final String path; // File/directory name
  private final String sha; // Hash reference

  public GitTreeEntry(byte[] mode, String path, String sha) {
    this.mode = mode;
    this.path = path;
    this.sha = sha;
  }

  public byte[] getMode() {
    return mode;
  }

  public String getPath() {
    return path;
  }

  public String getSha() {
    return sha;
  }

  public boolean isTree() {
    return mode.toString().startsWith("04");
  }

  public boolean isFile() {
    return mode.toString().startsWith("10");
  }

  public boolean isSymLink() {
    return mode.toString().startsWith("12");
  }
}
