package com.eayun.log.model;

public class SysLog extends BaseSysLog{

	/**
	 * 
	 */
	private static final long serialVersionUID = -4756924697042594203L;

	private String PrjName; //项目名称
	
	private String dcName; 	//数据中心名称

    public String getPrjName() {
        return PrjName;
    }

    public void setPrjName(String prjName) {
        PrjName = prjName;
    }

	public String getDcName() {
		return dcName;
	}

	public void setDcName(String dcName) {
		this.dcName = dcName;
	}
	
    
    
	
	
}
