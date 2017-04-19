package com.eayun.virtualization.model;

import java.util.List;

/**
 * @author jingang.liu@eayun.com
 * @Dadte 2016年11月7日
 */
public class CloudProjectType {
	
	private int all_prjs;						//所有项目数量

	private int formal_cus_prjs; 				//正式用户的项目数量
	
	private int cooperation_cus_prjs;			//合作用户的项目数量
	
	private int test_cus_prjs;					//测试用户的项目数量
	
	private int oneself_cus_prjs;				//公司自己的项目数量
	
	private int other_cus_prjs;					//其他用户的项目数量
	
	private int freeze_cus_prjs;				//冻结用户的项目数量
	
	private List<String> xTitle;				//月开通量X轴title
	
	private List<String> xData;					//月开通量的数据

	public int getAll_prjs() {
		return all_prjs;
	}

	public void setAll_prjs(int all_prjs) {
		this.all_prjs = all_prjs;
	}

	public int getFormal_cus_prjs() {
		return formal_cus_prjs;
	}

	public void setFormal_cus_prjs(int formal_cus_prjs) {
		this.formal_cus_prjs = formal_cus_prjs;
	}

	public int getCooperation_cus_prjs() {
		return cooperation_cus_prjs;
	}

	public void setCooperation_cus_prjs(int cooperation_cus_prjs) {
		this.cooperation_cus_prjs = cooperation_cus_prjs;
	}

	public int getTest_cus_prjs() {
		return test_cus_prjs;
	}

	public void setTest_cus_prjs(int test_cus_prjs) {
		this.test_cus_prjs = test_cus_prjs;
	}

	public int getOneself_cus_prjs() {
		return oneself_cus_prjs;
	}

	public void setOneself_cus_prjs(int oneself_cus_prjs) {
		this.oneself_cus_prjs = oneself_cus_prjs;
	}

	public int getOther_cus_prjs() {
		return other_cus_prjs;
	}

	public void setOther_cus_prjs(int other_cus_prjs) {
		this.other_cus_prjs = other_cus_prjs;
	}

	public int getFreeze_cus_prjs() {
		return freeze_cus_prjs;
	}

	public void setFreeze_cus_prjs(int freeze_cus_prjs) {
		this.freeze_cus_prjs = freeze_cus_prjs;
	}

	public List<String> getxTitle() {
		return xTitle;
	}

	public void setxTitle(List<String> xTitle) {
		this.xTitle = xTitle;
	}

	public List<String> getxData() {
		return xData;
	}

	public void setxData(List<String> xData) {
		this.xData = xData;
	}
	
	
	
}
