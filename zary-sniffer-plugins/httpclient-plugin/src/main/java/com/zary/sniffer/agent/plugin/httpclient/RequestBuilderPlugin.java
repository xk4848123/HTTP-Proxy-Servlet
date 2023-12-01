package com.zary.sniffer.agent.plugin.httpclient;

import com.zary.sniffer.agent.core.plugin.AbstractPlugin;
import com.zary.sniffer.agent.core.plugin.point.IConstructorPoint;
import com.zary.sniffer.agent.core.plugin.point.IInstanceMethodPoint;
import com.zary.sniffer.agent.core.plugin.point.IStaticMethodPoint;
import net.bytebuddy.description.method.MethodDescription;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.matcher.ElementMatcher;
import net.bytebuddy.matcher.ElementMatchers;

/**
 * 拦截插件：RequestBuilderPlugin
 * org.apache.http.client.methods.RequestBuilder.build()
 * 1.RequestBuilder是一个构造类，用于构造HttpUriRequest对象，HttpUriRequest是HttpGet、HttpPost等请求对象的基类
 * 2.通过此插件实现对该方式发送的请求进行head植入
 * 3.支持版本>=4.3，低于该版本RequestBuilder不存在
 */
public class RequestBuilderPlugin extends AbstractPlugin {
    private static final String TYPE = "org.apache.http.client.methods.RequestBuilder";
    private static final String HANDLER = "com.zary.sniffer.agent.plugin.httpclient.handler.RequestBuilderHandler";

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
                return ElementMatchers.named("build");
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
