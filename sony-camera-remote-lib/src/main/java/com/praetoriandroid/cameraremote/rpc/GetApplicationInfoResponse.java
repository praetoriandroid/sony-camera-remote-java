package com.praetoriandroid.cameraremote.rpc;

public class GetApplicationInfoResponse extends BaseResponse<String> {

    @Override
    public void validate() throws ValidationException {
        super.validate();

        if (isOk()) {
            String[] result = getResult();
            if (result.length != 2) {
                throw new IllegalResultSizeException(2, result.length);
            }
        }
    }

    public boolean isVersionOk() {
        return getVersion().compareTo("2.0.0") >= 0;
    }

    public String getVersion() {
        return getResult()[1];
    }

    public String getApplicationName() {
        return getResult()[0];
    }

}
