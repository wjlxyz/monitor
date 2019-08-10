package com.mason.project.monitor;

public enum ErrorCode {

    SUCCESS(0, "请求成功"),

    COMMON_ILLEGAL_ARG_ERROR(-11000, "参数不合法"),

    INTERNAL_ERROR(-12000, "内部错误"),
    INTERNAL_SERVER_TIMEOUT_ERROR(-12100, "服务器超时");


    private int errorCode;

    private String message;

    ErrorCode(int errorCode, String message) {
        this.errorCode = errorCode;
        this.message = message;
    }
}
