package com.zary.sniffer.server;

import com.zary.SqlParseUtil;
import com.zary.sniffer.server.config.ServerConfig;

import com.zary.sniffer.server.utils.BeanUtil;
import com.zary.sniffer.server.utils.RedisCacheUtil;
import com.zary.sniffer.server.utils.SqlTypeUtils;
import com.zary.sniffer.core.enums.EnumBlackWhiteType;
import com.zary.sniffer.core.enums.PluginType;
import com.zary.sniffer.core.enums.ThreatPromptType;
import com.zary.sniffer.core.model.SpanInfo;
import com.zary.sniffer.core.model.ThreadDataInfo;
import com.zary.sniffer.core.model.ext.DataOperateInfoExt;
import com.zary.sniffer.core.model.ext.SqlParseResultInfoExt;
import com.zary.sniffer.core.model.ext.ThreadDataInfoExt;
import com.zary.sniffer.core.model.ext.WebRequestInfoExt;
import com.zary.sniffer.transfer.wrapper.ThreadDataDeserializer;
import com.zary.sniffer.util.Md5Util;
import com.zary.util.JdbcConstants;
import com.zx.lib.elasticsearch.EsRestDocumentUtil;
import com.zx.lib.elasticsearch.EsRestIndexUtil;
import com.zx.lib.json.JsonUtil;
import com.zx.lib.utils.DateUtil;
import com.zx.lib.utils.LogUtil;
import com.zx.lib.utils.StringUtil;
import lombok.Data;
import org.slf4j.Logger;

import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author wanght
 * @date 2020-01-26 15:00:00
 */
@Data
public class MessageHandler {
    /**
     * 日志对象
     */
    static Logger logger = LogUtil.getLogger(MessageHandler.class);
    /**
     * 自定义指纹前缀
     */
    static final String CUSTOM_FINGER_PREFIX = "m-";
    /**
     * 正则-提取mysql链接字符串数据
     */
    static final Pattern PATTERN_MYSQL = Pattern.compile("(?i)mysql://(.+)?:(\\d+)?/");
    /**
     * 正则-提取oracle链接字符串数据
     */
    static final Pattern PATTERN_ORACLE = Pattern.compile("(?i)thin:@(.+)?:(\\d+)?:");
    /**
     * 正则-提取sql_server链接字符串数据
     */
    static final Pattern PATTERN_SQL_SERVER = Pattern.compile("(?i)sqlserver://(.+)?:(\\d+)?;");
    /**
     * 正则-提取db2链接字符串数据
     */
    static final Pattern PATTERN_DB2 = Pattern.compile("(?i)db2://(.+)?:(\\d+)?/");
    /**
     * 正则-提取sybase链接字符串数据
     */
    static final Pattern PATTERN_SYBASE = Pattern.compile("(?i)Tds:(.+)?:(\\d+)?/");

    /**
     * 线程锁
     */
    private static Lock lock = new ReentrantLock();
    /**
     * 配置参数
     */
    private ServerConfig config;

    private List<ThreadDataInfoExt> listData;

    private ConcurrentLinkedQueue<ThreadDataInfoExt> threadDataQueue;

    private ThreadDataDeserializer threadDataDeserializer;

    private long lastSendTimeStamp;

    private Thread doSendThread;

    public MessageHandler(ServerConfig config) {
        this.config = config;
        listData = new ArrayList<>();
        threadDataDeserializer = new ThreadDataDeserializer();
        this.lastSendTimeStamp = System.currentTimeMillis();
        doSendThread = newSendDataThread(config);
        doSendThread.start();
    }

    private Thread newSendDataThread(ServerConfig config) {
        return new Thread(() -> {
            for (; ; ) {
                try {
                    ThreadDataInfoExt threadDataInfoExt = threadDataQueue.poll();
                    if (threadDataInfoExt == null && listData.size() == 0) {
                        Thread.sleep(5000);

                    }
                    if (threadDataInfoExt != null) {
                        listData.add(threadDataInfoExt);
                    }
                    long curTimeStamp = System.currentTimeMillis();
                    if (listData.size() >= config.getEs_write_size() || (curTimeStamp - lastSendTimeStamp) >= config.getEs_write_interval() * 1000) {
                        if (listData.size() > 0) {
                            String[] indexNames = getEsIndexName();
                            if (indexNames != null) {
                                insertListToEs(listData, indexNames);
                                listData.clear();
                            }
                        }
                        lastSendTimeStamp = System.currentTimeMillis();
                    }
                } catch (Throwable t) {
                    logger.error("send data to es error", t);
                }
            }
        });
    }

