package com.praetoriandroid.cameraremote.rpc;

public class StopRecModeRequest extends BaseRequest<Void, SimpleResponse> {

    public StopRecModeRequest() {
        super(SimpleResponse.class, RpcMethod.stopRecMode);
    }

}
