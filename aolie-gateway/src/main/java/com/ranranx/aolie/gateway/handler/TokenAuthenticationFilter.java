package com.ranranx.aolie.gateway.handler;

import com.ranranx.aolie.common.interfaces.SessionStoreService;
import com.ranranx.aolie.common.runtime.LoginUser;
import com.ranranx.aolie.common.runtime.SimpleSessionUtils;
import com.ranranx.aolie.gateway.SessionProp;
import com.ranranx.aolie.gateway.entry.NamePassVersionScodeAuthenticationToken;
import com.ranranx.aolie.gateway.interfaces.TokenProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.ObjectUtils;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

/**
 * 难证TOKEN
 * @author xxl
 * @version V0.0.1
 * @date 2022/5/9 0009 14:35
 **/
@Component
@Qualifier("tokenAuthenticationFilter")
public class TokenAuthenticationFilter implements WebFilter {
    private static final String USERNAME = "username";
    private static final String PASSWORD = "password";
    private static final String SCODE = "scode";
    private final SessionProp jwtProperties;

    @Autowired
    private SessionStoreService sessionService;
    @Autowired
    private TokenProvider provider;

    public TokenAuthenticationFilter(SessionProp jwtProperties) {
        this.jwtProperties = jwtProperties;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {

        String token = SimpleSessionUtils.getToken(exchange.getRequest());

        //这里直接使用ＲＥＤＩＳ缓存来验证
        if (!ObjectUtils.isEmpty(token)) {
            LoginUser userInfo = provider.getUserInfo(token, jwtProperties);
            if (userInfo == null) {
                //已过期的
                return chain.filter(exchange);
            } else {
                //刷新过期时间
                sessionService.setExpire(token, (long) (jwtProperties.getTimeout() / 1000));
                return chain.filter(exchange)
                        .contextWrite(ReactiveSecurityContextHolder.withAuthentication(
                                new NamePassVersionScodeAuthenticationToken(userInfo,
                                        null, userInfo.getAuthorities())));
            }
        }
        return chain.filter(exchange);
    }


}
