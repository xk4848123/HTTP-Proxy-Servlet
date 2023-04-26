package com.zary.adm.agent.core.transfer;

import lombok.ToString;

@ToString
public class Message2 {

    Long innerType;

    String msg;

    public Message2(Long innerType, String msg) {
        this.innerType = innerType;
        this.msg = msg;
    }

    public Long getInnerType() {
        return innerType;
    }

    public void setInnerType(Long innerType) {
        this.innerType = innerType;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }
}
