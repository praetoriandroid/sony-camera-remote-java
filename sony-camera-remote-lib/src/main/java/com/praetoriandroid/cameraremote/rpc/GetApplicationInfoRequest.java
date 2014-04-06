package com.praetoriandroid.cameraremote.rpc;

public class GetApplicationInfoRequest extends BaseRequest<Void, GetApplicationInfoResponse> {
    public GetApplicationInfoRequest() {
        super(GetApplicationInfoResponse.class, RpcMethod.getApplicationInfo);
    }
}
