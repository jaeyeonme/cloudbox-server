package io.jaeyeon.cloudboxserver.file.domain.entity;

import java.net.URLConnection;
import java.util.Arrays;

public enum FileType {
  IMAGE("image"),
  VIDEO("video"),
  AUDIO("audio"),
  DOCUMENT("application"),
  FOLDER("folder"),
  OTHER("other");

  private final String mine;

  FileType(String mine) {
    this.mine = mine;
  }

  public static FileType fromMine(String mine) {
    if (mine == null) {
      return OTHER;
    }
    return Arrays.stream(FileType.values())
        .filter(fileType -> mine.startsWith(fileType.mine))
        .findFirst()
        .orElse(OTHER);
  }

  public static FileType fromPath(String path) {
    return path.endsWith("/") ? FOLDER : fromMine(URLConnection.guessContentTypeFromName(path));
  }
}
