package com.praetoriandroid.cameraremote.rpc;

public class GetSupportedSelfTimerRequest extends BaseRequest<Void, GetSupportedSelfTimerResponse> {
    public GetSupportedSelfTimerRequest() {
        super(GetSupportedSelfTimerResponse.class, RpcMethod.getSupportedSelfTimer);
    }
}
