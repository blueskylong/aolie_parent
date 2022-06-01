package com.ranranx.aolie.gateway.entry;

import org.springframework.security.authentication.ReactiveAuthenticationManager;
import org.springframework.security.web.server.authentication.AuthenticationWebFilter;
import org.springframework.security.web.server.authentication.ServerAuthenticationFailureHandler;
import org.springframework.security.web.server.authentication.ServerAuthenticationSuccessHandler;
import org.springframework.security.web.server.context.ServerSecurityContextRepository;
import org.springframework.security.web.server.util.matcher.ServerWebExchangeMatcher;
import org.springframework.web.server.WebFilter;

/**
 * @author xxl
 * @version V0.0.1
 * @date 2021/2/7 0007 10:03
 **/
public class NamePassVersionScodeAuthenticationFilter {
    private static final String USERNAME = "username";
    private static final String PASSWORD = "password";
    private static final String SCODE = "scode";


    public static WebFilter createWebFilter(ReactiveAuthenticationManager authenticationManager
            , ServerWebExchangeMatcher requiresAuthenticationMatcher,
                                            ServerAuthenticationFailureHandler authenticationFailureHandler,
                                            ServerAuthenticationSuccessHandler authenticationSuccessHandler,
                                            ServerSecurityContextRepository securityContextRepository
    ) {
        AuthenticationWebFilter authenticationFilter = new AuthenticationWebFilter(authenticationManager);
        authenticationFilter.setRequiresAuthenticationMatcher(requiresAuthenticationMatcher);
        authenticationFilter.setAuthenticationFailureHandler(authenticationFailureHandler);
        authenticationFilter.setAuthenticationConverter(new MyPostLoginServerAuthenticationConverter());
        authenticationFilter.setAuthenticationSuccessHandler(authenticationSuccessHandler);
        authenticationFilter.setSecurityContextRepository(securityContextRepository);
        return authenticationFilter;
    }
}
