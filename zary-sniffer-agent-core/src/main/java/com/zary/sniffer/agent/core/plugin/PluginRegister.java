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
import net.bytebuddy.implementation.MethodDelegation;
import net.bytebuddy.implementation.SuperMethodCall;
import net.bytebuddy.implementation.bind.annotation.Morph;
import net.bytebuddy.matcher.ElementMatcher;

import java.util.List;

public class PluginRegister {

    public void register(AgentBuilder agentBuilder, List<AbstractPlugin> plugins) {
        for (int i = 0; i < plugins.size(); i++) {
            final AbstractPlugin plugin = plugins.get(i);
            LogUtil.info("Plugin" + i, plugin.toString());
            try {
                ElementMatcher<TypeDescription> pluginTypeMatcher = registerConstructor(agentBuilder, plugin);

                registerInstanceMethod(agentBuilder, plugin, pluginTypeMatcher);

                registerStaticMethod(agentBuilder, plugin, pluginTypeMatcher);
            } catch (Exception e) {
                LogUtil.warn("Register plugin failed:" + plugin, e);
            }
        }
    }

    private static void registerStaticMethod(AgentBuilder agentBuilder, AbstractPlugin plugin, ElementMatcher<TypeDescription> pluginTypeMatcher) {
        IStaticMethodPoint[] staticMethodPoints = plugin.getStaticMethodPoints();
        if (staticMethodPoints != null) {
            for (final IStaticMethodPoint point : staticMethodPoints) {
                AgentBuilder.Transformer transformer = (builder, typeDescription, classLoader, javaModule) -> {
                    String handlerName = point.getHandlerClassName();
                    ElementMatcher<MethodDescription> methodMatcher = point.getMethodsMatcher();
                    boolean isMorph = point.isMorphArgs();
                    if (!isMorph) {//不需要带参数调用
                        builder = builder.method(methodMatcher).intercept(MethodDelegation.withDefaultConfiguration().to(new StaticMethodInterceptor(handlerName)));
                    } else {//需要带参数调用
                        builder = builder.method(methodMatcher).intercept(MethodDelegation.withDefaultConfiguration().withBinders(Morph.Binder.install(IMorphCall.class)).to(new StaticMethodMorphInterceptor(handlerName)));
                    }
                    return builder;
                };
                //符合条件的类使用transformer转换
                agentBuilder.type(pluginTypeMatcher).transform(transformer);
            }
        }
    }

    private static void registerInstanceMethod(AgentBuilder agentBuilder, AbstractPlugin plugin, ElementMatcher<TypeDescription> pluginTypeMatcher) {
        IInstanceMethodPoint[] instanceMethodPoints = plugin.getInstanceMethodPoints();
        if (instanceMethodPoints != null) {
            for (final IInstanceMethodPoint point : instanceMethodPoints) {
                LogUtil.info("register instance method handle class", point.getHandlerClassName());

                AgentBuilder.Transformer transformer = (builder, typeDescription, classLoader, javaModule) -> {
                    String handlerName = point.getHandlerClassName();
                    ElementMatcher<MethodDescription> methodMatcher = point.getMethodsMatcher();
                    boolean isMorph = point.isMorphArgs();
                    if (!isMorph) {//不需要带参数调用
                        builder = builder.method(methodMatcher).intercept(MethodDelegation.withDefaultConfiguration().to(new InstanceMethodInterceptor(handlerName, classLoader)));

                    } else {//需要带参数调用
                        builder = builder.method(methodMatcher).intercept(MethodDelegation.withDefaultConfiguration().withBinders(Morph.Binder.install(IMorphCall.class)).to(new InstanceMethodMorphInterceptor(handlerName, classLoader)));
                    }
                    return builder;
                };

                //符合条件的类使用transformer转换
                agentBuilder.type(pluginTypeMatcher).transform(transformer);
            }
        }
    }

    private static ElementMatcher<TypeDescription> registerConstructor(AgentBuilder agentBuilder, AbstractPlugin plugin) {
        ElementMatcher<TypeDescription> pluginTypeMatcher = plugin.getPluginTypeMatcher();
        IConstructorPoint[] constructorPoints = plugin.getConstructorPoints();
        if (constructorPoints != null) {
            for (final IConstructorPoint point : constructorPoints) {
                AgentBuilder.Transformer transformer = (builder, typeDescription, classLoader, javaModule) -> {
                    String handlerName = point.getHandlerClassName();
                    ElementMatcher<MethodDescription> methodMatcher = point.getConstructorMatcher();
                    builder = builder.constructor(methodMatcher).intercept(SuperMethodCall.INSTANCE.andThen(MethodDelegation.withDefaultConfiguration().to(new ConstructorInterceptor(handlerName, classLoader))));
                    return builder;
                };
                //符合条件的类使用transformer转换
                agentBuilder.type(pluginTypeMatcher).transform(transformer);
            }
        }
        return pluginTypeMatcher;
    }

}