    public boolean handle(String message) {
        try {
            ThreadDataInfo threadDataInfo = threadDataDeserializer.doDeserialize(message);
            ThreadDataInfoExt threadDataInfoExt = BeanUtil.copyBean(ThreadDataInfoExt.class, threadDataInfo);

            if (StringUtil.isEmpty(threadDataInfoExt.getWebRequestInfo().getFingerprint())
                    && StringUtil.isNotEmpty(threadDataInfoExt.getWebRequestInfo().getReq_ip())) {
                String fingerPrint = CUSTOM_FINGER_PREFIX + Md5Util.getMd5(threadDataInfoExt.getWebRequestInfo().getReq_ip());
                threadDataInfoExt.getWebRequestInfo().setFingerprint(fingerPrint);
                for (DataOperateInfoExt itemExt : threadDataInfoExt.getDataInfos()) {
                    itemExt.setFingerprint(fingerPrint);
                }
            }
            threadDataQueue.offer(dataHandle(threadDataInfoExt));
            return true;
        } catch (Throwable t) {
            logger.error("data handle error:", t);
            return true;
        }
    }

    /**
     * 判断当日索引是否创建
     *
     * @return
     */
    private String[] getEsIndexName() {
        lock.lock();
        try {
            String curDate = DateUtil.toDateString(new Date(), "yyyyMMdd");
            String curRequestIndexName = config.getEs_index_adm_request().replace("*", "") + curDate;
            String request = "request";
            if (!checkEsIndex(curRequestIndexName, request)) {
                return null;
            }
            String curOperateIndexName = config.getEs_index_adm_operate().replace("*", "") + curDate;
            String operate = "operate";
            if (!checkEsIndex(curOperateIndexName, operate)) {
                return null;
            }
            String curSpanIndexName = config.getEs_index_adm_span().replace("*", "") + curDate;
            /*if (!checkEsIndex(curSpanIndexName, "span")) {
                return null;
            }*/
            String sqlParse = "sqlParse";
            if (!checkEsIndex(config.getEs_index_adm_sqlParse(), sqlParse)) {
                return null;
            }
            String[] indexNames = new String[]{curRequestIndexName, curOperateIndexName, curSpanIndexName};

            return indexNames;
        } catch (Exception e) {
            logger.error("创建新索引异常", e);
        } finally {
            lock.unlock();
        }
        return null;
    }

    /**
     * 数据处理
     *
     * @param oldDataInfo
     */
    private ThreadDataInfoExt dataHandle(ThreadDataInfoExt oldDataInfo) {
        ThreadDataInfoExt newDataInfo = new ThreadDataInfoExt();
        WebRequestInfoExt oldRequestInfo = oldDataInfo.getWebRequestInfo();
        // 处理请求产生的数据操作sql
        if (oldDataInfo.getDataInfos().size() > 0) {
            List<SqlParseResultInfoExt> sqlParseResultInfos = new ArrayList<>();
            List<DataOperateInfoExt> operateInfos = new ArrayList<>();
            for (DataOperateInfoExt itemExt : oldDataInfo.getDataInfos()) {
                // 获取数据库类型
                String dbType = getDataBaseType(itemExt.getPluginType());
                if (StringUtil.isNotEmpty(dbType)) {
                    // 执行正则判断是否丢弃数据
                    if (ifDiscard(itemExt.getSql())) {
                        continue;
                    }
                    // 对sql进行语法解析
                    String parameterizedSql = SqlParseUtil.parameterize(itemExt.getSql(), dbType);
                    SqlParseResultInfoExt model = sqlParse(dbType, itemExt, parameterizedSql);

                    itemExt.setSql_hash_analy(model.getHash());
                    itemExt.setSql_parameterize(parameterizedSql);
                    itemExt.setReq_ip(oldRequestInfo.getReq_ip());
                    itemExt.setSql_types(SqlTypeUtils.getSqlTypes(model.getOperates()));
                    // 执行账号匹配规则
                    accountMatching(oldRequestInfo, model, dbType, itemExt);
                    // 解析数据库信息
                    analysisDBInfo(itemExt, dbType);
                    // 判断s如果参数化的sql hash不存在，则需入ES
                    if (!RedisCacheUtil.checkSqlHashList(model.getHash())) {
                        model.setParseId(UUID.randomUUID().toString().replaceAll("-", ""));
                        sqlParseResultInfos.add(model);
                    }
                    // 判断是否为白名单
                    int isWhite = checkWhiteList(itemExt);
                    if (isWhite > 0) {
                        itemExt.setWhite_type(isWhite);
                    } else if (isWhite == 0) {
                        // 进行语法分析
                        grammarAnalyse(model, dbType, itemExt);
                    }
                }
                operateInfos.add(itemExt);
            }
            newDataInfo.setSqlParseResultInfos(sqlParseResultInfos);
            newDataInfo.setDataInfos(operateInfos);
        }
        newDataInfo.setWebRequestInfo(oldRequestInfo);
        return newDataInfo;
    }

