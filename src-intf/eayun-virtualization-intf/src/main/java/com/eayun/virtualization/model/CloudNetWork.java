package com.eayun.virtualization.model;

import java.math.BigDecimal;

import com.eayun.common.util.BeanUtils;

public class CloudNetWork extends BaseCloudNetwork {

    /**
     *Comment for <code>serialVersionUID</code>
     */
    private static final long serialVersionUID = -3761841790517574313L;
    private String netStatusName;	//转义后的网络资源状态
    private String adminStaName;
    private int subNetCount;
    private String prjName;
    private String dcName;
    private String extNetId;
    private String extNetName;
    private int prjQuotaSubNet;
    private int rate;
    private String routeId;
    private String routeName;
    private String gatewayIp;
    /* 用户中心改版计费相关 */
    private String payTypeStr;
    private String orderNo;
    private int buyCycle;
    private BigDecimal price;
    
    /* ECMC 列表需要展示客户信息 */
    private String cusId;
    private String cusName;
    private String cusOrg;//客户的所属组织
    
	public CloudNetWork(){
    	super();
    }
    
    public CloudNetWork(BaseCloudNetwork baseCloudNetwork){
    	BeanUtils.copyPropertiesByModel(this, baseCloudNetwork);
    }
    
	public String getNetStatusName() {
        return netStatusName;
    }
    public void setNetStatusName(String netStatusName) {
        this.netStatusName = netStatusName;
    }
    public int getSubNetCount() {
        return subNetCount;
    }
    public int getRate() {
		return rate;
	}
	public void setRate(int rate) {
		this.rate = rate;
	}
	public void setSubNetCount(int subNetCount) {
        this.subNetCount = subNetCount;
    }
    public String getAdminStaName() {
        return adminStaName;
    }
    public void setAdminStaName(String adminStaName) {
        this.adminStaName = adminStaName;
    }
	public String getPrjName() {
		return prjName;
	}
	public void setPrjName(String prjName) {
		this.prjName = prjName;
	}
	public int getPrjQuotaSubNet() {
		return prjQuotaSubNet;
	}
	public void setPrjQuotaSubNet(int prjQuotaSubNet) {
		this.prjQuotaSubNet = prjQuotaSubNet;
	}
	public String getDcName() {
		return dcName;
	}
	public void setDcName(String dcName) {
		this.dcName = dcName;
	}
	public String getExtNetName() {
		return extNetName;
	}
	public void setExtNetName(String extNetName) {
		this.extNetName = extNetName;
	}
	public String getRouteId() {
		return routeId;
	}
	public void setRouteId(String routeId) {
		this.routeId = routeId;
	}
	public String getRouteName() {
		return routeName;
	}
	public void setRouteName(String routeName) {
		this.routeName = routeName;
	}
    public String getCusId() {
        return cusId;
    }
    public void setCusId(String cusId) {
        this.cusId = cusId;
    }
    public String getCusName() {
        return cusName;
    }
    public void setCusName(String cusName) {
        this.cusName = cusName;
    }
	public String getExtNetId() {
		return extNetId;
	}
	public void setExtNetId(String extNetId) {
		this.extNetId = extNetId;
	}

	public String getCusOrg() {
		return cusOrg;
	}

	public void setCusOrg(String cusOrg) {
		this.cusOrg = cusOrg;
	}

	public String getGatewayIp() {
		return gatewayIp;
	}

	public void setGatewayIp(String gatewayIp) {
		this.gatewayIp = gatewayIp;
	}

	public String getPayTypeStr() {
		return payTypeStr;
	}

	public void setPayTypeStr(String payTypeStr) {
		this.payTypeStr = payTypeStr;
	}

	public String getOrderNo() {
		return orderNo;
	}

	public void setOrderNo(String orderNo) {
		this.orderNo = orderNo;
	}

	public int getBuyCycle() {
		return buyCycle;
	}

	public void setBuyCycle(int buyCycle) {
		this.buyCycle = buyCycle;
	}

	public BigDecimal getPrice() {
		return price;
	}

	public void setPrice(BigDecimal price) {
		this.price = price;
	}
    
}
