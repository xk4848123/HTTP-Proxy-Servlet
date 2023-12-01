package com.zary.sniffer.server;

import com.zary.sniffer.server.config.ServerConfig;
import com.zary.sniffer.server.mysql.MySqlUtil;
import com.zary.sniffer.server.scheduler.AuthInspect;
import com.zary.sniffer.server.scheduler.DeedAnalyse;
import com.zary.sniffer.server.scheduler.GrammarAnalyse;
import com.zary.sniffer.server.utils.RedisCacheUtil;
import com.zary.sniffer.server.handle.HandleManager;
import com.zx.lib.utils.LogUtil;
import com.zx.lib.utils.PropertiesUtil;
import com.zx.lib.utils.StringUtil;
import org.quartz.*;
import org.quartz.impl.StdSchedulerFactory;
import org.slf4j.Logger;

import java.util.Properties;
import java.util.UUID;


/**
 * @author wanght
 * @date 2020-01-26 15:00:00
 */
public class ServerMain {
    /**
     * 日志对象
     */
    static Logger logger = LogUtil.getLogger(ServerMain.class);

    /**
     * 全局变量保存授权状态
     */
    public static boolean AUTH_STATUS = false;
    /**
     * 全局变量是否结束进程
     */
    public static boolean ifStop = false;
    /**
     * 配置文件参数
     */
    static ServerConfig config;
    /**
     * 定时任务对象
     */
    static Scheduler SCHEDULER;

    /**
     * 数据处理入口
     *
     * @param args
     */
    public static void main(String[] args) {
        try {
            if (initConfig()) {
                createQuartzJob();
                startMessageHandler();
            }
        } catch (Throwable t) {
            logger.error("main thread error:", t);
        }
    }

    private static void startMessageHandler() {
        DefaultServerStarter defaultServerStarter = new DefaultServerStarter();
        HandleManager handleManager = new HandleManager();
        MessageHandler messageHandler = new MessageHandler(config);
        handleManager.addTypeOneHandler(messageHandler::handle);
        defaultServerStarter.start(handleManager, () -> AUTH_STATUS, (token) -> StringUtil.isEmpty(token) || !RedisCacheUtil.isExistNodeToken(token),
                config.getThreadPool_consumer_size(), config.getPulsar_serviceUrl(),
                config.getPulsar_topic(), config.getJetty_server_target(), config.getJetty_server_port());
    }

    /**
     * 初始化配置文件
     */
    private static boolean initConfig() {
        try {
            Properties prop;
            String configPath = System.getProperty("config");
            if (StringUtil.isEmpty(configPath)) {
                prop = PropertiesUtil.getProperties("conf.properties");
            } else {
                prop = PropertiesUtil.getPropertiesByPath(configPath);
            }
            config = new ServerConfig(prop);
            // 初始化数据库
            config.initDB();
            // 初始化ES
            config.initElasticsearch();
            // 初始化redis
            config.initRedis();
            // 判断redis链接是否成功
            if (!RedisCacheUtil.checkRedis()) {
                return false;
            }
            // 判断判断数据库是否链接成功
            if (!MySqlUtil.checkDataBaseConnect()) {
                return false;
            }
            return true;
        } catch (Exception e) {
            logger.error("初始化配置异常.", e);
        }
        return false;
    }

    /**
     * 创建Quartz的计划任务
     */
    private static void createQuartzJob() throws SchedulerException {
        try {
            // 系统重启时执行一次授权检查
            AuthInspect inspect = new AuthInspect();
            inspect.executeNoJob(config);

            StdSchedulerFactory factory = getCustomFactory();
            SCHEDULER = factory.getScheduler();

            // 计划任务-授权检查(每分钟检查一次,内置计划任务)
            addJob(AuthInspect.class, "0 0/1 * * * ?", Scheduler.DEFAULT_GROUP);
            if (config.isQuartz_deedAnalyse_open()) {
                // 计划任务-行为分析
                addJob(DeedAnalyse.class, config.getQuartz_deedAnalyse_cron(), Scheduler.DEFAULT_GROUP);
            }
            if (config.isQuartz_grammarAnalyse_open()) {
                // 计划任务-语法分析
                addJob(GrammarAnalyse.class, config.getQuartz_grammarAnalyse_cron(), Scheduler.DEFAULT_GROUP);
            }
            // 开启
            SCHEDULER.start();
        } catch (Exception e) {
            throw new RuntimeException("创建计划任务异常");
        }
    }

    /**
     * 自定义StdSchedulerFactory
     *
     * @return
     */
    private static StdSchedulerFactory getCustomFactory() {
        StdSchedulerFactory factory = new StdSchedulerFactory();
        try {
            Properties props = new Properties();
            // 线程池定义
            props.put(StdSchedulerFactory.PROP_THREAD_POOL_CLASS, "org.quartz.simpl.SimpleThreadPool");
            // 设置Scheduler的线程数
            props.put("org.quartz.threadPool.threadCount", config.getThreadPool_scheduler_size());
            factory.initialize(props);
        } catch (Exception e) {
            logger.error("自定义StdSchedulerFactory异常", e);
        }
        return factory;
    }

    /**
     * 添加计划任务
     *
     * @param admJobClass
     * @param cronSchedule
     */
    private static void addJob(Class admJobClass, String cronSchedule, String group) throws SchedulerException {
        UUID uuid = UUID.randomUUID();
        JobDataMap jdm = new JobDataMap();
        jdm.put("config", config);
        JobDetail job = JobBuilder.newJob(admJobClass).withIdentity(uuid.toString(), group).setJobData(jdm).build();
        CronTrigger trigger = TriggerBuilder.newTrigger().withIdentity(uuid.toString(), group).withSchedule(CronScheduleBuilder.cronSchedule(cronSchedule)).forJob(job.getKey()).build();
        SCHEDULER.scheduleJob(job, trigger);
    }


}
