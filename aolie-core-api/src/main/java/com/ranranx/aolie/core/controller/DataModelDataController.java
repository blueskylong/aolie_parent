package com.ranranx.aolie.core.controller;

import com.ranranx.aolie.core.ds.dataoperator.DataOperatorFactory;
import com.ranranx.aolie.core.runtime.JQParameter;
import com.ranranx.aolie.core.runtime.SessionUtils;
import com.ranranx.aolie.core.service.DmDataService;
import com.ranranx.aolie.common.types.HandleResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingClass;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * @author xxl
 * @version V0.0.1
 * @date 2020/9/11 10:03
 **/
@RestController
@ConditionalOnMissingClass("com.ranranx.aolie.core.AolieCoreApplication")
@RequestMapping("/dmdata")
public class DataModelDataController {
    @Autowired
    private DataOperatorFactory factory;

    @Autowired
    private DmDataService dmDataService;
    /**
     * 保存增加的数据
     * 检查
     *
     * @param rows
     * @param dsId
     * @return
     */
    @PostMapping("/{dsId}/saveRows")
    public HandleResult saveRows(@RequestBody List<Map<String, Object>> rows, @PathVariable Long dsId) throws Exception {

        try {
            HandleResult result = dmDataService.saveRows(rows, dsId);
            return result;
        } catch (Exception e) {
            return HandleResult.failure(e.getMessage());
        }

    }

    /**
     * 删除指定ID数据
     *
     * @param ids
     * @param dsId
     * @return
     */
    @PostMapping("/{dsId}/deleteRowByIds")
    public HandleResult deleteRowByIds(@RequestBody List<Object> ids, @PathVariable Long dsId) {
        return dmDataService.deleteRowByIds(ids, dsId);
    }


    /**
     * 查询表的单行
     *
     * @param dsId
     * @param id
     * @return
     */
    @RequestMapping("/{dsId}/{id}/findTableRow")
    public HandleResult findTableRow(@PathVariable Long dsId, @PathVariable Long id) {
        return dmDataService.findTableRow(dsId, id, SessionUtils.getLoginVersion());
    }

    /**
     * 查询表的单行
     *
     * @param dsId
     * @return
     */
    @RequestMapping("/{dsId}/findTableRows")
    public HandleResult findTableRows(@PathVariable Long dsId, JQParameter queryParams) {
        return dmDataService.findTableRows(dsId, queryParams);
    }

    /**
     * 查询表数据
     *
     * @param dsId
     * @param fieldId
     * @param filter
     * @return
     */
    @RequestMapping("/{dsId}/{fieldId}/findTableFieldRows")
    public HandleResult findTableFieldRows(@PathVariable Long dsId, @PathVariable Long fieldId,
                                           @RequestBody Map<String, Object> filter) {
        return dmDataService.findTableFieldRows(dsId, fieldId, filter, SessionUtils.getLoginVersion());
    }

    @PostMapping("/{dsId}/{masterDsId}/{masterKey}/saveSlaveRows")
    public HandleResult saveSlaveRows(@RequestBody List<Map<String, Object>> rows,
                                      @PathVariable Long dsId,
                                      @PathVariable Long masterDsId,
                                      @PathVariable Long masterKey) {
        return dmDataService.saveSlaveRows(rows, dsId, masterDsId, masterKey);
    }

    @GetMapping("/{dsId}/{masterDsId}/{masterKey}/findSlaveRows")
    public HandleResult findSlaveRows(@PathVariable Long dsId,
                                      @PathVariable Long masterDsId,
                                      @PathVariable Long masterKey) {
        return dmDataService.findSlaveRows(dsId, masterDsId, masterKey, SessionUtils.getLoginVersion());
    }

}
