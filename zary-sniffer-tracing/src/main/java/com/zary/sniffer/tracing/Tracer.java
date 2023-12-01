package com.zary.sniffer.tracing;

import io.opentracing.*;
import io.opentracing.propagation.*;
import io.opentracing.tag.Tag;
import io.opentracing.util.ThreadLocalScopeManager;

import java.io.*;
import java.nio.ByteBuffer;
import java.util.*;

public class Tracer implements io.opentracing.Tracer {
    private final List<Span> finishedSpans = new ArrayList<>();

    private final ThreadLocal<Map<String, Object>> otherThreadData = ThreadLocal.withInitial(LinkedHashMap::new);
    private final Propagator propagator;
    private final ScopeManager scopeManager;
    private boolean isClosed;

    public Tracer() {
        this(new ThreadLocalScopeManager(), Propagator.TEXT_MAP);
    }

    public Tracer(ScopeManager scopeManager) {
        this(scopeManager, Propagator.TEXT_MAP);
    }

    public Tracer(ScopeManager scopeManager, Propagator propagator) {
        this.scopeManager = scopeManager;
        this.propagator = propagator;
    }

    /**
     * 创建一个新的 MockTracer，通过对 inject（） 和 or extract（） 的任何调用。
     */
    public Tracer(Propagator propagator) {
        this(new ThreadLocalScopeManager(), propagator);
    }

    /**
     * Clear the finishedSpans() queue.
     * Clear the otherThreadData.
     */
    public void reset() {
        this.otherThreadData.remove();
        this.finishedSpans.clear();
    }

    /**
     * @see Tracer#reset()
     */
    public List<Span> finishedSpans() {
        return new ArrayList<>(this.finishedSpans);
    }

    /**
     * 提取线程中的变量重新打包
     */
    public Map<String, Object> extractOtherThreadData() {
        Map<String, Object> otherThreadData = new LinkedHashMap<>(this.otherThreadData.get());
        return otherThreadData;
    }

    /**
     * 获取线程变量中相应key的对象
     */
    public <T> T acquireOtherThreadData(String key) {
        return (T) this.otherThreadData.get().get(key);
    }

    public boolean hasOtherThreadData(String key) {
        return this.otherThreadData.get().containsKey(key);
    }

    public void removeOtherThreadData(String key) {
        this.otherThreadData.get().remove(key);
    }

    public void fillOtherThreadData(String key, Object value) {
        this.otherThreadData.get().put(key, value);
    }


    /**
     * @return 这个 Tracer 启动，按 traceId 和 spanId 分组，采用 HashMap 格式。
     */
    public Map<String, Map<String, Span>> finishedTraces() {
        Map<String, Map<String, Span>> result = new LinkedHashMap<>();

        for (Span span : this.finishedSpans) {
            String traceId = span.context().toTraceId();

            Map<String, Span> spanId2Span = result.get(traceId);
            if (null == spanId2Span) {
                spanId2Span = new LinkedHashMap<>();
                result.put(traceId, spanId2Span);
            }

            String spanId = span.context().toSpanId();
            spanId2Span.put(spanId, span);
        }

        return result;
    }

    /**
     * Noop 方法调用 {@link Tracer#}。
     */
    protected void onSpanFinished(Span span) {
    }

    /**
     * Propagator allows the developer to intercept and verify any calls to inject() and/or extract().
     *
     * @see Tracer#Tracer(Propagator)
     */
    public interface Propagator {
        <C> void inject(Span.SpanContext ctx, Format<C> format, C carrier);

        <C> Span.SpanContext extract(Format<C> format, C carrier);

        Propagator PRINTER = new Propagator() {
            @Override
            public <C> void inject(Span.SpanContext ctx, Format<C> format, C carrier) {
                System.out.println("inject(" + ctx + ", " + format + ", " + carrier + ")");
            }

            @Override
            public <C> Span.SpanContext extract(Format<C> format, C carrier) {
                System.out.println("extract(" + format + ", " + carrier + ")");
                return null;
            }
        };

