package io.jaeyeon.numblemybox.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum ErrorCode {

  /** MEMBER */
  MEMBER_ALREADY_EXISTS(HttpStatus.BAD_REQUEST, "M-001", "이미 존재하는 회원입니다."),
  MEMBER_NOT_FOUND(HttpStatus.BAD_REQUEST, "M-002", "회원을 찾을 수 없습니다."),
  WRONG_PASSWORD(HttpStatus.BAD_REQUEST, "M-003", "잘못된 비밀번호입니다."),
  UNAUTHORIZED_MEMBER(HttpStatus.UNAUTHORIZED, "M-004", "현재 내 계정 정보가 존재하지 않습니다."),
  EMAIL_ALREADY_EXISTS(HttpStatus.BAD_REQUEST, "M-005", "이미 사용중인 이메일입니다."),
  IS_NOT_OWNER(HttpStatus.UNAUTHORIZED, "M-006", "작성자만 가능한 요청입니다."),
  UNAUTHENTICATED_ACCESS(HttpStatus.UNAUTHORIZED, "M-007", "인증되지 않은 사용자 입니다."),
  ;

  ErrorCode(HttpStatus httpStatus, String errorCode, String message) {
    this.httpStatus = httpStatus;
    this.errorCode = errorCode;
    this.message = message;
  }

  private HttpStatus httpStatus;
  private String errorCode;
  private String message;
}
