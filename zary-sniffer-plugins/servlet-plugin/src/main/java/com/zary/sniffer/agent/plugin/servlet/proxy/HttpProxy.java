package com.zary.sniffer.agent.plugin.servlet.proxy;

import com.zary.sniffer.agent.core.log.LogProducer;
import com.zary.sniffer.agent.core.log.LogUtil;
import com.zary.sniffer.config.Config;
import com.zary.sniffer.config.ConfigCache;
import org.apache.http.*;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.CookieSpecs;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.AbortableHttpRequest;
import org.apache.http.client.utils.URIUtils;
import org.apache.http.config.SocketConfig;
import org.apache.http.entity.InputStreamEntity;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.message.BasicHeader;
import org.apache.http.message.BasicHttpEntityEnclosingRequest;
import org.apache.http.message.BasicHttpRequest;
import org.apache.http.message.HeaderGroup;
import org.apache.http.util.EntityUtils;

import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpCookie;
import java.net.URI;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Http反向代理类
 */
public class HttpProxy implements Closeable {

    /**
     * 日志收集
     */
    LogProducer logProducer = LogUtil.getLogProducer();
    private static HttpProxy instance = new HttpProxy();

    public static HttpProxy getInstance() {
        return instance;
    }

    private HttpProxy() {
        try {
            init();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    protected static final String ATTR_TARGET_URI = HttpProxy.class.getSimpleName() + ".targetUri";

    protected static final String ATTR_TARGET_URI_BASE = HttpProxy.class.getSimpleName() + ".targetUriBase";
    protected static final String ATTR_TARGET_HOST = HttpProxy.class.getSimpleName() + ".targetHost";

    protected boolean doForwardIP = true;
    protected boolean doSendUrlFragment = true;
    protected boolean doPreserveHost = false;
    protected boolean doPreserveCookies = false;
    protected boolean doHandleRedirects = false;
    protected boolean useSystemProperties = true;
    protected boolean doHandleCompression = false;
    protected int connectTimeout = -1;
    protected int readTimeout = -1;
    protected int connectionRequestTimeout = -1;
    protected int maxConnections = -1;

    private Map<String, Target> targetMap;

    private HttpClient proxyClient;


    protected String getTargetUri(HttpServletRequest servletRequest) {
        return (String) servletRequest.getAttribute(ATTR_TARGET_URI);
    }

    protected String getTargetUriBase(HttpServletRequest servletRequest) {
        return (String) servletRequest.getAttribute(ATTR_TARGET_URI_BASE);
    }

    protected HttpHost getTargetHost(HttpServletRequest servletRequest) {
        return (HttpHost) servletRequest.getAttribute(ATTR_TARGET_HOST);
    }

    private void init() throws ServletException {

        String doForwardIPString = ConfigCache.getConfig().getForwardIp();
        if (doForwardIPString != null) {
            this.doForwardIP = Boolean.parseBoolean(doForwardIPString);
        }

        String preserveHostString = ConfigCache.getConfig().getPreserveHost();
        if (preserveHostString != null) {
            this.doPreserveHost = Boolean.parseBoolean(preserveHostString);
        }

        String preserveCookiesString = ConfigCache.getConfig().getPreserveCookies();
        if (preserveCookiesString != null) {
            this.doPreserveCookies = Boolean.parseBoolean(preserveCookiesString);
        }

        String handleRedirectsString = ConfigCache.getConfig().getHandleRedirects();
        if (handleRedirectsString != null) {
            this.doHandleRedirects = Boolean.parseBoolean(handleRedirectsString);
        }

        String connectTimeoutString = ConfigCache.getConfig().getSocketTimeout();
        if (connectTimeoutString != null) {
            this.connectTimeout = Integer.parseInt(connectTimeoutString);
        }

        String readTimeoutString = ConfigCache.getConfig().getReadTimeout();
        if (readTimeoutString != null) {
            this.readTimeout = Integer.parseInt(readTimeoutString);
        }

        String connectionRequestTimeout = ConfigCache.getConfig().getConnectionRequestTimeout();
        if (connectionRequestTimeout != null) {
            this.connectionRequestTimeout = Integer.parseInt(connectionRequestTimeout);
        }

        String maxConnections = ConfigCache.getConfig().getMaxConnections();
        if (maxConnections != null) {
            this.maxConnections = Integer.parseInt(maxConnections);
        }

        String useSystemPropertiesString = ConfigCache.getConfig().getUseSystemProperties();
        if (useSystemPropertiesString != null) {
            this.useSystemProperties = Boolean.parseBoolean(useSystemPropertiesString);
        }

        String doHandleCompression = ConfigCache.getConfig().getHandleCompression();
        if (doHandleCompression != null) {
            this.doHandleCompression = Boolean.parseBoolean(doHandleCompression);
        }


        initTarget();

        proxyClient = createHttpClient();
    }


    protected RequestConfig buildRequestConfig() {
        return RequestConfig.custom().setRedirectsEnabled(doHandleRedirects).setCookieSpec(CookieSpecs.IGNORE_COOKIES).setConnectTimeout(connectTimeout).setSocketTimeout(readTimeout).setConnectionRequestTimeout(connectionRequestTimeout).build();
    }

    protected SocketConfig buildSocketConfig() {
        if (readTimeout < 1) {
            return null;
        }
        return SocketConfig.custom().setSoTimeout(readTimeout).build();
    }

    protected void initTarget() throws ServletException {
        List<Config.Route> routes = ConfigCache.getConfig().getRoutes();

        targetMap = new LinkedHashMap<>();

        for (Config.Route route : routes) {
            String path = route.getPath();
            URI targetUriObj;
            try {
                targetUriObj = new URI(route.getTarget());
            } catch (Exception e) {
                throw new ServletException("Trying to process targetUrl init parameter: " + e, e);
            }
            HttpHost targetHost = URIUtils.extractHost(targetUriObj);

            targetMap.put(path, new Target(route.getTarget(), targetHost, route.getStripPrefix()));

        }
    }

    private static class Target {

        private String targetUrlBase;

        private HttpHost targetHost;

        private Boolean stripPrefix;


        public String getTargetUrlBase() {
            return targetUrlBase;
        }

        public HttpHost getTargetHost() {
            return targetHost;
        }

        public Boolean getStripPrefix() {
            return stripPrefix;
        }

        public Target(String targetUrlBase, HttpHost targetHost, Boolean stripPrefix) {
            this.targetUrlBase = targetUrlBase;
            this.targetHost = targetHost;
            this.stripPrefix = stripPrefix;
        }
    }


    protected HttpClient createHttpClient() {
        HttpClientBuilder clientBuilder = getHttpClientBuilder().setDefaultRequestConfig(buildRequestConfig()).setDefaultSocketConfig(buildSocketConfig());

        clientBuilder.setMaxConnTotal(maxConnections);
        clientBuilder.setMaxConnPerRoute(maxConnections);
        if (!doHandleCompression) {
            clientBuilder.disableContentCompression();
        }

        if (useSystemProperties) clientBuilder = clientBuilder.useSystemProperties();
        return buildHttpClient(clientBuilder);
    }


    protected HttpClient buildHttpClient(HttpClientBuilder clientBuilder) {
        return clientBuilder.build();
    }


    protected HttpClientBuilder getHttpClientBuilder() {
        return HttpClientBuilder.create();
    }


    RequestTarget extractUri(String uri) {
        for (String path : targetMap.keySet()) {
            if (!path.contains("*") && uri.equals(path)) {
                return new RequestTarget(targetMap.get(path).getTargetUrlBase(), targetMap.get(path).getTargetUrlBase() + uri, targetMap.get(path).getTargetHost());
            }
            Boolean stripPrefix = targetMap.get(path).getStripPrefix();
            if(path.replaceAll("/\\*$", "").equals(uri)){
                if (stripPrefix){
                    return new RequestTarget(targetMap.get(path).getTargetUrlBase(), targetMap.get(path).getTargetUrlBase() + "/", targetMap.get(path).getTargetHost());
                }
                return new RequestTarget(targetMap.get(path).getTargetUrlBase(), targetMap.get(path).getTargetUrlBase() + uri, targetMap.get(path).getTargetHost());
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

                if ("".equals(partUri)) {
                    return new RequestTarget(targetMap.get(path).getTargetUrlBase(), targetMap.get(path).getTargetUrlBase() + "/", targetMap.get(path).getTargetHost());
                }
                return new RequestTarget(targetMap.get(path).getTargetUrlBase(), targetMap.get(path).getTargetUrlBase() + encodeUriQuery(partUri, true), targetMap.get(path).getTargetHost());
            }
        }
        return null;
    }

    private static class RequestTarget {
        private String targetUrl;
        private HttpHost targetHost;
        private String targetUrlBase;

        public RequestTarget(String targetUrlBase, String targetUrl, HttpHost targetHost) {
            this.targetUrlBase = targetUrlBase;
            this.targetUrl = targetUrl;
            this.targetHost = targetHost;
        }

        public String getTargetUrl() {
            return targetUrl;
        }

        public HttpHost getTargetHost() {
            return targetHost;
        }

        public String getTargetUrlBase() {
            return targetUrlBase;
        }
    }

    public boolean service(HttpServletRequest servletRequest, HttpServletResponse servletResponse) {
        RequestTarget requestTarget;
        try {
            String uri = servletRequest.getRequestURI();
            requestTarget = extractUri(uri);
        } catch (Exception e) {
            logProducer.error("Parse url fail!", e.getMessage());
            return false;
        }


        if (requestTarget == null) {
            return false;
        }

        if (servletRequest.getAttribute(ATTR_TARGET_URI) == null) {
            servletRequest.setAttribute(ATTR_TARGET_URI, requestTarget.getTargetUrl());
        }
        if (servletRequest.getAttribute(ATTR_TARGET_URI_BASE) == null) {
            servletRequest.setAttribute(ATTR_TARGET_URI_BASE, requestTarget.getTargetUrlBase());
        }

        if (servletRequest.getAttribute(ATTR_TARGET_HOST) == null) {
            servletRequest.setAttribute(ATTR_TARGET_HOST, requestTarget.getTargetHost());
        }

        // 构造请求
        String method = servletRequest.getMethod();
        String proxyRequestUri = rewriteUrlFromRequest(servletRequest);


        HttpRequest proxyRequest;
        if (servletRequest.getHeader(HttpHeaders.CONTENT_LENGTH) != null || servletRequest.getHeader(HttpHeaders.TRANSFER_ENCODING) != null) {
            try {
                proxyRequest = newProxyRequestWithEntity(method, proxyRequestUri, servletRequest);
            } catch (IOException e) {
                logProducer.error("Error while creating the proxy request with entity", e.getMessage());
                return true;
            }

        } else {
            proxyRequest = new BasicHttpRequest(method, proxyRequestUri);
        }

        copyRequestHeaders(servletRequest, proxyRequest);

        setXForwardedForHeader(servletRequest, proxyRequest);

        HttpResponse proxyResponse = null;
        try {
            // 执行代理请求
            proxyResponse = doExecute(servletRequest, servletResponse, proxyRequest);

            // 处理响应:

            // 传递响应代码
            int statusCode = proxyResponse.getStatusLine().getStatusCode();
            servletResponse.setStatus(statusCode, proxyResponse.getStatusLine().getReasonPhrase());

            // 复制响应标头以确保SESSIONID或来自远程的其他Cookie
            copyResponseHeaders(proxyResponse, servletRequest, servletResponse);

            if (statusCode == HttpServletResponse.SC_NOT_MODIFIED) {
                servletResponse.setIntHeader(HttpHeaders.CONTENT_LENGTH, 0);
            } else {
                copyResponseEntity(proxyResponse, servletResponse, proxyRequest, servletRequest);
            }

        } catch (Exception e) {
            try {
                handleRequestException(proxyRequest, proxyResponse, e);
            } catch (Exception e2) {
                logProducer.error("Handel Request Exception!", e2.getMessage());
                return true;
            }

        } finally {
            // 确保整个实体都被消费掉了，这样连接就被释放了
            if (proxyResponse != null) EntityUtils.consumeQuietly(proxyResponse.getEntity());
        }
        return true;
    }

    /**
     * 处理代理请求中发生的异常
     */
    protected void handleRequestException(HttpRequest proxyRequest, HttpResponse proxyResonse, Exception e) throws ServletException, IOException {
        if (proxyRequest instanceof AbortableHttpRequest) {
            AbortableHttpRequest abortableHttpRequest = (AbortableHttpRequest) proxyRequest;
            abortableHttpRequest.abort();
        }
        if (proxyResonse instanceof Closeable) {
            ((Closeable) proxyResonse).close();
        }
        if (e instanceof RuntimeException) throw (RuntimeException) e;
        if (e instanceof ServletException) throw (ServletException) e;
        if (e instanceof IOException) throw (IOException) e;
        throw new RuntimeException(e);
    }

    protected HttpResponse doExecute(HttpServletRequest servletRequest, HttpServletResponse servletResponse, HttpRequest proxyRequest) throws IOException {
        return proxyClient.execute(getTargetHost(servletRequest), proxyRequest);
    }

    protected HttpRequest newProxyRequestWithEntity(String method, String proxyRequestUri, HttpServletRequest servletRequest) throws IOException {
        HttpEntityEnclosingRequest eProxyRequest = new BasicHttpEntityEnclosingRequest(method, proxyRequestUri);
        // 添加输入实体（流式）
        eProxyRequest.setEntity(new InputStreamEntity(servletRequest.getInputStream(), getContentLength(servletRequest)));
        return eProxyRequest;
    }

    private long getContentLength(HttpServletRequest request) {
        String contentLengthHeader = request.getHeader("Content-Length");
        if (contentLengthHeader != null) {
            return Long.parseLong(contentLengthHeader);
        }
        return -1L;
    }

    protected static final HeaderGroup hopByHopHeaders;

    static {
        hopByHopHeaders = new HeaderGroup();
        String[] headers = new String[]{"Connection", "Keep-Alive", "Proxy-Authenticate", "Proxy-Authorization", "TE", "Trailers", "Transfer-Encoding", "Upgrade"};
        for (String header : headers) {
            hopByHopHeaders.addHeader(new BasicHeader(header, null));
        }
    }

    /**
     * 将请求标头从servlet客户端复制到代理请求。
     */
    protected void copyRequestHeaders(HttpServletRequest servletRequest, HttpRequest proxyRequest) {
        @SuppressWarnings("unchecked") Enumeration<String> enumerationOfHeaderNames = servletRequest.getHeaderNames();
        while (enumerationOfHeaderNames.hasMoreElements()) {
            String headerName = enumerationOfHeaderNames.nextElement();
            copyRequestHeader(servletRequest, proxyRequest, headerName);
        }
    }

    protected void copyRequestHeader(HttpServletRequest servletRequest, HttpRequest proxyRequest, String headerName) {
        if (headerName.equalsIgnoreCase(HttpHeaders.CONTENT_LENGTH)) return;
        if (hopByHopHeaders.containsHeader(headerName)) return;

        if (doHandleCompression && headerName.equalsIgnoreCase(HttpHeaders.ACCEPT_ENCODING)) return;

        @SuppressWarnings("unchecked") Enumeration<String> headers = servletRequest.getHeaders(headerName);
        while (headers.hasMoreElements()) {//sometimes more than one value
            String headerValue = headers.nextElement();

            if (!doPreserveHost && headerName.equalsIgnoreCase(HttpHeaders.HOST)) {
                HttpHost host = getTargetHost(servletRequest);
                headerValue = host.getHostName();
                if (host.getPort() != -1) headerValue += ":" + host.getPort();
            } else if (!doPreserveCookies && headerName.equalsIgnoreCase(org.apache.http.cookie.SM.COOKIE)) {
                headerValue = getRealCookie(headerValue);
            }
            proxyRequest.addHeader(headerName, headerValue);
        }
    }

    private void setXForwardedForHeader(HttpServletRequest servletRequest, HttpRequest proxyRequest) {
        if (doForwardIP) {
            String forHeaderName = "X-Forwarded-For";
            String forHeader = servletRequest.getRemoteAddr();
            String existingForHeader = servletRequest.getHeader(forHeaderName);
            if (existingForHeader != null) {
                forHeader = existingForHeader + ", " + forHeader;
            }
            proxyRequest.setHeader(forHeaderName, forHeader);

            String protoHeaderName = "X-Forwarded-Proto";
            String protoHeader = servletRequest.getScheme();
            proxyRequest.setHeader(protoHeaderName, protoHeader);
        }
    }

    /**
     * 将代理的响应标头复制回servlet客户端。
     */
    protected void copyResponseHeaders(HttpResponse proxyResponse, HttpServletRequest servletRequest, HttpServletResponse servletResponse) {
        for (Header header : proxyResponse.getAllHeaders()) {
            copyResponseHeader(servletRequest, servletResponse, header);
        }
    }

    protected void copyResponseHeader(HttpServletRequest servletRequest, HttpServletResponse servletResponse, Header header) {
        String headerName = header.getName();
        if (hopByHopHeaders.containsHeader(headerName)) return;
        String headerValue = header.getValue();
        if (headerName.equalsIgnoreCase(org.apache.http.cookie.SM.SET_COOKIE) || headerName.equalsIgnoreCase(org.apache.http.cookie.SM.SET_COOKIE2)) {
            copyProxyCookie(servletRequest, servletResponse, headerValue);
        } else if (headerName.equalsIgnoreCase(HttpHeaders.LOCATION)) {
            // LOCATION Header may have to be rewritten.
            servletResponse.addHeader(headerName, rewriteUrlFromResponse(servletRequest, headerValue));
        } else {
            servletResponse.addHeader(headerName, headerValue);
        }
    }

    protected void copyProxyCookie(HttpServletRequest servletRequest, HttpServletResponse servletResponse, String headerValue) {
        for (HttpCookie cookie : HttpCookie.parse(headerValue)) {
            Cookie servletCookie = createProxyCookie(servletRequest, cookie);
            servletResponse.addCookie(servletCookie);
        }
    }

    /**
     * 从原始cookie中创建一个代理cookie。
     *
     * @param servletRequest original request
     * @param cookie         original cookie
     * @return proxy cookie
     */
    protected Cookie createProxyCookie(HttpServletRequest servletRequest, HttpCookie cookie) {
        String proxyCookieName = getProxyCookieName(cookie);
        Cookie servletCookie = new Cookie(proxyCookieName, cookie.getValue());
        servletCookie.setPath(buildProxyCookiePath(servletRequest)); //set to the path of the proxy servlet
        servletCookie.setComment(cookie.getComment());
        servletCookie.setMaxAge((int) cookie.getMaxAge());
        servletCookie.setSecure(cookie.getSecure());
        servletCookie.setVersion(cookie.getVersion());
        servletCookie.setHttpOnly(cookie.isHttpOnly());
        return servletCookie;
    }

    protected String getProxyCookieName(HttpCookie cookie) {
        //
        return doPreserveCookies ? cookie.getName() : getCookieNamePrefix(cookie.getName()) + cookie.getName();
    }

    /**
     * 为代理cookie创建路径。
     *
     * @param servletRequest original request
     * @return proxy cookie path
     */
    protected String buildProxyCookiePath(HttpServletRequest servletRequest) {
        String path = servletRequest.getContextPath();
        path += servletRequest.getServletPath();
        if (path.isEmpty()) {
            path = "/";
        }
        return path;
    }

    protected String getRealCookie(String cookieValue) {
        StringBuilder escapedCookie = new StringBuilder();
        String cookies[] = cookieValue.split("[;,]");
        for (String cookie : cookies) {
            String cookieSplit[] = cookie.split("=");
            if (cookieSplit.length == 2) {
                String cookieName = cookieSplit[0].trim();
                if (cookieName.startsWith(getCookieNamePrefix(cookieName))) {
                    cookieName = cookieName.substring(getCookieNamePrefix(cookieName).length());
                    if (escapedCookie.length() > 0) {
                        escapedCookie.append("; ");
                    }
                    escapedCookie.append(cookieName).append("=").append(cookieSplit[1].trim());
                }
            }
        }
        return escapedCookie.toString();
    }

    /**
     * 重写cookie的前缀。
     */
    protected String getCookieNamePrefix(String name) {
        return "!Proxy!" + "DispatcherServlet";
    }

    /**
     * 将响应主体数据（实体）从代理复制到servlet客户端。
     */
    protected void copyResponseEntity(HttpResponse proxyResponse, HttpServletResponse servletResponse, HttpRequest proxyRequest, HttpServletRequest servletRequest) throws IOException {
        HttpEntity entity = proxyResponse.getEntity();
        if (entity != null) {
            if (entity.isChunked()) {
                InputStream is = entity.getContent();
                OutputStream os = servletResponse.getOutputStream();
                byte[] buffer = new byte[10 * 1024];
                int read;
                while ((read = is.read(buffer)) != -1) {
                    os.write(buffer, 0, read);
                    if (doHandleCompression || is.available() == 0 /* next is.read will block */) {
                        os.flush();
                    }
                }
            } else {
                OutputStream servletOutputStream = servletResponse.getOutputStream();
                entity.writeTo(servletOutputStream);
            }
        }
    }


    protected String rewriteUrlFromRequest(HttpServletRequest servletRequest) {
        StringBuilder uri = new StringBuilder(500);
        uri.append(getTargetUri(servletRequest));
        logProducer.debug("Print Request Url", uri.toString());
        String queryString = servletRequest.getQueryString();//ex:(following '?'): name=value&foo=bar#fragment
        String fragment = null;
        if (queryString != null) {
            int fragIdx = queryString.indexOf('#');
            if (fragIdx >= 0) {
                fragment = queryString.substring(fragIdx + 1);
                queryString = queryString.substring(0, fragIdx);
            }
        }

        queryString = rewriteQueryStringFromRequest(servletRequest, queryString);
        if (queryString != null && queryString.length() > 0) {
            uri.append('?');
            // queryString未解码，因此我们需要encodeUriQuery不要对“%”个字符进行编码，以避免双重编码
            uri.append(encodeUriQuery(queryString, false));
        }

        if (doSendUrlFragment && fragment != null) {
            uri.append('#');
            // 片段未解码，因此我们需要encodeUriQuery不要对“%”个字符进行编码，以避免双重编码
            uri.append(encodeUriQuery(fragment, false));
        }
        return uri.toString();
    }

    protected String rewriteQueryStringFromRequest(HttpServletRequest servletRequest, String queryString) {
        return queryString;
    }


    protected String rewriteUrlFromResponse(HttpServletRequest servletRequest, String theUrl) {
        final String targetUri = getTargetUriBase(servletRequest);
        if (theUrl.startsWith(targetUri)) {
            StringBuffer curUrl = servletRequest.getRequestURL();//no query
            int pos;
            // 跳过协议部分
            if ((pos = curUrl.indexOf("://")) >= 0) {
                if ((pos = curUrl.indexOf("/", pos + 3)) >= 0) {
                    curUrl.setLength(pos);
                }
            }
            curUrl.append(servletRequest.getContextPath());
            curUrl.append(servletRequest.getServletPath());
            curUrl.append(theUrl, targetUri.length(), theUrl.length());
            return curUrl.toString();
        }
        return theUrl;
    }

    /**
     * 对URI的查询或片段部分中的字符进行编码。
     */
    protected CharSequence encodeUriQuery(CharSequence in, boolean encodePercent) {
        StringBuilder outBuf = null;
        Formatter formatter = null;
        for (int i = 0; i < in.length(); i++) {
            char c = in.charAt(i);
            boolean escape = true;
            if (c < 128) {
                if (asciiQueryChars.get(c) && !(encodePercent && c == '%')) {
                    escape = false;
                }
            } else if (!Character.isISOControl(c) && !Character.isSpaceChar(c)) {//not-ascii
                escape = false;
            }
            if (!escape) {
                if (outBuf != null) outBuf.append(c);
            } else {
                //escape
                if (outBuf == null) {
                    outBuf = new StringBuilder(in.length() + 5 * 3);
                    outBuf.append(in, 0, i);
                    formatter = new Formatter(outBuf);
                }
                formatter.format("%%%02X", (int) c);//TODO
            }
        }
        return outBuf != null ? outBuf : in;
    }

    protected static final BitSet asciiQueryChars;

    static {
        char[] c_unreserved = "_-!.~'()*".toCharArray();
        char[] c_punct = ",;:$&+=".toCharArray();
        char[] c_reserved = "/@".toCharArray();
        asciiQueryChars = new BitSet(128);
        for (char c = 'a'; c <= 'z'; c++) asciiQueryChars.set(c);
        for (char c = 'A'; c <= 'Z'; c++) asciiQueryChars.set(c);
        for (char c = '0'; c <= '9'; c++) asciiQueryChars.set(c);
        for (char c : c_unreserved) asciiQueryChars.set(c);
        for (char c : c_punct) asciiQueryChars.set(c);
        for (char c : c_reserved) asciiQueryChars.set(c);

        asciiQueryChars.set('%');
    }

    @Override
    public void close() throws IOException {
        if (proxyClient instanceof Closeable) {
            try {
                ((Closeable) proxyClient).close();
            } catch (IOException e) {
            }
        } else {
            if (proxyClient != null) proxyClient.getConnectionManager().shutdown();
        }
    }


}
