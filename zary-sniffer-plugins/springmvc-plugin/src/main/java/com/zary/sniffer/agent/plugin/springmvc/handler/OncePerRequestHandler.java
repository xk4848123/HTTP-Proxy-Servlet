package com.zary.sniffer.agent.plugin.springmvc.handler;

import com.zary.sniffer.agent.plugin.servlet.core.ContentCachingResponseWrapper;
import com.zary.sniffer.agent.plugin.servlet.core.HttpServletUtil;
import com.zary.sniffer.config.ConfigCache;
import com.zary.sniffer.agent.core.log.LogProducer;
import com.zary.sniffer.agent.core.log.LogUtil;
import com.zary.sniffer.agent.core.plugin.define.HandlerBeforeResult;
import com.zary.sniffer.agent.core.plugin.handler.IInstanceMethodHandler;
import com.zary.sniffer.tracing.TracerManager;

import javax.servlet.DispatcherType;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.lang.reflect.Method;


/**
 * 拦截处理器： OncePerRequestFilter
 * <p>
 * 1.before：执行wrapper逻辑以实现获取响应内容
 * 2.after：实现脚本注入、响应追加等逻辑
 * 3.很多filter继承自OncePerRequestFilter，所以该拦截器会执行多次，如FilterA、FilterB，一个请求执行的完整过程如下：FilterA.onBefore -> FilterB.onBefore -> FilterB.onAfter -> FilterA.onAfter
 * 4.只处理DispatcherType=REQUEST的请求，其余请求不管
 */
@Deprecated
public class OncePerRequestHandler implements IInstanceMethodHandler {
    private static final LogProducer logger = LogUtil.getLogProducer();

    @Override
    public void onBefore(Object instance, Method method, Object[] allArguments, HandlerBeforeResult result) {
        /** 响应是不是已经被替换为 wrapper？*/
        boolean isWrapperResponse = HttpServletUtil.isWrapperResponse(allArguments[1]);
        if (isWrapperResponse) {
            /** 此时进入后续filter，不处理 */
            return;
        } else {
            /** 此时是首个filter,需要进一步判断 */
            HttpServletRequest httpRequest = (HttpServletRequest) allArguments[0];
            //是否是静态文件后缀请求
            boolean isStaticReuqest = HttpServletUtil.isStaticRequest(httpRequest.getRequestURI());
            //是否是白名单匹配的请求
            boolean isAutoPass = ConfigCache.get().isAutoPass(httpRequest.getRequestURI());
            //是否是已经包含指纹的请求
            boolean hasFingerPrint = HttpServletUtil.hasRequestFingerprint(httpRequest);
            //是否是DispatcherType.REQUEST请求：https://blog.csdn.net/xiaokang123456kao/article/details/72885171
            boolean isTargetDispatcherType = (httpRequest.getDispatcherType() == DispatcherType.REQUEST);
            if (isStaticReuqest || isAutoPass || !isTargetDispatcherType || hasFingerPrint) {
                /** 不需要wrapper，探针不做处理 */
                return;
            } else {
                /** 需要wrapper */
                HttpServletResponse response = (HttpServletResponse) allArguments[1];
                //原参数：ServletRequest request, ServletResponse response, FilterChain filterChain
                ContentCachingResponseWrapper wrapper = new ContentCachingResponseWrapper(response);
                //新参数：ServletRequest request, ContentCachingResponseWrapper wrapper, FilterChain filterChain, ServletResponse response
                Object[] newArguments = new Object[]{allArguments[0], wrapper, allArguments[2], allArguments[1]};
                result.setNewArguments(newArguments);
            }
        }
    }

    @Override
    public Object onAfter(Object instance, Method method, Object[] allArguments, Object returnValue) throws Throwable {
        /** 响应是不是已经被替换为 wrapper？*/
        boolean isWrapperResponse = HttpServletUtil.isWrapperResponse(allArguments[1]);
        if (isWrapperResponse) {
            /** 此时进入后续filter，参数是wrapper */
            ContentCachingResponseWrapper wrapper = (ContentCachingResponseWrapper) allArguments[1];
            /** 是否4个参数：只有请求到达第一个filter执行时是4个参数(request,wrapper,chain,response)，后续的filter执行是3个参数(request,wrapper,chain) */
            boolean isFirstFilterAfter = allArguments.length == 4;
            if (isFirstFilterAfter) {
                /** 此时是一个请求执行完成后又回到第一层filter的after，是离开应用程序的最后一步，执行追加、写脚本逻辑 */
                HttpServletUtil.flushAdmScript(wrapper);
                /** 响应头追加(response.write之前) */
                HttpServletResponse response = (HttpServletResponse) allArguments[3];
                HttpServletUtil.flushThreadCookie(response);
                HttpServletUtil.flushThreadHeader(response);
                /** 解除wrapper */
                ServletOutputStream outputStream = response.getOutputStream();
                outputStream.write(wrapper.getContentAsByteArray());
                outputStream.flush();
//                outputStream.close();
//                PrintWriter writer = response.getWriter();
//                writer.write(new String(wrapper.getContentAsByteArray(), response.getCharacterEncoding()));
//                writer.flush();
                /** 释放线程数据：由于线程重用，需要完全释放数据避免污染 */
                TracerManager.getCurTracer().reset();
            } else {
                /** 此时是3个参数，除第一个filter以外后续的filter的after，根据应用程序配置的过滤器链数量不同可能触发多次，不用处理 */
                return returnValue;
            }
        }
        return returnValue;
    }
}
