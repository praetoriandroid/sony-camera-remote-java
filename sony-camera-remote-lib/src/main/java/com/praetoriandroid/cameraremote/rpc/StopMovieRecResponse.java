package com.praetoriandroid.cameraremote.rpc;

public class StopMovieRecResponse extends BaseResponse<String> {

    @Override
    public void validate() throws ValidationException {
        super.validate();

        if (isOk()) {
            String[] result = getResult();
            if (result.length != 1) {
                throw new IllegalResultSizeException(1, result.length);
            }
        }
    }

    public String getThumbnailUrl() {
        return getResult()[0];
    }

    public boolean isThumbnailAvailable() {
        return !getThumbnailUrl().isEmpty();
    }

}
