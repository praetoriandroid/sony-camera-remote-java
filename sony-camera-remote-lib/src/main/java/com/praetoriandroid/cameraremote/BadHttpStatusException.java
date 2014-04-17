package com.praetoriandroid.cameraremote;

import java.io.IOException;

public class BadHttpStatusException extends IOException {
    private static final long serialVersionUID = 5911995982406477578L;
    private int httpStatus;

    public BadHttpStatusException(int httpStatusCode, String httpStatusMessage) {
        super(httpStatusCode + ": " + httpStatusMessage);
        this.httpStatus = httpStatusCode;
    }

    public int getStatus() {
        return httpStatus;
    }

    @Override
    public String toString() {
        return getMessage();
    }
}
