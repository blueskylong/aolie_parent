package com.ranranx.aolie.gateway;

import java.util.UUID;

/**
 * @author xxl
 * @version V0.0.1
 * @date 2022/5/17 0017 8:12
 **/
public class SessionIdGenerator {

    public static String generateSessionId(String route) {
        return UUID.randomUUID().toString();
    }

}
