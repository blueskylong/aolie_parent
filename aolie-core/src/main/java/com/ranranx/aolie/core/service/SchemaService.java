package com.ranranx.aolie.core.service;

import com.alibaba.fastjson.JSON;
import com.ranranx.aolie.common.exceptions.InvalidParamException;
import com.ranranx.aolie.common.exceptions.NotExistException;
import com.ranranx.aolie.common.interfaces.SessionStoreService;
import com.ranranx.aolie.common.types.CommonUtils;
import com.ranranx.aolie.core.datameta.datamodel.*;
import com.ranranx.aolie.core.datameta.dto.*;
import com.ranranx.aolie.core.interfaces.ISchemaService;
import com.ranranx.aolie.core.interfaces.UiService;
import org.apache.dubbo.config.annotation.DubboService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.*;

/**
 * @author xxl
 * 暂时使用这个来手动组装, 并将过程中的数据缓存起来
 * @version V0.0.1
 * @date 2020/8/6 10:53
 **/
@Service
@DubboService
public class SchemaService implements ISchemaService {
    Logger logger = LoggerFactory.getLogger(SchemaService.class);

    /**
     * 记录动态查询的次数,出现在新增的情况下
     */
    private Map<String, Object> triedSchema = new HashMap<>();

    /**
     * 所有表信息 key:SCHEMA_VERSION value:Table
     */
    private static Map<String, TableInfo> mapTables;
    /**
     * 所有显示字段信息
     */
    private static Map<String, Component> mapFields;

    /**
     * 所有字段信息
     */
    private static Map<String, Column> mapColumns;
    /**
     * 表和字段对应信息
     */
    private static Map<String, Long> mapFieldToTable;

    /**
     * 公式缓存 ,key:COLUMN_ID_VERSION value:公式列表
     */
    private static Map<String, List<Formula>> mapFormula;

    /**
     * 数据库操作信息列表
     */
    private static Map<String, DataOperatorInfo> mapOperatorInfo;

    /**
     * 引用信息
     */
    private static Map<String, Reference> mapReference;

    private DataModelService service;

    private UiService uiService;

    @Autowired
    private SessionStoreService redisService;

    @Autowired
    private SchemaHolder schemaHolder;


    /**
     * 版本对库
     */
    private static Map<String, Schema> mapSchema;

    public SchemaService(DataModelService service, UIServiceImpl uiService) {
        this.service = service;
        this.uiService = uiService;
    }

    public static Component getField(Long fieldId, String version) {
        return mapFields.get(CommonUtils.makeKey(fieldId.toString(), version));
    }

    public static TableInfo getTable(Long tableId, String version) {
        return mapTables.get(CommonUtils.makeKey(tableId.toString(), version));
    }

    public static DataOperatorInfo getDataOperatorInfo(Long id, String version) {
        return mapOperatorInfo.get(CommonUtils.makeKey(id.toString(), version));
    }


    public static Column getColumn(Long id, String version) {
        return mapColumns.get(CommonUtils.makeKey(id.toString(), version));
    }

    public static Reference getReference(Long id, String version) {
        return mapReference.get(CommonUtils.makeKey(id.toString(), version));
    }

    public List<ReferenceDto> getReferenceDtos(String version) {
        return service.findAllReferences(version);
    }


    /**
     * 初始化数据.
     */
    @PostConstruct
    public void refresh() {
        //生成所有SCHEMA，并存入到缓存中
        logger.info("---初始化方案");
        mapTables = new HashMap<>(20);
        mapFields = new HashMap<>(200);
        mapFieldToTable = new HashMap<>(200);
        mapSchema = new HashMap<>(200);
        mapOperatorInfo = new HashMap<>(200);
        mapColumns = new HashMap<>(200);
        mapReference = new HashMap<>(200);

        List<SchemaDto> allSchemaDto = service.findAllSchemaDto(true);
        if (allSchemaDto == null || allSchemaDto.isEmpty()) {
            return;
        }
        List<String> lstVersion = new ArrayList<>();
        for (SchemaDto dto : allSchemaDto) {
            initSchema(dto);
            if (lstVersion.indexOf(dto.getVersionCode()) == -1) {
                lstVersion.add(dto.getVersionCode());
                initReference(dto.getVersionCode());
            }
        }
        initOperator();
        logger.info("---初始化方案完成");
    }


