package com.zary.sniffer.core.enums;


public enum ComStateType {
    删除(-1),
    正常(0),
    禁用(1);

    private int value;

    ComStateType(int value) {
        this.value = value;
    }

    public int getValue() {
        return this.value;
    }

    public static String getName(int value) {
        for (ComStateType type : ComStateType.values()) {
            if (type.getValue() == value) {
                return type.toString();
            }
        }
        return "";
    }
}
