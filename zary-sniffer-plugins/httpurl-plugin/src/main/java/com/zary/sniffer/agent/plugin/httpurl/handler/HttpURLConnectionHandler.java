package com.zary.sniffer.agent.plugin.httpurl.handler;

import com.zary.sniffer.core.model.WebRequestInfo;
import com.zary.sniffer.config.PluginConsts;
import com.zary.sniffer.agent.core.log.LogProducer;
import com.zary.sniffer.agent.core.log.LogUtil;
import com.zary.sniffer.agent.core.plugin.define.HandlerBeforeResult;
import com.zary.sniffer.agent.core.plugin.handler.IInstanceMethodHandler;
import com.zary.sniffer.tracing.TracerManager;
import com.zary.sniffer.util.StringUtil;

import java.lang.reflect.Method;
import java.net.HttpURLConnection;

/**
 * 拦截处理器
 * <p>
 * 1.before：
 * 2.after：如果当前线程有有效的fingerprint/sessionid，则发起的http请求都附加上
 */
public class HttpURLConnectionHandler implements IInstanceMethodHandler {
    private static final LogProducer logger = LogUtil.getLogProducer();

    @Override
    public void onBefore(Object instance, Method method, Object[] allArguments, HandlerBeforeResult result) throws Throwable {
        WebRequestInfo reqInfo = TracerManager.getCurTracer().acquireOtherThreadData(WebRequestInfo.IDENTITY);
        if (reqInfo == null || StringUtil.isEmpty(reqInfo.getReqId())) {
            TracerManager.getCurTracer().reset();
            logger.debug("IGNORE_BEFORE:HttpURLConnectionHandler:no reqInfo", "");
            return;
        }
        /** 追加请求头 */
        HttpURLConnection connection = (HttpURLConnection) instance;
        if (!StringUtil.isEmpty(reqInfo.getSession_id())) {
            connection.setRequestProperty(PluginConsts.KEY_CLIENT_SESSION_ID, reqInfo.getSession_id());
        }
        if (!StringUtil.isEmpty(reqInfo.getFingerprint())) {
            connection.setRequestProperty(PluginConsts.KEY_CLIENT_FINGERPRINT, reqInfo.getFingerprint());
        }
    }

    @Override
    public Object onAfter(Object instance, Method method, Object[] allArguments, Object returnValue) throws Throwable {
        return returnValue;
    }
}
