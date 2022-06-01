package com.ranranx.aolie.gateway.entry;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;

import java.util.Collection;
import java.util.Objects;

/**
 * @author xxl
 * @version V0.0.1
 * @date 2021/2/16 0016 17:07
 **/
public class NamePassVersionScodeAuthenticationToken extends UsernamePasswordAuthenticationToken {

    /**
     * 版本
     */
    private String version;

    public NamePassVersionScodeAuthenticationToken(Object principal, Object credentials) {
        super(principal, credentials);
    }

    public NamePassVersionScodeAuthenticationToken(Object principal, Object credentials, String version) {
        super(principal, credentials);

        this.version = version;
    }

    public NamePassVersionScodeAuthenticationToken(Object principal, Object credentials,
                                                   Collection<? extends GrantedAuthority> authorities) {
        super(principal, credentials, authorities);
    }


    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof NamePassVersionScodeAuthenticationToken)) {
            return false;
        }
        return this.getPrincipal().equals(((NamePassVersionScodeAuthenticationToken) o).getPrincipal());
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), version);
    }
}
