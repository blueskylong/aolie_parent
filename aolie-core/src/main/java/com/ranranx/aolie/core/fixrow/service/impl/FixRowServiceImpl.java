package com.ranranx.aolie.core.fixrow.service.impl;

import com.ranranx.aolie.core.datameta.datamodel.*;
import com.ranranx.aolie.core.datameta.dto.ColumnDto;
import com.ranranx.aolie.core.datameta.dto.ComponentDto;
import com.ranranx.aolie.core.datameta.dto.TableDto;
import com.ranranx.aolie.core.ds.dataoperator.DataOperatorFactory;
import com.ranranx.aolie.common.exceptions.InvalidConfigException;
import com.ranranx.aolie.common.exceptions.InvalidDataException;
import com.ranranx.aolie.common.exceptions.NotExistException;
import com.ranranx.aolie.core.fixrow.dto.FixData;
import com.ranranx.aolie.core.fixrow.dto.FixMain;
import com.ranranx.aolie.core.fixrow.dto.FixRelation;
import com.ranranx.aolie.core.fixrow.service.FixRowService;
import com.ranranx.aolie.core.handler.HandlerFactory;
import com.ranranx.aolie.core.handler.param.DeleteParam;
import com.ranranx.aolie.core.handler.param.InsertParam;
import com.ranranx.aolie.core.handler.param.QueryParam;
import com.ranranx.aolie.core.handler.param.UpdateParam;
import com.ranranx.aolie.core.runtime.JQParameter;
import com.ranranx.aolie.core.service.BaseDbService;
import com.ranranx.aolie.core.service.LocalDmDataService;
import com.ranranx.aolie.core.service.UIServiceImpl;
import com.ranranx.aolie.common.tree.LevelProvider;
import com.ranranx.aolie.common.tree.Node;
import com.ranranx.aolie.common.tree.SysCodeRule;
import com.ranranx.aolie.common.tree.TreeNodeHelper;
import com.ranranx.aolie.common.types.CommonUtils;
import com.ranranx.aolie.common.types.Constants;
import com.ranranx.aolie.common.types.HandleResult;
import com.ranranx.aolie.common.types.IdGenerator;
import com.ranranx.aolie.core.tools.SchemaHolderLocal;
import org.apache.dubbo.config.annotation.DubboService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

/**
 * @author xxl
 * @version V0.0.1
 * @date 2021/8/8 0008 10:45
 **/
@Service
@Transactional(readOnly = true)
@DubboService
public class FixRowServiceImpl extends BaseDbService implements FixRowService {


    private Map<String, Node> mapFixDataAsNode = new HashMap<>();


    /**
     * 固定列信息的固定属性列的定义视图。
     */
    private static final Long FIX_COL_OPERCOL = 1002L;
    @Autowired
    private DataOperatorFactory factory;

    @Autowired
    private HandlerFactory handlerFactory;

    @Autowired
    private UIServiceImpl uiService;

    @Autowired
    private LocalDmDataService modelApi;


    /**
     * 查询固定行表头
     * 表头有二个来源，一个数据表固定列，一个是固定行数据内容视图
     *
     * @param fixId
     * @param version
     * @return
     */
    @Override
    public BlockViewer findFixRowComponents(Long fixId, String version) {
        FixMain fixMain = new FixMain();
        fixMain.setFixId(fixId);
        fixMain = queryOne(fixMain, Constants.DEFAULT_DM_SCHEMA);
        Long tableId = fixMain.getTableId();
        TableInfo tableBusiness = SchemaHolder.getTable(tableId, version);

        List<Component> lstFixBusCols = genCompFromTableFixCol(tableBusiness);
        if (lstFixBusCols == null || lstFixBusCols.isEmpty()) {
            throw new InvalidConfigException("没有查询到固定列信息");
        }
        BlockViewer fixViewer = combineComponent(tableBusiness, lstFixBusCols);
        BlockViewer fixViewerCopy = CommonUtils.deepClone(fixViewer);
        fixViewerCopy.getLstComponent().addAll(lstFixBusCols);
        return fixViewerCopy;
    }

