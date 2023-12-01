package com.zary.sniffer.agent.plugin.jdbc.util;

import com.zary.sniffer.core.enums.PluginType;
import com.zary.sniffer.core.model.DataOperateInfo;
import com.zary.sniffer.util.ReflectUtil;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.sql.*;

/**
 * jdbc辅助类
 */
public class JdbcUtil {
    /**
     * 命名空间：拦截处理类用于区分pluginType
     */
    public static final String NS_MYSQL = "com.mysql.";
    public static final String NS_ORACLE = "oracle.jdbc.";
    public static final String NS_DB2 = "com.ibm.db2.jcc.am";
    public static final String NS_SQL_SERVER = "com.microsoft.sqlserver.jdbc";

    /**
     * statement锁
     * 1.jdbc中statement的继承关系：CallableStatement -> PreparedStatement -> Statement
     * 2.子类的execute可能会调用父类的execute，如果不做处理，同一条语句可能被多重拦截
     * 3.在子类开始执行execute时在threadlocal中加锁通知父类拦截器不要处理
     */
    public static final String LOCK_CALLABLE_STATEMENT = "_lock_callable_statement";
    public static final String LOCK_PREPARED_STATEMENT = "_lock_prepared_statement";

    /**
     * 通过类名确定pluginType
     *
     * @param className
     * @return
     */
    public static PluginType getJdbcPluginType(String className) {
        if (className.startsWith(NS_MYSQL))
            return PluginType.mysql;
        if (className.startsWith(NS_ORACLE))
            return PluginType.oracle;
        if (className.startsWith(NS_DB2))
            return PluginType.db2;
        if (className.startsWith(NS_SQL_SERVER))
            return PluginType.sqlserver;
        return PluginType.unknown;
    }


    /**
     * 填充数据库执行结果:res_status, res_objects, res_sample, res_lines
     *
     * @param info
     * @param pluginType
     * @param statementInstance
     * @deprecated 修改为通过反射获取函数处理
     */
    @Deprecated
    public static void fillStatementResults(DataOperateInfo info, PluginType pluginType, Object statementInstance, Object returnValue) {
        //status
        info.setRes_status(null == returnValue ? false : true);
        //res_objects, res_sample, res_lines
        if (pluginType == PluginType.mysql)
            fillStatementResultMysql(info, statementInstance);
        else if (pluginType == PluginType.oracle)
            fillStatementResultOracle(info, statementInstance);
        else if (pluginType == PluginType.db2)
            fillStatementResultDB2(info, statementInstance);
    }