    /**
     * 获取数据库类型
     *
     * @return
     */
    private String getDataBaseType(PluginType pluginType) {
        String dbType = "";
        switch (pluginType) {
            case mysql:
                dbType = JdbcConstants.MYSQL;
                break;
            case oracle:
                dbType = JdbcConstants.ORACLE;
                break;
            case sqlserver:
                dbType = JdbcConstants.SQL_SERVER;
                break;
            case db2:
                dbType = JdbcConstants.DB2;
                break;
            default:
                break;
        }
        return dbType;
    }

    /**
     * sql语法分析
     *
     * @param dbType
     * @param itemExt
     */
    private SqlParseResultInfoExt sqlParse(String dbType, DataOperateInfoExt itemExt, String parameterizedSql) {
        String hash = Md5Util.getMd5(parameterizedSql);
        String druidResult = SqlParseUtil.check(itemExt.getSql(), dbType);
        SqlParseResultInfoExt model = JsonUtil.fromJson(SqlParseResultInfoExt.class, druidResult);
        model.setHash(hash);
        return model;
    }

    /**
     * 账号匹配
     *
     * @param oldRequestInfo
     * @param model
     * @param dbType
     * @param itemExt
     */
    private void accountMatching(WebRequestInfoExt oldRequestInfo, SqlParseResultInfoExt model, String dbType, DataOperateInfoExt itemExt) {
        List<String[]> rules = getAccountMatchRule(oldRequestInfo.getAppId());
        for (String[] rule : rules) {
            if (StringUtil.isNotEmpty(oldRequestInfo.getReq_url())) {
                if (rule[0].equals(oldRequestInfo.getReq_url())
                        || rule[0].contains(oldRequestInfo.getReq_url())
                        || oldRequestInfo.getReq_url().contains(rule[0])) {
                    if (model.getTable_operates() != null
                            && model.getTable_operates().contains(rule[1].toUpperCase() + "=SELECT")) {
                        String auth_id = getColumnValue(itemExt.getSql(), dbType, rule[1], rule[2]);
                        if (StringUtil.isNotEmpty(auth_id)) {
                            itemExt.setLogin_operate(true);
                            itemExt.setAuth_id(auth_id);
                            oldRequestInfo.setAuth_id(auth_id);
                            if (itemExt.getRes_lines() > 0) {
                                if (StringUtil.isNotEmpty(oldRequestInfo.getSession_id())) {
                                    RedisCacheUtil.addAccount(oldRequestInfo.getSession_id() + "_" + oldRequestInfo.getAppId(), auth_id, 86400);
                                } else if (StringUtil.isNotEmpty(oldRequestInfo.getReq_ip())) {
                                    RedisCacheUtil.addAccount(oldRequestInfo.getReq_ip() + "_" + oldRequestInfo.getAppId(), auth_id, 86400);
                                }
                            }
                            break;
                        }
                    }
                }
            }
        }

        if (StringUtil.isEmpty(itemExt.getAuth_id())) {
            if (StringUtil.isNotEmpty(oldRequestInfo.getSession_id())) {
                itemExt.setAuth_id(RedisCacheUtil.getAccount(oldRequestInfo.getSession_id() + "_" + oldRequestInfo.getAppId()));
            } else if (StringUtil.isNotEmpty(oldRequestInfo.getReq_ip())) {
                itemExt.setAuth_id(RedisCacheUtil.getAccount(oldRequestInfo.getReq_ip() + "_" + oldRequestInfo.getAppId()));
            }
        }
    }

