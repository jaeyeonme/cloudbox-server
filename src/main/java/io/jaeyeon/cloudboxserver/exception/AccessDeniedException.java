package io.jaeyeon.cloudboxserver.exception;

public class AccessDeniedException extends NumbleMyBoxException {

  public AccessDeniedException(ErrorCode errorCode) {
    super(errorCode);
  }
}
