package com.praetoriandroid.cameraremote;

public class InvalidDataFormatException extends ParseException {
    private static final long serialVersionUID = -4574433030476745933L;

    public InvalidDataFormatException() {
    }

    public InvalidDataFormatException(String message) {
        super(message);
    }

    public InvalidDataFormatException(String message, Throwable cause) {
        super(message, cause);
    }

    public InvalidDataFormatException(Throwable e) {
        super(e);
    }
}
