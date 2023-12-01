package com.zary.sniffer.agent.core.log;

import com.zary.sniffer.util.DateUtil;

import java.util.Date;

/**
 * 日志消息体 disruptor event
 */

public class LogEvent {
    /**
     * 级别
     */
    private LogLevel level;
    /**
     * 标题
     */
    private String title;
    /**
     * 内容
     */
    private String message;

    public LogLevel getLevel() {
        return level;
    }

    public void setLevel(LogLevel level) {
        this.level = level;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public LogEvent() {
    }

    public LogEvent(LogLevel level, String title, String message) {
        this.level = level;
        this.title = title;
        this.message = message;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("--------------------------------------------------------------------------\n");
        sb.append(String.format("[%s][%s]:%s \n",
                this.getLevel().getName(),
                DateUtil.toDateString(new Date(), "yyyy-MM-dd HH:mm:ss.SSS"),
                this.getTitle()));
        sb.append(this.getMessage());
        sb.append("\n");
        return sb.toString();
    }
}
