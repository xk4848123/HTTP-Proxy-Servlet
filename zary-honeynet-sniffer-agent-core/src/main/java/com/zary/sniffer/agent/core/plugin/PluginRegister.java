package com.zary.sniffer.agent.core.plugin;

import com.zary.sniffer.agent.core.log.LogUtil;
import com.zary.sniffer.agent.core.plugin.define.IMorphCall;
import com.zary.sniffer.agent.core.plugin.interceptor.*;
import com.zary.sniffer.agent.core.plugin.point.IConstructorPoint;
import com.zary.sniffer.agent.core.plugin.point.IInstanceMethodPoint;
import com.zary.sniffer.agent.core.plugin.point.IStaticMethodPoint;
import net.bytebuddy.agent.builder.AgentBuilder;
import net.bytebuddy.description.method.MethodDescription;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.dynamic.DynamicType;
import net.bytebuddy.implementation.MethodDelegation;
import net.bytebuddy.implementation.SuperMethodCall;
import net.bytebuddy.implementation.bind.annotation.Morph;
import net.bytebuddy.matcher.ElementMatcher;
import net.bytebuddy.utility.JavaModule;

import java.util.List;

public class PluginRegister {

    public AgentBuilder register(String agentArgs, AgentBuilder agentBuilder, List<AbstractPlugin> plugins) {
        for (int i = 0; i < plugins.size(); i++) {
            final AbstractPlugin plugin = plugins.get(i);
            LogUtil.info("Plugin" + i, plugin.toString());
            try {
                /** 1.插件类筛选器 */
                ElementMatcher<TypeDescription> pluginTypeMatcher = plugin.getPluginTypeMatcher();
                /** 2.构造函数拦截点注册 */
                IConstructorPoint[] constructorPoints = plugin.getConstructorPoints();
                if (constructorPoints != null && constructorPoints.length > 0) {
                    for (int j = 0; j < constructorPoints.length; j++) {
                        final IConstructorPoint point = constructorPoints[j];
                        AgentBuilder.Transformer transformer = (builder, typeDescription, classLoader, javaModule) -> {
                            String handlerName = point.getHandlerClassName();
                            ElementMatcher<MethodDescription> methodMatcher = point.getConstructorMatcher();
                            builder = builder.constructor(methodMatcher).intercept(
                                    SuperMethodCall.INSTANCE
                                            .andThen(MethodDelegation.withDefaultConfiguration()
                                                    .to(new ConstructorInterceptor(agentArgs, handlerName, classLoader))
                                            )
                            );
                            return builder;
                        };
                        //符合条件的类使用transformer转换
                        agentBuilder = agentBuilder.type(pluginTypeMatcher).transform(transformer);
                    }
                }

                /** 3.实例函数拦截点注册 */
                IInstanceMethodPoint[] instanceMethodPoints = plugin.getInstanceMethodPoints();
                if (instanceMethodPoints != null && instanceMethodPoints.length > 0) {
                    for (int j = 0; j < instanceMethodPoints.length; j++) {
                        final IInstanceMethodPoint point = instanceMethodPoints[j];
                        AgentBuilder.Transformer transformer = (builder, typeDescription, classLoader, javaModule) -> {
                            String handlerName = point.getHandlerClassName();
                            ElementMatcher<MethodDescription> methodMatcher = point.getMethodsMatcher();
                            boolean isMorph = point.isMorphArgs();
                            if (!isMorph) {//不需要带参数调用
                                builder = builder.method(methodMatcher).intercept(
                                        MethodDelegation.withDefaultConfiguration()
                                                .to(new InstanceMethodInterceptor(agentArgs, handlerName, classLoader))
                                );
                            } else {//需要带参数调用
                                builder = builder.method(methodMatcher).intercept(
                                        MethodDelegation.withDefaultConfiguration()
                                                .withBinders(
                                                        Morph.Binder.install(IMorphCall.class)
                                                )
                                                .to(new InstanceMethodMorphInterceptor(agentArgs, handlerName, classLoader))
                                );
                            }
                            return builder;
                        };
                        //符合条件的类使用transformer转换
                        agentBuilder = agentBuilder.type(pluginTypeMatcher).transform(transformer);
                    }
                }

                /** 4.静态函数拦截点注册 */
                IStaticMethodPoint[] staticMethodPoints = plugin.getStaticMethodPoints();
                if (staticMethodPoints != null && staticMethodPoints.length > 0) {
                    for (int j = 0; j < staticMethodPoints.length; j++) {
                        final IStaticMethodPoint point = staticMethodPoints[j];
                        AgentBuilder.Transformer transformer = (builder, typeDescription, classLoader, javaModule) -> {
                            String handlerName = point.getHandlerClassName();
                            ElementMatcher<MethodDescription> methodMatcher = point.getMethodsMatcher();
                            boolean isMorph = point.isMorphArgs();
                            if (!isMorph) {//不需要带参数调用
                                builder = builder.method(methodMatcher).intercept(
                                        MethodDelegation.withDefaultConfiguration()
                                                .to(new StaticMethodInterceptor(agentArgs, handlerName))
                                );
                            } else {//需要带参数调用
                                builder = builder.method(methodMatcher).intercept(
                                        MethodDelegation.withDefaultConfiguration()
                                                .withBinders(
                                                        Morph.Binder.install(IMorphCall.class)
                                                )
                                                .to(new StaticMethodMorphInterceptor(agentArgs, handlerName))
                                );
                            }
                            return builder;
                        };
                        //符合条件的类使用transformer转换
                        agentBuilder = agentBuilder.type(pluginTypeMatcher).transform(transformer);
                    }
                }
            } catch (Exception e) {
                LogUtil.warn("Register plugin failed:" + plugin, e);
            }
        }
        return agentBuilder;
    }

}
