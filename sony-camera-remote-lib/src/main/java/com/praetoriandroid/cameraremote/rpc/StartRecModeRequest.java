package com.praetoriandroid.cameraremote.rpc;

public class StartRecModeRequest extends BaseRequest<Void, SimpleResponse> {

    public StartRecModeRequest() {
        super(SimpleResponse.class, RpcMethod.startRecMode);
    }

}
