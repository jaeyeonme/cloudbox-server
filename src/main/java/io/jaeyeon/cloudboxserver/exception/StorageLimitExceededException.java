package io.jaeyeon.cloudboxserver.exception;

public class StorageLimitExceededException extends NumbleMyBoxException {
  public StorageLimitExceededException(ErrorCode errorCode) {
    super(errorCode);
  }
}
