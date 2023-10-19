package io.jaeyeon.numblemybox.common;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.zip.ZipOutputStream;
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

  public FileOutputStream createFileOutputStream(File file) throws IOException {
    return new FileOutputStream(file);
  }

  public ZipOutputStream createZipOutputStream(FileOutputStream fileOutputStream) {
    return new ZipOutputStream(fileOutputStream);
  }

  public InputStream newInputStream(Path path) throws IOException {
    return Files.newInputStream(path);
  }
}
