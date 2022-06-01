package com.ranranx.aolie.common.runtime;

/**
 * @author xxl
 * @version V0.0.1
 * @date 2022/5/9 0009 11:44
 **/
public class ResponseResult<T> {
    private int code;
    private String msg;
    private T data;

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }

    public ResponseResult(int code, String msg) {
        this.code = code;
        this.msg = msg;
    }

    public ResponseResult(ResultCode code, T data) {
        this.code = code.getCode();
        this.msg = code.getMsg();
        this.data = data;
    }

    public ResponseResult(ResultCode code) {
        this.code = code.getCode();
        this.msg = code.getMsg();
    }

    public static <T> ResponseResult<T> success() {
        return new ResponseResult<T>(ResultCode.SUCCESS);
    }

    public static <T> ResponseResult<T> fail() {
        return new ResponseResult<>(ResultCode.FAIL);
    }

    public static <T> ResponseResult<T> unauth() {
        return new ResponseResult<>(ResultCode.UNAUTHORIZED);
    }
}
