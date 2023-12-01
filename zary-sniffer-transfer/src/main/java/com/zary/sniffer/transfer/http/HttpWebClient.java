package com.zary.sniffer.transfer.http;

import com.zary.sniffer.util.StringUtil;
import org.apache.http.HttpEntityEnclosingRequest;
import org.apache.http.HttpHost;
import org.apache.http.HttpRequest;
import org.apache.http.NoHttpResponseException;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CookieStore;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.HttpRequestRetryHandler;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.conn.ConnectTimeoutException;
import org.apache.http.conn.routing.HttpRoute;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.socket.LayeredConnectionSocketFactory;
import org.apache.http.conn.socket.PlainConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.cookie.Cookie;
import org.apache.http.impl.client.*;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.ssl.SSLContexts;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLException;
import javax.net.ssl.SSLHandshakeException;
import java.io.IOException;
import java.io.InterruptedIOException;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.util.List;

public class HttpWebClient {
    /**
     * Cookie容器
     */
    private CookieStore cookieContainer;
    /**
     * 代理
     */
    private WebProxy webProxy;
    /**
     * 超时时间(默认20S)
     */
    private Integer timeout = 20000;
    /**
     * SSL设置
     */
    private SSLContext sslContext;
    /**
     * 凭证容器，存储服务器认证、代理认证凭据
     */
    private CredentialsProvider credentials;
    /**
     * 请求设置
     */
    private RequestConfig requestConfig;
    /**
     * 连接池
     */
    private PoolingHttpClientConnectionManager poolingHttpClientConnectionManager;
    /**
     * 重试策略
     */
    private HttpRequestRetryHandler requestRetryHandler;

