package com.praetoriandroid.cameraremote.rpc;

public class StartMovieRecRequest extends BaseRequest<Void, SimpleResponse> {
    public StartMovieRecRequest() {
        super(SimpleResponse.class, RpcMethod.startMovieRec);
    }
}
