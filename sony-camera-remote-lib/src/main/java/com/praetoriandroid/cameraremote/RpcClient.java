package com.praetoriandroid.cameraremote;

import com.google.gson.Gson;
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

    public void sayHello() throws IOException {
        try {
            GetApplicationInfoResponse appInfo = send(new GetApplicationInfoRequest());
            if (!appInfo.isVersionOk()) {
                throw new IOException("Illegal camera API version (must be at least 2.0.0): " + appInfo.getVersion());
            }

            GetAvailableApiListResponse availableApiList = send(new GetAvailableApiListRequest());
            if (availableApiList.getApiList().contains(RpcMethod.startRecMode.name())) {
                SimpleResponse recModeResult = send(new StartRecModeRequest());
                if (!recModeResult.isOk()) {
                    throw new IOException("Could not start rec mode: " + recModeResult.getErrorCode());
                }
            }
        } catch (HttpClient.BadHttpResponseException e) {
            throw new IOException(e);
        }
    }

    public void sayGoodbye() throws IOException {
        try {
            GetAvailableApiListResponse availableApiList = send(new GetAvailableApiListRequest());
            if (availableApiList.getApiList().contains(RpcMethod.stopRecMode.name())) {
                SimpleResponse recModeResult = send(new StopRecModeRequest());
                if (!recModeResult.isOk()) {
                    throw new IOException("Could not stop rec mode: " + recModeResult.getErrorCode());
                }
            }
        } catch (HttpClient.BadHttpResponseException e) {
            throw new IOException(e);
        }
    }

    public <Response extends BaseResponse<?>> Response send(BaseRequest<?, Response> request)
            throws IOException, HttpClient.BadHttpResponseException {
        String requestText = gson.toJson(request);
        debug("Request: %s", requestText);
        String responseText = httpClient.fetchTextByPost(cameraServiceUrl, requestText);
        debug("Response: %s", responseText);
        try {
            Response response = request.parseResponse(gson, responseText);
            response.validate();
            return response;
        } catch (BaseResponse.IllegalResponseException e) {
            throw new IOException(e);
        } catch (RuntimeException e) {
            throw new IOException(e);
        }
    }

    private void debug(String format, Object... args) {
        if (logger != null) {
            logger.debug(format, args);
        }
    }
}
