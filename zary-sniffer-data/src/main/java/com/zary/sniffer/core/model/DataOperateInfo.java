package com.zary.sniffer.core.model;

import com.zary.sniffer.core.enums.DataOperateType;
import com.zary.sniffer.core.enums.PluginType;
import com.zary.sniffer.util.StringUtil;

/**
 * 数据跟踪信息：
 * 捕获到的数据操作记录，一个webtrack可能产生N条dataTrack
 */
public class DataOperateInfo {
    public static final String IDENTITY = "data_operate_info";

    public static final String LIST_IDENTITY = "data_operate_info_list";
    /**
     * 唯一标识
     */
    private String dataId;
    /**
     * 应用标识
     */
    private String appId;
    /**
     * 请求标识
     */
    private String reqId;
    /**
     * session id
     */
    private String session_id;
    /**
     * 客户端指纹
     */
    private String fingerprint;
    /**
     * 数据源信息(连接字符串或连接配置)
     */
    private String dataSource;
    /**
     * 数据操作类型 https://www.jb51.net/article/158527.htm
     */
    private DataOperateType operateType;
    /**
     * 数据操作语句
     */
    private String sql;
    /**
     * 数据操作原始语句指纹
     */
    private String sql_hash;
    /**
     * 数据操作后台特征语句指纹
     */
    private String sql_hash_analy;
    /**
     * 数据操作参数(预留)
     */
    @Deprecated
    private String sql_params;
    /**
     * 数据操作是否存在过(预留)
     */
    @Deprecated
    private Boolean is_exist;
    /**
     * 捕获时使用的插件类型
     */
    private PluginType pluginType;
    /**
     * 数据操作结果是否成功
     */
    private boolean res_status;
    /**
     * 数据操作结果对象(或者ResultSet实例对象)
     */
    private String res_object;
    /**
     * 数据操作结果影响行数
     */
    private int res_lines;
    /**
     * 数据操作结果采样(ResultSet尝试解析结构)
     */
    private String res_sample;
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

    public DataOperateInfo() {

    }

    public String getDataId() {
        return dataId;
    }

    public void setDataId(String dataId) {
        this.dataId = dataId;
    }

    public String getAppId() {
        return appId;
    }

    public void setAppId(String appId) {
        this.appId = appId;
    }

    public String getReqId() {
        return reqId;
    }

    public void setReqId(String reqId) {
        this.reqId = reqId;
    }

    public String getSession_id() {
        return session_id;
    }

    public void setSession_id(String session_id) {
        this.session_id = session_id;
    }

    public String getFingerprint() {
        return fingerprint;
    }

    public void setFingerprint(String fingerprint) {
        this.fingerprint = fingerprint;
    }

    public String getDataSource() {
        return dataSource;
    }

    public void setDataSource(String dataSource) {
        this.dataSource = dataSource;
    }

    public DataOperateType getOperateType() {
        return operateType;
    }

    public void setOperateType(DataOperateType operateType) {
        this.operateType = operateType;
    }

    public String getSql() {
        return sql;
    }

    public void setSql(String sql) {
        this.sql = sql;
    }

    public String getSql_hash() {
        return sql_hash;
    }

    public void setSql_hash(String sql_hash) {
        this.sql_hash = sql_hash;
    }

    public String getSql_hash_analy() {
        return sql_hash_analy;
    }

    public void setSql_hash_analy(String sql_hash_analy) {
        this.sql_hash_analy = sql_hash_analy;
    }

    public String getSql_params() {
        return sql_params;
    }

    public void setSql_params(String sql_params) {
        this.sql_params = sql_params;
    }

    public Boolean getIs_exist() {
        return is_exist;
    }

    public void setIs_exist(Boolean is_exist) {
        this.is_exist = is_exist;
    }

    public PluginType getPluginType() {
        return pluginType;
    }

    public void setPluginType(PluginType pluginType) {
        this.pluginType = pluginType;
    }

    public boolean isRes_status() {
        return res_status;
    }

    public void setRes_status(boolean res_status) {
        this.res_status = res_status;
    }

    public String getRes_object() {
        return res_object;
    }

    public void setRes_object(String res_object) {
        this.res_object = res_object;
    }

    public int getRes_lines() {
        return res_lines;
    }

    public void setRes_lines(int res_lines) {
        this.res_lines = res_lines;
    }

    public String getRes_sample() {
        return res_sample;
    }

    public void setRes_sample(String res_sample) {
        this.res_sample = res_sample;
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

}
