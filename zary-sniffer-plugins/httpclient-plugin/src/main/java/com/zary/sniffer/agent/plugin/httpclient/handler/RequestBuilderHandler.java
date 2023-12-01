package com.zary.sniffer.agent.plugin.httpclient.handler;


import com.zary.sniffer.core.model.WebRequestInfo;


import com.zary.sniffer.config.PluginConsts;
import com.zary.sniffer.agent.core.log.LogProducer;
import com.zary.sniffer.agent.core.log.LogUtil;
import com.zary.sniffer.agent.core.plugin.define.HandlerBeforeResult;
import com.zary.sniffer.agent.core.plugin.handler.IInstanceMethodHandler;
import com.zary.sniffer.tracing.TracerManager;
import com.zary.sniffer.util.StringUtil;
import org.apache.http.client.methods.RequestBuilder;

import java.lang.reflect.Method;


/**
 * 拦截处理器：RequestBuilder
 */
public class RequestBuilderHandler implements IInstanceMethodHandler {
    private static final LogProducer logger = LogUtil.getLogProducer();

    @Override
    public void onBefore(Object instance, Method method, Object[] allArguments, HandlerBeforeResult result) throws Throwable {
        WebRequestInfo reqInfo = TracerManager.getCurTracer().acquireOtherThreadData(WebRequestInfo.IDENTITY);
        if (null != reqInfo && instance != null && instance instanceof RequestBuilder) {
            RequestBuilder builder = (RequestBuilder) instance;
            if (!StringUtil.isEmpty(reqInfo.getSession_id())) {
                builder.setHeader(PluginConsts.KEY_CLIENT_SESSION_ID, reqInfo.getSession_id());
            }
            if (!StringUtil.isEmpty(reqInfo.getFingerprint())) {
                builder.setHeader(PluginConsts.KEY_CLIENT_FINGERPRINT, reqInfo.getFingerprint());
            }
        }
    }

    @Override
    public Object onAfter(Object instance, Method method, Object[] allArguments, Object returnValue) throws Throwable {
        return returnValue;
    }
}
