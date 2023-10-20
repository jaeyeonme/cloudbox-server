package io.jaeyeon.numblemybox.exception;

public class FileProcessingException extends NumbleMyBoxException {

  public FileProcessingException(ErrorCode errorCode) {
    super(errorCode);
  }
}
