package com.zary.sniffer.agent.plugin.struts2;

import com.zary.sniffer.agent.core.plugin.AbstractPlugin;
import com.zary.sniffer.agent.core.plugin.point.IConstructorPoint;
import com.zary.sniffer.agent.core.plugin.point.IInstanceMethodPoint;
import com.zary.sniffer.agent.core.plugin.point.IStaticMethodPoint;
import net.bytebuddy.description.method.MethodDescription;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.matcher.ElementMatcher;
import net.bytebuddy.matcher.ElementMatchers;

import static net.bytebuddy.matcher.ElementMatchers.nameMatches;
import static net.bytebuddy.matcher.ElementMatchers.not;

/**
 * 拦截Struct2的用户自定义的Action
 */
public class ActionSupportPlugin extends AbstractPlugin {
    private static final String TYPE = "com.opensymphony.xwork2.ActionSupport";
    private static final String HANDLER = "com.zary.sniffer.agent.plugin.struts2.handler.ActionSupportHandler";

    @Override
    public ElementMatcher<TypeDescription> getPluginTypeMatcher() {
        return ElementMatchers.hasSuperClass(ElementMatchers.<TypeDescription>named(TYPE))
                .and(not(ElementMatchers.<TypeDescription>named(TYPE)));

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
                try {
                    return ElementMatchers.returns(String.class)
                            .and(ElementMatchers.not(ElementMatchers.<MethodDescription>isAbstract()))
                            .and(ElementMatchers.not(ElementMatchers.<MethodDescription>isStatic()))
                            .and(ElementMatchers.not(ElementMatchers.isConstructor()))
                            .and(ElementMatchers.not(ElementMatchers.<MethodDescription>isDeclaredBy(ElementMatchers.<TypeDescription>named(TYPE))))
                            .and(ElementMatchers.not(ElementMatchers.<MethodDescription>isDeclaredBy(Object.class)));
                } catch (Exception e) {
                    e.printStackTrace();
                    return nameMatches("execute").and(not(ElementMatchers.<MethodDescription>isStatic()));
                }

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
