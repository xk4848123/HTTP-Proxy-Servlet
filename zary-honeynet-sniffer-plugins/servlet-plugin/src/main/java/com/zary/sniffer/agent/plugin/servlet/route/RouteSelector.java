package com.zary.sniffer.agent.plugin.servlet.route;


import com.zary.sniffer.agent.plugin.servlet.util.UriEncoder;
import com.zary.sniffer.config.Config;
import com.zary.sniffer.config.ConfigCache;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 路由选择器
 */
public class RouteSelector {

    public Config.Route choose(String root, String uri) {

        for (Config.Route route : ConfigCache.getConfig(root).getRoutes()) {
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


    public String generateJointUri(String uri, Config.Route route) {
        String path = route.getPath();
        if (!path.contains("*") && uri.equals(path)) {
            return UriEncoder.encodeUriQuery(uri, true).toString();
        }
        Boolean stripPrefix = route.getStripPrefix();
        if (path.replaceAll("/\\*$", "").equals(uri)) {
            if (stripPrefix) {
                return "";
            }
            return UriEncoder.encodeUriQuery(uri, true).toString();
        }

        String regex = path.replace("*", "(.*)");
        Pattern p = Pattern.compile(regex);
        Matcher m = p.matcher(uri);
        if (m.matches() && m.groupCount() > 0) {
            String partUri;
            if (stripPrefix) {
                partUri = "/" + m.group(1);
            } else {
                partUri = uri;
            }

            return UriEncoder.encodeUriQuery(partUri, true).toString();
        }
        return null;
    }


    private boolean isMatch(String uri, String pattern) {
        String regex = pattern.replace("*", ".*");
        return uri.matches(regex);
    }


}
