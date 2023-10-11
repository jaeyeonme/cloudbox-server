package io.jaeyeon.numblemybox.exception;

public class UnAuthenticatedAccessException extends NumbleMyBoxException {
  public UnAuthenticatedAccessException(ErrorCode errorCode) {
    super(errorCode);
  }
}
