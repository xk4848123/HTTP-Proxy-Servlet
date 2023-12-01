package com.zary.sniffer.agent.plugin.httpclient.handler;


import com.zary.sniffer.core.model.WebRequestInfo;

import com.zary.sniffer.config.PluginConsts;
import com.zary.sniffer.agent.core.log.LogProducer;
import com.zary.sniffer.agent.core.log.LogUtil;
import com.zary.sniffer.agent.core.plugin.handler.IConstructorHandler;
import com.zary.sniffer.tracing.TracerManager;
import com.zary.sniffer.util.StringUtil;
import org.apache.http.Header;

import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.message.BasicHeader;


import java.util.ArrayList;

import java.util.List;

/**
 * 拦截处理器：ClientBuilderHandler
 */
public class ClientBuilderHandler implements IConstructorHandler {
    private static final LogProducer logger = LogUtil.getLogProducer();

    @Override
    public void onConstruct(Object instance, Object[] allArguments) {
        WebRequestInfo reqInfo = TracerManager.getCurTracer().acquireOtherThreadData(WebRequestInfo.IDENTITY);
        if (null != reqInfo && instance != null && instance instanceof HttpClientBuilder) {
            HttpClientBuilder builder = (HttpClientBuilder) instance;
            List<Header> headers = new ArrayList<Header>();
            if (!StringUtil.isEmpty(reqInfo.getSession_id())) {
                headers.add(new BasicHeader(PluginConsts.KEY_CLIENT_SESSION_ID, reqInfo.getSession_id()));
            }
            if (!StringUtil.isEmpty(reqInfo.getFingerprint())) {
                headers.add(new BasicHeader(PluginConsts.KEY_CLIENT_SESSION_ID, reqInfo.getFingerprint()));
            }
            if (headers.size() > 0) {
                builder.setDefaultHeaders(headers);
            }
        }
    }
}
