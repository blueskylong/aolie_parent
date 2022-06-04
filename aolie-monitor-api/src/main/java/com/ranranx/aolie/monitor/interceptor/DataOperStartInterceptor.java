package com.ranranx.aolie.monitor.interceptor;


import com.ranranx.aolie.common.annotation.DbOperInterceptor;
import com.ranranx.aolie.common.exceptions.InvalidException;
import com.ranranx.aolie.common.types.HandleResult;
import com.ranranx.aolie.core.handler.param.OperParam;
import com.ranranx.aolie.core.interceptor.IOperInterceptor;
import com.ranranx.aolie.common.types.Ordered;
import com.ranranx.aolie.monitor.common.MonitorConstants;
import org.springframework.core.annotation.Order;

import java.util.Map;

/**
 * 记录数据库操作时间
 *
 * @author xxl
 * @version V0.0.1
 * @date 2021/7/2 0002 11:06
 **/

@DbOperInterceptor
@Order(10)
public class DataOperStartInterceptor implements IOperInterceptor {


    @Override
    public boolean isCanHandle(String type, Object objExtinfo) {
        return true;
    }

    @Override
    public HandleResult beforeOper(OperParam param, String handleType, Map<String, Object> globalParamData)
            throws InvalidException {
        //这里只记录开始的时候, 只记录最外层的时间
        if (globalParamData.containsKey(MonitorConstants.TIME_START_PARAM)) {
            return null;
        }
        globalParamData.put(MonitorConstants.TIME_START_PARAM, System.currentTimeMillis());
        return null;
    }

    @Override
    public int getOrder() {
        return Ordered.BASE_ORDER + 1;
    }
}
