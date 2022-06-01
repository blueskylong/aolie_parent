package com.ranranx.aolie.gateway;

import org.springframework.security.core.GrantedAuthority;

/**
 * @author xxl
 * @version V0.0.1
 * @date 2022/5/10 0010 13:18
 **/
public class MyGrantedAuthority implements GrantedAuthority {
    private String authority;

    public MyGrantedAuthority() {

    }

    public MyGrantedAuthority(String authItem) {
        this.authority = authItem;
    }

    @Override
    public String getAuthority() {
        return this.authority;
    }


    public void setAuthority(String authItem) {
        this.authority = authItem;

    }
}
