package com.zary.sniffer.agent.plugin.servlet.handler;


import com.zary.sniffer.core.enums.PluginType;

import com.zary.sniffer.core.model.DataOperateInfo;
import com.zary.sniffer.core.model.ThreadDataInfo;
import com.zary.sniffer.core.model.WebRequestInfo;
import com.zary.sniffer.config.ConfigCache;
import com.zary.sniffer.agent.core.log.LogProducer;
import com.zary.sniffer.agent.core.log.LogUtil;
import com.zary.sniffer.agent.core.plugin.define.HandlerBeforeResult;
import com.zary.sniffer.agent.core.plugin.handler.IInstanceMethodHandler;
import com.zary.sniffer.agent.plugin.servlet.core.ContentCachingResponseWrapper;
import com.zary.sniffer.agent.plugin.servlet.core.HttpServletUtil;
import com.zary.sniffer.agent.runtime.ThreadDataUtil;
import com.zary.sniffer.tracing.TracerManager;
import com.zary.sniffer.transfer.wrapper.ThreadDataSender;
import com.zary.sniffer.util.DateUtil;
import com.zary.sniffer.util.StringUtil;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.lang.reflect.Method;
import java.util.List;


/**
 * 拦截处理器：Servlet
 * <p>
 * 1.before：新请求进入时创建线程对象、Span对象
 * 2.after：完善线程对象数据，添加到发送队列
 */
public class ServletHandler implements IInstanceMethodHandler {
    private static final LogProducer logger = LogUtil.getLogProducer();
    @Override
    public void onBefore(Object instance, Method method, Object[] allArguments, HandlerBeforeResult result) throws Throwable {
        /** 忽略插件的情景 */
        HttpServletRequest request = (HttpServletRequest) allArguments[0];
        boolean isIgnorePluginRequest = HttpServletUtil.isIgnorePluginRequest(request);
        if (isIgnorePluginRequest) {
            logger.debug("IGNORE_BEFORE:ServletHandler:" + request.getRequestURI(), "isIgnorePluginRequest");
            return;
        }
        /** 请求开始：创建threadData、requestInfo */
        WebRequestInfo reqInfo = new WebRequestInfo();
        HttpServletUtil.fillWebRequestInfo(reqInfo, request);
        String appid = ConfigCache.get().getAppid(instance.getClass().getName());
        if (!StringUtil.isEmpty(appid)) {
            reqInfo.setAppId(appid);
        } else {
            TracerManager.getCurTracer().reset();
            logger.debug("IGNORE_BEFORE:ServletHandler:no appid", request.getRequestURL().toString());
            return;
        }
        reqInfo.setReqId(StringUtil.getGuid(false));
        reqInfo.setStarttime(DateUtil.getNowTimestamp());
        reqInfo.setPluginType(PluginType.servlet);

        /** Span入栈：每个请求的第一个Span */
        ThreadDataUtil.createSpan(PluginType.servlet);
        /** 指纹是否存在 */
        boolean hasFingerPrint = HttpServletUtil.hasRequestFingerprint(request);
        if (!hasFingerPrint) {
            HttpServletResponse response = (HttpServletResponse) allArguments[1];
            //原参数：HttpServletRequest request, HttpServletResponse response
            ContentCachingResponseWrapper wrapper = new ContentCachingResponseWrapper(response);
            //新参数：HttpServletRequest request, ContentCachingResponseWrapper wrapper, HttpServletResponse response
            Object[] newArguments = new Object[]{allArguments[0], wrapper, allArguments[1]};
            result.setNewArguments(newArguments);
        }
    }

    @Override
    public Object onAfter(Object instance, Method method, Object[] allArguments, Object returnValue) throws Throwable {
        WebRequestInfo reqInfo = TracerManager.getCurTracer().acquireOtherThreadData(WebRequestInfo.IDENTITY);
        if (reqInfo == null || StringUtil.isEmpty(reqInfo.getReqId())) {
            TracerManager.getCurTracer().reset();
            logger.debug("IGNORE_AFTER:ServletHandler:no reqInfo", "");
            return returnValue;
        }
        /** 出栈Span */
        ThreadDataUtil.popSpan(reqInfo, instance, method, allArguments, "ServletPlugin");
        /** 填充response信息：contentType、statusCode、cost */
        HttpServletResponse responseOrWrapper = (HttpServletResponse) allArguments[1];
        String contentType = responseOrWrapper.getContentType();
        long curTime = DateUtil.getNowTimestamp();
        reqInfo.setRep_content_type(contentType);
        //requestInfo.setRep_headers(getResponseHeaders(response)); */
        reqInfo.setRep_code(responseOrWrapper.getStatus() + "");
        reqInfo.setEndtime(curTime);
        reqInfo.setCost(curTime - reqInfo.getStarttime());
        /** 数据操作影响行数修正 */
        List<DataOperateInfo> dataOperateInfos = TracerManager.getCurTracer().acquireOtherThreadData(DataOperateInfo.LIST_IDENTITY);
        ThreadDataUtil.fillDataOperateResultCount(dataOperateInfos);
        /** 第二个参数是不是wrapper？ */
        boolean isWrapperResponse = HttpServletUtil.isWrapperResponse(allArguments[1]) && allArguments.length == 3;
        if (isWrapperResponse) {
            ContentCachingResponseWrapper wrapper = (ContentCachingResponseWrapper) allArguments[1];
            /**写脚本逻辑 */
            HttpServletRequest request = (HttpServletRequest) allArguments[0];
            HttpServletUtil.flushAdmScript(wrapper);
            /**解除wrapper*/
            HttpServletResponse response = (HttpServletResponse) allArguments[2];
            ServletOutputStream outputStream = response.getOutputStream();
            outputStream.write(wrapper.getContentAsByteArray());
            outputStream.flush();
        }

        ThreadDataInfo threadDataInfo = ThreadDataUtil.newTreadDataInfoFromTracer();
        /** 发送数据 */
        if (threadDataInfo.isValid() && ThreadDataUtil.uniteThreadDataAppId(threadDataInfo)) {
            ThreadDataSender.getInstance().send(threadDataInfo);
        } else {
            logger.debug("IGNORE_AFTER:Struts2 DispatcherHandler:no valid data." + reqInfo.getReq_url(), "");
        }
        /** 释放线程数据：此处为一个请求离开应用程序的最后一站，由于线程重用，需要完全释放数据避免污染 */
        TracerManager.getCurTracer().reset();
        return returnValue;
    }
}
