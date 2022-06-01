package com.ranranx.aolie.gateway;

/**
 * @author xxl
 * @version V0.0.1
 * @date 2022/5/16 0016 13:52
 **/

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * @author xxl
 * @version V0.0.1
 * @date 2022/5/9 0009 11:00
 **/
@Component
@ConfigurationProperties(prefix = "timing.session")
public class SessionProp {
    private String secretKey;
    private Integer timeout;

    public String getSecretKey() {
        return secretKey;
    }

    public void setSecretKey(String secretKey) {
        this.secretKey = secretKey;
    }

    public Integer getTimeout() {
        return timeout;
    }

    public void setTimeout(Integer timeout) {
        this.timeout = timeout;
    }
}
