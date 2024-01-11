package com.zary.sniffer.agent.plugin.servlet.route;


import com.zary.sniffer.config.Config;
import com.zary.sniffer.config.ConfigCache;

/**
 * 路由选择器
 */
public class RouteSelector {

    public Config.Route choose(String uri) {

        for (Config.Route route : ConfigCache.getConfig().getRoutes()) {
            String path = route.getPath();

            if (!path.contains("*")) {
                if (uri.equals(path)) {
                    return route;
                }
            } else {
                if (path.replaceAll("/\\*$", "").equals(uri)) {
                    return route;
                }
                // 通配符匹配
                if (isMatch(uri, route.getPath())) {
                    return route;
                }
            }
        }
        return null;
    }

    private boolean isMatch(String uri, String pattern) {
        String regex = pattern.replace("*", ".*");
        return uri.matches(regex);
    }

}
