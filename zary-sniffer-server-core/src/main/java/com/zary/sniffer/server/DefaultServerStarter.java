package com.zary.sniffer.server;

import com.zary.sniffer.server.auth.AuthChecker;
import com.zary.sniffer.server.handle.HandleManager;
import com.zary.sniffer.server.license.LicenseChecker;
import com.zary.sniffer.server.queue.DefaultConsumer;
import com.zary.sniffer.server.queue.base.PulsarClientFactory;
import org.apache.pulsar.client.api.PulsarClient;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

public class DefaultServerStarter {

    public void start(HandleManager handleManager, LicenseChecker licenseChecker, AuthChecker authChecker,
                      int ConsumerNum, String pulsarServerUrl, String topic, String jettyTargetPath, int jettyPort) {
        try {
            PulsarClient pulsarClient = PulsarClientFactory.newByConfig(pulsarServerUrl);


            ExecutorService poolExecutor = new ThreadPoolExecutor(ConsumerNum, ConsumerNum * 2, 0L,
                    TimeUnit.MILLISECONDS, new LinkedBlockingQueue<>(), new PulsrConsumerThreadFactory(), new ThreadPoolExecutor.AbortPolicy());
            for (int i = 0; i < ConsumerNum; i++) {
                poolExecutor.execute(new DefaultConsumer(topic, pulsarClient, handleManager));
            }

            new JettyServer().start(licenseChecker, authChecker, jettyTargetPath, jettyPort, pulsarClient, topic);
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }


    private static class PulsrConsumerThreadFactory implements ThreadFactory {

        private AtomicInteger atomicInteger = new AtomicInteger(1);

        @Override
        public Thread newThread(Runnable r) {
            Thread thread = new Thread(r);
            thread.setName("pulsar consumer thread:" + atomicInteger.getAndIncrement());
            thread.setPriority(Thread.MAX_PRIORITY);
            return thread;
        }
    }

}
