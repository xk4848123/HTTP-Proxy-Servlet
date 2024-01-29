package com.zary.sniffer.util;

import java.io.PrintWriter;
import java.io.StringWriter;

public class ExceptionUtil {
    /**
     * 输出异常详细信息到字符串
     * @param e
     * @return
     */
    public static String getStackTrace(Throwable e){
        if(e==null) {
            return "";
        }
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        e.printStackTrace(pw);
        return sw.toString();
    }
}
