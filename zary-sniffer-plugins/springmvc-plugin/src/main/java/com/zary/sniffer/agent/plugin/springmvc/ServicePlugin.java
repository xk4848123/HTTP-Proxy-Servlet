package com.zary.sniffer.agent.plugin.springmvc;


import com.zary.sniffer.agent.core.plugin.AbstractPlugin;
import com.zary.sniffer.agent.core.plugin.point.IConstructorPoint;
import com.zary.sniffer.agent.core.plugin.point.IInstanceMethodPoint;
import com.zary.sniffer.agent.core.plugin.point.IStaticMethodPoint;
import net.bytebuddy.description.method.MethodDescription;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.matcher.ElementMatcher;
import net.bytebuddy.matcher.ElementMatchers;

import static net.bytebuddy.matcher.ElementMatchers.*;

/**
 * 拦截插件：Service插件
 *
 * @deprecated 通过拦截@Service注解的类来识别应用程序命名空间，实际测试函数过多且无法有效过滤，易导致递归调用java.lang.StackOverflowError，弃用 - 20201031
 */
@Deprecated
public class ServicePlugin extends AbstractPlugin {
    private static final String TYPE_SERVICE = "org.springframework.stereotype.Service";
    private static final String HANDLER = "com.zary.sniffer.agent.plugin.springmvc.handler.ServiceHandler";

    @Override
    public ElementMatcher<TypeDescription> getPluginTypeMatcher() {
        return isAnnotatedWith(ElementMatchers.<TypeDescription>named(TYPE_SERVICE))
                .and(not(ElementMatchers.<TypeDescription>nameStartsWithIgnoreCase("org.springframework.")));
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
                return not(ElementMatchers.<MethodDescription>isStatic())
                        .and(ElementMatchers.<MethodDescription>isPublic());

            }

            @Override
            public String getHandlerClassName() {
                return HANDLER;
            }

            @Override
            public boolean isMorphArgs() {
                return false;
            }
        };
        return new IInstanceMethodPoint[]{point};
    }

    @Override
    public IStaticMethodPoint[] getStaticMethodPoints() {
        return new IStaticMethodPoint[0];
    }
}
