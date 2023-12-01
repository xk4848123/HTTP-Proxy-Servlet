/*
 * Copyright (c) 2016 Javaranger.com. All Rights Reserved.
 */
package com.zary.sniffer.server.mysql;


import com.alibaba.druid.filter.stat.StatFilter;
import com.alibaba.druid.wall.WallFilter;
import com.jfinal.plugin.activerecord.ActiveRecordPlugin;
import com.jfinal.plugin.activerecord.dialect.MysqlDialect;
import com.jfinal.plugin.druid.DruidPlugin;
import com.zx.lib.utils.LogUtil;
import org.slf4j.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class DB_MYSQL {

    /**
     * 日志对象
     */
    static Logger log = LogUtil.getLogger(DB_MYSQL.class);

    public static final Properties JDBC_PRO = new Properties();

    public static DB_MYSQL mysql = getDB_MYSQL();

    private DB_MYSQL() {
    }

    /**
     * 单例
     *
     * @return
     */
    private static DB_MYSQL getDB_MYSQL() {
        if (mysql == null) {
            mysql = new DB_MYSQL();
        }
        return mysql;
    }

    /**
     * 初始化数据链接配置文件
     *
     * @return
     * @throws IOException
     */
    private Boolean initJDBC() throws IOException {
        InputStream inStream_jdbc = null;
        try {
            inStream_jdbc = DB_MYSQL.class.getClassLoader().getResourceAsStream("jdbc.properties");
            JDBC_PRO.load(inStream_jdbc);
            return true;
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        } finally {
            inStream_jdbc.close();
        }
        return false;
    }

    public Boolean init(String jdbcUrl, String username, String password, String driver) {
        try {
            // 创建连接池
            DruidPlugin dpm = new DruidPlugin(jdbcUrl, username, password, driver);
            // 在链接池中增加过滤器防止sql注入
            dpm.addFilter(new StatFilter());
            WallFilter wm = new WallFilter();
            // 设置数据库类型
            wm.setDbType("mysql");
            dpm.addFilter(wm);
            // 处理事务实例
            ActiveRecordPlugin arpm = new ActiveRecordPlugin("MYSQL", dpm);
            arpm.setDialect(new MysqlDialect());
            arpm.setShowSql(true);
            log.info("Connecting database mysql Starting ...");
            // 启动连接池和事务处理
            if (!dpm.start() || !arpm.start()) {
                log.error("connect database mysql: {} fail.", jdbcUrl);
                return false;
            }
            log.info("connect mysql {} success", jdbcUrl);
            return true;
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        return false;
    }
}