package com.ranranx.aolie.gateway.service;

import com.ranranx.aolie.application.user.service.ILoginService;
import com.ranranx.aolie.common.runtime.LoginUser;
import org.apache.dubbo.config.annotation.DubboReference;
import org.apache.dubbo.config.annotation.DubboService;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;

/**
 * @author xxl
 * @version V0.0.1
 * @date 2022/5/9 0009 15:09
 **/
@Service
@DubboService
public class SecurityUserDetailService implements ReactiveUserDetailsServiceExt {
    @DubboReference
    private ILoginService loginService;


    /**
     * Find the {@link UserDetails} by username.
     *
     * @return the {@link UserDetails}. Cannot be null
     */
    @Override
    public Mono<UserDetails> findUserByPrincipal(String userName, String version) {
        LoginUser userDetails = (LoginUser) loginService.loadUserByUserNameAndVersion(userName,
                version);
        return Mono.just(userDetails);
    }

    /**
     * @param userName
     * @param versionCode
     * @return
     */
    @Override
    public UserDetails findUserByPrincipalDirect(String userName, String versionCode) {
        return (LoginUser) loginService.loadUserByUserNameAndVersion(userName,
                versionCode);
    }

    /**
     * 取得表与服务的对应关系
     *
     * @return
     */
    public Map<String, List<Long>> getDsServiceNameRelation() {
        return loginService.getDsServiceNameRelation();
    }
}
