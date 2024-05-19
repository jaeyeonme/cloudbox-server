package io.jaeyeon.cloudboxserver.exception;

public class FileProcessingException extends NumbleMyBoxException {

  public FileProcessingException(ErrorCode errorCode) {
    super(errorCode);
  }
}
