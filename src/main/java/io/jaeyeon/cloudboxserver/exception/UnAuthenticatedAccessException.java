package io.jaeyeon.cloudboxserver.exception;

public class UnAuthenticatedAccessException extends NumbleMyBoxException {
  public UnAuthenticatedAccessException(ErrorCode errorCode) {
    super(errorCode);
  }
}
