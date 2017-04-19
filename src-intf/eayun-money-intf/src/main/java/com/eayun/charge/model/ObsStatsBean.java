package com.eayun.charge.model;

import java.io.Serializable;
import java.util.Date;

/**
 * OBS数据统计实体Bean，用于OBS数据统计模块发消息通知OBS计费模块
 *
 * @Filename: ObsStatsBean.java
 * @Description:
 * @Version: 1.0
 * @Author: zhangfan
 * @Email: fan.zhang@eayun.com
 * @History:<br> <li>Date: 2016年8月10日</li>
 * <li>Version: 1.0</li>
 * <li>Content: create</li>
 */
public class ObsStatsBean implements Serializable{

    /**
     *Comment for <code>serialVersionUID</code>
     */
    private static final long serialVersionUID = -8675234311935408009L;
    /**
     * 客户ID
     */
    private String cusId;
    /**
     * 统计数据类型——<br/>ChargeConstant.OBS_STATS_TYPE.STORAGE-存储空间；ChargeConstant.OBS_STATS_TYPE.USED-下载流量和请求数
     */
    private String statsType;
    /**
     * 本次统计开始时间，即统计成功后计费的开始计费时间
     */
    private Date chargeFrom;
    /**
     * 本次统计截止时间，即统计成功计费的截止计费时间
     */
    private Date chargeTo;

    /**
     * 数据中心ID
     */
    private String datacenterId;

    public String getDatacenterId() {
        return datacenterId;
    }

    public void setDatacenterId(String datacenterId) {
        this.datacenterId = datacenterId;
    }

    public String getCusId() {
        return cusId;
    }

    public void setCusId(String cusId) {
        this.cusId = cusId;
    }

    public String getStatsType() {
        return statsType;
    }

    public void setStatsType(String statsType) {
        this.statsType = statsType;
    }

    public Date getChargeFrom() {
        return chargeFrom;
    }

    public void setChargeFrom(Date chargeFrom) {
        this.chargeFrom = chargeFrom;
    }

    public Date getChargeTo() {
        return chargeTo;
    }

    public void setChargeTo(Date chargeTo) {
        this.chargeTo = chargeTo;
    }
}
