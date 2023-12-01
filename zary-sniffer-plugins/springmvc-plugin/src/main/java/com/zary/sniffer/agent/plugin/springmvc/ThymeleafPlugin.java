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
 * 拦截插件：Thymeleaf插件
 * 1.Thymeleaf是springboot推荐的模板引擎
 * 2.通过拦截parseAndProcess函数，实现在请求结束时追加页面脚本
 *
 * @deprecated 通过拦截thymeleaf的parseAndProcess实现脚本输出，通用性差，改为 ViewRenderPlugin - 20201102
 */
@Deprecated
public class ThymeleafPlugin extends AbstractPlugin {
    private static final String TYPE = "org.thymeleaf.engine.TemplateManager";
    private static final String METHOD = "parseAndProcess";
    private static final String HANDLER = "com.zary.sniffer.agent.plugin.springmvc.handler.ThymeleafHandler";

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
