package com.zary.sniffer.agent.plugin.springmvc.handler;


import com.zary.sniffer.core.model.WebRequestInfo;
import com.zary.sniffer.agent.plugin.servlet.core.HttpServletUtil;
import com.zary.sniffer.config.PluginConsts;
import com.zary.sniffer.agent.core.plugin.define.HandlerBeforeResult;
import com.zary.sniffer.agent.core.plugin.handler.IInstanceMethodHandler;
import com.zary.sniffer.tracing.TracerManager;


import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.lang.reflect.Method;

/**
 * 拦截处理器：View.render
 * <p>
 * 1.before：
 * 2.after：在特定条件下输出脚本到页面
 */
public class ViewRenderHandler implements IInstanceMethodHandler {
    @Override
    public void onBefore(Object instance, Method method, Object[] allArguments, HandlerBeforeResult result) throws Throwable {

    }

    @Override
    public Object onAfter(Object instance, Method method, Object[] allArguments, Object returnValue) throws Throwable {
        /** 参数校验 */
        if (allArguments == null || allArguments.length != 3) {
            return returnValue;
        }
        if (!(allArguments[1] instanceof HttpServletRequest) || !(allArguments[2] instanceof HttpServletResponse)) {
            return returnValue;
        }
        /** 防止重复校验 */
        if (TracerManager.getCurTracer().hasOtherThreadData(PluginConsts.KEY_HAS_WRITE_SCRIPT)) {
            return returnValue;
        }
        WebRequestInfo reqInfo = TracerManager.getCurTracer().acquireOtherThreadData(WebRequestInfo.IDENTITY);
        if (null == reqInfo) {
            return returnValue;
        }
        /** 执行写脚本逻辑 */
        HttpServletRequest request = (HttpServletRequest) allArguments[1];
        HttpServletResponse response = (HttpServletResponse) allArguments[2];
        HttpServletUtil.flushAdmScript(request, response);
        /** 标记防止重复执行 */
        TracerManager.getCurTracer().fillOtherThreadData(PluginConsts.KEY_HAS_WRITE_SCRIPT, true);
        return returnValue;
    }
}
