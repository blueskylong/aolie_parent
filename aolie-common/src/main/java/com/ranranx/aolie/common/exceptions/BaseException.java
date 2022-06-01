package com.ranranx.aolie.common.exceptions;

/**
 * @author xxl
 * @version V0.0.1
 * @date 2022/5/23 0023 16:55
 **/
public abstract class BaseException extends RuntimeException {
    public BaseException(String errorInfo) {
        super(errorInfo);
    }


    class ExceptionLevel {
        static final int LEVEL_GREEN = 20;
        static final int LEVEL_YELLOW = 15;
        static final int LEVEL_ORANGE = 10;
        static final int LEVEL_RED = 5;
        static final int LEVEL_DEAD = 1;
        static final int LEVEL_EXPLODE = 0;
    }
}