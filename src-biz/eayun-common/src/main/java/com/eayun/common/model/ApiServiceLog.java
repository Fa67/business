package com.eayun.common.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

@Document(collection = "api.service.log")
public class ApiServiceLog implements Serializable {

    @Id
    private String job_Id ;                 //对应Mongodb数据库中的ID编号信息
    private String resourceTypeNodeId ;     //对应资源类型的NodeId
    private String apiNameNodeId ;          //对应具体API信息的NodeId编号
    private String operatorId ;             //对应客户ID信息
    private Date createTime = new Date() ;  //API请求时间
    private Date status_time = new Date() ; //API状态更新时间
    private String resourceId ;             //业务可能含带有的资源ID信息
    private String status = "2" ;           //0 失败   1 成功  2  执行中
    private Long takeTime ;                 //处理时间
    private String responseBody ;           //API请求响应内容信息
    private String regionId ;               //数据中心ID
    private String ip ;                     //初始化日志时赋值，IP地址信息
    private String requestBody ;            //初始化日志时赋值，API请求内容信息
    private String version ;                //初始化日志时赋值，版本号信息
    private String errCode ;                //如果档次API服务调用错误，记录对应的错误状态码

    public ApiServiceLog(){}

    public ApiServiceLog(String ip, String requestBody){
        this.ip = ip ;
        this.requestBody = requestBody ;
    }

    public ApiServiceLog(String ip, String operatorId, Date status_time, String resourceId,  String status, Long takeTime, String requestBody, String responseBody) {
        this.ip = ip;
        this.operatorId = operatorId;
        this.status_time = status_time;
        this.resourceId = resourceId;
        this.status = status;
        this.takeTime = takeTime;
        this.requestBody = requestBody;
        this.responseBody = responseBody;
    }

    public void seterrCode(String errCode) {
        this.errCode = errCode;
    }

    public String geterrCode() {
        return errCode;
    }

    public String getresourceTypeNodeId() {
        return resourceTypeNodeId;
    }

    public void setresourceTypeNodeId(String resourceTypeNodeId) {
        this.resourceTypeNodeId = resourceTypeNodeId;
    }

    public String getapiNameNodeId() {
        return apiNameNodeId;
    }

    public void setapiNameNodeId(String apiNameNodeId) {
        this.apiNameNodeId = apiNameNodeId;
    }

    public void setregionId(String regionId) {
        this.regionId = regionId;
    }

    public String getregionId() {
        return regionId;
    }

    public void setversion(String version) {
        this.version = version;
    }

    public String getversion() {
        return version;
    }


    public void setcreateTime(Date createTime) {
        this.createTime = createTime;
    }

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss'Z'",timezone = "UTC+8")
    public Date getcreateTime() {
        return createTime;
    }

    public String getjob_Id() {
        return job_Id;
    }

    public void setjob_Id(String job_Id) {
        this.job_Id = job_Id;
    }

    public String getip() {
        return ip;
    }

    public void setip(String ip) {
        this.ip = ip;
    }

    public String getoperatorId() {
        return operatorId;
    }

    public void setoperatorId(String operatorId) {
        this.operatorId = operatorId;
    }

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss'Z'")
    public Date getstatus_time() {
        return status_time;
    }

    public void setstatus_time(Date status_time) {
        this.status_time = status_time;
    }


    public String getresourceId() {
        return resourceId;
    }

    public void setresourceId(String resourceId) {
        this.resourceId = resourceId;
    }

    public String getstatus() {
        return status;
    }

    public void setstatus(String status) {
        this.status = status;
    }

    public Long gettakeTime() {
        return takeTime;
    }

    public void settakeTime(Long takeTime) {
        this.takeTime = takeTime;
    }

    public String getrequestBody() {
        return requestBody;
    }

    public void setrequestBody(String requestBody) {
        this.requestBody = requestBody;
    }

    public String getresponseBody() {
        return responseBody;
    }

    public void setresponseBody(String responseBody) {
        this.responseBody = responseBody;
    }

    @Override
    public String toString() {
        return "ApiServiceLog{" +
                "job_Id='" + job_Id + '\'' +
                ", ip='" + ip + '\'' +
                ", operatorId='" + operatorId + '\'' +
                ", operationTime='" + status_time + '\'' +
                ", resourceId='" + resourceId + '\'' +
                ", status='" + status + '\'' +
                ", takeTime=" + takeTime +
                ", requestBody='" + requestBody + '\'' +
                ", responseBody='" + responseBody + '\'' +
                '}';
    }

    /**
     * 判断当次API服务请求是否为可用的状态
     * @return
     */
    public boolean isServerError(){
        List<String> serverNotInServiceErrCodes = Arrays.asList("500001","500002","500003","500004") ;
        //如果错误码是以上四种错误码中的一种，则表示服务器端当次不可用
        return serverNotInServiceErrCodes.contains(this.errCode) ;
    }
}