package com.eayun.common.model;

/**
 * Created by Administrator on 2016/12/20.
 */
public class ApiServiceLogDetail extends ApiServiceLog {
    private String resourceType ;
    private String operationName ;
    private String apiName ;
    private String operator ;
    private String region ;

    public String getresourceType() {
        return resourceType;
    }

    public String getoperationName() {
        return operationName;
    }

    public String getapiName() {
        return apiName;
    }

    public String getoperator() {
        return operator;
    }

    public void setresourceType(String resourceType) {
        this.resourceType = resourceType;
    }

    public void setoperationName(String operationName) {
        this.operationName = operationName;
    }

    public void setapiName(String apiName) {
        this.apiName = apiName;
    }

    public void setoperator(String operator) {
        this.operator = operator;
    }

    public String getregion() {
        return region;
    }

    public void setregion(String region) {
        this.region = region;
    }
}
