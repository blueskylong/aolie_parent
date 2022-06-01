package com.ranranx.aolie.core.fixrow.service.impl;

import com.ranranx.aolie.common.types.CommonUtils;
import com.ranranx.aolie.common.types.HandleResult;
import com.ranranx.aolie.core.handler.HandlerFactory;
import com.ranranx.aolie.core.service.BaseDbService;
import com.ranranx.aolie.common.types.Constants;
import com.ranranx.aolie.core.datameta.datamodel.Column;
import com.ranranx.aolie.core.datameta.datamodel.SchemaHolder;
import com.ranranx.aolie.core.datameta.datamodel.TableInfo;
import com.ranranx.aolie.common.exceptions.InvalidConfigException;
import com.ranranx.aolie.common.exceptions.InvalidDataException;
import com.ranranx.aolie.core.fixrow.dto.FixData;
import com.ranranx.aolie.core.fixrow.dto.FixMain;
import com.ranranx.aolie.core.fixrow.service.FixRowDataService;
import com.ranranx.aolie.core.fixrow.service.FixRowService;
import com.ranranx.aolie.core.handler.param.QueryParam;
import com.ranranx.aolie.core.service.DmDataService;
import com.ranranx.aolie.common.tree.LevelProvider;
import com.ranranx.aolie.common.tree.Node;
import com.ranranx.aolie.common.tree.SysCodeRule;
import com.ranranx.aolie.common.tree.TreeNodeHelper;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingClass;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author xxl
 * @version V0.0.1
 * @date 2022/5/24 0024 22:09
 **/
@ConditionalOnMissingClass("com.ranranx.aolie.core.AolieCoreApplication")
@Service
public class FixRowDataServiceImpl extends BaseDbService implements FixRowDataService {

    @DubboReference
    private FixRowService rowSetService;

    @Autowired
    private HandlerFactory handlerFactory;


    @Autowired
    private DmDataService dmDataInner;


    @Override
    @Transactional(readOnly = false, propagation = Propagation.REQUIRES_NEW)
    public HandleResult copyFixTableRow(Map<String, Object> mapKeyValue, TableInfo tableInfo, QueryParam oraQueryParam) {
        String version = tableInfo.getTableDto().getVersionCode();
        //查询并转换固定行数据
        FixMain fixMain = rowSetService.findFixMainByTable(tableInfo.getTableDto().getTableId(), version);
        if (fixMain == null) {
            throw new InvalidConfigException("指定表没有固定行设置的信息");
        }
        //取得固定行数据
        List<Map<String, Object>> lstFixData = rowSetService.findFixData(fixMain.getFixId(), version, true);
        if (lstFixData == null || lstFixData.isEmpty()) {
            return HandleResult.success(0);
        }
        //补足主键和分组的数据
        String keyFieldName = tableInfo.getKeyField();

        int key = -1;
        for (Map<String, Object> row : lstFixData) {
            row.putAll(mapKeyValue);
            row.put(keyFieldName, key--);
        }
        insertBatch(lstFixData, tableInfo);
        if (oraQueryParam != null) {
            HandleResult result = handlerFactory.handleRequest(Constants.HandleType.TYPE_QUERY, oraQueryParam);
            makeFullControlInfo(result.getLstData(), fixMain.getFixId(), version);
            return result;
        }
        return HandleResult.success(lstFixData.size());
    }

