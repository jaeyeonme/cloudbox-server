package io.jaeyeon.numblemybox.exception;

public class FileServiceException extends NumbleMyBoxException {
	public FileServiceException(ErrorCode errorCode) {
		super(errorCode);
	}
}
