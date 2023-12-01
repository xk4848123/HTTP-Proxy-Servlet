package com.zary.sniffer.agent.plugin.jdbc;

import com.zary.sniffer.agent.core.plugin.AbstractPlugin;
import com.zary.sniffer.agent.core.plugin.point.IConstructorPoint;
import com.zary.sniffer.agent.core.plugin.point.IInstanceMethodPoint;
import com.zary.sniffer.agent.core.plugin.point.IStaticMethodPoint;
import com.zary.sniffer.agent.plugin.jdbc.util.JdbcUtil;

import net.bytebuddy.description.method.MethodDescription;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.matcher.ElementMatcher;
import net.bytebuddy.matcher.ElementMatchers;

/**
 * 拦截插件：ResultSet插件
 * <p>
 * 1.Result是jdbc中执行sql查询返回的结果集合，支持游标操作遍历数据
 * 2.通过拦截next函数监控对象实际读取到的数据
 */
public class ResultSetPlugin extends AbstractPlugin {
    private static final String HANDLER = "com.zary.sniffer.agent.plugin.jdbc.handler.ResultSetHandler";

    @Override
    public ElementMatcher<TypeDescription> getPluginTypeMatcher() {
        return ElementMatchers.isSubTypeOf(java.sql.ResultSet.class)
                .and(ElementMatchers.not(ElementMatchers.isInterface()))
                .and(getResultSetTypeMatcherMysql()
                        .or(getResultSetTypeMatcherOracle())
                        .or(getResultSetTypeMatcherDB2())
                        .or(getResultSetTypeMatcherSqlServer())
                );
    }


    /**
     * Mysql不同版本next()所在的ResultSet类
     * 5.0.2		com.mysql.jdbc.ResultSet.next()
     * 5.1.1		com.mysql.jdbc.ResultSetImpl.next()
     * 5.1.37		com.mysql.jdbc.ResultSetImpl.next()
     * 6.0.2		com.mysql.cj.jdbc.ResultSetImpl.next()
     * 6.0.3		com.mysql.cj.jdbc.result.ResultSetImpl.next()
     * 6.0.6		com.mysql.cj.jdbc.result.ResultSetImpl.next()
     * 8.0.11		com.mysql.cj.jdbc.result.ResultSetImpl.next()
     * 8.0.7-dmr	com.mysql.cj.jdbc.result.ResultSetImpl.next()
     * 8.0.19		com.mysql.cj.jdbc.result.ResultSetImpl.next()
     * 规则：命名空间 & 非抽象类 & 名称
     *
     * @return
     */
    private ElementMatcher.Junction<TypeDescription> getResultSetTypeMatcherMysql() {
        return ElementMatchers.<TypeDescription>nameStartsWith(JdbcUtil.NS_MYSQL)
                .and(ElementMatchers.not(ElementMatchers.<TypeDescription>isAbstract()))
                .and(ElementMatchers.<TypeDescription>named("com.mysql.jdbc.ResultSet")
                        .or(ElementMatchers.<TypeDescription>named("com.mysql.jdbc.ResultSetImpl"))
                        .or(ElementMatchers.<TypeDescription>named("com.mysql.cj.jdbc.ResultSetImpl"))
                        .or(ElementMatchers.<TypeDescription>named("com.mysql.cj.jdbc.result.ResultSetImpl"))
                );
    }

