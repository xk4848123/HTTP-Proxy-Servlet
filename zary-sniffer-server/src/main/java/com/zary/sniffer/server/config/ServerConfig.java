package com.zary.sniffer.server.config;

import com.zary.sniffer.server.mysql.DB_MYSQL;
import com.zary.sniffer.server.utils.RedisClientPoolUtil;
import com.zx.lib.elasticsearch.EsRestClientUtil;
import com.zx.lib.utils.PropertiesUtil;
import com.zx.lib.utils.StringUtil;
import lombok.Data;

import java.io.IOException;
import java.util.Properties;

@Data
public class ServerConfig {
    /**
     * 系统运行配置
     */
    int es_write_size = 500;
    int es_write_interval = 30;

    int threadPool_consumer_size = 1;
    String threadPool_scheduler_size = "10";

    String es_index_adm_request;
    String es_index_adm_operate;
    String es_index_adm_span;
    String es_index_adm_sqlParse;
    /**
     * jetty配置
     */
    String jetty_server_url = "localhost";
    int jetty_server_port = 8080;
    String jetty_server_target = "pulsar";

    /**
     * quartz配置
     */
    boolean quartz_deedAnalyse_open = false;
    String quartz_deedAnalyse_cron = "";
    int quartz_deedAnalyse_intervalTime = 30;
    boolean quartz_grammarAnalyse_open = false;
    String quartz_grammarAnalyse_cron = "";
    int quartz_grammarAnalyse_intervalTime = 1;
    /**
     * db配置
     */
    String db_jdbcUrl = "";
    String db_driver = "";
    String db_username = "";
    String db_password = "";
    /**
     * 消息队列配置
     */
    String pulsar_serviceUrl = "pulsar://127.0.0.1:6650";
    String pulsar_topic;
    /**
     * 索引配置项
     */
    String[] es_node_urls = null;
    int es_timeout_request = 1000;
    int es_timeout_connect = 1000;
    int es_timeout_socket = 30000;
    int es_max_connect = 100;
    int es_max_route = 100;
    int es_agg_size = 1000;
    int es_query_size = 10000;
    String es_pipeline_timestamp = "init_timestamp";
    /**
     * 缓存配置项
     */
    String redis_host;
    int redis_port = 6379;
    String redis_password;
    int redis_pool_maxactive = 20;
    int redis_pool_maxwait = 6000;
    int redis_pool_maxidle = 20;
    int redis_pool_minidle = 0;
    int redis_timeout = 5000;
    int redis_testonborrow = 1;
    int redis_dbindex = 0;

    /**
     * 正则过滤数据
     */
    String regex_filter;

    public ServerConfig(Properties properties) {
        initProperties(properties);
    }

    public ServerConfig(String propertiesPath) throws IOException {
        initProperties(propertiesPath);
    }

