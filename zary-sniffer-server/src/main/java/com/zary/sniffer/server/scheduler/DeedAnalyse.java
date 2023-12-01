package com.zary.sniffer.server.scheduler;

import com.alibaba.fastjson.JSON;
import com.zary.sniffer.server.config.ServerConfig;
import com.zary.sniffer.server.model.ThreatRules;
import com.zary.sniffer.server.model.ThreatWarning;
import com.zary.sniffer.server.mysql.MySqlUtil;
import com.zary.sniffer.server.utils.IPUtils;
import com.zary.sniffer.core.enums.EnumActionType;
import com.zary.sniffer.core.enums.ThreatPromptType;
import com.zary.sniffer.core.model.ext.DataOperateInfoExt;
import com.zx.lib.elasticsearch.EsRestSearchUtil;
import com.zx.lib.elasticsearch.model.EsRestSearchArgs;
import com.zx.lib.elasticsearch.model.EsRestSearchResult;
import com.zx.lib.json.JsonUtil;
import com.zx.lib.utils.DateUtil;
import com.zx.lib.utils.LogUtil;
import com.zx.lib.utils.StringUtil;
import org.apache.commons.lang3.time.DateUtils;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.aggregations.Aggregation;
import org.elasticsearch.search.aggregations.AggregationBuilder;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.Aggregations;
import org.elasticsearch.search.aggregations.bucket.histogram.DateHistogramInterval;
import org.elasticsearch.search.aggregations.bucket.histogram.Histogram;
import org.elasticsearch.search.aggregations.bucket.histogram.ParsedDateHistogram;
import org.elasticsearch.search.aggregations.bucket.terms.ParsedTerms;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;
import org.elasticsearch.search.aggregations.metrics.ParsedCardinality;
import org.elasticsearch.search.aggregations.metrics.ParsedSum;
import org.elasticsearch.search.sort.SortBuilders;
import org.elasticsearch.search.sort.SortOrder;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.ZoneId;
import java.util.*;

/**
 * @author wanght
 * @date 2020-01-26 15:00:00
 * 行为分析
 */
public class DeedAnalyse implements Job {
    /**
     * 日志对象
     */
    static Logger logger = LogUtil.getLogger(DeedAnalyse.class);
    /**
     * 配置参数
     */
    static ServerConfig config = null;
    static SimpleDateFormat SDF = new SimpleDateFormat("yyyy-MM-dd HH:mm");
    /**
     * 查询时间戳
     */
    long[] queryTimeStamp = null;
    /**
     * 目前监控的应用程序ID
     */
    List<String> listAllAppId = new ArrayList<>();
    /**
     * 详情显示的数据条数
     */
    final int detailSize = 30;

    /**
     * 执行计划任务
     *
     * @param context
     * @throws JobExecutionException
     */
    @Override
    public void execute(JobExecutionContext context) {
        try {
            long startTimeStamp = System.currentTimeMillis();
            logger.info("开始执行行为分析,时间:" + startTimeStamp);
            // 获取配置
            if (config == null) {
                config = (ServerConfig) context.getJobDetail().getJobDataMap().get("config");
            }
            // 查询数据前延迟5s,保证所有数据已落盘,es会延迟1秒
            Thread.sleep(5000);
            // 获取查询时间范围
            getQueryTimeStamp();
            // 获取所有应用程序UID
            listAllAppId = MySqlUtil.getAppIds();
            // 获取行为分析规则
            List<ThreatRules> rules = MySqlUtil.getAllLiveRules();
            for (ThreatRules rule : rules) {
                executeAnalyse(rule);
            }
            logger.info("行为分析完成,查询时间:" + queryTimeStamp[0] + "-" + queryTimeStamp[1]
                    + ",结束时间:" + System.currentTimeMillis()
                    + ",耗时:" + (System.currentTimeMillis() - startTimeStamp) / 1000 + "s");
        } catch (Exception e) {
            logger.error("行为分析异常.", e);
        }
    }

    /**
     * 获取查询开始结束时间戳(默认查询30分钟内的数据)
     *
     * @return
     */
    private void getQueryTimeStamp() throws ParseException {
        long curTimeStamp = SDF.parse(SDF.format(new Date())).getTime();
        long startTimeStamp = curTimeStamp - config.getQuartz_deedAnalyse_intervalTime() * 60 * 1000;
        queryTimeStamp = new long[]{startTimeStamp, curTimeStamp};
    }

    /**
     * 执行分析
     */
    private void executeAnalyse(ThreatRules rule) {
        try {
            if (rule.getType() == ThreatPromptType.数据机器人.getValue()) {
                analyse_3(rule);
            } else if (rule.getType() == ThreatPromptType.数据泄露.getValue()) {
                analyse_4(rule);
            } else if (rule.getType() == ThreatPromptType.账号安全.getValue()) {
                analyse_5(rule);
            } else if (rule.getType() == ThreatPromptType.自定义策略.getValue()) {
                analyse_7(rule);
            } else if (rule.getType() == ThreatPromptType.指纹伪造.getValue()) {
                analyse_8(rule);
            }
        } catch (Exception e) {
            logger.error("分析异常.", e);
        }
    }

