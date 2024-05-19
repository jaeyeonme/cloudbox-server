package io.jaeyeon.cloudboxserver.exception;

public class FileServiceException extends NumbleMyBoxException {
  public FileServiceException(ErrorCode errorCode) {
    super(errorCode);
  }
}
