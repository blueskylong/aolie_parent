package com.ranranx.aolie.core.handler.param.condition.express;

import java.io.Serializable;

/**
 * @author xxl
 * @version V0.0.1
 * @date 2021/3/11 0011 19:04
 **/
public class Include extends BaseCondition implements Serializable {
    public Include() {
        super();
    }
    public Include(String tableName, String fieldName, Object value) {
        super(tableName, fieldName, value, null);
    }

    @Override
    String getOperExpress() {
        return PLACEHOLDER_FIELD_NAME + " like concat(concat('%'," + PLACEHOLDER_FIRST_VALUE + "),'%')";
    }
}
