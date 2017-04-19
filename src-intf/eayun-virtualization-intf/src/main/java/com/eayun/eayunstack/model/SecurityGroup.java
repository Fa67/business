package com.eayun.eayunstack.model;


public class SecurityGroup {
	private String id;
	private String name;
	private String tenant_id;
	private String description;
	private Rule [] security_group_rules;//每1个安全组对应的管理规则，管理规则的ID存放在这个数组中；
	
	
	
	//private List<Rule> security_group_rules;//每1个安全组对应的管理规则，管理规则的ID存放在这个数组中；
	public String getTenant_id() {
		return tenant_id;
	}
	public void setTenant_id(String tenant_id) {
		this.tenant_id = tenant_id;
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
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
	public Rule[] getSecurity_group_rules() {
		return security_group_rules;
	}
	public void setSecurity_group_rules(Rule[] security_group_rules) {
		this.security_group_rules = security_group_rules;
	}
	
	
}
