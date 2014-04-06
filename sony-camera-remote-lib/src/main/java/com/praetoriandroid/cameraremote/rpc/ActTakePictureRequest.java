package com.praetoriandroid.cameraremote.rpc;

public class ActTakePictureRequest extends BaseRequest<Void, ActTakePictureResponse> {
    public ActTakePictureRequest() {
        super(ActTakePictureResponse.class, RpcMethod.actTakePicture);
    }
}
