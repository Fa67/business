package com.eayun.virtualization.ecmcvo;

import java.io.Serializable;

/**
 * 修改负载均衡成员 vo
 * @author zhujun
 * @date 2016年4月8日
 *
 */
public class UpdateLBMemberVo implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 3250468001745047299L;

	private String memberId;
	
	private String poolId;
	
	private Long weight;
	
	private Character adminStateUp;

	public String getMemberId() {
		return memberId;
	}

	public void setMemberId(String memberId) {
		this.memberId = memberId;
	}

	public String getPoolId() {
		return poolId;
	}

	public void setPoolId(String poolId) {
		this.poolId = poolId;
	}

	public Long getWeight() {
		return weight;
	}

	public void setWeight(Long weight) {
		this.weight = weight;
	}

	public Character getAdminStateUp() {
		return adminStateUp;
	}

	public void setAdminStateUp(Character adminStateUp) {
		this.adminStateUp = adminStateUp;
	}
	
}
