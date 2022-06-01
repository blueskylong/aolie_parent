package com.ranranx.aolie;

import org.apache.ibatis.annotations.Mapper;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;

/**
 * @author xxl
 * @version V0.0.1
 * @date 2021/3/4 0004 12:18
 **/
@MapperScan(basePackages = "com.ranranx.aolie", annotationClass = Mapper.class)
@SpringBootApplication(exclude = {DataSourceAutoConfiguration.class}, scanBasePackages = "com.ranranx.aolie")
public class AolieQueryApplication {

    public static void main(String[] args) {
        SpringApplication.run(AolieQueryApplication.class, args);
    }

}
