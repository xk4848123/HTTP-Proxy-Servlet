package com.zary.sniffer.agent;

import com.zary.sniffer.agent.core.AgentStarter;

import java.lang.instrument.Instrumentation;


public class AdmxAgent {

    public static void agentmain(String agentArgs, Instrumentation inst) {
        premain(agentArgs, inst);
    }

    public static void premain(String agentArgs, Instrumentation inst) {
        AgentStarter agentStarter = new AgentStarter();

        agentStarter.start(inst);
    }

}
