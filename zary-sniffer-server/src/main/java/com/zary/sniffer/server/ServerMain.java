package com.zary.sniffer.server;


import com.zary.sniffer.server.handle.HandleManager;

public class ServerMain {

    public static void main(String[] args) {
        AgentServerStarter agentServerStarter = new AgentServerStarter();

        HandleManager handleManager = new HandleManager();
        //根据业务添加处理器
        //handleManager.addHandler(...);
        agentServerStarter.start(handleManager);

    }

}
