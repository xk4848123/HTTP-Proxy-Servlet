package com.zary.sniffer.agent.plugin.servlet.handler;

import com.zary.sniffer.agent.core.plugin.define.HandlerBeforeResult;
import com.zary.sniffer.agent.core.plugin.handler.IInstanceMethodHandler;
import com.zary.sniffer.agent.plugin.servlet.proxy.HttpProxy;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.lang.reflect.Method;


public class ServletHandler implements IInstanceMethodHandler {

    @Override
    public void onBefore(Object instance, Method method, Object[] allArguments, HandlerBeforeResult result) throws Throwable {
        boolean isNeedProxy = HttpProxy.getInstance().service((HttpServletRequest) allArguments[0], (HttpServletResponse) allArguments[1]);
        if (isNeedProxy) {
            result.setReturnValue(null);
        }
    }

    @Override
    public Object onAfter(Object instance, Method method, Object[] allArguments, Object returnValue) throws Throwable {
        return returnValue;
    }
}
