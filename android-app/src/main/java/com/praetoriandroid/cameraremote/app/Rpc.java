package com.praetoriandroid.cameraremote.app;

import android.util.Log;

import com.praetoriandroid.cameraremote.DeviceDescription;
import com.praetoriandroid.cameraremote.LiveViewDisconnectedException;
import com.praetoriandroid.cameraremote.LiveViewFetcher;
import com.praetoriandroid.cameraremote.ParseException;
import com.praetoriandroid.cameraremote.RpcClient;
import com.praetoriandroid.cameraremote.RpcException;
import com.praetoriandroid.cameraremote.SsdpClient;
import com.praetoriandroid.cameraremote.rpc.BaseRequest;
import com.praetoriandroid.cameraremote.rpc.BaseResponse;
import com.praetoriandroid.cameraremote.rpc.StartLiveViewRequest;
import com.praetoriandroid.cameraremote.rpc.StartLiveViewResponse;

import org.androidannotations.annotations.Background;
import org.androidannotations.annotations.EBean;
import org.androidannotations.annotations.UiThread;

import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@EBean (scope = EBean.Scope.Singleton)
public class Rpc {

    private static final String RPC_NETWORK = "RPC network";
    private static final int SSDP_TIMEOUT = 1000;
    private static final int CONNECTION_TIMEOUT = 1000;

    public interface ConnectionListener {
        void onConnected();
        void onConnectionFailed(Throwable e);
    }

    public interface ResponseHandler<Response extends BaseResponse<?>> {
        void onSuccess(Response response);
        void onFail(Throwable e);
    }

    public interface LiveViewCallback {
        void onNextFrame(LiveViewFetcher.Frame frame);
        void onError(Throwable e);
    }

    private RpcClient rpcClient;
    private Throwable initializationError;
    private final Set<ConnectionListener> connectionListeners = new HashSet<ConnectionListener>();
    private boolean initialized;
    private final Map<Object, ResponseHandler<?>> responseHandlers = new HashMap<Object, ResponseHandler<?>>();
    private LiveViewFetcher liveViewFetcher = new LiveViewFetcher();
    private volatile boolean liveViewInProgress;

    public Rpc() {
        liveViewFetcher.setConnectionTimeout(CONNECTION_TIMEOUT);
        connect();
    }

    @Background (serial = RPC_NETWORK)
    public void connect() {
        try {
            initialized = false;
            initializationError = null;
            SsdpClient ssdpClient = new SsdpClient();
            ssdpClient.setSearchTimeout(SSDP_TIMEOUT);
            String deviceDescriptionUrl = ssdpClient.getDeviceDescriptionUrl();
            DeviceDescription description = new DeviceDescription.Fetcher()
                    .setConnectionTimeout(CONNECTION_TIMEOUT)
                    .fetch(deviceDescriptionUrl);
            String cameraServiceUrl = description.getServiceUrl(DeviceDescription.CAMERA);
            rpcClient = new RpcClient(cameraServiceUrl);
            rpcClient.setConnectionTimeout(CONNECTION_TIMEOUT);
            rpcClient.sayHello();
            onConnected(cameraServiceUrl);
        } catch (IOException e) {
            onConnectionFailed(e);
        } catch (RpcException e) {
            onConnectionFailed(e);
        }
    }

    @UiThread
    void onConnected(String cameraServiceUrl) {
        initialized = true;
        for (ConnectionListener callback : connectionListeners) {
            callback.onConnected();
        }
    }

    @UiThread
    void onConnectionFailed(Throwable e) {
        Log.e("@@@@@", "RPC connect failed", e);
        initialized = true;
        initializationError = e;
        for (ConnectionListener callback : connectionListeners) {
            callback.onConnectionFailed(e);
        }
    }

    @UiThread (propagation = UiThread.Propagation.REUSE)
    public void registerInitCallback(ConnectionListener callback) {
        if (initialized) {
            if (initializationError == null) {
                callback.onConnected();
            } else {
                callback.onConnectionFailed(initializationError);
            }
        }
        connectionListeners.add(callback);
    }

    @UiThread (propagation = UiThread.Propagation.REUSE)
    public void unregisterInitCallback(ConnectionListener callback) {
        connectionListeners.remove(callback);
    }

    @UiThread (propagation = UiThread.Propagation.REUSE)
    public <Response extends BaseResponse<?>>
    void sendRequest(BaseRequest<?, Response> request, Object tag) {
        if (!initialized) {
            throw new IllegalStateException();
        }
        sendRequestInt(request, tag);
    }

    @UiThread (propagation = UiThread.Propagation.REUSE)
    public <Response extends BaseResponse<?>>
    void sendRequest(BaseRequest<?, Response> request,
                     Object tag,
                     ResponseHandler<Response> responseHandler) {
        if (!initialized) {
            throw new IllegalStateException();
        }
        responseHandlers.put(tag, responseHandler);
        sendRequestInt(request, tag);
    }

    @UiThread
    public void cancelRequest(Object tag) {
        responseHandlers.remove(tag);
    }

    @Background (serial = RPC_NETWORK)
    <Response extends BaseResponse<?>>
    void sendRequestInt(BaseRequest<?, Response> request, Object tag) {
        try {
            Response response = rpcClient.send(request);
            if (response.isOk()) {
                onResponseSuccess(tag, response);
            } else {
                throw new ErrorResponseException(response.getErrorCode());
            }
        } catch (RpcException e) {
            onResponseFail(tag, e);
        }
    }

    @UiThread
    <Response extends BaseResponse<?>> void onResponseSuccess(Object tag, Response response) {
        @SuppressWarnings("unchecked")
        ResponseHandler<Response> handler = (ResponseHandler<Response>) responseHandlers.get(tag);
        if (handler != null) {
            handler.onSuccess(response);
        }
    }

    @UiThread
    <Response extends BaseResponse<?>> void onResponseFail(Object tag, Throwable e) {
        @SuppressWarnings("unchecked")
        ResponseHandler<Response> handler = (ResponseHandler<Response>) responseHandlers.get(tag);
        if (handler != null) {
            handler.onFail(e);
        }
    }

    public void startLiveView(final LiveViewCallback callback) {
        liveViewInProgress = true;
        sendRequest(new StartLiveViewRequest(), liveViewFetcher, new ResponseHandler<StartLiveViewResponse>() {
            @Override
            public void onSuccess(StartLiveViewResponse response) {
                onLiveViewStarted(response.getUrl(), callback);
            }

            @Override
            public void onFail(Throwable e) {
                callback.onError(e);
            }
        });
    }

    public void stopLiveView() {
        try {
            liveViewInProgress = false;
            liveViewFetcher.disconnect();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void onLiveViewStarted(final String url, final LiveViewCallback callback) {
        new Thread() {
            @Override
            public void run() {
                try {
                    liveViewFetcher.connect(url);
                    while (liveViewInProgress) {
                        LiveViewFetcher.Frame frame = liveViewFetcher.getNextFrame();
                        callback.onNextFrame(frame);
                    }
                } catch (IOException e) {
                    callback.onError(e);
                } catch (ParseException e) {
                    callback.onError(e);
                } catch (LiveViewDisconnectedException ignored) {
                }
            }
        }.start();
    }

    private static class ErrorResponseException extends RpcException {
        private int errorCode;

        public ErrorResponseException(int errorCode) {
            this.errorCode = errorCode;
        }

        public int getErrorCode() {
            return errorCode;
        }
    }
}
