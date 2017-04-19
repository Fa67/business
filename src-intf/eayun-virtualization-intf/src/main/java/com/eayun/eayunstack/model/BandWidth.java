package com.eayun.eayunstack.model;

public class BandWidth {
	/****带宽限速****/
	private String direction;//ingress下载，egress上传
	
	/*rate 和 ceil，表示速度。
	 *1. 只限制路由带宽的话是可以不用关注其他方面的，你的理解没有问题
      2. ingress 是下载，egress 是上传没错
	  3. 50Mbps/8 = 1250000*5，所以你的理解没错。当然如果我们要求的精度是 Mbps 的话，那么可以把对应的字节数 125000 保留作为倍数，多少M就乘以多少就对了（如125000*50）
	  4. 我更建议使用 RESTAPI 和其他组件保持一致，说明在 wiki 上之前的邮件已经给出了，其实只是要 POST /v2.0/eayun_qos/qoss 即可*/
	
	private String rate;
	private String id;//底层带宽的id
	private String name;
	private String tenant_id;
	private String target_id;//目标的 id(代表路由) 
	private String target_type;//port, router 目标类型，端口或者路由 
	public String getDirection() {
		return direction;
	}
	public void setDirection(String direction) {
		this.direction = direction;
	}
	public String getRate() {
		return rate;
	}
	public void setRate(String rate) {
		this.rate = rate;
	}
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getTenant_id() {
		return tenant_id;
	}
	public void setTenant_id(String tenant_id) {
		this.tenant_id = tenant_id;
	}
	public String getTarget_id() {
		return target_id;
	}
	public void setTarget_id(String target_id) {
		this.target_id = target_id;
	}
	public String getTarget_type() {
		return target_type;
	}
	public void setTarget_type(String target_type) {
		this.target_type = target_type;
	}
	
	
	
}
