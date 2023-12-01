package com.zary.sniffer.agent.core.jdk9;

import net.bytebuddy.agent.builder.AgentBuilder;

import java.lang.instrument.Instrumentation;

public class ModuleExporter {
    private static final String[] SNIFFER_CORE_CLASSES = {
            "com.zary.sniffer.agent.core.plugin.define.HandlerBeforeResult",
            "com.zary.sniffer.agent.core.plugin.define.IMorphCall",
            "com.zary.sniffer.agent.core.plugin.handler.IConstructorHandler",
            "com.zary.sniffer.agent.core.plugin.handler.IInstanceMethodHandler",
            "com.zary.sniffer.agent.core.plugin.handler.IStaticMethodHandler",
            "com.zary.sniffer.agent.core.plugin.interceptor.ConstructorInterceptor",
            "com.zary.sniffer.agent.core.plugin.interceptor.InstanceMethodInterceptor",
            "com.zary.sniffer.agent.core.plugin.interceptor.InstanceMethodMorphInterceptor",
            "com.zary.sniffer.agent.core.plugin.interceptor.StaticMethodInterceptor",
            "com.zary.sniffer.agent.core.plugin.interceptor.StaticMethodMorphInterceptor",
            "com.zary.sniffer.agent.core.plugin.point.IConstructorPoint",
            "com.zary.sniffer.agent.core.plugin.point.IInstanceMethodPoint",
            "com.zary.sniffer.agent.core.plugin.point.IStaticMethodPoint"
    };


    private static final String[] BYTEBUDDY_CORE_CLASSES = {
            "com.zary.sniffer.agent.depencies.net.bytebuddy.implementation.bind.annotation.RuntimeType",
            "com.zary.sniffer.agent.depencies.net.bytebuddy.implementation.bind.annotation.This",
            "com.zary.sniffer.agent.depencies.net.bytebuddy.implementation.bind.annotation.AllArguments",
            "com.zary.sniffer.agent.depencies.net.bytebuddy.implementation.bind.annotation.AllArguments$Assignment",
            "com.zary.sniffer.agent.depencies.net.bytebuddy.implementation.bind.annotation.SuperCall",
            "com.zary.sniffer.agent.depencies.net.bytebuddy.implementation.bind.annotation.Origin",
            "com.zary.sniffer.agent.depencies.net.bytebuddy.implementation.bind.annotation.Morph"
    };

    public static void export(Instrumentation instrumentation, AgentBuilder agentBuilder) {
        assureReadEdge(instrumentation, agentBuilder, BYTEBUDDY_CORE_CLASSES);
        assureReadEdge(instrumentation, agentBuilder, SNIFFER_CORE_CLASSES);
    }

    private static AgentBuilder assureReadEdge(Instrumentation instrumentation, AgentBuilder agentBuilder, String[] classes) {
        for (String className : classes) {
            try {
                agentBuilder = agentBuilder.assureReadEdgeFromAndTo(instrumentation, Class.forName(className));
            } catch (ClassNotFoundException e) {
                throw new UnsupportedOperationException("Fail to open read edge for class " + className + " to public access in JDK9+", e);
            }
        }
        return agentBuilder;
    }
}
