package com.zary.sniffer.agent.core.plugin.interceptor;

import com.zary.sniffer.util.StringUtil;
import com.zary.sniffer.agent.core.log.LogUtil;
import com.zary.sniffer.agent.core.plugin.define.HandlerBeforeResult;
import com.zary.sniffer.agent.core.plugin.define.IMorphCall;
import com.zary.sniffer.agent.core.plugin.handler.IInstanceMethodHandler;
import com.zary.sniffer.agent.core.plugin.loader.InterceptorHandlerClassLoader;
import net.bytebuddy.implementation.bind.annotation.*;

import java.lang.reflect.Method;

/**
 * 拦截器实现类：实例函数拦截器
 */
public class InstanceMethodMorphInterceptor {
    /**
     * 拦截处理类
     */
    private IInstanceMethodHandler handler;

    private String agentArgs;

    /**
     * 构造函数
     *
     * @param agentArgs
     * @param handlerImplName point提供的具体拦截处理类名
     * @param classLoader
     */
    public InstanceMethodMorphInterceptor(String agentArgs, String handlerImplName, ClassLoader classLoader) {
        try {
            this.handler = InterceptorHandlerClassLoader.load(agentArgs, handlerImplName, classLoader);
            this.agentArgs = agentArgs;
        } catch (Throwable t) {
            LogUtil.error(String.format(
                    "InstanceMethodMorphInterceptor::create failed.[%s %s]",
                    handlerImplName,
                    classLoader
            ), t);
        }
    }

    /**
     * 拦截函数：转发给handler处理
     *
     * @param obj
     * @param allArguments
     * @param method
     * @param zuper
     * @return
     */
    @RuntimeType
    public Object intercept(@This Object obj,
                            @AllArguments Object[] allArguments,
                            @Origin Method method,
                            @Morph IMorphCall zuper
    ) throws Throwable {
        /** 1.执行before */
        HandlerBeforeResult result = new HandlerBeforeResult();
        try {
            LogUtil.debug("InstanceMethodMorphInterceptor::enter",
                    String.format("thread=%s,%nobj=%s,%nmethod=%s,%nallArguments=%s%n",
                            Thread.currentThread().getName(),
                            obj,
                            method,
                            (allArguments == null ? "" : StringUtil.join(allArguments, "|"))
                    ));
            handler.onBefore(this.agentArgs, obj, method, allArguments, result);
        } catch (Throwable t) {
            LogUtil.warn(String.format(
                    "InstanceMethodMorphInterceptor::onbefore failed.[%s.%s]",
                    obj.getClass(),
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
            if (!result.isSubstitute()) {
                LogUtil.warn(String.format(
                        "InstanceMethodMorphInterceptor::supercall failed.[%s.%s]",
                        obj.getClass(),
                        method.getName()
                ), t);
                //异常要继续抛出，防止上层调用根据异常驱动业务逻辑
                throw t;
            }
        } finally {
            /** 3.执行after */
            try {
                Object newReturnValue = handler.onAfter(this.agentArgs, obj, method, newArguments, returnValue);
                if (result.isSubstitute()) {
                    returnValue = newReturnValue;
                }
                LogUtil.debug("InstanceMethodMorphInterceptor::exit",
                        String.format("thread=%s,%nobj=%s,%nmethod=%s,%nallArguments=%s,%nreturnValue=%s",
                                Thread.currentThread().getName(),
                                obj,
                                method,
                                (newArguments == null ? "" : StringUtil.join(newArguments, "|")),
                                (returnValue == null ? "null" : returnValue.toString())
                        ));
            } catch (Throwable t) {
                LogUtil.warn(String.format(
                        "InstanceMethodMorphInterceptor::onafter failed.[%s.%s]",
                        obj.getClass(),
                        method.getName()
                ), t);
            }
        }
        /** 4.函数返回值 */
        return returnValue;
    }
}
