package com.ranranx.aolie.monitor.controller;

import com.ranranx.aolie.core.runtime.SessionUtils;
import com.ranranx.aolie.monitor.service.MonitorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;

/**
 * @author xxl
 * @version V0.0.1
 * @date 2021/7/1 0021 11:13
 **/
@RestController
@RequestMapping("/monitor")
public class MonitorController {

    @Autowired
    private MonitorService queryService;

    /**
     * 取得在线数
     *
     * @return
     */
    @GetMapping("/getSessionCount")
    public int getSessionCount(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) {
        return SessionUtils.getOnlineUserNum();
    }

    /**
     * 热点访问表
     *
     * @return
     */
    @GetMapping("/getHotTableData")
    public List<Object[]> getHotTableData() {
        return queryService.getHotTableData();
    }

    /**
     * 热点访问方案
     *
     * @return
     */
    @GetMapping("/getHotSchemaData")
    public List<Object[]> getHotSchemaData() {
        return queryService.getHotSchemaData();
    }


    /**
     * 查询累计时间最长的TOP10
     *
     * @return
     */
    @GetMapping("/getOptLastTimeQueryData")
    public List<Object[]> getOptLastTimeQueryData() {
        return queryService.getOptLastTimeQueryData();
    }

    /**
     * 操作时间最长的更新TOP10
     *
     * @return
     */
    @GetMapping("/getOptLastTimeUpdateData")
    public List<Object[]> getOptLastTimeUpdateData() {
        return queryService.getOptLastTimeUpdateData();
    }

    /**
     * 操作次数最多的变更TOP10
     *
     * @return
     */
    @GetMapping("/getOptTimesUpdateData")
    public List<Object[]> getOptTimesUpdateData() {
        return queryService.getOptTimesUpdateData();
    }

    /**
     * 最多的查询TOP10
     *
     * @return
     */
    @GetMapping("/getOptTimesQueryData")
    public List<Object[]> getOptTimesQueryData() {
        return queryService.getOptTimesQueryData();
    }

    /**
     * 最多的查询TOP10
     *
     * @return
     */
    @GetMapping("/getOptTimesByHourData")
    public List<Object[]> getOptTimesByHourData() {
        return queryService.getOptTimesByHourData();
    }

    /**
     * 查询服务累计最长时间TOP10
     *
     * @return
     */
    @GetMapping("/getControllerServiceTimeData")
    public List<Object[]> getControllerServiceTimeData() {
        return queryService.getControllerServiceTimeData();
    }
}
