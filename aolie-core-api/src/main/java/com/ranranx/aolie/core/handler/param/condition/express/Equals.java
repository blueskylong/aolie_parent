package com.ranranx.aolie.core.handler.param.condition.express;

import java.io.Serializable;

/**
 * 等于条件
 *
 * @author xxl
 * @version V0.0.1
 * @date 2021/3/11 0011 18:02
 **/
public class Equals extends SingleOperCondition implements Serializable {
    public Equals() {
        super();
    }

    public Equals(String tableName, String fieldName, Object value) {
        super(tableName, fieldName, value);
    }

    @Override
    String getOperSign() {
        return "=";
    }
}
