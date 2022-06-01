package com.ranranx.aolie.gateway.interfaces;

import com.ranranx.aolie.common.runtime.LoginUser;
import com.ranranx.aolie.gateway.SessionProp;

/**
 * @author xxl
 * @version V0.0.1
 * @date 2022/5/25 0025 11:18
 **/
public interface TokenProvider {
    String BEAN_NAME_TOKEN_PROVIDER = "TokenProvider";

    String genToken(SessionProp prop, String extInfo);

    LoginUser getUserInfo(String token, SessionProp prop);
}
