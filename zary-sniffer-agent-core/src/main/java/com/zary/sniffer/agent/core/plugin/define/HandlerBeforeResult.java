package com.zary.sniffer.agent.core.plugin.define;


import com.zary.sniffer.util.StringUtil;

/**
 * 拦截处理类onBefore处理结果封装对象
 */
public class HandlerBeforeResult {
    public HandlerBeforeResult() {
    }

    /**
     * 是否继续执行：用于控制onBefore执行完成后还是否继续执行被代理的目标函数
     */
    private boolean isContinue = true;
    /**
     * 函数返回值：isContinue=false时，直接指定函数的返回值
     */
    private Object returnValue = null;
    /**
     * 函数参数：用于控制onBefore执行完成后对参数的修改，执行被代理的目标函数优先使用此参数
     */
    private Object[] newArguments = null;

    /**
     * 设置返回值，同时中止继续执行
     *
     * @param value
     */
    public void setReturnValue(Object value) {
        this.isContinue = false;
        this.returnValue = value;
    }

    /**
     * 获取返回值
     *
     * @return
     */
    public Object getReturnValue() {
        return returnValue;
    }

    /**
     * 设置新参数
     *
     * @return
     */
    public void setNewArguments(Object[] args) {
        newArguments = args;
    }

    /**
     * 获得新参数
     *
     * @return
     */
    public Object[] getNewArguments() {
        return newArguments;
    }

    /**
     * 获取是否继续执行标识
     *
     * @return
     */
    public boolean isContinue() {
        return isContinue;
    }

    @Override
    public String toString() {
        return String.format("{isContinue=%s,returnValue=%s,newArguments=%s}",
                isContinue,
                (returnValue == null ? "null" : returnValue.toString()),
                (newArguments == null ? "null" : StringUtil.join(newArguments))
        );
    }
}
