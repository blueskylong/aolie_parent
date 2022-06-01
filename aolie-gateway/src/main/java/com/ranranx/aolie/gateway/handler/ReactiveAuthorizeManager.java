package com.ranranx.aolie.gateway.handler;

import com.ranranx.aolie.common.runtime.LoginUser;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.security.authorization.AuthorizationDecision;
import org.springframework.security.authorization.ReactiveAuthorizationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.web.server.authorization.AuthorizationContext;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.Collection;

/**
 * @ClassName AuthorizeConfigManager.java
 * @Description 鉴权用户权限
 */

/**
 * @author xxl
 * @version V0.0.1
 * @date 2022/5/9 0009 22:21
 **/

@Component
public class ReactiveAuthorizeManager implements ReactiveAuthorizationManager<AuthorizationContext> {

    private AntPathMatcher antPathMatcher = new AntPathMatcher();

    @Override
    public Mono<AuthorizationDecision> check(Mono<Authentication> authentication,
                                             AuthorizationContext authorizationContext) {
        return authentication.map(auth -> {return new AuthorizationDecision(true);})
//        //先检查，将白名单的放过
//        //TODO
//        //拦截获得url路径
//        ServerWebExchange exchange = authorizationContext.getExchange();
//        ServerHttpRequest request = exchange.getRequest();
//        String path = request.getURI().getPath();
//
//        return authentication.map(auth -> {
//            LoginUser loginUser = (LoginUser) auth.getPrincipal();
//            Collection<? extends GrantedAuthority> resources = loginUser.getAuthorities();
//            if (resources == null || resources.isEmpty()) {
//                return new AuthorizationDecision(false);
//            }
//            //TODO 支持restful
//            String methodValue = request.getMethodValue();
//            for (GrantedAuthority resource : resources) {
//                //TODO 这里是不是需要支持REST的风格
//                if (antPathMatcher.match(resource.getAuthority(), path)) {
//                    return new AuthorizationDecision(true);
//                }
//            }
//            System.out.println(String.format("用户请求API校验未通过，Path:%s ", path));
//            return new AuthorizationDecision(false);
        .defaultIfEmpty(new AuthorizationDecision(false));

    }


}
