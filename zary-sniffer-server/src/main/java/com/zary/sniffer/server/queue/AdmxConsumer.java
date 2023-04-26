package com.zary.sniffer.server.queue;

import com.zary.sniffer.server.handle.HandleManager;
import com.zary.sniffer.server.queue.base.MessageConsumer;
import com.zary.sniffer.transfer.AgentData;
import com.zary.sniffer.util.JsonUtil;
import com.zary.sniffer.util.ThreadUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.reflect.TypeUtils;
import org.apache.pulsar.client.api.Message;
import org.apache.pulsar.client.api.Messages;
import org.apache.pulsar.client.api.PulsarClient;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.function.Function;

@Slf4j
public class AdmxConsumer implements Runnable {

    private final Properties prop;

    private final PulsarClient pulsarClient;

    private final HandleManager handleManager;

    public AdmxConsumer(Properties prop, PulsarClient pulsarClient, HandleManager handleManager) {
        this.prop = prop;
        this.pulsarClient = pulsarClient;
        this.handleManager = handleManager;
    }

    @Override
    public void run() {
        String topic = (String) prop.get("queue.topic");
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
                log.error("consume " + topic + " fail:", t);
            }
        }
    }

    private void handle(MessageConsumer consumer, Message<String> message, String data) {
        if (doHandle(data)) {
            consumer.ack(message);
        } else {
            ThreadUtils.sleep(1);
            handle(consumer, message, data);
        }
    }

    private boolean doHandle(String data) {
        List<Object> objects = JsonUtil.toJsonListObject(data, Object.class);
        int handleType = (Integer) ((Map) objects.get(0)).get("type");

        HandleManager.InnerHandler handler = handleManager.getHandler(handleType);

        Type type = handler.getType();

        List<AgentData<?>> agentData;

        if (type instanceof Class<?> || type instanceof ParameterizedType) {

            agentData = (List<AgentData<?>>) JsonUtil.parseAgentDataList(data,  TypeUtils.parameterize(AgentData.class,type));
        } else {
            log.error("It must be ensured that type is Class<?> or ParameterizedType");
            return true;
        }

        Function<List<AgentData<?>>, Boolean> agentDataConsumer = handler.getAgentDataConsumer();

        if (agentDataConsumer != null) {
            return agentDataConsumer.apply(agentData);
        }
        return true;
    }
}