    private void initOperator() {
        List<DataOperatorInfo> lstOperInfo = service.findAllOperatorInfo();
        if (lstOperInfo != null && !lstOperInfo.isEmpty()) {
            lstOperInfo.forEach(info -> {
                mapOperatorInfo.put(CommonUtils.makeKey(info.getOperatorDto().getId().toString(),
                        info.getOperatorDto().getVersionCode()), info);
            });
        }
    }

    public void initSchema(Long schemaId, String version) {
        SchemaDto schemaDto = service.findSchemaDto(schemaId, version);
        if (schemaDto == null) {
            return;
        }
        initSchema(schemaDto);
    }

    /**
     * 初始化单个方案
     *
     * @param dto
     */
    public void initSchema(SchemaDto dto) {

        clearViewChange(dto.getSchemaId(), dto.getVersionCode());
        service.clearSchemaCache(dto.getSchemaId(), dto.getVersionCode());
        service.clearSchemaCache2(dto.getVersionCode());
        clearCache(dto.getSchemaId(), dto.getVersionCode());

        Schema schema = new Schema(dto);
//        initReference(schema);
        setSchemaTable(schema);
        setTableColumn(schema);
        setTableReference(schema);
        setSchemaConstraint(schema);
        setSchemaFormula(schema);
        setSchemaRelation(schema);
        setFixInfo(schema);
        mapSchema.put(CommonUtils.makeKey(dto.getSchemaId().toString(),
                dto.getVersionCode()), schema);
        //增加版本缓存
        redisService.setValue(SchemaTools.getSchemaVersionCacheKey(dto.getSchemaId(), dto.getVersionCode())
                , dto.getLastUpdateDate());
        //增加缓存体
        redisService.setValue(SchemaTools.getSchemaCacheKey(dto.getSchemaId(),
                dto.getVersionCode()), JSON.toJSONString(schema));
    }

    private void clearViewChange(Long schemaId, String version) {
        List<BlockViewDto> blockViews = uiService.getBlockViews(schemaId);
        if (blockViews != null) {
            for (BlockViewDto blockView : blockViews) {
                uiService.clearViewCache(blockView.getBlockViewId(), version);
            }
        }

    }

    private void clearCache(long schemaId, String version) {
        if (mapTables != null && !mapTables.isEmpty()) {
            Iterator<Map.Entry<String, TableInfo>> iterator = mapTables.entrySet().iterator();
            while (iterator.hasNext()) {
                Map.Entry<String, TableInfo> entry = iterator.next();
                if (entry.getValue().getTableDto().getSchemaId() == schemaId
                        && entry.getValue().getTableDto().getVersionCode().equals(version)) {
                    iterator.remove();
                }
            }
        }
        if (mapColumns != null && !mapColumns.isEmpty()) {
            Iterator<Map.Entry<String, Column>> iterator = mapColumns.entrySet().iterator();
            while (iterator.hasNext()) {
                Map.Entry<String, Column> entry = iterator.next();
                if (entry.getValue().getColumnDto().getSchemaId() == schemaId
                        && entry.getValue().getColumnDto().getVersionCode().equals(version)) {
                    iterator.remove();
                }
            }
        }
        if (mapFormula != null && !mapFormula.isEmpty()) {
            Iterator<Map.Entry<String, List<Formula>>> iterator = mapFormula.entrySet().iterator();
            while (iterator.hasNext()) {
                Map.Entry<String, List<Formula>> entry = iterator.next();
                if (entry.getValue().get(0).getFormulaDto().getSchemaId() == schemaId
                        && entry.getValue().get(0).getFormulaDto().getVersionCode().equals(version)) {
                    iterator.remove();
                }
            }
        }
    }

    public void initSchema(long schemaId, String version) {
        if (schemaId < 0 || CommonUtils.isEmpty(version)) {
            throw new InvalidParamException("初始化失败,参数不下正确,schemaId:" + schemaId + "; version:" + version);
        }
        SchemaDto schemaDto = service.findSchemaDto(schemaId, version);
        if (schemaDto == null) {
            throw new NotExistException("指定的方案不存在:schemaId:" + schemaId + "; version:" + version);
        }
        this.initSchema(schemaDto);
    }