    /**
     * 策略3 - 数据机器人策略
     *
     * @param rule
     */
    private void analyse_3(ThreatRules rule) {
        try {
            String description = "检测到该终端指纹用户" + rule.getIntervaltime() + "分钟内";
            int executeType = 0;
            if (rule.getIntervaltime() > 0) {
                if (rule.getSamesqlcount() > 0 && rule.getIpcount() == 0) {
                    executeType = 1;
                    description += "进行多次相同结构查询语句操作,疑似爬虫行为";
                } else if (rule.getIpcount() > 0 && rule.getSamesqlcount() == 0) {
                    executeType = 2;
                    description += "IP地址发生多次变化,疑似通过多个代理IP进行操作";
                } else if (rule.getIpcount() > 0 && rule.getSamesqlcount() > 0) {
                    executeType = 3;
                    description += "进行多次相同结构查询语句操作,且IP地址发生多次变化,疑似通过多代理IP进行数据爬虫行为";
                }
            }
            if (executeType == 0) {
                return;
            }
            // 策略执行的动作SELECT / SELECT INTO
            String[] operates = EnumActionType.getActions(1);
            // 1.获取所有应用程序ID
            List<String> listAppId = rule.getListAppId();
            if (null == listAppId || listAppId.size() == 0) {
                listAppId = listAllAppId;
            }
            List<ThreatWarning> warnings = new ArrayList<>();
            BoolQueryBuilder queryBuilder = QueryBuilders.boolQuery();
            queryBuilder.must(QueryBuilders.rangeQuery("timestamp").gte(queryTimeStamp[0]).lt(queryTimeStamp[1]));
            queryBuilder.must(QueryBuilders.termQuery("white_type", 0));
            queryBuilder.must(QueryBuilders.termsQuery("appId", listAppId));
            if (rule.getExecutestate() != null) {
                queryBuilder.must(QueryBuilders.termQuery("res_status", rule.getExecutestate()));
            }
            // 聚合sql语句出现次数
            String aggName_sql = "term_sql";
            AggregationBuilder agg_sql = AggregationBuilders
                    .terms(aggName_sql)
                    .field("sql_hash_analy")
                    .size(detailSize);
            // 聚合所有ip地址
            String aggName_ip = "term_ip";
            AggregationBuilder agg_ip = AggregationBuilders
                    .terms(aggName_ip)
                    .field("req_ip")
                    .size(detailSize);
            // 聚合时间单位内数据
            String aggName_starttime = "date_starttime";
            AggregationBuilder agg_starttime = AggregationBuilders
                    .dateHistogram(aggName_starttime)
                    .field("starttime")
                    .fixedInterval(DateHistogramInterval.minutes(rule.getIntervaltime()))
                    .format("yyyy-MM-dd HH:mm:ss")
                    .minDocCount(0L)
                    .timeZone(ZoneId.of("+08:00"));
            // 聚合每个指纹
            String aggName_fingerprint = "term_fingerprint";
            AggregationBuilder agg_fingerprint = AggregationBuilders
                    .terms(aggName_fingerprint)
                    .field("fingerprint")
                    .size(config.getEs_agg_size());

            if (executeType == 1) {
                agg_fingerprint.subAggregation(agg_starttime.subAggregation(agg_sql));
            } else if (executeType == 2) {
                agg_fingerprint.subAggregation(agg_starttime.subAggregation(agg_ip));
            } else if (executeType == 3) {
                agg_fingerprint.subAggregation(agg_starttime.subAggregation(agg_sql).subAggregation(agg_ip));
            }

            // 聚合每个app
            String aggName_app = "term_app";
            AggregationBuilder agg_app = AggregationBuilders
                    .terms(aggName_app)
                    .field("appId")
                    .subAggregation(agg_fingerprint);

            EsRestSearchArgs args = new EsRestSearchArgs(new String[]{config.getEs_index_adm_operate()}, queryBuilder);
            args.setFetchSource(false);
            args.setAggregation(agg_app);
            EsRestSearchResult result = EsRestSearchUtil.search(args);
            Map<String, Object[]> data_app = getAggData(result.getAggregations(), AggType.terms, aggName_app);
            for (Map.Entry<String, Object[]> entry_app : data_app.entrySet()) {
                String appId = entry_app.getKey();
                Map<String, Object[]> data_fingerprint = getAggData((Aggregations) entry_app.getValue()[1], AggType.terms, aggName_fingerprint);
                for (Map.Entry<String, Object[]> entry_fingerprint : data_fingerprint.entrySet()) {
                    Map<String, Object[]> data_startTime = getAggData((Aggregations) entry_fingerprint.getValue()[1], AggType.dateHistogram, aggName_starttime);
                    Map<String, Long> map_sqlHash = new HashMap<>();
                    Map<String, Long> map_ip = new HashMap<>();
                    Date startTime = null;
                    Date endTime = null;
                    for (Map.Entry<String, Object[]> entry_startTime : data_startTime.entrySet()) {
                        long count_startTime = (long) entry_startTime.getValue()[0];
                        if (count_startTime == 0) {
                            continue;
                        }

                        boolean isAlarm = false;
                        if ((executeType & 1) > 0) {
                            Map<String, Object[]> data_sql = getAggData((Aggregations) entry_startTime.getValue()[1], AggType.terms, aggName_sql);
                            for (Map.Entry<String, Object[]> entry_sql : data_sql.entrySet()) {
                                long count_sql = (long) entry_sql.getValue()[0];
                                if (count_sql >= rule.getSamesqlcount()) {
                                    isAlarm = true;
                                    String parameterizedSql = checkSqlIsSelect(entry_sql.getKey(), operates);
                                    if (StringUtil.isNotEmpty(parameterizedSql)) {
                                        if (map_sqlHash.containsKey(parameterizedSql)) {
                                            long oldCount = map_sqlHash.get(parameterizedSql);
                                            long newCount = oldCount + (long) entry_sql.getValue()[0];
                                            map_sqlHash.put(parameterizedSql, newCount);
                                        } else {
                                            if (map_sqlHash.size() < detailSize) {
                                                map_sqlHash.put(parameterizedSql, (long) entry_sql.getValue()[0]);
                                            }
                                        }
                                    }
                                }
                            }
                        }
                        if ((executeType & 2) > 0) {
                            Map<String, Object[]> data_ip = getAggData((Aggregations) entry_startTime.getValue()[1], AggType.terms, aggName_ip);
                            if (data_ip.size() >= rule.getIpcount()) {
                                isAlarm = true;
                                for (Map.Entry<String, Object[]> entry_ip : data_ip.entrySet()) {
                                    if (map_ip.containsKey(entry_ip.getKey())) {
                                        long oldCount = map_ip.get(entry_ip.getKey());
                                        long newCount = oldCount + (long) entry_ip.getValue()[0];
                                        map_ip.put(entry_ip.getKey(), newCount);
                                    } else {
                                        if (map_ip.size() < detailSize) {
                                            map_ip.put(entry_ip.getKey(), (long) entry_ip.getValue()[0]);
                                        }
                                    }
                                }
                            }
                        }

                        if (isAlarm) {
                            if (startTime == null) {
                                startTime = DateUtil.getDate(entry_startTime.getKey());
                            }
                            endTime = DateUtil.getDate(entry_startTime.getKey());
                        }
                    }
                    List<Object> listSqlHash = new ArrayList<>();
                    if (map_sqlHash.size() > 0) {
                        for (Map.Entry<String, Long> entry : map_sqlHash.entrySet()) {
                            Map<String, Object> map = new HashMap<>();
                            map.put("sql", entry.getKey());
                            map.put("count", entry.getValue());
                            listSqlHash.add(map);
                        }
                    }
                    List<Object> listIp = new ArrayList<>();
                    if (map_ip.size() > 0) {
                        for (Map.Entry<String, Long> entry : map_ip.entrySet()) {
                            Map<String, Object> map = new HashMap<>();
                            map.put("ip", entry.getKey());
                            map.put("count", entry.getValue());
                            listIp.add(map);
                        }
                    }

                    if ((executeType == 1 && listSqlHash.size() > 0) ||
                            (executeType == 2 && listIp.size() > 0) ||
                            (executeType == 3 && listSqlHash.size() > 0 && listIp.size() > 0)) {
                        // 组装detail json数据
                        Map<String, Object> map_detail = new HashMap<>();
                        map_detail.put("sqlInfo", listSqlHash);
                        map_detail.put("ipInfo", listIp);
                        map_detail.put("description", description);
                        String detailJson = JSON.toJSONString(map_detail);
                        DataOperateInfoExt firstData = getFirstData(entry_fingerprint.getKey(), appId, rule, null, null, null, null, SortOrder.ASC);
                        ThreatWarning warning = getWarning(rule, appId, entry_fingerprint.getKey(), firstData.getReq_ip(), firstData.getAuth_id(), firstData.getDataId()
                                , startTime, endTime, detailJson, true);
                        warnings.add(warning);
                    }
                }
            }
            MySqlUtil.batchInsert(warnings);
        } catch (Exception e) {
            logger.error("[数据机器人策略]分析异常.", e);
        }
    }