    /**
     * 保存业务的固定数据，不分页，需要向上汇总计算
     *
     * @param lstRow
     * @param mapKey
     * @param fixId
     * @return
     */
    @Override
    @Transactional(readOnly = false)
    public HandleResult saveBusiFixData(List<Map<String, Object>> lstRow, Map<String, Object> mapKey, long fixId, String version) {
        //先检查数据结构是否一样
        Node<FixData> nodeFixRoot = rowSetService.makeFixDataToNode(fixId, version);
        Node<Map<String, Object>> nodeBusi = TreeNodeHelper.getInstance().generateByCode(lstRow, FixRowService.FIELD_LVL,
                FixRowService.FIELD_LVL, FixRowService.FIELD_NAME, SysCodeRule.createDefault());
        FixMain mainInfo = rowSetService.findMainInfo(fixId);
        TableInfo tableBus = SchemaHolder.getTable(mainInfo.getTableId(), mainInfo.getVersionCode());
        //检查提供的主键
        String err = checkRangeValue(tableBus, mapKey);
        if (CommonUtils.isNotEmpty(err)) {
            return HandleResult.failure(err);
        }
        //计算数据
        err = compareNode(nodeFixRoot, nodeBusi, tableBus);
        if (CommonUtils.isNotEmpty(err)) {
            return HandleResult.failure(err);
        }
        //收集数据,准备计算
        calcRollupFormula(lstRow, tableBus, fixId, version);

        //执行保存
        return dmDataInner.saveRangeRows(lstRow, tableBus.getTableDto().getTableId(), mapKey, version);

    }

    /**
     * 检查必须的范围条件
     *
     * @param tableInfo
     * @param mapRange
     * @return
     */
    private String checkRangeValue(TableInfo tableInfo, Map<String, Object> mapRange) {
        List<Column> lstColumn = tableInfo.getLstColumn();
        for (Column col : lstColumn) {
            //如果是分组字段,则
            if (CommonUtils.isTrue(col.getColumnDto().getFixGroup())) {
                if (CommonUtils.isEmpty(mapRange.get(col.getColumnDto().getFieldName()))) {
                    return "固定行分组值为空:" + col.getColumnDto().getTitle();
                }
            }
        }
        return null;
    }

    /**
     * 这里只计算向上汇总的公式
     *
     * @param lstRow
     * @param fixId
     * @param version
     * @return
     */
    private void calcRollupFormula(List<Map<String, Object>> lstRow, TableInfo tableInfo, Long fixId, String version) {
        if (lstRow == null || lstRow.isEmpty()) {
            return;
        }
        List<String> lstCalcField = findRollupCols(tableInfo);
        Node<Map<String, Object>> nodeBusiData
                = TreeNodeHelper.getInstance().generateByCode(lstRow, Constants.FixRowConstFields.lvlCode
                , Constants.FixRowConstFields.lvlCode, Constants.FixRowConstFields.itemName,
                SysCodeRule.createDefault());
        //取得固定设置信息
        Map<Long, FixData> mapFixData = makeFixDataMap(fixId, version);
        //找到所有末级节点
        List<Node<Map<String, Object>>> lstNeedToCalc = nodeBusiData.getLeafNodes();
        Map<String, Object> mapAllReady = new HashMap<>();
        while (!lstNeedToCalc.isEmpty()) {
            lstNeedToCalc = calcParentNode(lstNeedToCalc, mapAllReady, lstCalcField, mapFixData);
        }
    }

    /**
     * 生成ＩＤ到
     *
     * @param fixId
     * @param version
     * @return
     */
    private Map<Long, FixData> makeFixDataMap(Long fixId, String version) {
        List<FixData> lstDto = rowSetService.findFixDataDto(fixId, version);
        Map<Long, FixData> mapFixData = new HashMap<>();
        for (FixData fixData : lstDto) {
            mapFixData.put(fixData.getDataId(), fixData);
        }
        return mapFixData;
    }

