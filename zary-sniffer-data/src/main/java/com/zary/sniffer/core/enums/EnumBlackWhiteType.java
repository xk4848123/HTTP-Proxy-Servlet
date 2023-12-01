package com.zary.sniffer.core.enums;

/**
 * @Author weiyi
 * @create 2020/4/6 11:21
 */
public enum EnumBlackWhiteType {
    fingerprint(1),
    ip(2),
    sql(3);

    int value;

    EnumBlackWhiteType(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }

    public static String getName(int value) {
        for (EnumBlackWhiteType type : EnumBlackWhiteType.values()) {
            if (type.getValue() == value) {
                return type.name();
            }
        }
        return "";
    }

    public static EnumBlackWhiteType get(int value) {
        for (EnumBlackWhiteType item : values()) {
            if (item.getValue() == value) {
                return item;
            }
        }
        return null;
    }


}
