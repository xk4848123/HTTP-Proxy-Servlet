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
 * 拦截插件：OncePerRequestFilter插件
 * OncePerRequestFilter.doFilter(ServletRequest request,ServletResponse response)
 * 1.该过滤器是基础过滤器，默认实现防止filter重复执行功能，大多数Filter派生自该类
 * 2.主要用于通过wrapper修改request、response对象，获取、干预响应内容，修改请求、响应头、Cookie等
 *
 * @deprecated 通过插桩过滤器，篡改request为wrapper，实际项目测试容易出现问题，使用 ViewRenderPlugin 代替 - 20201211
 */
@Deprecated
public class OncePerRequestPlugin extends AbstractPlugin {
    private static final String TYPE = "org.springframework.web.filter.OncePerRequestFilter";
    private static final String METHOD = "doFilter";
    private static final String HANDLER = "com.zary.sniffer.agent.plugin.springmvc.handler.OncePerRequestHandler";

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
