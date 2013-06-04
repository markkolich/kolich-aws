package com.kolich.aws;

import com.kolich.common.KolichCommonException;

public class KolichAwsException extends KolichCommonException {

	private static final long serialVersionUID = -5593611166490432655L;

	public KolichAwsException(String message, Throwable cause) {
		super(message, cause);
	}
	
	public KolichAwsException(Throwable cause) {
		super(cause);
	}
	
	public KolichAwsException(String message) {
		super(message);
	}

}