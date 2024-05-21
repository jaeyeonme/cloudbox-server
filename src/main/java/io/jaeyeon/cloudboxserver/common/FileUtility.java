package io.jaeyeon.cloudboxserver.common;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import org.springframework.stereotype.Component;

@Component
public class FileUtility {

  public boolean exists(Path path) {
    return Files.exists(path);
  }

  public String probeContentType(Path path) throws IOException {
    return Files.probeContentType(path);
  }

  public void createDirectories(Path path) throws IOException {
    Files.createDirectories(path);
  }

  public String createFilePath(String fileName) {
    return Paths.get(fileName).toString();
  }

  public void deleteFile(Path path) throws IOException {
    Files.deleteIfExists(path);
  }
}
