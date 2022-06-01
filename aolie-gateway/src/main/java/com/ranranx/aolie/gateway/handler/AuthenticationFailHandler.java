package com.ranranx.aolie.gateway.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ranranx.aolie.common.runtime.LoginUser;
import com.ranranx.aolie.common.runtime.ResponseResult;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.server.WebFilterExchange;
import org.springframework.security.web.server.authentication.ServerAuthenticationFailureHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

/**
 * @author xxl
 * @version V0.0.1
 * @date 2022/5/9 0009 11:39
 **/
@Component
public class AuthenticationFailHandler implements ServerAuthenticationFailureHandler {


    @Override
    public Mono<Void> onAuthenticationFailure(WebFilterExchange webFilterExchange, AuthenticationException e) {
        ServerWebExchange exchange = webFilterExchange.getExchange();
        ServerHttpResponse response = exchange.getResponse();
        // 设置headers
        HttpHeaders httpHeaders = response.getHeaders();
        httpHeaders.add("Content-Type", "application/json; charset=UTF-8");
        httpHeaders.add("Cache-Control", "no-store, no-cache, must-revalidate, max-age=0");
        response.setStatusCode(HttpStatus.UNAUTHORIZED);
        // 设置body
        byte[] dataBytes = {};
        try {
            ObjectMapper mapper = new ObjectMapper();
            dataBytes = mapper.writeValueAsBytes(e.getMessage().toString());
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        DataBuffer bodyDataBuffer = response.bufferFactory().wrap(dataBytes);
        return response.writeWith(Mono.just(bodyDataBuffer));
    }

    private LoginUser toUserDetails(Object obj) {
        return null;
    }
}



































