package com.eayun.work.model;

import java.util.List;

/**
 * 
 * @author 陈鹏飞
 *
 */
public class WorkOpinion extends BaseWorkOpinion {

	private static final long serialVersionUID = -4350467751891814857L;
	private String FlagName;
	private boolean isEcmcCre;
	private String workEcscFalg;
	private String workTitle;
	private String logName;
	
	public String getWorkEcscFalg() {
		return workEcscFalg;
	}
	public void setWorkEcscFalg(String workEcscFalg) {
		this.workEcscFalg = workEcscFalg;
	}
	private WorkQuota workQuota;
	
	private List<WorkFile> workFile;
	
    public boolean getIsEcmcCre() {
        return isEcmcCre;
    }
    public void setIsEcmcCre(boolean isEcmcCre) {
        this.isEcmcCre = isEcmcCre;
    }
	public List<WorkFile> getWorkFile() {
		return workFile;
	}
	public void setWorkFile(List<WorkFile> workFile) {
		this.workFile = workFile;
	}
	public WorkQuota getWorkQuota() {
		return workQuota;
	}
	public void setWorkQuota(WorkQuota workQuota) {
		this.workQuota = workQuota;
	}
	public String getFlagName() {
		return FlagName;
	}
	public void setFlagName(String flagName) {
		FlagName = flagName;
	}
	public String getWorkTitle() {
		return workTitle;
	}
	public void setWorkTitle(String workTitle) {
		this.workTitle = workTitle;
	}
	public String getLogName(){return logName;}
	public void setLogName(String logName){this.logName=logName;}
}
