package com.zary.sniffer.core.enums;

import java.util.LinkedHashMap;
import java.util.Map;

public enum EnumActionType {
    查询(1, "SELECT,SELECT INTO"),
    修改(2, "INSERT,UPDATE,REPLACE"),
    删除(3, "DROP,DELETE,TRUNCATE"),
    管理(4, "CREATE,ALTER");

    private int index;

    private String actions;

    EnumActionType(int index) {
        this.index = index;
    }

    EnumActionType(int index, String actions) {
        this.index = index;
        this.actions = actions;
    }

    /**
     * 获取名称
     *
     * @param index
     * @return
     */
    public static String getName(int index) {
        for (EnumActionType type : EnumActionType.values()) {
            if (type.index == index) {
                return type.name();
            }
        }
        return "";
    }

    /**
     * 获取执行动作
     *
     * @param index
     * @return
     */
    public static String[] getActions(int index) {
        for (EnumActionType type : EnumActionType.values()) {
            if (type.index == index) {
                return type.actions.split(",");
            }
        }
        return null;
    }

    /**
     * 获取map
     *
     * @return
     */
    public static Map<String, String[]> toMap() {
        Map<String, String[]> map = new LinkedHashMap();
        for (EnumActionType type : EnumActionType.values()) {
            map.put(type.name(), type.actions.split(","));
        }
        return map;
    }
}
