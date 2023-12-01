package com.zary.sniffer.agent.plugin.struts2.handler;



import com.zary.sniffer.agent.core.log.LogProducer;
import com.zary.sniffer.agent.core.log.LogUtil;
import com.zary.sniffer.agent.core.plugin.define.HandlerBeforeResult;
import com.zary.sniffer.agent.core.plugin.handler.IInstanceMethodHandler;
import com.zary.sniffer.agent.plugin.servlet.core.ContentCachingResponseWrapper;
import com.zary.sniffer.agent.plugin.servlet.core.HttpServletUtil;
import com.zary.sniffer.tracing.TracerManager;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.lang.reflect.Method;


public class ExecuteOperationsHandler implements IInstanceMethodHandler {
    private static final LogProducer logger = LogUtil.getLogProducer();
    private static final Integer INDEX = 1;

    @Override
    public void onBefore(Object instance, Method method, Object[] allArguments, HandlerBeforeResult result) {
        HttpServletRequest request = (HttpServletRequest) allArguments[0];
        boolean isIgnorePluginRequest = HttpServletUtil.isIgnorePluginRequest(request);
        if (isIgnorePluginRequest) {
            return;
        }
        boolean hasFingerPrint = HttpServletUtil.hasRequestFingerprint(request);
        if (!hasFingerPrint) {
            /** 需要wrapper */
            HttpServletResponse response = (HttpServletResponse) allArguments[1];
            //原参数：ServletRequest request, ServletResponse response, FilterChain filterChain
            ContentCachingResponseWrapper wrapper = new ContentCachingResponseWrapper(response);
            //新参数：ServletRequest request, ContentCachingResponseWrapper wrapper, FilterChain filterChain, ServletResponse response
            Object[] newArguments = new Object[]{allArguments[0], wrapper, allArguments[2], allArguments[1]};
            result.setNewArguments(newArguments);
        }
    }

    @Override
    public Object onAfter(Object instance, Method method, Object[] allArguments, Object returnValue) throws Throwable {
        boolean isWrapperResponse = HttpServletUtil.isWrapperResponse(allArguments[INDEX]);
        /** 响应是不是已经被替换为 wrapper？*/
        if (isWrapperResponse) {
            /** 参数是wrapper */
            ContentCachingResponseWrapper wrapper = (ContentCachingResponseWrapper) allArguments[INDEX];
            /** 是否4个参数：只有请求到达第一个filter执行时是4个参数(request,wrapper,chain,response)，后续的filter执行是3个参数(request,wrapper,chain) */
            boolean isFirstFilterAfter = allArguments.length == 4;
            if (isFirstFilterAfter) {
                /**所有的filter和action都已经执行完毕，离开应用程序的最后一步，理论上只会执行一次*/
                /**写脚本逻辑*/
                HttpServletRequest request = (HttpServletRequest) allArguments[0];
                HttpServletUtil.flushAdmScript(wrapper);
                /**响应头追加(response.write之前)*/
                HttpServletResponse response = (HttpServletResponse) allArguments[3];
                HttpServletUtil.flushThreadCookie(response);
                HttpServletUtil.flushThreadHeader(response);
                /**解除wrapper*/
                ServletOutputStream outputStream = response.getOutputStream();
                outputStream.write(wrapper.getContentAsByteArray());
                outputStream.flush();
                /** 释放线程数据：此处为一个请求离开应用程序的最后一站，由于线程重用，需要完全释放数据避免污染 */
                TracerManager.getCurTracer().reset();
            } else {
                /**3个：说明是每个请求的除第一个filter以外后续的filter的after，不同应用程序可能触发多次，不用处理*/
                return returnValue;
            }
        } else {
            /**否:说明未被包装过，可能是 有指纹 或 URL是静态资源，理论上不存在*/
            HttpServletRequest request = (HttpServletRequest) allArguments[0];
            boolean isStaticRequest = HttpServletUtil.isStaticRequest(request.getRequestURL().toString());
            if (!isStaticRequest) {
                HttpServletResponse response = (HttpServletResponse) allArguments[1];
                HttpServletUtil.flushThreadCookie(response);
                HttpServletUtil.flushThreadHeader(response);
            }
        }
        return returnValue;
    }
}
