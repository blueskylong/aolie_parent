package com.ranranx.aolie.gateway.config;

import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.cors.reactive.CorsUtils;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.List;

/**
 * @author xxl
 * @version V0.0.1
 * @date 2022/5/29 0029 15:35
 **/
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class CORSFilter implements WebFilter {

    @Override
    public Mono<Void> filter(ServerWebExchange swe, WebFilterChain wfc) {
        ServerHttpRequest request = swe.getRequest();
        if (CorsUtils.isCorsRequest(request)) {
            ServerHttpResponse response = swe.getResponse();
            HttpHeaders headers = response.getHeaders();
//            List<String> allowedOrigins = new ArrayList<String>();
//            allowedOrigins.add("http://localhost:8000");
//            allowedOrigins.add("http://localhost:5000");
//            String origins = "http://localhost:80,http://localhost:5000";
//

            headers.add("Access-Control-Allow-Origin", "*");
            headers.add("Access-Control-Allow-Methods", "*");
            headers.add("Access-Control-Max-Age", "3600");
            headers.add("Access-Control-Allow-Headers", "x-token");
            headers.add("Access-Control-Allow-Headers", "Content-Type");
            if (request.getMethod() == HttpMethod.OPTIONS) {
                response.setStatusCode(HttpStatus.OK);
                return Mono.empty();
            }
        }
        return wfc.filter(swe);
    }
}