    /**
     * 解析数据库信息
     *
     * @param itemExt
     * @param dbType
     */
    private void analysisDBInfo(DataOperateInfoExt itemExt, String dbType) {
        String[] jdbcInfo = getJdbcInfo(itemExt.getDataSource(), dbType);
        if (null != jdbcInfo) {
            itemExt.setJdbc_ip(jdbcInfo[0]);
            itemExt.setJdbc_port(Integer.parseInt(jdbcInfo[1]));
            itemExt.setJdbc_databaseName(jdbcInfo[2]);
        }
    }

    /**
     * 语法分析
     *
     * @param model
     * @param dbType
     * @param itemExt
     */
    private void grammarAnalyse(SqlParseResultInfoExt model, String dbType, DataOperateInfoExt itemExt) {
        if (model.getViolations() != null && model.getViolations().size() > 0) {
            if (model.isSyntaxError()) {
                if (!itemExt.isRes_status()) {
                    itemExt.setThreat_type(ThreatPromptType.语法错误.getValue());
                }
            } else {
                itemExt.setThreat_type(ThreatPromptType.SQL注入.getValue());
            }
        }
        if (itemExt.getThreat_type() == 0 && model.getOperates() != null) {
            for (String operate : model.getOperates()) {
                if (RedisCacheUtil.isExistRuleViolation(operate.toUpperCase())) {
                    itemExt.setThreat_type(ThreatPromptType.违规操作.getValue());
                    break;
                }
            }
        }
        if (itemExt.getThreat_type() == 0 && model.getFunctions() != null) {
            for (String function : model.getFunctions()) {
                if (RedisCacheUtil.isExistRuleViolation(function.toUpperCase() + "-" + dbType.toUpperCase())) {
                    itemExt.setThreat_type(ThreatPromptType.违规操作.getValue());
                    break;
                }
            }
        }
    }

    /**
     * 检查黑白名单 1:指纹白名单 2.ip白名单 3.sql白名单
     *
     * @return
     */
    private int checkWhiteList(DataOperateInfoExt model) {
        try {
            String result = RedisCacheUtil.getWhiteList(model.getFingerprint(), EnumBlackWhiteType.fingerprint);
            if (result != null) {
                if (checkAppsIsWhiteList(result, model.getAppId())) {
                    return EnumBlackWhiteType.fingerprint.getValue();
                }
            }
            result = RedisCacheUtil.getWhiteList(model.getReq_ip(), EnumBlackWhiteType.ip);
            if (result != null) {
                if (checkAppsIsWhiteList(result, model.getAppId())) {
                    return EnumBlackWhiteType.ip.getValue();
                }
            }
            result = RedisCacheUtil.getWhiteList(model.getSql_parameterize(), EnumBlackWhiteType.sql);
            if (result != null) {
                if (checkAppsIsWhiteList(result, model.getAppId())) {
                    return EnumBlackWhiteType.sql.getValue();
                }
            }
        } catch (Exception e) {
            logger.error("黑白名单检查异常", e.getMessage());
        }
        return 0;
    }

    /**
     * 检测appid是否在白名单中
     *
     * @param apps
     * @param appid
     * @return
     */
    private boolean checkAppsIsWhiteList(String apps, String appid) {
        if (StringUtil.isEmpty(apps)) {
            return true;
        } else {
            String[] fps = apps.split(",");
            if (Arrays.binarySearch(fps, appid) > 0) {
                return true;
            }
        }
        return false;
    }

    /**
     * 解析数据库信息
     *
     * @param dataSource
     * @param dataType
     * @return
     */
    private String[] getJdbcInfo(String dataSource, String dataType) {
        Matcher matcher = null;
        switch (dataType) {
            case JdbcConstants.MYSQL:
                matcher = PATTERN_MYSQL.matcher(dataSource);
                break;
            case JdbcConstants.ORACLE:
                matcher = PATTERN_ORACLE.matcher(dataSource);
                break;
            case JdbcConstants.SQL_SERVER:
                matcher = PATTERN_SQL_SERVER.matcher(dataSource);
                break;
            case JdbcConstants.DB2:
                matcher = PATTERN_DB2.matcher(dataSource);
                break;
            case JdbcConstants.SYBASE:
                matcher = PATTERN_SYBASE.matcher(dataSource);
                break;
            default:
                break;
        }
        String[] jdbcInfo = null;
        if (matcher != null && matcher.find()) {
            String jIp = matcher.group(1);
            String jPort = matcher.group(2);
            String jName = "";
            jdbcInfo = new String[]{jIp, jPort, jName};
        }
        return jdbcInfo;
    }

