package com.praetoriandroid.cameraremote;

public class RpcException extends Exception {
    private static final long serialVersionUID = 4502326939007154793L;

    public RpcException() {
    }

    public RpcException(String message) {
        super(message);
    }

    public RpcException(String message, Throwable cause) {
        super(message, cause);
    }

    public RpcException(Throwable cause) {
        super(cause);
    }
}
