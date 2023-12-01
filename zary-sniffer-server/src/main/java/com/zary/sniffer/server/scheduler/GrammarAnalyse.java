package com.zary.sniffer.server.scheduler;

import com.alibaba.fastjson.JSON;
import com.zary.sniffer.server.config.ServerConfig;
import com.zary.sniffer.server.model.ThreatRules;
import com.zary.sniffer.server.model.ThreatWarning;
import com.zary.sniffer.server.mysql.MySqlUtil;
import com.zary.sniffer.server.utils.IPUtils;
import com.zary.sniffer.server.utils.JsonUtil;
import com.zary.sniffer.core.enums.ComStateType;
import com.zary.sniffer.core.enums.ThreatPromptType;
import com.zary.sniffer.core.model.ext.DataOperateInfoExt;
import com.zary.sniffer.core.model.ext.SqlParseResultInfoExt;
import com.zary.wall.violation.ErrorCode;
import com.zx.lib.elasticsearch.EsRestSearchUtil;
import com.zx.lib.elasticsearch.model.EsRestSearchArgs;
import com.zx.lib.elasticsearch.model.EsRestSearchResult;
import com.zx.lib.utils.DateUtil;
import com.zx.lib.utils.LogUtil;
import com.zx.lib.utils.StringUtil;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.sort.SortBuilders;
import org.elasticsearch.search.sort.SortOrder;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * @author wanght
 * @date 2020-01-26 15:00:00
 * 语法分析
 */
public class GrammarAnalyse implements Job {

    /**
     * 日志对象
     */
    static Logger logger = LogUtil.getLogger(GrammarAnalyse.class);
    /**
     * 配置参数
     */
    static ServerConfig config = null;
    /**
     * 查询时间戳
     */
    long[] queryTimeStamp = null;

