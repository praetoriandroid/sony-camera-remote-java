package com.praetoriandroid.cameraremote.rpc;

public class StartLiveviewRequest extends BaseRequest<Void, StartLiveviewResponse> {

    public StartLiveviewRequest() {
        super(StartLiveviewResponse.class, RpcMethod.startLiveview);
    }

}
