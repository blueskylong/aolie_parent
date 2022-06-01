package com.ranranx.aolie.core.handler.param.condition.express;

import java.io.Serializable;

/**
 * 不包含指定值
 * @author xxl
 * @version V0.0.1
 * @date 2021/3/11 0011 19:04
 **/
public class Exclude extends BaseCondition implements Serializable {
    public Exclude() {
        super();
    }
    public Exclude(String tableName, String fieldName, Object value) {
        super(tableName, fieldName, value, null);
    }

    @Override
    String getOperExpress() {
        return PLACEHOLDER_FIELD_NAME + " not like concat(concat('%'," + PLACEHOLDER_FIRST_VALUE + "),'%')";
    }
}
