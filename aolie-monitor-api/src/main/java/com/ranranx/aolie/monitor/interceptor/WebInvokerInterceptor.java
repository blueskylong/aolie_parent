package com.ranranx.aolie.monitor.interceptor;

import com.alibaba.fastjson.JSON;
import com.ranranx.aolie.core.runtime.SessionUtils;
import com.ranranx.aolie.monitor.common.MonitorConstants;
import com.ranranx.aolie.monitor.service.IMqService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.servlet.AsyncHandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * @author xxl
 * @version V0.0.1
 * @date 2021/7/2 0002 11:33
 **/
public class WebInvokerInterceptor implements AsyncHandlerInterceptor {
    @Autowired
    private IMqService logService;

    private ThreadLocal<Long> local = new ThreadLocal<>();

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        local.set(System.currentTimeMillis());
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        //收集信息
        try {


            Map<String, Object> mapLog = new HashMap<>();
            mapLog.put(MonitorConstants.LogTableFields.start_time,
                    new Date(local.get()));
            mapLog.put(MonitorConstants.LogTableFields.end_time, new Date());
            mapLog.put(MonitorConstants.LogTableFields.log_type, MonitorConstants.LogType.WEB_ACCESS);
            mapLog.put(MonitorConstants.LogTableFields.oper_type, request.getMethod());
            mapLog.put(MonitorConstants.LogTableFields.last_time, System.currentTimeMillis() - local.get());
            mapLog.put(MonitorConstants.LogTableFields.user_id,
                    SessionUtils.getLoginUser() != null ? SessionUtils.getLoginUser().getUserId() : null);
            //以下记录内部信息
            mapLog.put(MonitorConstants.LogTableFields.path, request.getServletPath());
            logService.sendMsg(JSON.toJSONString(mapLog));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
