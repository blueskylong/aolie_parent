package com.ranranx.aolie.monitor.service;

import java.util.List;

/**
 * 自定义综合查询的查询服务
 */
public interface MonitorService {

    /**
     * 热点访问表
     *
     * @return
     */
    List<Object[]> getHotTableData();

    /**
     * 热点访问方案
     *
     * @return
     */
    List<Object[]> getHotSchemaData();


    /**
     * 查询累计时间最长的TOP10
     *
     * @return
     */
    List<Object[]> getOptLastTimeQueryData();

    /**
     * 操作时间最长的更新TOP10
     *
     * @return
     */
    List<Object[]> getOptLastTimeUpdateData();

    /**
     * 操作次数最多的变更TOP10
     *
     * @return
     */
    List<Object[]> getOptTimesUpdateData();

    /**
     * 最多的查询TOP10
     *
     * @return
     */
    List<Object[]> getOptTimesQueryData();

    /**
     * 按小时统计操作频率
     *
     * @return
     */
    List<Object[]> getOptTimesByHourData();

    /**
     * @return
     */
    List<Object[]> getControllerServiceTimeData();

    /**
     * 收集在线人数
     */
    void saveOnlineUserNum();

}
