package com.zary.sniffer.agent.core.log;

import com.lmax.disruptor.EventHandler;
import com.zary.sniffer.agent.core.consts.CoreConsts;
import com.zary.sniffer.util.StringUtil;

/**
 * 日志消费者 disruptor
 */
public class LogEventHandler implements EventHandler<LogEvent> {
    @Override
    public void onEvent(LogEvent event, long index, boolean b) {
        try {
            if (event == null || StringUtil.isEmpty(event.getTitle())) {
                System.out.println(CoreConsts.AGENT_LOG_HEAD + "EMPTY LOG MESSAGE.");
            }
            LogWriter.writelog(event);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
