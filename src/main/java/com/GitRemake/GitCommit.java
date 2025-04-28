package com.GitRemake;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class GitCommit extends GitObject {
  private Map<String, Object> kvlm;

  public GitCommit(byte[] data) {
    super(data);
  }

  /**
   * Create a new commit with specific properties
   *
   * @param tree SHA-1 of the tree object
   * @param parents List of parent commit SHA-1s (empty for initial commit)
   * @param author Author info in format "Name <email> timestamp timezone"
   * @param committer Committer info in same format as author
   * @param message Commit message
   */
  public GitCommit(
      String tree, List<String> parents, String author, String committer, String message) {
    super(null);

    kvlm.put("tree", tree.getBytes(StandardCharsets.UTF_8));

    if (parents != null && !parents.isEmpty()) {
      if (parents.size() == 1) {
        kvlm.put("parent", parents.get(0).getBytes(StandardCharsets.UTF_8));
      } else {
        List<byte[]> parentByte = new ArrayList<>();
        for (String parent : parents) {
          parentByte.add(parent.getBytes(StandardCharsets.UTF_8));
        }
        kvlm.put("parent", parentByte);
      }
    }

    kvlm.put("author", author.getBytes(StandardCharsets.UTF_8));
    kvlm.put("commiter", committer.getBytes(StandardCharsets.UTF_8));

    kvlm.put(null, message.getBytes(StandardCharsets.UTF_8));
  }

  @Override
  public byte[] serialize() {
    return serializeKvlm(kvlm);
  }

  @Override
  public void deserialize(byte[] data) {
    kvlm = parseKvlm(data);
  }

  @Override
  public void init() {
    kvlm = new LinkedHashMap<>();
  }

  @Override
  public String getType() {
    return "commit";
  }

  public String getTree() {
    byte[] treeValue = (byte[]) kvlm.get("tree");
    if (treeValue == null) return null;
    return new String(treeValue, StandardCharsets.UTF_8);
  }

  public List<String> getParent() {
    Object parentValue = kvlm.get("parent");
    List<String> result = new ArrayList<>();

    if (parentValue instanceof List) {
      @SuppressWarnings("unchecked")
      List<byte[]> parentList = (List<byte[]>) parentValue;
      for (byte[] parent : parentList) {
        result.add(new String(parent, StandardCharsets.UTF_8));
      }
    } else {
      result.add(new String((byte[]) parentValue, StandardCharsets.UTF_8));
    }
    return result;
  }

  public String getAuthor() {
    byte[] authorValue = (byte[]) kvlm.get("author");
    if (authorValue == null) return null;
    return new String(authorValue, StandardCharsets.UTF_8);
  }

  public String getMessage() {
    byte[] message = (byte[]) kvlm.get(null);
    if (message == null) return "";
    return new String(message, StandardCharsets.UTF_8);
  }

  @SuppressWarnings("unchecked")
  private byte[] serializeKvlm(Map<String, Object> kvlm) {
    ByteArrayOutputStream output = new ByteArrayOutputStream();
    try {
      for (String key : kvlm.keySet()) {
        if (key == null) {
          continue;
        }
        Object value = kvlm.get(key);
        List<byte[]> valueList = new ArrayList<>();
        // check if the value is a list
        if (value instanceof List) {
          valueList = (List<byte[]>) value;
        } else if (value instanceof byte[]) {
          valueList.add((byte[]) value);
        }
        for (byte[] b : valueList) {
          // writing the key + space
          output.write(key.getBytes(StandardCharsets.UTF_8));
          output.write((byte) ' ');

          // replace newLine with newLine + space
          int position = 0;
          while (position < b.length) {
            int newLinePos = findNextByte(b, (byte) '\n', position);

            // only write if theres a newLine
            if (newLinePos != -1) {
              // includes the \n too
              output.write(b, position, newLinePos - position + 1);
              output.write((byte) ' ');
              position = newLinePos + 1;
            } else {
              // no more new lines = write until the end
              output.write(b, position, b.length - position);
              break;
            }
          }

          // add a final \n after the key value pairs
          output.write((byte) '\n');
        }
      }
      // add the blank line then message
      output.write((byte) '\n');
      output.write((byte[]) kvlm.get(null));

      return output.toByteArray();
    } catch (IOException e) {
      throw new RuntimeException("Error serializing commit", e);
    }
  }

  private Map<String, Object> parseKvlm(byte[] raw) {
    Map<String, Object> result = new LinkedHashMap<>();
    int position = 0;

    while (position < raw.length) {
      int spacePos = findNextByte(raw, (byte) ' ', position);

      int newLinePos = findNextByte(raw, (byte) '\n', position);

      // find out if we are at the end message because theres a blank line
      if (spacePos > newLinePos || spacePos == -1) {
        byte[] message = new byte[raw.length - (newLinePos + 1)];
        System.arraycopy(raw, newLinePos + 1, message, 0, message.length);
        result.put(null, message);
        break;
      }

      // get the key (tree, parent, author, committer,...)
      byte[] keyBytes = new byte[spacePos - position];
      System.arraycopy(raw, position, keyBytes, 0, keyBytes.length);
      String key = new String(keyBytes, StandardCharsets.UTF_8);

      // Now we find the end position of the value part
      int endPos = findEndValue(raw, newLinePos + 1);

      // Extract and process the value(handling continuous lines)
      byte[] valueBytes = extractValue(raw, spacePos + 1, endPos);

      // add to map (multiple values per key)
      addToMap(result, key, valueBytes);

      position = endPos + 1;
    }
    return result;
  }

  private int findNextByte(byte[] raw, byte target, int position) {
    for (int i = position; i < raw.length; i++) {
      if (raw[i] == target) {
        return i;
      }
    }
    return -1;
  }

  private int findEndValue(byte[] raw, int position) {
    int newLinePos = findNextByte(raw, (byte) '\n', position);
    // if cant find any more \n then at the end of file
    if (newLinePos == -1) {
      return raw.length;
    }

    // check if theres a space behind the new line indicating that theres a continuation
    // also that the newlinePos isnt the last character
    if (newLinePos + 1 < raw.length && raw[newLinePos + 1] == (byte) ' ') {
      // keep looking for the end
      return findEndValue(raw, newLinePos + 1);
    } else {
      return newLinePos;
    }
  }

  private byte[] extractValue(byte[] raw, int start, int end) {
    if (end <= start) {
      return new byte[0];
    }

    ByteArrayOutputStream output = new ByteArrayOutputStream();

    int startLine = start;
    boolean isFirstLine = true;

    while (startLine < end) {
      int lineEnd = findNextByte(raw, (byte) '\n', startLine);

      // check if this is the end of the file
      if (lineEnd == -1 || lineEnd > end) {
        lineEnd = end;
      }

      int copyStart = startLine;
      // only skip the space when its not the first line and if theres a space infront of the \n
      if (!isFirstLine && raw[copyStart] == (byte) ' ') {
        copyStart++;
      }

      if (lineEnd > copyStart) {
        output.write(raw, copyStart, lineEnd - copyStart);
      }

      // add the missing new line
      if (lineEnd < end) {
        output.write((byte) '\n');
      }

      // continue with next value line
      startLine = lineEnd + 1;
      isFirstLine = false;
    }

    return output.toByteArray();
  }

  private void addToMap(Map<String, Object> result, String key, byte[] valueBytes) {
    // check if map already has that value
    if (result.containsKey(key)) {
      Object existingValues = result.get(key);

      // check if value are a list or just byte[]
      if (existingValues instanceof byte[]) {
        // create a new list if dont
        List<byte[]> values = new ArrayList<>();
        values.add((byte[]) existingValues);
        values.add(valueBytes);
        result.put(key, values);
      } else if (existingValues instanceof List) {
        @SuppressWarnings("unchecked")
        // just add to the list if yes
        List<byte[]> values = (List<byte[]>) existingValues;
        values.add(valueBytes);
        result.put(key, values);
      }
    } else {
      // doesnt contain the key
      result.put(key, valueBytes);
    }
  }
}