    private void setSchemaRelation(Schema schema) {
        List<TableColumnRelationDto> lstDto = service.findRelationDto(schema.getSchemaDto().getSchemaId(),
                schema.getSchemaDto().getVersionCode());
        if (lstDto == null || lstDto.isEmpty()) {
            return;
        }
        List<TableColumnRelation> lstRelation = new ArrayList<>();
        lstDto.forEach(((dto) -> {
            TableColumnRelation re = new TableColumnRelation();
            re.setDto(dto);
            re.setTableFrom(getTable(getColumn(dto.getFieldFrom(), dto.getVersionCode()).getColumnDto().getTableId(), dto.getVersionCode()));
            re.setTableTo(getTable(getColumn(dto.getFieldTo(), dto.getVersionCode()).getColumnDto().getTableId(), dto.getVersionCode()));
            lstRelation.add(re);
        }));
        schema.setLstRelation(lstRelation);
    }

    /**
     * 初始化方案中的约束
     *
     * @param schema
     */
    private void setSchemaConstraint(Schema schema) {
        List<ConstraintDto> lstDto = service.findSchemaConstraints(schema.getSchemaDto().getSchemaId(),
                schema.getSchemaDto().getVersionCode());
        if (lstDto == null || lstDto.isEmpty()) {
            return;
        }
        List<Constraint> lstConstraint = new ArrayList<>();
        for (ConstraintDto dto : lstDto) {
            lstConstraint.add(new Constraint(dto));
        }
        schema.setLstConstraint(lstConstraint);
    }

    /**
     * 初始化公式信息
     *
     * @param schema
     */
    private void setSchemaFormula(Schema schema) {
        List<FormulaDto> lstDto = service.findSchemaFormula(schema.getSchemaDto().getSchemaId(),
                schema.getSchemaDto().getVersionCode());
        if (lstDto == null || lstDto.isEmpty()) {
            return;
        }
        Formula formula;
//        List<Formula> lstFormula = new ArrayList<>();
        List<Formula> formulas;
        if (mapFormula == null) {
            mapFormula = new HashMap<>();
        }

        for (FormulaDto dto : lstDto) {
            formula = new Formula(dto);
//            lstFormula.add(formula);
            String columnKey = CommonUtils.makeKey(dto.getColumnId().toString(), dto.getVersionCode());
            formulas = mapFormula.get(columnKey);
            if (formulas == null) {
                formulas = new ArrayList<>();
                mapFormula.put(columnKey, formulas);
            }
            formulas.add(formula);
            Column column = mapColumns.get(columnKey);
            column.setLstFormula(formulas);

        }
//        schema.setLstFormula(lstFormula);
    }

    /**
     * 设置方案中的表信息,还没有包含字段信息
     *
     * @param schema
     */
    private void setSchemaTable(Schema schema) {
        List<TableDto> schemaTables = service.findSchemaTables(schema.getSchemaDto().getSchemaId(),
                schema.getSchemaDto().getVersionCode());
        if (schemaTables != null && !schemaTables.isEmpty()) {
            List<TableInfo> lstTable = new ArrayList<>();
            for (TableDto tableDto : schemaTables) {
                TableInfo table = new TableInfo(tableDto);
                lstTable.add(table);
                mapTables.put(CommonUtils.makeKey(String.valueOf(tableDto.getTableId()), tableDto.getVersionCode()), table);
            }
            schema.setLstTable(lstTable);
        }
    }

    private void setTableColumn(Schema schema) {
        List<ColumnDto> lstDto = service.findSchemaColumns(schema.getSchemaDto().getSchemaId(),
                schema.getSchemaDto().getVersionCode());
        if (lstDto == null || lstDto.isEmpty()) {
            return;
        }
        Column column;
        List<Column> lstColumn = new ArrayList<>();
        for (ColumnDto dto : lstDto) {
            column = new Column(dto, mapReference.get(dto.getRefId()));
            lstColumn.add(column);
            mapColumns.put(CommonUtils.makeKey(dto.getColumnId().toString()
                    , dto.getVersionCode()), column);
            //插入到对应的表中
            mapTables.get(CommonUtils.makeKey(dto.getTableId().toString(), dto.getVersionCode())).addColumn(column);
        }


    }

