package com.zary.sniffer.agent.core.plugin.interceptor;

import com.zary.sniffer.util.StringUtil;
import com.zary.sniffer.agent.core.log.LogUtil;
import com.zary.sniffer.agent.core.plugin.handler.IConstructorHandler;
import com.zary.sniffer.agent.core.plugin.loader.InterceptorHandlerClassLoader;
import net.bytebuddy.implementation.bind.annotation.AllArguments;
import net.bytebuddy.implementation.bind.annotation.RuntimeType;
import net.bytebuddy.implementation.bind.annotation.This;

/**
 * 拦截器实现类：构造函数拦截器
 */
public class ConstructorInterceptor {
    /**
     * 拦截处理类
     */
    private IConstructorHandler handler;

    /**
     * 构造函数
     *
     * @param handlerImplName point提供的具体拦截处理类名
     * @param classLoader
     */
    public ConstructorInterceptor(String handlerImplName, ClassLoader classLoader) {
        try {
            handler = InterceptorHandlerClassLoader.load(handlerImplName, classLoader);
        } catch (Throwable t) {
            LogUtil.error("ConstructorInterceptor::create failed.", t);
        }
    }

    /**
     * 拦截函数：转发给handler处理
     *
     * @param obj
     * @param allArguments
     */
    @RuntimeType
    public void intercept(@This Object obj, @AllArguments Object[] allArguments) {
        try {
            handler.onConstruct(obj, allArguments);
            LogUtil.debug("ConstructorInterceptor::",
                    String.format("thread=%s,%nobj=%s,%nallArguments=%s",
                            Thread.currentThread().getName(),
                            obj,
                            (allArguments == null ? "" : StringUtil.join(allArguments,"|"))
                    ));
        } catch (Throwable t) {
            LogUtil.warn("ConstructorInterceptor::intercept failed.", t);
        }

    }
}