    /**
     * 查询 并合并字段
     *
     * @param tableBusiness
     * @param lstFixBusCols
     * @return
     */
    private BlockViewer combineComponent(TableInfo tableBusiness, List<Component> lstFixBusCols) {

        //检查是否有级次和名称字段
        boolean hasLvlCode = false;
        boolean hasName = false;
        Component comLvl;
        Component comName;
        String fieldName;
        Component comId = null;
        for (Component com : lstFixBusCols) {
            fieldName = com.getColumn().getColumnDto().getFieldName();
            if (fieldName.equals(FIELD_LVL)) {
                hasLvlCode = true;
            } else if (fieldName.equals(FIELD_NAME)) {
                hasName = true;
            } else if (fieldName.equals(FIELD_DATA_ID)) {
                //删除
                comId = com;
            }
        }
        if (comId != null) {
            lstFixBusCols.remove(comId);
        }
        //将固定列接入到后面
        LevelProvider provider = new LevelProvider("900");
        BlockViewer fixViewer = uiService.getViewerInfo(FIX_COL_OPERCOL, tableBusiness.getTableDto().getVersionCode());

        if (fixViewer == null) {
            throw new NotExistException("固定行固定属性列视图没有定义");
        }
        List<Component> lstFixComponent = fixViewer.getLstComponent();
        for (int i = lstFixComponent.size() - 1; i >= 0; i--) {
            Component component = lstFixComponent.get(i);
            if (component.getColumn().getColumnDto().getFieldName().equals(FIELD_NAME)) {
                if (hasName) {
                    lstFixComponent.remove(i);
                    continue;
                } else {
                    component.getComponentDto().setLvlCode("000");
                }
                continue;
            }

            if (component.getColumn().getColumnDto().getFieldName().equals(FIELD_LVL)) {
                if (hasLvlCode) {
                    lstFixComponent.remove(i);
                    continue;
                } else {
                    component.getComponentDto().setLvlCode("00-");
                }
                continue;
            }

            component.getComponentDto().setLvlCode(provider.getNextCode());
        }
        return fixViewer;
    }

    private List<Component> genCompFromTableFixCol(TableInfo tableInfo) {
        List<Column> lstColumn = tableInfo.getLstColumn();
        if (lstColumn == null || lstColumn.isEmpty()) {
            return null;
        }
        List<Component> lstResult = new ArrayList<>();
        LevelProvider levelProvider = new LevelProvider("600");
        for (Column column : lstColumn) {
            if (new Short((short) 1).equals(column.getColumnDto().getContentFix())) {
                lstResult.add(genDefaultDto(column, levelProvider.getNextCode()));
            }
        }
        return lstResult;
    }

    /**
     * 生成默认的控件信息
     *
     * @param col
     * @param lvlCode
     * @return
     */
    private Component genDefaultDto(Column col, String lvlCode) {
        Component com = new Component();
        com.setColumn(col);
        ComponentDto dto = new ComponentDto();
        dto.setLvlCode(lvlCode);
        dto.setTitle(col.getColumnDto().getTitle());
        dto.setDispType(genDefaultDispType(col.getColumnDto()));
        dto.setComponentId(IdGenerator.getNextId(null));
        dto.setHorSpan(12);
        dto.setTitleSpan(3);
        com.setComponentDto(dto);
        return com;
    }

    private String genDefaultDispType(ColumnDto dto) {
        if (dto.getRefId() != null) {
            return Constants.ComponentType.select;
        }
        String fieldType = dto.getFieldType();
        switch (fieldType) {
            case DmConstants.FieldType.INT:
            case DmConstants.FieldType.DECIMAL:
                return Constants.ComponentType.number;
            case DmConstants.FieldType.DATETIME:
                return Constants.ComponentType.date;
            case DmConstants.FieldType.TEXT:
                return Constants.ComponentType.textarea;
            default:
                return Constants.ComponentType.text;
        }
    }

    /**
     * 同步更新固定行对应设置
     *
     * @param schemaId
     */
    @Override
    @Transactional(readOnly = false)
    public void syncFixSet(Long schemaId, String version) {
        //删除多余的
        deleteUnfixConfig(version);
        //增加新的，并关联
        List<TableDto> needToAddRelation = findNeedToAddRelation(schemaId, version);
        if (needToAddRelation != null && !needToAddRelation.isEmpty()) {
            addFixRelation(needToAddRelation, version);
        }
        //更新需要更新的
        List<FixMain> lstNeedUpdate = findNeedUpdateRelation(version);
        if (lstNeedUpdate != null && !lstNeedUpdate.isEmpty()) {
            for (FixMain fixMain : lstNeedUpdate) {
                handleOneFix(fixMain.getTableId(), fixMain.getVersionCode(), fixMain.getFixId());
            }
        }

    }

    /**
     * 将固定数据生成状态结构(配置信息)
     *
     * @param fixId
     * @param version
     * @return
     */
    @Override
    public Node<FixData> getFixDataAsTree(Long fixId, String version) {
        String key = CommonUtils.makeKey(fixId.toString(), version);
        Node<FixData> node = mapFixDataAsNode.get(key);
        if (node == null) {
            node = makeFixDataToNode(fixId, version);
            mapFixDataAsNode.put(key, node);
        }
        return node;
    }

    /**
     * 生成固定行明细设置的树状结构
     *
     * @param fixId
     * @param version
     * @return
     */
    @Override
    public Node<FixData> makeFixDataToNode(Long fixId, String version) {
        List<FixData> lstData = findFixDataDto(fixId, version);
        if (lstData == null || lstData.isEmpty()) {
            return null;
        }
        try {
            return TreeNodeHelper.getInstance().generateByCode(lstData, "dataId",
                    "lvlCode", "itemName", SysCodeRule.createDefault());
        } catch (Exception e) {
            throw new InvalidConfigException("生成树状结构失败:" + e.getMessage());
        }

    }

