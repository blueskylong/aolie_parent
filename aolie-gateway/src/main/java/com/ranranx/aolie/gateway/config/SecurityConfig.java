package com.ranranx.aolie.gateway.config;

import com.ranranx.aolie.gateway.entry.NamePassVersionScodeAuthenticationFilter;
import com.ranranx.aolie.gateway.handler.CustomHttpBasicServerAuthenticationEntryPoint;
import com.ranranx.aolie.gateway.handler.AuthenticationFailHandler;
import com.ranranx.aolie.gateway.handler.MyAuthenticationManager;
import com.ranranx.aolie.gateway.service.ReactiveUserDetailsServiceExt;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.core.io.buffer.DefaultDataBufferFactory;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.ReactiveAuthenticationManager;
import org.springframework.security.authentication.UserDetailsRepositoryReactiveAuthenticationManager;
import org.springframework.security.authorization.ReactiveAuthorizationManager;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.SecurityWebFiltersOrder;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.core.userdetails.ReactiveUserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.NoOpPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.security.web.server.authentication.ServerAuthenticationConverter;
import org.springframework.security.web.server.authentication.ServerAuthenticationSuccessHandler;
import org.springframework.security.web.server.authentication.logout.ServerLogoutSuccessHandler;
import org.springframework.security.web.server.authorization.AuthorizationContext;
import org.springframework.security.web.server.context.NoOpServerSecurityContextRepository;
import org.springframework.security.web.server.context.WebSessionServerSecurityContextRepository;
import org.springframework.security.web.server.util.matcher.ServerWebExchangeMatchers;
import org.springframework.web.server.WebFilter;
import reactor.core.publisher.Mono;

/**
 * @author xxl
 * @version V0.0.1
 * @date 2022/5/9 0009 11:11
 **/
@EnableWebFluxSecurity
public class SecurityConfig {
    private static final String[] excludeAuthPages = {"/auth/login"};

    @Bean
    public PasswordEncoder passwordEncoder() {
        return NoOpPasswordEncoder.getInstance();
    }

    @Bean
    public ReactiveAuthenticationManager reactiveAuthenticationManager(ReactiveUserDetailsServiceExt userDetailsService,
                                                                       PasswordEncoder passwordEncoder) {
        MyAuthenticationManager manager =
                new MyAuthenticationManager(userDetailsService);
        manager.setPasswordEncoder(passwordEncoder);
        return manager;
    }

    @Bean
    public SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity httpSecurity,
                                                         CustomHttpBasicServerAuthenticationEntryPoint entryPoint,
                                                         ServerAuthenticationSuccessHandler successHandler,
                                                         @Qualifier("tokenAuthenticationFilter") WebFilter authenticationFilter,
                                                         AuthenticationFailHandler failHandler,
                                                         ReactiveAuthorizationManager authorizeManager,
                                                         ServerLogoutSuccessHandler logoutSuccessHandler
            , ReactiveAuthenticationManager manager) {
        WebFilter passAuthFilter = NamePassVersionScodeAuthenticationFilter.createWebFilter(manager,
                ServerWebExchangeMatchers
                        .pathMatchers(HttpMethod.POST, "/login"), failHandler, successHandler, new WebSessionServerSecurityContextRepository());

        return httpSecurity.csrf(ServerHttpSecurity.CsrfSpec::disable)
                .httpBasic(ServerHttpSecurity.HttpBasicSpec::disable)
                .authenticationManager(manager)
                .exceptionHandling()
                .authenticationEntryPoint(entryPoint)
                .accessDeniedHandler((swe, e) -> {
                    swe.getResponse().setStatusCode(HttpStatus.FORBIDDEN);
                    return swe.getResponse().writeWith(Mono.just(new DefaultDataBufferFactory().wrap("FORBIDDEN".getBytes())));
                })
                .and()
                .securityContextRepository(NoOpServerSecurityContextRepository.getInstance())
                .authorizeExchange()
                .pathMatchers(excludeAuthPages).permitAll()
                .pathMatchers(HttpMethod.OPTIONS).permitAll()
                .anyExchange().access(authorizeManager)
                .and()
//                .formLogin()
//                .requiresAuthenticationMatcher(ServerWebExchangeMatchers
//                        .pathMatchers(HttpMethod.POST, "/login"))
//                .authenticationSuccessHandler(successHandler)
//                .authenticationFailureHandler(failHandler)
//                .and()
                .logout().logoutUrl("/auth/logout")
                .logoutSuccessHandler(logoutSuccessHandler)
                .and()
                .addFilterAt(authenticationFilter, SecurityWebFiltersOrder.HTTP_BASIC)
                .addFilterAt(passAuthFilter, SecurityWebFiltersOrder.FORM_LOGIN)
                .build();


    }

}
