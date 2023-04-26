package com.zary.sniffer.agent.core;

import com.zary.sniffer.util.ExceptionUtil;
import com.zary.sniffer.util.PropUtil;
import com.zary.sniffer.util.SystemUtil;
import com.zary.sniffer.agent.core.consts.AdmxAgentConsts;
import com.zary.sniffer.agent.core.jdk9.ModuleExporter;
import com.zary.sniffer.agent.core.log.LogLevel;
import com.zary.sniffer.agent.core.log.LogUtil;
import com.zary.sniffer.agent.core.plugin.AbstractPlugin;
import com.zary.sniffer.agent.core.plugin.define.IMorphCall;
import com.zary.sniffer.agent.core.plugin.interceptor.*;
import com.zary.sniffer.agent.core.plugin.loader.AgentClassLoader;
import com.zary.sniffer.agent.core.plugin.loader.PluginLoader;
import com.zary.sniffer.agent.core.plugin.point.IConstructorPoint;
import com.zary.sniffer.agent.core.plugin.point.IInstanceMethodPoint;
import com.zary.sniffer.agent.core.plugin.point.IStaticMethodPoint;
import com.zary.sniffer.agent.core.transfer.AgentDataUtil;
import com.zary.sniffer.agent.core.transfer.http.RequestInfo;
import net.bytebuddy.ByteBuddy;
import net.bytebuddy.agent.builder.AgentBuilder;
import net.bytebuddy.description.method.MethodDescription;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.dynamic.DynamicType;
import net.bytebuddy.dynamic.scaffold.TypeValidation;
import net.bytebuddy.implementation.MethodDelegation;
import net.bytebuddy.implementation.SuperMethodCall;
import net.bytebuddy.implementation.bind.annotation.Morph;
import net.bytebuddy.matcher.ElementMatcher;
import net.bytebuddy.matcher.ElementMatchers;
import net.bytebuddy.utility.JavaModule;

import java.io.File;
import java.io.IOException;
import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.Instrumentation;
import java.security.ProtectionDomain;
import java.util.List;
import java.util.Properties;
import java.util.function.Consumer;

import static net.bytebuddy.matcher.ElementMatchers.nameContains;
import static net.bytebuddy.matcher.ElementMatchers.nameStartsWith;

public class AgentStarter {

    private final static int FILE_MAX_SIZE = 2048;

    private final static String USERNAME = "username";

    private final static String PASSWORD = "password";

    private final static String SERVER_URL = "serverUrl";

    public void startDev(Instrumentation inst, Consumer<ProtectionDomain> protectionDomainConsumer) {
        start0(inst, LogLevel.DEBUG, protectionDomainConsumer);
    }

    public void startDev(Instrumentation inst) {
        start0(inst, LogLevel.DEBUG, null);
    }

    public void start(Instrumentation inst, Consumer<ProtectionDomain> protectionDomainConsumer) {
        start0(inst, LogLevel.INFO, protectionDomainConsumer);
    }

    public void start(Instrumentation inst) {
        start0(inst, LogLevel.INFO, null);
    }