    /**
     * 查询固定行明细的设置
     *
     * @param fixId
     * @param version
     * @return
     */
    @Override
    public List<FixData> findFixDataDto(Long fixId, String version) {
        FixData data = new FixData();
        data.setFixId(fixId);
        data.setVersionCode(version);
        return queryList(data, Constants.DEFAULT_DM_SCHEMA);


    }


    private void addFixRelation(List<TableDto> lstTable, String version) {
        for (TableDto dto : lstTable) {
            Long fixId = addNewFixMain(dto.getTableId(), dto.getTitle(), dto.getTableName(), dto.getSchemaId());
            handleOneFix(dto.getTableId(), version, fixId);
        }
    }

    private TableInfo findFixDataTableInfo(String version) {
        return SchemaHolder.findTableByDto(FixData.class, Constants.DEFAULT_DM_SCHEMA, version);
    }

    /**
     * 新增加关联主表信息,
     *
     * @param tableId
     * @param title
     * @param tableName
     * @param schemaId
     * @return 返回新的ID
     */
    private Long addNewFixMain(Long tableId, String title, String tableName, Long schemaId) {
        FixMain main = new FixMain();
        main.setFixId(-1L);
        main.setMemo(title + "[" + tableName + "]");
        main.setTitle(title);
        main.setTableId(tableId);
        InsertParam param = new InsertParam();
        param.setObject(Constants.DEFAULT_DM_SCHEMA, main);
        List<Map<String, Object>> lstData = handlerFactory.handleInsert(param).getData();
        List<List<Long>> ids = (List<List<Long>>) lstData.get(0).get(Constants.ConstFieldName.CHANGE_KEYS_FEILD);
        return ids.get(0).get(1);


    }

    /**
     * 取得需要增加的关联关系
     *
     * @param schemaId
     * @param version
     * @return
     */
    private List<TableDto> findNeedToAddRelation(Long schemaId, String version) {
        QueryParam param = new QueryParam();
        TableDto dto = new TableDto();
        dto.setSchemaId(schemaId);
        dto.setIsFixrow((short) 1);
        param.setFilterObjectAndTableAndResultType(Constants.DEFAULT_DM_SCHEMA, version, dto);
        QueryParam existsParam = new QueryParam();
        existsParam.setTableDto(Constants.DEFAULT_DM_SCHEMA, FixMain.class, version);
        param.appendCriteria().andNotExists(existsParam);
        return handlerFactory.handleQuery(param).getData();
    }

    /**
     * 取得需要更新的关联关系
     *
     * @param version
     * @return
     */
    private List<FixMain> findNeedUpdateRelation(String version) {
        QueryParam param = new QueryParam();
        param.setTableDto(Constants.DEFAULT_DM_SCHEMA, FixMain.class, version);
        param.setResultClass(FixMain.class);
        QueryParam existsParam = new QueryParam();
        TableDto dto = new TableDto();
        dto.setIsFixrow((short) 1);
        existsParam.setFilterObjectAndTable(Constants.DEFAULT_DM_SCHEMA, version, dto);
        param.appendCriteria().andExists(existsParam);
        return handlerFactory.handleQuery(param).getData();
    }


    /**
     * 删除不存在的
     */
    private void deleteUnfixConfig(String version) {
        //更新表是否有固定列的标记,更新成没有的
        UpdateParam updateParam;
        TableDto tableDto = new TableDto();
        tableDto.setIsFixrow((short) 0);
        updateParam = UpdateParam.genUpdateParamByFilter(Constants.DEFAULT_DM_SCHEMA, version, tableDto, true);
        QueryParam paramQuery = new QueryParam();
        ColumnDto columnDto = new ColumnDto();
        columnDto.setContentFix((short) 1);
        paramQuery.setFilterObjectAndTable(Constants.DEFAULT_DM_SCHEMA, version, columnDto);
        updateParam.appendCriteria().andNotExists(paramQuery);
        handlerFactory.handleUpdate(updateParam);
        //更新成有的
        tableDto = new TableDto();
        tableDto.setIsFixrow((short) 1);
        updateParam = UpdateParam.genUpdateParamByFilter(Constants.DEFAULT_DM_SCHEMA, version, tableDto, true);
        paramQuery = new QueryParam();
        columnDto = new ColumnDto();
        columnDto.setContentFix((short) 1);
        paramQuery.setFilterObjectAndTable(Constants.DEFAULT_DM_SCHEMA, version, columnDto);
        updateParam.appendCriteria().andExists(paramQuery);
        handlerFactory.handleUpdate(updateParam);

        //删除不需要固定配置的表
        QueryParam existsParam = new QueryParam();
        TableDto dto = new TableDto();
        dto.setIsFixrow((short) 1);
        existsParam.setFilterObjectAndTable(Constants.DEFAULT_DM_SCHEMA, version, dto);
        DeleteParam param = new DeleteParam();
        param.setTableDto(Constants.DEFAULT_DM_SCHEMA, FixMain.class);
        param.appendCriteria().andNotExists(existsParam);
        handlerFactory.handleDelete(param);
    }

