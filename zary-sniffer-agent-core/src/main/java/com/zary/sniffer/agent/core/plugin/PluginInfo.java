package com.zary.sniffer.agent.core.plugin;

import lombok.Data;

/**
 * 探针插件对象
 */
@Data
public class PluginInfo {
    private String name;
    private String clazz;

    public PluginInfo(String name, String clazz) {
        this.name = name;
        this.clazz = clazz;
    }
}
