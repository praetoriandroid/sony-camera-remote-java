package com.praetoriandroid.cameraremote.rpc;

import java.util.List;

public class GetAvailableApiListResponse extends BaseResponse<List<String>> {

    @Override
    public void validate() throws ValidationException {
        super.validate();

        if (isOk()) {
            List<String>[] result = getResult();
            if (result.length != 1) {
                throw new IllegalResultSizeException(1, result.length);
            }
        }
    }

    public List<String> getApiList() {
        return getResult()[0];
    }

    @Override
    public String toString() {
        return getResult()[0].toString();
    }

}
