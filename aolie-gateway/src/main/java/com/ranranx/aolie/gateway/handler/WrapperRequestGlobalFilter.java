package com.ranranx.aolie.gateway.handler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.http.HttpHeaders;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpRequestDecorator;
import org.springframework.stereotype.Component;
import org.springframework.util.MultiValueMap;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.UnsupportedEncodingException;
import java.net.URI;

/**
 * @author xxl
 * @version V0.0.1
 * @date 2022/6/2 0002 19:17
 **/
@Component
public class WrapperRequestGlobalFilter implements GlobalFilter, Ordered {
    Logger log = LoggerFactory.getLogger(WrapperRequestGlobalFilter.class);

    /**
     * 优先级最高
     */
    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        URI URIPath = request.getURI();
        String path = request.getPath().value();
        String method = request.getMethodValue();
        HttpHeaders header = request.getHeaders();
        log.info("");
        log.info("***********************************请求信息**********************************");
        log.info("请求request信息：URI = {}, path = {}，method = {}，header = {}。", URIPath, path, method, header);
        if ("POST".equals(method)) {
            return DataBufferUtils.join(exchange.getRequest().getBody())
                    .flatMap(dataBuffer -> {
                        byte[] bytes = new byte[dataBuffer.readableByteCount()];
                        dataBuffer.read(bytes);
                        try {
                            String bodyString = new String(bytes, "utf-8");
                            log.info("请求参数：" + bodyString);
                            exchange.getAttributes().put("POST_BODY", bodyString);
                        } catch (UnsupportedEncodingException e) {
                            e.printStackTrace();
                        }
                        DataBufferUtils.release(dataBuffer);
                        Flux<DataBuffer> cachedFlux = Flux.defer(() -> {
                            DataBuffer buffer = exchange.getResponse().bufferFactory()
                                    .wrap(bytes);
                            return Mono.just(buffer);
                        });
                        ServerHttpRequest mutatedRequest = new ServerHttpRequestDecorator(
                                exchange.getRequest()) {
                            @Override
                            public Flux<DataBuffer> getBody() {
                                return cachedFlux;
                            }
                        };
                        log.info("****************************************************************************\n");
                        return chain.filter(exchange.mutate().request(mutatedRequest)
                                .build());
                    });
        } else if ("GET".equals(method)) {
            MultiValueMap<String, String> queryParams = request.getQueryParams();
            log.info("请求参数：" + queryParams);
            log.info("****************************************************************************\n");
            return chain.filter(exchange);
        }
        log.info("****************************************************************************\n");
        return chain.filter(exchange);
    }
}