    private void handleOneFix(Long dsId, String version, Long fixId) {
        FixRelation relationDto = new FixRelation();
        relationDto.setFixId(fixId);
        List<FixRelation> lstRelation = this.queryList(relationDto, Constants.DEFAULT_DM_SCHEMA);
        autoColRelation(findFixCols(dsId, version), findFixDataTableInfo(version).getLstColumn(), lstRelation, fixId);
    }

    private List<ColumnDto> findFixCols(Long tableId, String version) {

        //这里不能直接使用缓存中的表信息，因为当前正在保存，还没有刷新缓存
        List<ColumnDto> lstCol = new ArrayList<>();
        List<ColumnDto> lstAllCol = findTableCols(tableId,
                version);
        for (ColumnDto col : lstAllCol) {
            if (CommonUtils.isTrue(col.getContentFix())) {
                lstCol.add(col);
            }
        }
        return lstCol;
    }

    private List<ColumnDto> findTableCols(Long tableId, String version) {
        ColumnDto dto = new ColumnDto();
        dto.setTableId(tableId);
        dto.setVersionCode(version);
        return queryList(dto, Constants.DEFAULT_DM_SCHEMA);
    }


    /**
     * 更新一个表的关系
     * 即找到增加，删除，修改的记录
     * <p>
     * 自动的规则，必须存在的字段，需要有，如LVL_CODE,ITEM_NAME,DATA_ID，这些是控制字段，其它按类型分配字段，已分配的继续使用
     * 业务表的主键字段排队对应
     *
     * @param lstBusiCol
     * @param lstAllFields
     * @param lstRelation
     */
    private void autoColRelation(List<ColumnDto> lstBusiCol, List<Column> lstAllFields, List<FixRelation> lstRelation, long fixId) {
        if (lstRelation == null) {
            lstRelation = new ArrayList<>();
        }
        if (lstBusiCol == null || lstBusiCol.isEmpty()) {
            throw new InvalidConfigException("业务表没有字段");
        }
        FixDataTableColumnDeliver deliver = new FixDataTableColumnDeliver(lstAllFields);
        Map<Long, Long> mapExistsRelation = genMapRelation(lstRelation);
        Map<Long, Long> mapRelation = genRelationInfo(lstRelation);
        //需要增加关联的
        List<ColumnDto> toAdd = new ArrayList<>();
        //需要删除关联的
        List<Long> toDelete = new ArrayList<>();
        Long destColId;
        for (ColumnDto colDto : lstBusiCol) {
            destColId = colDto.getColumnId();
            //主健字段,不参与对应
            if (colDto.getIsKey() != null && colDto.getIsKey().equals((short) 1)) {
                //如果已存在，则还要删除
                if (mapExistsRelation.containsKey(destColId)) {
                    toDelete.add(findDeleteRelationId(destColId, lstRelation));
                    deliver.recoverOneField(mapExistsRelation.get(destColId), colDto.getFieldType());
                }
                continue;
            }

            if (!mapExistsRelation.containsKey(destColId)) {
                //如果还没有分配字段
                toAdd.add(colDto);
            } else {
                //如果存在，则还需要检查类型是不是还是一致的
                String destType = colDto.getFieldType();
                Long sourceId = mapExistsRelation.get(destColId);
                String sourceType = deliver.removeOneField(sourceId);
                if (!destType.equals(sourceType)) {
                    //如果分配的字段与当前不匹配，则要删除原来的分配，重新分配一个字段，收回原来分配的字段
                    // 注：如果有数据了，则会对原来的数据有影响
                    toDelete.add(findDeleteRelationId(destColId, lstRelation));
                    deliver.recoverOneField(sourceId, sourceType);
                    toAdd.add(colDto);
                }
                //认为正常存在
                mapRelation.remove(destColId);
            }

        }
        //开始执行分配
        List<FixRelation> lstAdd = null;
        if (!toAdd.isEmpty()) {
            lstAdd = genAddRelation(toAdd, deliver, fixId);

        }
        //这里检查必要字段是不是已完成分配
        String reserveFields = deliver.getReserveFields();
        if (CommonUtils.isNotEmpty(reserveFields)) {
            throw new InvalidConfigException("业务表表头需要有以下字段：" + reserveFields);
        }
        //执行增加
        if (lstAdd != null) {
            saveAddRelation(lstAdd, deliver, fixId);
        }
        //收集多余的关联
        if (!mapRelation.isEmpty()) {
            mapRelation.values().forEach((value) -> toDelete.add(value));
        }
        if (!toDelete.isEmpty()) {
            deleteRelation(toDelete);
        }
    }

