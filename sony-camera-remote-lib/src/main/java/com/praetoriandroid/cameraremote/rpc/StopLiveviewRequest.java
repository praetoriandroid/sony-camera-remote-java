package com.praetoriandroid.cameraremote.rpc;

public class StopLiveviewRequest extends BaseRequest<Void, SimpleResponse> {

    public StopLiveviewRequest() {
        super(SimpleResponse.class, RpcMethod.stopLiveview);
    }

}
