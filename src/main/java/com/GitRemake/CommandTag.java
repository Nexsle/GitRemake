package com.GitRemake;

import java.io.IOException;
import java.time.Instant;
import java.time.ZonedDateTime;
import java.util.Map;

public class CommandTag {
  public static void listTag(GitRepository repo) throws IOException {
    Map<String, Object> refsList = GitRefUtil.refList(repo, "refs/tags");

    if (refsList.isEmpty()) {
      System.out.println("No tags were found");
      return;
    }

    for (String tagName : refsList.keySet()) {
      System.out.println(tagName);
    }
  }

  // Example
  // object 6071c08bcb4757d8c89a30d9755d2466cef8c1de
  // type commit
  // tag v1.0
  // tagger John Doe <john@example.com> 1622541234 -0700
  //
  // This is the tag message explaining why this version is important.
  public static void createTag(
      GitRepository repo, String name, String targetObj, boolean createAnnotated, String message)
      throws IOException {
    String sha = GitObjectUtil.objectFind(repo, name, null);

    if (sha == null) {
      throw new IOException("Object not found: " + name);
    }

    if (createAnnotated) {
      GitTag tag = new GitTag(null);

      tag.setObject(sha);
      tag.setObjectType("commit");
      tag.setTagName(name);

      // get user info - real data would come from Git config
      // but we're just using place holder
      String userName = System.getProperty("user.name", "Unkown");
      String userEmail = userName.toLowerCase() + "@example.com";

      // format the time
      long timestamp = Instant.now().getEpochSecond();
      ZonedDateTime now = ZonedDateTime.now();
      int offsetMinutes = now.getOffset().getTotalSeconds() / 60;
      int hours = Math.abs(offsetMinutes) / 60;
      int minutes = Math.abs(offsetMinutes) % 60;
      String tzSign = offsetMinutes >= 0 ? "+" : "-";

      String tagger =
          String.format(
              "%s <%s> %d %s%02d%02d", userName, userEmail, timestamp, tzSign, hours, minutes);

      tag.setTagger(tagger);
      tag.setMessage(message);

      // write the tag object
      String tagSha = GitObjectUtil.objectWrite(repo, tag);

      GitRefUtil.refCreate(repo, "tags/" + name, tagSha);

      System.out.println("Created annotated tag: '" + name + "'");
    } else {
      // create a lighweight tag
      GitRefUtil.refCreate(repo, "tags/" + name, sha);

      System.out.println("Created lightweight tag: '" + name + "'");
    }
  }
}
