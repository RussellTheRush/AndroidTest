package com.example.webserver;

public class ResponseException extends Exception {
	private Response.Status mStatus;

	public ResponseException(Response.Status status, String msg) {
		super(msg);
		mStatus = status;
	}

	public ResponseException(Response.Status status, String msg, Exception e) {
		super(msg, e);
		mStatus = status;
	}

	public Response.Status getStatus() {
		return mStatus;
	}
}
