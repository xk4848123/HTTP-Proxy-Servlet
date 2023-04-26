package com.zary.sniffer.agent.core.log;

import com.zary.sniffer.util.DateUtil;
import com.zary.sniffer.util.DirectoryUtil;
import com.zary.sniffer.util.FileUtil;
import com.zary.sniffer.util.StringUtil;

import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Date;

/**
 * 日志消息写文件类: 通过全局FileOutputStream写入
 * 注意本项目日志处理时disruptor是单消费模式，不存在并发，所以代码没有任何加锁，节省性能！！
 *
 * @author xulibo
 */
public class LogWriter {
    /**
     * 日志文件名称头
     */
    private static final String logFileHead = "adm-agent-log-";
    /**
     * 当前日志文件
     */
    private static String logFileCurrent = "";
    /**
     * 单个日志文件大小上限(K)
     */
    private static int logFileMaxSize = 5120;
    /**
     * 日志根目录
     */
    private static String logDir = "";
    /**
     * 日志输出流
     */
    private static FileOutputStream outputStream = null;

    /**
     * 初始化
     */
    public static void init(String dir, int fileMaxSize) {
        logDir = dir;
        logFileMaxSize = fileMaxSize;
    }

    /**
     * 创建新日志文件
     */
    private static void createLogFile() throws Exception {
        //先清空并释放当前写入的文件流
        flushAndDispose();
        //初始化日志目录，防止不存在
        if (!DirectoryUtil.isExsit(logDir)) {
            DirectoryUtil.create(logDir);
        }
        //创建新日志文件(时间戳文件名，3次防重) adm-agent-log-20190601-080808.log
        String newFileName = "";
        for (int i = 0; i < 3; i++) {
            newFileName = String.format("%s/%s%s.log",
                    logDir,
                    logFileHead,
                    DateUtil.toDateString(new Date(), "yyyyMMdd-HHmmssSSS"));
            if (!FileUtil.isExsit(newFileName)) {
                break;
            }
        }
        //替换当前文件流
        try {
            logFileCurrent = newFileName;
            outputStream = new FileOutputStream(logFileCurrent, true);
        } catch (Exception e) {
            throw new Exception("adm log writer::output stream failed.", e);
        }
    }

    /**
     * 文件检查、重建
     *
     * @throws IOException
     */
    private static void checkCurrentFile() throws Exception {
        if (StringUtil.isEmpty(logFileCurrent)) {
            createLogFile();
        } else {
            //byte length
            long fileSize = FileUtil.getSize(logFileCurrent);
            if (fileSize >= (logFileMaxSize * 1024)) {
                createLogFile();
            }
        }
    }

    /**
     * 写日志
     *
     * @param event
     * @throws IOException
     */
    public static void writelog(LogEvent event) throws Exception {
        String eventString = event.toString();
        //检查文件
        checkCurrentFile();
        //写入文件
        outputStream.write(eventString.getBytes(Charset.forName("UTF-8")));
        outputStream.flush();
        System.out.println(eventString);
    }

    /**
     * 先清空再释放文件流
     */
    public synchronized static void flushAndDispose() {
        try {
            if (outputStream != null) {
                outputStream.flush();
                outputStream.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
