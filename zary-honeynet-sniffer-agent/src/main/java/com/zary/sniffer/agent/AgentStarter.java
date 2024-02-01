package com.zary.sniffer.agent;

import com.zary.sniffer.agent.core.consts.CoreConsts;
import com.zary.sniffer.agent.core.jdk9.ModuleExporter;
import com.zary.sniffer.agent.core.listener.DefaultAgentListener;
import com.zary.sniffer.agent.core.log.LogLevel;
import com.zary.sniffer.agent.core.log.LogUtil;
import com.zary.sniffer.agent.core.plugin.AbstractPlugin;
import com.zary.sniffer.agent.core.plugin.PluginRegister;
import com.zary.sniffer.agent.core.plugin.loader.AgentClassLoader;
import com.zary.sniffer.agent.core.plugin.loader.PluginLoader;
import com.zary.sniffer.config.ConfigLoader;
import com.zary.sniffer.util.FileUtil;
import com.zary.sniffer.util.SystemUtil;
import net.bytebuddy.ByteBuddy;
import net.bytebuddy.agent.builder.AgentBuilder;
import net.bytebuddy.dynamic.scaffold.TypeValidation;
import net.bytebuddy.matcher.ElementMatchers;

import javax.naming.ConfigurationException;
import java.io.File;
import java.io.IOException;
import java.lang.instrument.Instrumentation;
import java.util.List;

import static net.bytebuddy.matcher.ElementMatchers.nameContains;
import static net.bytebuddy.matcher.ElementMatchers.nameStartsWith;

public class AgentStarter {

    private final static int FILE_MAX_SIZE = 2048;

    public static void agentmain(String agentArgs, Instrumentation inst) {
        premain(agentArgs, inst);
    }

    public static void premain(String agentArgs, Instrumentation inst) {
        AgentStarter agentStarter = new AgentStarter();

        agentStarter.start(agentArgs, inst);
    }

    public void start(String agentArgs, Instrumentation inst) {
        start0(agentArgs, inst, LogLevel.INFO);
    }

    private void start0(String agentArgs, Instrumentation inst, LogLevel logLevel) {
        try {
            initConfig(agentArgs);

            printBanner();

            logStart(agentArgs, logLevel);

            agentStart(agentArgs, inst);
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }

    private static void initConfig(String root) throws Exception {
        String configFile = SystemUtil.getExecutePath(root) + File.separator + CoreConsts.AGENT_CONFIG;
        boolean isConfigExist = FileUtil.isExist(configFile);
        if (!isConfigExist) {
            throw new ConfigurationException("Could not find" + CoreConsts.AGENT_CONFIG + "in agent path");
        }

        ConfigLoader.loadConfigMonitorChanges(configFile);
    }


    private static void printBanner() {
        System.out.println(CoreConsts.BANNER_CHARS);
    }

    private void agentStart(String agentArgs, Instrumentation inst) throws IOException {
        AgentClassLoader classLoader = new AgentClassLoader(AgentStarter.class.getClassLoader(), agentArgs, new String[]{CoreConsts.PLUGIN_DIR});
        List<AbstractPlugin> plugins = PluginLoader.loadPlugins(classLoader);
        AgentBuilder agentBuilder = initAgentBuilder();

        ModuleExporter.export(inst, agentBuilder);
        agentBuilder = new PluginRegister().register(agentArgs, agentBuilder, plugins);
        AgentBuilder.Listener listener = new DefaultAgentListener();
        agentBuilder.with(listener).installOn(inst);

        if (plugins.size() == 0) {
            LogUtil.error("agent load", "no plugin found");
        }
    }

    private static void logStart(String root, LogLevel logLevel) throws Exception {
        LogUtil.start(SystemUtil.getExecutePath(root) + File.separator + "logs", FILE_MAX_SIZE, logLevel);
    }


    private AgentBuilder initAgentBuilder() {
        final ByteBuddy byteBuddy = new ByteBuddy().with(TypeValidation.of(false));
        return new AgentBuilder.Default(byteBuddy).ignore(nameStartsWith(CoreConsts.AGENT_PAKAGE)
                .or(nameContains("net.bytebuddy.")).or(nameStartsWith("org.slf4j."))
                .or(nameStartsWith("org.groovy.")).or(nameContains("javassist."))
                .or(nameContains(".asm.")).or(nameContains(".reflectasm."))
                .or(nameStartsWith("sun.reflect")).or(ElementMatchers.isSynthetic()));
    }
}
