package com.ranranx.aolie.application.user.service.impl;

import com.ranranx.aolie.application.menu.dto.MenuButtonDto;
import com.ranranx.aolie.application.user.dto.*;
import com.ranranx.aolie.application.user.service.UserService;
import com.ranranx.aolie.core.datameta.datamodel.SchemaHolder;
import com.ranranx.aolie.core.datameta.datamodel.TableInfo;
import com.ranranx.aolie.core.ds.definition.FieldOrder;
import com.ranranx.aolie.core.ds.definition.SqlExp;
import com.ranranx.aolie.common.exceptions.IllegalOperatorException;
import com.ranranx.aolie.common.exceptions.InvalidConfigException;
import com.ranranx.aolie.common.exceptions.InvalidParamException;
import com.ranranx.aolie.common.exceptions.NotExistException;
import com.ranranx.aolie.core.handler.HandlerFactory;
import com.ranranx.aolie.core.handler.param.DeleteParam;
import com.ranranx.aolie.core.handler.param.InsertParam;
import com.ranranx.aolie.core.handler.param.QueryParam;
import com.ranranx.aolie.core.handler.param.UpdateParam;
import com.ranranx.aolie.core.handler.param.condition.Criteria;
import com.ranranx.aolie.core.runtime.SessionUtils;
import com.ranranx.aolie.core.service.DmDataService;
import com.ranranx.aolie.common.tools.SqlLoader;
import com.ranranx.aolie.common.types.CommonUtils;
import com.ranranx.aolie.common.types.Constants;
import com.ranranx.aolie.common.types.HandleResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

/**
 * @author xxl
 * @version V0.0.1
 * @date 2021/1/6 0006 15:30
 **/
@Service
@Transactional(readOnly = true)
public class UserServiceImpl implements UserService {
    private static final String USER_RESOURCE_TABLE_NAME = "aolie_s_user_right";

    @Autowired
    private HandlerFactory handlerFactory;

    @Autowired
    private DmDataService dataService;

    //解密用的
    @Autowired
    private PasswordEncoder passwordEncoder;

    /**
     * 查询用户的所有数据权限,
     *
     * @param userId
     * @param versionCode
     * @return 数据块形式为:   rs_id:List<UserToResource>
     */
    @Override
    public HandleResult getUserRights(Long userId, String versionCode) {
        HandleResult result = getUserRightData(userId, versionCode);
        if (result.isSuccess()) {
            result.setData(makeResult((List<Map<String, Object>>) result.getData()));
        }
        return result;
    }

    private HandleResult getUserRightData(Long userId, String versionCode) {
        if (userId == null || userId < 0) {
            throw new InvalidParamException("用户id不合法");
        }
        TableInfo table =
                SchemaHolder.findTableByTableName(USER_RESOURCE_TABLE_NAME, Constants.DEFAULT_SYS_SCHEMA, versionCode);
        if (table == null) {
            throw new NotExistException("未查询到用户表定义");
        }

        QueryParam param = new QueryParam();
        param.setTable(table);
        param.setLstOrder(table.getDefaultOrder());
        param.appendCriteria().andEqualTo(null, "user_id", userId);
        return handlerFactory.handleQuery(param);

    }

    /**
     * 生成结果
     *
     * @param lstData
     * @return
     */
    private Map<Object, List<Map<String, Object>>> makeResult(List<Map<String, Object>> lstData) {
        if (lstData == null || lstData.isEmpty()) {
            return new HashMap<>(0);
        }
        Object objRsId;
        List<Map<String, Object>> lst;
        Map<Object, List<Map<String, Object>>> result = new HashMap<>(10);
        for (Map<String, Object> row : lstData) {
            objRsId = row.get("rs_id");
            lst = result.get(objRsId);
            if (lst == null) {
                lst = new ArrayList<>();
                result.put(objRsId, lst);
            }
            lst.add(row);
        }
        return result;
    }

