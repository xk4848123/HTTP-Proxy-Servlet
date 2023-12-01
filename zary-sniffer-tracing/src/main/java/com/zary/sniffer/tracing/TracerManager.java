package com.zary.sniffer.tracing;


public class TracerManager {

    private static final ThreadLocal<Tracer> curTracer = ThreadLocal.withInitial(() -> new Tracer());

    public static Tracer getCurTracer() {
        return curTracer.get();
    }

    public static void removeCurTracer() {
        curTracer.remove();
    }


}