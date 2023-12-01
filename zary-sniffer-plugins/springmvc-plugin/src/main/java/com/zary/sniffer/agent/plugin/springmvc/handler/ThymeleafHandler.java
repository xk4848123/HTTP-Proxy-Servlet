package com.zary.sniffer.agent.plugin.springmvc.handler;


import com.zary.sniffer.agent.core.plugin.define.HandlerBeforeResult;
import com.zary.sniffer.agent.core.plugin.handler.IInstanceMethodHandler;

import java.io.Writer;
import java.lang.reflect.Method;

@Deprecated
public class ThymeleafHandler implements IInstanceMethodHandler {
    @Override
    public void onBefore(Object instance, Method method, Object[] allArguments, HandlerBeforeResult result) throws Throwable {

    }

    @Override
    public Object onAfter(Object instance, Method method, Object[] allArguments, Object returnValue) throws Throwable {
        if (allArguments != null && allArguments.length == 3) {
            Object obj = allArguments[2];
            if (obj instanceof Writer) {
                Writer writer = (Writer) obj;
                writer.write("hello!!!");
            }
        }
        return returnValue;
    }
}
