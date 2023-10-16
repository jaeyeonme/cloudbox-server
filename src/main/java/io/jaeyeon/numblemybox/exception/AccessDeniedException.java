package io.jaeyeon.numblemybox.exception;

public class AccessDeniedException extends NumbleMyBoxException {

	public AccessDeniedException(ErrorCode errorCode) {
		super(errorCode);
	}
}
