package com.zary.adm.agent.core.transfer;

import com.zary.sniffer.agent.core.transfer.AgentDataUtil;
import com.zary.sniffer.agent.core.transfer.http.RequestInfo;
import com.zary.sniffer.transfer.AgentData;
import org.junit.Test;

public class AgentDataUtilTest {


    @Test
    public void agentDataSendTest() {
        AgentDataUtil.start(new RequestInfo("d","d","http://localhost:8080/pulsar"));

        Message message = new Message(1, "msg");
        AgentData<Message> agentData = new AgentData<Message>();
        agentData.setData(message);
        agentData.setType(1);
        AgentDataUtil.sendData(agentData);

        Message2 message1 = new Message2(2L, "msg1");
        AgentData<Message2> agentData1 = new AgentData<Message2>();
        agentData1.setData(message1);
        agentData1.setType(2);
        AgentDataUtil.sendData(agentData1);
    }

}
