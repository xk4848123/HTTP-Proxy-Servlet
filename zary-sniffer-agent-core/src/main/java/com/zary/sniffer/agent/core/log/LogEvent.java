package com.zary.sniffer.agent.core.log;

import com.zary.sniffer.util.DateUtil;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

/**
 * 日志消息体 disruptor event
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
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
