package com.ranranx.aolie.gateway;

import com.ranranx.aolie.gateway.session.EnableSimpleSession;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;

/**
 * @author xxl
 * @version V0.0.1
 * @date 2022/5/9 0009 9:02
 **/
@EnableSimpleSession()
@SpringBootApplication(exclude = DataSourceAutoConfiguration.class, scanBasePackages = {"com.ranranx.aolie"})
public class GatewayApplication {
    public static void main(String[] args) {
        SpringApplication.run(GatewayApplication.class, args);
    }
}
