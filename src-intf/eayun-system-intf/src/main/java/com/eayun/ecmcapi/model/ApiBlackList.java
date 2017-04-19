package com.eayun.ecmcapi.model;

public class ApiBlackList extends BaseApiBlackList {

	private static final long serialVersionUID = -7174132142491673815L;

	private String cusOrg;           //客户名称
	private String ipPartOne;         //ip第1部分
	private String ipPartTwo;         //ip第2部分
	private String ipPartThree;       //ip第3部分
	private String ipPartFour;        //ip第4部分
	
	
	
	public String getIpPartOne() {
		return ipPartOne;
	}
	public void setIpPartOne(String ipPartOne) {
		this.ipPartOne = ipPartOne;
	}
	public String getIpPartTwo() {
		return ipPartTwo;
	}
	public void setIpPartTwo(String ipPartTwo) {
		this.ipPartTwo = ipPartTwo;
	}
	public String getIpPartThree() {
		return ipPartThree;
	}
	public void setIpPartThree(String ipPartThree) {
		this.ipPartThree = ipPartThree;
	}
	public String getIpPartFour() {
		return ipPartFour;
	}
	public void setIpPartFour(String ipPartFour) {
		this.ipPartFour = ipPartFour;
	}
	public String getCusOrg() {
		return cusOrg;
	}
	public void setCusOrg(String cusOrg) {
		this.cusOrg = cusOrg;
	}
	

	
	
}
