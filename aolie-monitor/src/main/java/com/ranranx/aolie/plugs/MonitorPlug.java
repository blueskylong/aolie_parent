package com.ranranx.aolie.plugs;

import com.ranranx.aolie.common.annotation.Plug;
import com.ranranx.aolie.common.dto.PlugInfo;
import com.ranranx.aolie.common.types.Constants;
import com.ranranx.aolie.core.interfaces.PlugsService;
import com.ranranx.aolie.core.plugs.model.BasePlug;
import com.ranranx.aolie.monitor.service.impl.LogService;
import org.apache.dubbo.config.annotation.DubboReference;

/**
 * @author xxl
 * @version V0.0.1
 * @date 2021/7/6 0006 18:25
 **/
//@Plug
public class MonitorPlug extends BasePlug {
    private static final String PLUG_CODE = "AOLIE_MONITOR_PLUG";
    private static final String VERSION = "0.0.2";
    private final LogService logService;

    @DubboReference
    protected PlugsService service;


    public MonitorPlug(LogService logService) {
        this.logService = logService;
        logService.setEnabled(false);
    }


    @Override
    protected String getPlugCode() {
        return PLUG_CODE;
    }

    @Override
    protected boolean doInstall() {
        insertPlugInfo();
        logService.setEnabled(true);
        return true;
    }

    @Override
    protected String getPlugName() {
        return "系统运行监控";
    }

    @Override
    protected boolean doUninstall() {
        logService.setEnabled(false);
        return true;
    }

    @Override
    protected boolean doUpdate() {
        return true;
    }

    @Override
    protected String doRepair() {
        return null;
    }

    @Override
    public String getNewVersion() {
        return VERSION;
    }

    /**
     * 取得说明
     *
     * @return
     */
    @Override
    protected String getMemo() {
        return "系统运行数据的收集，通过拦截器，记录系统操作，是首页图表的来源,卸载后请去除JAR依赖";
    }

    /**
     * 更新缓存中的插件信息
     */
    @Override
    public void updatePlug() {
        PlugInfo plugInfo = getPlugInfo();
        if (plugInfo.getStatus() != Constants.PlugStatus.NEW) {
            logService.setEnabled(true);
        } else {
            logService.setEnabled(false);
        }
    }

    @Override
    protected void stop() {
        logService.setEnabled(false);
    }

    @Override
    protected void start() {
        logService.setEnabled(true);
    }
}