    private void initReference(String versionCode) {
        List<ReferenceDto> schemaReferences = service.findAllReferences(versionCode);
        if (schemaReferences == null || schemaReferences.isEmpty()) {
            return;
        }
        for (ReferenceDto dto : schemaReferences) {
            Reference reference = new Reference(dto);
            mapReference.put(CommonUtils.makeKey(dto.getRefId().toString(), dto.getVersionCode()), reference);
        }
    }

    /**
     * 装配引用信息
     *
     * @param schema
     */
    private void setTableReference(Schema schema) {
        //如果是全局引用的方案,才需要配置引用信息
        if (!SchemaTools.isReferenceSchema(schema.getSchemaDto().getSchemaId())) {
            return;
        }
        List<ReferenceDto> schemaReferences = service.findAllReferences(schema.getSchemaDto().getVersionCode());
        List<TableInfo> lstTable = schema.getLstTable();
        if (lstTable == null || lstTable.isEmpty()) {
            return;
        }
        Map<Long, List<ReferenceDto>> mapReference = makeReferenceMap(schemaReferences);
        if (mapReference.isEmpty()) {
            return;
        }
        for (TableInfo info : lstTable) {
            info.setLstReference(mapReference.get(info.getTableDto().getTableId()));
        }
    }


    private void setFixInfo(Schema schema) {

    }

    /**
     * 将引用分类组装
     *
     * @param schemaReferences
     * @return
     */
    private Map<Long, List<ReferenceDto>> makeReferenceMap(List<ReferenceDto> schemaReferences) {
        if (schemaReferences == null || schemaReferences.isEmpty()) {
            return new HashMap<>();
        }
        Map<Long, List<ReferenceDto>> result = new HashMap<>();
        for (ReferenceDto dto : schemaReferences) {
            List<ReferenceDto> referenceDtos = result.get(dto.getTableId());
            if (referenceDtos == null) {
                referenceDtos = new ArrayList<>();
                result.put(dto.getTableId(), referenceDtos);
            }
            referenceDtos.add(dto);
        }
        return result;
    }

    /**
     * 取得并生成
     *
     * @param version
     * @return
     */

    @Override
    public Schema getSchema(Long schemaId, String version) {
        String code = CommonUtils.makeKey(schemaId.toString(),
                version);
        return mapSchema.get(code);
    }


    /**
     * 根据列ID定位方案
     *
     * @param colId
     * @param version
     * @return
     */
    @Override
    public Long getColumnSchemaId(Long colId, String version) {
        Column column = getColumn(colId, version);
        if (column == null) {
            return null;
        }
        return column.getColumnDto().getSchemaId();
    }

    /**
     * 根据表ID定位方案
     *
     * @param tableId
     * @param version
     * @return
     */
    @Override
    public Long getTableSchemaId(Long tableId, String version) {
        TableInfo table = getTable(tableId, version);
        if (table == null) {
            return null;
        }
        return table.getTableDto().getSchemaId();
    }

    /**
     * 取得表与服务的对应关系
     *
     * @return
     */
    @Override
    public Map<String, List<Long>> getDsServiceNameRelation() {
        Map<String, List<Long>> result = new HashMap<>();
        SchemaService.mapSchema.entrySet().forEach((entry) -> {
            Schema schema = entry.getValue();
            String applicationCode = schema.getSchemaDto().getApplicationCode();
            List<TableInfo> lstTable = schema.getLstTable();
            if (lstTable == null || lstTable.isEmpty()) {
                return;
            }
            List<Long> lstTableId = new ArrayList<>();
            lstTable.forEach(table -> {
                lstTableId.add(table.getTableDto().getTableId());
            });
            List<Long> lstId = result.computeIfAbsent(applicationCode, key -> new ArrayList<>());
            lstId.addAll(lstTableId);
            result.put(applicationCode, lstId);

        });
        return result;
    }

    /**
     * 根据表名查询表定义
     *
     * @param tableName
     * @param version
     * @return
     */
    @Override
    public List<TableInfo> findTablesByTableName(String tableName, String version) {
        Iterator<TableInfo> iterator = mapTables.values().iterator();
        List<TableInfo> lstResult = new ArrayList<>();
        while (iterator.hasNext()) {
            TableInfo next = iterator.next();
            if (next.getTableDto().getTableName().equalsIgnoreCase(tableName)
                    && next.getTableDto().getVersionCode().equalsIgnoreCase(version)) {
                lstResult.add(next);
            }
        }
        return lstResult;
    }


}
