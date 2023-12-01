package com.zary.sniffer.agent.plugin.struts2.handler;


import com.zary.sniffer.core.enums.PluginType;

import com.zary.sniffer.core.model.DataOperateInfo;
import com.zary.sniffer.core.model.ThreadDataInfo;
import com.zary.sniffer.core.model.WebRequestInfo;
import com.zary.sniffer.agent.core.log.LogProducer;
import com.zary.sniffer.agent.core.log.LogUtil;
import com.zary.sniffer.agent.core.plugin.define.HandlerBeforeResult;
import com.zary.sniffer.agent.core.plugin.handler.IInstanceMethodHandler;
import com.zary.sniffer.agent.plugin.servlet.core.HttpServletUtil;
import com.zary.sniffer.agent.runtime.ThreadDataUtil;
import com.zary.sniffer.tracing.TracerManager;
import com.zary.sniffer.transfer.wrapper.ThreadDataSender;
import com.zary.sniffer.util.DateUtil;
import com.zary.sniffer.util.StringUtil;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.lang.reflect.Method;
import java.util.List;

public class DispatcherHandler implements IInstanceMethodHandler {

    private static final LogProducer logger = LogUtil.getLogProducer();
    private static final Integer INDEX = 1;

    @Override
    public void onBefore(Object instance, Method method, Object[] allArguments, HandlerBeforeResult result) {
        /** 忽略插件的情景 */
        HttpServletRequest request = (HttpServletRequest) allArguments[0];
        boolean isIgnorePluginRequest = HttpServletUtil.isIgnorePluginRequest(request);
        if (isIgnorePluginRequest) {
            logger.debug("IGNORE_BEFORE:DispatcherHandler:" + request.getRequestURI(), "isIgnorePluginRequest");
            return;
        }
        /** 请求开始：创建threadData、requestInfo */
        WebRequestInfo reqInfo = new WebRequestInfo();
        HttpServletUtil.fillWebRequestInfo(reqInfo, request);

        reqInfo.setReqId(StringUtil.getGuid(false));
        reqInfo.setStarttime(DateUtil.getNowTimestamp());
        reqInfo.setPluginType(PluginType.struts2x);
        /** Span入栈：每个请求的第一个Span */
        ThreadDataUtil.createSpan(PluginType.struts2x);
    }

    @Override
    public Object onAfter(Object instance, Method method, Object[] allArguments, Object returnValue) {
        WebRequestInfo reqInfo = TracerManager.getCurTracer().acquireOtherThreadData(WebRequestInfo.IDENTITY);
        if (reqInfo == null || StringUtil.isEmpty(reqInfo.getReqId())) {
            TracerManager.getCurTracer().reset();
            logger.debug("IGNORE_AFTER:Struts2 DispatcherHandler:no reqInfo", "");
            return returnValue;
        }

        /** 出栈Span */
        ThreadDataUtil.popSpan(reqInfo, instance, method, allArguments, "Struts2DispatcherPlugin");

        /** 填充response信息：contentType、statusCode、cost */
        HttpServletResponse response = (HttpServletResponse) allArguments[INDEX];
        String contentType = response.getContentType();
        long curTime = DateUtil.getNowTimestamp();
        reqInfo.setRep_content_type(contentType);
        //requestInfo.setRep_headers(getResponseHeaders(response)); */
        reqInfo.setRep_code(response.getStatus() + "");
        reqInfo.setEndtime(curTime);
        reqInfo.setCost(curTime - reqInfo.getStarttime());

        /** 数据操作影响行数修正 */
        List<DataOperateInfo> dataOperateInfos = TracerManager.getCurTracer().acquireOtherThreadData(DataOperateInfo.LIST_IDENTITY);
        ThreadDataUtil.fillDataOperateResultCount(dataOperateInfos);

        ThreadDataInfo threadDataInfo = ThreadDataUtil.newTreadDataInfoFromTracer();
        /** 发送数据 */
        if (threadDataInfo.isValid() && ThreadDataUtil.uniteThreadDataAppId(threadDataInfo)) {
            ThreadDataSender.getInstance().send(threadDataInfo);
        } else {
            logger.debug("IGNORE_AFTER:Struts2 DispatcherHandler:no valid data." + reqInfo.getReq_url(), "");
        }
        TracerManager.getCurTracer().reset();

        return returnValue;
    }
}
