package com.eayun.log.model;
/**
 * @author jingang.liu@eayun.com
 * @Dadte 2016年3月30日
 */
public class OperLogPname extends OperLog{

	private static final long serialVersionUID = 1L;
	
	private String PrjName; //项目名称

    public String getPrjName() {
        return PrjName;
    }

    public void setPrjName(String prjName) {
        PrjName = prjName;
    }
	
}
