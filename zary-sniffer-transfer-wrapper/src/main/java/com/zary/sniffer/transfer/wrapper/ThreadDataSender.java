package com.zary.sniffer.transfer.wrapper;

import com.zary.sniffer.core.model.ThreadDataInfo;
import com.zary.sniffer.transfer.TransferData;
import com.zary.sniffer.transfer.TransferDataUtil;

public class ThreadDataSender {

    private TreadDataSerializer treadDataSerializer;
    private static ThreadDataSender instance = new ThreadDataSender();

    private ThreadDataSender() {
        this.treadDataSerializer = new TreadDataSerializer();
    }

    public static ThreadDataSender getInstance() {
        return instance;
    }

    public void send(ThreadDataInfo threadDataInfo) {
        String threadDataInfoStr = treadDataSerializer.doSerialize(threadDataInfo);
        TransferDataUtil.sendData(new TransferData(threadDataInfoStr));
    }

}
