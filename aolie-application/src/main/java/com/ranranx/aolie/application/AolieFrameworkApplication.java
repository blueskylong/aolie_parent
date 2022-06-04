package com.ranranx.aolie.application;

import com.ranranx.aolie.common.types.CommonUtils;
import org.apache.ibatis.annotations.Mapper;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * For xxl the 40Th
 *
 * @version V0.0.1
 * @date 2020-08-04 11:10
 */

@SpringBootApplication(scanBasePackages = "com.ranranx.aolie")
@MapperScan(basePackages = "com.ranranx.aolie", annotationClass = Mapper.class)
public class AolieFrameworkApplication {

    public static void main(String[] args) {
        if (args != null && args.length > 0) {
            for (String param : args) {
                String[] p = param.split(":");
                if (p.length > 1) {
                    CommonUtils.addGlobalParam(p[0], p[1]);
                } else {
                    CommonUtils.addGlobalParam(param, param);
                }
            }
        }
        SpringApplication.run(AolieFrameworkApplication.class, args);
    }

}