    /**
     * 填充数据库执行结果:res_status, res_objects, res_sample, res_lines
     *
     * @param info
     * @param statementInstance
     * @param returnValue
     */
    public static void fillStatementResults(DataOperateInfo info, Object statementInstance, Object returnValue) {
        //status
        info.setRes_status(null == returnValue ? false : true);
        //res_objects, res_sample
        Method methodResultSet = ReflectUtil.getDeclareMethodWithParent(statementInstance, "getResultSet");
        if (methodResultSet != null) {
            try {
                methodResultSet.setAccessible(true);
                ResultSet resultSet = (ResultSet) methodResultSet.invoke(statementInstance, null);
                if (resultSet != null) {
                    info.setRes_object(resultSet.toString());
                    info.setRes_sample(getResultSetSample(resultSet));
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        //res_lines
        Method methodUpdateCount = ReflectUtil.getDeclareMethodWithParent(statementInstance, "getUpdateCount");
        if (methodUpdateCount != null) {
            try {
                methodUpdateCount.setAccessible(true);
                Object objCount = methodUpdateCount.invoke(statementInstance, null);
                if (objCount != null) {
                    info.setRes_lines(Integer.parseInt(objCount.toString()));
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 填充Mysql执行结果:res_objects->results, res_lines->updateCount
     *
     * @param info
     * @param statementInstance
     */
    @Deprecated
    protected static void fillStatementResultMysql(DataOperateInfo info, Object statementInstance) {
        try {
            /**
             * Mysql各版本执行结果影响行数获取方式：
             * 5.0.2    com.mysql.jdbc.Statement -> ResultSet results -> long updateCount
             * 5.1.1    com.mysql.jdbc.StatementImpl -> ResultSetInternalMethods results -> long updateCount
             * 5.1.37   com.mysql.jdbc.StatementImpl -> ResultSetInternalMethods results -> long updateCount
             * 6.0.2    com.mysql.cj.jdbc.StatementImpl -> ResultSetInternalMethods results -> long updateCount
             * 8.0.9    com.mysql.cj.jdbc.StatementImpl -> ResultSetInternalMethods results -> long updateCount
             */
            Field fieldResult = ReflectUtil.getDeclareFieldWithParent(statementInstance, "results");
            if (fieldResult != null) {
                fieldResult.setAccessible(true);
                Object objResult = fieldResult.get(statementInstance);
                if (objResult != null) {
                    //填充res_object(对象名称，与ResultSetPlugin中统计行数的key相同)
                    info.setRes_object(objResult.toString());
                    //填充res_lines
                    Field filedCount = ReflectUtil.getDeclareFieldWithParent(objResult, "updateCount");
                    if (filedCount != null) {
                        filedCount.setAccessible(true);
                        Object objCount = filedCount.get(objResult);
                        if (objCount != null) {
                            info.setRes_lines(Integer.parseInt(objCount.toString()));
                        }
                    }
                    //填充sample
                    info.setRes_sample(getResultSetSample((ResultSet) objResult));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 填充Oracle执行结果:res_objects->currentResultSet, res_lines->validRows
     *
     * @param info
     * @param statementInstance
     */
    @Deprecated
    protected static void fillStatementResultOracle(DataOperateInfo info, Object statementInstance) {
        try {
            /**
             * Oracle各版本执行结果影响行数获取方式：
             * 5.11.x   oracle.jdbc.driver.OracleStatement -> int validRows / OracleResultSetImpl currentResultSet
             * 6.11.x   oracle.jdbc.driver.OracleStatement -> int validRows / OracleResultSetImpl currentResultSet
             * 7.12.x   oracle.jdbc.driver.OracleStatement -> int validRows / OracleResultSet currentResultSet
             * 8.12.x   oracle.jdbc.driver.OracleStatement -> int validRows / OracleResultSet currentResultSet
             * ...
             * 14.10.x   oracle.jdbc.driver.OracleStatement -> int validRows / OracleResultSetImpl currentResultSet
             */
            //填充res_object(对象名称，与ResultSetPlugin中统计行数的key相同)
            Field fieldResult = ReflectUtil.getDeclareFieldWithParent(statementInstance, "currentResultSet");
            if (fieldResult != null) {
                fieldResult.setAccessible(true);
                Object objResult = fieldResult.get(statementInstance);
                if (objResult != null) {
                    info.setRes_object(objResult.toString());
                    //填充sample
                    info.setRes_sample(getResultSetSample((ResultSet) objResult));
                }
            }
            //填充res_lines
            Field countField = ReflectUtil.getDeclareFieldWithParent(statementInstance, "validRows");
            if (countField != null) {
                countField.setAccessible(true);
                Object objCount = countField.get(statementInstance);
                if (objCount != null) {
                    info.setRes_lines(Integer.parseInt(objCount.toString()));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 填充DB2执行结果:res_objects->？, res_lines->o
     *
     * @param info
     * @param statementInstance
     */
    @Deprecated
    protected static void fillStatementResultDB2(DataOperateInfo info, Object statementInstance) {
        try {
            //填充res_lines
            Method methodCount = ReflectUtil.getDeclareMethodWithParent(statementInstance, "getUpdateCount");

            if (methodCount != null) {
                methodCount.setAccessible(true);
                Object objCount = methodCount.invoke(statementInstance);
                if (objCount != null) {
                    Integer res_lines = Integer.parseInt(objCount.toString());
                    info.setRes_lines(res_lines);
                }
            }
            Method methodResultSet = ReflectUtil.getDeclareMethodWithParent(statementInstance, "getResultSet");
            if (methodResultSet != null) {
                methodResultSet.setAccessible(true);
                Object objResult = methodResultSet.invoke(statementInstance);
                if (objResult != null) {
                    //填充res_object(对象名称，与ResultSetPlugin中统计行数的key相同)
                    info.setRes_object(objResult.toString());
                    //填充sample
                    info.setRes_sample(getResultSetSample((ResultSet) objResult));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }


    /**
     * 获取数据库url信息
     *
     * @param statement CallableStatement & PreparedStatement & Statement
     * @return
     */
    public static String getConnectionSourceInfo(Statement statement) {
        String source = "";
        try {
            if (statement == null) {
                return source;
            }
            Connection connection = statement.getConnection();
            if (connection == null || connection.getMetaData() == null) {
                return source;
            }
            DatabaseMetaData metadata = connection.getMetaData();
            if (metadata != null) {
                source = metadata.getURL();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return source;
    }

    /**
     * 获取preparedStatement对象的sql语句
     *
     * @param pluginType  数据库类型
     * @param pstInstance preparedStatement 或 callableStatement
     * @return
     */
    public static String getPreparedStatementSql(PluginType pluginType, Object pstInstance) {
        try {
            String targetMethod = "";
            String targetField = "";
            if (pluginType == PluginType.mysql) {
                //mysql preparedStatement.asSql()
                targetMethod = "asSql";
            } else if (pluginType == PluginType.oracle) {
                //oracle preparedStatement.getOriginalSql()
                targetMethod = "getOriginalSql";
            } else if (pluginType == PluginType.db2) {
                //db2
                targetMethod = "b";
            } else if (pluginType == PluginType.sqlserver) {
                //sqlCommand字段部分版本中包含
                targetField = "userSQL";
            }

            Method method = ReflectUtil.getDeclareMethodWithParent(pstInstance, targetMethod);
            if (method != null) {
                method.setAccessible(true);
                Object obj = method.invoke(pstInstance, null);
                return null == obj ? "" : obj.toString();
            }
            if (targetField != null) {
                Field sqlField = ReflectUtil.getDeclareFieldWithParent(pstInstance, targetField);
                if (sqlField != null) {
                    sqlField.setAccessible(true);
                    Object obj = sqlField.get(pstInstance);
                    return null == obj ? "" : obj.toString();
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }

    /**
     * 获取ResultSet结果描述
     *
     * @param resultSet
     * @return
     */
    public static String getResultSetSample(ResultSet resultSet) {
        try {
            StringBuilder sb = new StringBuilder();
            sb.append(resultSet.getClass().getName());
            ResultSetMetaData metaData = resultSet.getMetaData();
            int columnCount = metaData.getColumnCount();
            if (metaData != null) {
                sb.append("(");
                for (int i = 1; i <= columnCount; i++) {
                    try {
                        String colName = metaData.getColumnName(i);
                        sb.append(colName + ",");
                    } catch (Exception e) {
                        break;
                    }
                }
            }
            String res = sb.toString();
            if (res.endsWith(",")) {
                res = res.substring(0, res.length() - 1);
            }
            return res + ")";
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }
}
