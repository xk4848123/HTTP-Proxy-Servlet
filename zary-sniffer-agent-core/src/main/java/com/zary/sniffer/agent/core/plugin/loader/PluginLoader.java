package com.zary.sniffer.agent.core.plugin.loader;

import com.zary.sniffer.agent.core.consts.AdmxAgentConsts;
import com.zary.sniffer.agent.core.plugin.AbstractPlugin;
import com.zary.sniffer.agent.core.plugin.PluginInfo;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

/**
 * 探针插件加载类：在初始化时装载指定的插件
 *
 * @author xulibo 2019-12-12
 */
public class PluginLoader {
    /**
     * 读取当前客户端plugin目录下所有插件jar包，并读取每个包里面的定义文件(plugin.define)
     *
     * @return
     */
    public static List<URL> loadPluginDefineFiles(AgentClassLoader classLoader) throws IOException {
        List<URL> files = new ArrayList<URL>();
        Enumeration<URL> enums = classLoader.getResources(AdmxAgentConsts.plugin_define_file);
        while (enums.hasMoreElements()) {
            URL pluginUrl = enums.nextElement();
            files.add(pluginUrl);
        }
        return files;
    }

    /**
     * 根据给定的定义文件集合总读取所有插件定义
     *
     * @return
     */
    public static List<PluginInfo> loadPluginDefines(List<URL> defineFiles) {
        List<PluginInfo> pluginDefines = new ArrayList<PluginInfo>();
        if (defineFiles != null && defineFiles.size() > 0) {
            for (URL fileUrl : defineFiles) {
                try {
                    fillPluginDefine(fileUrl.openStream(), pluginDefines);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        return pluginDefines;
    }

    /**
     * 根据给定的插件定义集合，动态加载插件类对象
     *
     * @return
     */
    public static List<AbstractPlugin> loadPlugins(AgentClassLoader loader, List<PluginInfo> defines) {
        List<AbstractPlugin> plugins = new ArrayList<AbstractPlugin>();
        for (PluginInfo define : defines) {
            try {
                AbstractPlugin plugin = (AbstractPlugin) Class.forName(define.getClazz(), true, loader).newInstance();
                plugin.setPluginName(define.getName());
                plugins.add(plugin);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return plugins;
    }

    public static List<AbstractPlugin> loadPlugins(AgentClassLoader classLoader) throws IOException {
        List<URL> files = PluginLoader.loadPluginDefineFiles(classLoader);
        List<PluginInfo> defines = PluginLoader.loadPluginDefines(files);
        return loadPlugins(classLoader, defines);
    }

    /**
     * 从一个定义文件中读取插件定义，追加到集合
     *
     * @param input            jar包里的plugin.define文件输入流
     * @param pluginDefineList 读取到的plugin定义集合
     * @throws IOException
     */
    private static void fillPluginDefine(InputStream input, List<PluginInfo> pluginDefineList) throws IOException {
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(input));
            String pluginDefine = null;
            while ((pluginDefine = reader.readLine()) != null) {
                if (pluginDefine == null || pluginDefine.trim().length() == 0 || pluginDefine.trim().startsWith("#")) {
                    continue;
                }
                pluginDefine = pluginDefine.trim();
                String[] defs = pluginDefine.split("=");
                if (defs.length != 2) {
                    continue;
                }
                pluginDefineList.add(new PluginInfo(defs[0], defs[1]));
            }
        } finally {
            input.close();
        }
    }

}
