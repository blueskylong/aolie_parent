package com.ranranx.aolie.application.sysconfig.service;

import com.ranranx.aolie.common.types.HandleResult;

/**
 * 数据库相关服务
 *
 * @author xxl
 * @version V0.0.1
 * @date 2022/1/18 0018 14:26
 **/
public interface SysConfigDbService {


    /**
     * 更新配置的值
     *
     * @param id
     * @param value
     * @return
     */
    HandleResult updateConfigValue(long id, String value);

    /**
     * 取得小数参数值
     *
     * @param id
     * @return
     */
    Double getDoubleParamValue(long id);

    /**
     * 取得字符串参数值
     *
     * @param id
     * @return
     */
    String getStringParamValue(long id);

    /**
     * 取得长整型参数值
     *
     * @param id
     * @return
     */
    Long getLongParamValue(long id);
}
