package io.jaeyeon.numblemybox.exception;

public class NoSuchElementException extends NumbleMyBoxException {

  public NoSuchElementException(ErrorCode errorCode) {
    super(errorCode);
  }
}
