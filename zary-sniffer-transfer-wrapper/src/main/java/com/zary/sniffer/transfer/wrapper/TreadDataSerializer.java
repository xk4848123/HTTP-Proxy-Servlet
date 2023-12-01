package com.zary.sniffer.transfer.wrapper;

import com.zary.sniffer.core.enums.DataOperateType;
import com.zary.sniffer.core.enums.PluginType;
import com.zary.sniffer.core.model.DataOperateInfo;
import com.zary.sniffer.core.model.ThreadDataInfo;
import com.zary.sniffer.core.model.WebRequestInfo;
import com.zary.sniffer.core.model.ext.ThreadDataInfoExt;
import io.protostuff.LinkedBuffer;
import io.protostuff.ProtostuffIOUtil;
import io.protostuff.Schema;
import io.protostuff.runtime.RuntimeSchema;

import java.lang.reflect.InvocationTargetException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

public class TreadDataSerializer {

    private LinkedBufferPool pool;

    public TreadDataSerializer() {
        pool = new LinkedBufferPool(8192);
    }

    public String doSerialize(ThreadDataInfo threadDataInfo) {
        Schema<ThreadDataInfo> schema = RuntimeSchema.getSchema(ThreadDataInfo.class);

        final byte[] protostuff;
        LinkedBuffer buffer = pool.get();
        try {
            protostuff = ProtostuffIOUtil.toByteArray(threadDataInfo, schema, buffer);
            return Base64.getEncoder().encodeToString(protostuff);
        } finally {
            pool.release(buffer);
        }
    }

    private static class LinkedBufferPool {
        private final BlockingQueue<LinkedBuffer> pool;

        public LinkedBufferPool(int maxSize) {
            this.pool = new ArrayBlockingQueue<>(maxSize);

            for (int i = 0; i < maxSize; i++) {
                pool.add(LinkedBuffer.allocate(1024));
            }
        }

        public LinkedBuffer get() {
            return pool.poll();
        }

        public void release(LinkedBuffer buf) {
            buf.clear();
            pool.offer(buf);
        }
    }

//    public static void main(String[] args) throws InterruptedException, InvocationTargetException, IllegalAccessException {
////        TransferDataUtil.start("http://127.0.0.1:9090/pulsar");
//        System.out.println(StandardCharsets.UTF_8.name());
//        ThreadDataInfo threadDataInfo = new ThreadDataInfo();
//
//        WebRequestInfo webRequestInfo = new WebRequestInfo();
//        webRequestInfo.setAppId("dddd");
//        webRequestInfo.setPluginType(PluginType.jdbc);
//        webRequestInfo.setCost(1321321321L);
//
//        threadDataInfo.setWebRequestInfo(webRequestInfo);
//
//        DataOperateInfo dataOperateInfo1 = new DataOperateInfo();
//        dataOperateInfo1.setOperateType(DataOperateType.DELETE);
//        dataOperateInfo1.setDataId("dddd");
//        dataOperateInfo1.setCost(144545455L);
//
//
//        DataOperateInfo dataOperateInfo2 = new DataOperateInfo();
//        dataOperateInfo2.setOperateType(DataOperateType.SELECT);
//        dataOperateInfo2.setDataId("dddd");
//        dataOperateInfo2.setCost(14454545511L);
//        dataOperateInfo2.setIs_exist(true);
//
//        List<DataOperateInfo> dataOperateInfoList = new ArrayList<>();
//        dataOperateInfoList.add(dataOperateInfo1);
//        dataOperateInfoList.add(dataOperateInfo2);
//
//        threadDataInfo.setDataInfos(dataOperateInfoList);
//
//
//        ThreadDataInfoExt threadDataInfoExt = BeanUtil.copyBean(ThreadDataInfoExt.class, threadDataInfo);
//        System.out.println(threadDataInfoExt);
//
//       while (true){
//           ThreadDataSender.getInstance().send(threadDataInfo);
//           ThreadDataSender.getInstance().send(threadDataInfo);
//           ThreadDataSender.getInstance().send(threadDataInfo);
//           Thread.sleep(1000);
//       }
//
//
//    }

}



