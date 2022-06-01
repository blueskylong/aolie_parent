package com.ranranx.aolie.core.datameta.datamodel;

import com.ranranx.aolie.core.interfaces.IDmService;
import com.ranranx.aolie.core.interfaces.ISchemaService;
import com.ranranx.aolie.common.types.CommonUtils;
import com.ranranx.aolie.core.datameta.dto.ReferenceDto;
import com.ranranx.aolie.common.exceptions.InvalidParamException;
import org.apache.dubbo.config.annotation.DubboReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingClass;

import java.util.*;

/**
 * TODO 需要有一个刷新机制
 *
 * @author xxl
 * 暂时使用这个来手动组装, 并将过程中的数据缓存起来
 * @version V0.0.1
 * @date 2020/8/6 10:53
 **/
@ConditionalOnMissingClass("com.ranranx.aolie.core.AolieCoreApplication")
@org.springframework.stereotype.Component
public class SchemaHolder {
    Logger logger = LoggerFactory.getLogger(SchemaHolder.class);


    private static SchemaHolder thas;
    /**
     * 记录动态查询的次数,出现在新增的情况下
     */
    private static Map<String, Object> triedSchema = new HashMap<>();

    /**
     * 所有表信息 key:SCHEMA_VERSION value:Table
     */
    private static Map<String, TableInfo> mapTables = new HashMap<>();
    /**
     * 所有显示字段信息
     */
    private static Map<String, Component> mapFields = new HashMap<>();

    /**
     * 所有字段信息
     */
    private static Map<String, Column> mapColumns = new HashMap<>();

    /**
     * 公式缓存 ,key:COLUMN_ID_VERSION value:公式列表
     */
    private static Map<String, List<Formula>> mapFormula = new HashMap<>();

    /**
     * 数据库操作信息列表
     */
    private static Map<String, DataOperatorInfo> mapOperatorInfo = new HashMap<>();

    /**
     * 引用信息
     */
    private static Map<String, Reference> mapReference = null;


    /**
     * 版本对库
     */
    private static Map<String, Schema> mapSchema = new HashMap<>();

    /**
     * 方案远程服务
     */
    @DubboReference
    protected ISchemaService schemaService;

    @DubboReference
    protected IDmService dmService;


    public void setSchemaService(ISchemaService schemaService) {
        this.schemaService = schemaService;
    }

    public SchemaHolder() {

        SchemaHolder.thas = this;
    }

    public static SchemaHolder getInstance() {
        return SchemaHolder.thas;
    }


    public static Component getField(Long fieldId, String version) {
        return mapFields.get(CommonUtils.makeKey(fieldId.toString(), version));
    }

    public static TableInfo getTable(Long tableId, String version) {
        TableInfo tableInfo = mapTables.get(CommonUtils.makeKey(tableId.toString(), version));
        if (tableInfo == null) {
            Long schemaId = thas.schemaService.getTableSchemaId(tableId, version);
            if (schemaId == null) {
                return null;
            }
            Schema schema = thas.schemaService.getSchema(schemaId, version);
            setOneSchema(schema);
        }
        return mapTables.get(CommonUtils.makeKey(tableId.toString(), version));
    }

    public static DataOperatorInfo getDataOperatorInfo(Long id, String version) {
        return mapOperatorInfo.get(CommonUtils.makeKey(id.toString(), version));
    }


    public static Column getColumn(Long id, String version) {
        Column column = mapColumns.get(CommonUtils.makeKey(id.toString(), version));
        if (column == null) {
            Long schemaId = thas.schemaService.getColumnSchemaId(id, version);
            if (schemaId == null) {
                return null;
            }
            Schema schema = thas.schemaService.getSchema(schemaId, version);
            setOneSchema(schema);
        }
        return mapColumns.get(CommonUtils.makeKey(id.toString(), version));
    }

    public static Reference getReference(Long id, String version) {
        String key = CommonUtils.makeKey(id.toString(), version);
        if (mapReference == null) {
            synchronized (Reference.class) {
                if (mapReference == null) {
                    SchemaHolder.thas.initReference(version);
                }
            }
        }
        //TODO 增加缓存一样需要比较版本

        return mapReference.get(key);
    }

    public static List<Reference> getReferences(String version) {
        if (mapReference == null) {
            synchronized (Reference.class) {
                if (mapReference == null) {
                    SchemaHolder.thas.initReference(version);
                }
            }
        }
        List<Reference> lstRef = new ArrayList<>();
        mapReference.values().forEach(ref -> {
            if (ref.getReferenceDto().getVersionCode().equals(version)) {
                lstRef.add(ref);
            }
        });
        return lstRef;
    }


    /**
     * 取得并生成
     *
     * @param version
     * @return
     */

    public static Schema getSchema(Long schemaId, String version) {
        String code = CommonUtils.makeKey(schemaId.toString(),
                version);
        if (!mapSchema.containsKey(code)) {
            //如果没有,则使用远程调用取得数据
            Schema schema = thas.schemaService.getSchema(schemaId, version);
            setOneSchema(schema);
            return schema;
        }
        return mapSchema.get(code);
    }


