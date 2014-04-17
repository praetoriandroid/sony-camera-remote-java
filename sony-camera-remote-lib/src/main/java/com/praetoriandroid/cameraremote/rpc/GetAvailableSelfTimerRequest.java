package com.praetoriandroid.cameraremote.rpc;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSyntaxException;

import java.lang.reflect.Type;

public class GetAvailableSelfTimerRequest extends BaseRequest<Void, GetAvailableSelfTimerResponse> {

    private static Gson customGson;

    public GetAvailableSelfTimerRequest() {
        super(GetAvailableSelfTimerResponse.class, RpcMethod.getAvailableSelfTimer);
    }

    @Override
    public GetAvailableSelfTimerResponse parseResponse(Gson gson, String data)
            throws JsonSyntaxException {
        synchronized (GetAvailableSelfTimerRequest.class) {
            if (customGson == null) {
                customGson = getGson();
            }
        }
        return super.parseResponse(customGson, data);
    }

    private Gson getGson() {
        return new GsonBuilder()
                .registerTypeAdapter(CustomizableEntity.class, new SelfTimerEntityDeserializer())
                .create();
    }

    private static class SelfTimerEntityDeserializer implements JsonDeserializer<CustomizableEntity> {

        @Override
        public CustomizableEntity deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
                throws JsonParseException {
            try {
                if (json.isJsonPrimitive()) {
                    JsonPrimitive primitive = (JsonPrimitive) json;
                    if (primitive.isNumber()) {
                        return new GetAvailableSelfTimerResponse.IntEntity(primitive.getAsNumber().intValue());
                    }
                } else if (json.isJsonArray()) {
                    JsonArray jsonArray = json.getAsJsonArray();
                    int[] array = new int[jsonArray.size()];
                    for (int i = 0; i < jsonArray.size(); i++) {
                        JsonElement item = jsonArray.get(i);
                        if (item.isJsonPrimitive()) {
                            array[i] = item.getAsNumber().intValue();
                        } else {
                            throw new JsonParseException("Illegal array item type: " + item);
                        }
                    }
                    return new GetAvailableSelfTimerResponse.IntArrayEntity(array);
                } else {
                    throw new JsonParseException("Illegal result type: " + json);
                }
            } catch (NumberFormatException e) {
                throw new JsonParseException(e);
            }
            return null;
        }

    }

}
