package com.GitRemake;

/**
 * Abstract base class for all Git objects (blob, commit, tree, tag). Note: In Git, "object" refers
 * to storage units, not OOP objects. Git objects are stored as files whose names are SHA-1 hashes
 * of their content.
 */
public abstract class GitObject {
  public GitObject(byte[] data) {
    // From existing data (like when reading an object from disk)
    // As a brand new empty object (like when creating a new blob)
    if (data != null) {
      deserialize(data);
    } else {
      init();
    }
  }

  // methods that subclass must implement
  // reads the content from data, a byte string and
  // converts it into meaningful representation for code
  // to process
  public abstract byte[] serialize();

  public abstract void deserialize(byte[] data);

  // Initialize a new empty object when creating object from scratch
  // Use when no exsiting data is provided to the constructor
  public abstract void init();

  public abstract String getType();
}
