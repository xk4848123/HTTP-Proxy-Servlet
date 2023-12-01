package com.zary.sniffer.agent.plugin.springmvc;

import com.zary.sniffer.agent.core.plugin.AbstractPlugin;
import com.zary.sniffer.agent.core.plugin.point.IConstructorPoint;
import com.zary.sniffer.agent.core.plugin.point.IInstanceMethodPoint;
import com.zary.sniffer.agent.core.plugin.point.IStaticMethodPoint;
import net.bytebuddy.description.method.MethodDescription;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.matcher.ElementMatcher;
import net.bytebuddy.matcher.ElementMatchers;

import static net.bytebuddy.matcher.ElementMatchers.isAnnotatedWith;
import static net.bytebuddy.matcher.ElementMatchers.named;
import static net.bytebuddy.matcher.ElementMatchers.not;

/**
 * 拦截插件：Controller插件
 * 1.SpringWeb用户自定义的Controller层控制器通过Mapping注解映射到url
 * 2.通过拦截Mapping注解的函数，关联url与应用程序命名空间之间关系
 *
 * @deprecated Controller父类定义的mapping子类插桩出现BUG，报错 Name for argument type [java.lang.String] not available - 20201102
 */
@Deprecated
public class ControllerPlugin extends AbstractPlugin {
    private static final String TYPE_CONTROLLER = "org.springframework.stereotype.Controller";
    private static final String TYPE_REST_CONTROLLER = "org.springframework.web.bind.annotation.RestController";
    private static final String HANDLER = "com.zary.sniffer.agent.plugin.springmvc.handler.ControllerHandler";

    @Override
    public ElementMatcher<TypeDescription> getPluginTypeMatcher() {
        return isAnnotatedWith(ElementMatchers.<TypeDescription>named(TYPE_CONTROLLER))
                .or(ElementMatchers.<TypeDescription>isAnnotatedWith(ElementMatchers.<TypeDescription>named(TYPE_REST_CONTROLLER)))
                .and(not(ElementMatchers.<TypeDescription>nameStartsWithIgnoreCase("org.springframework.")));
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
                ElementMatcher.Junction<MethodDescription> methodDescription = isAnnotatedWith(
                        named("org.springframework.web.bind.annotation.RequestMapping")
                                .or(named("org.springframework.web.bind.annotation.GetMapping"))
                                .or(named("org.springframework.web.bind.annotation.PostMapping"))
                                .or(named("org.springframework.web.bind.annotation.PutMapping"))
                                .or(named("org.springframework.web.bind.annotation.DeleteMapping"))
                                .or(named("org.springframework.web.bind.annotation.PatchMapping"))
                );
                methodDescription = methodDescription.and(not(ElementMatchers.<MethodDescription>isStatic()));
                return methodDescription;
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
