package com.zary.sniffer.agent.plugin.test.handler;


import com.zary.sniffer.agent.core.log.LogProducer;
import com.zary.sniffer.agent.core.log.LogUtil;
import com.zary.sniffer.agent.core.plugin.define.HandlerBeforeResult;
import com.zary.sniffer.agent.core.plugin.handler.IInstanceMethodHandler;
import com.zary.sniffer.agent.runtime.PluginReflectUtil;


import java.lang.reflect.Method;


public class TestHandler implements IInstanceMethodHandler {

    private static final LogProducer logger = LogUtil.getLogProducer();

    @Override
    public void onBefore(Object instance, Method method, Object[] allArguments, HandlerBeforeResult result) {
        result.setSubstituteTrue();
    }

    @Override
    public Object onAfter(Object instance, Method method, Object[] allArguments, Object returnValue) {
        String dir = (String) PluginReflectUtil.execute("com.telit.microgenerator.util.DirUtil", "getBizDir", String.class, null, "D:\\dddd");
        System.out.println(dir);

        return returnValue;
    }

}
