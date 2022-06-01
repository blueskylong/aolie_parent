package com.ranranx.aolie.gateway.handler;

import com.alibaba.fastjson.JSON;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ranranx.aolie.gateway.SessionProp;
import com.ranranx.aolie.common.interfaces.SessionStoreService;
import com.ranranx.aolie.common.runtime.LoginUser;
import com.ranranx.aolie.common.runtime.ResultCode;
import com.ranranx.aolie.common.types.HandleResult;
import com.ranranx.aolie.gateway.interfaces.TokenProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpHeaders;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.server.WebFilterExchange;
import org.springframework.security.web.server.authentication.WebFilterChainServerAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.Map;

/**
 * @author xxl
 * @version V0.0.1
 * @date 2022/5/9 0009 11:39
 **/
@Component
public class AuthenticationSuccessHandler extends WebFilterChainServerAuthenticationSuccessHandler {

    private final SessionProp jwtProperties;

    @Autowired
    private SessionStoreService redisService;

    @Autowired
    private TokenProvider tokenProvider;

    public AuthenticationSuccessHandler(SessionProp jwtProperties) {
        this.jwtProperties = jwtProperties;
    }

    @Override
    public Mono<Void> onAuthenticationSuccess(WebFilterExchange webFilterExchange, Authentication authentication) {
        ServerWebExchange exchange = webFilterExchange.getExchange();
        ServerHttpResponse response = exchange.getResponse();
        HttpHeaders headers = response.getHeaders();
        headers.add("Content-Type", "application/json;charset=UTF-8");
        headers.add("Catch-Control", "no-store,no-cache,must-revalidate,max-age-8");
        byte[] dataBytes = {};
        ObjectMapper mapper = new ObjectMapper();
        try {
            LoginUser userDetails = (LoginUser) authentication.getPrincipal();


            String jwtToken = tokenProvider.genToken(jwtProperties, JSON.toJSONString(userDetails));
            HandleResult result = HandleResult.success(1);
            result.setData(authentication);
            userDetails.setPassword(jwtToken);
            dataBytes = mapper.writer().writeValueAsBytes(result);
            //添加TOKEN到缓存
            redisService.setValue(jwtToken, JSON.toJSONString(userDetails),
                    jwtProperties.getTimeout() / 1000L);

        } catch (Exception e) {
            e.printStackTrace();
            Map<String, Object> map = new HashMap<>();
            map.put("status", ResultCode.UNAUTHORIZED.getCode());
            map.put("msg", ResultCode.UNAUTHORIZED.getMsg());
            dataBytes = JSON.toJSONString(map).getBytes();

        }
        DataBuffer bodyDataBuffer = response.bufferFactory().wrap(dataBytes);
        return response.writeWith(Mono.just(bodyDataBuffer));
    }


}



































