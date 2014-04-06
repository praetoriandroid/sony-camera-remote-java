package com.praetoriandroid.cameraremote.rpc;

import java.util.Arrays;

/**
 * Base API call response class.
 * @param <Result> type of <code>result</code> array item. See 'Sony Camera Remote API Developer Guide' for more details.
 */
@SuppressWarnings("UnusedDeclaration")
public abstract class BaseResponse<Result> {

    public static final int ERROR_OK = 0; // WTF???
    public static final int ERROR_ANY = 1;
    public static final int ERROR_TIMEOUT = 2;
    public static final int ERROR_ILLEGAL_ARGUMENT = 3;
    public static final int ERROR_ILLEGAL_DATA_FORMAT = 4;
    public static final int ERROR_ILLEGAL_REQUEST = 5;
    public static final int ERROR_ILLEGAL_RESPONSE = 6;
    public static final int ERROR_ILLEGAL_STATE = 7;
    public static final int ERROR_ILLEGAL_TYPE = 8;
    public static final int ERROR_INDEX_OUT_OF_BOUNDS = 9;
    public static final int ERROR_NO_SUCH_ELEMENT = 10;
    public static final int ERROR_NO_SUCH_FIELD = 11;
    public static final int ERROR_NO_SUCH_METHOD = 12;
    public static final int ERROR_NULL_POINTER = 13;
    public static final int ERROR_UNSUPPORTED_VERSION = 14;
    public static final int ERROR_UNSUPPORTED_OPERATION = 15;
    public static final int ERROR_SHOOTING_FAIL = 40400;
    public static final int ERROR_CAMERA_NOT_READY = 40401;
    public static final int ERROR_ALREADY_RUNNING_POLLING_API = 40402;
    public static final int ERROR_STILL_CAPTURING_NOT_FINISHED = 40403;

    private Result[] result;

    private Result[] results;

    private String[] error;

    private int id;

    public void validate() throws IllegalResponseException {
        if (result != null && results != null) {
            throw new IllegalResponseException("Both 'result' and 'results' are not allowed");
        }

        if (getResult() != null && error != null) {
            throw new IllegalResponseException("Both 'result[s]' and 'error' are not allowed");
        }

        if (getResult() == null && error == null) {
            throw new IllegalResponseException("Both 'result[s]' and 'error' could not be null");
        }

        if (error != null) {
            if (error.length != 2) {
                throw new IllegalResponseException("'error' contains " + error.length + " (must be 2)");
            }

            try {
                //noinspection ResultOfMethodCallIgnored
                Integer.parseInt(error[0]);
            } catch (NumberFormatException e) {
                throw new IllegalResponseException("Illegal error code value: " + error[0]);
            }
        }
    }

    public boolean isOk() {
        return error == null;
    }

    /**
     * Returns error code or 0 if there is no valid error code.
     */
    public int getErrorCode() {
        if (error == null) {
            return 0;
        }

        if (error.length != 2) {
            return 0;
        }

        try {
            return Integer.parseInt(error[0]);
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    protected Result[] getResult() {
        if (result == null) {
            return results;
        }
        return result;
    }

    @Override
    public String toString() {
        return Arrays.toString(getResult()) + '/' + Arrays.toString(error) + '[' + id + ']';
    }

    public static class IllegalResponseException extends Exception {

        public IllegalResponseException(String message) {
            super(message);
        }

    }

    public static class IllegalResultSizeException extends IllegalResponseException {

        public IllegalResultSizeException(int expectedSize, int actualSize) {
            super("Illegal 'result' size (must be " + expectedSize + "): " + actualSize);
        }

    }

    public static class ResponseParseException extends RuntimeException {

        public ResponseParseException() {
            super();
        }

        public ResponseParseException(String message) {
            super(message);
        }

        public ResponseParseException(Throwable cause) {
            super(cause);
        }
    }
}
