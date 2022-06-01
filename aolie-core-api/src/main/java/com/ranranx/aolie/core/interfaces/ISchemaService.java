package com.ranranx.aolie.core.interfaces;

import com.ranranx.aolie.core.datameta.datamodel.Schema;
import com.ranranx.aolie.core.datameta.datamodel.TableInfo;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * 提供方案服务,只用于查询
 *
 * @author xxl
 * @version V0.0.1
 * @date 2022/5/24 0024 13:09
 **/
public interface ISchemaService {

    /**
     * 查找方案
     *
     * @param schemaId
     * @param version
     * @return
     */
    Schema getSchema(Long schemaId, String version);

    /**
     * 根据列ID定位方案
     *
     * @param colId
     * @param version
     * @return
     */
    Long getColumnSchemaId(Long colId, String version);

    /**
     * 根据表ID定位方案
     *
     * @param tableId
     * @param version
     * @return
     */
    Long getTableSchemaId(Long tableId, String version);

    /**
     * 取得表与服务的对应关系
     *
     * @return
     */
    Map<String, List<Long>> getDsServiceNameRelation();

    /**
     * 根据表名查询表定义
     *
     * @param tableName
     * @param version
     * @return
     */
    List<TableInfo> findTablesByTableName(String tableName, String version);
}
