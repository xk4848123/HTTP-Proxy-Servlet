package com.zary.sniffer.agent.core.log;


import com.zary.sniffer.util.StringUtil;

/**
 * 日志级别枚举
 */
public enum LogLevel {
    DEBUG(1, "DEBUG"), INFO(2, "INFO"), WARN(3, "WARN"), ERROR(4, "ERROR");

    private int level_id;
    private String level_name;

    LogLevel(int id, String name) {
        level_id = id;
        level_name = name;
    }

    public int getId() {
        return level_id;
    }

    public String getName() {
        return level_name;
    }

    /**
     * 根据字符串获取枚举，忽略大小写
     *
     * @param name
     * @return
     */
    public static LogLevel fromName(String name) {
        if(StringUtil.isEmpty(name))
            return LogLevel.INFO;
        for (LogLevel level : values()) {
            if (name.equalsIgnoreCase(level.getName())) {
                return level;
            }
        }
        return LogLevel.INFO;
    }
}
