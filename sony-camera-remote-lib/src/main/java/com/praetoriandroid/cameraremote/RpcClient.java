package com.praetoriandroid.cameraremote;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.praetoriandroid.cameraremote.rpc.BaseRequest;
import com.praetoriandroid.cameraremote.rpc.BaseResponse;
import com.praetoriandroid.cameraremote.rpc.GetApplicationInfoRequest;
import com.praetoriandroid.cameraremote.rpc.GetApplicationInfoResponse;
import com.praetoriandroid.cameraremote.rpc.GetAvailableApiListRequest;
import com.praetoriandroid.cameraremote.rpc.GetAvailableApiListResponse;
import com.praetoriandroid.cameraremote.rpc.RpcMethod;
import com.praetoriandroid.cameraremote.rpc.SimpleResponse;
import com.praetoriandroid.cameraremote.rpc.StartRecModeRequest;
import com.praetoriandroid.cameraremote.rpc.StopRecModeRequest;

import java.io.IOException;

public class RpcClient {

    private Gson gson = new Gson();

    private HttpClient httpClient = new HttpClient();

    private String cameraServiceUrl;

    private Logger logger;

    public RpcClient(String cameraServiceUrl) {
        this.cameraServiceUrl = cameraServiceUrl;
    }

    public void setConnectionTimeout(int timeout) {
        httpClient.setConnectionTimeout(timeout);
    }

    public void setLogger(Logger logger) {
        this.logger = logger;
    }

    public void sayHello() throws RpcException {
        GetApplicationInfoResponse appInfo = send(new GetApplicationInfoRequest());
        if (!appInfo.isVersionOk()) {
            throw new RpcException("Illegal camera API version (must be at least 2.0.0): "
                    + appInfo.getVersion());
        }

        GetAvailableApiListResponse availableApiList = send(new GetAvailableApiListRequest());
        if (availableApiList.getApiList().contains(RpcMethod.startRecMode.name())) {
            SimpleResponse recModeResult = send(new StartRecModeRequest());
            if (!recModeResult.isOk()) {
                throw new RpcException("Could not start rec mode: " + recModeResult.getErrorCode());
            }
        }
    }

    public void sayGoodbye() throws RpcException {
        GetAvailableApiListResponse availableApiList = send(new GetAvailableApiListRequest());
        if (availableApiList.getApiList().contains(RpcMethod.stopRecMode.name())) {
            SimpleResponse recModeResult = send(new StopRecModeRequest());
            if (!recModeResult.isOk()) {
                throw new RpcException("Could not stop rec mode: " + recModeResult.getErrorCode());
            }
        }
    }

    public <Response extends BaseResponse<?>>
    Response send(BaseRequest<?, Response> request) throws RpcException {
        try {
            String requestText = gson.toJson(request);
            debug("Request: %s", requestText);
            String responseText = httpClient.fetchTextByPost(cameraServiceUrl, requestText);
            debug("Response: %s", responseText);
            Response response = request.parseResponse(gson, responseText);
            response.validate();
            return response;
        } catch (JsonSyntaxException e) {
            throw new RpcException(e);
        } catch (IOException e) {
            throw new RpcException(e);
        }
    }

    private void debug(String format, Object... args) {
        if (logger != null) {
            logger.debug(format, args);
        }
    }

}