    private List<Node<Map<String, Object>>> calcParentNode(List<Node<Map<String, Object>>> lstLeafNode,
                                                           Map<String, Object> mapAllReady,
                                                           List<String> lstFieldToRollup,
                                                           Map<Long, FixData> mapFixData) {
        List<Node<Map<String, Object>>> lstReserveNode = new ArrayList<>();
        //处理所有当前要处理的节点的,
        while (!lstLeafNode.isEmpty()) {
            //查找到此节点的上级节点
            Node<Map<String, Object>> node = lstLeafNode.remove(0);
            //如果没有父亲则结束了
            Node<Map<String, Object>> nodeParent = node.getParent();
            Map<String, Object> mapParent = nodeParent.getUserObject();
            if (mapParent == null) {
                continue;
            }
            if (mapAllReady.containsKey(CommonUtils.getStringField(mapParent, Constants.FixRowConstFields.lvlCode))) {
                continue;
            }
            //检查 子节点是不是都已处理过,并试图计算
            Node<Map<String, Object>>[] children = nodeParent.getChildren();
            boolean isPassCheck = true;
            //初始化值
            Double[] lstValue = new Double[lstFieldToRollup.size()];
            for (int i = 0; i < lstValue.length; i++) {
                lstValue[i] = 0D;
            }
            for (Node<Map<String, Object>> nodeSub : children) {
                //检查此子节点是否已处理过
                lstLeafNode.remove(nodeSub);
                if (!isPassCheck) {
                    //如果这个节点的不能处理，则下面的不再做
                    continue;
                }
                if (nodeSub.getChildrenCount() > 0
                        && !mapAllReady.containsKey(
                        CommonUtils.getStringField(nodeSub.getUserObject(), Constants.FixRowConstFields.lvlCode))) {
                    //如果子节点没有完全处理完成，则丢到下一次处理
                    lstReserveNode.add(nodeSub);
                    isPassCheck = false;
                }
                //判断是不是其中数据
                if (isInclude(nodeSub.getUserObject(), mapFixData)) {
                    continue;
                }
                //收集数据，并加和
                sumUp(lstFieldToRollup, nodeSub, lstValue);
            }
            if (isPassCheck) {
                //更新父节点数据，并放入完成行列
                for (int i = 0; i < lstFieldToRollup.size(); i++) {
                    String field = lstFieldToRollup.get(i);
                    mapParent.put(field, lstValue[i]);
                }
                mapAllReady.put(CommonUtils.getStringField(mapParent, Constants.FixRowConstFields.lvlCode), null);
                //加入到下一次的待计算
                lstReserveNode.add(nodeParent);
            }
        }
        return lstReserveNode;
    }

    private void sumUp(List<String> lstFieldToRollup, Node<Map<String, Object>> nodeSub, Double[] lstValue) {
        for (int i = 0; i < lstFieldToRollup.size(); i++) {
            String field = lstFieldToRollup.get(i);
            Double value = CommonUtils.getDoubleField(nodeSub.getUserObject(), field);
            if (value == null) {
                value = 0D;
            }
            lstValue[i] += value;
        }
    }

    /**
     * 是否其中数
     *
     * @param map
     * @param mapFixData
     * @return
     */
    private boolean isInclude(Map<String, Object> map, Map<Long, FixData> mapFixData) {
        Long dataId = CommonUtils.getLongField(map, Constants.FixRowConstFields.dataId);
        if (dataId == null) {
            return false;
        }
        return CommonUtils.isTrue(mapFixData.get(dataId).getIsInclude());
    }

    /**
     * 取得向上汇总的列
     *
     * @param tableInfo
     * @return
     */
    private List<String> findRollupCols(TableInfo tableInfo) {
        List<String> result = new ArrayList<>();
        List<Column> lstColumn = tableInfo.getLstColumn();
        for (Column column : lstColumn) {
            if (CommonUtils.isTrue(column.getColumnDto().getFixRollUp())) {
                result.add(column.getColumnDto().getFieldName());
            }
        }
        return result;
    }

