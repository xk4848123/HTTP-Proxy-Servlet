package com.zary.sniffer.server.utils;
import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.ReadContext;
import org.nutz.json.Json;

/**
 * Json处理辅助函数（基于nutz.json）
 *
 * @author xulibo
 * @version 2017/11/3
 */
public class JsonUtil extends Json {
    /**
     * 读取路径集合
     * @param json
     * @param path
     * @return
     */
    public static<T> T readPath(String json,String path){
        ReadContext reader = JsonPath.parse(json);
        return reader.read(path);
    }

    public static String getJsonString(Object object){
        return toJson(object);
    }
}
