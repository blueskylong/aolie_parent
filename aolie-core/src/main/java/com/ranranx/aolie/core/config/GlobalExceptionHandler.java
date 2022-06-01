package com.ranranx.aolie.core.config;

import com.ranranx.aolie.common.types.HandleResult;
import com.ranranx.aolie.common.exceptions.BaseException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * @author xxl
 * @version V0.0.1
 * @date 2021/6/11 0011 14:46
 **/
@ControllerAdvice
public class GlobalExceptionHandler {


    @ExceptionHandler(BaseException.class)
    @ResponseBody
    public HandleResult handleMyException(BaseException e) {
        return HandleResult.failure(e.getMessage());
    }


}
