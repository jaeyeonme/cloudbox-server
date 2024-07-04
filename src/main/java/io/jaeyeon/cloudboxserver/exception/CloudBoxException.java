package io.jaeyeon.cloudboxserver.exception;

import lombok.Getter;

@Getter
public sealed class CloudBoxException extends RuntimeException
    permits CloudBoxException.AccessDeniedException,
        CloudBoxException.FileDatabaseException,
        CloudBoxException.FileDatabaseSaveException,
        CloudBoxException.FileDeleteFailedException,
        CloudBoxException.FileNotFoundException,
        CloudBoxException.FileProcessingException,
        CloudBoxException.FileServiceException,
        CloudBoxException.FileStorageException,
        CloudBoxException.FileTransferException,
        CloudBoxException.FileUploadFailedException,
        CloudBoxException.NoSuchElementException,
        CloudBoxException.StorageLimitExceededException {

  private final ErrorCode errorCode;

  public CloudBoxException(ErrorCode errorCode) {
    super(errorCode.getMessage());
    this.errorCode = errorCode;
  }

  public static final class AccessDeniedException extends CloudBoxException {
    public AccessDeniedException(ErrorCode errorCode) {
      super(errorCode);
    }
  }

  public static final class FileDatabaseException extends CloudBoxException {
    public FileDatabaseException(ErrorCode errorCode) {
      super(errorCode);
    }
  }

  public static final class FileNotFoundException extends CloudBoxException {
    public FileNotFoundException(ErrorCode errorCode) {
      super(errorCode);
    }
  }

  public static final class FileProcessingException extends CloudBoxException {
    public FileProcessingException(ErrorCode errorCode) {
      super(errorCode);
    }
  }

  public static final class FileServiceException extends CloudBoxException {
    public FileServiceException(ErrorCode errorCode) {
      super(errorCode);
    }
  }

  public static final class FileStorageException extends CloudBoxException {
    public FileStorageException(ErrorCode errorCode) {
      super(errorCode);
    }
  }

  public static final class NoSuchElementException extends CloudBoxException {
    public NoSuchElementException(ErrorCode errorCode) {
      super(errorCode);
    }
  }

  public static final class StorageLimitExceededException extends CloudBoxException {
    public StorageLimitExceededException(ErrorCode errorCode) {
      super(errorCode);
    }
  }

  public static final class FileTransferException extends CloudBoxException {
    public FileTransferException(ErrorCode errorCode) {
      super(errorCode);
    }
  }

  public static final class FileDatabaseSaveException extends CloudBoxException {
    public FileDatabaseSaveException(ErrorCode errorCode) {
      super(errorCode);
    }
  }

  public static final class FileUploadFailedException extends CloudBoxException {
    public FileUploadFailedException(ErrorCode errorCode) {
      super(errorCode);
    }
  }

  public static final class FileDeleteFailedException extends CloudBoxException {
    public FileDeleteFailedException(ErrorCode errorCode) {
      super(errorCode);
    }
  }
}
