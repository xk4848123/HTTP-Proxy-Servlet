package com.zary.sniffer.transfer.http;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.ProtocolVersion;
import org.apache.http.StatusLine;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.cookie.Cookie;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.List;
import java.util.Locale;

public class HttpWebResponse {
    /**
     * 响应状态
     */
    private StatusLine statusLine;
    /**
     * 区域编码信息
     */
    private Locale locale;
    /**
     * 响应内容实体
     */
    private HttpEntity httpEntity;
    /**
     * 响应头集合
     */
    private Header[] headers;
    /**
     * cookies
     */
    private List<Cookie> cookies;
    /**
     * 协议版本
     */
    private ProtocolVersion protocolVersion;

    /**
     * 原始响应对象
     */
    private CloseableHttpResponse httpResponse;

    /**
     * 根据apache响应对象构建实例
     *
     * @param response
     */
    public HttpWebResponse(CloseableHttpResponse response, List<Cookie> cookie) {
        if (response != null) {
            statusLine = response.getStatusLine();
            locale = response.getLocale();
            httpEntity = response.getEntity();
            headers = response.getAllHeaders();
            protocolVersion = response.getProtocolVersion();
            cookies = cookie;
            httpResponse = response;
        }
    }

    /**
     * 释放响应对象(httpEntity使用之后)
     */
    public void close() {
        if (httpResponse != null) {
            try {
                httpResponse.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 获取HTTP响应码
     *
     * @return
     */
    public Integer getStatusCode() {
        return statusLine.getStatusCode();
    }

    /**
     * 获取响应协议版本号 HTTP/1.1
     *
     * @return
     */
    public String getProtocalVersion() {
        return statusLine.getProtocolVersion().toString();
    }

    /**
     * 获取响应内容文本（使用默认字符集）
     *
     * @return
     * @throws IOException
     */
    public String getContent() throws IOException {
        return EntityUtils.toString(httpEntity);
    }

    /**
     * 获取响应内容文本（使用指定字符集）
     *
     * @return
     * @throws IOException
     */
    public String getContent(Charset charset) throws IOException {
        if (charset == null)
            return getContent();
        return EntityUtils.toString(httpEntity, charset);
    }

    public Locale getLocale() {
        return locale;
    }

    /**
     * 获取响应实体
     */
    public HttpEntity getHttpEntity() {
        return httpEntity;
    }

    /**
     * 获取响应头集合
     */
    public Header[] getHeaders() {
        return headers;
    }

    public List<Cookie> getCookies() {
        return cookies;
    }

    public void setCookies(List<Cookie> cookies) {
        this.cookies = cookies;
    }
}
