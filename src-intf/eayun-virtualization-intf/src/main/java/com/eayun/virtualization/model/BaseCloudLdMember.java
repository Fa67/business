package com.eayun.virtualization.model;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import org.apache.commons.lang3.StringUtils;

import com.eayun.eayunstack.model.Member;

@Entity
@Table(name = "cloud_ldmember")
public class BaseCloudLdMember implements java.io.Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -2842424212920374573L;
	private String memberId;
	private String poolId;
	private String prjId;
	private String dcId;
	private String createName;
	private String memberAddress;
	private Long protocolPort;
	private Long memberWeight;
	private String memberStatus;
	private Character adminStateup;
	private Date createTime;
	private String reserve1;
	private String reserve2;
	private String reserve3;
	private String vmId;
	private String role;
	private Boolean isUndertaker;
	private Integer priority;
	
	
	@Id
	@Column(name = "member_id", unique = true, nullable = false, length = 100)
	public String getMemberId() {
		return this.memberId;
	}

	public void setMemberId(String memberId) {
		this.memberId = memberId;
	}

	@Column(name = "pool_id", length = 100)
	public String getPoolId() {
		return this.poolId;
	}

	public void setPoolId(String poolId) {
		this.poolId = poolId;
	}

	@Column(name = "prj_id", length = 100)
	public String getPrjId() {
		return this.prjId;
	}

	public void setPrjId(String prjId) {
		this.prjId = prjId;
	}

	@Column(name = "dc_id", length = 100)
	public String getDcId() {
		return this.dcId;
	}

	public void setDcId(String dcId) {
		this.dcId = dcId;
	}

	@Column(name = "create_name", length = 100)
	public String getCreateName() {
		return this.createName;
	}

	public void setCreateName(String createName) {
		this.createName = createName;
	}

	@Column(name = "member_address", length = 64)
	public String getMemberAddress() {
		return this.memberAddress;
	}

	public void setMemberAddress(String memberAddress) {
		this.memberAddress = memberAddress;
	}

	@Column(name = "protocol_port", precision = 11, scale = 0)
	public Long getProtocolPort() {
		return this.protocolPort;
	}

	public void setProtocolPort(Long protocolPort) {
		this.protocolPort = protocolPort;
	}

	@Column(name = "member_weight", precision = 11, scale = 0)
	public Long getMemberWeight() {
		return this.memberWeight;
	}

	public void setMemberWeight(Long memberWeight) {
		this.memberWeight = memberWeight;
	}

	@Column(name = "member_status", length = 16)
	public String getMemberStatus() {
		return this.memberStatus;
	}

	public void setMemberStatus(String memberStatus) {
		this.memberStatus = memberStatus;
	}

	@Column(name = "admin_stateup", length = 1)
	public Character getAdminStateup() {
		return this.adminStateup;
	}

	public void setAdminStateup(Character adminStateup) {
		this.adminStateup = adminStateup;
	}

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "create_time", length = 19)
	public Date getCreateTime() {
		return this.createTime;
	}

	public void setCreateTime(Date createTime) {
		this.createTime = createTime;
	}

	@Column(name = "reserve1", length = 100)
	public String getReserve1() {
		return this.reserve1;
	}

	public void setReserve1(String reserve1) {
		this.reserve1 = reserve1;
	}

	@Column(name = "reserve2", length = 100)
	public String getReserve2() {
		return this.reserve2;
	}

	public void setReserve2(String reserve2) {
		this.reserve2 = reserve2;
	}

	@Column(name = "reserve3", length = 100)
	public String getReserve3() {
		return this.reserve3;
	}

	public void setReserve3(String reserve3) {
		this.reserve3 = reserve3;
	}
	
	public BaseCloudLdMember (){}
	
	@Column(name = "vm_id", length = 100)
	public String getVmId() {
		return vmId;
	}

	public void setVmId(String vmId) {
		this.vmId = vmId;
	}
	@Column(name = "role", length = 10)
	public String getRole() {
		return role;
	}

	public void setRole(String role) {
		this.role = role;
	}
	@Column(name = "is_undertaker")
	public Boolean getIsUndertaker() {
		return isUndertaker;
	}

	public void setIsUndertaker(Boolean isUndertaker) {
		this.isUndertaker = isUndertaker;
	}
	
	@Column(name = "priority", length = 11)
	public Integer getPriority() {
		return priority;
	}

	public void setPriority(Integer priority) {
		this.priority = priority;
	}

	public BaseCloudLdMember(Member member,String  dcId){
		if(null!=member){
			this.memberId = member.getId();
			this.poolId = member.getPool_id();
			this.prjId = member.getTenant_id();
			this.dcId = dcId;
			this.memberAddress = member.getAddress();
			if(!StringUtils.isEmpty(member.getProtocol_port())){
				this.protocolPort =Long.parseLong(member.getProtocol_port());
			}
			if(!StringUtils.isEmpty(member.getWeight())){
				this.memberWeight =Long.parseLong(member.getWeight());
			}
			this.memberStatus = member.getStatus()!=null?member.getStatus().toUpperCase():"";
			this.adminStateup = (member.isAdmin_state_up())?'1':'0';
		}
	}
}
