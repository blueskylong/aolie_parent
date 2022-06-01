package com.ranranx.aolie.common.runtime;

/**
 * @author xxl
 * @version V0.0.1
 * @date 2022/5/9 0009 11:45
 **/
public enum ResultCode {
    SUCCESS(200, "成功"), FAIL(500, "失败"), UNAUTHORIZED(401, "未授权");
    private int code;
    private String msg;

    ResultCode(int code, String msg) {
        this.code = code;
        this.msg = msg;
    }

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
}
