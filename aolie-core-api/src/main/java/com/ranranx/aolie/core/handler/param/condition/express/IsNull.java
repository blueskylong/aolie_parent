package com.ranranx.aolie.core.handler.param.condition.express;

import com.ranranx.aolie.common.types.CommonUtils;

import java.io.Serializable;

/**
 * 字段值 为空
 *
 * @author xxl
 * @version V0.0.1
 * @date 2021/3/12 0012 14:56
 **/
public class IsNull extends BaseCondition implements Serializable {
    public IsNull() {
        super();
    }

    public static final String express = PLACEHOLDER_FIELD_NAME + " is null ";

    public IsNull(String tableName, String fieldName) {
        super(tableName, fieldName, null, null);
    }



    @Override
    String getOperExpress() {
        return express;
    }


    @Override
    public String checkValid() {
        if (CommonUtils.isEmpty(fieldName)) {
            return "条件字段没有提供";
        }
        return null;
    }
}
