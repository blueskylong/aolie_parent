package com.ranranx.aolie.core.controller;

import com.ranranx.aolie.common.types.HandleResult;
import com.ranranx.aolie.core.ds.dataoperator.DataOperatorFactory;
import com.ranranx.aolie.core.runtime.JQParameter;
import com.ranranx.aolie.core.runtime.SessionUtils;
import com.ranranx.aolie.core.service.DmDataService;
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
@RequestMapping("/viewdata")
public class ViewDataController {
    @Autowired
    private DataOperatorFactory factory;

    @Autowired
    private DmDataService dmDataService;

    @RequestMapping("/{viewId}/findBlockData")
    public HandleResult findBlockDataForPage(@PathVariable Long viewId, JQParameter queryParams) throws Exception {
        return dmDataService.findBlockDataForPage(viewId, queryParams);
    }

    @RequestMapping("/{viewId}/findBlockDataNoPage")
    public List<Map<String, Object>> findBlockDataNoPage(@PathVariable Long viewId, JQParameter queryParams) throws Exception {
        return dmDataService.findBlockDataNoPage(viewId, queryParams);
    }

    /**
     * 更新层次编码
     *
     * @param mapIdToCode
     */
    @PostMapping("/{viewId}/updateLevel")
    public HandleResult updateLevel(@RequestBody Map<Long, String> mapIdToCode, @PathVariable long viewId) {
        return dmDataService.updateLevel(mapIdToCode, viewId);
    }


}
