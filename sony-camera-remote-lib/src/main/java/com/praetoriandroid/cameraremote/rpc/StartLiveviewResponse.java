package com.praetoriandroid.cameraremote.rpc;

public class StartLiveviewResponse extends BaseResponse<String> {

    @Override
    public void validate() throws IllegalResponseException {
        super.validate();

        if (isOk()) {
            String[] result = getResult();
            if (result.length != 1) {
                throw new IllegalResultSizeException(1, result.length);
            }
        }
    }

    public String getUrl() {
        return getResult()[0];
    }

}
