package com.zary.sniffer.transfer;

import com.lmax.disruptor.EventFactory;

/**
 * 请求信息创建工厂 disruptor
 */
public class TransferDataEventFactory implements EventFactory<TransferData> {

    @Override
    public TransferData newInstance() {
        return new TransferData();
    }
}