    /**
     * 取得表关系,可以是多个
     *
     * @param versionCode
     * @param tableIds
     * @return
     */
    public static List<TableColumnRelation> getTableRelations(String versionCode, Long... tableIds) {
        if (tableIds == null || tableIds.length < 1) {
            return null;
        }
        long schemaId = SchemaHolder.getTable(tableIds[0], versionCode).getTableDto().getSchemaId();
        return getSchema(schemaId, versionCode).getTablesRelation(tableIds);
    }


    public static TableInfo findTableByTableName(String tableName, long schemaId, String version) {

        if (schemaId < 1) {
            List<TableInfo> lstTables = findTablesByTableName(tableName, version);
            if (lstTables == null) {
                return null;
            }
            if (lstTables.size() > 1) {
                throw new InvalidParamException("查询到多个表对象");
            }
            return lstTables.get(0);
        }
        Schema schema = getSchema(schemaId, version);
        if (schema == null) {
            return null;
        }
        return schema.findTableByName(tableName);
    }

    /**
     * 查询所有方案中包含的表
     *
     * @param tableName
     * @param version
     * @return
     */
    public static List<TableInfo> findTablesByTableName(String tableName, String version) {
        Iterator<Schema> iterator = mapSchema.values().iterator();
        List<TableInfo> lstResult = new ArrayList<>();
        while (iterator.hasNext()) {
            Schema schema = iterator.next();
            if (schema.getSchemaDto().getVersionCode().equals(version)) {
                TableInfo tableInfo = schema.findTableByName(tableName);
                if (tableInfo != null) {
                    lstResult.add(tableInfo);
                }
            }

        }
        if (lstResult.isEmpty()) {
            //如果没有查询到，则需要到远程端再查询一次,这里可以需要做攻击的预防
            if (triedSchema.containsKey(CommonUtils.makeKey(tableName, version))) {
                return lstResult;
            }
            triedSchema.put(CommonUtils.makeKey(tableName, version), null);
            getInstance().schemaService.findTablesByTableName(tableName, version);
            List<TableInfo> lstTable = getInstance().schemaService.findTablesByTableName(tableName, version);
            if (lstTable == null) {
                return lstResult;
            }
            //初始化方案
            lstTable.forEach(tableInfo -> {
                getSchema(tableInfo.getTableDto().getSchemaId(), tableInfo.getTableDto().getVersionCode());
            });
            return lstTable;


        }
        return lstResult;
    }

    /**
     * 根据DTO类查询表定义
     *
     * @param clazz
     * @param schemaId
     * @param version
     * @return
     */
    public static TableInfo findTableByDto(Class clazz, long schemaId, String version) {
        String tableName = CommonUtils.getTableName(clazz);
        Schema schema = getSchema(schemaId, version);
        if (schema == null) {
            return null;
        }
        return schema.findTableByName(tableName);
    }

    /**
     * 根据表ID查询表定义
     *
     * @param id
     * @param version
     * @return
     */
    public static TableInfo findTableById(Long id, String version) {
        return getTable(id, version);
    }

    /**
     * 根据传入的方案初始化缓存
     *
     * @param schema
     */
    private static void setOneSchema(Schema schema) {
        setSchemaTableAndColumnAndRef(schema);
    }

    private static void setSchemaTableAndColumnAndRef(Schema schema) {
        List<TableInfo> lstTable = schema.getLstTable();

        for (TableInfo tableInfo : lstTable) {
            mapTables.put(CommonUtils.makeKey(String.valueOf(tableInfo.getTableDto().getTableId()),
                    tableInfo.getTableDto().getVersionCode()), tableInfo);
            List<Column> lstColumn = tableInfo.getLstColumn();
            if (lstColumn != null && !lstColumn.isEmpty()) {
                lstColumn.forEach(column -> {
                    //增加列信息缓存
                    mapColumns.put(CommonUtils.makeKey(column.getColumnDto().getColumnId().toString()
                            , column.getColumnDto().getVersionCode()), column);
                    List<Formula> lstFormula = column.getLstFormula();
                    //增加列公式信息
                    if (lstFormula != null && !lstFormula.isEmpty()) {
                        lstFormula.forEach(formula -> {
                            String columnKey = CommonUtils.makeKey(formula.getFormulaDto().getColumnId().toString(),
                                    formula.getFormulaDto().getVersionCode());
                            List<Formula> formulas = mapFormula.computeIfAbsent(columnKey, (key) -> new ArrayList<Formula>());
                            mapFormula.put(columnKey, formulas);
                        });

                    }
                });
            }
            //增加表格中的引用缓存
//            List<ReferenceDto> lstReference = tableInfo.getLstReference();
//            if (lstReference != null && !lstReference.isEmpty()) {
//                lstReference.forEach(referenceDto -> {
//                    Reference reference = new Reference(referenceDto);
//                    mapReference.put(CommonUtils.makeKey(referenceDto.getRefId().toString(),
//                            referenceDto.getVersionCode()), reference);
//                });
//
//            }
        }
    }

    private void initReference(String versionCode) {
        mapReference = new HashMap<>();
        List<ReferenceDto> schemaReferences = dmService.findAllReferences(versionCode);
        if (schemaReferences == null || schemaReferences.isEmpty()) {
            return;
        }
        for (ReferenceDto dto : schemaReferences) {
            Reference reference = new Reference(dto);
            mapReference.put(CommonUtils.makeKey(dto.getRefId().toString(), dto.getVersionCode()), reference);
        }
    }

    //TODO 增加消息队列,如果配置有变化,需要更新数据
}
