package com.ranranx.aolie.gateway.session;

import com.alibaba.fastjson.JSON;
import com.ranranx.aolie.common.interfaces.SessionStoreService;
import com.ranranx.aolie.common.runtime.LoginUser;
import com.ranranx.aolie.gateway.SessionIdGenerator;
import com.ranranx.aolie.gateway.SessionProp;
import com.ranranx.aolie.gateway.interfaces.TokenProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.ObjectUtils;

/**
 * @author xxl
 * @version V0.0.1
 * @date 2022/5/17 0017 17:10
 **/
public class SimpleSessionProvider implements TokenProvider {
    @Autowired
    private SessionStoreService storeService;

    @Override
    public String genToken(SessionProp prop, String extInfo) {
        return SessionIdGenerator.generateSessionId("root");
    }

    @Override
    public LoginUser getUserInfo(String token, SessionProp prop) {
        if (ObjectUtils.isEmpty(token)) {
            return null;
        }
        String value = storeService.getValue(token);
        if (ObjectUtils.isEmpty(value)) {
            return null;
        }
        return JSON.parseObject(value, LoginUser.class);
    }
}
