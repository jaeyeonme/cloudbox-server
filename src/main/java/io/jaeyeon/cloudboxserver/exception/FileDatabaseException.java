package io.jaeyeon.cloudboxserver.exception;

public class FileDatabaseException extends NumbleMyBoxException {
  public FileDatabaseException(ErrorCode errorCode) {
    super(errorCode);
  }
}
