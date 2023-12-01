package com.zary.sniffer.transfer;

import com.lmax.disruptor.BlockingWaitStrategy;
import com.lmax.disruptor.RingBuffer;
import com.lmax.disruptor.WaitStrategy;
import com.lmax.disruptor.dsl.Disruptor;
import com.lmax.disruptor.dsl.ProducerType;
import com.zary.sniffer.transfer.http.HttpWebClient;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 线程消息发送类
 * 注意处理时disruptor是单消费模式，不存在并发，所以代码没有任何加锁，节省性能！！
 */
public class TransferDataUtil {
    /**
     * 环形队列长度
     */
    private static final int buffer_size = 1024;
    /**
     * 消费线程阻塞策略
     * //BlockingWaitStrategy 使用锁和条件变量, 效率较低, 但CPU的消耗最小, 在不同部署环境下性能表现比较一致
     * //SleepingWaitStrategy 多次循环尝试不成功后, 让出CPU, 等待下次调度; 多次调度后仍不成功, 睡眠纳秒级别的时间再尝试. 平衡了延迟和CPU资源占用, 但延迟不均匀.
     * //YieldingWaitStrategy 多次循环尝试不成功后, 让出CPU, 等待下次调度. 平衡了延迟和CPU资源占用, 延迟也比较均匀.
     * //BusySpinWaitStrategy 自旋等待，类似自旋锁. 低延迟但同时对CPU资源的占用也多.
     */
    private static final WaitStrategy waitStrategy = new BlockingWaitStrategy();
    /**
     * 全局disruptor对象
     */
    private static Disruptor<TransferData> disruptor = null;

    private static TransferDataProducer defaultProducer = null;

    private static volatile boolean isStart = false;

    private static ThreadFactory getDisruptorThreadFactory() {
        return new ThreadFactory() {
            private final AtomicInteger index = new AtomicInteger(1);

            @Override
            public Thread newThread(Runnable r) {
                return new Thread((ThreadGroup) null, r, "sniffer-data-thread-" + index.getAndIncrement());
            }
        };
    }

    /**
     * 日志系统初始化，程序启动时调用
     */
    public synchronized static void start(String address, String token) {
        if (!isStart) {
            try {
                ThreadFactory threadFactory = getDisruptorThreadFactory();
                TransferDataEventFactory eventFactory = new TransferDataEventFactory();

                HttpWebClient httpWebClient = new HttpWebClient();
                httpWebClient.trustServerCert();
                httpWebClient.setTimeOut(30);
                httpWebClient.setPoolingManager(200);
                httpWebClient.setRequestRetryHandler();

                disruptor = new Disruptor<>(eventFactory, buffer_size, threadFactory, ProducerType.MULTI, waitStrategy);
                disruptor.handleEventsWith(new TransferDataEventHandler(address, token, httpWebClient));
                disruptor.start();

                RingBuffer<TransferData> ringBuffer = disruptor.getRingBuffer();
                defaultProducer = new TransferDataProducer(ringBuffer);

                isStart = true;
            } catch (Throwable t) {
                t.printStackTrace();
            }
        }
    }


    public static void sendData(TransferData transferData) {
        defaultProducer.sendEvent(transferData);
    }


}
