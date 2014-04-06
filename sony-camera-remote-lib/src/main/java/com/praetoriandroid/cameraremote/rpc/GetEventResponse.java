package com.praetoriandroid.cameraremote.rpc;

import java.util.HashMap;
import java.util.Map;

public class GetEventResponse extends BaseResponse<EventEntity> {

    private transient Map<Class<? extends EventEntity>, EventEntity> entities
            = new HashMap<Class<? extends EventEntity>, EventEntity>();

    void postProcess() {
        for (EventEntity entity : getResult()) {
            if (entity != null) {
                entities.put(entity.getType().getEntityClass(), entity);
            }
        }
    }

    public <T extends EventEntity> T getEntity(Class<T> eventType) {
        //noinspection unchecked
        return (T) entities.get(eventType);
    }

}
