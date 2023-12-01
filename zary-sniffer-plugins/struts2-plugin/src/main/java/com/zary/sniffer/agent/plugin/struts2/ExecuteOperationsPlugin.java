package com.zary.sniffer.agent.plugin.struts2;

import com.zary.sniffer.agent.core.plugin.AbstractPlugin;
import com.zary.sniffer.agent.core.plugin.point.IConstructorPoint;
import com.zary.sniffer.agent.core.plugin.point.IInstanceMethodPoint;
import com.zary.sniffer.agent.core.plugin.point.IStaticMethodPoint;
import net.bytebuddy.description.method.MethodDescription;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.matcher.ElementMatcher;
import net.bytebuddy.matcher.ElementMatchers;

/**
 * 拦截Struct2响应用于写脚本和响应信息
 */
public class ExecuteOperationsPlugin extends AbstractPlugin {
    /**
     * 2.5.22---2.5-BETA1中命名空间不包含ng
     * 2.3.37---2.1.6中包含ng
     */
    private static final String TYPE = "org.apache.struts2.dispatcher.(.*?)ExecuteOperations";
    private static final String METHOD = "executeAction";
    private static final String HANDLER = "com.zary.sniffer.agent.plugin.struts2.handler.ExecuteOperationsHandler";

    @Override
    public ElementMatcher<TypeDescription> getPluginTypeMatcher() {
        return ElementMatchers.nameMatches(TYPE);
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
