package com.zary.sniffer.core.enums;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;


public enum ThreatPromptType {
    SQL注入(2),
    违规操作(999),
    账号安全(5),
    数据泄露(4),
    指纹伪造(8),
    语法错误(1),
    数据机器人(3),
    自定义策略(7);
    /**
     * 内置规则
     */

    int value;

    ThreatPromptType(int index) {
        this.value = index;
    }

    public int getValue() {
        return value;
    }

    public static String getName(int index) {
        for (ThreatPromptType type : ThreatPromptType.values()) {
            if (type.getValue() == index) {
                return type.name();
            }
        }
        return "";
    }

    public static Map<String, Integer> toMap() {
        Map<String, Integer> map = new LinkedHashMap();
        ThreatPromptType[] var1 = values();
        int var2 = var1.length;

        for (int var3 = 0; var3 < var2; ++var3) {
            ThreatPromptType type = var1[var3];
            map.put(type.name(), type.value);
        }

        return map;
    }

    /**
     * 自定义返回结果集
     * @param indexs
     * @return
     */
    public static Map<String, Integer> toMap(int... indexs) {
        Map<String, Integer> map = new LinkedHashMap();
        ThreatPromptType[] var1 = values();
        int var2 = var1.length;

        for (int var3 = 0; var3 < var2; ++var3) {
            ThreatPromptType type = var1[var3];
            if (Arrays.asList(indexs).contains(type.getValue())) {
                map.put(type.name(), type.value);
            }
        }

        return map;
    }
}
