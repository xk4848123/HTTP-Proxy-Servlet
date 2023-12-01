package com.zary.sniffer.server.mysql;

import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.DbPro;
import com.jfinal.plugin.activerecord.Record;
import com.zary.sniffer.server.model.ThreatRules;
import com.zary.sniffer.server.model.ThreatWarning;
import com.zary.sniffer.core.enums.ThreatPromptType;
import com.zx.lib.utils.DateUtil;
import com.zx.lib.utils.LogUtil;
import com.zx.lib.utils.StringUtil;
import org.slf4j.Logger;

import java.util.*;

/**
 * 操作mysql
 */
public class MySqlUtil {
    /**
     * 日志对象
     */
    static Logger logger = LogUtil.getLogger(MySqlUtil.class);
    /**
     * mysql
     */
    private static final DbPro db = Db.use("MYSQL");

    /**
     * 检查数据库链接
     *
     * @return
     */
    public static boolean checkDataBaseConnect() {
        try {
            String selSql = "select 1";
            List<Record> records = db.find(selSql);
            if (records != null && records.size() > 0) {
                return true;
            } else {
                logger.error("数据库链接失败.");
                return false;
            }
        } catch (Exception e) {
            logger.error("数据库链接异常.", e);
            return false;
        }

    }

    /**
     * 获取Appid
     *
     * @return
     */
    public static List<String> getAppIds() {
        List<String> listAppId = new ArrayList<>();
        try {
            String selSql = "select uid from core_apps where state = 0";
            List<Record> records = db.find(selSql);
            for (Record record : records) {
                String uid = record.getStr("uid");
                listAppId.add(uid);
            }
        } catch (Exception e) {
            logger.error("读取App信息表失败.", e);
        }
        return listAppId;
    }

    /**
     * 获取行为分析规则
     *
     * @return
     */
    public static List<ThreatRules> getRules(String selSql) {
        List<ThreatRules> rulesList = new ArrayList<>();
        try {
            List<Record> records = db.find(selSql);
            for (Record record : records) {
                ThreatRules rule = new ThreatRules();
                rule.setUid(record.getStr("uid"));
                rule.setName(record.getStr("name"));
                rule.setFingerprint(record.getStr("fingerprint"));
                rule.setDescription(record.getStr("description"));
                rule.setLevel(record.getInt("level") == null ? 0 : record.getInt("level"));
                rule.setState(record.getInt("state"));
                rule.setType(record.getInt("type") == null ? 0 : record.getInt("type"));
                rule.setTypename(ThreatPromptType.getName(rule.getType()));
                rule.setIp(record.getStr("ip"));
                rule.setLoginname(record.getStr("loginname"));
                rule.setAppids(record.getStr("appids"));
                rule.setListAppId((StringUtil.isNotEmpty(rule.getAppids())) ? Arrays.asList(rule.getAppids().split(",")) : null);
                rule.setActions(record.getStr("actions"));
                rule.setListOperate(rule.getOperates(rule.getActions()));
                rule.setTables(record.getStr("tables"));
                rule.setMultiTable(StringUtil.isNotEmpty(rule.getTables()) ? rule.getTables().replace("，", ",").split(",") : null);
                rule.setResultrows(record.getInt("resultrows") == null ? 0 : record.getInt("resultrows"));
                Integer executeState = record.getInt("executestate");
                rule.setExecutestate((executeState == null || executeState == 0) ? null : (executeState == 1 ? true : false));
                rule.setSamesqlcount(record.getInt("samesqlcount") == null ? 0 : record.getInt("samesqlcount"));
                rule.setIpcount(record.getInt("ipcount") == null ? 0 : record.getInt("ipcount"));
                rule.setRatecount(record.getInt("ratecount") == null ? 0 : record.getInt("ratecount"));
                rule.setIntervaltime(record.getInt("intervaltime") == null ? 1 : record.getInt("intervaltime"));
                rule.setUrl(record.getStr("url"));
                rule.setExt(record.getStr("ext"));
                rulesList.add(rule);
            }
        } catch (Exception e) {
            logger.error("数据库读取失败.", e);
        }
        return rulesList;
    }