    /**
     * 保存用户权限
     *
     * @param userId
     * @param version
     * @param mapNewUserRight rsID:list<rsDetailId>
     * @return
     */
    @Transactional(readOnly = false)
    @Override
    public HandleResult saveUserRight(long userId, String version, Map<Long, List<Long>> mapNewUserRight) {
        HandleResult userRightData = this.getUserRightData(userId, version);
        if (!userRightData.isSuccess()) {
            return userRightData;
        }
        List<Map<String, Object>> lstExistRight = (List<Map<String, Object>>) userRightData.getData();
        if (lstExistRight == null) {
            lstExistRight = new ArrayList<>();
        }
        Map<String, long[]> mapNewRight = toMap(mapNewUserRight);
        List<Object> lstToDelete = new ArrayList<>();
        String key;
        for (Map<String, Object> row : lstExistRight) {
            key = genRsDetailKey(row.get("rs_id"), row.get("rs_detail_id"));
            //如果新的数据里也有,则不需要处理
            if (mapNewRight.containsKey(key)) {
                mapNewRight.remove(key);
            } else {
                //如果新的数据里没有,则要删除
                lstToDelete.add(row.get("user_right_id"));
            }
        }
        TableInfo table =
                SchemaHolder.findTableByTableName(USER_RESOURCE_TABLE_NAME, Constants.DEFAULT_SYS_SCHEMA, version);
        if (table == null) {
            throw new NotExistException("未查询到用户表定义");
        }
        HandleResult result = null;
        int count = 0;
        if (!lstToDelete.isEmpty()) {
            //执行删除
            result = dataService.deleteRowByIds(lstToDelete, table.getTableDto().getTableId());
            count += result.getChangeNum();
            if (!result.isSuccess()) {
                return result;
            }
        }
        //增加
        if (!mapNewRight.isEmpty()) {
            result = dataService.saveRowByAdd(toList(mapNewRight, userId), table.getTableDto().getTableId());
            if (!result.isSuccess()) {
                throw new IllegalOperatorException(result.getErr());
            }
            count += result.getChangeNum();
        }
        if (result == null) {
            return HandleResult.success(0);
        }
        result.setChangeNum(count);
        return result;


    }

    private List<Map<String, Object>> toList(Map<String, long[]> mapUserRight, Long userId) {
        List<Map<String, Object>> result = new ArrayList<>();
        Iterator<long[]> iterator = mapUserRight.values().iterator();
        Map<String, Object> row;
        int index = -1;
        String version = SessionUtils.getLoginVersion();
        while (iterator.hasNext()) {
            long[] ids = iterator.next();
            row = new HashMap<>();
            row.put("user_id", userId);
            row.put("rs_id", ids[0]);
            row.put("rs_detail_id", ids[1]);
            row.put("user_right_id", index--);
            row.put("version_code", version);
            result.add(row);
        }
        return result;
    }

    private Map<String, long[]> toMap(Map<Long, List<Long>> mapNewUserRight) {
        if (mapNewUserRight == null || mapNewUserRight.isEmpty()) {
            return new HashMap<>();
        }
        Iterator<Map.Entry<Long, List<Long>>> iterator = mapNewUserRight.entrySet().iterator();
        Map<String, long[]> result = new HashMap<>();
        while (iterator.hasNext()) {
            Map.Entry<Long, List<Long>> entry = iterator.next();
            oneRsToMap(entry.getKey(), entry.getValue(), result);
        }
        return result;
    }

    private void oneRsToMap(Long rsId, List<Long> lstDetailId, Map<String, long[]> result) {
        if (lstDetailId == null) {
            return;
        }
        for (Long detailId : lstDetailId) {
            result.put(genRsDetailKey(rsId, detailId), new long[]{rsId, detailId});
        }
        return;
    }

    private String genRsDetailKey(Object rsId, Object rsDetailId) {
        return rsId.toString() + "_" + rsDetailId.toString();
    }


