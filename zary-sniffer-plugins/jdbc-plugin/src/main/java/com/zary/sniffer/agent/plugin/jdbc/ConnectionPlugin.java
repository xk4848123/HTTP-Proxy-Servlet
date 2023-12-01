package com.zary.sniffer.agent.plugin.jdbc;


import com.zary.sniffer.agent.core.plugin.AbstractPlugin;
import com.zary.sniffer.agent.core.plugin.point.IConstructorPoint;
import com.zary.sniffer.agent.core.plugin.point.IInstanceMethodPoint;
import com.zary.sniffer.agent.core.plugin.point.IStaticMethodPoint;
import net.bytebuddy.description.method.MethodDescription;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.matcher.ElementMatcher;
import net.bytebuddy.matcher.ElementMatchers;

/**
 * 拦截插件：Connection插件
 * <p>
 * 1.计划用于拦截数据源信息，但通过statement.getConnection可以获取到，所以暂时不用
 */
public class ConnectionPlugin extends AbstractPlugin {
    private static final String HANDLER = "com.zary.sniffer.agent.plugin.jdbc.handler.ConnectionHandler";

    @Override
    public ElementMatcher<TypeDescription> getPluginTypeMatcher() {
        return null;
    }

    @Override
    public IConstructorPoint[] getConstructorPoints() {
        IConstructorPoint point = new IConstructorPoint() {
            @Override
            public ElementMatcher<MethodDescription> getConstructorMatcher() {
                return ElementMatchers.isConstructor();
            }

            @Override
            public String getHandlerClassName() {
                return HANDLER;
            }
        };
        return new IConstructorPoint[]{point};
    }

    @Override
    public IInstanceMethodPoint[] getInstanceMethodPoints() {
        return new IInstanceMethodPoint[0];
    }

    @Override
    public IStaticMethodPoint[] getStaticMethodPoints() {
        return new IStaticMethodPoint[0];
    }
}
