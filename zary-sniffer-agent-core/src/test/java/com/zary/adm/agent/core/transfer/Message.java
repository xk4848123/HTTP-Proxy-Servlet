package com.zary.adm.agent.core.transfer;

import lombok.ToString;

@ToString
public class Message {

    Integer innerType;

    String msg;

    public Message(Integer innerType, String msg) {
        this.innerType = innerType;
        this.msg = msg;
    }

    public Integer getInnerType() {
        return innerType;
    }

    public void setInnerType(Integer innerType) {
        this.innerType = innerType;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }


}
