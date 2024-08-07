package io.jaeyeon.cloudboxserver.exception;

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
  INVALID_PASSWORD(HttpStatus.BAD_REQUEST, "M-008", "잘못된 비밀번호입니다."),

  /** Files */
  FILE_NOT_FOUND(HttpStatus.NOT_FOUND, "F-001", "파일이 존재하지 않습니다."),
  FILE_DOWNLOAD_FAILED(HttpStatus.BAD_REQUEST, "F-002", "파일 다운로드에 실패했습니다."),
  FILE_DATABASE_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "F-003", "파일 정보를 데이터베이스에 저장하는데 실패했습니다."),
  FILE_STORAGE_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "F-004", "파일 시스템에 파일을 저장하는데 실패했습니다."),
  FILE_TYPE_ERROR(HttpStatus.BAD_REQUEST, "F-005", "지원하지 않는 파일 형식입니다."),
  FILE_TYPE_DETERMINATION_FAILED(HttpStatus.BAD_REQUEST, "F-006", "파일 MIME 타입 결정에 실패했습니다."),
  ACCESS_DENIED(HttpStatus.UNAUTHORIZED, "F-007", "접근이 거부되었습니다."),
  FILE_UPLOAD_FAILED(HttpStatus.BAD_REQUEST, "F-008", "파일 업로드에 실패했습니다."),
  DUPLICATE_FILE_NAME(HttpStatus.BAD_REQUEST, "F-009", "파일 이름이 중복됩니다."),
  FILE_DELETE_FAILED(HttpStatus.BAD_REQUEST, "F-010", "파일 삭제에 실패했습니다."),
  FILE_PROCESSING_FAILED(HttpStatus.BAD_REQUEST, "F-011", "파일 처리에 실패했습니다."),
  FILE_EMPTY(HttpStatus.BAD_REQUEST, "F-012", "파일이 비어있습니다."),
  FILE_SIZE_EXCEEDED(HttpStatus.BAD_REQUEST, "F-013", "파일 크기가 초과되었습니다."),
  FILE_TRANSFER_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "F-014", "파일 전송에 실패했습니다."),
  FILE_DATABASE_SAVE_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "F-015", "파일을 데이터베이스에 저장하는데 실패했습니다."),
  INVALID_FILE_NAME(HttpStatus.BAD_REQUEST, "F-016", "파일 이름이 유효하지 않습니다."),
  INVALID_EXTENSION(HttpStatus.BAD_REQUEST, "F-017", "확장자가 유효하지 않습니다."),
  INVALID_CONTENT_TYPE(HttpStatus.BAD_REQUEST, "F-018", "콘텐츠 타입이 유효하지 않습니다."),
  INVALID_CONTENT_LENGTH(HttpStatus.BAD_REQUEST, "F-019", "콘텐츠 길이가 유효하지 않습니다."),
  INVALID_FILE(HttpStatus.BAD_REQUEST, "F-020", "유효하지 않은 파일입니다."),
  FILE_LIST_FAILED(HttpStatus.BAD_REQUEST, "F-021", "파일 목록을 불러오는데 실패했습니다."),

  /** Folder */
  FOLDER_NOT_FOUND(HttpStatus.NOT_FOUND, "F0-001", "폴더가 존재하지 않습니다."),
  DIRECTORY_CREATION_FAILED(HttpStatus.BAD_REQUEST, "F0-002", "디렉토리 생성에 실패했습니다."),

  /** Storage */
  STORAGE_LIMIT_EXCEEDED(HttpStatus.BAD_REQUEST, "S-001", "저장 공간이 부족합니다."),

  /** Common */
  INVALID_PATH(HttpStatus.BAD_REQUEST, "C-001", "잘못된 경로입니다."),
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
