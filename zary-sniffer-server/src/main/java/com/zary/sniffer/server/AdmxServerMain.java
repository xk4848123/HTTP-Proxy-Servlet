package com.zary.sniffer.server;

import com.zary.sniffer.transfer.Message2;
import com.zary.sniffer.server.handle.AgentDataHandler;
import com.zary.admx.server.AgentServerStarter;
import com.zary.admx.server.handle.HandleManager;

public class AdmxServerMain {

    public static void main(String[] args) {
        AgentServerStarter agentServerStarter = new AgentServerStarter();

        HandleManager handleManager = new HandleManager();
        AgentDataHandler agentDataHandler = new AgentDataHandler();
        handleManager.addHandler(1, Message2.class, agentDataHandler::handleMessage2);

        agentServerStarter.start(handleManager);

    }

}