    static SimpleDateFormat SDF = new SimpleDateFormat("yyyy-MM-dd HH:mm");
    /**
     * 违规操作
     */
    Map<String, ThreatRules> mapViolations = null;

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        try {
            long startTimeStamp = System.currentTimeMillis();
            logger.info("开始执行语法分析,时间:" + startTimeStamp);
            // 获取配置
            if (config == null) {
                config = (ServerConfig) context.getJobDetail().getJobDataMap().get("config");
            }
            // 查询数据前延迟5s
            Thread.sleep(5000);
            // 获取查询时间范围
            getQueryTimeStamp();
            // 执行语法分析
            executeAnalyse();
            logger.info("语法分析完成,查询时间:" + queryTimeStamp[0] + "-" + queryTimeStamp[1]
                    + ",结束时间:" + System.currentTimeMillis()
                    + ",耗时:" + (System.currentTimeMillis() - startTimeStamp) / 1000 + "s");
        } catch (Exception e) {
            logger.error("语法分析异常.", e);
        }
    }

    /**
     * 获取查询开始结束时间戳
     *
     * @return
     */
    private void getQueryTimeStamp() throws ParseException {
        long curTimeStamp = SDF.parse(SDF.format(new Date())).getTime();
        long startTimeStamp = curTimeStamp - config.getQuartz_grammarAnalyse_intervalTime() * 60 * 1000;
        queryTimeStamp = new long[]{startTimeStamp, curTimeStamp};
    }

    /**
     * 获取所有违规操作策略
     */
    private void getMapViolations() {
        List<ThreatRules> rules = MySqlUtil.getRules_999();
        if (rules.size() > 0) {
            mapViolations = new HashMap<>();
            for (ThreatRules rule : rules) {
                String ext = rule.getExt();
                if (ext.contains("operates")) {
                    String operate = JsonUtil.readPath(ext, "$.operates");
                    mapViolations.put(operate, rule);
                } else if (ext.contains("functions") && ext.contains("database")) {
                    String function = JsonUtil.readPath(ext, "$.functions");
                    String[] sqlTypes = JsonUtil.readPath(ext, "$.database").toString().split(",");
                    for (String type : sqlTypes) {
                        String key = function + "-" + type;
                        mapViolations.put(key, rule);
                    }
                }
            }
        }
    }

    /**
     * 执行分析
     */
    private void executeAnalyse() {
        try {
            // 1.读取全部数据
            List<DataOperateInfoExt> operateInfoExtList = getOperateList();
            if (operateInfoExtList.size() == 0) {
                return;
            }

            // 2.去重获取所有SqlHash,判断是否存在违规操作策略数据
            List<String> listSqlHash = new ArrayList<>();
            boolean ifExistViolations = false;
            for (DataOperateInfoExt item : operateInfoExtList) {
                if (!ifExistViolations && item.getThreat_type() == ThreatPromptType.违规操作.getValue()) {
                    ifExistViolations = true;
                }
                if (!listSqlHash.contains(item.getSql_hash_analy())) {
                    listSqlHash.add(item.getSql_hash_analy());
                }
            }

            if (ifExistViolations) {
                // 3.获取所有违规操作策略
                getMapViolations();
            }

            // 4.获取非法的SqlParseInfo
            Map<String, SqlParseResultInfoExt> mapSqlParseInfo = getIllegalSqlHash(listSqlHash);
            if (mapSqlParseInfo.size() > 0) {
                List<ThreatWarning> syntaxErrorList = new ArrayList<>();
                for (DataOperateInfoExt ext : operateInfoExtList) {
                    if (mapSqlParseInfo.containsKey(ext.getSql_hash_analy())) {
                        ThreatWarning warning = threadWarning(ext, mapSqlParseInfo.get(ext.getSql_hash_analy()));
                        if (warning != null) {
                            syntaxErrorList.add(warning);
                        }
                    }
                }
                MySqlUtil.batchInsert(syntaxErrorList);
            }

        } catch (Exception e) {
            logger.error("语法分析异常.", e);
        }
    }

    /**
     * 组装threat_warning 数据
     *
     * @param operateInfo
     * @param sqlParseInfo
     * @return
     */
    private ThreatWarning threadWarning(DataOperateInfoExt operateInfo, SqlParseResultInfoExt sqlParseInfo) {
        ThreatWarning warning = new ThreatWarning();
        warning.setAppid(operateInfo.getAppId());
        warning.setDataid(operateInfo.getDataId());
        warning.setFingerprint(operateInfo.getFingerprint());
        warning.setIp(operateInfo.getReq_ip());
        warning.setLoginname(operateInfo.getAuth_id());
        warning.setGeo(IPUtils.getIPDesc(warning.getIp()));
        warning.setMessage(operateInfo.getSql_parameterize());
        warning.setState(ComStateType.正常.getValue());
        warning.setStarttime(DateUtil.getDate(operateInfo.getStarttime()));
        warning.setEndtime(DateUtil.getDate(operateInfo.getEndtime()));
        warning.setAttackstate(operateInfo.isRes_status() ? 1 : 0);
        warning.setAlarmcount(1);

        int levelValue = 60;
        if (operateInfo.getThreat_type() == ThreatPromptType.语法错误.getValue()) {
            // 语法错误告警
            warning.setLevel(levelValue);
            warning.setType(ThreatPromptType.语法错误.getValue());
        } else if (operateInfo.getThreat_type() == ThreatPromptType.SQL注入.getValue()) {
            // sql注入告警
            List<String> descList = new ArrayList<>();
            for (SqlParseResultInfoExt.ViolationItem violationItem : sqlParseInfo.getViolations()) {
                switch (violationItem.getCode()) {
                    case ErrorCode.NONE_CONDITION:
                        descList.add("禁止执行无WHERE条件的DELETE/UPDATE语句");
                        break;
                    case ErrorCode.ALWAYS_TRUE:
                        levelValue = levelValue < 70 ? 70 : levelValue;
                        descList.add("查询语句包含永真条件");
                        break;
                    case ErrorCode.ALWAYS_FALSE:
                        levelValue = levelValue < 70 ? 70 : levelValue;
                        descList.add("查询语句包含永假条件");
                        break;
                    case ErrorCode.NOT_PARAMETERIZED:
                        levelValue = levelValue < 70 ? 70 : levelValue;
                        descList.add("使用非参数化语句");
                        break;
                    case ErrorCode.XOR:
                        levelValue = levelValue < 70 ? 70 : levelValue;
                        descList.add("查询条件存在XOR条件");
                        break;
                    case ErrorCode.DOUBLE_CONST_CONDITION:
                        levelValue = levelValue < 70 ? 70 : levelValue;
                        descList.add("查询条件存在连续两个常量运算表达式");
                        break;
                    case ErrorCode.LIMIT_ZERO:
                        levelValue = levelValue < 70 ? 70 : levelValue;
                        descList.add("存在LIMIT 0危险语句");
                        break;
                    case ErrorCode.TABLE_DENY:
                        levelValue = levelValue < 70 ? 70 : levelValue;
                        if (violationItem.getMessage().contains(":")) {
                            descList.add("使用了禁用的表：" + violationItem.getMessage().split(":")[1].trim());
                        } else {
                            descList.add("使用了禁用的表：*");
                        }
                        break;
                    case ErrorCode.SCHEMA_DENY:
                        levelValue = levelValue < 70 ? 70 : levelValue;
                        if (violationItem.getMessage().contains(":")) {
                            descList.add("使用了禁用的Schema：" + violationItem.getMessage().split(":")[1].trim());
                        } else {
                            descList.add("使用了禁用的Schema：*");
                        }
                        break;
                    case ErrorCode.FUNCTION_DENY:
                        levelValue = levelValue < 70 ? 70 : levelValue;
                        if (violationItem.getMessage().contains(":")) {
                            descList.add("使用了禁用的函数：" + violationItem.getMessage().split(":")[1].trim());
                        } else {
                            descList.add("使用了禁用的函数：*");
                        }
                        break;
                    case ErrorCode.OBJECT_DENY:
                        levelValue = levelValue < 70 ? 70 : levelValue;
                        if (violationItem.getMessage().contains(":")) {
                            descList.add("使用了禁用的对象：" + violationItem.getMessage().split(":")[1].trim());
                        } else {
                            descList.add("使用了禁用的对象：*");
                        }
                        break;
                    case ErrorCode.VARIANT_DENY:
                        levelValue = levelValue < 70 ? 70 : levelValue;
                        if (violationItem.getMessage().contains(":")) {
                            descList.add("使用了禁用的变量：" + violationItem.getMessage().split(":")[1].trim());
                        } else {
                            descList.add("使用了禁用的变量：*");
                        }
                        break;
                    case ErrorCode.INTO_OUTFILE:
                        levelValue = 80;
                        descList.add("进行SELECT ... INTO OUTFILE危险操作");
                        break;

                    case ErrorCode.COMMENT_STATEMENT_NOT_ALLOW:
                        levelValue = levelValue < 70 ? 70 : levelValue;
                        descList.add("语句中存在注释");
                        break;
                    case ErrorCode.NONE_BASE_STATEMENT_NOT_ALLOW:
                        levelValue = levelValue < 70 ? 70 : levelValue;
                        descList.add("使用禁止的非基本语句");
                        break;
                    case ErrorCode.MULTI_STATEMENT:
                        levelValue = levelValue < 70 ? 70 : levelValue;
                        descList.add("一次执行多条语句");
                        break;
                    case ErrorCode.UNION:
                        levelValue = levelValue < 70 ? 70 : levelValue;
                        descList.add("联合查询注入");
                        break;
                    case ErrorCode.CONST_CASE_CONDITION:
                        levelValue = levelValue < 70 ? 70 : levelValue;
                        descList.add("使用了常量条件");
                        break;
                    default:
                        break;
                }
            }
            Map<String, Object> map_detail = new HashMap<>();
            map_detail.put("description", descList);
            String detailJson = JSON.toJSONString(map_detail);
            warning.setDetail(detailJson);
            warning.setType(ThreatPromptType.SQL注入.getValue());
            warning.setLevel(operateInfo.isRes_status() ? 80 : levelValue);
        } else if (operateInfo.getThreat_type() == ThreatPromptType.违规操作.getValue()) {
            if (mapViolations == null) {
                return null;
            }
            String key = "";
            if (sqlParseInfo.getOperates() != null) {
                for (String operate : sqlParseInfo.getOperates()) {
                    String tmpKey = operate.toUpperCase();
                    if (mapViolations.containsKey(tmpKey)) {
                        key = tmpKey;
                        break;
                    }
                }
            }
            if (StringUtil.isEmpty(key) && sqlParseInfo.getFunctions() != null) {
                for (String function : sqlParseInfo.getFunctions()) {
                    String tmpKey = function.toUpperCase() + "-" + operateInfo.getPluginType().name().toUpperCase();
                    if (mapViolations.containsKey(tmpKey)) {
                        key = tmpKey;
                        break;
                    }
                }
            }
            ThreatRules rule = mapViolations.get(key);
            Map<String, Object> map_detail = new HashMap<>();
            map_detail.put("description", rule.getDescription());
            String detailJson = JSON.toJSONString(map_detail);
            warning.setDetail(detailJson);
            warning.setLevel(rule.getLevel());
            warning.setRuleid(rule.getUid());
            warning.setType(ThreatPromptType.违规操作.getValue());
        }
        return warning;
    }

    /**
     * 读取条件范围内数据
     *
     * @return
     */
    private List<DataOperateInfoExt> getOperateList() {
        List<DataOperateInfoExt> operateInfoExtList = new ArrayList<>();
        try {
            BoolQueryBuilder queryBuilder = QueryBuilders.boolQuery();
            queryBuilder.must(QueryBuilders.rangeQuery("timestamp").gte(queryTimeStamp[0]).lt(queryTimeStamp[1]));
            queryBuilder.must(QueryBuilders.termsQuery("threat_type", new Object[]{1, 2, 999}));
            EsRestSearchArgs args = new EsRestSearchArgs(new String[]{config.getEs_index_adm_operate()}, queryBuilder);
            args.setSort(SortBuilders.fieldSort("starttime").order(SortOrder.ASC));
            args.setSize(config.getEs_query_size());
            EsRestSearchResult result = EsRestSearchUtil.search(args);
            operateInfoExtList = result.getRecordsList(DataOperateInfoExt.class);
        } catch (Exception e) {
            logger.error("读取operate数据异常.", e);
        }
        return operateInfoExtList;
    }

    /**
     * 获取非法sql hash
     *
     * @return
     */
    private Map<String, SqlParseResultInfoExt> getIllegalSqlHash(List<String> sqlHash) {
        Map<String, SqlParseResultInfoExt> mapSqlParse = new HashMap<>();
        try {
            BoolQueryBuilder queryBuilder = QueryBuilders.boolQuery();
            queryBuilder.must(QueryBuilders.termsQuery("hash", sqlHash));
            EsRestSearchArgs args = new EsRestSearchArgs(new String[]{config.getEs_index_adm_sqlParse()}, queryBuilder);
            args.setIncludeFields(new String[]{"hash", "operates", "violations", "functions"});
            args.setSize(config.getEs_query_size());
            EsRestSearchResult result = EsRestSearchUtil.search(args);
            List<SqlParseResultInfoExt> records = result.getRecordsList(SqlParseResultInfoExt.class);
            for (SqlParseResultInfoExt ext : records) {
                mapSqlParse.put(ext.getHash(), ext);
            }
        } catch (Exception e) {
            logger.error("读取sqlParse数据异常.", e);
        }
        return mapSqlParse;
    }
}
