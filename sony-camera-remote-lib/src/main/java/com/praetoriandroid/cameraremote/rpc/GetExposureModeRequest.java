package com.praetoriandroid.cameraremote.rpc;

public class GetExposureModeRequest extends BaseRequest<Void, SimpleResponse> {
    public GetExposureModeRequest() {
        super(SimpleResponse.class, RpcMethod.getExposureMode);
    }
}
