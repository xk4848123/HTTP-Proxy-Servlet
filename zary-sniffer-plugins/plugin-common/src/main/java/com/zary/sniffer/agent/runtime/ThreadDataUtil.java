package com.zary.sniffer.agent.runtime;

import com.zary.sniffer.agent.core.consts.CoreConsts;
import com.zary.sniffer.config.PluginConsts;
import com.zary.sniffer.core.enums.DataOperateType;
import com.zary.sniffer.core.enums.PluginType;
import com.zary.sniffer.core.model.DataOperateInfo;
import com.zary.sniffer.core.model.SpanInfo;
import com.zary.sniffer.core.model.ThreadDataInfo;
import com.zary.sniffer.core.model.WebRequestInfo;
import com.zary.sniffer.config.ConfigCache;
import com.zary.sniffer.tracing.Span;
import com.zary.sniffer.tracing.Tracer;
import com.zary.sniffer.tracing.TracerManager;
import com.zary.sniffer.util.DateUtil;
import com.zary.sniffer.util.StringUtil;

import java.lang.reflect.Method;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;


/**
 * 线程数据容器类
 */
public class ThreadDataUtil {
    /**
     * 创建一个Span
     */
    public static void createSpan(PluginType pluginType) {
        if (!ConfigCache.get().isOpen_span()) {
            return;
        }
        Tracer curTracer = TracerManager.getCurTracer();
        Tracer.SpanBuilder spanBuilder = curTracer.buildSpan(CoreConsts.AGENT_TRACE_OPERATION_NAME);
        Span span = spanBuilder.start();
        span.setTag("pluginType", pluginType);
    }


    public static void popSpan(WebRequestInfo requestInfo, Object instance,
                               Method method, Object[] allArguments, String pluginName) {
        if (!ConfigCache.get().isOpen_span()) {
            return;
        }
        if (requestInfo == null) {
            return;
        }
        Tracer curTracer = TracerManager.getCurTracer();
        Span span = (Span) curTracer.activeSpan();

        span.setTag("appId", requestInfo.getAppId());
        span.setTag("reqId", requestInfo.getReqId());
        span.setTag("className", instance.getClass().getName());
        span.setTag("methodName", method.getName());
        span.setTag("methodArgs", StringUtil.join(allArguments, ','));
        span.setTag("pluginName", pluginName);
        span.finish();
    }

    /**
     * 创建数据操作对象
     */
    public static void createDataOperate(WebRequestInfo reqInfo, PluginType pluginType) {
        //创建对象
        DataOperateInfo info = new DataOperateInfo();
        info.setDataId(StringUtil.getGuid(false));
        info.setOperateType(DataOperateType.UNKNOWN);
        info.setStarttime(DateUtil.getNowTimestamp());
        info.setPluginType(pluginType);
        //填充请求ID、应用ID、指纹
        info.setAppId(reqInfo.getAppId());
        info.setReqId(reqInfo.getReqId());
        info.setSession_id(reqInfo.getSession_id());
        info.setFingerprint(reqInfo.getFingerprint());

        TracerManager.getCurTracer().fillOtherThreadData(DataOperateInfo.IDENTITY, info);
    }


    /**
     * 数据操作结果补充
     * 1.jdbc插件中通过反射statement的results下的指定字段来获取影响行数
     * 2.但是部分ResultSet取不到值，此时需要从ResultSetPlugin的计数器来辅助修正
     * 3.本函数在请求发送前遍历数据操作结果，修正影响行数
     *
     * @param dataOperateInfos
     */
    public static void fillDataOperateResultCount(List<DataOperateInfo> dataOperateInfos) {
        if (dataOperateInfos == null || dataOperateInfos.size() == 0) {
            return;
        }
        for (DataOperateInfo data : dataOperateInfos) {
            //如果resultset计数有效，优先使用
            try {
                String resObj = data.getRes_object();
                if (!StringUtil.isEmpty(resObj)) {
                    String reskey = PluginConsts.KEY_RESULT_SET + resObj;
                    if (TracerManager.getCurTracer().hasOtherThreadData(reskey)) {
                        String count = TracerManager.getCurTracer().acquireOtherThreadData(reskey).toString();
                        int cnt = Integer.parseInt(count);
                        if (cnt > 0) {
                            data.setRes_lines(Integer.parseInt(count));
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public static ThreadDataInfo newTreadDataInfoFromTracer() {
        ThreadDataInfo threadDataInfo = new ThreadDataInfo();

        WebRequestInfo reqInfo = TracerManager.getCurTracer().acquireOtherThreadData(WebRequestInfo.IDENTITY);
        List<DataOperateInfo> dataOperateInfos = TracerManager.getCurTracer().acquireOtherThreadData(DataOperateInfo.LIST_IDENTITY);
        List<Span> originalSpans = TracerManager.getCurTracer().finishedSpans();
        Collections.reverse(originalSpans);
        List<SpanInfo> spanInfos = originalSpans.stream().map(originalSpan ->
                new SpanInfo(originalSpan.context().toSpanId(), (String) originalSpan.tags().get("appId"),
                        (String) originalSpan.tags().get("reqId"), String.valueOf(originalSpan.parentId()),
                        (PluginType) originalSpan.tags().get("pluginType"), (String) originalSpan.tags().get("pluginName"),
                        (String) originalSpan.tags().get("className"), (String) originalSpan.tags().get("methodName"),
                        (String) originalSpan.tags().get("methodArgs"), null, originalSpan.startMicros(),
                        originalSpan.finishMicros(), originalSpan.spendMicros())).collect(Collectors.toList());

        threadDataInfo.setWebRequestInfo(reqInfo);
        threadDataInfo.setDataInfos(dataOperateInfos);
        threadDataInfo.setSpanInfos(spanInfos);

        return threadDataInfo;
    }

    /**
     * 统一线程下所有DataOperateInfo的AppId
     * 1.appid是在拦截servlet过程中根据命名空间匹配出来的
     * 2.对于有些db操作没有经过servlet(比如内部调用)那么appid就会缺失，但是当前线程的第一个数据操作肯定是有的
     * 3.此函数将一个线程下的所有db操作的appid统一为一个
     */
    public static boolean uniteThreadDataAppId(ThreadDataInfo threadData) {
        try {
            if (threadData == null) {
                return false;
            }
            List<DataOperateInfo> dataInfos = threadData.getDataInfos();
            if (dataInfos == null || dataInfos.size() == 0) {
                return false;
            }
            String anyId = null;
            //找到appid
            for (DataOperateInfo data : dataInfos) {
                if (!StringUtil.isEmpty(data.getAppId())) {
                    anyId = data.getAppId();
                    break;
                }
            }
            if (StringUtil.isEmpty(anyId)) {
                return false;
            }
            //填充其余的
            for (DataOperateInfo data : dataInfos) {
                data.setAppId(anyId);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return true;
    }
}
