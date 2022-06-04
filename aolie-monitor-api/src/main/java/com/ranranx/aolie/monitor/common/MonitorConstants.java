package com.ranranx.aolie.monitor.common;

/**
 * @author xxl
 * @version V0.0.1
 * @date 2021/7/2 0002 15:32
 **/
public class MonitorConstants {
    /**
     * 开始时间参数
     */
    public static final String TIME_START_PARAM = "_TIME_START_";

    public static class LogTableFields {
        public static final String start_time = "start_time";
        public static final String end_time = "end_time";
        public static final String user_id = "user_id";
        public static final String schema_id = "schema_id";
        public static final String table_id = "table_id";
        public static final String result_size = "result_size";
        public static final String oper_type = "oper_type";
        public static final String log_type = "log_type";
        public static final String memo = "memo";
        public static final String path = "path";
        public static final String last_time = "last_time";
        public static final String oper_id = "oper_id";
        public static final String log_id = "log_id";
        public static final String sys_id = "sys_id";
    }

    public static class LogType {
        /**
         * 数据库操作
         */
        public static final int DATA_OPER = 1;
        /**
         * 服务访问
         */
        public static final int WEB_ACCESS = 2;
        /**
         * 自定义要素
         */
        public static final int CUSTOM_ELEMENT = 3;

    }

}
