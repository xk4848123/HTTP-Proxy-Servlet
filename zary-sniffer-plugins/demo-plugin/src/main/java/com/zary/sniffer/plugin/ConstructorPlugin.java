package com.zary.sniffer.plugin;


import com.zary.sniffer.agent.core.plugin.AbstractPlugin;
import com.zary.sniffer.agent.core.plugin.point.IConstructorPoint;
import com.zary.sniffer.agent.core.plugin.point.IInstanceMethodPoint;
import com.zary.sniffer.agent.core.plugin.point.IStaticMethodPoint;
import net.bytebuddy.description.method.MethodDescription;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.matcher.ElementMatcher;
import net.bytebuddy.matcher.ElementMatchers;

public class ConstructorPlugin extends AbstractPlugin {
    private static final String TYPE = "com.telit.microgenerator.ScannerCollector";
    private static final String HANDLER = "com.zary.sniffer.plugin.handler.ConstructorHandler";

    @Override
    public ElementMatcher<TypeDescription> getPluginTypeMatcher() {
        return ElementMatchers.named(TYPE);
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
