package com.zary.sniffer.agent.core.log;

import com.lmax.disruptor.EventFactory;

/**
 * 日志模型创建工厂 disruptor
 */
public class LogEventFactory implements EventFactory<LogEvent> {
    @Override
    public LogEvent newInstance() {
        return new LogEvent();
    }
}
