package com.zary.sniffer.agent.core.plugin.interceptor;

import com.zary.sniffer.util.StringUtil;
import com.zary.sniffer.agent.core.log.LogUtil;
import com.zary.sniffer.agent.core.plugin.define.HandlerBeforeResult;
import com.zary.sniffer.agent.core.plugin.handler.IInstanceMethodHandler;
import com.zary.sniffer.agent.core.plugin.loader.InterceptorHandlerClassLoader;
import net.bytebuddy.implementation.bind.annotation.*;

import java.lang.reflect.Method;
import java.util.concurrent.Callable;

/**
 * 拦截器实现类：实例函数拦截器
 *
 * @author xulibo
 */
public class InstanceMethodInterceptor {

    /**
     * 拦截处理类
     */
    private IInstanceMethodHandler handler;

    private String agentArgs;

    /**
     * 构造函数
     *
     * @param handlerImplName point提供的具体拦截处理类名
     * @param classLoader
     */
    public InstanceMethodInterceptor(String agentArgs, String handlerImplName, ClassLoader classLoader) {
        try {
            this.handler = InterceptorHandlerClassLoader.load(agentArgs, handlerImplName, classLoader);
            this.agentArgs = agentArgs;
        } catch (Throwable t) {
            LogUtil.error(String.format(
                    "InstanceMethodInterceptor::create failed.[%s %s]",
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
     * @param zuper
     * @param method
     */
    @RuntimeType
    public Object intercept(@This Object obj,
                            @AllArguments Object[] allArguments,
                            @SuperCall Callable<?> zuper,
                            @Origin Method method
    ) throws Throwable {
        /** 1.执行before */
        HandlerBeforeResult result = new HandlerBeforeResult();
        try {
            //输出debug：排除ResultSetHandler调用太频繁
            if (!handler.getClass().getName().contains("ResultSetHandler")) {
                LogUtil.debug("InstanceMethodInterceptor::enter",
                        String.format("thread=%s,%nobj=%s,%nmethod=%s,%nallArguments=%s",
                                Thread.currentThread().getName(),
                                obj,
                                method,
                                (allArguments == null ? "" : StringUtil.join(allArguments, "|"))
                        ));
            }
            handler.onBefore(agentArgs, obj, method, allArguments, result);
        } catch (Throwable t) {
            LogUtil.warn(String.format(
                    "InstanceMethodInterceptor::onbefore failed.[%s.%s]",
                    obj.getClass(),
                    method.getName()
            ), t);
        }
        /** 2.执行函数本身 */
        Object returnValue = null;
        try {
            //如果需要中断执行，直接读取返回值，否则继续调用被代理函数
            if (!result.isContinue()) {
                returnValue = result.getReturnValue();
            } else {
                returnValue = zuper.call();
            }
        } catch (Throwable t) {
            if (!result.isSubstitute()) {
                LogUtil.warn(String.format(
                        "InstanceMethodInterceptor::supercall failed.[%s.%s]",
                        obj.getClass(),
                        method.getName()
                ), t);
                //异常要继续抛出，防止上层调用根据异常驱动业务逻辑
                throw t;
            }
        } finally {
            /** 3.执行after */
            try {
                Object newReturnValue = handler.onAfter(agentArgs, obj, method, allArguments, returnValue);
                if (result.isSubstitute()) {
                    returnValue = newReturnValue;
                }
                //输出debug：排除ResultSetHandler调用太频繁
                if (!handler.getClass().getName().contains("ResultSetHandler")) {
                    LogUtil.debug("InstanceMethodInterceptor::exit",
                            String.format("thread=%s,%nobj=%s,%nmethod=%s,%nallArguments=%s,%nreturnValue=%s",
                                    Thread.currentThread().getName(),
                                    obj,
                                    method,
                                    (allArguments == null ? "" : StringUtil.join(allArguments, "|")),
                                    (returnValue == null ? "null" : returnValue.toString())
                            ));
                }
            } catch (Throwable t) {
                LogUtil.warn(String.format(
                        "InstanceMethodInterceptor::onafter failed.[%s.%s]",
                        obj.getClass(),
                        method.getName()
                ), t);
            }
        }
        /** 4.函数返回值 */
        return returnValue;
    }
}
