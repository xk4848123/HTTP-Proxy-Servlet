package com.zary.sniffer.config;

import java.util.List;

public class Config {

    /**
     * 路由列表
     */
    private List<Route> routes;

    /**
     * 标识客户端IP转发
     */
    private String forwardIp;

    /**
     * 是否保持HOST参数原样
     */
    private String preserveHost;

    /**
     * 是否保持COOKIES不变
     */
    private String preserveCookies;

    /**
     * 是否自动处理重定向
     */
    private String handleRedirects;

    /**
     * 设置套接字连接超时时间（毫秒）
     */
    private String socketTimeout;

    /**
     * 设置套接字读取超时时间（毫秒）
     */
    private String readTimeout;

    /**
     * 设置连接请求超时时间（毫秒）
     */
    private String connectionRequestTimeout;

    /**
     * 设置最大连接数
     */
    private String maxConnections;

    /**
     * 是否使用JVM定义的系统属性来配置。
     */
    private String useSystemProperties;

    /**
     * 是否在servlet中处理压缩
     */
    private String handleCompression;

    /**
     * 去掉匹配的父路径，如:/index/a匹配到/index/*，去掉/index,实际uri成/a
     */
    private String stripPrefix;


    public String getForwardIp() {
        return forwardIp;
    }

    public void setForwardIp(String forwardIp) {
        this.forwardIp = forwardIp;
    }

    public String getPreserveHost() {
        return preserveHost;
    }

    public void setPreserveHost(String preserveHost) {
        this.preserveHost = preserveHost;
    }

    public String getPreserveCookies() {
        return preserveCookies;
    }

    public void setPreserveCookies(String preserveCookies) {
        this.preserveCookies = preserveCookies;
    }

    public String getHandleRedirects() {
        return handleRedirects;
    }

    public void setHandleRedirects(String handleRedirects) {
        this.handleRedirects = handleRedirects;
    }

    public String getSocketTimeout() {
        return socketTimeout;
    }

    public void setSocketTimeout(String socketTimeout) {
        this.socketTimeout = socketTimeout;
    }

    public String getReadTimeout() {
        return readTimeout;
    }

    public void setReadTimeout(String readTimeout) {
        this.readTimeout = readTimeout;
    }

    public String getConnectionRequestTimeout() {
        return connectionRequestTimeout;
    }

    public void setConnectionRequestTimeout(String connectionRequestTimeout) {
        this.connectionRequestTimeout = connectionRequestTimeout;
    }

    public String getMaxConnections() {
        return maxConnections;
    }

    public void setMaxConnections(String maxConnections) {
        this.maxConnections = maxConnections;
    }

    public String getUseSystemProperties() {
        return useSystemProperties;
    }

    public void setUseSystemProperties(String useSystemProperties) {
        this.useSystemProperties = useSystemProperties;
    }

    public String getHandleCompression() {
        return handleCompression;
    }

    public void setHandleCompression(String handleCompression) {
        this.handleCompression = handleCompression;
    }

    public String getStripPrefix() {
        return stripPrefix;
    }

    public void setStripPrefix(String stripPrefix) {
        this.stripPrefix = stripPrefix;
    }

    public List<Route> getRoutes() {
        return routes;
    }

    public void setRoutes(List<Route> routes) {
        this.routes = routes;
    }

    public static class Cookie {

        private String name;

        private String value;

        private String cookiePath;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getValue() {
            return value;
        }

        public void setValue(String value) {
            this.value = value;
        }

        public String getCookiePath() {
            return cookiePath;
        }

        public void setCookiePath(String cookiePath) {
            this.cookiePath = cookiePath;
        }
    }

    public static class Route {
        private String path;

        private String type;

        private String target;

        private Boolean stripPrefix;

        private List<Cookie> cookies;

        public String getPath() {
            return path;
        }

        public void setPath(String path) {
            this.path = path;
        }


        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

        public String getTarget() {
            return target;
        }

        public void setTarget(String target) {
            this.target = target;
        }

        public Boolean getStripPrefix() {
            return stripPrefix;
        }

        public void setStripPrefix(Boolean stripPrefix) {
            this.stripPrefix = stripPrefix;
        }

        public List<Cookie> getCookies() {
            return cookies;
        }

        public void setCookies(List<Cookie> cookies) {
            this.cookies = cookies;
        }
    }

    public enum RouteType {
        PROXY("proxy"),

        FILE("file"),

        REDIRECT_301("301"),

        REDIRECT_302("302"),

        NOT_FOUND_404("404"),

        UNAUTHORIZED_403("403"),
        SERVER_ERROR_500("500"),

        INJECT_SCRIPT("script"),
        ;
        private final String type;

        RouteType(String type) {
            this.type = type;
        }

        //提供一个将字符串转换为RouteType的静态方法
        public static RouteType fromString(String type) {
            for (RouteType value : RouteType.values()) {
                if (value.type.equalsIgnoreCase(type)) {
                    return value;
                }
            }
            throw new IllegalArgumentException("Invalid route type: " + type);
        }
    }

}
