package com.zary.sniffer.config;

import java.util.Map;

public class Config {

    private Map<String, String> pattern2TargetUrlList;

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

    public Map<String, String> getPattern2TargetUrlList() {
        return pattern2TargetUrlList;
    }

    public void setPattern2TargetUrlList(Map<String, String> pattern2TargetUrlList) {
        this.pattern2TargetUrlList = pattern2TargetUrlList;
    }

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
}
