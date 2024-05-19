package io.jaeyeon.cloudboxserver.common;

import io.jaeyeon.cloudboxserver.exception.ErrorCode;
import io.jaeyeon.cloudboxserver.exception.FileServiceException;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.zip.ZipOutputStream;
import lombok.extern.slf4j.Slf4j;
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

  public String createFilePath(String folderPath, String fileName) {
    return Paths.get(folderPath, fileName).toString();
  }

  public void deleteFile(Path path) throws IOException {
    Files.deleteIfExists(path);
  }
}
