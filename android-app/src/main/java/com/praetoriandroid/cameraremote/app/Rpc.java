package com.praetoriandroid.cameraremote.app;

import android.util.Log;

import com.praetoriandroid.cameraremote.DeviceDescription;
import com.praetoriandroid.cameraremote.HttpClient;
import com.praetoriandroid.cameraremote.LiveViewFetcher;
import com.praetoriandroid.cameraremote.RpcClient;
import com.praetoriandroid.cameraremote.SsdpClient;
import com.praetoriandroid.cameraremote.rpc.BaseRequest;
import com.praetoriandroid.cameraremote.rpc.BaseResponse;
import com.praetoriandroid.cameraremote.rpc.StartLiveviewRequest;
import com.praetoriandroid.cameraremote.rpc.StartLiveviewResponse;

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

    public interface InitCallback {
        void onRpcInitSucceeded();
        void onRpcInitFailed(Throwable e);
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
    private final Set<InitCallback> initializationCallbacks = new HashSet<InitCallback>();
    private boolean initialized;
    private final Map<Object, ResponseHandler<?>> responseHandlers = new HashMap<Object, ResponseHandler<?>>();
    private LiveViewFetcher liveViewFetcher = new LiveViewFetcher();
    private volatile boolean liveViewInProgress;

    public Rpc() {
        initialized = false;
        init();
    }

    @Background (serial = RPC_NETWORK)
    void init() {
        try {
            SsdpClient ssdpClient = new SsdpClient();
            String deviceDescriptionUrl = ssdpClient.getDeviceDescriptionUrl();
            DeviceDescription description = new DeviceDescription(deviceDescriptionUrl);
            String cameraServiceUrl = description.getServiceUrl(DeviceDescription.CAMERA);
            onInitSucceeded(cameraServiceUrl);
            rpcClient = new RpcClient(cameraServiceUrl);
            rpcClient.sayHello();
        } catch (SsdpClient.SsdpException e) {
            onInitFailed(e);
        } catch (DeviceDescription.ServiceNotSupportedException e) {
            onInitFailed(e);
        } catch (IOException e) {
            onInitFailed(e);
        }
    }

    @UiThread (propagation = UiThread.Propagation.REUSE)
    public void reinit() {
        initialized = false;
        initializationError = null;
        init();
    }

    @UiThread
    void onInitSucceeded(String cameraServiceUrl) {
        initialized = true;
        for (InitCallback callback : initializationCallbacks) {
            callback.onRpcInitSucceeded();
        }
//        initializationCallbacks.clear();
    }

    @UiThread
    void onInitFailed(Throwable e) {
        Log.e("@@@@@", "RPC init failed", e);
        initialized = true;
        initializationError = e;
        for (InitCallback callback : initializationCallbacks) {
            callback.onRpcInitFailed(e);
        }
//        initializationCallbacks.clear();
    }

    @UiThread (propagation = UiThread.Propagation.REUSE)
    public void registerInitCallback(InitCallback callback) {
        if (initialized) {
            if (initializationError == null) {
                callback.onRpcInitSucceeded();
            } else {
                callback.onRpcInitFailed(initializationError);
            }
        }// else {
            initializationCallbacks.add(callback);
        //}
    }

    @UiThread (propagation = UiThread.Propagation.REUSE)
    public void unregisterInitCallback(InitCallback callback) {
        initializationCallbacks.remove(callback);
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

    void onLiveViewStarted(final String url, final LiveViewCallback callback) {
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
                } catch (LiveViewFetcher.DisconnectedException ignored) {
                }
            }
        }.start();
    }

    public void startLiveView(final LiveViewCallback callback) {
        liveViewInProgress = true;
        sendRequest(new StartLiveviewRequest(), liveViewFetcher, new ResponseHandler<StartLiveviewResponse>() {
            @Override
            public void onSuccess(StartLiveviewResponse response) {
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

}
