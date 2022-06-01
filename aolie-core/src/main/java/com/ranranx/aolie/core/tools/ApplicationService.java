package com.ranranx.aolie.core.tools;

import com.ranranx.aolie.common.exceptions.NotExistException;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

/**
 * @author xxl
 * @version V0.0.1
 * @date 2020/12/23 0023 10:34
 **/
@Component
public class ApplicationService implements ApplicationContextAware {
    private static ApplicationContext applicationContext;

    private ApplicationService() {
        super();
    }


    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        ApplicationService.applicationContext = applicationContext;
    }

    public static ApplicationContext getApplicationContext() {
        return applicationContext;
    }

    /**
     * 取得容器中的服务
     *
     * @param serviceName
     * @return
     */
    public static <T> T getService(String serviceName, Class<T> tClass) {

        Object service = applicationContext.getBean(serviceName);
        if (tClass.isInstance(service)) {
            return (T) service;
        }
        throw new NotExistException("没有查询到指定服务");
    }
}
