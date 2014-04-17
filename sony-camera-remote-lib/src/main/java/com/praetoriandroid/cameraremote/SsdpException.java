package com.praetoriandroid.cameraremote;

public class SsdpException extends RpcException {
    private static final long serialVersionUID = 1410831473745729216L;

    public SsdpException() {
    }

    public SsdpException(String message) {
        super(message);
    }

    public SsdpException(String message, Throwable cause) {
        super(message, cause);
    }

    public SsdpException(Throwable cause) {
        super(cause);
    }
}
