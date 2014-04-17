package com.praetoriandroid.cameraremote.rpc;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSyntaxException;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GetMethodTypesRequest extends BaseRequest<String, GetMethodTypesResponse> {

    private static Gson customGson;

    public GetMethodTypesRequest() {
        this("");
    }

    public GetMethodTypesRequest(String version) {
        super(GetMethodTypesResponse.class, RpcMethod.getMethodTypes, version);
    }

    @Override
    public GetMethodTypesResponse parseResponse(Gson gson, String data) throws JsonSyntaxException {
        synchronized (GetMethodTypesRequest.class) {
            if (customGson == null) {
                customGson = createGson();
            }
        }
        return super.parseResponse(customGson, data);
    }

    private Gson createGson() {
        return new GsonBuilder()
                .registerTypeAdapter(MethodTypesEntity.class, new MethodTypesEntityDeserializer())
                .create();
    }

    private static class MethodTypesEntityDeserializer implements JsonDeserializer<MethodTypesEntity> {
        @Override
        public MethodTypesEntity deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
                throws JsonParseException {
            try {
                if (!json.isJsonArray()) {
                    throw new JsonParseException("The entity must be an array");
                }
                JsonArray entities = json.getAsJsonArray();
                if (entities.size() != 4) {
                    throw new JsonParseException("The entity must be of size 4");
                }
                if (!entities.get(1).isJsonArray() || !entities.get(2).isJsonArray()) {
                    throw new JsonParseException("Both [1] and [2] elements must be string-arrays");
                }
                String name = entities.get(0).getAsString();
                List<MethodTypesEntity.AbstractClass> parameterTypes = new ArrayList<MethodTypesEntity.AbstractClass>();
                for (JsonElement element : entities.get(1).getAsJsonArray()) {
                    String className = element.getAsString();
                    parameterTypes.add(getClassByName(className));
                }
                List<MethodTypesEntity.AbstractClass> responseTypes = new ArrayList<MethodTypesEntity.AbstractClass>();
                for (JsonElement element : entities.get(2).getAsJsonArray()) {
                    String className = element.getAsString();
                    responseTypes.add(getClassByName(className));
                }
                String version = entities.get(3).getAsString();
                return new MethodTypesEntity(name, parameterTypes, responseTypes, version);
            } catch (RuntimeException e) {
                throw new JsonParseException(e);
            }
        }

        private MethodTypesEntity.AbstractClass getClassByName(String name) throws JsonParseException {
            MethodTypesEntity.AbstractClass result = classNames.get(name);
            if (result == null) {
                if (isCompoundObject(name)) {
                    return parseClassDescription(name);
                } else {
                    throw new IllegalArgumentException("Unknown type: '" + name + "'");
                }
            }
            return result;
        }

        private boolean isCompoundObject(String description) {
            return description.startsWith("{") && (description.endsWith("}") || description.endsWith("}*"));
        }

        private MethodTypesEntity.ClassDescription parseClassDescription(String descriptionText) {
            MethodTypesEntity.ClassDescription description;
            int tail;
            if (descriptionText.endsWith("*")) {
                description = new MethodTypesEntity.ClassDescriptionArray();
                tail = 1;
            } else {
                description = new MethodTypesEntity.ClassDescription();
                tail = 0;
            }

            String[] fields = descriptionText.substring(1, descriptionText.length() - 1 - tail).split(", ?");
            for (String field : fields) {
                String[] pair = field.split(":");
                if (pair.length != 2) {
                    throw new IllegalArgumentException();
                }
                String name = pair[0];
                String type = pair[1];
                MethodTypesEntity.AbstractClass clazz = classNames.get(type.substring(1, type.length() - 1));
                if (clazz == null) {
                    throw new IllegalArgumentException("Unknown type: " + type);
                }
                description.put(name.substring(1, name.length() - 1), clazz.getSimpleClass());
            }
            return description;
        }

        private static final Map<String, MethodTypesEntity.AbstractClass> classNames = new HashMap<String, MethodTypesEntity.AbstractClass>();
        static {
            classNames.put("bool", new MethodTypesEntity.SimpleClass(boolean.class));
            classNames.put("bool*", new MethodTypesEntity.SimpleClass(boolean[].class));
            classNames.put("int", new MethodTypesEntity.SimpleClass(int.class));
            classNames.put("int*", new MethodTypesEntity.SimpleClass(int[].class));
            classNames.put("double", new MethodTypesEntity.SimpleClass(double.class));
            classNames.put("double*", new MethodTypesEntity.SimpleClass(double[].class));
            classNames.put("string", new MethodTypesEntity.SimpleClass(String.class));
            classNames.put("string*", new MethodTypesEntity.SimpleClass(String[].class));
        }

    }

}
