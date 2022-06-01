package com.ranranx.aolie.monitor.service.impl;

import com.ranranx.aolie.core.ds.definition.SqlExp;
import com.ranranx.aolie.core.handler.HandlerFactory;
import com.ranranx.aolie.core.handler.param.QueryParam;
import com.ranranx.aolie.core.runtime.SessionUtils;
import com.ranranx.aolie.common.tools.SqlLoader;
import com.ranranx.aolie.common.types.CommonUtils;
import com.ranranx.aolie.monitor.common.MonitorConstants;
import com.ranranx.aolie.monitor.service.MonitorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

/**
 * @author xxl
 * @version V0.0.1
 * @date 2021/6/21 0021 17:07
 **/
@Service
@Transactional(readOnly = true)
public class MonitorServiceImpl implements MonitorService {
    private static final String TITLE_FIELD = "title";
    private static final String NUM_FIELD = "num";
    @Autowired
    private HandlerFactory handlerFactory;

    @Autowired
    private LogService logService;

    @Override
    public List<Object[]> getHotTableData() {
        return findData("monitor.hotTable");
    }

    @Override
    public List<Object[]> getHotSchemaData() {
        return findData("monitor.hotSchema");
    }

    @Override
    public List<Object[]> getOptLastTimeQueryData() {
        return findData("monitor.optLastTime.query");
    }

    @Override
    public List<Object[]> getOptLastTimeUpdateData() {
        return findData("monitor.optLastTime.update");
    }

    @Override
    public List<Object[]> getOptTimesUpdateData() {
        return findData("monitor.optTimes.update");
    }

    @Override
    public List<Object[]> getOptTimesQueryData() {
        return findData("monitor.optTimes.query");
    }

    @Override
    public List<Object[]> getOptTimesByHourData() {
        return findData("monitor.opertimebyhour");
    }

    @Override
    public List<Object[]> getControllerServiceTimeData() {
        return findData("monitor.controllerServiceTime");
    }

    private List<Object[]> findData(String sqlName) {
        String sql = SqlLoader.getSql(sqlName);
        SqlExp sqlExp = new SqlExp(sql);
        QueryParam param = new QueryParam();
        param.setSqlExp(sqlExp);
        return makeResult(handlerFactory.handleQuery(param).getLstData());
    }

    /**
     * 生成格式的结果
     *
     * @param lstData
     * @return
     */
    private List<Object[]> makeResult(List<Map<String, Object>> lstData) {
        if (lstData == null || lstData.isEmpty()) {
            return null;
        }
        List<Object[]> lstResult = new ArrayList<>();
        lstData.forEach(map -> {
            String title = CommonUtils.getStringField(map, TITLE_FIELD);
            Object obj = map.get(NUM_FIELD);
            lstResult.add(new Object[]{title, obj});
        });
        return lstResult;
    }

    /**
     * 收集在线人数
     */
    @Override
    public void saveOnlineUserNum() {
        Map<String, Object> mapLog = new HashMap<>();
        mapLog.put(MonitorConstants.LogTableFields.start_time,
                new Date());
        mapLog.put(MonitorConstants.LogTableFields.end_time, new Date());
        mapLog.put(MonitorConstants.LogTableFields.log_type, MonitorConstants.LogType.CUSTOM_ELEMENT);
        mapLog.put(MonitorConstants.LogTableFields.oper_type, "OnlineUserNum");
        mapLog.put(MonitorConstants.LogTableFields.result_size, SessionUtils.getOnlineUserNum());
        mapLog.put(MonitorConstants.LogTableFields.last_time, 0);
        mapLog.put(MonitorConstants.LogTableFields.user_id, null);
        logService.saveLog(mapLog);
    }
}
