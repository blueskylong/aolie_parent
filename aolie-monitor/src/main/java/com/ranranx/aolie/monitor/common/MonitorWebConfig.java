package com.ranranx.aolie.monitor.common;

import com.ranranx.aolie.monitor.interceptor.WebInvokerInterceptor;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import javax.annotation.Resource;

/**
 * @author xxl
 * @version V0.0.1
 * @date 2020/9/1 16:14
 **/
@Configuration
public class MonitorWebConfig implements WebMvcConfigurer {
    @Resource
    private ApplicationContext ctx;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        WebInvokerInterceptor interceptor = new WebInvokerInterceptor();
        AutowireCapableBeanFactory factory = ctx.getAutowireCapableBeanFactory();
        factory.autowireBean(interceptor);
        registry.addInterceptor(interceptor);
    }
}

