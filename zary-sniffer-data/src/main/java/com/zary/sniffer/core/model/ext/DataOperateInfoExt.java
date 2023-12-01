package com.zary.sniffer.core.model.ext;

import com.zary.sniffer.core.model.DataOperateInfo;

/**
 * @Author weiyi
 * @create 2020/3/26 16:59
 */
public class DataOperateInfoExt extends DataOperateInfo {
    /**
     * 请求ip
     */
    private String req_ip;
    /**
     * SQL语言的分类
     */
    private String sql_types;
    /**
     * SQL语言的分类名称
     */
    private String sql_types_name;

    /**
     * 扩展：应用程序名称
     */
    private String appname;
    /**
     * 参数化后sql语句
     */
    private String sql_parameterize;
    /**
     * 数据库ip
     */
    private String jdbc_ip;
    /**
     * 数据库端口
     */
    private Integer jdbc_port;
    /**
     * 数据名称
     */
    private String jdbc_databaseName;
    /**
     * 可能包含的userid
     */
    private String auth_id;
    /**
     * 标记登录操作
     */
    private boolean login_operate;
    /**
     * 威胁类型 0.无威胁 1.语法错误 2.sql注入
     */
    private int threat_type;
    /**
     * 白名单类型 0.非白 1.指纹白 2.ip白 4.sql白 用与运算标记多个
     */
    private int white_type;
    public DataOperateInfoExt(){
        white_type = 0;
        threat_type = 0;
    }

    public String getReq_ip() {
        return req_ip;
    }

    public void setReq_ip(String req_ip) {
        this.req_ip = req_ip;
    }

    public String getSql_types() {
        return sql_types;
    }

    public void setSql_types(String sql_types) {
        this.sql_types = sql_types;
    }

    public String getSql_types_name() {
        return sql_types_name;
    }

    public void setSql_types_name(String sql_types_name) {
        this.sql_types_name = sql_types_name;
    }

    public String getAppname() {
        return appname;
    }

    public void setAppname(String appname) {
        this.appname = appname;
    }

    public String getSql_parameterize() {
        return sql_parameterize;
    }

    public void setSql_parameterize(String sql_parameterize) {
        this.sql_parameterize = sql_parameterize;
    }

    public String getJdbc_ip() {
        return jdbc_ip;
    }

    public void setJdbc_ip(String jdbc_ip) {
        this.jdbc_ip = jdbc_ip;
    }

    public Integer getJdbc_port() {
        return jdbc_port;
    }

    public void setJdbc_port(Integer jdbc_port) {
        this.jdbc_port = jdbc_port;
    }

    public String getJdbc_databaseName() {
        return jdbc_databaseName;
    }

    public void setJdbc_databaseName(String jdbc_databaseName) {
        this.jdbc_databaseName = jdbc_databaseName;
    }

    public String getAuth_id() {
        return auth_id;
    }

    public void setAuth_id(String auth_id) {
        this.auth_id = auth_id;
    }

    public boolean isLogin_operate() {
        return login_operate;
    }

    public void setLogin_operate(boolean login_operate) {
        this.login_operate = login_operate;
    }

    public int getThreat_type() {
        return threat_type;
    }

    public void setThreat_type(int threat_type) {
        this.threat_type = threat_type;
    }

    public int getWhite_type() {
        return white_type;
    }

    public void setWhite_type(int white_type) {
        this.white_type = white_type;
    }
}
