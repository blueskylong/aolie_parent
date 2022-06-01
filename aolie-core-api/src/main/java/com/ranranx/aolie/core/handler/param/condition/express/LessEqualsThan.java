package com.ranranx.aolie.core.handler.param.condition.express;

import java.io.Serializable;

/**
 * 小于等于
 *
 * @author xxl
 * @version V0.0.1
 * @date 2021/3/11 0011 18:02
 **/
public class LessEqualsThan extends SingleOperCondition implements Serializable {
    public LessEqualsThan() {
        super();
    }
    public LessEqualsThan(String tableName, String fieldName, Object value) {
        super(tableName, fieldName, value);
    }

    @Override
    String getOperSign() {
        return "<=";
    }
}
