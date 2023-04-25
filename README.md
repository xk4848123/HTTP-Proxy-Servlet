# 简介

这是一个探针模板工程，可快速进行探针应用开发


# 如何开始

plugins模块中自定义plugin即可，例如demo-plugin中：
```java
public class MethodPlugin extends AbstractPlugin {
    private static final String HANDLER = "handler.com.zary.sniffer.plugin.MethodHandler";

    private static final String TYPE = "com.telit.microgenerator.LogoPrinter";

    @Override
    public ElementMatcher<TypeDescription> getPluginTypeMatcher() {
        return ElementMatchers.named(TYPE);
    }

    @Override
    public IConstructorPoint[] getConstructorPoints() {
        return new IConstructorPoint[0];
    }

    @Override
    public IInstanceMethodPoint[] getInstanceMethodPoints() {
        IInstanceMethodPoint point = new IInstanceMethodPoint() {
            @Override
            public ElementMatcher<MethodDescription> getMethodsMatcher() {
                return ElementMatchers.named("print2");
            }

            @Override
            public String getHandlerClassName() {
                return HANDLER;
            }

            //是否有参数调用
            @Override
            public boolean isMorphArgs() {
                return true;
            }
        };
        return new IInstanceMethodPoint[]{point};
    }

    @Override
    public IStaticMethodPoint[] getStaticMethodPoints() {
        return new IStaticMethodPoint[0];
    }
}

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
        LogUtil.info("spans", spans.toString());

        return returnValue;
    }
}
```

# span使用介绍

## 基本使用

```java
    Tracer tracer = TracerUtil.getCurTracer();

    Span span = tracer.buildSpan("tester").withStartTimestamp(1000).start();
    span.setTag("string", "foo");
    span.setTag("int", 7);
    span.log("foo");
    Map<String, Object> fields = new HashMap<>();
    fields.put("f1", 4);
    fields.put("f2", "two");
    span.log(1002, fields);
    span.log(1003, "event name");
    span.finish(2000);

    List<MockSpan> finishedSpans = tracer.finishedSpans();

        TracerUtil.removeCurTracer();
    
```

## 跨方法传递span
```java
    //方法一
    Tracer tracer = TracerUtil.getCurTracer();
    Span span = tracer.buildSpan("foo").start();
    tracer.activateSpan(span);

    //方法二
    Tracer tracer = TracerUtil.getCurTracer();
    Span span = tracer.activeSpan();
```

## 跨进程传递span
```java
    //进程一
    Tracer tracer = TracerUtil.getCurTracer();
    Span parentSpan = tracer.buildSpan("foo").start();
    parentSpan.finish();

    HashMap<String, String> injectMap = new HashMap<>();
    tracer.inject(parentSpan.context(), Format.Builtin.HTTP_HEADERS,
                    new TextMapAdapter(injectMap));
    
    //进程二
    Tracer tracer = TracerUtil.getCurTracer();
    SpanContext extract = tracer.extract(Format.Builtin.HTTP_HEADERS, new TextMapAdapter(injectMap));
    tracer.buildSpan("bar")
                    .asChildOf(extract)
                    .start();
```
# 如何上报数据

## 插件端上报
```java
    Message2 message2 = new Message2(1L,"msg");
    AgentData<Message2> agentData = new AgentData<>();
    //与server端接收type匹配
    agentData.setType(1);
    agentData.setData(message2);
    AgentDataUtil.sendData(agentData);
```
## server端接收

在server端main方法中启动并订阅数据，定义接收回调处理
```java
public static void main(String[] args) {
    AgentServerStarter agentServerStarter = new AgentServerStarter();

    HandleManager handleManager = new HandleManager();
    AgentDataHandler agentDataHandler = new AgentDataHandler();
    
    //如果上报数据类型复杂可使用ParameterizedType自定义，这里的1对应插件端agentData.setType(1)
    handleManager.addHandler(1, Message2.class, agentDataHandler::handleMessage2);

    agentServerStarter.start(handleManager);
    

    }

/**
 * 回调处理类
 */
public class AgentDataHandler {

    public Boolean handleMessage2(List<AgentData<Message2>> datas) {
        for (AgentData agentData : datas) {
            Message2 message2 = (Message2) agentData.getData();
            log.info(message2.toString());
        }
        return true;
    }

}
```

## 打包部署

mvn package后工程目录下会有zary-admx-x-packages目录，将plugins和zary-admx-xxx-agent.xxx.jar复制到需要执行的jar所在目录下

执行java -javaagent:zary-admx-xxx-agent.xxx.jar -jar xxx.jar即可