    /**
     * 根据正则判断是否需要丢弃数据
     *
     * @param sql
     * @return
     */
    private boolean ifDiscard(String sql) {
        String regex = config.getRegex_filter();
        if (StringUtil.isNotEmpty(regex)) {
            Pattern pattern = Pattern.compile(regex);
            Matcher matcher = pattern.matcher(sql);
            return matcher.matches();
        }
        return false;
    }

    /**
     * 数据写入ES
     */
    private void insertListToEs(List<ThreadDataInfoExt> listModel, String[] indexNames) {
        try {
            // WebRequestInfo
            List<String> requestInfoJsons = new ArrayList<>();
            List<String> requestIds = new ArrayList<>();
            // DataOperateInfo
            List<String> operateInfoJsons = new ArrayList<>();
            List<String> operateIds = new ArrayList<>();
            // SpanInfo
            List<String> spanInfoJsons = new ArrayList<>();
            List<String> spanIds = new ArrayList<>();
            // SqlParseResultInfoExt
            List<String> parseResultInfos = new ArrayList<>();
            List<String> parseResultIds = new ArrayList<>();
            List<String> sqlHashs = new ArrayList<>();
            for (ThreadDataInfoExt model : listModel) {
                WebRequestInfoExt requestInfo = model.getWebRequestInfo();
                String requestInfoJson = JsonUtil.toJson(requestInfo);
                requestInfoJsons.add(requestInfoJson);
                requestIds.add(requestInfo.getReqId());

                List<DataOperateInfoExt> operateInfos = model.getDataInfos();
                for (DataOperateInfoExt operate : operateInfos) {
                    String operateInfoJson = JsonUtil.toJson(operate);
                    operateInfoJsons.add(operateInfoJson);
                    operateIds.add(operate.getDataId());
                }
                List<SpanInfo> spanInfos = model.getSpanInfos();
                for (SpanInfo span : spanInfos) {
                    String spanInfoJson = JsonUtil.toJson(span);
                    spanInfoJsons.add(spanInfoJson);
                    spanIds.add(span.getSpanId());
                }
                List<SqlParseResultInfoExt> resultInfos = model.getSqlParseResultInfos();
                for (SqlParseResultInfoExt parse : resultInfos) {
                    String parseResultInfo = JsonUtil.toJson(parse);
                    parseResultInfos.add(parseResultInfo);
                    parseResultIds.add(parse.getParseId());
                    sqlHashs.add(parse.getHash());
                }
            }

            if (parseResultIds.size() > 0) {
                try {
                    EsRestDocumentUtil.indexBulk(config.getEs_index_adm_sqlParse()
                            , parseResultInfos.toArray(new String[parseResultInfos.size()])
                            , parseResultIds);
                } catch (Exception e) {
                    logger.error("数据写入adm_data_sqlParse异常", e);
                    try {
                        String[] values = sqlHashs.toArray(new String[sqlHashs.size()]);
//                        RedisCacheUtil.deleteSqlHashValues(values);
                    } catch (Exception ee) {
                        logger.error("删除缓存中value出现异常", e);
                    }
                }
            }
            if (requestIds.size() > 0) {
                try {
                    EsRestDocumentUtil.indexBulk(indexNames[0]
                            , requestInfoJsons.toArray(new String[requestInfoJsons.size()])
                            , requestIds
                            , config.getEs_pipeline_timestamp());
                } catch (Exception e) {
                    logger.error("数据写入adm_data_request异常", e);
                }
            }
            if (operateIds.size() > 0) {
                try {
                    EsRestDocumentUtil.indexBulk(indexNames[1]
                            , operateInfoJsons.toArray(new String[operateInfoJsons.size()])
                            , operateIds
                            , config.getEs_pipeline_timestamp());
                } catch (Exception e) {
                    logger.error("数据写入adm_data_operate异常", e);
                }
            }
            if (spanIds.size() > 0) {
                try {
                    EsRestDocumentUtil.indexBulk(indexNames[2]
                            , spanInfoJsons.toArray(new String[spanInfoJsons.size()])
                            , spanIds
                            , config.getEs_pipeline_timestamp());
                } catch (Exception e) {
                    logger.error("数据写入adm_data_span异常", e);
                }
            }
        } catch (Exception e) {
            logger.error("数据写入ES异常", e);
        }
    }

