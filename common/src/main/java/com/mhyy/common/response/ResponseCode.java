package com.mhyy.common.response;

public enum ResponseCode {
    SUCCESS(200,"Reuqest Successfully!"),
    BAD_REQUEST(400,"Request, Successfully!"),
    UNAUTHORIZED(401,"Reuqest, Successfully!");

    private final Integer returnCode;
    private final String returnMsg;

    private ResponseCode(Integer code, String message){
        this.returnCode = code;
        this.returnMsg = message;
    }

    public Integer getReturnCode() {
        return returnCode;
    }

    public String getReturnMsg() {
        return returnMsg;
    }
}