    /**
     * 查询菜单和按钮,组成一个树
     *
     * @return
     */
    @Override
    public HandleResult findMenuAndButton() {
        QueryParam param = new QueryParam();
        SqlExp exp = new SqlExp();
        exp.setSql(SqlLoader.getSql("userRight.findAllMenu"));
        exp.addParam("versionCode", SessionUtils.getLoginVersion());
        param.setSqlExp(exp);
        return handlerFactory.handleRequest(Constants.HandleType.TYPE_QUERY, param);
    }

    /**
     * 查询一权限关系数据
     *
     * @param rrId        权限关系ID
     * @param sourceId    主权限的ID
     * @param versionCode
     * @return
     */
    @Override
    public HandleResult findRightRelationDetail(long rrId, long sourceId, String versionCode) {
        //Query

        TableInfo tableInfo = SchemaHolder.findTableByTableName(CommonUtils.getTableName(RightRelationDetailDto.class),
                Constants.DEFAULT_SYS_SCHEMA, versionCode);
        QueryParam param = new QueryParam();
        param.setTable(tableInfo);
        param.setResultClass(RightRelationDetailDto.class);
        Criteria criteria = param.appendCriteria().andEqualTo(null, "rr_id", rrId);
        if (sourceId >= 0) {
            criteria.andEqualTo(null, "id_source", sourceId);
        }
        return handlerFactory.handleQuery(param);
    }

    /**
     * 查询一权限关系数据
     *
     * @param sourceRsId  源权限ID
     * @param destRsId    目标权限 ID
     * @param sourceId    源权限的ID值
     * @param versionCode
     * @return
     */
    @Override
    public HandleResult findRightRelationDetail(long sourceRsId, long destRsId, long sourceId, String versionCode) {
        //Query
        long rrid = findRrid(sourceRsId, destRsId, versionCode);
        if (rrid < 0) {
            return HandleResult.failure("没有查询到二个权限的关联关系");
        }
        return findRightRelationDetail(rrid, sourceId, versionCode);
    }

    private long findRrid(long sourceRsId, Long destRsId, String versionCode) {
        QueryParam param = new QueryParam();
        TableInfo info = SchemaHolder.findTableByTableName(
                CommonUtils.getTableName(RightRelationDto.class),
                Constants.DEFAULT_SYS_SCHEMA, versionCode);
        param.setTable(info);
        param.appendCriteria().andEqualTo(null, "rs_id_from", sourceRsId)
                .andEqualTo(null, "rs_id_to", destRsId);
        HandleResult result = handlerFactory.handleQuery(param);
        if (!result.isSuccess() || result.getData() == null) {
            return -1;
        }
        List<Map<String, Object>> lstData = (List<Map<String, Object>>) result.getData();
        if (lstData == null || lstData.isEmpty()) {
            return -1;
        }
        return CommonUtils.getLongField(lstData.get(0), "rr_id");
    }

    /**
     * @param rsSource                主资源
     * @param sourceId                主权限定义ID
     * @param destNewRsIdAndDetailIds 从权限定义ID 及权限数据ID
     * @param versionCode
     * @return
     */
    @Transactional(readOnly = false)
    @Override
    public HandleResult saveRightRelationDetails(long rsSource, long sourceId, Map<Long, List<Long>> destNewRsIdAndDetailIds, String versionCode) {
        Iterator<Map.Entry<Long, List<Long>>> iterator = destNewRsIdAndDetailIds.entrySet().iterator();
        HandleResult result = HandleResult.success(0);
        while (iterator.hasNext()) {
            Map.Entry<Long, List<Long>> rsEntry = iterator.next();
            Long destRsId = rsEntry.getKey();
            List<Long> lstIds = rsEntry.getValue();
            long rrId = findRrid(rsSource, destRsId, versionCode);
            if (rrId < 0) {
                throw new InvalidConfigException("没有查询到二个权限的关联关系");
            }
            HandleResult oneResult = saveRightRelationDetail(rrId, sourceId, lstIds, versionCode);
            if (!oneResult.isSuccess()) {
                throw new IllegalOperatorException(oneResult.getErr());
            }
            result.setChangeNum(result.getChangeNum() + oneResult.getChangeNum());
        }
        return result;
    }

