package com.zary.sniffer.server.handle;

import lombok.Value;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

public class HandleManager {

    private final Map<Integer, InnerHandler> handleMaps = new HashMap<>();

    private final int DEFAULT_HANDLE_TYPE = 1;

    public void addHandler(int handleType, Function<String, Boolean> dataHandler) {
        handleMaps.put(handleType, new InnerHandler(dataHandler));
    }

    public void addTypeOneHandler(Function<String, Boolean> dataHandler) {
        addHandler(DEFAULT_HANDLE_TYPE, dataHandler);
    }

    public InnerHandler getHandler(int type) {
        return handleMaps.get(type);
    }

    @Value
    public static class InnerHandler {
        Function<String, Boolean> dataHandler;
    }

}
