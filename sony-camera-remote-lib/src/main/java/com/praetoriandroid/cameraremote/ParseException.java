package com.praetoriandroid.cameraremote;

public class ParseException extends RpcException {
    private static final long serialVersionUID = 1625818761965314252L;

    public ParseException() {
    }

    public ParseException(String message) {
        super(message);
    }

    public ParseException(String message, Throwable cause) {
        super(message, cause);
    }

    public ParseException(Throwable cause) {
        super(cause);
    }
}
