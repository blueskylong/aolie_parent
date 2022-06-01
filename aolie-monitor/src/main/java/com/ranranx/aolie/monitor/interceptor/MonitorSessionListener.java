package com.ranranx.aolie.monitor.interceptor;

import com.ranranx.aolie.core.runtime.SessionUtils;

import javax.servlet.annotation.WebListener;
import javax.servlet.http.HttpSessionEvent;
import javax.servlet.http.HttpSessionListener;

/**
 * @author xxl
 * @version V0.0.1
 * @date 2021/7/5 0005 9:52
 **/
@WebListener
public class MonitorSessionListener implements HttpSessionListener {

    @Override
    public void sessionCreated(HttpSessionEvent arg0) {
        SessionUtils.incrementSessionNum();

    }

    @Override
    public void sessionDestroyed(HttpSessionEvent arg0) {
        SessionUtils.decrementSessionNum();
    }
}
