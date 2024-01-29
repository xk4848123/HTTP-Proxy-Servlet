package com.zary.sniffer.agent.core.plugin.handler;

import com.zary.sniffer.agent.core.plugin.define.HandlerBeforeResult;

import java.lang.reflect.Method;

/**
 * iinstance方法处理程序
 * 拦截处理类：实例调用函数
 *
 * @author xulibo
 * @date 2020/04/26
 */
public interface IInstanceMethodHandler {

    void onBefore(Object instance, Method method, Object[] allArguments, HandlerBeforeResult result) throws Throwable;

    Object onAfter(Object instance, Method method, Object[] allArguments, Object returnValue) throws Throwable;
}