    /**
     * ES adm_data_request、adm_data_operate、adm_data_span每天创建新的索引 如:adm_data_request20200429
     *
     * @return
     */
    private boolean checkEsIndex(String curIndexName, String cacheField) {
        // 先去redis判断缓存
        String cacheIndexName = RedisCacheUtil.getESIndex(cacheField);
        if (StringUtil.isEmpty(curIndexName) || !curIndexName.equals(cacheIndexName)) {
            // 再判断索引是否存在,不存在则创建新的索引
            if (!EsRestIndexUtil.exists(new String[]{curIndexName})) {
                logger.info("创建[" + curIndexName + "]索引.");
                boolean acknowledged = EsRestIndexUtil.create(curIndexName, getJsonSource(cacheField));
                if (!acknowledged) {
                    return false;
                }
            }
            // 新的索引更新到redis中
            RedisCacheUtil.setESIndex(cacheField, curIndexName);
        }
        return true;
    }

    /**
     * 获取索引setting和mapping
     *
     * @param aName
     * @return
     */
    private String getJsonSource(String aName) {
        String jsonSource = "";
        switch (aName) {
            case "request":
                jsonSource = "{\"mappings\":{\"properties\":{\"reqId\":{\"type\":\"keyword\"},\"appId\":{\"type\":\"keyword\"},\"fingerprint\":{\"type\":\"keyword\"},\"session_id\":{\"type\":\"keyword\"},\"req_url\":{\"type\":\"text\",\"fields\":{\"keyword\":{\"type\":\"keyword\"}}},\"req_method\":{\"type\":\"keyword\"},\"req_agent\":{\"type\":\"keyword\"},\"req_ip\":{\"type\":\"keyword\"},\"req_size\":{\"type\":\"long\"},\"req_params\":{\"type\":\"keyword\"},\"req_cookie\":{\"type\":\"keyword\"},\"req_headers\":{\"type\":\"keyword\"},\"rep_headers\":{\"type\":\"keyword\"},\"rep_code\":{\"type\":\"keyword\"},\"rep_content_type\":{\"type\":\"keyword\"},\"rep_size\":{\"type\":\"long\"},\"pluginType\":{\"type\":\"keyword\"},\"starttime\":{\"type\":\"date\"},\"endtime\":{\"type\":\"date\"},\"cost\":{\"type\":\"long\"},\"auth_id\":{\"type\":\"keyword\"},\"white_type\":{\"type\":\"integer\"}}}}";
                break;
            case "operate":
                jsonSource = "{\"mappings\":{\"properties\":{\"dataId\":{\"type\":\"keyword\"},\"appId\":{\"type\":\"keyword\"},\"reqId\":{\"type\":\"keyword\"},\"session_id\":{\"type\":\"keyword\"},\"req_ip\":{\"type\":\"keyword\"},\"fingerprint\":{\"type\":\"keyword\"},\"dataSource\":{\"type\":\"keyword\"},\"operateType\":{\"type\":\"keyword\"},\"sql\":{\"type\":\"text\",\"fields\":{\"keyword\":{\"type\":\"keyword\",\"ignore_above\":32766}}},\"sql_hash\":{\"type\":\"keyword\"},\"sql_hash_analy\":{\"type\":\"keyword\"},\"sql_params\":{\"type\":\"keyword\"},\"is_exist\":{\"type\":\"boolean\"},\"pluginType\":{\"type\":\"keyword\"},\"res_status\":{\"type\":\"boolean\"},\"res_object\":{\"type\":\"keyword\"},\"res_lines\":{\"type\":\"integer\"},\"res_sample\":{\"type\":\"keyword\"},\"starttime\":{\"type\":\"date\"},\"endtime\":{\"type\":\"date\"},\"cost\":{\"type\":\"long\"},\"sql_types\":{\"type\":\"keyword\"},\"sql_parameterize\":{\"type\":\"text\",\"fields\":{\"keyword\":{\"type\":\"keyword\",\"ignore_above\":32766}}},\"jdbc_ip\":{\"type\":\"keyword\"},\"jdbc_port\":{\"type\":\"integer\"},\"jdbc_databaseName\":{\"type\":\"keyword\"},\"auth_id\":{\"type\":\"keyword\"},\"login_operate\":{\"type\":\"boolean\"},\"white_type\":{\"type\":\"integer\"},\"threat_type\":{\"type\":\"integer\"}}}}";
                break;
            case "span":
                jsonSource = "{\"mappings\":{\"properties\":{\"spanId\":{\"type\":\"keyword\"},\"appId\":{\"type\":\"keyword\"},\"reqId\":{\"type\":\"keyword\"},\"parentSpanId\":{\"type\":\"keyword\"},\"pluginType\":{\"type\":\"keyword\"},\"pluginName\":{\"type\":\"keyword\"},\"className\":{\"type\":\"keyword\"},\"methodName\":{\"type\":\"keyword\"},\"methodArgs\":{\"type\":\"keyword\"},\"lineNum\":{\"type\":\"keyword\"},\"starttime\":{\"type\":\"date\"},\"endtime\":{\"type\":\"date\"},\"cost\":{\"type\":\"long\"}}}}";
                break;
            case "sqlParse":
                jsonSource = "{\"settings\":{\"index\":{\"refresh_interval\":\"1s\",\"number_of_shards\":\"3\",\"number_of_replicas\":\"0\",\"max_result_window\":100000,\"blocks.read_only_allow_delete\":\"false\",\"analysis\":{\"normalizer\":{\"my_normalizer\":{\"type\":\"custom\",\"filter\":[\"lowercase\",\"asciifolding\"]}}}}},\"mappings\":{\"properties\":{\"parseId\":{\"type\":\"keyword\"},\"statusCode\":{\"type\":\"integer\"},\"hash\":{\"type\":\"keyword\"},\"syntaxError\":{\"type\":\"boolean\"},\"parameterizedSql\":{\"type\":\"keyword\",\"ignore_above\":32766},\"violations\":{\"properties\":{\"code\":{\"type\":\"integer\"},\"message\":{\"type\":\"keyword\"}}},\"message\":{\"type\":\"keyword\"},\"tables\":{\"type\":\"keyword\",\"normalizer\":\"my_normalizer\"},\"operates\":{\"type\":\"keyword\",\"normalizer\":\"my_normalizer\"},\"table_operates\":{\"type\":\"keyword\",\"normalizer\":\"my_normalizer\"},\"functions\":{\"type\":\"keyword\",\"normalizer\":\"my_normalizer\"},\"score\":{\"type\":\"integer\"}}}}";
                break;
            default:
                break;
        }
        return jsonSource;
    }

