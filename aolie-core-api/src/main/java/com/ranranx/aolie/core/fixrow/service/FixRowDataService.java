package com.ranranx.aolie.core.fixrow.service;

import com.ranranx.aolie.common.types.HandleResult;
import com.ranranx.aolie.core.datameta.datamodel.TableInfo;
import com.ranranx.aolie.core.handler.param.QueryParam;

import java.util.List;
import java.util.Map;

/**
 * @author xxl
 * @version V0.0.1
 * @date 2022/5/24 0024 23:05
 **/
public interface FixRowDataService {


    /**
     * 复制固定行数据
     *
     * @param mapKeyValue
     * @param tableInfo
     * @param oraQueryParam
     * @return
     */
    HandleResult copyFixTableRow(Map<String, Object> mapKeyValue, TableInfo tableInfo,
                                 QueryParam oraQueryParam);

    /**
     * 保存固定行的业务信息
     *
     * @param lstRow
     * @param mapKey
     * @param fixId
     * @param version
     * @return
     */
    HandleResult saveBusiFixData(List<Map<String, Object>> lstRow, Map<String, Object> mapKey, long fixId, String version);

    /**
     * 生成控制信息
     * TODO 这里如果固定内容升级了,应该如何处理,是需要解决的,当前只处理
     *
     * @param lstData
     * @param fixId
     * @param version
     */
    void makeFullControlInfo(List<Map<String, Object>> lstData, Long fixId, String version);
}