    /**
     * 检查数据结构是否相同,    考虑固定行间可以插入自定义行.
     *
     * @param nodeFix
     * @param nodeBusi
     * @return
     */
    private String compareNode(Node<FixData> nodeFix, Node<Map<String, Object>> nodeBusi, TableInfo
            tableBus) {
        FixData fixData = nodeFix.getUserObject();
        if (fixData != null) {
            //先比较本节点,
            // 检查点: 1.ID肯定是一样的,控制信息,以固定行为标准
            //不可修改的列,值不能变
            String err = checkFixRowData(fixData, nodeBusi.getUserObject(), tableBus, false);
            if (CommonUtils.isNotEmpty(err)) {
                return err;
            }
            //2. 以控制信息来判断信息是否符合要求的行为
            if (!CommonUtils.isTrue(fixData.getCanInsert())) {//如果不可以插入子集
                if (nodeFix.getChildrenCount() < nodeBusi.getChildrenCount()) {
                    return "节点" + fixData.getItemName() + "不可以插入子节点";
                }
            }
        }
        // 比较下级节点
        int indexBus = 0;//记录比较的位置,只可以增加
        if (nodeFix.getChildrenCount() > 0) {
            for (Node<FixData> subFixNode : nodeFix.getChildren()) {
                Long dataId = subFixNode.getUserObject().getDataId();
                boolean bingo = false;
                for (int i = indexBus; i < nodeBusi.getChildrenCount(); i++) {
                    Node<Map<String, Object>> subBusNode = nodeBusi.getChildren()[i];
                    Map<String, Object> mapSubRow = subBusNode.getUserObject();
                    Long dataIdBus = CommonUtils.getLongField(mapSubRow, FixRowService.FIELD_DATA_ID);
                    indexBus++;//转向下一个节点
                    if (dataIdBus == null) {
                        //如果此业务行没有DATA_ID值 ,则说明是新增的自定义行
                        continue;
                    }
                    //比较
                    if (dataId.equals(dataIdBus)) {
                        bingo = true;
                        String err = compareNode(subFixNode, subBusNode, tableBus);
                        if (CommonUtils.isNotEmpty(err)) {
                            return err;
                        }
                        removeFixField(tableBus, subBusNode.getUserObject());
                        break;
                    } else {//如果遇到的第一个固定数据,不相同,则肯定有问题
                        if (!CommonUtils.isTrue(subFixNode.getUserObject().getCanDelete())) {
                            return "数据行不匹配,或位置出现问题";
                        } else {
                            indexBus--;//退回到上一个
                            break;//如果可以删除,就当被删除了
                        }
                    }
                }
                if (!bingo) {
                    return "未找到对应节点信息,且是不可删除的数据";
                }
            }
        } else if (nodeBusi.getChildrenCount() > 0) {
            //这里处理的都应该是用户自已增加的行
            String err = checkCustomSubRow(nodeFix, nodeBusi.getChildren(), tableBus);
            if (!CommonUtils.isNotEmpty(err)) {
                return err;
            }
        }
        //处理多余的自定义输入
        String err;
        if (indexBus < nodeBusi.getChildrenCount() - 1) {
            for (int i = indexBus; i < nodeBusi.getChildrenCount(); i++) {
                err = checkCustomSubRow(nodeFix.getParent(), nodeBusi.getChildren()[i], tableBus);
                if (CommonUtils.isNotEmpty(err)) {
                    return err;
                }
            }
        }
        return null;

    }


    /**
     * 检查自定义插入的自定义的行
     *
     * @param nodeParent
     * @param nodeSubBus
     * @return
     */
    private String checkCustomSubRow(Node<FixData> nodeParent, Node<Map<String, Object>> nodeSubBus[], TableInfo
            tableBus) {
        FixData fixData = nodeParent.getUserObject();
        if (fixData != null) {
            if (!CommonUtils.isTrue(fixData.getCanInsert())) {
                return "固定行" + fixData.getItemName() + "下不可以插入数据行";
            }
        }
        for (Node<Map<String, Object>> nodeBus : nodeSubBus) {
            if (nodeBus.getChildrenCount() > 0) {
                return "自增加行不可以增加下级";
            }
            removeFixField(tableBus, nodeBus.getUserObject());
        }
        return null;
    }

