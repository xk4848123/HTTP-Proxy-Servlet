package com.zary.sniffer.agent.core.plugin.point;

import net.bytebuddy.description.method.MethodDescription;
import net.bytebuddy.matcher.ElementMatcher;

/**
 * 拦截点原型一：构造函数拦截点
 */
public interface IConstructorPoint {
    /**
     * 筛选器
     *
     * @return
     */
    ElementMatcher<MethodDescription> getConstructorMatcher();

    /**
     * 拦截器
     *
     * @return
     */
    String getHandlerClassName();
}