    /**
     * 根据appId获取对应的账号匹配规则
     *
     * @param appId
     * @return
     */
    private List<String[]> getAccountMatchRule(String appId) {
        List<String[]> rules = new ArrayList<>();
        try {
            String jsonRule = RedisCacheUtil.getAppAuth(appId);
            if (StringUtil.isNotEmpty(jsonRule)) {
                String[] authUrls = JsonUtil.readPath(jsonRule, "$.authurl").toString().replace("，", ",").split(",");
                String[] authKeys = JsonUtil.readPath(jsonRule, "$.authkey").toString().replace("，", ",").split(",");
                String[] authTables = JsonUtil.readPath(jsonRule, "$.authtable").toString().replace("，", ",").split(",");
                for (int i = 0; i < authUrls.length; i++) {
                    String url = authUrls[i].trim();
                    String key = authKeys.length > i ? authKeys[i].trim() : "";
                    String table = authTables.length > i ? authTables[i].trim() : "";
                    if (StringUtil.isNotEmpty(url)
                            && StringUtil.isNotEmpty(key)
                            && StringUtil.isNotEmpty(table)) {
                        rules.add(new String[]{url, table, key});
                    }
                }
            }
        } catch (Exception e) {
            logger.info("获取redis数据异常.", e);
        }
        return rules;
    }

    /**
     * 获取sql语句中列对应value
     *
     * @param sql
     * @param sqlType
     * @param table
     * @param column
     * @return
     */
    private String getColumnValue(String sql, String sqlType, String table, String column) {
        String value = "";
        try {
            List<HashMap<String, Object>> list = SqlParseUtil.getConditions(sql, sqlType);
            if (null != list && list.size() > 0) {
                for (HashMap map : list) {
                    String targetTable = map.get("table").toString().toUpperCase();
                    String targetColumn = map.get("column").toString().toUpperCase();
                    if (table.toUpperCase().equals(targetTable) && column.toUpperCase().equals(targetColumn)) {
                        value = ((ArrayList) map.get("value")).get(0).toString();
                    }
                }
            }
        } catch (Exception e) {
            logger.error("获取sql语句参数值异常.", e);
        }
        return value;
    }
}
