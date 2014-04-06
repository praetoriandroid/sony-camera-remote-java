package com.praetoriandroid.cameraremote.rpc;

import com.google.gson.Gson;

import java.util.Arrays;

/**
 * Base API call request class. See {@link RpcMethod} for all possible requests.
 * @param <Param> type of <code>params</code> array item. Use <code>Void</code> for requests without parameters.
 *               See 'Sony Camera Remote API Developer Guide' for more details.
 * @param <Response> class that represents appropriate response for given request.
 *                  Use {@link com.praetoriandroid.cameraremote.rpc.SimpleResponse} for responses with only ok/fail
 *                  status.
 */
public abstract class BaseRequest<Param, Response extends BaseResponse> {

    private transient final Class<Response> responseClass;

    private final RpcMethod method;

    private Param[] params;

    private int id;

    @SuppressWarnings("FieldCanBeLocal")
    private final String version = "1.0";

    private static int nextId = 1;

    /**
     * All subclass constructors must explicitly define response class and request method. See any subclass for example.
     * @param responseClass class of the response for this request. The same as a {@link Response} type parameter.
     * @param method request method for the given request. Probably named as a request class without 'Request' suffix.
     * @param params optional request parameter(s).
     */
    public BaseRequest(Class<Response> responseClass, RpcMethod method, Param... params) {
        this.responseClass = responseClass;
        this.method = method;
        this.params = Arrays.copyOf(params, params.length);
        id = getNextId();
    }

    public Response parseResponse(Gson gson, String data) {
        return gson.fromJson(data, responseClass);
    }

    @Override
    public String toString() {
        return "{" + method + "[" + id + "](" + version + "): " + Arrays.toString(params) + "}";
    }

    private synchronized int getNextId() {
        return nextId++;
    }
}