    private void start0(Instrumentation inst, LogLevel logLevel, Consumer<ProtectionDomain> protectionDomainConsumer) {

        try {
            Properties prop = PropUtil.readProperties();

            printBanner();

            logStart(logLevel);

            dataCollectStart(new RequestInfo((String) prop.get(USERNAME), (String) prop.get(PASSWORD), (String) prop.get(SERVER_URL)));

            agentStart(inst, protectionDomainConsumer);
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }


    private static void printBanner() {
        System.out.println(AdmxAgentConsts.banner_chars);
    }

    private void agentStart(Instrumentation inst, Consumer<ProtectionDomain> protectionDomainConsumer) throws IOException {
        AgentClassLoader classLoader = new AgentClassLoader(AgentStarter.class.getClassLoader(), new String[]{AdmxAgentConsts.plugin_dir});
        List<AbstractPlugin> plugins = PluginLoader.loadPlugins(classLoader);

        AgentBuilder agentBuilder = initAgentBuilder();
        ModuleExporter.export(inst, agentBuilder);
        agentBuilder = registerAgentPlugins(agentBuilder, plugins);
        AgentBuilder.Listener listener = initAgentListener();
        agentBuilder.with(listener).installOn(inst);
        if (protectionDomainConsumer != null) {
            inst.addTransformer(new ClassDumpTransformer(protectionDomainConsumer));
        }
        if (plugins.size() == 0) {
            LogUtil.error("agent load", "no plugin found");
        }
    }

    private static void dataCollectStart(RequestInfo requestInfo) {
        AgentDataUtil.start(requestInfo);
    }

    private static void logStart(LogLevel logLevel) throws Exception {
        String excutePath = SystemUtil.getExcutePath();
        LogUtil.start(excutePath + File.separator + "logs", FILE_MAX_SIZE, logLevel);
    }

    private static class ClassDumpTransformer implements ClassFileTransformer {

        private final Consumer<ProtectionDomain> domainConsumer;

        public ClassDumpTransformer(Consumer<ProtectionDomain> domainConsumer) {
            this.domainConsumer = domainConsumer;
        }

        public byte[] transform(ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain domain, byte[] classfileBuffer) {
            domainConsumer.accept(domain);
            return classfileBuffer;
        }
    }

    private AgentBuilder registerAgentPlugins(AgentBuilder agentBuilder, List<AbstractPlugin> plugins) {
        for (int i = 0; i < plugins.size(); i++) {
            final AbstractPlugin plugin = plugins.get(i);
            LogUtil.info("Plugin" + i, plugin.toString());
            try {
                ElementMatcher<TypeDescription> pluginTypeMatcher = plugin.getPluginTypeMatcher();
                IConstructorPoint[] constructorPoints = plugin.getConstructorPoints();
                if (constructorPoints != null) {
                    for (final IConstructorPoint point : constructorPoints) {
                        AgentBuilder.Transformer transformer = new AgentBuilder.Transformer() {
                            @Override
                            public DynamicType.Builder<?> transform(DynamicType.Builder<?> builder, TypeDescription typeDescription, ClassLoader classLoader, JavaModule javaModule) {
                                String handlerName = point.getHandlerClassName();
                                ElementMatcher<MethodDescription> methodMatcher = point.getConstructorMatcher();
                                builder = builder.constructor(methodMatcher).intercept(SuperMethodCall.INSTANCE.andThen(MethodDelegation.withDefaultConfiguration().to(new ConstructorInterceptor(handlerName, classLoader))));
                                return builder;
                            }
                        };
                        //符合条件的类使用transformer转换
                        agentBuilder = agentBuilder.type(pluginTypeMatcher).transform(transformer);
                    }
                }

                IInstanceMethodPoint[] instanceMethodPoints = plugin.getInstanceMethodPoints();
                if (instanceMethodPoints != null) {
                    for (final IInstanceMethodPoint point : instanceMethodPoints) {
                        AgentBuilder.Transformer transformer = new AgentBuilder.Transformer() {
                            @Override
                            public DynamicType.Builder<?> transform(DynamicType.Builder<?> builder, TypeDescription typeDescription, ClassLoader classLoader, JavaModule javaModule) {
                                String handlerName = point.getHandlerClassName();
                                ElementMatcher<MethodDescription> methodMatcher = point.getMethodsMatcher();
                                boolean isMorph = point.isMorphArgs();
                                if (!isMorph) {//不需要带参数调用
                                    builder = builder.method(methodMatcher).intercept(MethodDelegation.withDefaultConfiguration().to(new InstanceMethodInterceptor(handlerName, classLoader)));
                                } else {//需要带参数调用
                                    builder = builder.method(methodMatcher).intercept(MethodDelegation.withDefaultConfiguration().withBinders(Morph.Binder.install(IMorphCall.class)).to(new InstanceMethodMorphInterceptor(handlerName, classLoader)));
                                }
                                return builder;
                            }
                        };
                        //符合条件的类使用transformer转换
                        agentBuilder = agentBuilder.type(pluginTypeMatcher).transform(transformer);
                    }
                }

                IStaticMethodPoint[] staticMethodPoints = plugin.getStaticMethodPoints();
                if (staticMethodPoints != null) {
                    for (final IStaticMethodPoint point : staticMethodPoints) {
                        AgentBuilder.Transformer transformer = new AgentBuilder.Transformer() {
                            @Override
                            public DynamicType.Builder<?> transform(DynamicType.Builder<?> builder, TypeDescription typeDescription, ClassLoader classLoader, JavaModule javaModule) {
                                String handlerName = point.getHandlerClassName();
                                ElementMatcher<MethodDescription> methodMatcher = point.getMethodsMatcher();
                                boolean isMorph = point.isMorphArgs();
                                if (!isMorph) {//不需要带参数调用
                                    builder = builder.method(methodMatcher).intercept(MethodDelegation.withDefaultConfiguration().to(new StaticMethodInterceptor(handlerName)));
                                } else {//需要带参数调用
                                    builder = builder.method(methodMatcher).intercept(MethodDelegation.withDefaultConfiguration().withBinders(Morph.Binder.install(IMorphCall.class)).to(new StaticMethodMorphInterceptor(handlerName)));
                                }
                                return builder;
                            }
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

    private AgentBuilder initAgentBuilder() {
        final ByteBuddy byteBuddy = new ByteBuddy().with(TypeValidation.of(false));
        //初始化builder，忽略不应该加载的包
        return new AgentBuilder.Default(byteBuddy).ignore(nameStartsWith(AdmxAgentConsts.agent_pakage).or(nameContains("net.bytebuddy.")).or(nameStartsWith("org.slf4j.")).or(nameStartsWith("org.groovy.")).or(nameContains("javassist.")).or(nameContains(".asm.")).or(nameContains(".reflectasm.")).or(nameStartsWith("sun.reflect")).or(ElementMatchers.<TypeDescription>isSynthetic()));
    }


    private AgentBuilder.Listener initAgentListener() {
        return new AgentBuilder.Listener() {
            @Override
            public void onDiscovery(String s, ClassLoader classLoader, JavaModule javaModule, boolean b) {
                //jvm虚拟机加载类时触发，非常频繁，只用于开发调试输出
                if (s.endsWith("View")) {//s.endsWith("DispatcherServlet") ||
                    LogUtil.debug("OnDiscovery::", String.format("ClassLoader:%s %nString:%s %nJavaModule:%s %nBoolean:%s", classLoader + "", s, javaModule + "", b));
                }

            }

            @Override
            public void onTransformation(TypeDescription typeDescription, ClassLoader classLoader, JavaModule javaModule, boolean b, DynamicType dynamicType) {
                //插桩捕获目标type后执行transform时触发
                LogUtil.debug("OnTransformation::", String.format("ClassLoader:%s %nType:%s %nJavaModule:%s %nBoolean:%s %nDynamicType:%s", classLoader + "", typeDescription + "", javaModule + "", b, dynamicType + ""));
            }

            @Override
            public void onIgnored(TypeDescription typeDescription, ClassLoader classLoader, JavaModule javaModule, boolean b) {
                //插桩捕获忽略类时触发
            }

            @Override
            public void onError(String s, ClassLoader classLoader, JavaModule javaModule, boolean b, Throwable throwable) {
                //插桩执行目标函数发生错误时触发
                LogUtil.warn("OnError::", String.format("ClassLoader:%s %nString:%s %nJavaModule:%s %nBoolean:%s %nThrowable:%n%s", classLoader + "", s, javaModule + "", b, ExceptionUtil.getStackTrace(throwable)));
            }

            @Override
            public void onComplete(String s, ClassLoader classLoader, JavaModule javaModule, boolean b) {
                //插桩执行目标函数结束时触发
            }
        };
    }

}
