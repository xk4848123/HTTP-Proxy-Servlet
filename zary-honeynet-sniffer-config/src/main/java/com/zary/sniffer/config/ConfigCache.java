package com.zary.sniffer.config;

public class ConfigCache {

    private volatile static Config config;

    public static Config getConfig() {
        return config;
    }

    public static void setConfig(Config config) {
        ConfigCache.config = config;
    }

}
