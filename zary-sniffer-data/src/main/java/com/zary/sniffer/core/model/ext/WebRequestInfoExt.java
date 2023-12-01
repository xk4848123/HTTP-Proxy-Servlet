package com.zary.sniffer.core.model.ext;

import com.zary.sniffer.core.model.WebRequestInfo;

/**
 * 应用程序Web跟踪信息：
 * 每个请求从进入代码到处理完成会产生一条web跟踪信息
 *
 * @author weiyi
 */
public class WebRequestInfoExt extends WebRequestInfo {

    private String os_name;
    private String geo;
    private String browser_name;
    /**
     * 白名单类型 0.非白 1.指纹白 2.ip白 4.sql白 用与运算标记多个
     */
    private int white_type;
    public WebRequestInfoExt() {
        white_type = 0;
    }

}
