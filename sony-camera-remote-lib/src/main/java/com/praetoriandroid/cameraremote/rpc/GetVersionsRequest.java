package com.praetoriandroid.cameraremote.rpc;

public class GetVersionsRequest extends BaseRequest<String, GetVersionsResponse> {

    public GetVersionsRequest() {
        super(GetVersionsResponse.class, RpcMethod.getVersions);
    }

}
