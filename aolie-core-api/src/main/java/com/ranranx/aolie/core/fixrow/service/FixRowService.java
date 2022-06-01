package com.ranranx.aolie.core.fixrow.service;

import com.ranranx.aolie.common.types.HandleResult;
import com.ranranx.aolie.core.datameta.datamodel.BlockViewer;
import com.ranranx.aolie.core.datameta.datamodel.TableInfo;
import com.ranranx.aolie.core.fixrow.dto.FixData;
import com.ranranx.aolie.common.tree.Node;
import com.ranranx.aolie.core.fixrow.dto.FixMain;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * @author xxl
 * @version V0.0.1
 * @date 2021/8/8 0008 10:45
 **/

public interface FixRowService {
    String FIELD_LVL = "lvl_code";
    String FIELD_NAME = "item_name";
    String FIELD_DATA_ID = "data_id";

    String FIELD_IS_INCLUDE = "is_include";
    String FIELD_SUM_UP = "sum_up";
    String FIELD_CAN_DELETE = "can_delete";
    String FIELD_CAN_INSERT = "can_insert";
    String FIELD_CAN_EDIT = "can_edit";
    //是否可以插入，这个虚拟列
    String FIELD_CAN_INSERT_BEFORE_V = "can_insert_before";


    //这是控件字段，不保存在业务表中，而是在这里，然后动态生成控制信息
    List<String> controlFields = Arrays.asList(new String[]{FIELD_IS_INCLUDE, FIELD_SUM_UP,
            FIELD_CAN_DELETE, FIELD_CAN_INSERT, FIELD_CAN_EDIT, FIELD_CAN_INSERT_BEFORE_V});


    /**
     * 查询固定行表头
     *
     * @param fixId
     * @param version
     * @return
     */
    BlockViewer findFixRowComponents(Long fixId, String version);


    /**
     * 同步更新固定行对应设置
     *
     * @param schemaId
     */

    void syncFixSet(Long schemaId, String version);

    /**
     * 保存固定行数据,传过来的数据，是业务表的字段，需要先转换成固定行表字段（表fix_data)
     *
     * @param rows
     * @param fixId
     * @return
     */

    HandleResult saveFixData(List<Map<String, Object>> rows, Long fixId, String version);

    /**
     * 查询一个固定行数据
     *
     * @param fixId
     * @param version
     * @return
     */
    List<Map<String, Object>> findFixData(Long fixId, String version, boolean isOnlyRelationFields);

    /**
     * 检查 固定行数据情况
     *
     * @param mapKeyValue
     * @param tableInfo
     */
    boolean checkNeedFixBlock(Map<String, Object> mapKeyValue, TableInfo tableInfo);


    /**
     * 生成控制信息
     * TODO 这里如果固定内容升级了,应该如何处理,是需要解决的,当前只处理
     *
     * @param lstData
     * @param fixId
     * @param version
     */
    void makeFullControlInfo(List<Map<String, Object>> lstData, Long fixId, String version);

    /**
     * 通过表查询固定行信息
     *
     * @param tableId
     * @param version
     * @return
     */
    FixMain findFixMainByTable(Long tableId, String version);

    /**
     * 通过表查询固定行信息
     *
     * @param version
     * @return
     */
    HandleResult findFixMain(String version);


    /**
     * 查询固定行明细的设置
     *
     * @param fixId
     * @param version
     * @return
     */
    List<FixData> findFixDataDto(Long fixId, String version);

    /**
     * 将固定数据生成状态结构(配置信息)
     *
     * @param fixId
     * @param version
     * @return
     */
    Node<FixData> getFixDataAsTree(Long fixId, String version);

    FixMain findMainInfo(Long fixId);

    /**
     * 生成固定行明细设置的树状结构
     *
     * @param fixId
     * @param version
     * @return
     */
    Node<FixData> makeFixDataToNode(Long fixId, String version);

    /**
     * 取得固定行表对应的服务名
     *
     * @return
     */
    Map<String, List<Long>> findFixToServiceName();
}