    /**
     * 策略4 - 数据泄露策略
     *
     * @param rule
     */
    private void analyse_4(ThreatRules rule) {
        try {
            // 1.获取所有应用程序ID(此策略默认只有一个应用程序)
            List<String> listAppId = rule.getListAppId();
            if (null == listAppId || listAppId.size() == 0) {
                listAppId = listAllAppId;
            }
            List<ThreatWarning> warnings = new ArrayList<>();
            for (String appId : listAppId) {
                for (String table : rule.getMultiTable()) {
                    // 2.获取所有动作
                    List<String> listTablePlusOperate = rule.getTablePulsOperates(rule.getListOperate(), table);
                    // 3.获取所有动作匹配的SqlHash
                    List<String> listSqlHash = getListSqlHash(null, null, listTablePlusOperate, null);
                    if (listSqlHash.size() == 0) {
                        return;
                    }
                    // 4.获取所有符合条件的Sql语句
                    BoolQueryBuilder queryBuilder = QueryBuilders.boolQuery();
                    queryBuilder.must(QueryBuilders.termsQuery("sql_hash_analy", listSqlHash));
                    queryBuilder.must(QueryBuilders.rangeQuery("timestamp").gte(queryTimeStamp[0]).lt(queryTimeStamp[1]));
                    queryBuilder.must(QueryBuilders.termQuery("white_type", 0));
                    queryBuilder.must(QueryBuilders.termQuery("appId", appId));
                    if (rule.getResultrows() > 0) {
                        queryBuilder.must(QueryBuilders.rangeQuery("res_lines").gte(rule.getResultrows()));
                    }
                    if (rule.getExecutestate() != null) {
                        queryBuilder.must(QueryBuilders.termQuery("res_status", rule.getExecutestate()));
                    }
                    EsRestSearchArgs args = new EsRestSearchArgs(new String[]{config.getEs_index_adm_operate()}, queryBuilder);
                    args.setSize(config.getEs_query_size());
                    args.setIncludeFields(new String[]{"fingerprint", "req_ip", "sql", "dataId", "starttime", "endtime", "res_lines"});
                    args.setSort(SortBuilders.fieldSort("starttime").order(SortOrder.ASC));
                    EsRestSearchResult result = EsRestSearchUtil.search(args);
                    List<DataOperateInfoExt> records = result.getRecordsList(DataOperateInfoExt.class);
                    // 5.一次统计一个指纹只告警一次
                    Map<String, List<Object>> maps = new HashMap<>();
                    Map<String, String> map_ip = new HashMap<>();
                    Map<String, String> map_dataId = new HashMap<>();
                    Map<String, String> map_loginName = new HashMap<>();
                    Map<String, Date> map_startTime = new HashMap<>();
                    Map<String, Date> map_endTime = new HashMap<>();
                    for (DataOperateInfoExt record : records) {
                        String fingerPrint = record.getFingerprint();
                        Map<String, Object> map_detail = new HashMap<>();
                        map_detail.put("sql", record.getSql());
                        map_detail.put("table", table);
                        map_detail.put("lines", record.getRes_lines());
                        map_detail.put("time", DateUtil.toDateString(record.getStarttime(), "yyyy-MM-dd HH:mm:ss"));
                        if (maps.containsKey(fingerPrint)) {
                            List<Object> mapList = maps.get(fingerPrint);
                            if (mapList.size() < detailSize) {
                                mapList.add(map_detail);
                                maps.put(fingerPrint, mapList);
                            }
                        } else {
                            List<Object> mapList = new ArrayList<>();
                            mapList.add(map_detail);
                            maps.put(fingerPrint, mapList);
                        }
                        if (!map_ip.containsKey(fingerPrint) && StringUtil.isNotEmpty(record.getReq_ip())) {
                            map_ip.put(fingerPrint, record.getReq_ip());
                        }
                        if (!map_startTime.containsKey(fingerPrint) && record.getStarttime() > 0) {
                            map_startTime.put(fingerPrint, DateUtil.getDate(record.getStarttime()));
                        }
                        if (!map_dataId.containsKey(fingerPrint) && StringUtil.isNotEmpty(record.getDataId())) {
                            map_dataId.put(fingerPrint, record.getDataId());
                        }
                        if (!map_loginName.containsKey(fingerPrint) && StringUtil.isNotEmpty(record.getAuth_id())) {
                            map_loginName.put(fingerPrint, record.getAuth_id());
                        }
                        map_endTime.put(fingerPrint, DateUtil.getDate(record.getEndtime()));
                    }
                    // 6.数据入库
                    for (Map.Entry<String, List<Object>> entry : maps.entrySet()) {
                        // 组装detail json数据
                        Map<String, Object> map_detail = new HashMap<>();
                        map_detail.put("sqlInfo", entry.getValue());
                        map_detail.put("description", "检测到该终端指纹用户数据库读取操作异常,疑似拖库攻击行为");
                        String detailJson = JSON.toJSONString(map_detail);
                        String ip = map_ip.getOrDefault(entry.getKey(), "");
                        String dataId = map_dataId.getOrDefault(entry.getKey(), "");
                        String loginName = map_loginName.getOrDefault(entry.getKey(), "");
                        Date startTime = map_startTime.getOrDefault(entry.getKey(), null);
                        Date endTime = map_endTime.getOrDefault(entry.getKey(), null);
                        ThreatWarning warning = getWarning(rule, appId, entry.getKey(), ip, loginName, dataId, startTime, endTime, detailJson, true);
                        warnings.add(warning);
                    }
                }
            }
            MySqlUtil.batchInsert(warnings);
        } catch (Exception e) {
            logger.error("[数据泄露策略]分析异常.", e);
        }
    }

