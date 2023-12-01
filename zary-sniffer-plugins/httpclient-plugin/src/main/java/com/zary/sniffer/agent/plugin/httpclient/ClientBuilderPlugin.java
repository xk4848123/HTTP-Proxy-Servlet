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
 * 拦截插件：HttpClientBuilderPlugin
 * org.apache.http.impl.client.HttpClientBuilder.build()
 * 1.HttpClientBuilder是一个构造类，用于构造CloseableHttpClient，是所有请求发送的客户端对象
 * 2.通过此插件实现在发送请求时设置一个默认的head，进而实现进行head植入
 * 3.支持版本>=4.3，低于该版本HttpClientBuilder不存在
 */
public class ClientBuilderPlugin extends AbstractPlugin {
    private static final String TYPE = "org.apache.http.impl.client.HttpClientBuilder";
    private static final String HANDLER = "com.zary.sniffer.agent.plugin.httpclient.handler.ClientBuilderHandler";

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
