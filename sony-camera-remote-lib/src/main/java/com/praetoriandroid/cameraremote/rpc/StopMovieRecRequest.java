package com.praetoriandroid.cameraremote.rpc;

public class StopMovieRecRequest extends BaseRequest<Void, StopMovieRecResponse> {
    public StopMovieRecRequest() {
        super(StopMovieRecResponse.class, RpcMethod.stopMovieRec);
    }
}
