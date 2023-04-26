package com.zary.sniffer.agent.core.plugin.point;

import net.bytebuddy.description.method.MethodDescription;
import net.bytebuddy.matcher.ElementMatcher;

/**
 * 拦截点原型二：实例化函数拦截点
 */
public interface IInstanceMethodPoint {
    /**
     * 筛选器
     *
     * @return
     */
    ElementMatcher<MethodDescription> getMethodsMatcher();

    /**
     * 拦截器
     *
     * @return
     */
    String getHandlerClassName();

    /**
     * 是否需要篡改参数(@Morph)
     * @return
     */
    boolean isMorphArgs();
}
