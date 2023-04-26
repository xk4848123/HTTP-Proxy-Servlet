package com.zary.sniffer.server;

import com.zary.sniffer.server.queue.AdmxJettyProducer;
import com.zary.sniffer.server.queue.base.MessageProducer;
import lombok.extern.slf4j.Slf4j;
import org.apache.pulsar.client.api.PulsarClient;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.util.thread.QueuedThreadPool;

import java.util.Properties;

@Slf4j
public class JettyServer {

    private final int MAX_THREADS = 200;

    private final int MIN_THREADS = 100;

    public void start(Properties prop,PulsarClient pulsarClient) {
        try {
            QueuedThreadPool threadPool = new QueuedThreadPool(MAX_THREADS, MIN_THREADS);
            Server server = new Server(threadPool);
            server.setAttribute("org.eclipse.jetty.server.Request.maxFormContentSize", "104857600");
            ServerConnector connector = new ServerConnector(server);

            connector.setPort(Integer.valueOf((String) prop.get("http.server.port")));

            server.addConnector(connector);
            MessageProducer messageProducer = new MessageProducer(pulsarClient, (String) prop.get("queue.topic"));

            server.setHandler(new AdmxJettyProducer(messageProducer));
            server.start();
            server.join();
        } catch (Exception e) {
            log.error(e.getMessage());
        }
    }

}