    private void deleteRelation(List<Long> lstId) {
        DeleteParam param = new DeleteParam();
        param.setIds(lstId);
        param.setTableDto(Constants.DEFAULT_DM_SCHEMA, FixRelation.class);
        handlerFactory.handleDelete(param);

    }

    private void saveAddRelation(List<FixRelation> lstAdd, FixDataTableColumnDeliver deliver, Long fixId) {

        InsertParam param = new InsertParam();
        param.setObjects(Constants.DEFAULT_DM_SCHEMA, lstAdd);
        handlerFactory.handleInsert(param);
    }

    /**
     * 生成新的关联关系
     *
     * @param lstNeed
     * @param deliver
     * @param fixId
     * @return
     */
    private List<FixRelation> genAddRelation(List<ColumnDto> lstNeed, FixDataTableColumnDeliver deliver, Long fixId) {
        List<FixRelation> lstResult = new ArrayList<>();
        long newId = -1;
        for (ColumnDto colDto : lstNeed) {
            FixRelation relation = new FixRelation();
            relation.setFixId(fixId);
            relation.setDetailId(newId--);
            relation.setDestColumnId(colDto.getColumnId());
            relation.setSourceColumnId(deliver.getOneField(colDto.getFieldType(),
                    colDto.getFieldName()));
            lstResult.add(relation);
        }
        return lstResult;
    }

    /**
     * 查找删除的ＩＤ
     *
     * @param destColId
     * @param lstRelation
     * @return
     */
    private Long findDeleteRelationId(Long destColId, List<FixRelation> lstRelation) {
        for (FixRelation relation : lstRelation) {
            if (relation.getDestColumnId().equals(destColId)) {
                return relation.getDetailId();
            }
        }
        return null;
    }

    /**
     * 生成字段和对应关系ID的对应关系
     *
     * @param lstRelation
     * @return
     */
    private Map<Long, Long> genRelationInfo(List<FixRelation> lstRelation) {
        Map<Long, Long> result = new HashMap<>();
        if (lstRelation != null) {
            for (FixRelation relation : lstRelation) {
                result.put(relation.getDestColumnId(), relation.getDetailId());
            }
        }
        return result;
    }

    /**
     * 转换成对应关系
     * 业务表字段 对应  固定行数据表字段
     *
     * @param lstRelation
     * @return
     */
    Map<Long, Long> genMapRelation(List<FixRelation> lstRelation) {
        Map<Long, Long> mapResult = new HashMap<>();
        if (lstRelation != null && !lstRelation.isEmpty()) {
            for (FixRelation relation : lstRelation) {
                mapResult.put(relation.getDestColumnId(), relation.getSourceColumnId());
            }
        }
        return mapResult;
    }

    /**
     * 管理固定行数据表所有字段信息
     */
    static class FixDataTableColumnDeliver {
        //必须分配
        static List<String> reserveFields = Arrays.asList(new String[]{"lvl_code", "item_name", "data_id"});
        //这些不可分配
        static List<String> excludeFields = Arrays.asList(new String[]{"fix_id", "version_code", "create_date", "last_update_date",
                "create_user", "last_update_user", "is_include", "sum_up", "can_delete", "can_insert"});

        Map<String, List<Long>> mapValidField = new HashMap<>();
        //必须分配的字段信息
        Map<String, Column> mapReserveFields = new HashMap<>();

        FixDataTableColumnDeliver(List<Column> lstFields) {
            //以下初始化
            String colName;
            for (Column col : lstFields) {
                colName = col.getColumnDto().getFieldName();
                if (reserveFields.indexOf(colName) != -1) {
                    mapReserveFields.put(colName, col);
                    continue;
                }
                if (excludeFields.indexOf(colName) != -1) {
                    continue;
                }
                String fieldType = col.getColumnDto().getFieldType();
                List<Long> colIds = mapValidField.computeIfAbsent(fieldType, (key) -> new ArrayList<Long>());
                colIds.add(col.getColumnDto().getColumnId());
            }
        }

        /**
         * 取得未分配的保留字段
         *
         * @return
         */
        public String getReserveFields() {
            if (mapReserveFields.isEmpty()) {
                return null;
            }
            StringBuilder sb = new StringBuilder();
            mapReserveFields.keySet().forEach(key -> sb.append(key).append(","));
            return sb.substring(0, sb.length() - 1);
        }

        /**
         * 分配一个字段
         *
         * @param fieldType
         * @return
         */
        Long getOneField(String fieldType, String fieldName) {
            //优先分配必须同名
            if (mapReserveFields.containsKey(fieldName)) {
                Column rCol = mapReserveFields.remove(fieldName);
                //还要检查类型是不是一样
                if (!fieldType.equals(rCol.getColumnDto().getFieldType())) {
                    throw new InvalidConfigException("固定行表中，保留字段：" + fieldName + " 字段类型不一致");
                }
                return rCol.getColumnDto().getColumnId();
            }
            List<Long> validColIds = mapValidField.get(fieldType);
            if (validColIds == null || validColIds.isEmpty()) {
                throw new InvalidConfigException("超出固定行数据保留字段数量，字段类型：" + fieldType);
            }
            return validColIds.remove(0);
        }

