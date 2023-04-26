package com.zary.sniffer.server;

import com.zary.sniffer.util.PropUtil;
import com.zary.sniffer.server.handle.HandleManager;
import com.zary.sniffer.server.queue.AdmxConsumer;
import com.zary.sniffer.server.queue.base.PulsarClientFactory;
import org.apache.pulsar.client.api.PulsarClient;

import java.util.Properties;

public class AgentServerStarter {

    public void start(HandleManager handleManager) {
        try {
            Properties prop = PropUtil.readProperties();
            PulsarClient pulsarClient = PulsarClientFactory.newByConfig(prop);

            new Thread(new AdmxConsumer(prop,pulsarClient,handleManager)).start();

            new JettyServer().start(prop, pulsarClient);
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }

}
