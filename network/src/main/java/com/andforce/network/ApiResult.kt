package com.andforce.network;

public class ApiResult {
    private final int code;
    private final String message;

    public ApiResult(int code, String message) {
        this.code = code;
        this.message = message;
    }

    public int getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }
}