        /**
         * 去掉一个可分配字段
         *
         * @param columnId
         * @return
         */
        String removeOneField(Long columnId) {
            Iterator<Map.Entry<String, List<Long>>> iterator = mapValidField.entrySet().iterator();
            while (iterator.hasNext()) {
                Map.Entry<String, List<Long>> next = iterator.next();
                List<Long> lstIds = next.getValue();
                if (lstIds.indexOf(columnId) != -1) {
                    lstIds.remove(columnId);
                    return next.getKey();
                }

            }
            Iterator<Map.Entry<String, Column>> it = mapReserveFields.entrySet().iterator();
            while (it.hasNext()) {
                Map.Entry<String, Column> entry = it.next();
                ColumnDto col = entry.getValue().getColumnDto();
                if (col.getColumnId().equals(columnId)) {
                    mapReserveFields.remove(entry.getKey());
                    return col.getFieldType();
                }
            }
            return null;
        }

        /**
         * 收回一个字段
         *
         * @param colId
         * @param fieldType
         */
        void recoverOneField(Long colId, String fieldType) {
            List<Long> colIds = mapValidField.computeIfAbsent(fieldType, (key) -> new ArrayList<Long>());
            colIds.add(0, colId);
        }

    }

    @Override
    public FixMain findMainInfo(Long fixId) {
        FixMain fixMain = new FixMain();
        fixMain.setFixId(fixId);
        return queryOne(fixMain, Constants.DEFAULT_DM_SCHEMA);
    }

    /**
     * 保存固定行数据,传过来的数据，是业务表的字段，需要先转换成固定行表字段（表fix_data)
     *
     * @param rows
     * @param fixId
     * @return
     */
    @Override
    @Transactional(readOnly = false)
    public HandleResult saveFixData(List<Map<String, Object>> rows, Long fixId, String version) {


        TableInfo tableFixData = SchemaHolder.findTableByDto(FixData.class, Constants.DEFAULT_DM_SCHEMA, version);
        if (tableFixData == null) {
            throw new InvalidConfigException("查询不到固定数据表信息");
        }
        TableInfo tableMainInfo = SchemaHolder.findTableByDto(FixMain.class, Constants.DEFAULT_DM_SCHEMA, version);

        FixMain fixMain = findMainInfo(fixId);
        Map<String, String> mapBusiToFixRelation = findFieldRelation(fixId, fixMain.getTableId(), tableFixData, true, version);
        List<Map<String, Object>> lstResult = new ArrayList<>();
        if (rows != null && !rows.isEmpty()) {
            for (Map<String, Object> row : rows) {
                Map<String, Object> newRow = convertRow(row, mapBusiToFixRelation, false);
                //需要加上固定ＩＤ
                newRow.put("fix_id", fixId);
                lstResult.add(newRow);
            }
        }
        return modelApi.saveSlaveRows(lstResult, tableFixData.getTableDto().getTableId(),
                tableMainInfo.getTableDto().getTableId(), fixId);
    }


    private Map<String, String> findFieldRelation(Long fixId, Long busiTableId, TableInfo tableFixData, boolean isBusiToFix, String version) {
        //先找到对就用表信息
        TableInfo tableBusiness = SchemaHolder.getTable(busiTableId, version);

        FixRelation relation = new FixRelation();
        relation.setFixId(fixId);
        List<FixRelation> fixRelations = queryList(relation, Constants.DEFAULT_DM_SCHEMA);
        if (fixRelations == null || fixRelations.isEmpty()) {
            throw new InvalidConfigException("没有固定行与业务表的关联信息");
        }

        Map<String, String> mapRelation = new HashMap<>();
        String fieldBusiness, fieldFixData;
        for (FixRelation aRelation : fixRelations) {
            if (aRelation == null) {
                System.out.println(" ");
            }
            fieldBusiness = tableBusiness.findColumn(aRelation.getDestColumnId()).getColumnDto().getFieldName();
            fieldFixData = tableFixData.findColumn(aRelation.getSourceColumnId()).getColumnDto().getFieldName();
            if (isBusiToFix) {
                mapRelation.put(fieldBusiness, fieldFixData);
            } else {
                mapRelation.put(fieldFixData, fieldBusiness);
            }

        }
        return mapRelation;
    }

