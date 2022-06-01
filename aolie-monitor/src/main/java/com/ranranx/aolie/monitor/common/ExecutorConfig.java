package com.ranranx.aolie.monitor.common;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.security.core.session.SessionRegistryImpl;

import java.util.concurrent.ThreadPoolExecutor;

/**
 * @author xxl
 * @version V0.0.1
 * @date 2021/7/2 0002 14:52
 **/
@Configuration
public class ExecutorConfig {

    @Bean
    public ThreadPoolTaskExecutor getExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        //此方法返回可用处理器的虚拟机的最大数量; 不小于1
        int core = Runtime.getRuntime().availableProcessors();
        executor.setCorePoolSize(core);
        executor.setMaxPoolSize(core * 2 + 1);
        executor.setKeepAliveSeconds(3);
        executor.setQueueCapacity(40);
        executor.setThreadNamePrefix("thread-log");
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        return executor;

    }

    @Bean
    public SessionRegistryImpl createSessionRegistry() {
        return new Se();
    }

    class Se extends SessionRegistryImpl {
        @Override
        public void registerNewSession(String sessionId, Object principal) {
            System.out.println("------------------>Session");
            super.registerNewSession(sessionId, principal);
        }
    }


}
