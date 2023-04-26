package com.zary.sniffer.agent.core.transfer;

import com.lmax.disruptor.EventFactory;
import com.zary.sniffer.transfer.AgentData;

/**
 * 请求信息创建工厂 disruptor
 */
public class AgentDataEventFactory implements EventFactory<AgentData<?>> {

    @Override
    public AgentData newInstance() {
        return new AgentData();
    }
}
