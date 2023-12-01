package com.zary.sniffer.server.queue.base;

import lombok.extern.slf4j.Slf4j;
import org.apache.pulsar.client.api.PulsarClient;
import org.apache.pulsar.client.api.PulsarClientException;

import java.util.Properties;
import java.util.concurrent.TimeUnit;

@Slf4j
public class PulsarClientFactory {

    // broker集群地址,默认本地地址
    static String serviceUrl = "pulsar://127.0.0.1:6650";
    static int operationTimeoutMs = 30;
    static int statsIntervalSeconds = 60;
    static int numIoThreads = 1;
    static int numListenerThreads = 1;
    static int connectionsPerBroker = 1;
    static boolean useTcpNoDelay = true;
    static int concurrentLookupRequest = 5000;
    static int maxLookupRequest = 50000;
    static int maxNumberOfRejectedRequestPerConnection = 50;
    static int keepAliveIntervalSeconds = 30;
    static int connectionTimeoutSeconds = 10;

    private static PulsarClient newClient() {
        try {
            return PulsarClient.builder()
                    .serviceUrl(serviceUrl)
                    .operationTimeout(operationTimeoutMs, TimeUnit.SECONDS)
                    .statsInterval(statsIntervalSeconds, TimeUnit.SECONDS)
                    .connectionsPerBroker(connectionsPerBroker)
                    .keepAliveInterval(keepAliveIntervalSeconds, TimeUnit.SECONDS)
                    .maxNumberOfRejectedRequestPerConnection(maxNumberOfRejectedRequestPerConnection)
                    .maxLookupRequests(maxLookupRequest)
                    .maxConcurrentLookupRequests(concurrentLookupRequest)
                    .listenerThreads(numListenerThreads)
                    .ioThreads(numIoThreads)
                    .enableTcpNoDelay(useTcpNoDelay)
                    .connectionTimeout(connectionTimeoutSeconds, TimeUnit.SECONDS)
                    .build();
        } catch (PulsarClientException e) {
            log.error("pulsar client init fail.", e);
            return null;
        }
    }

    public static PulsarClient newByConfig(String pulsarServerUrl) {
        serviceUrl = pulsarServerUrl;
        return newClient();
    }

    public static PulsarClient newByConfig(Properties prop) {
        if (prop != null) {
            if (prop.get("pulsar.serviceUrl") != null) {
                serviceUrl = prop.getProperty("pulsar.serviceUrl");
            }
            if (prop.get("pulsar.operationTimeoutMs") != null) {
                operationTimeoutMs = Integer.parseInt(prop.getProperty("pulsar.operationTimeoutMs"));
            }
            if (prop.get("pulsar.statsIntervalSeconds") != null) {
                statsIntervalSeconds = Integer.parseInt(prop.getProperty("pulsar.statsIntervalSeconds"));
            }
            if (prop.get("pulsar.numIoThreads") != null) {
                numIoThreads = Integer.parseInt(prop.getProperty("pulsar.numIoThreads"));
            }
            if (prop.get("pulsar.numListenerThreads") != null) {
                numListenerThreads = Integer.parseInt(prop.getProperty("pulsar.numListenerThreads"));
            }
            if (prop.get("pulsar.connectionsPerBroker") != null) {
                connectionsPerBroker = Integer.parseInt(prop.getProperty("pulsar.connectionsPerBroker"));
            }
            if (prop.get("pulsar.useTcpNoDelay") != null) {
                useTcpNoDelay = Boolean.parseBoolean(prop.getProperty("pulsar.useTcpNoDelay"));
            }
            if (prop.get("pulsar.concurrentLookupRequest") != null) {
                concurrentLookupRequest = Integer.parseInt(prop.getProperty("pulsar.concurrentLookupRequest"));
            }
            if (prop.get("pulsar.maxLookupRequest") != null) {
                maxLookupRequest = Integer.parseInt(prop.getProperty("pulsar.maxLookupRequest"));
            }
            if (prop.get("pulsar.maxNumberOfRejectedRequestPerConnection") != null) {
                maxNumberOfRejectedRequestPerConnection = Integer.parseInt(prop.getProperty("pulsar.maxNumberOfRejectedRequestPerConnection"));
            }
            if (prop.get("pulsar.keepAliveIntervalSeconds") != null) {
                keepAliveIntervalSeconds = Integer.parseInt(prop.getProperty("pulsar.keepAliveIntervalSeconds"));
            }
            if (prop.get("pulsar.connectionTimeoutSeconds") != null) {
                connectionTimeoutSeconds = Integer.parseInt(prop.getProperty("pulsar.connectionTimeoutSeconds"));
            }
        }
        return newClient();
    }

}
