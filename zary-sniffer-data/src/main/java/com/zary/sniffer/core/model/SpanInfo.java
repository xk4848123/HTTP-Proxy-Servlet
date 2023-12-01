package com.zary.sniffer.core.model;

import com.zary.sniffer.core.enums.PluginType;


/**
 * Span是一个拦截点输出的过程记录，是系统定义的Plugin的调用堆栈记录
 * 如一次操作数据的http请求，探针内置的ServletPlugin、MysqlPlugin均会拦截请求并生成Span数据
 */

public class SpanInfo {
    /**
     * 唯一标识
     */
    private String spanId;
    /**
     * 应用标识
     */
    private String appId;
    /**
     * 请求标识
     */
    private String reqId;
    /**
     * 上级SpanId：
     * Span是有层级关系的，上一层Span包含了下一层Span的执行过程
     * ... [span A start]
     * ... ... [span B start]
     * ... ... [span B end]
     * ... [span A end]
     */
    private String parentSpanId;
    /**
     * 捕获时使用的插件类型
     */
    private PluginType pluginType;
    /**
     * 插件名称
     */
    private String pluginName;
    /**
     * 类名
     */
    private String className;
    /**
     * 函数名
     */
    private String methodName;
    /**
     * 函数参数
     */
    private String methodArgs;
    /**
     * 行号(保留)
     */
    @Deprecated
    private String lineNum;
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

    public SpanInfo() {
    }

    public String getSpanId() {
        return spanId;
    }

    public void setSpanId(String spanId) {
        this.spanId = spanId;
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

    public String getParentSpanId() {
        return parentSpanId;
    }

    public void setParentSpanId(String parentSpanId) {
        this.parentSpanId = parentSpanId;
    }

    public PluginType getPluginType() {
        return pluginType;
    }

    public void setPluginType(PluginType pluginType) {
        this.pluginType = pluginType;
    }

    public String getPluginName() {
        return pluginName;
    }

    public void setPluginName(String pluginName) {
        this.pluginName = pluginName;
    }

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public String getMethodName() {
        return methodName;
    }

    public void setMethodName(String methodName) {
        this.methodName = methodName;
    }

    public String getMethodArgs() {
        return methodArgs;
    }

    public void setMethodArgs(String methodArgs) {
        this.methodArgs = methodArgs;
    }

    public String getLineNum() {
        return lineNum;
    }

    public void setLineNum(String lineNum) {
        this.lineNum = lineNum;
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

    public SpanInfo(String spanId, String appId, String reqId, String parentSpanId, PluginType pluginType, String pluginName,
                    String className, String methodName, String methodArgs, String lineNum, long starttime, long endtime, long cost) {
        this.spanId = spanId;
        this.appId = appId;
        this.reqId = reqId;
        this.parentSpanId = parentSpanId;
        this.pluginType = pluginType;
        this.pluginName = pluginName;
        this.className = className;
        this.methodName = methodName;
        this.methodArgs = methodArgs;
        this.lineNum = lineNum;
        this.starttime = starttime;
        this.endtime = endtime;
        this.cost = cost;
    }
}