        Propagator BINARY = new Propagator() {
            static final int BUFFER_SIZE = 128;

            @Override
            public <C> void inject(Span.SpanContext ctx, Format<C> format, C carrier) {
                if (!(carrier instanceof BinaryInject)) {
                    throw new IllegalArgumentException("Expected BinaryInject, received " + carrier.getClass());
                }

                BinaryInject binary = (BinaryInject) carrier;
                ByteArrayOutputStream stream = new ByteArrayOutputStream();
                ObjectOutputStream objStream = null;
                try {
                    objStream = new ObjectOutputStream(stream);
                    objStream.writeLong(ctx.spanId());
                    objStream.writeLong(ctx.traceId());

                    for (Map.Entry<String, String> entry : ctx.baggageItems()) {
                        objStream.writeUTF(entry.getKey());
                        objStream.writeUTF(entry.getValue());
                    }
                    objStream.flush(); // *need* to flush ObjectOutputStream.

                    byte[] buff = stream.toByteArray();
                    binary.injectionBuffer(buff.length).put(buff);

                } catch (IOException e) {
                    throw new RuntimeException("Corrupted state", e);
                } finally {
                    if (objStream != null) {
                        try {
                            objStream.close();
                        } catch (Exception e2) {
                        }
                    }
                }
            }

            @Override
            public <C> Span.SpanContext extract(Format<C> format, C carrier) {
                if (!(carrier instanceof BinaryExtract)) {
                    throw new IllegalArgumentException("Expected BinaryExtract, received " + carrier.getClass());
                }

                Long traceId = null;
                Long spanId = null;
                Map<String, String> baggage = new HashMap<>();

                BinaryExtract binary = (BinaryExtract) carrier;
                ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                ObjectInputStream objStream = null;
                try {
                    ByteBuffer extractBuff = binary.extractionBuffer();
                    byte[] buff = new byte[extractBuff.remaining()];
                    extractBuff.get(buff);

                    objStream = new ObjectInputStream(new ByteArrayInputStream(buff));
                    spanId = objStream.readLong();
                    traceId = objStream.readLong();

                    while (objStream.available() > 0) {
                        baggage.put(objStream.readUTF(), objStream.readUTF());
                    }
                } catch (IOException e) {
                    throw new RuntimeException("Corrupted state", e);
                } finally {
                    if (objStream != null) {
                        try {
                            objStream.close();
                        } catch (Exception e2) {
                        }
                    }
                }

                if (traceId != null && spanId != null) {
                    return new Span.SpanContext(traceId, spanId, baggage);
                }

                return null;
            }
        };

        Propagator TEXT_MAP = new Propagator() {
            public static final String SPAN_ID_KEY = "spanid";
            public static final String TRACE_ID_KEY = "traceid";
            public static final String BAGGAGE_KEY_PREFIX = "baggage-";

            @Override
            public <C> void inject(Span.SpanContext ctx, Format<C> format, C carrier) {
                if (carrier instanceof TextMapInject) {
                    TextMapInject textMap = (TextMapInject) carrier;
                    for (Map.Entry<String, String> entry : ctx.baggageItems()) {
                        textMap.put(BAGGAGE_KEY_PREFIX + entry.getKey(), entry.getValue());
                    }
                    textMap.put(SPAN_ID_KEY, String.valueOf(ctx.spanId()));
                    textMap.put(TRACE_ID_KEY, String.valueOf(ctx.traceId()));
                } else {
                    throw new IllegalArgumentException("Unknown carrier");
                }
            }

            @Override
            public <C> Span.SpanContext extract(Format<C> format, C carrier) {
                Long traceId = null;
                Long spanId = null;
                Map<String, String> baggage = new HashMap<>();

                if (carrier instanceof TextMapExtract) {
                    TextMapExtract textMap = (TextMapExtract) carrier;
                    for (Map.Entry<String, String> entry : textMap) {
                        if (TRACE_ID_KEY.equals(entry.getKey())) {
                            traceId = Long.valueOf(entry.getValue());
                        } else if (SPAN_ID_KEY.equals(entry.getKey())) {
                            spanId = Long.valueOf(entry.getValue());
                        } else if (entry.getKey().startsWith(BAGGAGE_KEY_PREFIX)) {
                            String key = entry.getKey().substring((BAGGAGE_KEY_PREFIX.length()));
                            baggage.put(key, entry.getValue());
                        }
                    }
                } else {
                    throw new IllegalArgumentException("Unknown carrier");
                }

                if (traceId != null && spanId != null) {
                    return new Span.SpanContext(traceId, spanId, baggage);
                }

                return null;
            }
        };
    }

