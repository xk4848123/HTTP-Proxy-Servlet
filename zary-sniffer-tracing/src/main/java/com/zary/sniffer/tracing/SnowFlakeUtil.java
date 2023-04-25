package com.zary.sniffer.tracing;

public class SnowFlakeUtil {

    private static final SnowFlake snowFlake = new SnowFlake(1, 1, 1);

    public static Long nextId() {
        return snowFlake.nextId();
    }


}
