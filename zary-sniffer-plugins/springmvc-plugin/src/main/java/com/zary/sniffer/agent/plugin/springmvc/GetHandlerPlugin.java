package com.zary.sniffer.agent.plugin.springmvc;


import com.zary.sniffer.agent.core.plugin.AbstractPlugin;
import com.zary.sniffer.agent.core.plugin.point.IConstructorPoint;
import com.zary.sniffer.agent.core.plugin.point.IInstanceMethodPoint;
import com.zary.sniffer.agent.core.plugin.point.IStaticMethodPoint;
import net.bytebuddy.description.method.MethodDescription;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.matcher.ElementMatcher;
import net.bytebuddy.matcher.ElementMatchers;

/**
 * 拦截插件：GetHandler插件
 * DispatcherServlet.getHandler(HttpServletRequest request)
 * <p>
 * 1.getHandler函数在doDispatch里调用，主要找到某个请求的控制器
 * 2.主要用于搜集应用程序的命名空间appid，替代导致BUG的ControllerPlugin
 */
public class GetHandlerPlugin extends AbstractPlugin {
    private static final String TYPE = "org.springframework.web.servlet.DispatcherServlet";
    private static final String METHOD = "getHandler";
    private static final String HANDLER = "com.zary.sniffer.agent.plugin.springmvc.handler.GetHandlerHandler";

    @Override
    public ElementMatcher<TypeDescription> getPluginTypeMatcher() {
        return ElementMatchers.named(TYPE);
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
                return ElementMatchers.named(METHOD);
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