    /**
     * 策略5 - 账号安全策略
     *
     * @param rule
     */
    private void analyse_5(ThreatRules rule) {
        try {
            if (rule.getIntervaltime() <= 0) {
                return;
            }
            // 1.获取所有应用程序ID(此策略默认只有一个应用程序)
            List<String> listAppId = rule.getListAppId();
            if (null == listAppId || listAppId.size() == 0) {
                listAppId = listAllAppId;
            }
            List<ThreatWarning> warnings = new ArrayList<>();
            BoolQueryBuilder queryBuilder = QueryBuilders.boolQuery();
            queryBuilder.must(QueryBuilders.rangeQuery("timestamp").gte(queryTimeStamp[0]).lt(queryTimeStamp[1]));
            queryBuilder.must(QueryBuilders.termQuery("white_type", 0));
            queryBuilder.must(QueryBuilders.termsQuery("appId", listAppId));
            queryBuilder.must(QueryBuilders.termQuery("login_operate", true));
            queryBuilder.mustNot(QueryBuilders.termQuery("auth_id", ""));

            // 聚合时间单位内数据
            String aggName_startTime = "date_startTime";
            AggregationBuilder agg_startTime = AggregationBuilders
                    .dateHistogram(aggName_startTime)
                    .field("starttime")
                    .fixedInterval(DateHistogramInterval.minutes(rule.getIntervaltime()))
                    .format("yyyy-MM-dd HH:mm:ss")
                    .minDocCount(0L)
                    .timeZone(ZoneId.of("+08:00"));

            // 聚合每个指纹
            String aggName_fingerprint = "term_fingerprint";
            AggregationBuilder agg_fingerprint = AggregationBuilders
                    .terms(aggName_fingerprint)
                    .field("fingerprint")
                    .subAggregation(agg_startTime)
                    .size(config.getEs_agg_size());

            // 聚合每个app
            String aggName_app = "term_app";
            AggregationBuilder agg_app = AggregationBuilders
                    .terms(aggName_app)
                    .field("appId")
                    .subAggregation(agg_fingerprint);

            EsRestSearchArgs args = new EsRestSearchArgs(new String[]{config.getEs_index_adm_operate()}, queryBuilder);
            args.setFetchSource(false);
            args.setAggregation(agg_app);
            EsRestSearchResult result = EsRestSearchUtil.search(args);
            Map<String, Object[]> data_app = getAggData(result.getAggregations(), AggType.terms, aggName_app);
            for (Map.Entry<String, Object[]> entry_app : data_app.entrySet()) {
                String appId = entry_app.getKey();
                Map<String, List<long[]>> data_fingerprint_times = new HashMap<>();
                Map<String, Object[]> data_fingerprint = getAggData((Aggregations) entry_app.getValue()[1], AggType.terms, aggName_fingerprint);
                for (Map.Entry<String, Object[]> entry_fingerprint : data_fingerprint.entrySet()) {
                    Map<String, Object[]> data_startTime = getAggData((Aggregations) entry_fingerprint.getValue()[1], AggType.dateHistogram, aggName_startTime);
                    List<long[]> listTime = new LinkedList<>();
                    for (Map.Entry<String, Object[]> entry_startTime : data_startTime.entrySet()) {
                        if ((long) entry_startTime.getValue()[0] >= rule.getRatecount()) {
                            Date beginDate = DateUtil.getDate(entry_startTime.getKey());
                            long beginTime = beginDate.getTime();
                            long endTime = DateUtils.addMinutes(beginDate, rule.getIntervaltime()).getTime();
                            listTime.add(new long[]{beginTime, endTime});
                        }
                    }
                    if (listTime.size() > 0) {
                        data_fingerprint_times.put(entry_fingerprint.getKey(), listTime);
                    }
                }

                for (Map.Entry<String, List<long[]>> entry : data_fingerprint_times.entrySet()) {
                    String fingerPrint = entry.getKey();
                    List<Object> successUserInfo = new ArrayList<>();
                    List<Object> failUserInfo = new ArrayList<>();
                    Date startTime = null;
                    Date endTime = null;
                    String ip = null;
                    String loginName = null;
                    String dataId = null;
                    List<long[]> queryTimes = entry.getValue();
                    for (long[] time : queryTimes) {
                        List<DataOperateInfoExt> tmpListData = getOperateInfo(fingerPrint, appId, time);
                        for (DataOperateInfoExt data : tmpListData) {
                            if (startTime == null) {
                                startTime = DateUtil.getDate(data.getStarttime());
                            }
                            endTime = DateUtil.getDate(data.getEndtime());
                            if (StringUtil.isEmpty(ip)) {
                                ip = data.getReq_ip();
                            }
                            if (StringUtil.isEmpty(loginName) && data.getRes_lines() > 0) {
                                loginName = data.getAuth_id();
                            }
                            if (StringUtil.isEmpty(dataId)) {
                                dataId = data.getDataId();
                            }
                            String attackTime = DateUtil.toDateString(data.getStarttime(), "yyyy-MM-dd HH:mm:ss");
                            Map<String, Object> map_detail = new HashMap<>();
                            map_detail.put("user", data.getAuth_id());
                            map_detail.put("time", attackTime);
                            if (successUserInfo.size() < detailSize && data.getRes_lines() > 0) {
                                successUserInfo.add(map_detail);
                            }
                            if (failUserInfo.size() < detailSize && data.getRes_lines() <= 0) {
                                failUserInfo.add(map_detail);
                            }
                        }
                    }
                    // 组装detail json数据
                    Map<String, Object> map_detail = new HashMap<>();
                    map_detail.put("successUserInfo", successUserInfo);
                    map_detail.put("failUserInfo", failUserInfo);
                    map_detail.put("description", "检测到该终端指纹用户短时间内多次尝试用户登陆操作,疑似撞库攻击行为");
                    String detailJson = JSON.toJSONString(map_detail);
                    ThreatWarning warning = getWarning(rule, appId, fingerPrint, ip, loginName, dataId, startTime, endTime, detailJson, successUserInfo.size() > 0);
                    warnings.add(warning);
                }
            }
            MySqlUtil.batchInsert(warnings);
        } catch (Exception e) {
            logger.error("[账号安全策略]分析异常.", e);
        }
    }