    /**
     * 获取所有存活的策略
     * @return
     */
    public static List<ThreatRules> getAllLiveRules(){
        String selSql = "select * from threat_rules where state = 0 and type != 999";
        return getRules(selSql);
    }

    /**
     * 获取违规操作策略(包括禁用和启用)
     * @return
     */
    public static List<ThreatRules> getRules_999(){
        String selSql = "select * from threat_rules where type = 999";
        return getRules(selSql);
    }

    /**
     * 告警信息插入
     *
     * @return
     */
    public static String insertThreatWarning(ThreatWarning warning) {
        try {
            String uid = UUID.randomUUID().toString().replaceAll("-", "");
            Record record = new Record();
            record.set("uid", uid);
            record.set("appid", warning.getAppid());
            record.set("fingerprint", warning.getFingerprint());
            record.set("ip", warning.getIp());
            record.set("loginname", warning.getLoginname());
            record.set("geo", warning.getGeo());
            record.set("dataid", warning.getDataid());
            record.set("level", warning.getLevel());
            record.set("type", warning.getType());
            record.set("attackstate", warning.getAttackstate());
            record.set("state", warning.getState());
            record.set("starttime", warning.getStarttime());
            record.set("endtime", warning.getEndtime());
            record.set("createtime", DateUtil.getDateNow());
            record.set("detail", warning.getDetail());
            record.set("ruleid", warning.getRuleid());
            record.set("message", warning.getMessage());
            record.set("alarmcount", warning.getAlarmcount());
            db.save("threat_warning", record);
            return uid;
        } catch (Exception ex) {
            ex.printStackTrace();
            return null;
        }
    }

    /**
     * 告警信息插入
     *
     * @return
     */
    public static boolean updateThreatWarning(String uid) {
        try {
            String date = DateUtil.toDateString(DateUtil.getDateNow(), "yyyy-MM-dd HH:mm:ss");
            db.update("UPDATE `threat_warning`\n" +
                    "SET endtime = '" + date + "',\n" +
                    " alarmcount = IFNULL(alarmcount, 0) + 1\n" +
                    "WHERE uid = '" + uid + "'");
            return true;
        } catch (Exception ex) {
            ex.printStackTrace();
            return false;
        }
    }

    /**
     * 批量写入
     *
     * @param warnings
     * @return
     */
    public static boolean batchInsert(List<ThreatWarning> warnings) {
        try {
            List<Record> records = new ArrayList<>();
            for (ThreatWarning warning : warnings) {
                Record record = new Record();
                record.set("uid", UUID.randomUUID().toString().replaceAll("-", ""));
                record.set("appid", warning.getAppid());
                record.set("fingerprint", warning.getFingerprint());
                record.set("ip", warning.getIp());
                record.set("loginname", warning.getLoginname());
                record.set("geo", warning.getGeo());
                record.set("dataid", warning.getDataid());
                record.set("level", warning.getLevel());
                record.set("type", warning.getType());
                record.set("attackstate", warning.getAttackstate());
                record.set("state", warning.getState());
                record.set("starttime", warning.getStarttime());
                record.set("endtime", warning.getEndtime());
                record.set("detail", warning.getDetail());
                record.set("ruleid", warning.getRuleid());
                record.set("message", warning.getMessage());
                record.set("alarmcount", warning.getAlarmcount());
                record.set("createtime", DateUtil.getDateNow());
                records.add(record);
                if (records.size() >= 1000) {
                    db.batchSave("threat_warning", records, records.size());
                    records.clear();
                }
            }
            if (records.size() > 0) {
                db.batchSave("threat_warning", records, records.size());
            }
            return true;
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return false;
    }
}
