package com.ranranx.aolie.monitor.interceptor;


import com.alibaba.fastjson.JSON;
import com.ranranx.aolie.common.annotation.DbOperInterceptor;
import com.ranranx.aolie.common.types.HandleResult;
import com.ranranx.aolie.common.types.Ordered;
import com.ranranx.aolie.core.datameta.datamodel.TableInfo;
import com.ranranx.aolie.core.handler.param.OperParam;
import com.ranranx.aolie.core.interceptor.IOperInterceptor;
import com.ranranx.aolie.core.runtime.SessionUtils;
import com.ranranx.aolie.monitor.common.MonitorConstants;
import com.ranranx.aolie.monitor.service.IMqService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.annotation.Order;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * 记录数据库操作时间
 *
 * @author xxl
 * @version V0.0.1
 * @date 2021/7/2 0002 11:06
 **/

@DbOperInterceptor
@Order(10000)
public class DataOperEndInterceptor implements IOperInterceptor {

    @Autowired
    private IMqService mqService;

    @Value("${spring.application.name:core}")
    private String applicationName;


    @Override
    public boolean isCanHandle(String type, Object objExtinfo) {
        return true;
    }

    @Override
    public HandleResult beforeReturn(OperParam param, String handleType, Map<String, Object> globalParamData,
                                     HandleResult handleResult) {
        if (!globalParamData.containsKey(MonitorConstants.TIME_START_PARAM)) {
            return null;
        }
        //收集信息
        Map<String, Object> mapLog = new HashMap<>();
        mapLog.put(MonitorConstants.LogTableFields.start_time,
                new Date((long) globalParamData.get(MonitorConstants.TIME_START_PARAM)));
        mapLog.put(MonitorConstants.LogTableFields.end_time, new Date());
        mapLog.put(MonitorConstants.LogTableFields.log_type, MonitorConstants.LogType.DATA_OPER);
        mapLog.put(MonitorConstants.LogTableFields.oper_type, handleType);
        mapLog.put(MonitorConstants.LogTableFields.result_size, handleResult.getChangeNum());
        mapLog.put(MonitorConstants.LogTableFields.last_time, System.currentTimeMillis()
                - (long) globalParamData.get(MonitorConstants.TIME_START_PARAM));
        mapLog.put(MonitorConstants.LogTableFields.user_id,
                SessionUtils.getLoginUser() != null ? SessionUtils.getLoginUser().getUserId() : null);
        mapLog.put(MonitorConstants.LogTableFields.sys_id, applicationName);
        //以下记录内部信息
        TableInfo table = param.getTable();
        if (table != null) {
            mapLog.put(MonitorConstants.LogTableFields.table_id,
                    table.getTableDto().getTableId());
            mapLog.put(MonitorConstants.LogTableFields.schema_id,
                    table.getTableDto().getSchemaId());
        }
        mqService.sendMsg(JSON.toJSONString(mapLog));
        return null;
    }

    @Override
    public int getOrder() {
        return Ordered.BASE_ORDER + 10000;
    }
}
