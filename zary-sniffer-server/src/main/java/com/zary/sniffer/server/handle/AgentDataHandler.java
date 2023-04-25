package com.zary.sniffer.server.handle;

import com.zary.sniffer.transfer.Message2;
import com.zary.admx.model.AgentData;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

@Slf4j
public class AgentDataHandler {

    public Boolean handleMessage2(List<AgentData<Message2>> datas) {
        for (AgentData agentData : datas) {
            Message2 message2 = (Message2) agentData.getData();
            log.info(message2.toString());
        }
        return true;
    }

}
