package com.praetoriandroid.cameraremote.rpc;

public class AwaitTakePictureRequest extends BaseRequest<Void, ActTakePictureResponse> {
    public AwaitTakePictureRequest() {
        super(ActTakePictureResponse.class, RpcMethod.awaitTakePicture);
    }
}
