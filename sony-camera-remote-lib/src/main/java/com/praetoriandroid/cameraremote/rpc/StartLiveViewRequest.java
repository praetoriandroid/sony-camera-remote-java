package com.praetoriandroid.cameraremote.rpc;

public class StartLiveViewRequest extends BaseRequest<Void, StartLiveViewResponse> {

    public StartLiveViewRequest() {
        super(StartLiveViewResponse.class, RpcMethod.startLiveview);
    }

}
