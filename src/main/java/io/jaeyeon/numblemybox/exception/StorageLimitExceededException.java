package io.jaeyeon.numblemybox.exception;

public class StorageLimitExceededException extends NumbleMyBoxException {
  public StorageLimitExceededException(ErrorCode errorCode) {
    super(errorCode);
  }
}
