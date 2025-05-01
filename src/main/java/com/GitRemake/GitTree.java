package com.GitRemake;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HexFormat;
import java.util.List;

public class GitTree extends GitObject {
  private List<GitTreeEntry> entries;

  public GitTree(byte[] data) {
    super(data);
  }

  @Override
  public byte[] serialize() {
    List<GitTreeEntry> sortedEntries = new ArrayList<>(entries);
    sortedEntries.sort(
        (a, b) -> {
          String aCompares = a.isTree() ? a.getPath() + "/" : a.getPath();
          String bCompares = b.isTree() ? b.getPath() + "/" : b.getPath();
          return aCompares.compareTo(bCompares);
        });

    ByteArrayOutputStream output = new ByteArrayOutputStream();

    try {
      for (GitTreeEntry entry : sortedEntries) {
        output.write(entry.getMode());

        output.write((byte) ' ');

        output.write(entry.getPath().getBytes(StandardCharsets.UTF_8));

        output.write((byte) 0);

        output.write(hexToByte(entry.getSha()));
      }
      return output.toByteArray();
    } catch (IOException e) {
      throw new RuntimeException("Error serializing the tree", e);
    }
  }

  @Override
  public void deserialize(byte[] data) {
    entries = new ArrayList<>();
    int position = 0;

    while (position < data.length) {
      int spacePos = findNextByte(data, (byte) ' ', position);
      if (spacePos == -1 || spacePos - position > 6) {
        throw new RuntimeException("Invalid tree format: mode too long or missing");
      }

      byte[] mode = new byte[spacePos - position];
      System.arraycopy(data, position, mode, 0, mode.length);

      // Git sometimes uses 5 bytes
      // so we normalize to 6 bytes
      if (mode.length == 5) {
        byte[] normalizeMode = new byte[6];
        normalizeMode[0] = (byte) '0';
        System.arraycopy(mode, 0, normalizeMode, 1, 5);
        mode = normalizeMode;
      }

      // find the null behind the spacePos
      int nullPos = findNextByte(data, (byte) 0, spacePos + 1);

      if (nullPos == -1) {
        throw new RuntimeException("Invalid tree format: path without null terminator");
      }

      byte[] pathBytes = new byte[nullPos - (spacePos + 1)];
      System.arraycopy(data, spacePos + 1, pathBytes, 0, pathBytes.length);
      String path = new String(pathBytes, StandardCharsets.UTF_8);

      // ensures that there are atleast 20 bytes of the SHA-1
      if (nullPos + 21 > data.length) {
        throw new RuntimeException("Invalid tree format: truncated SHA-1");
      }

      byte[] rawSha = new byte[20];
      System.arraycopy(data, nullPos + 1, rawSha, 0, 20);
      String sha = bytesToHex(rawSha);

      entries.add(new GitTreeEntry(mode, path, sha));

      position = nullPos + 21;
    }
  }

  @Override
  public void init() {
    entries = new ArrayList<>();
  }

  @Override
  public String getType() {
    return "tree";
  }

  public List<GitTreeEntry> getEntries() {
    return new ArrayList<GitTreeEntry>(entries);
  }

  private byte[] hexToByte(String sha) {
    return HexFormat.of().parseHex(sha);
  }

  private String bytesToHex(byte[] rawSha) {
    return HexFormat.of().formatHex(rawSha);
  }

  private int findNextByte(byte[] raw, byte target, int position) {
    for (int i = position; i < raw.length; i++) {
      if (raw[i] == target) {
        return i;
      }
    }
    return -1;
  }
}
