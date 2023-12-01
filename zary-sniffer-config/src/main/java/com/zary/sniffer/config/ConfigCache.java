package com.zary.sniffer.config;

public class ConfigCache {

    private static Config config;

    public static Config get() {
        return ConfigCache.config;
    }

    public static void setConfig(Config config) {
        ConfigCache.config = config;
    }
}
