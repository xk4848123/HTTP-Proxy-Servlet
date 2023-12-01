package com.zary.sniffer.core.enums;

import java.util.Arrays;
import java.util.List;


public enum EnumSqlType {
    DDL("DDL", "数据定义", "CREATE,DROP,ALTER,TRUNCATE"),
    DML("DML", "数据操纵", "SELECT,SELECT INTO,UPDATE,INSERT,DELETE,REPLACE"),
    DCL("DCL", "数据操控", "GRANT,DENY,REVOKE");

    private String name;
    private String type;

    private String actions;

    EnumSqlType(String type, String name, String actions) {
        this.type = type;
        this.name = name;
        this.actions = actions;
    }

    /**
     * 获取名称
     *
     * @return
     */
    public static String getName(String type) {
        for (EnumSqlType item : EnumSqlType.values()) {
            if (item.type.equals(type)) {
                return item.name;
            }
        }
        return "";
    }

    /**
     * 获取执行动作
     *
     * @return
     */
    public static List<String> getActions(String type) {
        for (EnumSqlType item : EnumSqlType.values()) {
            if (type.equals(item.type)) {
                return Arrays.asList(item.actions.split(","));
            }
        }
        return null;
    }
}
