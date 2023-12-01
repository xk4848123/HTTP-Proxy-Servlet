package com.zary.sniffer.agent.plugin.okhttp3.handler;


import com.zary.sniffer.core.model.WebRequestInfo;

import com.zary.sniffer.config.PluginConsts;
import com.zary.sniffer.agent.core.log.LogProducer;
import com.zary.sniffer.agent.core.log.LogUtil;
import com.zary.sniffer.agent.core.plugin.define.HandlerBeforeResult;
import com.zary.sniffer.agent.core.plugin.handler.IInstanceMethodHandler;
import com.zary.sniffer.tracing.TracerManager;
import com.zary.sniffer.util.StringUtil;
import okhttp3.Request;
import java.lang.reflect.Method;

/**
 * 拦截处理器：OkHttpClientCallHandler
 * <p>
 * 1.before：如果当前线程数据中包含凭据，则附加到当前线程发起的请求的header中
 * 2.after：
 */
public class OkHttpClientCallHandler implements IInstanceMethodHandler {
    private static final LogProducer logger = LogUtil.getLogProducer();

    @Override
    public void onBefore(Object instance, Method method, Object[] allArguments, HandlerBeforeResult result) throws Throwable {
        WebRequestInfo reqInfo = TracerManager.getCurTracer().acquireOtherThreadData(WebRequestInfo.IDENTITY);
        if (reqInfo == null || StringUtil.isEmpty(reqInfo.getReqId())) {
            TracerManager.getCurTracer().reset();
            logger.debug("IGNORE_CONSTRUCT:OkHttp3 OkHttpClientCallHandler:no reqInfo", "");
            return;
        }
        /** 如果当前线程数据中包含凭据，则附加到当前线程发起的请求的header中 */
        if (allArguments != null && allArguments.length > 0 && allArguments[0] instanceof Request) {
            Request request = (Request) allArguments[0];
            Request.Builder builder = request.newBuilder();
            if (!StringUtil.isEmpty(reqInfo.getSession_id()))
                builder.addHeader(PluginConsts.KEY_CLIENT_SESSION_ID, reqInfo.getSession_id());
            if (!StringUtil.isEmpty(reqInfo.getFingerprint()))
                builder.addHeader(PluginConsts.KEY_CLIENT_FINGERPRINT, reqInfo.getFingerprint());

            Object[] newArguments = new Object[]{builder.build()};
            result.setNewArguments(newArguments);
        }
    }

    @Override
    public Object onAfter(Object instance, Method method, Object[] allArguments, Object returnValue) throws Throwable {
        return returnValue;
    }
}
