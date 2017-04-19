package com.eayun.virtualization.model;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import org.apache.commons.lang3.StringUtils;
import com.eayun.eayunstack.model.FloatIp;
/**
 * 浮动IP        
 * @Filename: BaseCloudFloatIp.java
 * @Description: 
 * @Version: 1.0
 * @Author: duanbinbin
 * @Email: binbin.duan@eayun.com
 * @History:<br>
 *<li>Date: 2015年11月3日</li>
 *<li>Version: 1.0</li>
 *<li>Content: create</li>
 *
 */
@Entity
@Table(name = "cloud_floatip")
public class BaseCloudFloatIp implements Serializable {

    /**
     *Comment for <code>serialVersionUID</code>
     */
    private static final long serialVersionUID = -7453664158772725172L;

    private String floId;           //主键id
    private String floIp;           //浮动IP地址
    private Date createTime;        //创建时间
    private String prjId;           //项目id
    private String dcId;            //数据中心id
    private String resourceId;      //绑定资源的id
    private String netId;           //网络id
    private String reservepoolId;   //保留vip的id字段
    private String resourceType;    //绑定的资源类型 vm和lb
    private String isDeleted;       //删除状态 0 未删除  ；1 已删除
    private Date deleteTime;        //删除时间（释放时间）
    private Date endTime;           //到期时间
    private String chargeState;     //弹性公网ip的状态。0：正常；1：余额不足；2：已到期
    private String payType;         //付款类型。1：包年包月；2：按需付费
    private String floStatus;       //浮动ip状态。0：已使用；1：未使用
    private String isVisable;       //是否展现。0：无效；1：生效
    
    @Id
    @Column(name = "flo_id", unique = true, nullable = false, length = 100)
    public String getFloId() {
        return floId;
    }
    public void setFloId(String floId) {
        this.floId = floId;
    }
    @Column(name = "flo_ip", length = 100)
    public String getFloIp() {
        return floIp;
    }
    public void setFloIp(String floIp) {
        this.floIp = floIp;
    }
    @Column(name = "create_time")
    public Date getCreateTime() {
        return createTime;
    }
    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }
    @Column(name = "prj_id", length = 100)
    public String getPrjId() {
        return prjId;
    }
    public void setPrjId(String prjId) {
        this.prjId = prjId;
    }
    @Column(name = "dc_id", length = 100)
    public String getDcId() {
        return dcId;
    }
    public void setDcId(String dcId) {
        this.dcId = dcId;
    }
    @Column(name = "resource_id", length = 100)
    public String getResourceId() {
        return resourceId;
    }
    public void setResourceId(String resourceId) {
        this.resourceId = resourceId;
    }
    @Column(name = "net_id", length = 100)
    public String getNetId() {
        return netId;
    }
    public void setNetId(String netId) {
        this.netId = netId;
    }
    @Column(name = "reservepool_id", length = 100)
    public String getReservepoolId() {
        return reservepoolId;
    }
    public void setReservepoolId(String reservepoolId) {
        this.reservepoolId = reservepoolId;
    }
    
    public BaseCloudFloatIp(){}
    
    public BaseCloudFloatIp(FloatIp floatIp,String dcId,String projectId){
		this.floId =floatIp.getId() ;
		this.floIp = floatIp.getIp();
		this.prjId = projectId;
		this.dcId = dcId;
		if(!StringUtils.isEmpty(floatIp.getInstance_id())){
			this.resourceId=floatIp.getInstance_id();
			this.resourceType="vm";
		}
	}
    @Column(name = "resource_type", length = 100)
	public String getResourceType() {
		return resourceType;
	}
	public void setResourceType(String resourceType) {
		this.resourceType = resourceType;
	}
	@Column(name = "is_deleted", length = 1)
	public String getIsDeleted() {
		return isDeleted;
	}

	public void setIsDeleted(String isDeleted) {
		this.isDeleted = isDeleted;
	}
	
	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "delete_time", length = 19)
	public Date getDeleteTime() {
		return deleteTime;
	}

	public void setDeleteTime(Date deleteTime) {
		this.deleteTime = deleteTime;
	}

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "end_time", length = 19)
    public Date getEndTime() {
        return endTime;
    }

    public void setEndTime(Date endTime) {
        this.endTime = endTime;
    }
    @Column(name = "charge_state", length = 1)
    public String getChargeState() {
        return chargeState;
    }

    public void setChargeState(String chargeState) {
        this.chargeState = chargeState;
    }
    @Column(name = "pay_type", length = 1)
    public String getPayType() {
        return payType;
    }

    public void setPayType(String payType) {
        this.payType = payType;
    }
    @Column(name = "flo_status", length = 1)
    public String getFloStatus() {
        return floStatus;
    }

    public void setFloStatus(String floStatus) {
        this.floStatus = floStatus;
    }
    @Column(name = "is_visable", length = 1)
    public String getIsVisable() {
        return isVisable;
    }

    public void setIsVisable(String isVisable) {
        this.isVisable = isVisable;
    }
}
