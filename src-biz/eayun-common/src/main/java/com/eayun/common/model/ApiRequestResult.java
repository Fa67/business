package com.eayun.common.model;

import com.alibaba.fastjson.JSONObject;
import com.eayun.common.exception.ApiException;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class ApiRequestResult implements Serializable {
    private String Job_Id ;
    private String Code ;
    private String Message ;
    private String Action ;

    public ApiRequestResult(){}

    public ApiRequestResult(String requestId, String code, String message, String action) {
        this.Job_Id = requestId;
        this.Code = code;
        this.Message = message;
        this.Action = action;
        if ((this.Action != null) && (!"".equals(this.Action.trim()))){
            this.Action = this.Action + "Response" ;
        }
    }

    public Map classToMap(){
        Map map = new HashMap();
        map.put("RequestId",this.Job_Id) ;
        map.put("Code",this.Code) ;
        map.put("Message",this.Message) ;
        map.put("Action", this.Action);
        return map ;
    }

    public static JSONObject normalReturn(String requestId, String action, JSONObject serviceResult){
        Map map = new ApiRequestResult(requestId, "0", "Success", action).classToMap();
        if ((serviceResult != null)){
            serviceResult.putAll(map);
        }else {
            serviceResult = new JSONObject();
            serviceResult.putAll(map);
        }
        return serviceResult ;
    }

    @SuppressWarnings("unchecked")
    public static JSONObject errorReturn(String requestId, String action, ApiException apiException){
        apiException.setRequestId(requestId);
        return new JSONObject(new ApiRequestResult(requestId, apiException.getErrCode(), apiException.getMessage(), action).classToMap()) ;
    }

    public String getRequestId() {
        return Job_Id;
    }

    public void setRequestId(String requestId) {
        Job_Id = requestId;
    }

    public String getCode() {return Code;}

    public void setCode(String code) {Code = code;}

    public String getMessage() {
        return Message;
    }

    public void setMessage(String message) {
        Message = message;
    }

    public String getAction() {
        return Action;
    }

    public void setAction(String action) {
        Action = action;
    }

    @Override
    public String toString() {
        return "ApiRequestResult{" +
                "RequestId='" + Job_Id + '\'' +
                ", Code='" + Code + '\'' +
                ", Message='" + Message + '\'' +
                ", Action='" + Action + '\'' +
                '}';
    }
}