    /**
     * 获取符合条件的操作数据
     *
     * @param fingerPrint
     * @param appId
     * @param queryTimes
     * @return
     * @throws IOException
     */
    private List<DataOperateInfoExt> getOperateInfo(String fingerPrint, String appId, long[] queryTimes) throws IOException {
        BoolQueryBuilder queryBuilder = QueryBuilders.boolQuery();
        queryBuilder.must(QueryBuilders.rangeQuery("timestamp").gte(queryTimes[0]).lt(queryTimes[1]));
        queryBuilder.must(QueryBuilders.termQuery("white_type", 0));
        queryBuilder.must(QueryBuilders.termQuery("fingerprint", fingerPrint));
        queryBuilder.must(QueryBuilders.termQuery("appId", appId));
        queryBuilder.must(QueryBuilders.termQuery("login_operate", true));
        queryBuilder.mustNot(QueryBuilders.termQuery("auth_id", ""));
        EsRestSearchArgs args = new EsRestSearchArgs(new String[]{config.getEs_index_adm_operate()}, queryBuilder);
        args.setSize(config.getEs_query_size());
        args.setSort(SortBuilders.fieldSort("starttime").order(SortOrder.ASC));
        args.setIncludeFields(new String[]{"auth_id", "req_ip", "dataId", "res_status", "res_lines", "starttime", "endtime"});
        EsRestSearchResult result = EsRestSearchUtil.search(args);
        List<DataOperateInfoExt> operateInfoExts = result.getRecordsList(DataOperateInfoExt.class);
        return operateInfoExts;
    }

