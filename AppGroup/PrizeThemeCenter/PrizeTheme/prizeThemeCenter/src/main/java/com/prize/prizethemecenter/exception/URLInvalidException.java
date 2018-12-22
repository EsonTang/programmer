package com.prize.prizethemecenter.exception;

import java.io.IOException;

public class URLInvalidException extends IOException {
	private static final long serialVersionUID = 1L;

	public URLInvalidException() {
		super();
	}

	public URLInvalidException(String detailMessage) {
		super(detailMessage);
	}

}
