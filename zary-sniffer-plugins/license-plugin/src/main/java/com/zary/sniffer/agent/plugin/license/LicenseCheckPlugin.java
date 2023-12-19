package com.zary.sniffer.agent.plugin.license;

import com.zary.sniffer.agent.core.plugin.AbstractPlugin;
import com.zary.sniffer.agent.core.plugin.point.IConstructorPoint;
import com.zary.sniffer.agent.core.plugin.point.IInstanceMethodPoint;
import com.zary.sniffer.agent.core.plugin.point.IStaticMethodPoint;
import net.bytebuddy.description.method.MethodDescription;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.matcher.ElementMatcher;
import net.bytebuddy.matcher.ElementMatchers;

public class LicenseCheckPlugin extends AbstractPlugin {

    private static final String HANDLER = "com.zary.sniffer.agent.plugin.license.handler.LicenseCheckHandler";

    private static final String TYPE = "com.ambersec.cloud.modules.company.service.impl.LicenseServiceImpl";

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
                return ElementMatchers.named("checkLicense");
            }

            @Override
            public String getHandlerClassName() {
                return HANDLER;
            }

            //是否有参数调用
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
