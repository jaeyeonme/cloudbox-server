package io.jaeyeon.cloudboxserver.common;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class FileUtility {

  public static boolean exists(Path path) {
    return Files.exists(path);
  }

  public static String probeContentType(Path path) throws IOException {
    return Files.probeContentType(path);
  }

  public static void createDirectories(Path path) throws IOException {
    Files.createDirectories(path);
  }

  public static String createFilePath(String fileName) {
    return Paths.get(fileName).toString();
  }

  public static void deleteFile(Path path) throws IOException {
    Files.deleteIfExists(path);
  }
}
