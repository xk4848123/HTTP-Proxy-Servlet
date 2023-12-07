package com.zary.sniffer.agent.plugin.jdbc;


import com.zary.sniffer.config.PluginConsts;
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
 * 拦截插件：Statement插件
 * <p>
 * 1.Statement是jdbc中执行非参数化查询器
 * 2.通过拦截excute相关函数解析数据库执行参数
 */
public class StatementPlugin extends AbstractPlugin {
    private static final String HANDLER = "com.zary.sniffer.agent.plugin.jdbc.handler.StatementHandler";

    @Override
    public ElementMatcher<TypeDescription> getPluginTypeMatcher() {
        return ElementMatchers.isSubTypeOf(java.sql.Statement.class)
                .and(ElementMatchers.not(ElementMatchers.isSubTypeOf(java.sql.PreparedStatement.class)))
                .and(ElementMatchers.not(ElementMatchers.isSubTypeOf(java.sql.CallableStatement.class)))
                .and(ElementMatchers.not(ElementMatchers.isInterface()))
                .and(ElementMatchers.not(ElementMatchers.<TypeDescription>isAbstract()))
                .and(ElementMatchers.not(ElementMatchers.<TypeDescription>nameStartsWith(PluginConsts.PKG_JAVA_SUN)))
                .and(ElementMatchers.not(ElementMatchers.<TypeDescription>nameStartsWith("com.alibaba.druid.")))
                .and(
                        ElementMatchers.<TypeDescription>nameStartsWith(JdbcUtil.NS_MYSQL)
                                .or(ElementMatchers.<TypeDescription>nameStartsWith(JdbcUtil.NS_ORACLE))
                                .or(ElementMatchers.<TypeDescription>nameStartsWith(JdbcUtil.NS_DB2))
                                .or(ElementMatchers.<TypeDescription>nameStartsWith(JdbcUtil.NS_SQL_SERVER))
                );
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
                return ElementMatchers.named("execute")
                        .or(ElementMatchers.<MethodDescription>named("executeQuery"))
                        .or(ElementMatchers.<MethodDescription>named("executeUpdate"))
                        .or(ElementMatchers.<MethodDescription>named("executeLargeUpdate"));
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