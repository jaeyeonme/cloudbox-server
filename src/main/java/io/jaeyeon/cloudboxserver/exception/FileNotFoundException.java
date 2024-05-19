package io.jaeyeon.cloudboxserver.exception;

public class FileNotFoundException extends NumbleMyBoxException {
  public FileNotFoundException(ErrorCode errorCode) {
    super(errorCode);
  }
}
