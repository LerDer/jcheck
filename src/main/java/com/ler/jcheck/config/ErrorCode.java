package com.ler.jcheck.config;

public enum ErrorCode {

    /**
     *
     */
    paramError(1, "参数错误！"),
    dbError(2, "数据库操作错误！"),
    userNoLogin(3, "用户未登录！"),
    serverError(4, "服务器异常"),
    //
    ;

    private int code;

    private String desc;

    ErrorCode(int code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public int getCode() {
        return code;
    }

    public String getDesc() {
        return desc;
    }
}
