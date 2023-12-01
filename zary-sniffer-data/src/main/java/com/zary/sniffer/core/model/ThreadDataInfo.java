package com.zary.sniffer.core.model;

import java.util.ArrayList;
import java.util.List;

/**
 * 线程数据对象：
 * 封装一个请求从开始到结束过程中产生的各种数据(请求记录、数据操作记录、Span堆栈记录)，在请求结束时一次性发送到服务端
 */
public class ThreadDataInfo {
    /**
     * 请求信息
     */
    private WebRequestInfo webRequestInfo;
    /**
     * 请求产生的数据操作集合
     */
    private List<DataOperateInfo> dataInfos;
    /**
     * 请求过程产生的span拦截过程记录
     */
    private List<SpanInfo> spanInfos;

    public WebRequestInfo getWebRequestInfo() {
        return webRequestInfo;
    }

    public void setWebRequestInfo(WebRequestInfo webRequestInfo) {
        this.webRequestInfo = webRequestInfo;
    }

    public List<DataOperateInfo> getDataInfos() {
        return dataInfos;
    }

    public void setDataInfos(List<DataOperateInfo> dataInfos) {
        this.dataInfos = dataInfos;
    }

    public List<SpanInfo> getSpanInfos() {
        return spanInfos;
    }

    public void setSpanInfos(List<SpanInfo> spanInfos) {
        this.spanInfos = spanInfos;
    }

    public ThreadDataInfo() {
        webRequestInfo = new WebRequestInfo();
        dataInfos = new ArrayList<DataOperateInfo>();
        spanInfos = new ArrayList<SpanInfo>();
    }


    public void copyFrom(ThreadDataInfo source) {
        this.webRequestInfo = source.getWebRequestInfo();
        this.dataInfos = source.getDataInfos();
        this.spanInfos = source.getSpanInfos();
    }


    public boolean isValid() {
        if (webRequestInfo == null ||
                !webRequestInfo.isValid() ||
                dataInfos == null ||
                dataInfos.size() == 0){
            return Boolean.FALSE;
        }
        return Boolean.TRUE;
    }
}
