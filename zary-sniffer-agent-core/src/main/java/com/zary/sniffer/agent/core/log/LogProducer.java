package com.zary.sniffer.agent.core.log;

import com.lmax.disruptor.RingBuffer;
import com.zary.sniffer.util.ExceptionUtil;
import com.zary.sniffer.util.StringUtil;

/**
 * 日志生产者 disruptor
 */
public class LogProducer {
    /**
     * 缓存队列
     */
    private RingBuffer<LogEvent> ringBuffer;
    /**
     * 日志起始级别(低于该级别日志忽略)
     */
    private LogLevel logStartLevel = LogLevel.INFO;

    public LogProducer(RingBuffer<LogEvent> ringBuffer) {
        this.ringBuffer = ringBuffer;
    }

    public LogProducer(RingBuffer<LogEvent> ringBuffer, LogLevel startLevel) {
        this.ringBuffer = ringBuffer;
        this.logStartLevel = startLevel;
    }

    /**
     * 添加日志消息到队列
     *
     * @param level
     * @param title
     * @param message
     */
    protected void sendEvent(LogLevel level, String title, String message) {
        //低于起始日志级别的忽略
        if (level.getId() < logStartLevel.getId()) {
            return;
        }
        //数据检查
        if (level == null || StringUtil.isEmpty(title) || ringBuffer == null) {
            return;
        }
        //获取可用游标
        long sequence = ringBuffer.next();
        try {
            //生成可用位置的对象
            LogEvent event = ringBuffer.get(sequence);
            event.setLevel(level);
            event.setTitle(title);
            event.setMessage(message);
        } finally {
            //确保领用后无论如何都发布
            ringBuffer.publish(sequence);
        }
    }

    /**
     * 添加日志消息到队列
     * @param event
     */
    public void sendEvent(LogEvent event) {
        try {
            sendEvent(event.getLevel(), event.getTitle(), event.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void debug(String title, String message) {
        try {
            sendEvent(LogLevel.DEBUG, title, message);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void debug(String title, Throwable e) {
        debug(title, ExceptionUtil.getStackTrace(e));
    }

    public void info(String title, String message) {
        try {
            sendEvent(LogLevel.INFO, title, message);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void info(String title, Throwable e) {
        info(title, ExceptionUtil.getStackTrace(e));
    }

    public void warn(String title, String message) {
        try {
            sendEvent(LogLevel.WARN, title, message);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void warn(String title, Throwable e) {
        warn(title, ExceptionUtil.getStackTrace(e));
    }

    public void error(String title, String message) {
        try {
            sendEvent(LogLevel.ERROR, title, message);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void error(String title, Throwable e) {
        error(title, ExceptionUtil.getStackTrace(e));
    }
}
