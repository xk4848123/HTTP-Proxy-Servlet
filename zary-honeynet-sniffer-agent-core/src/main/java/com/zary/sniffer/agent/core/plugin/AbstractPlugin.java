package com.zary.sniffer.agent.core.plugin;

import com.zary.sniffer.agent.core.plugin.point.IConstructorPoint;
import com.zary.sniffer.agent.core.plugin.point.IInstanceMethodPoint;
import com.zary.sniffer.agent.core.plugin.point.IStaticMethodPoint;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.matcher.ElementMatcher;

/**
 * 探测程序插件基类：提供插件函数原型定义
 *
 * @author xulibo 2019-12-12
 */
public abstract class AbstractPlugin {
    /**
     * 插件名称
     */
    private String pluginName;

    public String getPluginName() {
        return pluginName;
    }

    public void setPluginName(String pluginName) {
        this.pluginName = pluginName;
    }

    /**
     * 插件拦截的类筛选器(一个插件只有一个目标类过滤器，但是有多个拦截点)
     *
     * @return
     */
    public abstract ElementMatcher<TypeDescription> getPluginTypeMatcher();

    /**
     * 构造函数拦截点
     *
     * @return
     */
    public abstract IConstructorPoint[] getConstructorPoints();

    /**
     * 实例函数拦截点
     *
     * @return
     */
    public abstract IInstanceMethodPoint[] getInstanceMethodPoints();

    /**
     * 静态函数拦截点
     *
     * @return
     */
    public abstract IStaticMethodPoint[] getStaticMethodPoints();

}