    /**
     * 保存权限关系
     *
     * @param rrId
     * @param destNewIds
     * @param versionCode
     * @return
     */
    @Transactional(readOnly = false)
    @Override
    public HandleResult saveRightRelationDetailsByRrId(long rrId, Map<Long, List<Long>> destNewIds, String versionCode) {
        HandleResult existDetail = findRightRelationDetail(rrId, -1, versionCode);
        List<RightRelationDetailDto> lstDto = (List<RightRelationDetailDto>) existDetail.getData();
        List<Object> toDelete = new ArrayList<>();
        if (destNewIds == null) {
            destNewIds = new HashMap<>();
        }
        long targetId, sourceId;
        if (lstDto != null && !lstDto.isEmpty()) {
            for (RightRelationDetailDto dto : lstDto) {
                targetId = dto.getIdTarget();
                sourceId = dto.getIdSource();
                //如果新的不存在,则要删除
                if (!destNewIds.containsKey(sourceId) ||
                        (destNewIds.get(sourceId) != null && destNewIds.get(sourceId).indexOf(targetId) == -1)) {
                    toDelete.add(dto.getRrDetailId());
                } else if (destNewIds.containsKey(sourceId) && destNewIds.get(sourceId) != null && destNewIds.get(sourceId).indexOf(targetId) != -1) {
                    //如果有则在列表中删除
                    destNewIds.get(sourceId).remove(targetId);
                }
            }

        }
        HandleResult result = HandleResult.success(0);
        //如果删除的不是空,则去删除
        if (!toDelete.isEmpty()) {
            HandleResult deleteResult = deleteRrDetail(rrId, toDelete, versionCode);
            if (!deleteResult.isSuccess()) {
                return deleteResult;
            }
            result.setChangeNum(deleteResult.getChangeNum());
        }
        if (!destNewIds.isEmpty()) {
            Iterator<Map.Entry<Long, List<Long>>> iterator = destNewIds.entrySet().iterator();
            while (iterator.hasNext()) {
                Map.Entry<Long, List<Long>> entry = iterator.next();
                sourceId = entry.getKey();
                List<Long> lstDest = entry.getValue();
                if (lstDest == null || lstDest.isEmpty()) {
                    continue;
                }
                HandleResult insertResult = insertRrDetail(rrId, sourceId, lstDest, versionCode);
                if (!insertResult.isSuccess()) {
                    throw new IllegalOperatorException(insertResult.getErr());
                }
                result.setChangeNum(result.getChangeNum() + insertResult.getChangeNum());
            }

        }
        return result;
    }

    /**
     * 保存一个权限设置
     *
     * @param rrId        关系ID
     * @param sourceId    主权限数据ID
     * @param destNewIds  从权限数据ID
     * @param versionCode
     * @return
     */
    @Override
    public HandleResult saveRightRelationDetail(long rrId, long sourceId, List<Long> destNewIds, String versionCode) {
        HandleResult existDetail = findRightRelationDetail(rrId, sourceId, versionCode);
        List<RightRelationDetailDto> lstDto = (List<RightRelationDetailDto>) existDetail.getData();
        List<Object> toDelete = new ArrayList<>();
        if (destNewIds == null) {
            destNewIds = new ArrayList<>();
        }
        long targetId;
        if (lstDto != null && !lstDto.isEmpty()) {
            for (RightRelationDetailDto dto : lstDto) {
                targetId = dto.getIdTarget();
                //如果新的不存在,则要删除
                if (destNewIds.indexOf(targetId) == -1) {
                    toDelete.add(dto.getRrDetailId());
                } else {
                    //如果有则在列表中删除
                    destNewIds.remove(targetId);
                }
            }

        }
        HandleResult result = HandleResult.success(0);
        //如果删除的不是空,则去删除
        if (!toDelete.isEmpty()) {
            HandleResult deleteResult = deleteRrDetail(rrId, toDelete, versionCode);
            if (!deleteResult.isSuccess()) {
                return deleteResult;
            }
            result.setChangeNum(deleteResult.getChangeNum());
        }
        if (destNewIds != null && !destNewIds.isEmpty()) {
            HandleResult insertResult = insertRrDetail(rrId, sourceId, destNewIds, versionCode);
            if (!insertResult.isSuccess()) {
                throw new IllegalOperatorException(insertResult.getErr());
            }
            result.setChangeNum(result.getChangeNum() + insertResult.getChangeNum());
        }
        return result;
    }

