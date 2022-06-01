package com.ranranx.aolie.common.exceptions;

/**
 * @author xxl
 * @version V0.0.1
 * @date 2020/8/6 14:32
 **/
public class InvalidParamException  extends InvalidException{
    public InvalidParamException(String errorInfo) {
        super("不正确的参数," + errorInfo);
    }

}
