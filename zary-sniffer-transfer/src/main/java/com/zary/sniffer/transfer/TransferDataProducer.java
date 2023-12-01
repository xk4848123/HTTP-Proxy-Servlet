package com.zary.sniffer.transfer;

import com.lmax.disruptor.RingBuffer;

/**
 * 数据生产者 disruptor
 */
public class TransferDataProducer {
    /**
     * 缓存队列
     */
    private RingBuffer<TransferData> ringBuffer;

    /**
     * 根据buffer构造
     */
    public TransferDataProducer(RingBuffer<TransferData> ringBuffer) {
        this.ringBuffer = ringBuffer;
    }

    /**
     * 添加消息到队列
     */
    public void sendEvent(TransferData transferData) throws IllegalArgumentException {
        if (transferData == null || transferData.getData() == null) {
           return;
        }

        long sequence = ringBuffer.next();

        TransferData bufferData = ringBuffer.get(sequence);
        bufferData.setData(transferData.getData());
        bufferData.setType(transferData.getType());

        ringBuffer.publish(sequence);
    }
}
