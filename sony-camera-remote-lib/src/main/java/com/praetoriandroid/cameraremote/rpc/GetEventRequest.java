package com.praetoriandroid.cameraremote.rpc;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

import java.lang.reflect.Type;

public class GetEventRequest extends BaseRequest<Boolean, GetEventResponse> {

    private static Gson customGson;

    public GetEventRequest(boolean longPolling) {
        super(GetEventResponse.class, RpcMethod.getEvent, longPolling);
    }

    @Override
    public GetEventResponse parseResponse(Gson gson, String data) {
        synchronized (GetEventRequest.class) {
            if (customGson == null) {
                customGson = createGson();
            }
        }
        GetEventResponse response = super.parseResponse(customGson, data);
        response.postProcess();
        return response;
    }

    private Gson createGson() {
        return new GsonBuilder()
                .registerTypeAdapter(EventEntity.class, new EventEntityDeserializer())
                .create();
    }

    private static class EventEntityDeserializer implements JsonDeserializer<EventEntity> {
        @Override
        public EventEntity deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
                throws JsonParseException {
            try {
                System.err.println("json: " + json);
                if (!json.isJsonObject()) {
                    return null;
                }
                JsonObject entity = json.getAsJsonObject();
                String typeString = entity.get("type").getAsString();
                try {
                    EventEntity.Type type = EventEntity.Type.valueOf(typeString);
                    return context.deserialize(json, type.getEntityClass());
                } catch (IllegalArgumentException e) {
                    return null;
                }
            } catch (RuntimeException e) {
                throw new JsonParseException(e);
            }
        }
    }
}
