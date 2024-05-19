package io.jaeyeon.cloudboxserver.exception;

public class FileStorageException extends NumbleMyBoxException {
  public FileStorageException(ErrorCode errorCode) {
    super(errorCode);
  }
}
