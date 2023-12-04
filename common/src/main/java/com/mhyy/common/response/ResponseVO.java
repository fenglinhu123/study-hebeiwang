package com.mhyy.common.response;

import lombok.Data;

import java.io.Serializable;

@Data
public class ResponseVO<T> implements Serializable {

    private static final long serialVersionUID = 1L;

    private Integer returnCode;

    private T data;

    private String returnMsg;

    private Boolean success;

    public static <T> ResponseVO<T> success(T data){
        ResponseVO<T> responseVO = new ResponseVO<>();
        responseVO.setReturnCode(ResponseCode.SUCCESS.getReturnCode());
        responseVO.setReturnMsg(ResponseCode.SUCCESS.getReturnMsg());
        responseVO.setData(data);
        responseVO.setSuccess(true);
        return responseVO;
    }

    public static ResponseVO<Object> success(){
        ResponseVO<Object> responseVO = new ResponseVO<>();
        responseVO.setReturnCode(ResponseCode.SUCCESS.getReturnCode());
        responseVO.setReturnMsg(ResponseCode.SUCCESS.getReturnMsg());
        responseVO.setSuccess(true);
        return responseVO;
    }

    public static ResponseVO<Object> fail(ResponseCode responseCode){
        ResponseVO<Object> responseVO = new ResponseVO<>();
        responseVO.setReturnMsg(responseCode.getReturnMsg());
        responseVO.setReturnCode(responseCode.getReturnCode());
        responseVO.setSuccess(false);
        return responseVO;
    }

    public static ResponseVO<Object> fail(String errorMsg){
        ResponseVO<Object> responseVO = new ResponseVO<>();
        responseVO.setSuccess(false);
        responseVO.setReturnCode(20000);
        responseVO.setReturnMsg(errorMsg);
        return responseVO;
    }

    public static ResponseVO<Object> fail(Integer errorCode, String errorMsg){
        ResponseVO<Object> responseVO = new ResponseVO<>();
        responseVO.setSuccess(false);
        responseVO.setReturnCode(errorCode);
        responseVO.setReturnMsg(errorMsg);
        return responseVO;
    }

    public Boolean isSuccess() {
        return this.success;
    }
}
