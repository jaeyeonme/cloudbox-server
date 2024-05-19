package io.jaeyeon.cloudboxserver.exception;

import lombok.Getter;

@Getter
public class NumbleMyBoxException extends RuntimeException {

  private ErrorCode errorCode;

  public NumbleMyBoxException(ErrorCode errorCode) {
    super(errorCode.getMessage());
    this.errorCode = errorCode;
  }
}
