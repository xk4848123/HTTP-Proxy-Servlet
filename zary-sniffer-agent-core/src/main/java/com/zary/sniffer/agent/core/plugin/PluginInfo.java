package com.zary.sniffer.agent.core.plugin;


/**
 * 探针插件对象
 */
public class PluginInfo {
    private String name;
    private String clazz;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getClazz() {
        return clazz;
    }

    public void setClazz(String clazz) {
        this.clazz = clazz;
    }

    public PluginInfo(String name, String clazz) {
        this.name = name;
        this.clazz = clazz;
    }
}
