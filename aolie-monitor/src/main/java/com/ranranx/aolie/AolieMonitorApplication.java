package com.ranranx.aolie;

import org.apache.ibatis.annotations.Mapper;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.web.servlet.ServletComponentScan;

/**
 * @author xxl
 * @version V0.0.1
 * @date 2021/7/1 0021 11:13
 **/
@MapperScan(basePackages = "com.ranranx.aolie", annotationClass = Mapper.class)
@SpringBootApplication(exclude = {DataSourceAutoConfiguration.class}, scanBasePackages = "com.ranranx.aolie")
public class AolieMonitorApplication {

    public static void main(String[] args) {
        SpringApplication.run(AolieMonitorApplication.class, args);
    }

}