    private HandleResult deleteRrDetail(long rrId, List<Object> detailId, String versionCode) {
        DeleteParam param = new DeleteParam();
        TableInfo info = SchemaHolder.findTableByTableName(
                CommonUtils.getTableName(RightRelationDetailDto.class),
                Constants.DEFAULT_SYS_SCHEMA, versionCode);
        param.setTable(info);
        param.setIds(detailId);
        return handlerFactory.handleDelete(param);
    }

    private HandleResult insertRrDetail(long rrId, long sourceId, List<Long> lstTargetId, String versionCode) {
        InsertParam param = new InsertParam();
        long id = -1;
        List<RightRelationDetailDto> lstDto = new ArrayList<>();
        RightRelationDetailDto dto;
        for (Long targetId : lstTargetId) {
            dto = new RightRelationDetailDto();
            dto.setIdSource(sourceId);
            dto.setIdTarget(targetId);
            dto.setRrDetailId(id--);
            dto.setVersionCode(versionCode);
            dto.setRrId(rrId);
            lstDto.add(dto);
        }
        param.setObjects(Constants.DEFAULT_SYS_SCHEMA, lstDto);
        return handlerFactory.handleInsert(param);
    }

    /**
     * 根据权限资源ID,查询权限资源全信息
     *
     * @param lstId
     * @param versionCode
     * @return
     */
    @Override
    public HandleResult findRightResources(List<Long> lstId, String versionCode) {
        if (lstId == null || lstId.isEmpty()) {
            return null;
        }
        QueryParam param = new QueryParam();
        param.setTableDtos(Constants.DEFAULT_SYS_SCHEMA, versionCode, RightResourceDto.class);

        param.appendCriteria().andIn(CommonUtils.getTableName(RightResourceDto.class), "rs_id", lstId);
        return handlerFactory.handleQuery(param);
    }

    /**
     * 查询角色对应的其它资源信息
     *
     * @return
     */
    @Override
    public HandleResult findRoleRightOtherRelation() {
        List<Long> lstOtherRsID = findRoleRelationOtherIds();
        if (lstOtherRsID == null || lstOtherRsID.isEmpty()) {
            return HandleResult.success(0);
        }
        QueryParam param = new QueryParam();
        param.setTableDtos(Constants.DEFAULT_SYS_SCHEMA, SessionUtils.getLoginVersion(), RightResourceDto.class);
        param.appendCriteria().andIn(CommonUtils.getTableName(RightResourceDto.class), "rs_id", lstOtherRsID);
        return handlerFactory.handleQuery(param);
    }

