package com.zary.sniffer.agent.core.transfer;

import com.lmax.disruptor.EventHandler;
import com.zary.sniffer.transfer.AgentData;
import com.zary.sniffer.util.DateUtil;
import com.zary.sniffer.util.JsonUtil;
import com.zary.sniffer.agent.core.log.LogUtil;
import com.zary.sniffer.agent.core.transfer.http.HttpWebClient;
import com.zary.sniffer.agent.core.transfer.http.HttpWebRequest;
import com.zary.sniffer.agent.core.transfer.http.HttpWebResponse;
import com.zary.sniffer.agent.core.transfer.http.RequestInfo;
import org.apache.http.HttpStatus;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 数据消费者(单消费者) disruptor
 * 批量发送数据到服务端
 */
public class AgentDataEventHandler implements EventHandler<AgentData<?>> {

    private final RequestInfo requestInfo;

    private final HttpWebClient httpWebClient;

    /**
     * 临时数据缓存buffer
     */
    private final List<AgentData> agentDataCache = new ArrayList<AgentData>();
    /**
     * 临时数据缓存大小，数据积累发送
     */
    private static final int CACHE_BULK_SIZE = 2;

    private static final long MAX_TIME_INTERVAL = 500;

    private final long lastSendTimeStamp = DateUtil.getNowTimestamp();

    public AgentDataEventHandler(RequestInfo requestInfo, HttpWebClient httpWebClient) {
        this.requestInfo = requestInfo;
        this.httpWebClient = httpWebClient;
    }

    @Override
    public void onEvent(AgentData event, long sequence, boolean endOfBatch) {
        try {
            agentDataCache.add(event);

            int cacheSize = agentDataCache.size();
            long timeDifference = DateUtil.getNowTimestamp() - lastSendTimeStamp;

            if (cacheSize >= CACHE_BULK_SIZE || timeDifference > MAX_TIME_INTERVAL) {
                boolean success = sendToServer(agentDataCache);
                if (success) {
                    agentDataCache.clear();
                } else {
                    LogUtil.error("SEND DATA FAILED::CURRENT SIZE", String.valueOf(agentDataCache.size()));
                }
            }
        } catch (Throwable t) {
            LogUtil.warn("SEND DATA FAILED::EXCEPTION", t);
        }
    }

    /**
     * 发送缓存
     */
    private boolean sendToServer(List<AgentData> agentDataList) {
        Map<Integer, List<AgentData>> typeAgentData = agentDataList.stream().collect(
                Collectors.groupingBy(AgentData::getType));

        Set<Map.Entry<Integer, List<AgentData>>> set = typeAgentData.entrySet();
        Iterator<Map.Entry<Integer, List<AgentData>>> it = set.iterator();

        while (it.hasNext()) {
            Map.Entry<Integer, List<AgentData>> dataEntry = it.next();
            if (!doSend(dataEntry.getKey(), dataEntry.getValue())) {
                return false;
            }
        }
        return true;
    }

    private boolean doSend(Integer type, List<AgentData> agentData) {
        HttpWebResponse response = null;
        try {
            String json = JsonUtil.toJsonStr(agentData);
            HashMap<String, String> params = new HashMap<>();
            params.put("type", String.valueOf(type));
            params.put("messages", json);
            params.put("r", DateUtil.getNowTimestamp() + "");

            HashMap<String, String> headers = new HashMap<String, String>();

            HttpWebRequest request = new HttpWebRequest(requestInfo.getServerUrl(), "POST");
            request.setHeaders(headers);
            request.setParams(params);

            response = httpWebClient.getWebContent(request);
            Integer statusCode = response.getStatusCode();
            if (statusCode.equals(HttpStatus.SC_OK)) {
                return true;
            }
            return false;
        } catch (Throwable t) {
            t.printStackTrace();
            return false;
        } finally {
            if (response != null) {
                response.close();
            }
        }
    }
}
