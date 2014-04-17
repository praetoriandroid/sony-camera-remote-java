package com.praetoriandroid.cameraremote.rpc;

public class ActTakePictureResponse extends BaseResponse<String[]> {
    @Override
    public void validate() throws ValidationException {
        super.validate();
        String[][] result = getResult();
        if (isOk()) {
            if (result.length != 1) {
                throw new IllegalResultSizeException(1, result.length);
            }
        }
    }

    public boolean capturingInProgress() {
        return !isOk() && getErrorCode() == ERROR_STILL_CAPTURING_NOT_FINISHED;
    }

    public String[] getUrls() {
        return getResult()[0];
    }
}
