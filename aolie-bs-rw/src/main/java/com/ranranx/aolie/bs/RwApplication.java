package com.ranranx.aolie.bs;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.context.annotation.Import;
import tk.mybatis.mapper.autoconfigure.MapperAutoConfiguration;

/**
 * @author xxl
 * @version V0.0.1
 * @date 2022/6/2 0002 9:09
 **/
@MapperScan(basePackages = "com.ranranx.aolie", annotationClass = Mapper.class)
@SpringBootApplication(scanBasePackages = "com.ranranx.aolie")
public class RwApplication {
    public static void main(String[] args) {
        SpringApplication.run(RwApplication.class, args);
    }
}
