package com.zary.sniffer.agent.core.plugin.handler;

/**
 * 拦截处理类：构造函数
 */
public interface IConstructorHandler {

    void onConstruct(String root, Object instance, Object[] allArguments);
}
