package com.ranranx.aolie.core.fixrow.contoller;

import com.ranranx.aolie.common.types.HandleResult;
import com.ranranx.aolie.core.fixrow.service.FixRowDataService;
import com.ranranx.aolie.core.runtime.SessionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingClass;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * @author xxl
 * @version V0.0.1
 * @date 2022/5/31 0031 15:14
 **/
@RestController
@ConditionalOnMissingClass("com.ranranx.aolie.core.AolieCoreApplication")
@RequestMapping("/fixrow")
public class FixrowDataController {

    @Autowired
    private FixRowDataService dataService;

    /**
     * 保存业务的固定数据，不分页，需要向上汇总计算
     *
     * @param lstRowAll 第一行为主键控件信息
     * @param fixId
     * @return
     */
    @PostMapping("/{fixId}/saveBusiFixData")
    public HandleResult saveBusiFixData(@RequestBody List<Map<String, Object>> lstRowAll,
                                        @PathVariable long fixId) {
        //默认第一行为键信息
        if (lstRowAll == null || lstRowAll.isEmpty()) {
            return HandleResult.failure("未指定保存的信息");
        }
        return dataService.saveBusiFixData(lstRowAll, lstRowAll.remove(0), fixId, SessionUtils.getLoginVersion());
    }
}
