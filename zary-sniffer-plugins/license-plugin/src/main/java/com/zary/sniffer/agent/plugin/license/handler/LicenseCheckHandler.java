package com.zary.sniffer.agent.plugin.license.handler;

import com.zary.sniffer.agent.core.plugin.define.HandlerBeforeResult;
import com.zary.sniffer.agent.core.plugin.handler.IInstanceMethodHandler;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.EnumSet;

public class LicenseCheckHandler implements IInstanceMethodHandler {
    @Override
    public void onBefore(Object o, Method method, Object[] objects, HandlerBeforeResult handlerBeforeResult) throws Throwable {
        handlerBeforeResult.setSubstituteTrue();
    }

    @Override
    public Object onAfter(Object instance, Method method, Object[] allArguments, Object returnValue) throws Throwable {
        return null;
    }


}
