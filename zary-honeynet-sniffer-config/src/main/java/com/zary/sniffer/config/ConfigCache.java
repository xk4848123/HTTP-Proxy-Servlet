package com.zary.sniffer.config;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ConfigCache {


    //为了支持多个javaagent这里使用ConcurrentHashMap
    private static Map<String, Config> configs = new ConcurrentHashMap<>();

    //单个javaagent时使用该方法即可
    public static Config getConfig() {
        for (Map.Entry<String, Config> entry : configs.entrySet()) {
            return entry.getValue();
        }
        return new Config();
    }

    public static Config getConfig(String root) {
        return configs.get(root);
    }


    public static void setConfig(String root, Config config) {
        configs.put(root, config);
    }

}
