package com.zary.sniffer.server;

import com.zary.sniffer.server.auth.AuthChecker;
import com.zary.sniffer.server.license.LicenseChecker;
import com.zary.sniffer.server.queue.JettyHandler;
import com.zary.sniffer.server.queue.base.MessageProducer;
import lombok.extern.slf4j.Slf4j;
import org.apache.pulsar.client.api.PulsarClient;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.util.thread.QueuedThreadPool;

@Slf4j
public class JettyServer {

    private final int MAX_THREADS = 50;

    private final int MIN_THREADS = 10;

    public void start(LicenseChecker licenseChecker, AuthChecker authChecker, String jettyTargetPath, int jettyPort, PulsarClient pulsarClient, String topic) {
        try {
            QueuedThreadPool threadPool = new QueuedThreadPool(MAX_THREADS, MIN_THREADS);
            Server server = new Server(threadPool);
            server.setAttribute("org.eclipse.jetty.server.Request.maxFormContentSize", "104857600");
            ServerConnector connector = new ServerConnector(server);

            connector.setPort(jettyPort);

            server.addConnector(connector);
            MessageProducer messageProducer = new MessageProducer(pulsarClient, topic);

            server.setHandler(new JettyHandler(messageProducer, licenseChecker, authChecker, jettyTargetPath));
            server.start();
            server.join();
        } catch (Exception e) {
            log.error(e.getMessage());
        }
    }

}
