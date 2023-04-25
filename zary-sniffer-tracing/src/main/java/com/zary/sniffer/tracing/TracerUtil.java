package com.zary.sniffer.tracing;

import io.opentracing.Tracer;

public class TracerUtil {

    private static final ThreadLocal<Tracer> curTracer = ThreadLocal.withInitial(() -> new AdmxTracer());

    public static Tracer getCurTracer() {
        return curTracer.get();
    }

    public static void removeCurTracer() {
        curTracer.remove();
    }


}
