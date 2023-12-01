package com.zary.sniffer.config;

import com.zary.sniffer.util.RegexUtil;
import com.zary.sniffer.util.StringUtil;

import java.util.List;

public class Config {

    private String instance;

    private String env;

    private String token;

    private String server;

    private boolean auto_script;

    private String auto_pass;

    private boolean open_span;

    private List<AppConfig> apps;

    private String script;

    public Config() {
    }

    public String getInstance() {
        return instance;
    }

    public void setInstance(String instance) {
        this.instance = instance;
    }

    public String getEnv() {
        return env;
    }

    public void setEnv(String env) {
        this.env = env;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getServer() {
        return server;
    }

    public void setServer(String server) {
        this.server = server;
    }

    public boolean isAuto_script() {
        return auto_script;
    }

    public void setAuto_script(boolean auto_script) {
        this.auto_script = auto_script;
    }

    public String getAuto_pass() {
        return auto_pass;
    }

    public void setAuto_pass(String auto_pass) {
        this.auto_pass = auto_pass;
    }

    public boolean isOpen_span() {
        return open_span;
    }

    public void setOpen_span(boolean open_span) {
        this.open_span = open_span;
    }

    public List<AppConfig> getApps() {
        return apps;
    }

    public void setApps(List<AppConfig> apps) {
        this.apps = apps;
    }

    public String getScript() {
        return script;
    }

    public void setScript(String script) {
        this.script = script;
    }

    public boolean isValid() {
        if (instance == null || instance.length() == 0) {
            return false;
        }
        if (token == null || token.length() == 0) {
            return false;
        }
        if (apps == null) {
            return false;
        }
        if (server == null || server.length() == 0) {
            return false;
        }
        for (AppConfig app : apps) {
            if (!app.isValid()) {
                return false;
            }
        }
        return true;
    }

    public String getAppid(String clsname) {
        if (clsname == null || clsname.length() == 0 || apps == null || apps.size() == 0) {
            return "";
        }
        for (AppConfig app : apps) {
            String[] appSpaces = app.getSpace().split(",");
            if (appSpaces != null) {
                for (String appSpace : appSpaces) {
                    if (clsname.startsWith(appSpace)) {
                        return app.getAppid();
                    }
                }
            }
        }
        return "";
    }

    public boolean isAutoPass(String url) {
        try {
            String auto_pass = this.auto_pass;
            if (StringUtil.isNotEmpty(auto_pass)) {
                String[] arrs = auto_pass.split(",");
                for (String item : arrs) {
                    if (StringUtil.isNotEmpty(item) && RegexUtil.matchs(item, url).size() > 0) {
                        return true;
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }
}
