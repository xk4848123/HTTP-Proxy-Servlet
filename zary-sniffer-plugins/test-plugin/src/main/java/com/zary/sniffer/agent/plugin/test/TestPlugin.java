package com.zary.sniffer.agent.plugin.test;

import com.zary.sniffer.agent.core.plugin.AbstractPlugin;
import com.zary.sniffer.agent.core.plugin.point.IConstructorPoint;
import com.zary.sniffer.agent.core.plugin.point.IInstanceMethodPoint;
import com.zary.sniffer.agent.core.plugin.point.IStaticMethodPoint;
import net.bytebuddy.description.method.MethodDescription;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.matcher.ElementMatcher;
import net.bytebuddy.matcher.ElementMatchers;

import static net.bytebuddy.matcher.ElementMatchers.named;

public class TestPlugin extends AbstractPlugin {

    private static final String TYPE = "com.telit.upms.controller.pub.UpmsVersionController";
    private static final String HANDLER = "com.zary.sniffer.agent.plugin.test.handler.TestHandler";
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
                return named("getVersion");
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
