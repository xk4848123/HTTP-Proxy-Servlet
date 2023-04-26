package com.zary.sniffer.server.queue.base;

import lombok.extern.slf4j.Slf4j;
import org.apache.pulsar.client.api.*;

import java.util.concurrent.TimeUnit;

@Slf4j
public class MessageConsumer {

    private final Consumer<String> consumer;

    public MessageConsumer(PulsarClient pulsarClient, String topic, String subscription) {
        this.consumer = createConsumer_single(pulsarClient, topic, subscription, null);
    }

    private Consumer<String> createConsumer_single(PulsarClient pulsarClient, String topicName, String subscription, SubscriptionType subType) {
        try {
            return pulsarClient
                    .newConsumer(Schema.STRING)
                    .topic(topicName)
                    .subscriptionName(subscription)
                    .ackTimeout(10, TimeUnit.SECONDS)
                    .subscriptionType(subType == null ? SubscriptionType.Exclusive : subType)
                    .batchReceivePolicy(
                            BatchReceivePolicy
                                    .builder()
                                    .maxNumBytes(1024 * 1024)
                                    .maxNumMessages(100)
                                    .timeout(2000, TimeUnit.MICROSECONDS)
                                    .build())
                    .subscribe();
        } catch (Throwable t) {
            log.error("create consumer fail:", t);
        }
        return null;
    }

    public Messages<String> batchReceive() {
        try {
            Messages<String> messages = consumer.batchReceive();
            return messages;
        } catch (Throwable t) {
            log.error("consumer receive message fail:", t);
        }
        return null;
    }

    public void ack(Message<String> message) {
        try {
            consumer.acknowledge(message);
        } catch (Throwable t) {
            log.error("consumer ack fail:", t);
        }
    }

}
