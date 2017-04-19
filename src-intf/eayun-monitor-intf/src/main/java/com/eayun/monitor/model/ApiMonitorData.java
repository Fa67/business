package com.eayun.monitor.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.io.Serializable;
import java.util.Date;

/**
 * Created by Administrator on 2016/12/22.
 */
@Document(collection = "api.monitor.data")
public class ApiMonitorData implements Serializable {
    @Id
    private String id ;

    private String cusId ;          //客户ID信息
    private String ip ;             //IP地址信息
    private String region ;         //数据中心ID

    //四个指标值
    private Double availability ;   //可用率值大小，四舍五入一位小数
    private Double correct ;        //正确率值大小，四舍五入一位小数
    private Long requestsNumber ;   //请求次数值大小，为整型数字
    private Double avgdealTime ;    //平均处理时间值大小，四舍五入一位小数

    private Date timestamp ;        //计划任务执行计算的时间点


    private Long availabilityfz ;   //计算可用率时候的分子之和
    private Long correctfz ;        //计算正确率时候的分子之和
    private Long avgDealTimefz ;    //计算平均处理时间时候的分子之和


    private Integer availabilityChange ;    //存放本次时刻与上一个时刻可用率的变化趋势
    private Integer correctChange ;         //存放本次时刻与上一个时刻正确率的变化趋势
    private Integer requestsNumberChange ;  //存放本次时刻与上一个时刻可用率的变化趋势
    private Integer avgdealTimeChange ;     //存放本次时刻与上一个时刻平均处理时间的变化趋势

    public ApiMonitorData(){}

    public ApiMonitorDataIndicitor getApiIndicitorData(){
        return new ApiMonitorDataIndicitor(availability, avgdealTime, correct, requestsNumber);
    }

    public ApiMonitorData(String weidu,
                          Double availability, Double correct, Long requestsNumber, Double avgdealTime,
                          Date timestamp,
                          Long availabilityfz, Long correctfz, Long avgDealTimefz,
                          Integer availabilityChange, Integer correctChange, Integer requestsNumberChange, Integer avgdealTimeChange) {

        //维度信息
        this.cusId = weidu.split(":")[0];
        this.ip = weidu.split(":")[1];
        this.region = weidu.split(":")[2];


        this.availability = availability;
        this.correct = correct;
        this.requestsNumber = requestsNumber;
        this.avgdealTime = avgdealTime;


        this.timestamp = timestamp;


        this.availabilityfz = availabilityfz;
        this.correctfz = correctfz;
        this.avgDealTimefz = avgDealTimefz;


        this.availabilityChange = availabilityChange;
        this.correctChange = correctChange;
        this.requestsNumberChange = requestsNumberChange;
        this.avgdealTimeChange = avgdealTimeChange;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getCusId() {
        return cusId;
    }

    public void setCusId(String cusId) {
        this.cusId = cusId;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public String getRegion() {
        return region;
    }

    public void setRegion(String region) {
        this.region = region;
    }

    public Double getAvailability() {
        return availability;
    }

    public void setAvailability(Double availability) {
        this.availability = availability;
    }

    public Double getCorrect() {
        return correct;
    }

    public void setCorrect(Double correct) {
        this.correct = correct;
    }

    public Long getRequestsNumber() {
        return requestsNumber;
    }

    public void setRequestsNumber(Long requestsNumber) {
        this.requestsNumber = requestsNumber;
    }

    public Double getAvgdealTime() {
        return avgdealTime;
    }

    public void setAvgdealTime(Double avgdealTime) {
        this.avgdealTime = avgdealTime;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }

    public Long getAvailabilityfz() {
        return availabilityfz;
    }

    public void setAvailabilityfz(Long availabilityfz) {
        this.availabilityfz = availabilityfz;
    }

    public Long getCorrectfz() {
        return correctfz;
    }

    public void setCorrectfz(Long correctfz) {
        this.correctfz = correctfz;
    }

    public Long getAvgDealTimefz() {
        return avgDealTimefz;
    }

    public void setAvgDealTimefz(Long avgDealTimefz) {
        this.avgDealTimefz = avgDealTimefz;
    }

    public Integer getAvailabilityChange() {
        return availabilityChange;
    }

    public void setAvailabilityChange(Integer availabilityChange) {
        this.availabilityChange = availabilityChange;
    }

    public Integer getCorrectChange() {
        return correctChange;
    }

    public void setCorrectChange(Integer correctChange) {
        this.correctChange = correctChange;
    }

    public Integer getRequestsNumberChange() {
        return requestsNumberChange;
    }

    public void setRequestsNumberChange(Integer requestsNumberChange) {
        this.requestsNumberChange = requestsNumberChange;
    }

    public Integer getAvgdealTimeChange() {
        return avgdealTimeChange;
    }

    public void setAvgdealTimeChange(Integer avgdealTimeChange) {
        this.avgdealTimeChange = avgdealTimeChange;
    }


    public String getWeiduMessage(){
        return cusId + ":" + ip + ":" + region ;
    }

    @Override
    public String toString() {
        return "ApiMonitorData{" +
                "id='" + id + '\'' +
                ", cusId='" + cusId + '\'' +
                ", ip='" + ip + '\'' +
                ", region='" + region + '\'' +
                ", availability=" + availability +
                ", correct=" + correct +
                ", requestsNumber=" + requestsNumber +
                ", avgdealTime=" + avgdealTime +
                ", timestamp=" + timestamp +
                ", availabilityfz=" + availabilityfz +
                ", correctfz=" + correctfz +
                ", avgDealTimefz=" + avgDealTimefz +
                ", availabilityChange=" + availabilityChange +
                ", correctChange=" + correctChange +
                ", requestsNumberChange=" + requestsNumberChange +
                ", avgdealTimeChange=" + avgdealTimeChange +
                '}';
    }
}