    /**
     * 策略7 - 自定义操作策略
     *
     * @param rule
     */
    private void analyse_7(ThreatRules rule) {
        try {
            // 1.获取所有应用程序ID(此策略默认只有一个应用程序)
            List<String> listAppId = rule.getListAppId();
            if (null == listAppId || listAppId.size() == 0) {
                listAppId = listAllAppId;
            }
            List<ThreatWarning> warnings = new ArrayList<>();
            // 2.根据表名+动作获取所有SqlHash
            List<String> listSqlHash;
            if (rule.getMultiTable() != null && rule.getMultiTable().length > 0) {
                List<String> listTablePlusOperate = rule.getTablePulsOperates(rule.getListOperate(), rule.getMultiTable());
                listSqlHash = getListSqlHash(null, null, listTablePlusOperate, null);
            } else {
                listSqlHash = getListSqlHash(null, rule.getListOperate(), null, null);
            }
            if (listSqlHash.size() == 0) {
                return;
            }
            List<String> listReqId = null;
            if (StringUtil.isNotEmpty(rule.getUrl())) {
                listReqId = getReqIdByUrl(rule.getUrl());
                if (listReqId.size() == 0) {
                    return;
                }
            }
            // 3.根据SqlHash去操作索引中获取数据
            BoolQueryBuilder queryBuilder = QueryBuilders.boolQuery();
            queryBuilder.must(QueryBuilders.rangeQuery("timestamp").gte(queryTimeStamp[0]).lt(queryTimeStamp[1]));
            queryBuilder.must(QueryBuilders.termsQuery("sql_hash_analy", listSqlHash));
            queryBuilder.must(QueryBuilders.termQuery("white_type", 0));
            queryBuilder.must(QueryBuilders.termsQuery("appId", listAppId));
            if (listReqId != null && listReqId.size() > 0) {
                queryBuilder.must(QueryBuilders.termsQuery("reqId", listReqId));
            }
            if (rule.getResultrows() > 0) {
                queryBuilder.must(QueryBuilders.rangeQuery("res_lines").gte(rule.getResultrows()));
            }
            if (rule.getExecutestate() != null) {
                queryBuilder.must(QueryBuilders.termQuery("res_status", rule.getExecutestate()));
            }

            // 聚合时间单位内数据
            String aggName_startTime = "date_starttime";
            AggregationBuilder agg_startTime = AggregationBuilders
                    .dateHistogram(aggName_startTime)
                    .field("starttime")
                    .fixedInterval(DateHistogramInterval.minutes(rule.getIntervaltime()))
                    .format("yyyy-MM-dd HH:mm:ss")
                    .minDocCount(0L)
                    .timeZone(ZoneId.of("+08:00"));

            // 聚合每个指纹
            String aggName_fingerprint = "term_fingerprint";
            AggregationBuilder agg_fingerprint = AggregationBuilders
                    .terms(aggName_fingerprint)
                    .field("fingerprint")
                    .size(config.getEs_agg_size());

            // 4.执行频率检查
            if (rule.getRatecount() > 0 && rule.getIntervaltime() > 0) {
                agg_fingerprint.subAggregation(agg_startTime);
            }

            // 聚合每个app
            String aggName_app = "term_app";
            AggregationBuilder agg_app = AggregationBuilders
                    .terms(aggName_app)
                    .field("appId")
                    .subAggregation(agg_fingerprint);

            EsRestSearchArgs args = new EsRestSearchArgs(new String[]{config.getEs_index_adm_operate()}, queryBuilder);
            args.setFetchSource(false);
            args.setAggregation(agg_app);
            EsRestSearchResult result = EsRestSearchUtil.search(args);
            Map<String, Object[]> data_app = getAggData(result.getAggregations(), AggType.terms, aggName_app);
            for (Map.Entry<String, Object[]> entry_app : data_app.entrySet()) {
                String appId = entry_app.getKey();
                Map<String, Object[]> data_fingerprint = getAggData((Aggregations) entry_app.getValue()[1], AggType.terms, aggName_fingerprint);
                for (Map.Entry<String, Object[]> entry_fingerprint : data_fingerprint.entrySet()) {
                    long requestCount = (long) entry_fingerprint.getValue()[0];
                    if (requestCount > 0) {
                        Date startTime = null;
                        Date endTime = null;
                        boolean isAlarm = false;
                        // 执行频率查询
                        if (rule.getRatecount() > 0) {
                            Map<String, Object[]> data_startTime = getAggData((Aggregations) entry_fingerprint.getValue()[1], AggType.dateHistogram, aggName_startTime);
                            for (Map.Entry<String, Object[]> entry_startTime : data_startTime.entrySet()) {
                                long count = (long) entry_startTime.getValue()[0];
                                if (count >= rule.getRatecount()) {
                                    isAlarm = true;
                                    if (startTime == null) {
                                        startTime = DateUtil.getDate(entry_startTime.getKey());
                                    }
                                    endTime = DateUtil.getDate(entry_startTime.getKey());
                                }
                            }
                        } else {
                            isAlarm = true;
                        }
                        if (isAlarm) {
                            DataOperateInfoExt firstData = getFirstData(entry_fingerprint.getKey(), appId, rule, listSqlHash, null, listReqId, null, SortOrder.ASC);
                            if (startTime == null) {
                                startTime = DateUtil.getDate(firstData.getStarttime());
                            }
                            if (endTime == null) {
                                DataOperateInfoExt endData = getFirstData(entry_fingerprint.getKey(), appId, rule, listSqlHash, null, listReqId, null, SortOrder.DESC);
                                endTime = DateUtil.getDate(endData.getEndtime());
                            }
                            Map<String, Object> map_detail = new HashMap<>();
                            map_detail.put("description", rule.getDescription());
                            String detailJson = JSON.toJSONString(map_detail);
                            ThreatWarning warning = getWarning(rule, appId, entry_fingerprint.getKey(), firstData.getReq_ip(), firstData.getAuth_id(), firstData.getDataId()
                                    , startTime, endTime, detailJson, true);
                            warnings.add(warning);
                        }
                    }
                }
            }
            MySqlUtil.batchInsert(warnings);
        } catch (Exception e) {
            logger.error("[自定义操作策略]分析异常.", e);
        }
    }

    /**
     * 根据http请求url获取reqId
     *
     * @param req_url
     * @return
     */
    private List<String> getReqIdByUrl(String req_url) {
        List<String> listReqId = new ArrayList<>();
        try {
            if (StringUtil.isNotEmpty(req_url)) {
                BoolQueryBuilder queryBuilder = QueryBuilders.boolQuery();
                queryBuilder.must(QueryBuilders.rangeQuery("timestamp").gte(queryTimeStamp[0]).lt(queryTimeStamp[1]));
                queryBuilder.must(QueryBuilders.matchPhraseQuery("req_url", req_url));
                EsRestSearchArgs args = new EsRestSearchArgs(new String[]{config.getEs_index_adm_request()}, queryBuilder);
                args.setIncludeFields(new String[]{"reqId"});
                args.setSize(config.getEs_query_size());
                EsRestSearchResult result = EsRestSearchUtil.search(args);
                List<String> records = result.getRecords();
                for (String record : records) {
                    String reqId = JsonUtil.readPath(record, "$.reqId");
                    listReqId.add(reqId);
                }
            }
        } catch (Exception e) {
            logger.error("获取请求ID异常.", e);
        }
        return listReqId;
    }

