package com.zary.sniffer.agent.plugin.servlet.processor;

import com.zary.sniffer.config.Config;


import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class StatusProcessor {
    private static final StatusProcessor instance = new StatusProcessor();

    public static StatusProcessor getInstance() {
        return instance;
    }

    private StatusProcessor() {

    }

    public void service(HttpServletRequest servletRequest, HttpServletResponse servletResponse, Config.Route route, Config.RouteType routeType) throws Exception {

        if (routeType == Config.RouteType.REDIRECT_302) {
            servletResponse.sendRedirect(route.getTarget());
        } else if (routeType == Config.RouteType.REDIRECT_301) {
            servletResponse.setStatus(HttpServletResponse.SC_MOVED_PERMANENTLY);
            servletResponse.setHeader("Location", route.getTarget());
        } else if (routeType == Config.RouteType.NOT_FOUND_404) {
            servletResponse.setStatus(HttpServletResponse.SC_NOT_FOUND);
            servletResponse.getWriter().println("<h1>404 - Page not found</h1>");
        }else if (routeType == Config.RouteType.UNAUTHORIZED_403) {
            servletResponse.setStatus(HttpServletResponse.SC_FORBIDDEN);
            servletResponse.getWriter().println("<h1>403 - Access denied</h1>");
        }else if (routeType == Config.RouteType.SERVER_ERROR_500) {
            servletResponse.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            servletResponse.getWriter().println("<h1>500 - Internal server</h1>");
        }
    }

}
