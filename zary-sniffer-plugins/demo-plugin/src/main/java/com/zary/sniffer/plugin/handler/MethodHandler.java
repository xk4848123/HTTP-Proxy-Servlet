package com.zary.sniffer.plugin.handler;

import com.zary.sniffer.agent.core.log.LogUtil;
import com.zary.sniffer.agent.core.plugin.define.HandlerBeforeResult;
import com.zary.sniffer.agent.core.plugin.handler.IInstanceMethodHandler;
import com.zary.sniffer.tracing.AdmxSpan;
import com.zary.sniffer.tracing.AdmxTracer;
import com.zary.sniffer.tracing.TracerUtil;
import io.opentracing.Span;
import io.opentracing.Tracer;

import java.lang.reflect.Method;
import java.util.List;

public class MethodHandler implements IInstanceMethodHandler {

    @Override
    public void onBefore(Object o, Method method, Object[] objects, HandlerBeforeResult handlerBeforeResult) throws Throwable {
        Tracer curTracer = TracerUtil.getCurTracer();
        Span span = curTracer.buildSpan("method").start();
        curTracer.activateSpan(span);


    }

    @Override
    public Object onAfter(Object instance, Method method, Object[] allArguments, Object returnValue) throws Throwable {
        Tracer curTracer = TracerUtil.getCurTracer();
        Span span = curTracer.activeSpan();
        span.finish();
        List<AdmxSpan> spans = ((AdmxTracer) curTracer).finishedSpans();
        System.out.println(spans.toString());
        LogUtil.info("spans", spans.toString());

        return returnValue;
    }
}
