package com.ranranx.aolie.gateway.entry;

import com.alibaba.fastjson.JSON;
import org.springframework.http.HttpMethod;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.util.Assert;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.support.ServerRequestWrapper;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.io.UnsupportedEncodingException;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

/**
 * @author xxl
 * @version V0.0.1
 * @date 2022/5/29 0029 19:27
 **/
public class MyPostLoginServerAuthenticationConverter implements Function<ServerWebExchange, Mono<Authentication>> {
    private String usernameParameter = "username";

    private String passwordParameter = "password";
    private String versionParameter = "version";

    @Override
    @Deprecated
    public Mono<Authentication> apply(ServerWebExchange exchange) {
        ServerHttpRequest request = exchange.getRequest();
        String bodyStr = "";
        String method = request.getMethodValue();
        String uri = request.getPath().toString();
        //判断是否为POST请求
        final CompletableFuture<Authentication> future = new CompletableFuture<>();
        if (HttpMethod.POST.name().equalsIgnoreCase(method)) {
            exchange.getRequest().getBody().subscribe(dataBuffer -> {
                int len = dataBuffer.readableByteCount();
                byte[] bt = new byte[len];
                dataBuffer.read(bt);
                try {
                    String body = new String(bt, "UTF-8");
                    Map<String, String> map = (Map<String, String>) JSON.parse(body);
                    NamePassVersionScodeAuthenticationToken token = new NamePassVersionScodeAuthenticationToken(map.get(usernameParameter),
                            map.get(passwordParameter), map.get(versionParameter));

                    future.obtrudeValue(token);
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
            });
        }
        ;
        return Mono.fromFuture(future);


    }

    private UsernamePasswordAuthenticationToken createAuthentication(String name, String password, String version) {

        return new NamePassVersionScodeAuthenticationToken(name, password, version);
    }

    /**
     * The parameter name of the form data to extract the username
     *
     * @param usernameParameter the username HTTP parameter
     */
    public void setUsernameParameter(String usernameParameter) {
        Assert.notNull(usernameParameter, "usernameParameter cannot be null");
        this.usernameParameter = usernameParameter;
    }

    /**
     * The parameter name of the form data to extract the password
     *
     * @param passwordParameter the password HTTP parameter
     */
    public void setPasswordParameter(String passwordParameter) {
        Assert.notNull(passwordParameter, "passwordParameter cannot be null");
        this.passwordParameter = passwordParameter;
    }

}
