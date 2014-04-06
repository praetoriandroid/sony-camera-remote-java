package com.praetoriandroid.cameraremote;

import com.praetoriandroid.cameraremote.rpc.BaseRequest;
import com.praetoriandroid.cameraremote.rpc.RpcMethod;
import com.praetoriandroid.cameraremote.rpc.SimpleResponse;

public class GetExposureModeRequest extends BaseRequest<Void, SimpleResponse> {
    public GetExposureModeRequest() {
        super(SimpleResponse.class, RpcMethod.getExposureMode);
    }
}
