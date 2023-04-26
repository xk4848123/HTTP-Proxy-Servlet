package com.zary.sniffer.transfer;

public class AgentData<T> {

    /**
     * 业务数据
     */
    private T data;


    /**
     * 业务类型
     */
    private int type = 1;

    public void setType(int type) {
        this.type = type;
    }

    public int getType() {
        return type;
    }

    public T getData() {
        return data;
    }

    public AgentData() {

    }
    public AgentData(T data, int type) {
        this.data = data;
        this.type = type;
    }

    public void setData(T data) {
        this.data = data;
    }
}
