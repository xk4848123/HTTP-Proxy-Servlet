package com.zary.sniffer.core.model.ext;

import com.zary.sniffer.core.model.SpanInfo;

import java.util.ArrayList;
import java.util.List;

/**
 * ThreadDataInfo数据语法分析结果集
 *
 * @Author weiyi
 * @create 2020/3/19 13:57
 */
public class ThreadDataInfoExt {
    /**
     * 请求信息
     */
    private WebRequestInfoExt webRequestInfo;
    /**
     * 请求产生的数据操作集合
     */
    private List<DataOperateInfoExt> dataInfos;
    /**
     * sql语法分析结果集合
     */
    private List<SqlParseResultInfoExt> sqlParseResultInfos;
    /**
     * 请求过程产生的span拦截过程记录
     */
    private List<SpanInfo> spanInfos;

    public ThreadDataInfoExt() {
        sqlParseResultInfos = new ArrayList<>();
        dataInfos = new ArrayList<>();
        webRequestInfo = new WebRequestInfoExt();
        spanInfos = new ArrayList<>();
    }

    public WebRequestInfoExt getWebRequestInfo() {
        return webRequestInfo;
    }

    public void setWebRequestInfo(WebRequestInfoExt webRequestInfo) {
        this.webRequestInfo = webRequestInfo;
    }

    public List<DataOperateInfoExt> getDataInfos() {
        return dataInfos;
    }

    public void setDataInfos(List<DataOperateInfoExt> dataInfos) {
        this.dataInfos = dataInfos;
    }

    public List<SqlParseResultInfoExt> getSqlParseResultInfos() {
        return sqlParseResultInfos;
    }

    public void setSqlParseResultInfos(List<SqlParseResultInfoExt> sqlParseResultInfos) {
        this.sqlParseResultInfos = sqlParseResultInfos;
    }

    public List<SpanInfo> getSpanInfos() {
        return spanInfos;
    }

    public void setSpanInfos(List<SpanInfo> spanInfos) {
        this.spanInfos = spanInfos;
    }
}
