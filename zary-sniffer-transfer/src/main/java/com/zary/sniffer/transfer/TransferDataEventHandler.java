package com.zary.sniffer.transfer;

import com.lmax.disruptor.EventHandler;
import com.zary.sniffer.transfer.http.HttpWebClient;
import com.zary.sniffer.transfer.http.HttpWebRequest;
import com.zary.sniffer.transfer.http.HttpWebResponse;
import com.zary.sniffer.util.DateUtil;
import org.apache.http.HttpStatus;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 数据消费者(单消费者) disruptor
 * 批量发送数据到服务端
 */
public class TransferDataEventHandler implements EventHandler<TransferData> {

    private final String address;

    private final String token;

    private final HttpWebClient httpWebClient;

    /**
     * 临时数据缓存buffer
     */
    private final List<TransferData> transferDataCache = new ArrayList<>();
    /**
     * 临时数据缓存大小，数据积累发送
     */
    private static final int CACHE_BULK_SIZE = 2;

    private static final long MAX_TIME_INTERVAL = 5000;

    private static final long LIMIT_CACHE_BULK_SIZE = 1000;

    private long lastSendTimeStamp = DateUtil.getNowTimestamp();

    private Thread checkAndSendThread = new Thread(() -> {
        while (true) {
            synchronized (this) {
                int cacheSize = transferDataCache.size();
                if (cacheSize > 0 && DateUtil.getNowTimestamp() - lastSendTimeStamp > MAX_TIME_INTERVAL) {
                    boolean success = sendToServer(transferDataCache);
                    if (success) {
                        lastSendTimeStamp = DateUtil.getNowTimestamp();
                        transferDataCache.clear();
                    }
                }
            }
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    });

    public TransferDataEventHandler(String address, String token, HttpWebClient httpWebClient) {
        this.address = address;
        this.token = token;
        this.httpWebClient = httpWebClient;
        this.checkAndSendThread.start();
    }

    @Override
    public synchronized void onEvent(TransferData event, long sequence, boolean endOfBatch) {
        try {
            transferDataCache.add(event);

            int cacheSize = transferDataCache.size();
            long timeDifference = DateUtil.getNowTimestamp() - lastSendTimeStamp;

            if (cacheSize >= CACHE_BULK_SIZE || timeDifference > MAX_TIME_INTERVAL) {
                boolean success = sendToServer(transferDataCache);
                if (success) {
                    lastSendTimeStamp = DateUtil.getNowTimestamp();
                    transferDataCache.clear();
                } else {
                    if (cacheSize > LIMIT_CACHE_BULK_SIZE) {
                        transferDataCache.clear();
                    }
                }
            }
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }

    /**
     * 发送缓存
     */
    private boolean sendToServer(List<TransferData> transferDataList) {
        Map<Integer, List<TransferData>> typeAgentData = transferDataList.stream().collect(Collectors.groupingBy(TransferData::getType));

        Set<Map.Entry<Integer, List<TransferData>>> set = typeAgentData.entrySet();
        Iterator<Map.Entry<Integer, List<TransferData>>> it = set.iterator();

        while (it.hasNext()) {
            Map.Entry<Integer, List<TransferData>> dataEntry = it.next();
            if (!doSend(dataEntry.getKey(), dataEntry.getValue())) {
                return false;
            }
        }
        return true;
    }

    private boolean doSend(Integer type, List<TransferData> transferDatas) {
        HttpWebResponse response = null;
        try {
            String messages = transferDatas.stream().map(transferData -> transferData.getData()).collect(Collectors.joining(","));
            HashMap<String, String> params = new HashMap<>();
            params.put("type", String.valueOf(type));
            params.put("messages", messages);
            params.put("r", DateUtil.getNowTimestamp() + "");

            HashMap<String, String> headers = new HashMap<String, String>();
            headers.put("token", this.token);
            HttpWebRequest request = new HttpWebRequest(address, "POST");
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
