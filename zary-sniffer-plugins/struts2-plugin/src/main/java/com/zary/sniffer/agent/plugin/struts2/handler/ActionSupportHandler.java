package com.zary.sniffer.agent.plugin.struts2.handler;


import com.zary.sniffer.core.enums.PluginType;

import com.zary.sniffer.core.model.WebRequestInfo;
import com.zary.sniffer.config.ConfigCache;
import com.zary.sniffer.agent.core.log.LogProducer;
import com.zary.sniffer.agent.core.log.LogUtil;
import com.zary.sniffer.agent.core.plugin.define.HandlerBeforeResult;
import com.zary.sniffer.agent.core.plugin.handler.IInstanceMethodHandler;
import com.zary.sniffer.agent.runtime.ThreadDataUtil;
import com.zary.sniffer.tracing.TracerManager;
import com.zary.sniffer.util.StringUtil;


import java.lang.reflect.Method;

public class ActionSupportHandler implements IInstanceMethodHandler {
    private static final LogProducer logger = LogUtil.getLogProducer();

    @Override
    public void onBefore(Object instance, Method method, Object[] allArguments, HandlerBeforeResult result) throws Throwable {
        WebRequestInfo reqInfo = TracerManager.getCurTracer().acquireOtherThreadData(WebRequestInfo.IDENTITY);
        if (reqInfo == null || StringUtil.isEmpty(reqInfo.getReqId())) {
            TracerManager.getCurTracer().reset();
            logger.debug("IGNORE_BEFORE:ActionSupportHandler:no reqInfo", "");
            return;
        }
        /** Span入栈：该span在after中同条件移除 */
        ThreadDataUtil.createSpan(PluginType.struts2x);
        /** 识别appid并更新threadData */
        String appid = ConfigCache.get().getAppid(instance.getClass().getName());
        if (!StringUtil.isEmpty(appid)) {
            reqInfo.setAppId(appid);
        } else {
            TracerManager.getCurTracer().reset();
            logger.debug("IGNORE_BEFORE:ActionSupportHandler:no appid", "");
        }
    }

    @Override
    public Object onAfter(Object instance, Method method, Object[] allArguments, Object returnValue) throws Throwable {
        WebRequestInfo reqInfo = TracerManager.getCurTracer().acquireOtherThreadData(WebRequestInfo.IDENTITY);
        if (reqInfo == null || StringUtil.isEmpty(reqInfo.getReqId())) {
            TracerManager.getCurTracer().reset();
            logger.debug("IGNORE_AFTER:ActionSupportHandler:no reqInfo", "");
            return returnValue;
        }
        /** Span出栈：补充信息更新到threadData */
        ThreadDataUtil.popSpan(reqInfo, instance, method, allArguments, "ActionSupportPlugin");
        return returnValue;
    }
}
