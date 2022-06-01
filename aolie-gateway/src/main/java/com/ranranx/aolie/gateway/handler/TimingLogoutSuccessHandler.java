package com.ranranx.aolie.gateway.handler;

import com.ranranx.aolie.common.runtime.SimpleSessionUtils;
import com.ranranx.aolie.common.services.RedisService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpMessage;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.server.WebFilterExchange;
import org.springframework.security.web.server.authentication.logout.ServerLogoutSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.util.ObjectUtils;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

/**
 * @author xxl
 * @version V0.0.1
 * @date 2022/5/9 0009 15:34
 **/
@Component
public class TimingLogoutSuccessHandler implements ServerLogoutSuccessHandler {
    @Autowired
    private RedisService redisService;

    @Override
    public Mono<Void> onLogoutSuccess(WebFilterExchange webFilterExchange, Authentication authentication) {
        ServerWebExchange exchange = webFilterExchange.getExchange();
        ServerHttpResponse response = exchange.getResponse();
        String token = getToken(exchange.getRequest());
        if (!ObjectUtils.isEmpty(token)) {
            redisService.remove(token);
        }
        DataBuffer wrap = response.bufferFactory().wrap("success".getBytes());
        return response.writeWith(Mono.just(wrap));
    }

    public static String getToken(HttpMessage request) {
        return request.getHeaders().getFirst(SimpleSessionUtils.KEY_TOKEN);
    }
}
