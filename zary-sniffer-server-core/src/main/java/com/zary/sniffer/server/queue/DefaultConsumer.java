package com.zary.sniffer.server.queue;

import com.zary.sniffer.server.handle.HandleManager;
import com.zary.sniffer.server.queue.base.MessageConsumer;
import lombok.extern.slf4j.Slf4j;
import org.apache.pulsar.client.api.Message;
import org.apache.pulsar.client.api.Messages;
import org.apache.pulsar.client.api.PulsarClient;


import java.util.function.Function;

@Slf4j
public class DefaultConsumer implements Runnable {

    private final String topic;

    private final PulsarClient pulsarClient;

    private final HandleManager handleManager;

    public DefaultConsumer(String topic, PulsarClient pulsarClient, HandleManager handleManager) {
        this.topic = topic;
        this.pulsarClient = pulsarClient;
        this.handleManager = handleManager;
    }

    @Override
    public void run() {
        String subscription = Thread.currentThread().getName();
        MessageConsumer consumer = new MessageConsumer(pulsarClient, topic, subscription);
        while (true) {
            try {
                Messages<String> messages = consumer.batchReceive();
                if (messages == null) {
                    continue;
                }
                for (Message<String> message : messages) {
                    String data = new String(message.getData());
                    handle(consumer, message, data);
                }
            } catch (Throwable t) {
                log.error("consume:" + topic + " fail:", t);
            }
        }
    }

    private void handle(MessageConsumer consumer, Message<String> message, String data) {
        if (doHandle(data)) {
            consumer.ack(message);
        } else {
            consumer.neAck(message);
        }
    }

    private boolean doHandle(String data) {
        String[] parts = data.split("\\|");
        if (parts.length < 2) {
            return true;
        }
        String message = parts[0];
        int type = Integer.parseInt(parts[1]);

        HandleManager.InnerHandler handler = handleManager.getHandler(type);

        Function<String, Boolean> dataHandler = handler.getDataHandler();

        if (dataHandler != null) {
            return dataHandler.apply(message);
        }
        return true;
    }
}
