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
