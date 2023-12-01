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
import java.sql.Statement;
import java.util.List;

public class StatementHandler implements IInstanceMethodHandler {
    private static final LogProducer logger = LogUtil.getLogProducer();

    @Override
    public void onBefore(Object instance, Method method, Object[] allArguments, HandlerBeforeResult result) throws Throwable {
        /** 1.锁处理：阻止父类execute继续拦截*/
        if (TracerManager.getCurTracer().hasOtherThreadData(JdbcUtil.LOCK_CALLABLE_STATEMENT)) {
            return;
        }
        if (TracerManager.getCurTracer().hasOtherThreadData(JdbcUtil.LOCK_PREPARED_STATEMENT)) {
            return;
        }

        /** 2.通过命名空间识别pluginType */
        String className = instance.getClass().getName();
        PluginType pluginType = JdbcUtil.getJdbcPluginType(className);
        if (pluginType == PluginType.unknown) {
            logger.debug("IGNORE_BEFORE:StatementHandler:no pluginType", "instance:" + instance);
            return;
        }

        WebRequestInfo reqInfo = TracerManager.getCurTracer().acquireOtherThreadData(WebRequestInfo.IDENTITY);
        if (reqInfo == null) {
            logger.debug("IGNORE_BEFORE:StatementHandler:no webRequestInfo", "");
            return;
        }

        /** 4.Span入栈：该span在after中同条件移除 */
        ThreadDataUtil.createSpan(pluginType);

        /** 5.创建一个空数据对象 */
        ThreadDataUtil.createDataOperate(reqInfo, pluginType);
    }


    @Override
    public Object onAfter(Object instance, Method method, Object[] allArguments, Object returnValue) throws Throwable {
        /** 1.锁处理：阻止父类execute继续拦截*/
        if (TracerManager.getCurTracer().hasOtherThreadData(JdbcUtil.LOCK_CALLABLE_STATEMENT)) {
            return returnValue;
        }
        if (TracerManager.getCurTracer().hasOtherThreadData(JdbcUtil.LOCK_PREPARED_STATEMENT)) {
            return returnValue;
        }

        /** 2.通过命名空间识别pluginType */
        String className = instance.getClass().getName();
        PluginType pluginType = JdbcUtil.getJdbcPluginType(className);
        if (pluginType == PluginType.unknown) {
            logger.debug("IGNORE_AFTER:StatementHandler:no pluginType", "instance:" + instance);
            return returnValue;
        }

        WebRequestInfo reqInfo = TracerManager.getCurTracer().acquireOtherThreadData(WebRequestInfo.IDENTITY);
        if (reqInfo == null) {
            logger.debug("IGNORE_AFTER:PreparedStatementHandler:no webRequestInfo", "");
            return returnValue;
        }

        /** 3.Span出栈：与before同条件对应 */
        ThreadDataUtil.popSpan(reqInfo, instance, method, allArguments, "StatementPlugin");

        /** 4.填充数据操作信息详情 */
        DataOperateInfo info = TracerManager.getCurTracer().acquireOtherThreadData(DataOperateInfo.IDENTITY);
        if (info == null) {
            logger.debug("IGNORE_AFTER:StatementHandler:no dataOperateInfo", "");
            return returnValue;
        }
        //datasource:通过Connection获取
        info.setDataSource(JdbcUtil.getConnectionSourceInfo((Statement) instance));
        //sql:execute相关函数第一个参数
        String sql = (allArguments != null && allArguments.length > 0) ? allArguments[0] + "" : "";
        if (StringUtil.isEmpty(sql)) {
            logger.debug("IGNORE_AFTER:StatementHandler:no sql", "");
            return returnValue;
        }
        info.setSql(sql);
        //results
        JdbcUtil.fillStatementResults(info, instance, returnValue);

        /** 5.填充执行时间 */
        long curTime = DateUtil.getNowTimestamp();
        info.setEndtime(curTime);
        info.setCost(curTime - info.getStarttime());

        List<DataOperateInfo> dataOperateInfos = TracerManager.getCurTracer().acquireOtherThreadData(DataOperateInfo.LIST_IDENTITY);
        dataOperateInfos.add(info);
        TracerManager.getCurTracer().removeOtherThreadData(DataOperateInfo.IDENTITY);

        return returnValue;
    }
}
