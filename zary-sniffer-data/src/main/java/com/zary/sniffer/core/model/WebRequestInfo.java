package com.zary.sniffer.core.model;

import com.zary.sniffer.core.enums.PluginType;


/**
 * 应用程序Web跟踪信息：
 * 每个请求从进入代码到处理完成会产生一条web跟踪信息
 */

public class WebRequestInfo {
    public static final String IDENTITY = "web_request_info";

    /**
     * 唯一标识
     */
    private String reqId;
    /**
     * 应用程序标识
     */
    private String appId;
    /**
     * 客户端指纹
     */
    private String fingerprint;
    /**
     * 请求url
     */
    private String req_url;
    /**
     * 请求method
     */
    private String req_method;
    /**
     * 请求agent
     */
    private String req_agent;
    /**
     * 请求来源IP
     */
    private String req_ip;
    /**
     * 请求大小
     */
    private long req_size;
    /**
     * 请求参数
     */
    private String req_params;
    /**
     * 请求cookie
     */
    private String req_cookie;
    /**
     * 请求头信息(保留)
     */
    @Deprecated
    private String req_headers;
    /**
     * session id
     */
    private String session_id;
    /**
     * 响应头信息(保留)
     */
    @Deprecated
    private String rep_headers;
    /**
     * 响应码
     */
    private String rep_code;
    /**
     * 响应类型
     */
    private String rep_content_type;
    /**
     * 响应大小
     */
    private long rep_size;
    /**
     * 捕获时使用的插件类型
     */
    private PluginType pluginType;
    /**
     * 开始时间
     */
    private long starttime;
    /**
     * 结束时间
     */
    private long endtime;
    /**
     * 总耗时
     */
    private long cost;
    /**
     * 可能包含的登录id
     */
    private String auth_id;

    public WebRequestInfo() {

    }


    public WebRequestInfo(String reqId, String appId, String fingerprint, String req_url, String req_method, String req_agent,
                          String req_ip, long req_size, String req_params, String req_cookie, String req_headers, String session_id,
                          String rep_headers, String rep_code, String rep_content_type, long rep_size, PluginType pluginType, long starttime, long endtime, long cost, String auth_id) {
        this.reqId = reqId;
        this.appId = appId;
        this.fingerprint = fingerprint;
        this.req_url = req_url;
        this.req_method = req_method;
        this.req_agent = req_agent;
        this.req_ip = req_ip;
        this.req_size = req_size;
        this.req_params = req_params;
        this.req_cookie = req_cookie;
        this.req_headers = req_headers;
        this.session_id = session_id;
        this.rep_headers = rep_headers;
        this.rep_code = rep_code;
        this.rep_content_type = rep_content_type;
        this.rep_size = rep_size;
        this.pluginType = pluginType;
        this.starttime = starttime;
        this.endtime = endtime;
        this.cost = cost;
        this.auth_id = auth_id;
    }


    public String getReqId() {
        return reqId;
    }

    public void setReqId(String reqId) {
        this.reqId = reqId;
    }

    public String getAppId() {
        return appId;
    }

    public void setAppId(String appId) {
        this.appId = appId;
    }

    public String getFingerprint() {
        return fingerprint;
    }

    public void setFingerprint(String fingerprint) {
        this.fingerprint = fingerprint;
    }

    public String getReq_url() {
        return req_url;
    }

    public void setReq_url(String req_url) {
        this.req_url = req_url;
    }

    public String getReq_method() {
        return req_method;
    }

    public void setReq_method(String req_method) {
        this.req_method = req_method;
    }

    public String getReq_agent() {
        return req_agent;
    }

    public void setReq_agent(String req_agent) {
        this.req_agent = req_agent;
    }

    public String getReq_ip() {
        return req_ip;
    }

    public void setReq_ip(String req_ip) {
        this.req_ip = req_ip;
    }

    public long getReq_size() {
        return req_size;
    }

    public void setReq_size(long req_size) {
        this.req_size = req_size;
    }

    public String getReq_params() {
        return req_params;
    }

    public void setReq_params(String req_params) {
        this.req_params = req_params;
    }

    public String getReq_cookie() {
        return req_cookie;
    }

    public void setReq_cookie(String req_cookie) {
        this.req_cookie = req_cookie;
    }

    public String getReq_headers() {
        return req_headers;
    }

    public void setReq_headers(String req_headers) {
        this.req_headers = req_headers;
    }

    public String getSession_id() {
        return session_id;
    }

    public void setSession_id(String session_id) {
        this.session_id = session_id;
    }

    public String getRep_headers() {
        return rep_headers;
    }

    public void setRep_headers(String rep_headers) {
        this.rep_headers = rep_headers;
    }

    public String getRep_code() {
        return rep_code;
    }

    public void setRep_code(String rep_code) {
        this.rep_code = rep_code;
    }

    public String getRep_content_type() {
        return rep_content_type;
    }

    public void setRep_content_type(String rep_content_type) {
        this.rep_content_type = rep_content_type;
    }

    public long getRep_size() {
        return rep_size;
    }

    public void setRep_size(long rep_size) {
        this.rep_size = rep_size;
    }

    public PluginType getPluginType() {
        return pluginType;
    }

    public void setPluginType(PluginType pluginType) {
        this.pluginType = pluginType;
    }

    public long getStarttime() {
        return starttime;
    }

    public void setStarttime(long starttime) {
        this.starttime = starttime;
    }

    public long getEndtime() {
        return endtime;
    }

    public void setEndtime(long endtime) {
        this.endtime = endtime;
    }

    public long getCost() {
        return cost;
    }

    public void setCost(long cost) {
        this.cost = cost;
    }

    public String getAuth_id() {
        return auth_id;
    }

    public void setAuth_id(String auth_id) {
        this.auth_id = auth_id;
    }

    public boolean isValid() {
        if (reqId == null || "".equals(reqId) ||
                appId == null || "".equals(appId) ||
                req_url == null || "".equals(req_url) ||
                pluginType == PluginType.unknown) {
            return Boolean.FALSE;
        }
        return Boolean.TRUE;
    }


}
