package io.jaeyeon.numblemybox.common;

import io.jaeyeon.numblemybox.exception.ErrorCode;
import io.jaeyeon.numblemybox.exception.FileServiceException;
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

@Slf4j
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

  public FileOutputStream createFileOutputStream(File file) throws IOException {
    return new FileOutputStream(file);
  }

  public ZipOutputStream createZipOutputStream(FileOutputStream fileOutputStream) {
    return new ZipOutputStream(fileOutputStream);
  }

  public InputStream newInputStream(Path path) throws IOException {
    return Files.newInputStream(path);
  }

  public String extractFileExtension(String filename) {
    return filename.substring(filename.lastIndexOf(".") + 1);
  }

  public String createFilePath(String folderPath, String fileName) {
    return Paths.get(folderPath, fileName).toString();
  }

  public void deleteFile(Path path) throws IOException {
    Files.deleteIfExists(path);
  }

  public void createDirectoryIfNotExists(String path) {
    File directory = new File(path);
    if (!directory.exists()) {
      // 디렉토리가 없으면 생성합니다.
      boolean dirCreated = directory.mkdirs();
      if (!dirCreated) {
        log.error("Failed to create directory: {}", path);
        throw new FileServiceException(ErrorCode.DIRECTORY_CREATION_FAILED);
      }
    }
  }
}
