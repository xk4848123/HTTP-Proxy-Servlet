package com.zary.sniffer.server.queue.base;

import lombok.extern.slf4j.Slf4j;
import org.apache.pulsar.client.api.Producer;
import org.apache.pulsar.client.api.PulsarClient;
import org.apache.pulsar.client.api.PulsarClientException;
import org.apache.pulsar.client.api.Schema;

import java.util.concurrent.TimeUnit;

@Slf4j
public class MessageProducer {

    private Producer<String> producer;

    public MessageProducer(PulsarClient pulsarClient, String topic) {
        producer = newProducer(pulsarClient, topic);
    }


    private Producer<String> newProducer(PulsarClient pulsarClient, String topic) {
        try {
            return pulsarClient
                    .newProducer(Schema.STRING)
                    .topic(topic)
                    .batchingMaxPublishDelay(10, TimeUnit.MILLISECONDS)
                    .sendTimeout(10, TimeUnit.SECONDS)
                    .enableBatching(true)
                    .blockIfQueueFull(true)
                    .create();
        } catch (PulsarClientException e) {
            log.error("init producer fail.", e);
        }
        return null;
    }

    public void sendAsync(String message) {
        producer.sendAsync(message);
    }
}
