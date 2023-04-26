package com.zary.sniffer.agent.core.transfer.http;

import com.zary.sniffer.util.StringUtil;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.methods.RequestBuilder;

import java.util.HashMap;
import java.util.Map;

public class HttpWebRequest {
    /**
     * 目标url
     */
    private String url;
    /**
     * 访问method
     */
    private String method;
    /**
     * 参数列表
     */
    private HashMap<String, String> params;
    /**
     * Headers指定列表，覆盖默认值
     */
    private HashMap<String, String> headers;

    public HttpWebRequest(String surl, String smethod) {
        url = surl;
        method = smethod;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public HashMap<String, String> getParams() {
        return params;
    }

    public void setParams(HashMap<String, String> params) {
        this.params = params;
    }

    public HashMap<String, String> getHeaders() {
        if (headers == null || headers.size() <= 0) {
            //add default headers
            headers = new HashMap<String, String>();
            headers.put("Accept", "*/*");
            headers.put("User-Agent", WebUserAgent.getRandomUserAgent());
        }
        return headers;
    }

    public void setHeaders(HashMap<String, String> headers) {
        this.headers = headers;
    }

    public void setParam(String key, String value) {
        if (params == null)
            params = new HashMap<String, String>();
        params.put(key, value);
    }

    public void setHeader(String key, String value) {
        if (headers == null)
            headers = new HashMap<String, String>();
        headers.put(key, value);
    }

    /**
     * 当前对象转换为HttpUriRequest
     *
     * @return
     */
    protected HttpUriRequest toUriRequest() {
        if (StringUtil.isEmpty(url) || StringUtil.isEmpty(method))
            throw new IllegalArgumentException("url & method should not be empty.");
        RequestBuilder builder = RequestBuilder.create(method).setUri(url);
        //headers
        for (Map.Entry<String, String> entry : getHeaders().entrySet()) {
            builder.addHeader(entry.getKey(), entry.getValue());
        }
        //params
        if (params != null && params.size() > 0) {
            for (Map.Entry<String, String> entry : getParams().entrySet()) {
                builder.addParameter(entry.getKey(), entry.getValue());
            }
        }
        HttpUriRequest uriRequest = builder.build();
        return uriRequest;
    }
}
