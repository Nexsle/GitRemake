package com.GitRemake;

public class GitBlob extends GitObject {
  private byte[] blobData;

  public GitBlob(byte[] blobData) {
    super(blobData);
  }

  @Override
  public byte[] serialize() {
    return blobData;
  }

  @Override
  public void deserialize(byte[] data) {
    this.blobData = data;
  }

  @Override
  public void init() {
    this.blobData = new byte[0];
  }

  @Override
  public String getType() {
    return "blob";
  }

  public byte[] getBlobData() {
    return blobData;
  }
}
