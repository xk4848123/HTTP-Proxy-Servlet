package com.zary.sniffer.agent.core.plugin.handler;

import com.zary.sniffer.agent.core.plugin.define.HandlerBeforeResult;

import java.lang.reflect.Method;

/**
 * 拦截处理类：静态调用函数
 */
public interface IStaticMethodHandler {

    void onBefore(Class<?> clz, Method method, Object[] allArguments, HandlerBeforeResult result) throws Throwable;

    Object onAfter(Class<?> clz, Method method, Object[] allArguments, Object returnValue) throws Throwable;
}