    /**
     * 查询一个固定行数据(业务表字段)
     *
     * @param fixId
     * @param version
     * @return
     */
    @Override
    public List<Map<String, Object>> findFixData(Long fixId, String version, boolean isOnlyRelationFields) {
        if (fixId == null) {
            return null;
        }
        FixData data = new FixData();
        data.setFixId(fixId);
        data.setVersionCode(version);
        List<Map<String, Object>> lstData = queryMapList(data, Constants.DEFAULT_DM_SCHEMA);
        if (lstData == null || lstData.isEmpty()) {
            return lstData;
        }
        FixMain fixMain = findMainInfo(fixId);
        TableInfo tableFixData = SchemaHolder.findTableByDto(FixData.class, Constants.DEFAULT_DM_SCHEMA, version);
        //做数据列的转换
        Map<String, String> mapRelation = findFieldRelation(fixId, fixMain.getTableId(), tableFixData, false, version);
        List<Map<String, Object>> lstBusData = conventToBusiList(lstData, mapRelation, isOnlyRelationFields);
        makeFullControlInfo(lstBusData, fixId, version);
        return lstBusData;

    }

    /**
     * 生成控制信息
     * TODO 这里如果固定内容升级了,应该如何处理,是需要解决的,当前只处理
     *
     * @param lstData
     * @param fixId
     * @param version
     */
    @Override
    public void makeFullControlInfo(List<Map<String, Object>> lstData, Long fixId, String version) {
        if (lstData == null || lstData.isEmpty()) {
            return;
        }
        Node nodeBus = TreeNodeHelper.getInstance().generateByCode(lstData,
                FIELD_DATA_ID, FIELD_LVL, FIELD_NAME, SysCodeRule.createDefault());
        Node nodeSet = getFixDataAsTree(fixId, version);
        Node<Map<String, Object>>[] lstNodeBus = nodeBus.getChildren();

        //将每一个子节点的一一对应的设置值
        for (Node<FixData> subSetNode : nodeSet.getChildren()) {
            Object identifier = subSetNode.getIdentifier();
            Node subBusNode = findNodeById(lstNodeBus, identifier);
            if (subBusNode == null && !CommonUtils.isTrue(subSetNode.getUserObject().getCanDelete())) {
                throw new InvalidDataException("固定行数据不全,缺少行数据:" + subSetNode.getUserObject().getItemName());
            }
            handleOneNode(subBusNode, subSetNode);
        }
    }

    /**
     * 处理一个节点,判断标准是ID
     *
     * @param nodeBus
     * @param nodeSet
     */
    private void handleOneNode(Node<Map<String, Object>> nodeBus, Node<FixData> nodeSet) {
        Map<String, Object> map = nodeBus.getUserObject();
        FixData fixData = nodeSet.getUserObject();
        //使用配置的字段信息
        map.put(FIELD_LVL, fixData.getLvlCode());
        map.put(FIELD_NAME, fixData.getItemName());
        map.put(FIELD_IS_INCLUDE, fixData.getIsInclude());
        map.put(FIELD_SUM_UP, fixData.getSumUp());
        map.put(FIELD_CAN_DELETE, fixData.getCanDelete());
        map.put(FIELD_CAN_INSERT, fixData.getCanInsert());
        map.put(FIELD_CAN_EDIT, fixData.getCanEdit());
        //如果本级不允许插入子节点,则本节点的数量与设置的数量要一样,否则是错误
        if (!CommonUtils.isTrue(nodeSet.getUserObject().getCanInsert())) {
            if (nodeSet.getChildrenCount() != nodeBus.getChildrenCount()) {
                throw new InvalidDataException("业务数据与设置的数据数量不一致:" + nodeSet.getUserObject().getItemName());
            }
        }
        //检查这个节点是不是有下级,
        if (nodeSet.getChildrenCount() > 0) {
            if (!CommonUtils.isTrue(nodeSet.getUserObject().getCanInsert())) {
                if (nodeSet.getChildrenCount() < nodeBus.getChildrenCount()) {
                    throw new InvalidDataException("数据多与固定行的配置");
                }
            }
            Node<FixData>[] lstChild = nodeSet.getChildren();
            Node<Map<String, Object>>[] lstBusiChild = nodeBus.getChildren();
            for (Node<FixData> child : lstChild) {
                Object identifier = child.getIdentifier();
                Node nodeSubBusi = findNodeById(lstBusiChild, identifier);
                if (nodeSubBusi == null && CommonUtils.isTrue(child.getUserObject().getCanDelete())) {
                    throw new InvalidDataException("固定行数据不全,缺少行数据:" + child.getUserObject().getItemName());
                }
                handleOneNode(nodeSubBusi, child);
            }
        } else {
            //如果配置里没有下级了,还要检查业务数据里有没有下级
            if (nodeBus.getChildrenCount() > 0) {
                if (!CommonUtils.isTrue(nodeSet.getUserObject().getCanInsert())) {
                    throw new InvalidDataException("此行不允许有下级数据:" + nodeSet.getUserObject().getItemName());
                }
                //整理级次及控制信息
                LevelProvider aProvider = new LevelProvider(SysCodeRule.createDefault(), nodeSet.getUserObject().getLvlCode());
                String lvlCode = aProvider.getFirstSubCode();
                for (Node<Map<String, Object>> nodeSubBus : nodeBus.getChildren()) {
                    Map<String, Object> mapRow = nodeSubBus.getUserObject();
                    mapRow.put(FIELD_LVL, lvlCode);
                    mapRow.put(FIELD_IS_INCLUDE, (short) 0);
                    mapRow.put(FIELD_SUM_UP, fixData.getSumUp());
                    mapRow.put(FIELD_CAN_DELETE, (short) 1);
                    mapRow.put(FIELD_CAN_INSERT, (short) 0);
                    mapRow.put(FIELD_CAN_EDIT, (short) 1);
                    mapRow.put(FIELD_CAN_INSERT_BEFORE_V, (short) 1);
                    lvlCode = aProvider.getNextCode();
                }

            }

        }

    }

