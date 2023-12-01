package com.zary.sniffer.transfer;

public class TransferData {

    private final int DEFAULT_TYPE = 1;
    private String data;

    private Integer type;

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }

    public Integer getType() {
        return type;
    }

    public void setType(Integer type) {
        this.type = type;
    }

    public TransferData() {

    }

    public TransferData(String data, int type) {
        this.data = data;
        this.type = type;
    }

    public TransferData(String data) {
        this.data = data;
        this.type = DEFAULT_TYPE;
    }

}
