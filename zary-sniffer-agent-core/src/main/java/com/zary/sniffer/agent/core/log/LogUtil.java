package com.zary.sniffer.agent.core.log;

import com.lmax.disruptor.BlockingWaitStrategy;
import com.lmax.disruptor.RingBuffer;
import com.lmax.disruptor.WaitStrategy;
import com.lmax.disruptor.dsl.Disruptor;
import com.lmax.disruptor.dsl.ProducerType;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 日志系统辅助类
 */
public class LogUtil {
    /**
     * 环形队列长度
     */
    private static final int buffer_size = 1024 * 8;
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
    private static Disruptor<LogEvent> disruptor = null;
    /**
     * 全局唯一默认日志producer，用于不需要实例化多个生产者时快速输出日志
     */
    private static LogProducer defaultProducer = null;

    private static volatile boolean isStart = false;


    private static ThreadFactory getDisruptorThreadFactory() {
        return new ThreadFactory() {
            private final AtomicInteger index = new AtomicInteger(1);

            @Override
            public Thread newThread(Runnable r) {
                return new Thread((ThreadGroup) null, r, "admx-log-thread-" + index.getAndIncrement());
            }
        };
    }

    /**
     * 日志系统初始化，程序启动时调用
     */
    public synchronized static void start(String dir, int fileMaxSize, LogLevel levelStart) {
        if (!isStart) {
            LogWriter.init(dir, fileMaxSize);
            ThreadFactory threadFactory = getDisruptorThreadFactory();
            LogEventFactory eventFactory = new LogEventFactory();
            disruptor = new Disruptor<LogEvent>(eventFactory, buffer_size, threadFactory, ProducerType.MULTI, waitStrategy);
            disruptor.handleEventsWith(new LogEventHandler());
            disruptor.start();

            RingBuffer<LogEvent> ringBuffer = disruptor.getRingBuffer();
            defaultProducer = new LogProducer(ringBuffer, levelStart);
            isStart = true;
        }
    }

    /**
     * 实例化一个生产者
     */
    private static LogProducer getLogProducer() {
        return defaultProducer;
    }

    public static void shutDown() {
        try {
            if (disruptor != null) {
                disruptor.shutdown();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public static void debug(String title, String message) {
        try {
            getLogProducer().debug(title, message);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void debug(String title, Throwable e) {
        getLogProducer().debug(title, e);
    }

    public static void info(String title, String message) {
        try {
            getLogProducer().info(title, message);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void info(String title, Throwable e) {
        getLogProducer().info(title, e);
    }

    public static void warn(String title, String message) {
        try {
            getLogProducer().warn(title, message);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void warn(String title, Throwable e) {
        getLogProducer().warn(title, e);
    }

    public static void error(String title, String message) {
        try {
            getLogProducer().error(title, message);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void error(String title, Throwable e) {
        getLogProducer().error(title, e);
    }


}