    /**
     * 根据ID查询节点,
     *
     * @param lstNode
     * @param id
     * @return
     */
    private Node findNodeById(Node[] lstNode, Object id) {
        for (Node node : lstNode) {
            if (node.getIdentifier().equals(id)) {
                return node;
            }
        }
        return null;
    }

    /**
     * 转换成业务表字段
     *
     * @param rows
     * @param mapFixToBusiRelation
     * @param isOnlyRelationFields 是不是仅仅关联字段
     * @return
     */
    private List<Map<String, Object>> conventToBusiList(List<Map<String, Object>> rows,
                                                        Map<String, String> mapFixToBusiRelation, boolean isOnlyRelationFields) {
        List<Map<String, Object>> lstResult = new ArrayList<>();
        rows.forEach(row -> {
            lstResult.add(convertRow(row, mapFixToBusiRelation, isOnlyRelationFields));
        });
        return lstResult;
    }

    private Map convertRow(Map<String, Object> row, Map<String, String> mapFixToBusiRelation, boolean isOnlyRelationFields) {
        //只翻译有对应的字段，控制字段也加入
        Map<String, Object> newRow = new HashMap<>();
        mapFixToBusiRelation.entrySet().forEach(entry -> {
            newRow.put(entry.getValue(), row.get(entry.getKey()));
        });
        //再加上控件信息列
        if (!isOnlyRelationFields) {
            controlFields.forEach(field -> {
                newRow.put(field, row.get(field));
            });
        }

        return newRow;

    }


    /**
     * 检查 并插件固定行数据
     *
     * @param mapKeyValue
     * @param tableInfo
     */
    @Override
    public boolean checkNeedFixBlock(Map<String, Object> mapKeyValue, TableInfo tableInfo) {
        //先查询表固定分组信息
        //检查给定的值是否包含分组列
        if (!CommonUtils.isTrue(tableInfo.getTableDto().getIsFixrow())) {
            return false;
        }
        QueryParam param = new QueryParam();
        param.setTable(tableInfo);
        JQParameter.genFilter(param.getCriteria(), false, mapKeyValue);
        HandleResult result = handlerFactory.handleQuery(param);
        if (result.getLstData() != null && !result.getLstData().isEmpty()) {
            //如果存在数据,则不处理
            return false;
        }
        //这里需要插件
//        copyFixTableRow(mapKeyValue, tableInfo);
        return true;
    }


    /**
     * 通过表查询固定行信息
     *
     * @param tableId
     * @param version
     * @return
     */
    @Override
    public FixMain findFixMainByTable(Long tableId, String version) {
        FixMain main = new FixMain();
        main.setTableId(tableId);
        main.setVersionCode(version);
        return queryOne(main, Constants.DEFAULT_DM_SCHEMA);
    }


    /**
     * 查询方案下的固定表信息
     *
     * @param version
     * @return
     */
    @Override
    public HandleResult findFixMain(String version) {
        FixMain fixMain = new FixMain();
        fixMain.setVersionCode(version);
        QueryParam queryParam = new QueryParam();
        queryParam.setFilterObjectAndTableAndResultType(Constants.DEFAULT_DM_SCHEMA, version, fixMain);
        return handlerFactory.handleQuery(queryParam);
    }

    /**
     * 取得固定行表对应的服务名
     *
     * @return
     */
    @Override
    public Map<String, List<Long>> findFixToServiceName() {
        HandleResult fixMain = findFixMain(Constants.DEFAULT_VERSION);
        List<FixMain> lstMain = fixMain.getData();
        if (lstMain == null || lstMain.isEmpty()) {
            return new HashMap<>();
        }
        Map<String, List<Long>> result = new HashMap<>();
        for (FixMain main : lstMain) {
            TableInfo table = SchemaHolderLocal.getTable(main.getTableId(), main.getVersionCode());
            Schema schema = SchemaHolderLocal.getSchema(table.getTableDto().getSchemaId(), table.getTableDto().getVersionCode());
            List<Long> longs = result.computeIfAbsent(schema.getSchemaDto().getApplicationCode(), key -> new ArrayList<>());
            longs.add(main.getFixId());
        }
        return result;
    }


}
