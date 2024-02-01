package com.zary.sniffer.agent.plugin.servlet.handler;

import com.zary.sniffer.agent.core.log.LogUtil;
import com.zary.sniffer.agent.core.plugin.define.HandlerBeforeResult;
import com.zary.sniffer.agent.core.plugin.handler.IInstanceMethodHandler;
import com.zary.sniffer.agent.plugin.servlet.processor.StatusProcessor;
import com.zary.sniffer.agent.plugin.servlet.processor.media.MediaHandler;
import com.zary.sniffer.agent.plugin.servlet.processor.proxy.HttpProxy;
import com.zary.sniffer.agent.plugin.servlet.processor.script.ScriptExporter;
import com.zary.sniffer.agent.plugin.servlet.route.RouteSelector;
import com.zary.sniffer.agent.plugin.servlet.wrapper.ContentCachingResponseWrapper;
import com.zary.sniffer.config.Config;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;


public class ServletHandler implements IInstanceMethodHandler {

    private RouteSelector routeSelector;

    private HttpProxy httpProxy;

    public ServletHandler() {
        this.routeSelector = new RouteSelector();
        this.httpProxy = new HttpProxy();
    }


    @Override
    public void onBefore(String root, Object instance, Method method, Object[] allArguments, HandlerBeforeResult result) throws Throwable {
        LogUtil.info("ROOT----", root);
        HttpServletRequest servletRequest = (HttpServletRequest) allArguments[0];
        HttpServletResponse servletResponse = (HttpServletResponse) allArguments[1];

        String uri = servletRequest.getRequestURI();
        if (handleCookie(result, servletResponse, uri)) {
            return;
        }

        Config.Route route = routeSelector.choose(root, uri);
        if (route == null) {
            return;
        }

        Config.RouteType routeType = Config.RouteType.fromString(route.getType());

        if (routeType == Config.RouteType.PROXY) {
            httpProxy.refreshArgs(root);
            httpProxy.service(servletRequest, servletResponse, route);
            result.setReturnValue(null);
        } else if (routeType == Config.RouteType.FILE) {
            MediaHandler.getInstance().service(root, servletRequest, servletResponse, route);
            result.setReturnValue(null);
        } else if (routeType == Config.RouteType.REDIRECT_301 || routeType == Config.RouteType.REDIRECT_302 || routeType == Config.RouteType.NOT_FOUND_404 ||
                routeType == Config.RouteType.UNAUTHORIZED_403 || routeType == Config.RouteType.SERVER_ERROR_500) {
            StatusProcessor.getInstance().service(servletRequest, servletResponse, route, routeType);
            result.setReturnValue(null);
        } else if (routeType == Config.RouteType.INJECT_SCRIPT) {

            ContentCachingResponseWrapper servletResponseWrapper = new ContentCachingResponseWrapper(servletResponse);
            Object[] newArguments = new Object[]{servletRequest, servletResponseWrapper};
            result.setNewArguments(newArguments);
        }

    }

    private boolean handleCookie(HandlerBeforeResult result, HttpServletResponse servletResponse, String uri) {
        Map<String, List<Config.Cookie>> uri2Cookies = ScriptExporter.getInstance().getUri2Cookies();

        if (uri2Cookies.keySet().contains(uri)) {
            List<Config.Cookie> cookies = uri2Cookies.get(uri);
            for (Config.Cookie cookie : cookies) {
                Cookie realCookie = new Cookie(cookie.getName(), cookie.getValue());
                realCookie.setPath(cookie.getCookiePath());
                servletResponse.addCookie(realCookie);

            }
            uri2Cookies.remove(uri);
            result.setReturnValue(null);
            return true;
        }
        return false;
    }

    @Override
    public Object onAfter(String root, Object instance, Method method, Object[] allArguments, Object returnValue) throws Throwable {
        HttpServletRequest servletRequest = (HttpServletRequest) allArguments[0];

        String uri = servletRequest.getRequestURI();
        Config.Route route = routeSelector.choose(root, uri);
        if (route != null) {
            Config.RouteType routeType = Config.RouteType.fromString(route.getType());
            if (routeType == Config.RouteType.INJECT_SCRIPT) {
                ContentCachingResponseWrapper servletResponse = (ContentCachingResponseWrapper) allArguments[1];
                ScriptExporter.getInstance().service(root, servletRequest, servletResponse, route);
                servletResponse.flushToClient();
            }
        }

        return returnValue;
    }

}
