package com.ranranx.aolie.core.fixrow.contoller;

import com.ranranx.aolie.core.fixrow.service.FixRowService;
import com.ranranx.aolie.core.runtime.SessionUtils;
import com.ranranx.aolie.common.types.HandleResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * @author xxl
 * @version V0.0.1
 * @date 2021/8/10 0010 20:27
 **/
@RestController
@RequestMapping("/fixrow")
public class FixRowController {

    @Autowired(required = false)
    private FixRowService fixRowService;

    /**
     * 查询固定行表头
     *
     * @param fixId
     * @return
     */
    @GetMapping("/findFixRowComponents/{fixId}")
    public HandleResult findFixRowComponents(@PathVariable Long fixId) {
        return HandleResult.success(fixRowService.findFixRowComponents(fixId, SessionUtils.getLoginVersion()));
    }

    /**
     * 保存固定行数据,传过来的数据，是业务表的字段，需要先转换成固定行表字段（表fix_data)
     *
     * @param rows
     * @param fixId
     * @return
     */
    @PostMapping("/saveFixData/{fixId}")
    public HandleResult saveFixData(@RequestBody List<Map<String, Object>> rows, @PathVariable Long fixId) {
        return fixRowService.saveFixData(rows, fixId, SessionUtils.getLoginVersion());
    }

    /**
     * 查询一个固定行数据
     *
     * @param fixId
     * @return
     */
    @GetMapping("/findFixData/{fixId}")
    public HandleResult findFixData(@PathVariable Long fixId) {
        return HandleResult.success(fixRowService.findFixData(fixId, SessionUtils.getLoginVersion(), false));
    }


}
