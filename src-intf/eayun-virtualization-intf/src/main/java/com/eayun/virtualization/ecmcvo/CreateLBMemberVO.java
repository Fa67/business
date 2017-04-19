package com.eayun.virtualization.ecmcvo;

import com.eayun.virtualization.model.BaseCloudLdMember;

/**
 * 创建 成员VO
 * @author zhujun
 * @date 2016年4月8日
 *
 */
public class CreateLBMemberVO extends BaseCloudLdMember {

	/**
	 * 
	 */
	private static final long serialVersionUID = -2763371130411087976L;

	private String rules;

	public String getRules() {
		return rules;
	}

	public void setRules(String rules) {
		this.rules = rules;
	}
	
	
}