    /**
     * 策略8 - 指纹伪造
     */
    private void analyse_8(ThreatRules rule) {
        try {
            // 1.获取所有应用程序ID
            List<String> listAppId = rule.getListAppId();
            if (null == listAppId || listAppId.size() == 0) {
                listAppId = listAllAppId;
            }
            List<ThreatWarning> warnings = new ArrayList<>();
            BoolQueryBuilder queryBuilder = QueryBuilders.boolQuery();
            queryBuilder.must(QueryBuilders.rangeQuery("timestamp").gte(queryTimeStamp[0]).lt(queryTimeStamp[1]));
            queryBuilder.must(QueryBuilders.termsQuery("appId", listAppId));
            queryBuilder.mustNot(QueryBuilders.termQuery("session_id", ""));
            // 聚合session下所有指纹
            String aggName_fingerprint = "term_fingerPrint";
            AggregationBuilder agg_fingerPrint = AggregationBuilders
                    .terms(aggName_fingerprint)
                    .field("fingerprint")
                    .size(detailSize);

            // 聚合session_id
            String aggName_session_id = "term_session_id";
            AggregationBuilder agg_session_id = AggregationBuilders
                    .terms(aggName_session_id)
                    .field("session_id")
                    .subAggregation(agg_fingerPrint)
                    .size(config.getEs_agg_size());

            // 聚合每个app
            String aggName_app = "term_app";
            AggregationBuilder agg_app = AggregationBuilders
                    .terms(aggName_app)
                    .field("appId")
                    .subAggregation(agg_session_id);

            EsRestSearchArgs args = new EsRestSearchArgs(new String[]{config.getEs_index_adm_operate()}, queryBuilder);
            args.setFetchSource(false);
            args.setAggregation(agg_app);
            EsRestSearchResult result = EsRestSearchUtil.search(args);
            Map<String, Object[]> data_app = getAggData(result.getAggregations(), AggType.terms, aggName_app);
            for (Map.Entry<String, Object[]> entry_app : data_app.entrySet()) {
                String appId = entry_app.getKey();
                Map<String, Object[]> data_session = getAggData((Aggregations) entry_app.getValue()[1], AggType.terms, aggName_session_id);
                for (Map.Entry<String, Object[]> entry_session : data_session.entrySet()) {
                    Map<String, Object[]> data_fingerPrint = getAggData((Aggregations) entry_session.getValue()[1], AggType.terms, aggName_fingerprint);
                    if (data_fingerPrint.size() >= rule.getRatecount()) {
                        List<String> listFingerPrint = new LinkedList<>(data_fingerPrint.keySet());
                        List<Object> fingerInfo = new ArrayList<>();
                        for (String finger : listFingerPrint) {
                            Map<String, Object> map_detail = new HashMap<>();
                            map_detail.put("fingerprint", finger);
                            fingerInfo.add(map_detail);
                        }
                        String firstFingerPrint = listFingerPrint.get(0);
                        // 组装detail json数据
                        Map<String, Object> map_detail = new HashMap<>();
                        map_detail.put("fingerInfo", fingerInfo);
                        map_detail.put("description", "检测到同一会话标识下出现多个终端指纹信息,疑似伪造指纹攻击行为");
                        String detailJson = JSON.toJSONString(map_detail);
                        DataOperateInfoExt firstData = getFirstData(firstFingerPrint, appId, rule, null, null, null, entry_session.getKey(), SortOrder.ASC);
                        DataOperateInfoExt endData = getFirstData(firstFingerPrint, appId, rule, null, null, null, entry_session.getKey(), SortOrder.DESC);
                        ThreatWarning warning = getWarning(rule, appId, firstFingerPrint, firstData.getReq_ip(), firstData.getAuth_id(), firstData.getDataId()
                                , DateUtil.getDate(firstData.getStarttime()), DateUtil.getDate(endData.getEndtime()), detailJson, true);
                        warnings.add(warning);
                    }
                }
            }
            MySqlUtil.batchInsert(warnings);
        } catch (Exception e) {
            logger.error("[指纹伪造策略]分析异常", e);
        }
    }

    /**
     * 查询Sql纹是否是查询语句,返回参数化后数据
     *
     * @param sql_hash
     * @return
     */
    private String checkSqlIsSelect(String sql_hash, String[] operates) {
        String parameterizedSql = "";
        try {
            BoolQueryBuilder queryBuilder = QueryBuilders.boolQuery();
            queryBuilder.must(QueryBuilders.termQuery("hash", sql_hash));
            queryBuilder.must(QueryBuilders.termsQuery("operates", operates));
            EsRestSearchArgs args = new EsRestSearchArgs(new String[]{config.getEs_index_adm_sqlParse()}, queryBuilder);
            args.setIncludeFields(new String[]{"parameterizedSql"});
            EsRestSearchResult result = EsRestSearchUtil.search(args);
            List<String> records = result.getRecords();
            for (String record : records) {
                parameterizedSql = JsonUtil.readPath(record, "$.parameterizedSql");
            }
        } catch (Exception e) {
            logger.error("查询Sql纹异常.", e);
        }
        return parameterizedSql;
    }

    /**
     * 查询符合条件的sql指纹
     *
     * @param tableName
     * @param operates
     * @param table_operates
     * @return
     * @throws IOException
     */
    private List<String> getListSqlHash(String tableName, List<String> operates, List<String> table_operates, List<String> functions) throws IOException {
        List<String> listSqlHash = new ArrayList<>();
        if (StringUtil.isEmpty(tableName)
                && (null == operates || operates.size() == 0)
                && (null == table_operates || table_operates.size() == 0)
                && (null == functions || functions.size() == 0)) {
            return listSqlHash;
        }
        BoolQueryBuilder queryBuilder = QueryBuilders.boolQuery();
        if (StringUtil.isNotEmpty(tableName)) {
            queryBuilder.must(QueryBuilders.termQuery("tables", tableName));
        }
        if (null != functions && functions.size() > 0) {
            queryBuilder.must(QueryBuilders.termsQuery("functions", functions));
        }
        if (null != operates && operates.size() > 0) {
            queryBuilder.must(QueryBuilders.termsQuery("operates", operates));
        }
        if (null != table_operates && table_operates.size() > 0) {
            queryBuilder.must(QueryBuilders.termsQuery("table_operates", table_operates));
        }
        EsRestSearchArgs args = new EsRestSearchArgs(new String[]{config.getEs_index_adm_sqlParse()}, queryBuilder);
        args.setIncludeFields(new String[]{"hash"});
        args.setSize(config.getEs_query_size());
        EsRestSearchResult result = EsRestSearchUtil.search(args);
        List<String> records = result.getRecords();
        for (String record : records) {
            listSqlHash.add(JsonUtil.readPath(record, "$.hash"));
        }
        return listSqlHash;
    }

