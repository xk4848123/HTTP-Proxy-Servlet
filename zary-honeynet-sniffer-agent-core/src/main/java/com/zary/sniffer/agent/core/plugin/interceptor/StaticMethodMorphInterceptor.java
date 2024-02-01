package com.zary.sniffer.agent.core.plugin.interceptor;

import com.zary.sniffer.util.StringUtil;
import com.zary.sniffer.agent.core.log.LogUtil;
import com.zary.sniffer.agent.core.plugin.define.HandlerBeforeResult;
import com.zary.sniffer.agent.core.plugin.define.IMorphCall;
import com.zary.sniffer.agent.core.plugin.handler.IStaticMethodHandler;
import com.zary.sniffer.agent.core.plugin.loader.InterceptorHandlerClassLoader;
import net.bytebuddy.implementation.bind.annotation.AllArguments;
import net.bytebuddy.implementation.bind.annotation.Morph;
import net.bytebuddy.implementation.bind.annotation.Origin;
import net.bytebuddy.implementation.bind.annotation.RuntimeType;

import java.lang.reflect.Method;

/**
 * 拦截器实现类：静态函数拦截器
 */
public class StaticMethodMorphInterceptor {
    /**
     * 拦截处理类名称
     */
    private String handlerName;

    private String agentArgs;

    /**
     * 构造函数
     *
     * @param handlerImplName
     */
    public StaticMethodMorphInterceptor(String agentArgs, String handlerImplName) {
        this.handlerName = handlerImplName;
        this.agentArgs = agentArgs;
    }

    /**
     * 拦截函数：转发给handler处理
     *
     * @param clazz
     * @param allArguments
     * @param method
     * @param zuper
     * @return
     */
    @RuntimeType
    public Object intercept(@Origin Class<?> clazz,
                            @AllArguments Object[] allArguments,
                            @Origin Method method,
                            @Morph IMorphCall zuper
    ) throws Throwable {
        /** 1.执行before */
        HandlerBeforeResult result = new HandlerBeforeResult();
        //从目标类所在classloader加载拦截类
        IStaticMethodHandler handler = null;
        try {
            handler = InterceptorHandlerClassLoader.load(this.agentArgs, this.handlerName, clazz.getClassLoader());
            if (handler == null) {
                throw new RuntimeException();
            }
        } catch (Throwable t) {
            LogUtil.warn(String.format(
                    "StaticMethodMorphInterceptor::handler load failed.[%s.%s]",
                    handlerName,
                    clazz.getClassLoader()
            ), t);
        }
        try {
            LogUtil.debug("StaticMethodMorphInterceptor::enter",
                    String.format("thread=%s,%nclass=%s,%nmethod=%s,%nallArguments=%s",
                            Thread.currentThread().getName(),
                            clazz,
                            method,
                            (allArguments == null ? "" : StringUtil.join(allArguments, "|"))
                    ));
            handler.onBefore(this.agentArgs, clazz, method, allArguments, result);
        } catch (Throwable t) {
            LogUtil.warn(String.format(
                    "StaticMethodMorphInterceptor::onbefore failed.[%s.%s]",
                    clazz,
                    method.getName()
            ), t);
        }
        /** 2.执行函数本身 */
        Object returnValue = null;
        //后续执行使用的参数:onBefore有变动就用变动过的，没有变动就用原来的
        Object[] newArguments = (result.getNewArguments() == null) ? allArguments : result.getNewArguments();
        try {
            //如果需要中断执行，直接读取返回值，否则继续调用被代理函数
            if (!result.isContinue()) {
                returnValue = result.getReturnValue();
            } else {
                returnValue = zuper.call(newArguments);
            }
        } catch (Throwable t) {
            LogUtil.warn(String.format(
                    "StaticMethodMorphInterceptor::supercall failed.[%s.%s]",
                    clazz,
                    method.getName()
            ), t);
            //异常要继续抛出，防止上层调用根据异常驱动业务逻辑
            throw t;
        } finally {
            /** 3.执行after */
            try {
                handler.onAfter(this.agentArgs, clazz, method, newArguments, returnValue);
                LogUtil.debug("StaticMethodMorphInterceptor::exit",
                        String.format("thread=%s,%nclass=%s,%nmethod=%s,%nallArguments=%s,%nreturnValue=%s",
                                Thread.currentThread().getName(),
                                clazz,
                                method,
                                (allArguments == null ? "" : StringUtil.join(allArguments, "|")),
                                (returnValue == null ? "null" : returnValue.toString())
                        ));
            } catch (Throwable t) {
                LogUtil.warn(String.format(
                        "StaticMethodMorphInterceptor::onafter failed.[%s.%s]",
                        clazz,
                        method.getName()
                ), t);
            }
        }
        /** 4.函数返回值 */
        return returnValue;
    }
}
