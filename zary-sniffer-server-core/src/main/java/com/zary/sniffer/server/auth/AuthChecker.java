package com.zary.sniffer.server.auth;

@FunctionalInterface
public interface AuthChecker {
    boolean check(String token);

}
