package com.praetoriandroid.cameraremote.rpc;

public class GetAvailableApiListRequest extends BaseRequest<Void, GetAvailableApiListResponse> {

    public GetAvailableApiListRequest() {
        super(GetAvailableApiListResponse.class, RpcMethod.getAvailableApiList);
    }

}
