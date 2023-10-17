package io.jaeyeon.numblemybox.exception;

public class FileNotFoundException extends NumbleMyBoxException {
  public FileNotFoundException(ErrorCode errorCode) {
    super(errorCode);
  }
}
