package com.zary.sniffer.tracing;

import io.opentracing.References;
import io.opentracing.Scope;
import io.opentracing.tag.Tag;

import java.util.*;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Spans 使用Tracer.buildSpan(...)被创建
 * Tracer.finishedSpans(). 它们提供所有 Span 状态的访问器。
 *
 * @see Tracer#finishedSpans()
 */
public final class Span implements io.opentracing.Span {
    private static AtomicLong nextId = new AtomicLong(0);

    private final Tracer tracer;
    private SpanContext context;
    private final long parentId; // 0 if there's no parent.
    private final long startMicros;
    private boolean finished;
    private long finishMicros;

    private long spendMicros;
    private final Map<String, Object> tags;
    private final List<LogEntry> logEntries = new ArrayList<>();
    private String operationName;
    private final List<Reference> references;

    private Scope scope;

    private final List<RuntimeException> errors = new ArrayList<>();

    public String operationName() {
        return this.operationName;
    }

    @Override
    public Span setOperationName(String operationName) {
        finishedCheck("Setting operationName {%s} on already finished span", operationName);
        this.operationName = operationName;
        return this;
    }

    public void setScope(Scope scope) {
        this.scope = scope;
    }

    public long parentId() {
        return parentId;
    }

    public long startMicros() {
        return startMicros;
    }

    public long finishMicros() {
        return finishMicros;
    }

    public long spendMicros() {
        return spendMicros;
    }

    public Map<String, Object> tags() {
        return new HashMap<>(this.tags);
    }

    public List<LogEntry> logEntries() {
        return new ArrayList<>(this.logEntries);
    }


    public List<RuntimeException> generatedErrors() {
        return new ArrayList<>(errors);
    }

    public List<Reference> references() {
        return new ArrayList<>(references);
    }

    @Override
    public SpanContext context() {
        return this.context;
    }

    @Override
    public void finish() {
        this.finish(nowMicros());
    }

    @Override
    public void finish(long finishMicros) {
        finishedCheck("Finishing already finished span");
        scope.close();
        this.finishMicros = finishMicros;
        this.spendMicros = finishMicros - spendMicros;
        this.tracer.appendFinishedSpan(this);
        this.finished = true;
    }

    @Override
    public Span setTag(String key, String value) {
        return setObjectTag(key, value);
    }

    @Override
    public Span setTag(String key, boolean value) {
        return setObjectTag(key, value);
    }

    public Span setTag(String key, Object value) {
        return setObjectTag(key, value);
    }

    @Override
    public Span setTag(String key, Number value) {
        return setObjectTag(key, value);
    }

    @Override
    public <T> Span setTag(Tag<T> tag, T value) {
        tag.set(this, value);
        return this;
    }

    public Span setObjectTag(String key, Object value) {
        finishedCheck("Adding tag {%s:%s} to already finished span", key, value);
        tags.put(key, value);
        return this;
    }

    @Override
    public final io.opentracing.Span log(Map<String, ?> fields) {
        return log(nowMicros(), fields);
    }

    @Override
    public final synchronized Span log(long timestampMicros, Map<String, ?> fields) {
        finishedCheck("Adding logs %s at %d to already finished span", fields, timestampMicros);
        this.logEntries.add(new LogEntry(timestampMicros, fields));
        return this;
    }

    @Override
    public Span log(String event) {
        return this.log(nowMicros(), event);
    }

    @Override
    public Span log(long timestampMicroseconds, String event) {
        return this.log(timestampMicroseconds, Collections.singletonMap("event", event));
    }

    @Override
    public io.opentracing.Span setBaggageItem(String key, String value) {
        finishedCheck("Adding baggage {%s:%s} to already finished span", key, value);
        this.context = this.context.withBaggageItem(key, value);
        return this;
    }

    @Override
    public String getBaggageItem(String key) {
        return this.context.getBaggageItem(key);
    }

    /**
     * MockContext实现了开放跟踪。具有跟踪和跨度 ID 的 SpanContext。
     */
    public static final class SpanContext implements io.opentracing.SpanContext {
        private final long traceId;
        private final Map<String, String> baggage;
        private final long spanId;

