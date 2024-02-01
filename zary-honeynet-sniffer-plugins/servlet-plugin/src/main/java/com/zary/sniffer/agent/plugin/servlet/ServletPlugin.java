package com.zary.sniffer.agent.plugin.servlet;


import com.zary.sniffer.agent.core.plugin.AbstractPlugin;
import com.zary.sniffer.agent.core.plugin.point.IConstructorPoint;
import com.zary.sniffer.agent.core.plugin.point.IInstanceMethodPoint;
import com.zary.sniffer.agent.core.plugin.point.IStaticMethodPoint;
import net.bytebuddy.description.method.MethodDescription;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.matcher.ElementMatcher;
import net.bytebuddy.matcher.ElementMatchers;

/**
 * 拦截插件：Servlet插件
 * 1.拦截基础Servlet应用程序请求，继承HttpServlet的类里面的doGet、doPost、service函数
 * 2.排除SpringWeb的DispatcherServlet
 */
public class ServletPlugin extends AbstractPlugin {
    private static final String HANDLER = "com.zary.sniffer.agent.plugin.spring.handler.ServletHandler";

    @Override
    public ElementMatcher<TypeDescription> getPluginTypeMatcher() {

        return ElementMatchers.hasSuperClass(ElementMatchers.<TypeDescription>named("javax.servlet.http.HttpServlet"))
                .and(ElementMatchers.not(ElementMatchers.isInterface()))
                .and(ElementMatchers.not(ElementMatchers.isAbstract()));
    }

    @Override
    public IConstructorPoint[] getConstructorPoints() {
        return new IConstructorPoint[0];
    }

    @Override
    public IInstanceMethodPoint[] getInstanceMethodPoints() {
        IInstanceMethodPoint point = new IInstanceMethodPoint() {
            @Override
            public ElementMatcher<MethodDescription> getMethodsMatcher() {
                return ElementMatchers.isMethod()
                        .and(ElementMatchers.takesArguments(2))
                        .and(ElementMatchers.takesArgument(0, ElementMatchers.named("javax.servlet.http.HttpServletRequest")))
                        .and(ElementMatchers.takesArgument(1, ElementMatchers.named("javax.servlet.http.HttpServletResponse")))
                        .and(
                                ElementMatchers.<MethodDescription>named("service")
                        );
            }

            @Override
            public String getHandlerClassName() {
                return HANDLER;
            }

            @Override
            public boolean isMorphArgs() {
                return true;
            }
        };
        return new IInstanceMethodPoint[]{point};
    }

    @Override
    public IStaticMethodPoint[] getStaticMethodPoints() {
        return new IStaticMethodPoint[0];
    }
}
