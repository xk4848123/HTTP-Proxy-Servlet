package com.zary.sniffer.transfer;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.ToString;

@ToString
@NoArgsConstructor
@AllArgsConstructor
public class Message2 {

    Long innerType;

    String msg;

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