    /**
     * 查询角色对应的其它资源的ID,排队了菜单和按钮的资源ID
     *
     * @return
     */
    private List<Long> findRoleRelationOtherIds() {
        QueryParam param = new QueryParam();
        param.setTableDtos(Constants.DEFAULT_SYS_SCHEMA, SessionUtils.getLoginVersion(), RightRelationDto.class);
        //排除菜单和按钮资源
        List<Long> menuAndButtonId = new ArrayList<>();
        menuAndButtonId.add(Constants.DefaultRsIds.menu);
        menuAndButtonId.add(Constants.DefaultRsIds.menuButton);
        param.appendCriteria().andEqualTo(null, "rs_id_from", Constants.DefaultRsIds.role)
                .andNotIn(null, "rs_id_to", menuAndButtonId);
        param.setResultClass(RightRelationDto.class);
        HandleResult result = handlerFactory.handleQuery(param);
        if (result.isSuccess()) {
            List<RightRelationDto> lstData = (List<RightRelationDto>) result.getData();
            if (lstData == null || lstData.isEmpty()) {
                return null;
            }
            List<Long> lstResult = new ArrayList<>(lstData.size());
            for (RightRelationDto dto : lstData) {
                lstResult.add(dto.getRsIdTo());
            }
            return lstResult;
        }
        return null;
    }

    /**
     * 查询所有权限关系
     *
     * @return
     */
    @Override
    public List<RightRelationDto> findAllRelationDto(String version) {
        QueryParam param = new QueryParam();
        param.setTableDtos(Constants.DEFAULT_SYS_SCHEMA, SessionUtils.getDefaultVersion(), RightRelationDto.class);
        if (CommonUtils.isNotEmpty(version)) {
        } else {
            param.setNoVersionFilter(true);
        }
        param.setResultClass(RightRelationDto.class);
        HandleResult result = handlerFactory.handleQuery(param);
        if (result.isSuccess()) {
            return (List<RightRelationDto>) result.getData();
        }
        return null;
    }

    /**
     * 查询所有权限关系
     *
     * @return
     */
    @Override
    public List<RightResourceDto> findAllRightSourceDto(String version) {
        QueryParam param = new QueryParam();
        param.setTableDtos(Constants.DEFAULT_SYS_SCHEMA,
                version == null ? SessionUtils.getDefaultVersion() : version, RightResourceDto.class);
        if (CommonUtils.isNotEmpty(version)) {
        } else {
            param.setNoVersionFilter(true);
        }

        param.setResultClass(RightResourceDto.class);
        HandleResult result = handlerFactory.handleQuery(param);
        if (result.isSuccess()) {
            return (List<RightResourceDto>) result.getData();
        }
        return null;
    }

    /**
     * 查询直接授予用户的权限
     *
     * @param userId      用户ID
     * @param rsId        资源类型ID
     * @param versionCode 版本号
     * @return 返回权限的明细ID
     */
    @Override
    public Set<Long> findUserDirectRights(Long userId, Long rsId, String versionCode) {
        QueryParam param = new QueryParam();
        param.setTableDtos(Constants.DEFAULT_SYS_SCHEMA, versionCode, UserRightDto.class);

        param.appendCriteria().andEqualTo(null, "user_id", userId)
                .andEqualTo(null, "rs_id", rsId);
        param.setResultClass(UserRightDto.class);
        HandleResult result = handlerFactory.handleQuery(param);
        if (result.isSuccess() && result.getData() != null) {
            List<UserRightDto> lstDto = (List<UserRightDto>) result.getData();
            Set<Long> lstIDs = new HashSet<>(lstDto.size());
            lstDto.forEach(el -> {
                lstIDs.add(el.getRsDetailId());
            });
            return lstIDs;
        }
        return null;
    }

