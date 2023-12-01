package com.zary.sniffer.server.utils;


import com.zary.sniffer.core.enums.EnumBlackWhiteType;
import com.zary.sniffer.util.DateUtil;
import redis.clients.jedis.Jedis;


/**
 * 系统Redis相关数据操作
 *
 * @author weiyi
 */
public class RedisCacheUtil {
    /**
     * 指纹白名单key(hash)
     */
    static final String KEY_ADM_FINGERPRINT_WHITE = "adm:fingerprint:white";
    /**
     * 指纹黑名单key(hash)
     */
    static final String KEY_ADM_FINGERPRINT_BLACK = "adm:fingerprint:black";
    /**
     * IP白名单key(hash)
     */
    static final String KEY_ADM_IP_WHITE = "adm:ip:white";
    /**
     * IP黑名单key(hash)
     */
    static final String KEY_ADM_IP_BLACK = "adm:ip:black";
    /**
     * sql白名单key(hash)
     */
    static final String KEY_ADM_SQL_WHITE = "adm:sql:white";
    /**
     * sql黑名单key(hash)
     */
    static final String KEY_ADM_SQL_BLACK = "adm:sql:black";
    /**
     * sql hash存储
     */
    static final String KEY_ADM_SQL_HASH = "adm:sql:hash";
    /**
     * 威胁告警相似计数key(hash)
     */
    static final String KEY_ADM_WARN_COUNT = "adm:warn:count";
    /**
     * 应用程序登录(hash) authurl,authkey,authtable
     */
    static final String KEY_ADM_APP_AUTH = "adm:app:auth";
    /**
     * 实例节点token
     */
    static final String KEY_ADM_NODE_TOKEN = "adm:node:token";
    /**
     * es索引名称
     */
    static final String KEY_ADM_ES_INDEX = "adm:es:index";
    /**
     * 首页统计
     */
    static final String KEY_ADM_HOME_STATISTICS = "adm:home:statistics";
    /**
     * 违规操作
     */
    static final String KEY_ADM_RULE_VIOLATION = "adm:rule:violation";

