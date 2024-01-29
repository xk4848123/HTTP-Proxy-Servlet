package com.zary.sniffer.agent.core.listener;

import com.zary.sniffer.agent.core.log.LogUtil;
import com.zary.sniffer.util.ExceptionUtil;
import net.bytebuddy.agent.builder.AgentBuilder;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.dynamic.DynamicType;
import net.bytebuddy.utility.JavaModule;

public class DefaultAgentListener implements AgentBuilder.Listener {

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
}
