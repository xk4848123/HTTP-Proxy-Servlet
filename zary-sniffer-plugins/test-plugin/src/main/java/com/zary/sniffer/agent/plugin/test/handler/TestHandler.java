package com.zary.sniffer.agent.plugin.test.handler;


import com.zary.sniffer.agent.core.log.LogProducer;
import com.zary.sniffer.agent.core.log.LogUtil;
import com.zary.sniffer.agent.core.plugin.define.HandlerBeforeResult;
import com.zary.sniffer.agent.core.plugin.handler.IInstanceMethodHandler;
import com.zary.sniffer.core.enums.DataOperateType;
import com.zary.sniffer.core.enums.PluginType;
import com.zary.sniffer.core.model.DataOperateInfo;
import com.zary.sniffer.core.model.ThreadDataInfo;
import com.zary.sniffer.core.model.WebRequestInfo;
import com.zary.sniffer.transfer.wrapper.ThreadDataSender;
import com.zary.sniffer.transfer.wrapper.TreadDataSerializer;


import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

public class TestHandler implements IInstanceMethodHandler {

    private static final LogProducer logger = LogUtil.getLogProducer();

    @Override
    public void onBefore(Object instance, Method method, Object[] allArguments, HandlerBeforeResult result) {

    }

    @Override
    public Object onAfter(Object instance, Method method, Object[] allArguments, Object returnValue) {
        LogUtil.info("enter","getVersion");
        return returnValue;
    }
}