    /**
     * 启用Cookie(参数为空时初始化空cookie容器)
     */
    public void setCookie(List<Cookie> cookies) {
        if (cookieContainer == null) {
            cookieContainer = new BasicCookieStore();
        }
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                cookieContainer.addCookie(cookie);
            }
        }
    }

    public void setProxy(WebProxy proxy) {
        webProxy = proxy;
    }

    public void setTimeOut(Integer seconds) {
        timeout = seconds * 1000;
    }

    public void setPoolingManager(int maxTotal) {
        //http socket factory
        ConnectionSocketFactory plainsf = PlainConnectionSocketFactory.getSocketFactory();
        //ssl socket factory
        LayeredConnectionSocketFactory sslsf = SSLConnectionSocketFactory.getSocketFactory();
        //registry
        Registry<ConnectionSocketFactory> registry = RegistryBuilder
                .<ConnectionSocketFactory>create().register("http", plainsf)
                .register("https", sslsf).build();
        //pooling manager
        poolingHttpClientConnectionManager = new PoolingHttpClientConnectionManager(registry);
        //连接池总大小
        poolingHttpClientConnectionManager.setMaxTotal(maxTotal);
        //默认路由大小(默认只有1个路由所以即独占)
        poolingHttpClientConnectionManager.setDefaultMaxPerRoute(maxTotal);
    }

    public void setPoolingManager(int maxTotal, int maxPerRoute, int maxRoute, String hostname, int port) {
        //http socket factory
        ConnectionSocketFactory plainsf = PlainConnectionSocketFactory.getSocketFactory();
        //ssl socket factory
        LayeredConnectionSocketFactory sslsf = SSLConnectionSocketFactory.getSocketFactory();
        //registry
        Registry<ConnectionSocketFactory> registry = RegistryBuilder
                .<ConnectionSocketFactory>create().register("http", plainsf)
                .register("https", sslsf).build();
        //pooling manager
        poolingHttpClientConnectionManager = new PoolingHttpClientConnectionManager(registry);
        //连接池总大小
        poolingHttpClientConnectionManager.setMaxTotal(maxTotal);
        //默认路由大小
        poolingHttpClientConnectionManager.setDefaultMaxPerRoute(maxPerRoute);
        //目标路由的可用大小
        HttpHost httpHost = new HttpHost(hostname, port);
        poolingHttpClientConnectionManager.setMaxPerRoute(new HttpRoute(httpHost), maxRoute);
    }

    /**
     * 开启重试策略
     *
     * @return
     */
    public void setRequestRetryHandler() {
        requestRetryHandler = new HttpRequestRetryHandler() {
            @Override
            public boolean retryRequest(IOException exception, int executionCount, org.apache.http.protocol.HttpContext context) {
                //失败1次，不重试
                if (executionCount >= 3) {
                    return false;
                }
                //如果服务器丢掉了连接，那么就重试
                if (exception instanceof NoHttpResponseException) {
                    return true;
                }
                // 不要重试SSL握手异常
                if (exception instanceof SSLHandshakeException) {
                    return false;
                }
                // 超时
                if (exception instanceof InterruptedIOException) {
                    return false;
                }
                // 目标服务器不可达
                if (exception instanceof UnknownHostException) {
                    return false;
                }
                // 连接被拒绝
                if (exception instanceof ConnectTimeoutException) {
                    return false;
                }
                // SSL握手异常
                if (exception instanceof SSLException) {
                    return false;
                }
                HttpClientContext clientContext = HttpClientContext.adapt(context);
                HttpRequest request = clientContext.getRequest();
                // 如果请求是幂等的，就再次尝试
                if (!(request instanceof HttpEntityEnclosingRequest)) {
                    return true;
                }
                return false;
            }
        };
    }

    /**
     * 忽略服务端证书
     *
     * @throws Exception
     */
    public void trustServerCert() throws NoSuchAlgorithmException, KeyStoreException, KeyManagementException {
        if (sslContext == null) {
            sslContext = SSLContexts.custom().loadTrustMaterial((chain, authType) -> {
                return true;
            }).build();
        }
    }

    public HttpWebResponse getWebContent(HttpWebRequest request) throws IOException {
        //1.如果指定代理且有密码保护，添加账号凭据
        if (webProxy != null && !StringUtil.isEmpty(webProxy.getUid())) {
            if (credentials == null) {
                credentials = new BasicCredentialsProvider();
            }
            credentials.setCredentials(AuthScope.ANY, new UsernamePasswordCredentials(webProxy.getUid(), webProxy.getPassword()));
        }
        //2.如果指定代理且是sock代理，并且没有初始化连接池，给定一个默认连接池
        if (webProxy != null && webProxy.isSockProxy() && poolingHttpClientConnectionManager == null) {
            setPoolingManager(100);
        }
        //3.初始化请求配置
        requestConfig = getRequestConfig(request);
        //4.初始化 CloseableHttpClient
        HttpClientBuilder clientBuilder = HttpClients.custom();
        if (requestConfig != null) {
            clientBuilder = clientBuilder.setDefaultRequestConfig(requestConfig);
        }
        if (sslContext != null) {
            clientBuilder = clientBuilder.setSSLContext(sslContext);
        }
        if (poolingHttpClientConnectionManager != null) {
            clientBuilder = clientBuilder.setConnectionManager(poolingHttpClientConnectionManager);
        }
        if (requestRetryHandler != null) {
            clientBuilder = clientBuilder.setRetryHandler(requestRetryHandler);
        }
        CloseableHttpClient webclient = clientBuilder.build();
        //5.初始化 HttpClientContext
        HttpClientContext context = HttpClientContext.create();
        if (cookieContainer != null) {
            context.setCookieStore(cookieContainer);
        }
        if (credentials != null) {
            context.setCredentialsProvider(credentials);
        }
        if (webProxy != null && webProxy.isSockProxy()) {
            InetSocketAddress socksaddr = new InetSocketAddress(webProxy.getIp(), webProxy.getPort());
            context.setAttribute("socks.address", socksaddr);
        }
        //6. 执行获取Response
        CloseableHttpResponse response = webclient.execute(request.toUriRequest(), context);
        HttpWebResponse res = null;
        if (response != null) {
            res = new HttpWebResponse(response, context.getCookieStore().getCookies());
        }
        return res;
    }

    private RequestConfig getRequestConfig(HttpWebRequest request) {
        RequestConfig.Builder reqBuilder = RequestConfig.custom();
        //超时处理
        if (timeout > 0) {
            reqBuilder = reqBuilder.setConnectionRequestTimeout(timeout)
                    .setConnectTimeout(timeout)
                    .setSocketTimeout(timeout);
        }
        //如果指定代理且是普通代理，在config中设置
        if (webProxy != null && !webProxy.isSockProxy() && !StringUtil.isEmpty(webProxy.getIp())) {
            HttpHost phost = new HttpHost(webProxy.getIp(), webProxy.getPort());
            reqBuilder = reqBuilder.setProxy(phost);
        }
        return reqBuilder.build();
    }
}