    /**
     * 检查自定义插入的自定义的行
     *
     * @param nodeParent
     * @param nodeSubBus
     * @return
     */
    private String checkCustomSubRow(Node<FixData> nodeParent, Node<Map<String, Object>> nodeSubBus, TableInfo
            tableBus) {
        Node<Map<String, Object>>[] nodes = new Node[]{nodeSubBus};
        return checkCustomSubRow(nodeParent, nodes, tableBus);
    }

    /**
     * 检查单个固定节点的信息
     *
     * @param fixData
     * @param rowData
     * @param tableBus
     * @param needCorrect
     * @return
     */
    private String checkFixRowData(FixData fixData, Map<String, Object> rowData, TableInfo tableBus,
                                   boolean needCorrect) {
        if (!fixData.getItemName().equals(CommonUtils.getStringField(rowData, FixRowService.FIELD_NAME))) {
            if (!needCorrect) {
                return "固定名称不一致";
            }
            rowData.put(FixRowService.FIELD_NAME, fixData.getItemName());
        }
        //级次可以不相等,但是长度一定是一样的
        String lvlCode = CommonUtils.getStringField(rowData, FixRowService.FIELD_LVL);
        if (CommonUtils.isEmpty(lvlCode) || !fixData.getLvlCode().equals(lvlCode)) {
            if (!needCorrect) {
                return "级次信息不正确";
            }
            //这里的设置,可能会有问题,如果中间可以插入行的话,是有问题的
            rowData.put(FixRowService.FIELD_LVL, fixData.getLvlCode());
        }
        //删除不存在的控制信息列
        removeFixField(tableBus, rowData);
        return null;

    }

    private void removeFixField(TableInfo tableBus, Map<String, Object> rowData) {
        for (String fieldName : FixRowService.controlFields) {
            if (tableBus.findColumnByName(fieldName) == null) {
                rowData.remove(fieldName);
            }
        }
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
                FixRowService.FIELD_DATA_ID, FixRowService.FIELD_LVL, FixRowService.FIELD_NAME, SysCodeRule.createDefault());
        Node nodeSet = rowSetService.getFixDataAsTree(fixId, version);
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
     * 处理一个节点,判断标准是ID
     *
     * @param nodeBus
     * @param nodeSet
     */
    private void handleOneNode(Node<Map<String, Object>> nodeBus, Node<FixData> nodeSet) {
        Map<String, Object> map = nodeBus.getUserObject();
        FixData fixData = nodeSet.getUserObject();
        //使用配置的字段信息
        map.put(FixRowService.FIELD_LVL, fixData.getLvlCode());
        map.put(FixRowService.FIELD_NAME, fixData.getItemName());
        map.put(FixRowService.FIELD_IS_INCLUDE, fixData.getIsInclude());
        map.put(FixRowService.FIELD_SUM_UP, fixData.getSumUp());
        map.put(FixRowService.FIELD_CAN_DELETE, fixData.getCanDelete());
        map.put(FixRowService.FIELD_CAN_INSERT, fixData.getCanInsert());
        map.put(FixRowService.FIELD_CAN_EDIT, fixData.getCanEdit());
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
                    mapRow.put(FixRowService.FIELD_LVL, lvlCode);
                    mapRow.put(FixRowService.FIELD_IS_INCLUDE, (short) 0);
                    mapRow.put(FixRowService.FIELD_SUM_UP, fixData.getSumUp());
                    mapRow.put(FixRowService.FIELD_CAN_DELETE, (short) 1);
                    mapRow.put(FixRowService.FIELD_CAN_INSERT, (short) 0);
                    mapRow.put(FixRowService.FIELD_CAN_EDIT, (short) 1);
                    mapRow.put(FixRowService.FIELD_CAN_INSERT_BEFORE_V, (short) 1);
                    lvlCode = aProvider.getNextCode();
                }

            }

        }

    }

}
