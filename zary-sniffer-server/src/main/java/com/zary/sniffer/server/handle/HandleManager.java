package com.zary.sniffer.server.handle;

import com.zary.sniffer.transfer.AgentData;
import lombok.Value;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

public class HandleManager {

    private final Map<Integer, InnerHandler> handleMaps = new HashMap<>();

    public <T> void addHandler(int handleType, Type type, Function<List<AgentData<T>>, Boolean> agentDataConsumer) {
        handleMaps.put(handleType, new InnerHandler<>(type, agentDataConsumer));
    }


    public InnerHandler getHandler(int type) {
        return handleMaps.get(type);
    }

    @Value
    public static class InnerHandler<K> {

        Type type;

        Function<List<AgentData<K>>, Boolean> agentDataConsumer;
    }

}