    /**
     * Oracle不同版本next()所在的ResultSet类
     * abstract class GeneratedResultSet
     * ---- abstract class OracleResultSet
     * -------- abstract class GeneratedScrollableResultSet
     * ------------  class InsensitiveScrollableResultSet.next()
     * ---- abstract class GeneratedUpdatableResultSet
     * -------- class UpdatableResultSet.next()
     * ---- abstract class BaseResultSet
     * -------- class OracleResultSetImpl.next()
     * -------- class ScrollableResultSet.next()
     * -------- class ArrayDataResultSet.next()
     * ---- class OldUpdatableResultSet.next()
     * 规则：命名空间 & 非抽象类 & 名称
     *
     * @return
     */
    private ElementMatcher.Junction<TypeDescription> getResultSetTypeMatcherOracle() {
        return ElementMatchers.<TypeDescription>nameStartsWith(JdbcUtil.NS_ORACLE)
                .and(ElementMatchers.not(ElementMatchers.<TypeDescription>isAbstract()))
                .and(ElementMatchers.<TypeDescription>named("oracle.jdbc.driver.InsensitiveScrollableResultSet")
                        .or(ElementMatchers.<TypeDescription>named("oracle.jdbc.driver.UpdatableResultSet"))
                        .or(ElementMatchers.<TypeDescription>named("oracle.jdbc.driver.OracleResultSetImpl"))
                        .or(ElementMatchers.<TypeDescription>named("oracle.jdbc.driver.ScrollableResultSet"))
                        .or(ElementMatchers.<TypeDescription>named("oracle.jdbc.driver.ArrayDataResultSet"))
                        .or(ElementMatchers.<TypeDescription>named("oracle.jdbc.driver.OldUpdatableResultSet"))
                );
    }


    /**
     * DB2中包含实现接口ResultSet中next()方法的类
     * db2jcc4-4.23.42.jar	com.ibm.db2.jcc.am.eb	com.ibm.db2.jcc.am.ResultSet	com.ibm.db2.jcc.am.ig   调用 Result.next
     * db2jcc4-4.9.78.jar	com.ibm.db2.jcc.am.cm	com.ibm.db2.jcc.am.fb	com.ibm.db2.jcc.am.nf 调用cm中的next
     * db2jcc4-10.1.jar	com.ibm.db2.jcc.am.eb	com.ibm.db2.jcc.am.bo	com.ibm.db2.jcc.am.xf调用bo中的next
     * db2jcc.jar	com.ibm.db2.jcc.am.ResultSet	com.ibm.db2.jcc.am.db	com.ibm.db2.jcc.am.db.cj调用ResultSet的next
     * db2jcc4.jar	com.ibm.db2.jcc.am.ResultSet	com.ibm.db2.jcc.am.eb	com.ibm.db2.jcc.am.hg调用result
     *
     * @return
     */
    private ElementMatcher.Junction<TypeDescription> getResultSetTypeMatcherDB2() {
        return ElementMatchers.<TypeDescription>nameStartsWith(JdbcUtil.NS_DB2)
                .and(ElementMatchers.<TypeDescription>named("com.ibm.db2.jcc.am.ResultSet")
                        .or(ElementMatchers.<TypeDescription>named("com.ibm.db2.jcc.am.fb"))
                        .or(ElementMatchers.<TypeDescription>named("com.ibm.db2.jcc.am.eb"))
                        .or(ElementMatchers.<TypeDescription>named("com.ibm.db2.jcc.am.db"))
                        .or(ElementMatchers.<TypeDescription>named("com.ibm.db2.jcc.am.cm"))
                        .or(ElementMatchers.<TypeDescription>named("com.ibm.db2.jcc.am.bo"))
                );
    }


    /**
     * sql server
     * com.microsoft.sqlserver » mssql-jdbc » 6.1.0.jre7-8.3.0.jre14-preview
     * com.microsoft.sqlserver » sqljdbc4 » 4.0
     *
     * @return
     */
    private ElementMatcher<? super TypeDescription> getResultSetTypeMatcherSqlServer() {
        return ElementMatchers.<TypeDescription>nameStartsWith(JdbcUtil.NS_SQL_SERVER)
                .and(ElementMatchers.<TypeDescription>named("com.microsoft.sqlserver.jdbc.SQLServerResultSet"));
    }


    @Override
    public IConstructorPoint[] getConstructorPoints() {
        return new IConstructorPoint[0];
    }

    @Override
    public IInstanceMethodPoint[] getInstanceMethodPoints() {
        IInstanceMethodPoint point = new IInstanceMethodPoint() {
            @Override
            public ElementMatcher<MethodDescription> getMethodsMatcher() {
                return ElementMatchers.named("next");
            }

            @Override
            public String getHandlerClassName() {
                return HANDLER;
            }

            @Override
            public boolean isMorphArgs() {
                return false;
            }
        };
        return new IInstanceMethodPoint[]{point};
    }

    @Override
    public IStaticMethodPoint[] getStaticMethodPoints() {
        return new IStaticMethodPoint[0];
    }
}
