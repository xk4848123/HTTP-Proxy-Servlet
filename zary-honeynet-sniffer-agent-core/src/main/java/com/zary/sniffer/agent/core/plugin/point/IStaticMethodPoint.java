package com.zary.sniffer.agent.core.plugin.point;

import net.bytebuddy.description.method.MethodDescription;
import net.bytebuddy.matcher.ElementMatcher;

/**
 * 拦截点原型三：静态函数拦截点
 */
public interface IStaticMethodPoint {
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
