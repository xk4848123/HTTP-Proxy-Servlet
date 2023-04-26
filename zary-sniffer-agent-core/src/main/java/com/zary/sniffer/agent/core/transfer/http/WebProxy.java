package com.zary.sniffer.agent.core.transfer.http;

public class WebProxy {
    /**
     * 代理IP
     */
    private String ip;
    /**
     * 代理端口
     */
    private Integer port;
    /**
     * 访问账号ID
     */
    private String uid;
    /**
     * 访问密码
     */
    private String password;
    /**
     * 是否是sock代理
     */
    private boolean isSockProxy;

    public WebProxy(String _ip,Integer _port,boolean _isSockProxy){
        ip =_ip;
        port = _port;
        isSockProxy = _isSockProxy;
    }
    public WebProxy(String _ip,Integer _port,String _uid,String _password,boolean _isSockProxy){
        ip =_ip;
        port = _port;
        uid = _uid;
        password = _password;
        isSockProxy = _isSockProxy;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public Integer getPort() {
        return port;
    }

    public void setPort(Integer port) {
        this.port = port;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public boolean isSockProxy() {
        return isSockProxy;
    }

    public void setSockProxy(boolean sockProxy) {
        isSockProxy = sockProxy;
    }
}
