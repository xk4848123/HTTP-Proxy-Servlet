package com.zary.sniffer.agent.plugin.jdbc.handler;

import com.zary.sniffer.core.model.WebRequestInfo;
import com.zary.sniffer.agent.core.log.LogProducer;
import com.zary.sniffer.agent.core.log.LogUtil;
import com.zary.sniffer.agent.core.plugin.define.HandlerBeforeResult;
import com.zary.sniffer.agent.core.plugin.handler.IInstanceMethodHandler;
import com.zary.sniffer.agent.plugin.jdbc.util.JdbcUtil;
import com.zary.sniffer.core.enums.PluginType;

import com.zary.sniffer.core.model.DataOperateInfo;
import com.zary.sniffer.agent.runtime.ThreadDataUtil;
import com.zary.sniffer.tracing.TracerManager;
import com.zary.sniffer.util.DateUtil;
import com.zary.sniffer.util.StringUtil;


import java.lang.reflect.Method;
import java.sql.CallableStatement;
import java.util.List;

/**
 * 拦截处理器：CallableStatement
 * <p>
 * 1.before：创建Span对象
 * 2.after：提取数据库操作信息及结果，更新到线程缓存数据
 */
public class CallableStatementHandler implements IInstanceMethodHandler {
    private static final LogProducer logger = LogUtil.getLogProducer();

    @Override
    public void onBefore(Object instance, Method method, Object[] allArguments, HandlerBeforeResult result) throws Throwable {
        /** 1.锁处理：阻止父类execute继续拦截*/
        TracerManager.getCurTracer().fillOtherThreadData(JdbcUtil.LOCK_CALLABLE_STATEMENT, true);

        /** 2.通过命名空间识别pluginType */
        String className = instance.getClass().getName();
        PluginType pluginType = JdbcUtil.getJdbcPluginType(className);
        if (pluginType == PluginType.unknown) {
            logger.debug("IGNORE_BEFORE:CallableStatementHandler:no pluginType", "instance:" + instance);
            return;
        }

        WebRequestInfo reqInfo = TracerManager.getCurTracer().acquireOtherThreadData(WebRequestInfo.IDENTITY);
        if (reqInfo == null) {
            logger.debug("IGNORE_BEFORE:CallableStatementHandler:no webRequestInfo", "");
            return;
        }
        /** 4.Span入栈：该span在after中同条件移除 */
        ThreadDataUtil.createSpan(pluginType);

        /** 5.创建一个空数据对象 */
        ThreadDataUtil.createDataOperate(reqInfo,pluginType);
    }

    @Override
    public Object onAfter(Object instance, Method method, Object[] allArguments, Object returnValue) throws Throwable {
        /** 1.锁处理：阻止父类execute继续拦截*/
        TracerManager.getCurTracer().removeOtherThreadData(JdbcUtil.LOCK_CALLABLE_STATEMENT);

        /** 2.通过命名空间识别pluginType */
        String className = instance.getClass().getName();
        PluginType pluginType = JdbcUtil.getJdbcPluginType(className);
        if (pluginType == PluginType.unknown) {
            logger.debug("IGNORE_AFTER:CallableStatementHandler:no pluginType", "instance:" + instance);
            return returnValue;
        }

        WebRequestInfo reqInfo = TracerManager.getCurTracer().acquireOtherThreadData(WebRequestInfo.IDENTITY);
        /** 3.Span出栈：与before同条件对应 */
        ThreadDataUtil.popSpan(reqInfo, instance, method, allArguments, "CallableStatementPlugin");

        /** 4.填充数据操作信息详情 */
        DataOperateInfo info = TracerManager.getCurTracer().acquireOtherThreadData(DataOperateInfo.IDENTITY);
        if (info == null) {
            logger.debug("IGNORE_AFTER:CallableStatementHandler:no dataOperateInfo", "");
            return returnValue;
        }
        pluginType = info.getPluginType();
        //datasource:通过Connection获取
        info.setDataSource(JdbcUtil.getConnectionSourceInfo((CallableStatement) instance));
        //sql: 通过反射调用内部函数获取
        String sql = JdbcUtil.getPreparedStatementSql(pluginType, instance);
        if (StringUtil.isEmpty(sql)) {
            logger.debug("IGNORE_AFTER:CallableStatementHandler:no sql", "");
            return returnValue;
        }
        info.setSql(sql);
        //results
        JdbcUtil.fillStatementResults(info, instance, returnValue);

        /** 6.填充执行时间 */
        long curTime = DateUtil.getNowTimestamp();
        info.setEndtime(curTime);
        info.setCost(curTime - info.getStarttime());

        List<DataOperateInfo> dataOperateInfos = TracerManager.getCurTracer().acquireOtherThreadData(DataOperateInfo.LIST_IDENTITY);
        dataOperateInfos.add(info);
        TracerManager.getCurTracer().removeOtherThreadData(DataOperateInfo.IDENTITY);
        return returnValue;
    }
}