    /**
     * 查询直接授予用户的所有权限
     *
     * @param userId      用户ID
     * @param versionCode 版本号
     * @return 返回权限的明细ID
     */
    @Override
    public Map<Long, Set<Long>> findUserDirectAllRights(Long userId, String versionCode, Long roleId) {
        QueryParam param = new QueryParam();
        param.setTableDtos(Constants.DEFAULT_SYS_SCHEMA, versionCode, UserRightDto.class);

        param.appendCriteria().andEqualTo(null, "user_id", userId);
        param.setResultClass(UserRightDto.class);
        HandleResult result = handlerFactory.handleQuery(param);
        if (result.isSuccess() && result.getData() != null) {
            List<UserRightDto> lstDto = (List<UserRightDto>) result.getData();
            Map<Long, Set<Long>> mapRight = new HashMap<>();
            lstDto.forEach(el -> {
                //确定只选择指定的角色,
                if (roleId != null && el.getRsId() == Constants.DefaultRsIds.role) {
                    if (!el.getRsDetailId().equals(roleId)) {
                        return;
                    }
                }
                Set<Long> lstId = mapRight.computeIfAbsent(el.getRsId(), key -> new HashSet<>());
                lstId.add(el.getRsDetailId());
            });
            return mapRight;
        }
        return new HashMap<>();
    }

    /**
     * 查询传递的权限明细
     *
     * @param rsFrom     开始的资源类别ID
     * @param rsTo       结束的资源类别ID
     * @param lstFromIds 开始资源的所有ID
     * @return 对应结果节点的所有ID
     */
    @Override
    public Set<Long> findNextRights(Long rsFrom, Long rsTo, Set<Long> lstFromIds, String versionCode) {
        //查询他们的对应关系ID
        if (lstFromIds == null || lstFromIds.isEmpty()) {
            return null;
        }
        long rrid = findRrid(rsFrom, rsTo, versionCode);
        if (rrid < 0) {
            //没有查询到关系
            return null;
        }
        //查询明细
        QueryParam param = new QueryParam();
        param.setTableDtos(Constants.DEFAULT_SYS_SCHEMA, versionCode, RightRelationDetailDto.class);

        param.appendCriteria().andEqualTo(null, "rr_id", rrid).andIn(null, "id_source",
                CommonUtils.toList(lstFromIds));
        param.setResultClass(RightRelationDetailDto.class);
        HandleResult result = handlerFactory.handleQuery(param);
        if (result.isSuccess() && result.getData() != null) {
            List<RightRelationDetailDto> lstDto = (List<RightRelationDetailDto>) result.getData();
            Set<Long> lstIds = new HashSet<>(lstDto.size());
            lstDto.forEach(el -> {
                lstIds.add(el.getIdTarget());
            });
            return lstIds;
        } else {
            return null;
        }
    }

    /**
     * 查询所有的带有权限的操作按钮信息
     * 所有存在权限设置的按钮 key:version+tableId+operId, value:此表此操作的按钮列表. 因为同一个操作,可能在不同的功能里,所以可能会出现多次.
     * 而出现一次,就认定有权限
     *
     * @return
     */
    @Override
    public Map<String, Set<Long>>[] findAllButtonsOperator() {
        //查询所有相关数据,只需要查询删除,修改和增加的操作
        QueryParam param = new QueryParam();
        param.setTableDtos(Constants.DEFAULT_SYS_SCHEMA, SessionUtils.getDefaultVersion(), MenuButtonDto.class);
        param.setNoVersionFilter(true);
        Set<Integer> lstOper = new HashSet<>();
        lstOper.add(Constants.TableOperType.delete);
        lstOper.add(Constants.TableOperType.add);
        lstOper.add(Constants.TableOperType.edit);
        lstOper.add(Constants.TableOperType.editMulti);
        lstOper.add(Constants.TableOperType.saveSingle);
        lstOper.add(Constants.TableOperType.saveLevel);
        lstOper.add(Constants.TableOperType.saveMulti);
        param.setResultClass(MenuButtonDto.class);
        String tableName = CommonUtils.getTableName(MenuButtonDto.class);
        param.appendCriteria().andIn(tableName, "table_opertype", CommonUtils.toList(lstOper))
                .andIsNotNull(tableName, "relation_tableid");
        HandleResult result = handlerFactory.handleQuery(param);
        Map<String, Set<Long>> mapOperTypeToBtnId = new HashMap<>();
        Map<String, Set<Long>> mapTableAllOperType = new HashMap<>();
        Map<String, Set<Long>>[] arrResult = new Map[]{mapOperTypeToBtnId, mapTableAllOperType};
        if (!result.isSuccess() || result.getData() == null) {
            return arrResult;
        }
        List<MenuButtonDto> lstDto = (List<MenuButtonDto>) result.getData();
        lstDto.forEach(el -> {
            //将操作类型中的单行编辑和多行编辑合并成单行编辑,保存单行和多选合并成单行
            String key = el.getVersionCode() + "_"
                    + el.getRelationTableid() + "_";
            String tableKey = el.getVersionCode() + "_"
                    + el.getRelationTableid();
            if (Constants.TableOperType.editMulti.equals(el.getTableOpertype())) {
                key += Constants.TableOperType.edit;
            } else if (Constants.TableOperType.saveMulti.equals(el.getTableOpertype())) {
                key += Constants.TableOperType.saveSingle;
            } else {
                key += el.getTableOpertype();
            }
            Set<Long> lstId = mapOperTypeToBtnId.computeIfAbsent(key, (k) -> new HashSet<>());
            lstId.add(el.getBtnId());
            Set<Long> lstType = mapTableAllOperType.computeIfAbsent(tableKey, k -> new HashSet<>());
            lstType.add(el.getTableOpertype().longValue());
        });

        return arrResult;
    }