    /**
     * 根据配置文件初始化
     *
     * @param properties
     */
    private void initProperties(Properties properties) {
        if (properties == null) {
            return;
        }
        //运行时相关配置
        threadPool_consumer_size = PropertiesUtil.setPropInt(properties, threadPool_consumer_size, "threadPool.consumer.size");
        threadPool_scheduler_size = PropertiesUtil.setPropString(properties, threadPool_scheduler_size, "threadPool.scheduler.size");
        es_write_size = PropertiesUtil.setPropInt(properties, es_write_size, "es.write.size");
        es_write_interval = PropertiesUtil.setPropInt(properties, es_write_interval, "es.write.interval");
        es_index_adm_request = PropertiesUtil.setPropString(properties, es_index_adm_request, "es.index.adm.request");
        es_index_adm_operate = PropertiesUtil.setPropString(properties, es_index_adm_operate, "es.index.adm.operate");
        es_index_adm_span = PropertiesUtil.setPropString(properties, es_index_adm_span, "es.index.adm.span");
        es_index_adm_sqlParse = PropertiesUtil.setPropString(properties, es_index_adm_sqlParse, "es.index.adm.sqlParse");
        // jetty配置
        jetty_server_url = PropertiesUtil.setPropString(properties, jetty_server_url, "jetty.server.url");
        jetty_server_port = PropertiesUtil.setPropInt(properties, jetty_server_port, "jetty.server.port");
        jetty_server_target = PropertiesUtil.setPropString(properties, jetty_server_target, "jetty.server.target");
        // quartz配置
        String tmpStr = "";
        tmpStr = PropertiesUtil.setPropString(properties, tmpStr, "quartz.deedAnalyse.open");
        quartz_deedAnalyse_open = Boolean.parseBoolean(tmpStr);
        quartz_deedAnalyse_cron = PropertiesUtil.setPropString(properties, quartz_deedAnalyse_cron, "quartz.deedAnalyse.cron");
        quartz_deedAnalyse_intervalTime = PropertiesUtil.setPropInt(properties, quartz_deedAnalyse_intervalTime, "quartz.deedAnalyse.intervalTime");
        tmpStr = PropertiesUtil.setPropString(properties, tmpStr, "quartz.grammarAnalyse.open");
        quartz_grammarAnalyse_open = Boolean.parseBoolean(tmpStr);
        quartz_grammarAnalyse_cron = PropertiesUtil.setPropString(properties, quartz_grammarAnalyse_cron, "quartz.grammarAnalyse.cron");
        quartz_grammarAnalyse_intervalTime = PropertiesUtil.setPropInt(properties, quartz_grammarAnalyse_intervalTime, "quartz.grammarAnalyse.intervalTime");
        // 消息队列配置
        pulsar_serviceUrl = PropertiesUtil.setPropString(properties, pulsar_serviceUrl, "pulsar.serviceUrl");
        pulsar_topic = PropertiesUtil.setPropString(properties, pulsar_topic, "pulsar.topic");
        //es相关
        String es_urls = null;
        es_urls = PropertiesUtil.setPropString(properties, es_urls, "es.node.urls");
        if (!StringUtil.isEmpty(es_urls)) {
            es_node_urls = es_urls.split(",");
        }
        es_timeout_request = PropertiesUtil.setPropInt(properties, es_timeout_request, "es.timeout.request");
        es_timeout_connect = PropertiesUtil.setPropInt(properties, es_timeout_connect, "es.timeout.connect");
        es_timeout_socket = PropertiesUtil.setPropInt(properties, es_timeout_socket, "es.timeout.socket");
        es_max_connect = PropertiesUtil.setPropInt(properties, es_max_connect, "es.max.connect");
        es_max_route = PropertiesUtil.setPropInt(properties, es_max_route, "es.max.route");
        es_agg_size = PropertiesUtil.setPropInt(properties, es_agg_size, "es.agg.size");
        es_query_size = PropertiesUtil.setPropInt(properties, es_query_size, "es.query.size");
        //redis相关
        redis_host = PropertiesUtil.setPropString(properties, redis_host, "redis.host");
        redis_port = PropertiesUtil.setPropInt(properties, redis_port, "redis.port");
        redis_password = PropertiesUtil.setPropString(properties, redis_password, "redis.password");
        redis_pool_maxactive = PropertiesUtil.setPropInt(properties, redis_pool_maxactive, "redis.pool.maxactive");
        redis_pool_maxwait = PropertiesUtil.setPropInt(properties, redis_pool_maxwait, "redis.pool.maxwait");
        redis_pool_maxidle = PropertiesUtil.setPropInt(properties, redis_pool_maxidle, "redis.pool.maxidle");
        redis_pool_minidle = PropertiesUtil.setPropInt(properties, redis_pool_minidle, "redis.pool.minidle");
        redis_timeout = PropertiesUtil.setPropInt(properties, redis_timeout, "redis.timeout");
        redis_testonborrow = PropertiesUtil.setPropInt(properties, redis_testonborrow, "redis.testonborrow");
        redis_dbindex = PropertiesUtil.setPropInt(properties, redis_dbindex, "redis.dbindex");

        //db相关
        db_driver = PropertiesUtil.setPropString(properties, db_driver, "db.driver");
        db_jdbcUrl = PropertiesUtil.setPropString(properties, db_jdbcUrl, "db.jdbcUrl");
        db_username = PropertiesUtil.setPropString(properties, db_username, "db.username");
        db_password = PropertiesUtil.setPropString(properties, db_password, "db.password");
        //正则表达式
        regex_filter = PropertiesUtil.setPropString(properties, regex_filter, "regex.filter");
    }

    /**
     * 初始化数据库
     */
    public void initDB() {
        DB_MYSQL.mysql.init(
                db_jdbcUrl,
                db_username,
                db_password,
                db_driver
        );
    }

    /**
     * 初始化ES连接
     */
    public void initElasticsearch() {
        EsRestClientUtil.init(
                es_node_urls,
                es_timeout_request,
                es_timeout_connect,
                es_timeout_socket,
                es_max_connect,
                es_max_route
        );
    }

    /**
     * 初始化Redis链接
     */
    public void initRedis() {
        RedisClientPoolUtil.init(
                redis_host,
                redis_port,
                ((StringUtil.isEmpty(redis_password) || redis_password.equalsIgnoreCase("null")) ? (String) null : redis_password),
                redis_pool_maxactive,
                redis_pool_maxwait,
                redis_pool_maxidle,
                redis_pool_minidle,
                redis_timeout,
                redis_testonborrow > 0, redis_dbindex);
    }

    /**
     * 按照配置文件路径初始化
     *
     * @param propertiesName
     */
    private void initProperties(String propertiesName) throws IOException {
        Properties prop = PropertiesUtil.getPropertiesByPath(propertiesName);
        initProperties(prop);
    }
}
