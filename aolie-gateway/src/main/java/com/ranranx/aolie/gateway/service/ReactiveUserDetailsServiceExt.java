package com.ranranx.aolie.gateway.service;

import com.ranranx.aolie.common.runtime.LoginUser;
import org.springframework.security.core.userdetails.UserDetails;
import reactor.core.publisher.Mono;

import java.security.Principal;

/**
 * @author xxl
 * @version V0.0.1
 * @date 2022/5/27 0027 16:06
 **/
public interface ReactiveUserDetailsServiceExt  {
    /**
     * Find the {@link UserDetails} by username.
     *
     * @return the {@link UserDetails}. Cannot be null
     */
    Mono<UserDetails> findUserByPrincipal(String userName, String versionCode);

    /**
     *
     * @param userName
     * @param versionCode
     * @return
     */
    UserDetails findUserByPrincipalDirect(String userName, String versionCode);
}
