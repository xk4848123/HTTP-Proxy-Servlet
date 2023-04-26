package com.zary.sniffer.agent.core.transfer.http;

import lombok.Value;

@Value
public class RequestInfo {

    private final String username;

    private final String password;

    private final String serverUrl;
}
