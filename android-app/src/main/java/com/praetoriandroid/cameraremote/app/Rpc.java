package com.praetoriandroid.cameraremote.app;

import android.util.Log;

import com.praetoriandroid.cameraremote.DeviceDescription;
import com.praetoriandroid.cameraremote.HttpClient;
import com.praetoriandroid.cameraremote.LiveViewFetcher;
import com.praetoriandroid.cameraremote.RpcClient;
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

    public interface ConnectionListener {
        void onConnected();
        void onConnectionFailed(Throwable e);
    }

    public interface ResponseHandler<Response extends BaseResponse<?>> {
        void onSuccess(Response response);
        void onErrorResponse(int errorCode);
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
        connect();
    }

    @Background (serial = RPC_NETWORK)
    public void connect() {
        try {
            initialized = false;
            initializationError = null;
            SsdpClient ssdpClient = new SsdpClient();
            String deviceDescriptionUrl = ssdpClient.getDeviceDescriptionUrl();
            DeviceDescription description = new DeviceDescription(deviceDescriptionUrl);
            String cameraServiceUrl = description.getServiceUrl(DeviceDescription.CAMERA);
            rpcClient = new RpcClient(cameraServiceUrl);
            rpcClient.sayHello();
            onConnected(cameraServiceUrl);
        } catch (SsdpClient.SsdpException e) {
            onConnectionFailed(e);
        } catch (DeviceDescription.ServiceNotSupportedException e) {
            onConnectionFailed(e);
        } catch (IOException e) {
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
                onErrorResponse(tag, response.getErrorCode());
            }
        } catch (HttpClient.BadHttpResponseException e) {
            onResponseFail(tag, e);
        } catch (IOException e) {
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
    <Response extends BaseResponse<?>> void onErrorResponse(Object tag, int errorCode) {
        @SuppressWarnings("unchecked")
        ResponseHandler<Response> handler = (ResponseHandler<Response>) responseHandlers.get(tag);
        if (handler != null) {
            handler.onErrorResponse(errorCode);
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
            public void onErrorResponse(int errorCode) {
                callback.onError(new IllegalStateException("HTTP error: " + errorCode));
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
                    e.printStackTrace();
                    callback.onError(e);
                } catch (HttpClient.BadHttpResponseException e) {
                    e.printStackTrace();
                    callback.onError(e);
                } catch (LiveViewFetcher.ParseException e) {
                    e.printStackTrace();
                    callback.onError(e);
                } catch (LiveViewFetcher.DisconnectedException e) {
                    callback.onError(e);
                    onConnectionFailed(e);
                }
            }
        }.start();
    }

}
