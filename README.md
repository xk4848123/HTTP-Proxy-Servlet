# 简介

基于插桩在Servlet内部对配置路径进行反向代理，实现将请求转发到其他服务器，目前支持基于Spring MVC和Spring Boot的应用。

# 快速开始

## 配置简介
```shell
#匹配模式到目标URL的映射列表，目前支持带*的路径匹配和全路径匹配
#匹配优先级是从上到下，匹配到就停止匹配
#注意/index/*可以匹配/index/、/index、/index/a、/index/a/b等
#stripPrefix: true会去掉匹配的父路径，如:/index/a匹配到/index/*，去掉/index,实际uri成/a
routes:
- path: /pub/version
  target: http://192.168.120.144:3004
  stripPrefix: false
- path: /pub/*
  target: http://192.168.120.144:3004
  stripPrefix: false
- path: /*
  target: http://127.0.0.1:3005
  stripPrefix: false
```



## 部署使用
```shell
mvn clean package -DskipTests
```
zary-sniffer-x-packages目录，将plugins和zary-sniffer-xxx-agent.xxx.jar复制到需要执行的jar所在目录下

执行java -javaagent:zary-sniffer-xxx-agent.xxx.jar -jar xxx.jar即可