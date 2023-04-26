package com.zary.sniffer.server;

import lombok.Data;
import org.apache.pulsar.shade.com.fasterxml.jackson.annotation.JsonInclude;

import java.io.Serializable;

@Data
@JsonInclude(value = JsonInclude.Include. NON_NULL)
public class ResponseModel implements Serializable {
    private static final long serialVersionUID = -4526311006546321640L;
    /**
     * 响应状态码 EStatusCode
     */
    private Integer statusCode;
    /**
     * 响应信息
     */
    private String message;
    /**
     * 响应数据
     */
    private Object data;

    public ResponseModel(){}

}