    @Override
    public ScopeManager scopeManager() {
        return this.scopeManager;
    }

    @Override
    public SpanBuilder buildSpan(String operationName) {
        return new SpanBuilder(operationName);
    }

    @Override
    public <C> void inject(SpanContext spanContext, Format<C> format, C carrier) {
        this.propagator.inject((Span.SpanContext) spanContext, format, carrier);
    }

    @Override
    public <C> SpanContext extract(Format<C> format, C carrier) {
        return this.propagator.extract(format, carrier);
    }

    @Override
    public io.opentracing.Span activeSpan() {
        return this.scopeManager.activeSpan();
    }

    @Override
    public Scope activateSpan(io.opentracing.Span span) {
        Scope scope = this.scopeManager.activate(span);

        if (span instanceof Span) {
            ((Span) span).setScope(scope);
        }
        return scope;
    }

    @Override
    public void close() {
        this.isClosed = true;
        this.otherThreadData.remove();
        this.finishedSpans.clear();
    }

    void appendFinishedSpan(Span span) {
        if (isClosed)
            return;

        this.finishedSpans.add(span);
        this.onSpanFinished(span);
    }

    private SpanContext activeSpanContext() {
        io.opentracing.Span span = activeSpan();
        if (span == null) {
            return null;
        }

        return span.context();
    }

    public final class SpanBuilder implements io.opentracing.Tracer.SpanBuilder {
        private final String operationName;
        private long startMicros;
        private List<Span.Reference> references = new ArrayList<>();
        private boolean ignoringActiveSpan;
        private Map<String, Object> initialTags = new HashMap<>();

        SpanBuilder(String operationName) {
            this.operationName = operationName;
            this.startMicros = Span.nowMicros();
        }

        @Override
        public SpanBuilder asChildOf(SpanContext parent) {
            return addReference(References.CHILD_OF, parent);
        }

        @Override
        public SpanBuilder asChildOf(io.opentracing.Span parent) {
            if (parent == null) {
                return this;
            }
            return addReference(References.CHILD_OF, parent.context());
        }

        @Override
        public SpanBuilder ignoreActiveSpan() {
            ignoringActiveSpan = true;
            return this;
        }

        @Override
        public SpanBuilder addReference(String referenceType, SpanContext referencedContext) {
            if (referencedContext != null) {
                this.references.add(new Span.Reference((Span.SpanContext) referencedContext, referenceType));
            }
            return this;
        }

        @Override
        public SpanBuilder withTag(String key, String value) {
            this.initialTags.put(key, value);
            return this;
        }

        @Override
        public SpanBuilder withTag(String key, boolean value) {
            this.initialTags.put(key, value);
            return this;
        }

        @Override
        public SpanBuilder withTag(String key, Number value) {
            this.initialTags.put(key, value);
            return this;
        }

        @Override
        public <T> io.opentracing.Tracer.SpanBuilder withTag(Tag<T> tag, T value) {
            this.initialTags.put(tag.getKey(), value);
            return this;
        }

        @Override
        public SpanBuilder withStartTimestamp(long microseconds) {
            this.startMicros = microseconds;
            return this;
        }

        @Override
        public Span start() {
            SpanContext activeSpanContext = activeSpanContext();
            if (references.isEmpty() && !ignoringActiveSpan && activeSpanContext != null) {
                references.add(new Span.Reference((Span.SpanContext) activeSpanContext, References.CHILD_OF));
            }
            Span span = new Span(Tracer.this, operationName, startMicros, initialTags, references);
            activateSpan(span);

            return span;
        }
    }
}
