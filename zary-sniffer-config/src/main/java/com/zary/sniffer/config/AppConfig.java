package com.zary.sniffer.config;

public class AppConfig {
    private String appid;

    private String name;

    private String url;

    private String authurl;

    private String authkey;

    private String space;

    public AppConfig() {
    }

    public void setAppid(String appid) {
        this.appid = appid;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public void setAuthurl(String authurl) {
        this.authurl = authurl;
    }

    public void setAuthkey(String authkey) {
        this.authkey = authkey;
    }

    public void setSpace(String space) {
        this.space = space;
    }

    public String getAppid() {
        return appid;
    }

    public String getName() {
        return name;
    }

    public String getUrl() {
        return url;
    }

    public String getAuthurl() {
        return authurl;
    }

    public String getAuthkey() {
        return authkey;
    }

    public String getSpace() {
        return space;
    }

    public boolean isValid() {
        if (appid == null || appid.length() == 0) {
            return false;
        }

        if (name == null || name.length() == 0) {
            return false;
        }

        return space != null && space.length() != 0;
    }
}
