package com.ranranx.aolie.application.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.NoOpPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * @author xxl
 * @version V0.0.1
 * @date 2022/5/25 0025 22:25
 **/
@Configuration
public class ApplicationConfig {
    /**
     * BCrypt加密
     *
     * @return
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return NoOpPasswordEncoder.getInstance();
    }
}
