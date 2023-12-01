package com.zary.sniffer.transfer.wrapper;

import com.zary.sniffer.core.model.ThreadDataInfo;
import io.protostuff.ProtostuffIOUtil;
import io.protostuff.Schema;
import io.protostuff.runtime.RuntimeSchema;

import java.util.Base64;


public class ThreadDataDeserializer {
    public ThreadDataInfo doDeserialize(String threadDataInfoStr) {

        Schema<ThreadDataInfo> schema = RuntimeSchema.getSchema(ThreadDataInfo.class);

        ThreadDataInfo threadDataInfo = schema.newMessage();
        ProtostuffIOUtil.mergeFrom(Base64.getDecoder().decode(threadDataInfoStr), threadDataInfo, schema);

        return threadDataInfo;
    }

}
