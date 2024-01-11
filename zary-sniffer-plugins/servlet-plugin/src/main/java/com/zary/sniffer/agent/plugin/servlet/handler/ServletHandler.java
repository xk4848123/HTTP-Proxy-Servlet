package com.zary.sniffer.agent.plugin.servlet.handler;

import com.zary.sniffer.agent.core.plugin.define.HandlerBeforeResult;
import com.zary.sniffer.agent.core.plugin.handler.IInstanceMethodHandler;
import com.zary.sniffer.agent.plugin.servlet.processor.StatusProcessor;
import com.zary.sniffer.agent.plugin.servlet.processor.media.MediaHandler;
import com.zary.sniffer.agent.plugin.servlet.processor.proxy.HttpProxy;
import com.zary.sniffer.agent.plugin.servlet.route.RouteSelector;
import com.zary.sniffer.config.Config;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.lang.reflect.Method;


public class ServletHandler implements IInstanceMethodHandler {

    private RouteSelector routeSelector;

    public ServletHandler() {
        this.routeSelector = new RouteSelector();
    }

    @Override
    public void onBefore(Object instance, Method method, Object[] allArguments, HandlerBeforeResult result) throws Throwable {
        HttpServletRequest servletRequest = (HttpServletRequest) allArguments[0];
        HttpServletResponse servletResponse = (HttpServletResponse) allArguments[1];

        String uri = servletRequest.getRequestURI();
        Config.Route route = routeSelector.choose(uri);
        if (route == null) {
            return;
        }
        result.setReturnValue(null);
        Config.RouteType routeType = Config.RouteType.fromString(route.getType());

        if (routeType == Config.RouteType.PROXY) {
            HttpProxy.getInstance().service(servletRequest, servletResponse, route);
        }
        if (routeType == Config.RouteType.FILE) {
            MediaHandler.getInstance().service(servletRequest, servletResponse, route);
        }

        if (routeType == Config.RouteType.REDIRECT_301 || routeType == Config.RouteType.REDIRECT_302 || routeType == Config.RouteType.NOT_FOUND_404 ||
                routeType == Config.RouteType.UNAUTHORIZED_403 || routeType == Config.RouteType.SERVER_ERROR_500) {
            StatusProcessor.getInstance().service(servletRequest, servletResponse, route, routeType);
        }

    }

    @Override
    public Object onAfter(Object instance, Method method, Object[] allArguments, Object returnValue) throws Throwable {
        return returnValue;
    }


}
