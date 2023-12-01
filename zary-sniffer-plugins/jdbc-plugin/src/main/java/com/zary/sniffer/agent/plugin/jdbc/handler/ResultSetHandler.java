package com.zary.sniffer.agent.plugin.jdbc.handler;


import com.zary.sniffer.config.PluginConsts;
import com.zary.sniffer.agent.core.plugin.define.HandlerBeforeResult;
import com.zary.sniffer.agent.core.plugin.handler.IInstanceMethodHandler;
import com.zary.sniffer.tracing.TracerManager;

import java.lang.reflect.Method;

public class ResultSetHandler implements IInstanceMethodHandler {
    @Override
    public void onBefore(Object instance, Method method, Object[] allArguments, HandlerBeforeResult result) throws Throwable {

    }

    @Override
    public Object onAfter(Object instance, Method method, Object[] allArguments, Object returnValue) throws Throwable {
        /** 如果next()返回true说明成功读取到一行数据，累加计数器并缓存在线程中，请求离开时会依据缓存对数据行数进行修正 */
        String reskey = PluginConsts.KEY_RESULT_SET + instance.toString();
        int count = 0;
        Object resLines = TracerManager.getCurTracer().acquireOtherThreadData(reskey);
        if (resLines != null) {
            count = Integer.parseInt(resLines + "");
        }
        if ((Boolean) returnValue) {
            count = count + 1;
        }
        TracerManager.getCurTracer().fillOtherThreadData(reskey, count);
        return returnValue;
    }
}