    /**
     * 根据用户名或编辑,查询用户列表
     *
     * @param name
     * @return
     */
    @Override
    public List<UserDto> findUserByCodeOrName(String name) {
        QueryParam param = new QueryParam();
        UserDto dto = new UserDto();
        dto.setVersionCode(SessionUtils.getLoginVersion());
        param.setFilterObjectAndTableAndResultType(Constants.DEFAULT_SYS_SCHEMA, SessionUtils.getLoginVersion(), dto);
        param.appendCriteria().and(new Criteria().andInclude(null, "account_code", name)
                .orInclude(null, "user_name", name));
        param.addOrder(new FieldOrder(UserDto.class, "user_name", false, 1));
        return (List<UserDto>) handlerFactory.handleQuery(param).getData();
    }

    /**
     * 禁用用户账号
     *
     * @param userId
     * @return
     */
    @Override
    @Transactional(readOnly = false)
    public HandleResult disableUser(Long userId) {
        return updateUserState(userId, Constants.UserState.disabled, SessionUtils.getLoginVersion());
    }

    /**
     * 启用账号
     *
     * @param userId
     * @return
     */
    @Override
    @Transactional(readOnly = false)
    public HandleResult enableUser(Long userId) {
        return updateUserState(userId, Constants.UserState.normal, SessionUtils.getLoginVersion());
    }

    /**
     * 重置用户密码
     *
     * @param userId
     * @return
     */
    @Override
    @Transactional(readOnly = false)
    public HandleResult resetUserPassword(Long userId) {
        if (userId == null) {
            return HandleResult.failure("未指定用户ID");
        }
        String version = SessionUtils.getLoginVersion();
        UserDto dto = new UserDto();
        dto.setUserId(userId);
        dto.setPassword(passwordEncoder.encode(Constants.DEFAULT_PASSWORD));
        dto.setVersionCode(SessionUtils.getLoginVersion());
        return handlerFactory.handleUpdate(UpdateParam
                .genUpdateByObject(Constants.DEFAULT_SYS_SCHEMA, version, dto, true));
    }

    /**
     * 更新用户的状态
     *
     * @param userId
     * @param state
     * @param version
     * @return
     */
    private HandleResult updateUserState(Long userId, Integer state, String version) {
        if (userId == null) {
            return HandleResult.failure("未指定用户ID");
        }
        UserDto userDto = new UserDto();
        userDto.setVersionCode(version);
        userDto.setUserId(userId);
        userDto.setState(state);
        return handlerFactory.handleUpdate(UpdateParam
                .genUpdateByObject(Constants.DEFAULT_SYS_SCHEMA, version, userDto, true));
    }

}
