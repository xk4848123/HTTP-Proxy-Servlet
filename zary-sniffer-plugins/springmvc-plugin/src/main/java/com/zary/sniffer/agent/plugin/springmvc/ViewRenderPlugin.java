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
 * 拦截插件：ViewRender插件
 * org.springframework.web.servlet.View.render(Map<String, ?> model, HttpServletRequest request, HttpServletResponse response)
 * <p>
 * 1.通过拦截render函数实现对页面输出脚本
 * 2.不同视图模板对render的实现机制如下所示：
 * - Thymeleaf::    View >> AbstractThymeleafView >> ThymeleafView.render()
 * - FreeMarker::   View >> AbstractView.render() >> AbstractUrlBasedView >> AbstractTemplateView >> FreeMarkerView
 * - Velocity::     View >> AbstractView.render() >> AbstractUrlBasedView >> AbstractTemplateView >> VelocityView
 * - JstlView::     View >> AbstractView.render() >> AbstractUrlBasedView >> InternalResourceView >> JstlView (注：有问题，代码可以调用但脚本无法输出，可能与jstl基于foward实现有关)
 */
public class ViewRenderPlugin extends AbstractPlugin {
    private static final String TYPE_THYMELEAF = "org.thymeleaf.spring(.*?).view.ThymeleafView";
    private static final String TYPE_OTHER = "org.springframework.web.servlet.view.AbstractView";
    private static final String METHOD = "render";
    private static final String HANDLER = "com.zary.sniffer.agent.plugin.springmvc.handler.ViewRenderHandler";

    @Override
    public ElementMatcher<TypeDescription> getPluginTypeMatcher() {
        return ElementMatchers.named(TYPE_OTHER).or(ElementMatchers.<TypeDescription>nameMatches(TYPE_THYMELEAF));
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
                return ElementMatchers.named(METHOD)
                        .and(ElementMatchers.takesArguments(3))
                        .and(ElementMatchers.takesArgument(1, ElementMatchers.<TypeDescription>named("javax.servlet.http.HttpServletRequest")))
                        .and(ElementMatchers.takesArgument(2, ElementMatchers.<TypeDescription>named("javax.servlet.http.HttpServletResponse")))
                        .and(ElementMatchers.isMethod());
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
