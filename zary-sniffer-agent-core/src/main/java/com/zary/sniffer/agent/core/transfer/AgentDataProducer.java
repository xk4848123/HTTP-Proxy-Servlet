package com.zary.sniffer.agent.core.transfer;

import com.lmax.disruptor.RingBuffer;
import com.zary.sniffer.agent.core.log.LogUtil;
import com.zary.sniffer.transfer.AgentData;

/**
 * 数据生产者 disruptor
 */
public class AgentDataProducer {
    /**
     * 缓存队列
     */
    private RingBuffer<AgentData<?>> ringBuffer;

    /**
     * 根据buffer构造
     */
    public AgentDataProducer(RingBuffer<AgentData<?>> ringBuffer) {
        this.ringBuffer = ringBuffer;
    }

    /**
     * 添加消息到队列
     */
    public void sendEvent(AgentData<?> agentData) throws IllegalArgumentException {
        if (agentData == null || agentData.getData() == null) {
            LogUtil.error("agentData", "data is null");
        }

        long sequence = ringBuffer.next();

        AgentData bufferData = ringBuffer.get(sequence);
        bufferData.setData(agentData.getData());
        bufferData.setType(agentData.getType());

        ringBuffer.publish(sequence);
    }
}
