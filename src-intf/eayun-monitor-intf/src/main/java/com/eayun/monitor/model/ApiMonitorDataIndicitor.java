package com.eayun.monitor.model;

/**
 * Created by Administrator on 2016/12/22.
 */

/**
 * 存放四个API指标名称
 */
public class ApiMonitorDataIndicitor {
    //可用率
   private Double availableValue  ;
    //平均处理时间值
   private Double dateValue ;
    //正确率值
   private Double rightValue ;
    //API访问总次数
   private Long allNumberValue ;

    public ApiMonitorDataIndicitor(){}

    public ApiMonitorDataIndicitor(Double availableValue, Double dateValue, Double rightValue, Long allNumberValue) {
        this.availableValue = availableValue;
        this.dateValue = dateValue;
        this.rightValue = rightValue;
        this.allNumberValue = allNumberValue;
    }

    public Double getAvailableValue() {
        return availableValue;
    }

    public void setAvailableValue(Double availableValue) {
        this.availableValue = availableValue;
    }

    public Double getDateValue() {
        return dateValue;
    }

    public void setDateValue(Double dateValue) {
        this.dateValue = dateValue;
    }

    public Double getRightValue() {
        return rightValue;
    }

    public void setRightValue(Double rightValue) {
        this.rightValue = rightValue;
    }

    public Long getAllNumberValue() {
        return allNumberValue;
    }

    public void setAllNumberValue(Long allNumberValue) {
        this.allNumberValue = allNumberValue;
    }

    @Override
    public String toString() {
        return "ApiMonitorDataIndicitor{" +
                "availableValue=" + availableValue +
                ", dateValue=" + dateValue +
                ", rightValue=" + rightValue +
                ", allNumberValue=" + allNumberValue +
                '}';
    }

    /**
     * 比较指标值的变化趋势
     * @param compare
     * @return
     */
    public Integer availableValueChange(Double compare){
        return (compare > this.availableValue ? 1 : (compare < this.availableValue ? -1 : 0)) ;
    }
    public Integer rightValueChange(Double compare){
        return (compare > this.rightValue ? 1 : (compare < this.rightValue ? -1 : 0)) ;
    }
    public Integer dateValueChange(Double compare){
        return (compare > this.dateValue ? 1 : (compare < this.dateValue ? -1 : 0)) ;
    }
    public Integer allValueChange(Long compare){
        return (compare > this.allNumberValue ? 1 : (compare < this.allNumberValue ? -1 : 0)) ;
    }
}
