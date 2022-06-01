package com.ranranx.aolie.monitor.interceptor;

import com.ranranx.aolie.monitor.service.MonitorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

/**
 * 20分钟保存一次用户在线人数
 *
 * @author xxl
 * @version V0.0.1
 * @date 2021/7/5 0005 13:10
 **/
@Configuration
@EnableScheduling
public class OnlineUserTask {
    @Autowired
    private MonitorService monitorService;

    @Scheduled(fixedRate = 1200000)
    private void configureTasks() {
        monitorService.saveOnlineUserNum();
    }


}
