package io.jaeyeon.cloudboxserver.common;

import io.jaeyeon.cloudboxserver.exception.ErrorCode;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;

import static io.jaeyeon.cloudboxserver.exception.CloudBoxException.*;

@Slf4j
public class FileUtility {

  public static boolean exists(Path path) {
    try {
      return Files.exists(path);
    } catch (SecurityException e) {
      log.error("Security exception while checking if path exists: {}", path, e);
      return false;
    }
  }

  public static String probeContentType(Path path) {
    try {
      return Files.probeContentType(path);
    } catch (IOException e) {
      log.error("IOException while probing content type for path: {}", path, e);
      throw new FileProcessingException(ErrorCode.FILE_TYPE_DETERMINATION_FAILED);
    } catch (SecurityException e) {
      log.error("Security exception while probing content type for path: {}", path, e);
      throw new FileProcessingException(ErrorCode.ACCESS_DENIED);
    }
  }

  public static void createDirectories(Path path) {
    try {
      Files.createDirectories(path);
    } catch (IOException e) {
      log.error("IOException while creating directories for path: {}", path, e);
      throw new FileProcessingException(ErrorCode.DIRECTORY_CREATION_FAILED);
    }
  }

  public static String createFilePath(String fileName) {
    try {
      return Paths.get(fileName).toString();
    } catch (InvalidPathException e) {
      log.error("InvalidPathException while creating file path for fileName: {}", fileName, e);
      throw new FileProcessingException(ErrorCode.INVALID_PATH);
    }
  }

  public static void deleteFile(Path path) {
    try {
      Files.deleteIfExists(path);
    } catch (IOException e) {
      log.error("IOException while deleting file for path: {}", path, e);
      throw new FileStorageException(ErrorCode.FILE_DELETE_FAILED);
    } catch (SecurityException e) {
      log.error("Security exception while deleting file for path: {}", path, e);
      throw new FileStorageException(ErrorCode.ACCESS_DENIED);
    }
  }
}
