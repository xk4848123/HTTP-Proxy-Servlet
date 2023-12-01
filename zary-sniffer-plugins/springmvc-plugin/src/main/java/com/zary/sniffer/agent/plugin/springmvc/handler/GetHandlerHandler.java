package com.zary.sniffer.agent.plugin.springmvc.handler;


import com.zary.sniffer.config.ConfigCache;
import com.zary.sniffer.core.model.WebRequestInfo;
import com.zary.sniffer.agent.core.log.LogProducer;
import com.zary.sniffer.agent.core.log.LogUtil;
import com.zary.sniffer.agent.core.plugin.define.HandlerBeforeResult;
import com.zary.sniffer.agent.core.plugin.handler.IInstanceMethodHandler;
import com.zary.sniffer.tracing.TracerManager;
import com.zary.sniffer.util.StringUtil;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerExecutionChain;

import java.lang.reflect.Method;

/**
 * 拦截处理器： DispatcherServlet.getHandler
 * <p>
 * 1.before：
 * 2.after：找到请求处理器类，匹配appid
 */
public class GetHandlerHandler implements IInstanceMethodHandler {
    private static final LogProducer logger = LogUtil.getLogProducer();

    @Override
    public void onBefore(Object instance, Method method, Object[] allArguments, HandlerBeforeResult result) throws Throwable {

    }

    @Override
    public Object onAfter(Object instance, Method method, Object[] allArguments, Object returnValue) throws Throwable {
        WebRequestInfo reqInfo = TracerManager.getCurTracer().acquireOtherThreadData(WebRequestInfo.IDENTITY);
        if (null != reqInfo) {
            /** 获取当前请求handler，即为用户的控制器类，并识别appid */
            HandlerExecutionChain handlerChain = (HandlerExecutionChain) returnValue;
            if (null != handlerChain) {
                Object obj = handlerChain.getHandler();
                if (obj != null && obj instanceof HandlerMethod) {
                    String beanName = ((HandlerMethod) obj).getBeanType().getName();
                    String appid = ConfigCache.get().getAppid(beanName);
                    if (!StringUtil.isEmpty(appid)) {
                        reqInfo.setAppId(appid);
                    }
                }
            }
        }
        return returnValue;
    }
}
