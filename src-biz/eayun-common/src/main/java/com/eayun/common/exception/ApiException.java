package com.eayun.common.exception;

import com.alibaba.fastjson.JSONObject;
import com.eayun.common.util.ApiUtil;

public class ApiException extends Exception {

    private String RequestId ;
    private String ErrCode ;
    private String ErrMsg ;
    private ErrorType ErrorType ;
    private JSONObject baseInformation ;

    public ApiException(){}

    public ApiException(String requestId, String errCode, String errMsg, ErrorType errorType){
        super(errorType.toString() + ":" + errMsg);
        RequestId = requestId;
        ErrCode = errCode;
        ErrMsg = errMsg;
        ErrorType = errorType;
    }

    public ApiException(String requestId, String errCode, String errMsg, ErrorType errorType, JSONObject baseInformation){
        super(errorType.toString() + ":" + errMsg);
        RequestId = requestId;
        ErrCode = errCode;
        ErrMsg = errMsg;
        ErrorType = errorType;
        this.baseInformation = baseInformation ;
    }

    public static ApiException createApiException(String ErrCode, JSONObject baseInformation){
        String[] errors = ApiUtil.getErrMsgByErrCode(ErrCode).split(":") ;
        return new ApiException(null, ErrCode, errors[1], com.eayun.common.exception.ErrorType.valueOf(errors[0]), baseInformation);
    }

    public static ApiException createApiException(String ErrCode){
        String[] errors = ApiUtil.getErrMsgByErrCode(ErrCode).split(":") ;
        return new ApiException(null, ErrCode, errors[1], com.eayun.common.exception.ErrorType.valueOf(errors[0]));
    }

    public String getRequestId() {
        return RequestId;
    }

    public void setRequestId(String requestId) {
        RequestId = requestId;
    }


    public void setBaseInformation(JSONObject baseInformation) {
        this.baseInformation = baseInformation;
    }

    public JSONObject getBaseInformation() {
        return baseInformation;
    }

    public String getErrCode() {
        return ErrCode;
    }

    public void setErrCode(String errCode) {
        ErrCode = errCode;
    }

    public String getErrMsg() {
        return ErrMsg;
    }

    public void setErrMsg(String errMsg) {
        ErrMsg = errMsg;
    }

    public com.eayun.common.exception.ErrorType getErrorType() {
        return ErrorType;
    }

    public void setErrorType(com.eayun.common.exception.ErrorType errorType) {
        ErrorType = errorType;
    }

    @Override
    public String toString() {
        return "ApiException{" +
                "RequestId='" + RequestId + '\'' +
                ", ErrCode='" + ErrCode + '\'' +
                ", ErrMsg='" + ErrMsg + '\'' +
                ", ErrorType=" + ErrorType +
                '}';
    }
}