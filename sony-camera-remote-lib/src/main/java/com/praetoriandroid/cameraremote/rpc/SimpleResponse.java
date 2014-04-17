package com.praetoriandroid.cameraremote.rpc;

/**
 * Simplest form of API response with the only integer value in <code>'result'</code> field. In most cases the value
 * is 0 and indicates the success status as well as absence of <code>'error'</code> field.
 */
public class SimpleResponse extends BaseResponse<Integer> {

    @Override
    public void validate() throws ValidationException {
        super.validate();

        if (isOk()) {
            Integer[] result = getResult();
            if (result.length != 1) {
                throw new IllegalResultSizeException(1, result.length);
            }
        }
    }

    public int getValue() {
        return getResult()[0];
    }
}
