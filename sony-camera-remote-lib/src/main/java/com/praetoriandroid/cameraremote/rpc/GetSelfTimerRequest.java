package com.praetoriandroid.cameraremote.rpc;

public class GetSelfTimerRequest extends BaseRequest<Void, SimpleResponse> {
    public GetSelfTimerRequest() {
        super(SimpleResponse.class, RpcMethod.getSelfTimer);
    }
}
