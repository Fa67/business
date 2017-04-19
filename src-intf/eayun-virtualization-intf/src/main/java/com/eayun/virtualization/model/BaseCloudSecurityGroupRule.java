package com.eayun.virtualization.model;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import com.eayun.eayunstack.model.Rule;

@Entity
@Table(name = "cloud_grouprule")
public class BaseCloudSecurityGroupRule implements java.io.Serializable {

	private static final long serialVersionUID = -7588803956951601649L;

	private String sgrId;// 自己ID
	private String direction;// egress出口、ingress入口
	private String ethertype;// IP4或者IP6
	private String portRangeMax;// 端口最大值
	private String portRangeMin;// 端口最小值
	private String protocol;// IP协议 tcp udp等
	private String remoteIpPrefix;// 远程 CIDR
	private String sgId;// 所在的安全组ID
	private String prjId;// 所在的项目ID
	private String remoteGroupId;// 远程选择安全组时 显示的值是安全组的name 用id标记；

	private Date createTime;// 设置规则的创建时间；
	private String createName;// 创建人
	private String dcId;// 数据中心id
	private String icMp;//新增字段（icmp）
	
	private String protocolExpand;//扩展字段（DNS...）
	
	
	@Column(name = "icmp", length = 100)
	public String getIcMp() {
		return icMp;
	}

	public void setIcMp(String icMp) {
		this.icMp = icMp;
	}

	@Column(name = "create_name", length = 100)
	public String getCreateName() {
		return createName;
	}

	public void setCreateName(String createName) {
		this.createName = createName;
	}
	@Column(name = "protocol_expand", length = 100)
	public String getProtocolExpand() {
		return protocolExpand;
	}

	public void setProtocolExpand(String protocolExpand) {
		this.protocolExpand = protocolExpand;
	}
	@Column(name = "dc_id", length = 100)
	public String getDcId() {
		return dcId;
	}

	public void setDcId(String dcId) {
		this.dcId = dcId;
	}

	@Id
	@Column(name = "sgr_id", unique = true, nullable = false, length = 100)
	public String getSgrId() {
		return sgrId;
	}

	public void setSgrId(String sgrId) {
		this.sgrId = sgrId;
	}

	@Column(name = "direction", length = 15)
	public String getDirection() {
		return direction;
	}

	public void setDirection(String direction) {
		this.direction = direction;
	}

	@Column(name = "ethertype", length = 40)
	public String getEthertype() {
		return ethertype;
	}

	public void setEthertype(String ethertype) {
		this.ethertype = ethertype;
	}

	@Column(name = "port_rangemax", length = 6)
	public String getPortRangeMax() {
		return portRangeMax;
	}

	public void setPortRangeMax(String portRangeMax) {
		this.portRangeMax = portRangeMax;
	}

	@Column(name = "port_rangemin", length = 6)
	public String getPortRangeMin() {
		return portRangeMin;
	}

	public void setPortRangeMin(String portRangeMin) {
		this.portRangeMin = portRangeMin;
	}

	@Column(name = "protocol", length = 40)
	public String getProtocol() {
		return protocol;
	}

	public void setProtocol(String protocol) {
		this.protocol = protocol;
	}

	@Column(name = "remote_ipprefix", length = 50)
	public String getRemoteIpPrefix() {
		return remoteIpPrefix;
	}

	public void setRemoteIpPrefix(String remoteIpPrefix) {
		this.remoteIpPrefix = remoteIpPrefix;
	}

	@Column(name = "sg_id", length = 100)
	public String getSgId() {
		return sgId;
	}

	public void setSgId(String sgId) {
		this.sgId = sgId;
	}

	@Column(name = "prj_id", length = 100)
	public String getPrjId() {
		return prjId;
	}

	public void setPrjId(String prjId) {
		this.prjId = prjId;
	}

	@Column(name = "remote_groupid", length = 100)
	public String getRemoteGroupId() {
		return remoteGroupId;
	}

	public void setRemoteGroupId(String remoteGroupId) {
		this.remoteGroupId = remoteGroupId;
	}

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "create_time", length = 19)
	public Date getCreateTime() {
		return createTime;
	}

	public void setCreateTime(Date createTime) {
		this.createTime = createTime;
	}
	
	public BaseCloudSecurityGroupRule(){}
	
	
	public BaseCloudSecurityGroupRule(Rule rule,String dcId){
		if(null!=rule){
			this.sgrId = rule.getId();
			this.direction = rule.getDirection();
			this.ethertype = rule.getEthertype();
			this.portRangeMax = rule.getPort_range_max();
			this.portRangeMin = rule.getPort_range_min();
			this.protocol = rule.getProtocol();
			this.remoteIpPrefix = rule.getRemote_ip_prefix();
			this.sgId = rule.getSecurity_group_id();
			this.prjId = rule.getTenant_id();
			this.remoteGroupId = rule.getRemote_group_id();
			this.createTime = rule.getCreat_time();
			this.dcId = dcId;
		}
	}
}