    /**
     * 安全关闭jedis
     *
     * @param jedis
     */
    private static void closeJedis(Jedis jedis) {
        try {
            if (jedis != null) {
                jedis.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 获取一个白名单缓存
     *
     * @return
     */
    public static String getWhiteList(String field, EnumBlackWhiteType type) {
        Jedis jedis = null;
        try {
            String value = "";
            jedis = RedisClientPoolUtil.getJedis();
            jedis.select(RedisClientPoolUtil.DBINDEX);
            if (EnumBlackWhiteType.fingerprint.equals(type)) {
                value = jedis.hget(KEY_ADM_FINGERPRINT_WHITE, field);
            } else if (EnumBlackWhiteType.ip.equals(type)) {
                value = jedis.hget(KEY_ADM_IP_WHITE, field);
            } else if (EnumBlackWhiteType.sql.equals(type)) {
                value = jedis.hget(KEY_ADM_SQL_WHITE, field);
            }
            return value;
        } catch (Exception e) {
            throw new RuntimeException("白名单缓存获取失败:" + field, e);
        } finally {
            closeJedis(jedis);
        }
    }

    /**
     * 设置一个白名单缓存
     *
     * @return
     */
    public static boolean setWhiteList(String field, String value, EnumBlackWhiteType type) {
        Jedis jedis = null;
        try {
            jedis = RedisClientPoolUtil.getJedis();
            jedis.select(RedisClientPoolUtil.DBINDEX);
            if (EnumBlackWhiteType.fingerprint.equals(type)) {
                jedis.hset(KEY_ADM_FINGERPRINT_WHITE, field, value);
            } else if (EnumBlackWhiteType.ip.equals(type)) {
                jedis.hset(KEY_ADM_IP_WHITE, field, value);
            } else if (EnumBlackWhiteType.sql.equals(type)) {
                jedis.hset(KEY_ADM_SQL_WHITE, field, value);
            }
            return true;
        } catch (Exception e) {
            throw new RuntimeException("白名单缓存失败" + field, e);
        } finally {
            closeJedis(jedis);
        }
    }

    /**
     * 移除一个白名单缓存
     *
     * @return
     */
    public static boolean deleteWhiteList(String field, EnumBlackWhiteType type) {
        Jedis jedis = null;
        try {
            jedis = RedisClientPoolUtil.getJedis();
            jedis.select(RedisClientPoolUtil.DBINDEX);
            if (EnumBlackWhiteType.fingerprint.equals(type)) {
                jedis.hdel(KEY_ADM_FINGERPRINT_WHITE, field);
            } else if (EnumBlackWhiteType.ip.equals(type)) {
                jedis.hdel(KEY_ADM_IP_WHITE, field);
            } else if (EnumBlackWhiteType.sql.equals(type)) {
                jedis.hdel(KEY_ADM_SQL_WHITE, field);
            }
            return true;
        } catch (Exception e) {
            throw new RuntimeException("白名单删除缓存失败" + field, e);
        } finally {
            closeJedis(jedis);
        }
    }

    /**
     * 检查是否存在白名单
     *
     * @return
     */
    public static boolean existWhiteList(String field, EnumBlackWhiteType type) {
        Jedis jedis = null;
        Boolean res = false;
        try {
            jedis = RedisClientPoolUtil.getJedis();
            jedis.select(RedisClientPoolUtil.DBINDEX);
            if (EnumBlackWhiteType.fingerprint.equals(type)) {
                res = jedis.hexists(KEY_ADM_FINGERPRINT_WHITE, field);
            } else if (EnumBlackWhiteType.ip.equals(type)) {
                res = jedis.hexists(KEY_ADM_IP_WHITE, field);
            } else if (EnumBlackWhiteType.sql.equals(type)) {
                res = jedis.hexists(KEY_ADM_SQL_WHITE, field);
            }
            return res;
        } catch (Exception e) {
            throw new RuntimeException("白名单检查失败" + field, e);
        } finally {
            closeJedis(jedis);
        }
    }

    /**
     * 检查Sql hash是否存在
     * 不存在则添加
     *
     * @param value
     * @return
     */
    public static boolean checkSqlHashList(String value) {
        Jedis jedis = null;
        try {
            jedis = RedisClientPoolUtil.getJedis();
            jedis.select(RedisClientPoolUtil.DBINDEX);
            if (jedis.sismember(KEY_ADM_SQL_HASH, value)) {
                return true;
            }
            jedis.sadd(KEY_ADM_SQL_HASH, value);
        } catch (Exception e) {
            throw new RuntimeException("检查SqlHash存在异常", e);
        } finally {
            closeJedis(jedis);
        }
        return false;
    }

    /**
     * 删除sql hash中的values
     * @param values
     * @return
     */
    public static void deleteSqlHashValues(String... values){
        if (values == null || values.length == 0){
            return;
        }
        Jedis jedis = null;
        try {
            jedis = RedisClientPoolUtil.getJedis();
            jedis.select(RedisClientPoolUtil.DBINDEX);
            jedis.srem(KEY_ADM_SQL_HASH, values);
        } catch (Exception e) {
            throw new RuntimeException("删除SqlHash中value出现异常", e);
        } finally {
            closeJedis(jedis);
        }
    }
    /**
     * 添加key对应account
     *
     * @param key
     * @param value
     * @return
     */
    public static boolean addAccount(String key, String value, Integer seconds) {
        Jedis jedis = null;
        try {
            jedis = RedisClientPoolUtil.getJedis();
            jedis.select(RedisClientPoolUtil.DBINDEX);
            jedis.set(key, value);
            if (seconds != null && seconds > 0)
                jedis.expire(key, seconds);
            return true;
        } catch (Exception e) {
            return false;
        } finally {
            closeJedis(jedis);
        }
    }

    /**
     * 获取key对应account
     *
     * @param key
     * @return
     */
    public static String getAccount(String key) {
        Jedis jedis = null;
        try {
            jedis = RedisClientPoolUtil.getJedis();
            jedis.select(RedisClientPoolUtil.DBINDEX);
            if (jedis.exists(key)) {
                return jedis.get(key);
            }
            return "";
        } catch (Exception e) {
            throw new RuntimeException("获取key对应Account异常", e);
        } finally {
            closeJedis(jedis);
        }
    }


    /**
     * 设置一个告警统计
     *
     * @return
     */
    public static boolean setWarnCountList(String field, String value) {
        Jedis jedis = null;
        try {
            jedis = RedisClientPoolUtil.getJedis();
            jedis.select(RedisClientPoolUtil.DBINDEX);
            jedis.hset(KEY_ADM_WARN_COUNT, field, value);
            jedis.expire(KEY_ADM_WARN_COUNT, DateUtil.getTodayRemainSecondNum());
            return true;
        } catch (Exception e) {
            // throw new RuntimeException("添加告警统计缓存失败" + field, e);
            return false;
        } finally {
            closeJedis(jedis);
        }
    }


    /**
     * 检查是否存在告警统计
     *
     * @return
     */
    public static boolean existWarnCountList(String field) {
        Jedis jedis = null;
        Boolean res;
        try {
            jedis = RedisClientPoolUtil.getJedis();
            jedis.select(RedisClientPoolUtil.DBINDEX);
            res = jedis.hexists(KEY_ADM_WARN_COUNT, field);
            return res;
        } catch (Exception e) {
            throw new RuntimeException("告警统计检查失败" + field, e);
        } finally {
            closeJedis(jedis);
        }
    }


    /**
     * 获取一个告警统计缓存
     *
     * @return
     */
    public static String getWarnCountList(String field) {
        return getHashValue(KEY_ADM_WARN_COUNT, field);
    }

    /**
     * 设置一个应用程序的账号相关缓存
     *
     * @return
     */
    public static boolean setAppAuth(String field, String value) {
        return setHashValue(KEY_ADM_APP_AUTH, field, value);
    }

    /**
     * 获取一个应用程序的账号相关缓存
     *
     * @return
     */
    public static String getAppAuth(String field) {
        return getHashValue(KEY_ADM_APP_AUTH, field);

    }

    /**
     * 移除一个应用程序的账号相关缓存
     *
     * @return
     */
    public static boolean deleteAppAuth(String field) {
        return deleteHashValue(KEY_ADM_APP_AUTH, field);
    }

    /**
     * 设置hset类型数据
     *
     * @param key
     * @param field
     * @param value
     * @return
     */
    public static boolean setHashValue(String key, String field, String value) {
        Jedis jedis = null;
        try {
            jedis = RedisClientPoolUtil.getJedis();
            jedis.select(RedisClientPoolUtil.DBINDEX);
            jedis.hset(key, field, value);
            return true;
        } catch (Exception e) {
            throw new RuntimeException("设置缓存失败" + field, e);
        } finally {
            closeJedis(jedis);
        }
    }

    /**
     * 获取hset类型数据
     *
     * @param key
     * @param field
     * @return
     */
    public static String getHashValue(String key, String field) {
        Jedis jedis = null;
        try {
            String value;
            jedis = RedisClientPoolUtil.getJedis();
            jedis.select(RedisClientPoolUtil.DBINDEX);
            value = jedis.hget(key, field);
            return value;
        } catch (Exception e) {
            return null;
        } finally {
            closeJedis(jedis);
        }
    }

    /**
     * 删除hset
     *
     * @param key
     * @param field
     * @return
     */
    public static boolean deleteHashValue(String key, String field) {
        Jedis jedis = null;
        try {
            jedis = RedisClientPoolUtil.getJedis();
            jedis.select(RedisClientPoolUtil.DBINDEX);
            jedis.hdel(key, field);
            return true;
        } catch (Exception e) {
            throw new RuntimeException("删除缓存失败" + field, e);
        } finally {
            closeJedis(jedis);
        }
    }


    /**
     * 设置节点的token缓存
     *
     * @return
     */
    public static boolean setNodeToken(String... token) {
        Jedis jedis = null;
        try {
            jedis = RedisClientPoolUtil.getJedis();
            jedis.select(RedisClientPoolUtil.DBINDEX);
            jedis.sadd(KEY_ADM_NODE_TOKEN, token);
            return true;
        } catch (Exception e) {
            throw new RuntimeException("实例节点相关缓存失败" + token, e);
        } finally {
            closeJedis(jedis);
        }
    }


    /**
     * 判断数据是否存在redis缓存中
     */
    public static boolean isExistNodeToken(String token) {
        Jedis jedis = null;
        try {
            jedis = RedisClientPoolUtil.getJedis();
            jedis.select(RedisClientPoolUtil.DBINDEX);
            return jedis.sismember(KEY_ADM_NODE_TOKEN, token);
        } catch (Exception e) {
            throw new RuntimeException("实例节点相关缓存异常" + token, e);
        } finally {
            closeJedis(jedis);
        }
    }

    /**
     * 移除一个应用程序的账号相关缓存
     *
     * @return
     */
    public static boolean deleteNodeToken(String token) {
        Jedis jedis = null;
        try {
            jedis = RedisClientPoolUtil.getJedis();
            jedis.select(RedisClientPoolUtil.DBINDEX);
            jedis.srem(KEY_ADM_NODE_TOKEN, token);
            return true;
        } catch (Exception e) {
            throw new RuntimeException("实例节点相关缓存删除失败" + token, e);
        } finally {
            closeJedis(jedis);
        }
    }

    /**
     * 设置ES索引分类对应名称
     *
     * @return
     */
    public static boolean setESIndex(String field, String value) {
        return setHashValue(KEY_ADM_ES_INDEX, field, value);
    }

    /**
     * 获取ES索引分类对应名称
     *
     * @return
     */
    public static String getESIndex(String field) {
        return getHashValue(KEY_ADM_ES_INDEX, field);

    }

    /**
     * 设置首页统计数值
     *
     * @return
     */
    public static boolean setHomeStatistics(String field, String value) {
        return setHashValue(KEY_ADM_HOME_STATISTICS, field, value);
    }

    /**
     * 获取首页统计数值
     *
     * @return
     */
    public static String getHomeStatistics(String field) {
        return getHashValue(KEY_ADM_HOME_STATISTICS, field);

    }

    /**
     * 设置违规操作
     *
     * @param field
     * @return
     */
    public static boolean setRuleViolation(String field, String value) {
        return setHashValue(KEY_ADM_RULE_VIOLATION, field, value);
    }

    /**
     * 获取违规操作
     *
     * @param field
     * @return
     */
    public static String getRuleViolation(String field) {
        return getHashValue(KEY_ADM_RULE_VIOLATION, field);
    }

    /**
     * 删除违规操作
     *
     * @param field
     * @return
     */
    public static boolean deleteRuleViolation(String field) {
        return deleteHashValue(KEY_ADM_RULE_VIOLATION, field);
    }


    /**
     * 判断数据是否存在redis缓存中
     */
    public static boolean isExistRuleViolation(String field) {
        Jedis jedis = null;
        try {
            jedis = RedisClientPoolUtil.getJedis();
            jedis.select(RedisClientPoolUtil.DBINDEX);
            return jedis.hexists(KEY_ADM_RULE_VIOLATION, field);
        } catch (Exception e) {
            throw new RuntimeException("相关缓存异常" + field, e);
        } finally {
            closeJedis(jedis);
        }
    }
    /**
     * 检查redis是否正常启动
     *
     * @return
     */
    public static boolean checkRedis() {
        Jedis jedis = null;
        try {
            jedis = RedisClientPoolUtil.getJedis();
            jedis.select(RedisClientPoolUtil.DBINDEX);
            return true;
        } catch (Exception e) {
            throw new RuntimeException("redis存在异常.", e);
        } finally {
            closeJedis(jedis);
        }
    }
}