        /**
         * span上下文
         *
         * @param baggage 模拟上下文获取行李参数的所有权
         * @see SpanContext#withBaggageItem(String, String)
         */
        public SpanContext(long traceId, long spanId, Map<String, String> baggage) {
            this.baggage = baggage;
            this.traceId = traceId;
            this.spanId = spanId;
        }

        public String getBaggageItem(String key) {
            return this.baggage.get(key);
        }

        public String toTraceId() {
            return String.valueOf(traceId);
        }

        public String toSpanId() {
            return String.valueOf(spanId);
        }

        public long traceId() {
            return traceId;
        }

        public long spanId() {
            return spanId;
        }


        public SpanContext withBaggageItem(String key, String val) {
            Map<String, String> newBaggage = new HashMap<>(this.baggage);
            newBaggage.put(key, val);
            return new SpanContext(this.traceId, this.spanId, newBaggage);
        }

        @Override
        public Iterable<Map.Entry<String, String>> baggageItems() {
            return baggage.entrySet();
        }
    }

    public static final class LogEntry {
        private final long timestampMicros;
        private final Map<String, ?> fields;

        public LogEntry(long timestampMicros, Map<String, ?> fields) {
            this.timestampMicros = timestampMicros;
            this.fields = fields;
        }

        public long timestampMicros() {
            return timestampMicros;
        }

        public Map<String, ?> fields() {
            return fields;
        }
    }

    public static final class Reference {
        private final SpanContext context;
        private final String referenceType;

        public Reference(SpanContext context, String referenceType) {
            this.context = context;
            this.referenceType = referenceType;
        }

        public SpanContext getContext() {
            return context;
        }

        public String getReferenceType() {
            return referenceType;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Reference reference = (Reference) o;
            return Objects.equals(context, reference.context) &&
                    Objects.equals(referenceType, reference.referenceType);
        }

        @Override
        public int hashCode() {
            return Objects.hash(context, referenceType);
        }
    }

    Span(Tracer tracer, String operationName, long startMicros, Map<String, Object> initialTags, List<Reference> refs) {
        this.tracer = tracer;
        this.operationName = operationName;
        this.startMicros = startMicros;
        if (initialTags == null) {
            this.tags = new HashMap<>();
        } else {
            this.tags = new HashMap<>(initialTags);
        }
        if (refs == null) {
            this.references = Collections.emptyList();
        } else {
            this.references = new ArrayList<>(refs);
        }
        SpanContext parent = findPreferredParentRef(this.references);

        if (parent == null) {
            this.context = new SpanContext(nextTracerId(), nextSpanId(), new HashMap<>());
            this.parentId = 0;
        } else {
            this.context = new SpanContext(parent.traceId, nextSpanId(), mergeBaggages(this.references));
            this.parentId = parent.spanId;
        }
    }

    private static SpanContext findPreferredParentRef(List<Reference> references) {
        if (references.isEmpty()) {
            return null;
        }
        for (Reference reference : references) {
            if (References.CHILD_OF.equals(reference.getReferenceType())) {
                return reference.getContext();
            }
        }
        return references.get(0).getContext();
    }

    private static Map<String, String> mergeBaggages(List<Reference> references) {
        Map<String, String> baggage = new HashMap<>();
        for (Reference ref : references) {
            if (ref.getContext().baggage != null) {
                baggage.putAll(ref.getContext().baggage);
            }
        }
        return baggage;
    }

    static long nextSpanId() {
        return nextId.addAndGet(1);
    }

    static long nextTracerId() {
        return SnowFlakeUtil.nextId();
    }

    static long nowMicros() {
        return System.currentTimeMillis() * 1000;
    }

    private synchronized void finishedCheck(String format, Object... args) {
        if (finished) {
            RuntimeException ex = new IllegalStateException(String.format(format, args));
            errors.add(ex);
            throw ex;
        }
    }

    @Override
    public String toString() {
        return "{" +
                "traceId:" + context.traceId() +
                ", spanId:" + context.spanId() +
                ", parentId:" + parentId +
                ", operationName:\"" + operationName + "\"}";
    }
}
