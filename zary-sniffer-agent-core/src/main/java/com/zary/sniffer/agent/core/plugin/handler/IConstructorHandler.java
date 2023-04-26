package com.zary.sniffer.agent.core.plugin.handler;

/**
 * 拦截处理类：构造函数
 */
public interface IConstructorHandler {
    /**
     * 构造函数拦截
     * @param instance
     * @param allArguments
     */
    void onConstruct(Object instance, Object[] allArguments);
}
