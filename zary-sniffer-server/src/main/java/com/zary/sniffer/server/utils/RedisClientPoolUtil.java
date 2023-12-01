package com.zary.sniffer.server.utils;

import com.zary.sniffer.util.StringUtil;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

import java.io.IOException;
import java.util.Properties;

/**
 * Redis客户端连接池辅助类
 *
 * @author xulibo
 * @version 2019/9/2
 */
public class RedisClientPoolUtil {
    /**
     * 默认加载配置文件名
     */
    static String default_properties = "redis.properties";
    /**
     * 全局JedisPool
     */
    static JedisPool jedisPool = null;
    /**
     * 默认端口
     */
    static int default_port = 6379;
    /**
     * 默认活动连接数
     */
    static int default_max_active = 20;
    /**
     * 默认连接超时时间
     */
    static int default_max_wait = 5000;
    /**
     * 默认空闲最大值
     */
    static int default_max_idle = 20;
    /**
     * 默认空闲最小值
     */
    static int default_min_idle = 0;
    /**
     * 默认查询超时时间
     */
    static int default_timeout = 5000;
    /**
     * 默认开启连接借出时检查(ping)
     */
    static boolean default_test_on_borrow = true;
    /**
     * 默认redis存储位置
     */
    static int DBINDEX = 0;

    /**
     * 根据配置文件自动初始化
     */
    static {
        try {
            init(default_properties);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 初始化连接池(指定项目目录下的配置文件)
     */
    public static void init(String propertiesName) throws IOException {
        Properties prop = PropertiesUtil.getProperties(propertiesName);
        init(prop);
    }

    /**
     * 初始化连接池(指定外部配置)
     */
    public static void init(Properties properties) throws IOException {
        if (properties == null || properties.size() == 0){
            return;
        }
        String host = properties.getProperty("redis.host");
        int port = Integer.parseInt(properties.getProperty("redis.port"));
        String pwd = properties.getProperty("redis.password");
        pwd = StringUtil.isEmpty(pwd) ? null : pwd;
        int max_active = Integer.parseInt(properties.getProperty("redis.pool.maxactive"));
        int max_wait = Integer.parseInt(properties.getProperty("redis.pool.maxwait"));
        int max_idle = Integer.parseInt(properties.getProperty("redis.pool.maxidle"));
        int min_idle = Integer.parseInt(properties.getProperty("redis.pool.minidle"));
        int min_timeout = Integer.parseInt(properties.getProperty("redis.timeout"));
        boolean testOnBorrow = Integer.parseInt(properties.getProperty("redis.testonborrow")) > 0;
        DBINDEX = Integer.parseInt(properties.getProperty("redis.dbindex"));
        //初始化
        init(host, port, pwd, max_active, max_wait, max_idle, min_idle, min_timeout, testOnBorrow);
    }

    /**
     * 初始化连接池
     */
    public static void init(String host, int port) {
        init(host, port, null, default_max_active, default_max_wait, default_max_idle, default_min_idle, default_timeout, default_test_on_borrow);
    }

    /**
     * 初始化连接池
     */
    public static void init(String host, int port, String pwd) {
        init(host, port, pwd, default_max_active, default_max_wait, default_max_idle, default_min_idle, default_timeout, default_test_on_borrow);
    }

    /**
     * 初始化连接池
     */
    public static void init(String host, int port, String pwd, int max_active, int max_wait, int max_idle, int min_idle, int timeout, boolean testOnBorrow,int dbindex) {
        DBINDEX = dbindex;
        init(host, port, pwd, max_active, max_wait, max_idle, min_idle, timeout, testOnBorrow);
    }
    /**
     * 初始化连接池
     */
    public static void init(String host, int port, String pwd, int max_active, int max_wait, int max_idle, int min_idle, int timeout, boolean testOnBorrow) {
        try {
            JedisPoolConfig config = new JedisPoolConfig();
            config.setMaxTotal(max_active);
            config.setMaxWaitMillis(max_wait);
            config.setMaxIdle(max_idle);
            config.setMinIdle(min_idle);
            config.setTestOnBorrow(testOnBorrow);
            jedisPool = new JedisPool(config, host, port, timeout, pwd);
        } catch (Exception e) {
            new Exception("Jedis Pool init error", e);
        }
    }

    /**
     * 获取一个连接(使用后手工close())
     *
     * @return
     */
    public synchronized static Jedis getJedis() {
        if (jedisPool == null) {
            throw new NullPointerException("init jedis client pool first.");
        }
        return jedisPool.getResource();
    }

    /**
     * 归还一个连接
     *
     * @param jedis
     */
    public synchronized static void returnJedis(Jedis jedis) {
        try {
            //jedisPool.returnResource(jedis);
            jedis.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //public static void main(String[] args) {
    //    RedisClientPoolUtil.init("172.16.3.250", 16379, null, 20, 5000, 20, 0, 5000, true);
    //    Jedis jedis = RedisClientPoolUtil.getJedis();
    //    jedis.select(3);
    //
    //    //string
    //    System.out.println("--- string ---");
    //    System.out.println(jedis.set("str111","str111"));
    //    System.out.println(jedis.get("str111"));
    //    System.out.println(jedis.del("str111"));
    //    //list
    //    System.out.println("--- list ---");
    //    System.out.println(jedis.rpush("list111","a:b:c"));
    //    System.out.println(jedis.lindex("list111",0));
    //    System.out.println(jedis.del("list111"));
    //
    //    jedis.close();
    //}
}