    /**
     * 符合条件的第一条数据
     *
     * @param fingerPrint
     * @param appId
     * @throws IOException
     */
    private DataOperateInfoExt getFirstData(String fingerPrint
            , String appId
            , ThreatRules rule
            , List<String> listSqlHash
            , List<String> listDataBase
            , List<String> listReqId
            , String session_id
            , SortOrder order) throws IOException {
        BoolQueryBuilder queryBuilder = QueryBuilders.boolQuery();
        queryBuilder.must(QueryBuilders.rangeQuery("timestamp").gte(queryTimeStamp[0]).lt(queryTimeStamp[1]));
        queryBuilder.must(QueryBuilders.termQuery("fingerprint", fingerPrint));
        queryBuilder.must(QueryBuilders.termQuery("white_type", 0));
        queryBuilder.must(QueryBuilders.termQuery("appId", appId));
        queryBuilder.mustNot(QueryBuilders.termQuery("req_ip", ""));
        if (StringUtil.isNotEmpty(session_id)) {
            queryBuilder.must(QueryBuilders.termQuery("session_id", session_id));
        }
        if (listDataBase != null && listDataBase.size() > 0) {
            queryBuilder.must(QueryBuilders.termsQuery("pluginType", listDataBase));
        }
        if (listSqlHash != null && listSqlHash.size() > 0) {
            queryBuilder.must(QueryBuilders.termsQuery("sql_hash_analy", listSqlHash));
        }
        if (listReqId != null && listReqId.size() > 0) {
            queryBuilder.must(QueryBuilders.termsQuery("reqId", listReqId));
        }
        if (rule.getResultrows() > 0) {
            queryBuilder.must(QueryBuilders.rangeQuery("res_lines").gte(rule.getResultrows()));
        }
        if (rule.getExecutestate() != null) {
            queryBuilder.must(QueryBuilders.termQuery("res_status", rule.getExecutestate()));
        }
        EsRestSearchArgs args = new EsRestSearchArgs(new String[]{config.getEs_index_adm_operate()}, queryBuilder);
        args.setSize(1);
        args.setSort(SortBuilders.fieldSort("starttime").order(order));
        EsRestSearchResult result = EsRestSearchUtil.search(args);
        List<DataOperateInfoExt> records = result.getRecordsList(DataOperateInfoExt.class);
        if (records.size() == 1) {
            return records.get(0);
        }
        return null;
    }

    /**
     * 组装入库对象
     *
     * @param rule
     * @param fingerPrint
     * @param detailJson
     */
    private ThreatWarning getWarning(ThreatRules rule, String appId, String fingerPrint, String ip, String loginName
            , String dataId, Date startTime, Date endTime, String detailJson, Boolean attackState) {
        ThreatWarning warning = new ThreatWarning();
        warning.setRuleid(rule.getUid());
        warning.setAppid(appId);
        warning.setLevel(rule.getLevel());
        warning.setType(rule.getType());
        warning.setFingerprint(fingerPrint);
        if (StringUtil.isNotEmpty(ip)) {
            warning.setIp(ip);
            warning.setGeo(IPUtils.getIPDesc(ip));
        }
        if (StringUtil.isNotEmpty(dataId)) {
            warning.setDataid(dataId);
        }
        if (StringUtil.isNotEmpty(loginName)) {
            warning.setLoginname(loginName);
        }
        if (startTime != null) {
            warning.setStarttime(startTime);
        }
        if (endTime != null) {
            warning.setEndtime(endTime);
        }
        warning.setDetail(detailJson);
        warning.setAttackstate(attackState ? 1 : 0);
        warning.setAlarmcount(1);
        return warning;
    }

    /**
     * 解析聚合数据
     *
     * @param aggregations
     * @param aggType
     * @param aggName
     * @return
     */
    private Map<String, Object[]> getAggData(Aggregations aggregations, AggType aggType, String aggName) {
        Map<String, Object[]> mapData = new LinkedHashMap<>();
        if (aggregations != null) {
            Aggregation aggregation = aggregations.get(aggName);
            if (aggregation != null) {
                if (aggType.equals(AggType.terms)) {
                    ParsedTerms terms = (ParsedTerms) aggregation;
                    List<? extends Terms.Bucket> buckets = terms.getBuckets();
                    for (Terms.Bucket bucket : buckets) {
                        String key = bucket.getKey().toString();
                        long totalCount = bucket.getDocCount();
                        Aggregations bucketAggregations = bucket.getAggregations();
                        mapData.put(key, new Object[]{totalCount, bucketAggregations});
                    }
                } else if (aggType.equals(AggType.dateHistogram)) {
                    ParsedDateHistogram dateHistogram = (ParsedDateHistogram) aggregation;
                    List<? extends Histogram.Bucket> buckets = dateHistogram.getBuckets();
                    for (Histogram.Bucket bucket : buckets) {
                        long totalCount = bucket.getDocCount();
                        Aggregations bucketAggregations = bucket.getAggregations();
                        mapData.put(bucket.getKeyAsString(), new Object[]{totalCount, bucketAggregations});
                    }
                } else if (aggType.equals(AggType.cardinality)) {
                    ParsedCardinality cardinality = (ParsedCardinality) aggregation;
                    String key = cardinality.getName();
                    long totalCount = cardinality.getValue();
                    mapData.put(key, new Object[]{totalCount, null});
                } else if (aggType.equals(AggType.sum)) {
                    ParsedSum sum = (ParsedSum) aggregation;
                    String key = sum.getName();
                    double sumValue = sum.getValue();
                    mapData.put(key, new Object[]{sumValue, null});
                }
            }
        }
        return mapData;
    }

    /**
     * 聚合类型枚举
     */
    public enum AggType {
        terms, dateHistogram, cardinality, sum
    }
}