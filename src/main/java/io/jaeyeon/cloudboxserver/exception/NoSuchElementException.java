package io.jaeyeon.cloudboxserver.exception;

public class NoSuchElementException extends NumbleMyBoxException {

  public NoSuchElementException(ErrorCode errorCode) {
    super(errorCode);
  }
}
