package com.ranranx.aolie.monitor.service.impl;

import com.ranranx.aolie.common.types.IdGenerator;
import com.ranranx.aolie.core.ds.dataoperator.IDataOperator;
import com.ranranx.aolie.core.ds.definition.SqlExp;
import com.ranranx.aolie.monitor.common.MonitorConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * 保存日志服务
 *
 * @author xxl
 * @version V0.0.1
 * @date 2021/7/2 0002 12:56
 **/
@Component
public class LogService {

    @Autowired
    private ThreadPoolTaskExecutor taskExecutor;
    @Autowired
    private IDataOperator dataOperator;
    private static String sSql;
    private static Map<String, Object> mapEmpty = new HashMap<>();
    private boolean enabled = true;

    static {
        sSql = "INSERT INTO aolie_s_log (start_time,end_time," +
                "  user_id,schema_id,table_id,result_size,oper_type,log_type,memo,path,oper_id,log_id" +
                ",last_time,sys_id)VALUES(#{start_time},#{end_time},#{user_id},#{schema_id}," +
                "     #{table_id},#{result_size},#{oper_type},#{log_type},#{memo}," +
                "    #{path},#{oper_id},#{log_id},#{last_time},#{sys_id})";
        mapEmpty.put(MonitorConstants.LogTableFields.start_time, null);
        mapEmpty.put(MonitorConstants.LogTableFields.end_time, null);
        mapEmpty.put(MonitorConstants.LogTableFields.user_id, null);
        mapEmpty.put(MonitorConstants.LogTableFields.schema_id, null);
        mapEmpty.put(MonitorConstants.LogTableFields.table_id, null);
        mapEmpty.put(MonitorConstants.LogTableFields.result_size, null);
        mapEmpty.put(MonitorConstants.LogTableFields.oper_type, null);
        mapEmpty.put(MonitorConstants.LogTableFields.log_type, null);
        mapEmpty.put(MonitorConstants.LogTableFields.memo, null);
        mapEmpty.put(MonitorConstants.LogTableFields.path, null);
        mapEmpty.put(MonitorConstants.LogTableFields.oper_id, null);
        mapEmpty.put(MonitorConstants.LogTableFields.log_id, null);
        mapEmpty.put(MonitorConstants.LogTableFields.last_time, null);
        mapEmpty.put(MonitorConstants.LogTableFields.sys_id, null);

    }

    public void saveLog(Map<String, Object> mapLog) {
        if (!enabled) {
            return;
        }
        taskExecutor.execute(new LoggerSaver(mapLog));
    }

    class LoggerSaver implements Runnable {
        private Map<String, Object> mapValue;

        public LoggerSaver(Map<String, Object> mapRow) {
            mapValue = mapRow;
        }

        @Override
        public void run() {
            Map<String, Object> mapTemp = new HashMap<>();
            mapTemp.putAll(mapEmpty);
            mapTemp.putAll(this.mapValue);
            handleType(mapTemp);
            mapTemp.put(MonitorConstants.LogTableFields.log_id, IdGenerator.getNextId(""));
            SqlExp sqlExp = new SqlExp(sSql, mapTemp);
            dataOperator.executeDirect(sqlExp.getExecuteMap());
            this.mapValue = null;
        }
    }

    private void handleType(Map<String, Object> map) {
        Object o = map.get(MonitorConstants.LogTableFields.start_time);
        if (o != null) {
            map.put(MonitorConstants.LogTableFields.start_time,
                    new Date((Long) map.get(MonitorConstants.LogTableFields.start_time)));
        }
        o = map.get(MonitorConstants.LogTableFields.end_time);
        if (o != null) {
            map.put(MonitorConstants.LogTableFields.end_time,
                    new Date((Long) map.get(MonitorConstants.LogTableFields.end_time)));
        }
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }
}
