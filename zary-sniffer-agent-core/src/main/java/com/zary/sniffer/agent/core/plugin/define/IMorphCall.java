package com.zary.sniffer.agent.core.plugin.define;

/**
 * bytebuddy morph.binding指定的函数原型，可以通过该接口带参数调用目标函数(SuperCall不支持带参数调用)
 * 参数必须使用Object[]，否则报错：does not take a single argument of type Object[]
 */
public interface IMorphCall {
    Object call(Object[] args);
}
