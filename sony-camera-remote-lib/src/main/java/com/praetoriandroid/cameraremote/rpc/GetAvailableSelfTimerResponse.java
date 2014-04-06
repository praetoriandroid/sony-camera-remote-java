package com.praetoriandroid.cameraremote.rpc;

public class GetAvailableSelfTimerResponse extends BaseResponse<CustomizableEntity> {

    @Override
    public void validate() throws IllegalResponseException {
        super.validate();

        if (isOk()) {
            CustomizableEntity[] result = getResult();
            if (result.length != 2) {
                throw new IllegalResultSizeException(2, result.length);
            }

            if (!(result[0] instanceof IntEntity)) {
                throw new IllegalResponseException("First 'result' element should be integer");
            }

            if (!(result[0] instanceof IntArrayEntity)) {
                throw new IllegalResponseException("First 'result' element should be integer-array");
            }
        }
    }

    public int getCurrentTimer() {
        return ((IntEntity) getResult()[0]).getValue();
    }

    public int[] getAvailableTimers() {
        return ((IntArrayEntity) getResult()[0]).getValue();
    }

    public static class IntArrayEntity implements CustomizableEntity {

        private int[] value;

        public IntArrayEntity(int[] value) {
            this.value = value;
        }

        public int[] getValue() {
            return value;
        }

    }

    public static class IntEntity implements CustomizableEntity {

        private int value;

        public IntEntity(int value) {
            this.value = value;
        }

        public int getValue() {
            return value;
        }

    }

}
