package com.eayun.unit.model;
import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name="unit_apply_web")
public class BaseUnitWeb implements Serializable{
	
	private static final long serialVersionUID = 8342902017147456332L;

	@Id
	@Column(name="apply_id")
	private String applyId;
	
	@Id
	@Column(name="web_id")
	private String webId;
	
	@Column(name="old_webId")
	private String oldWebId;

	public String getApplyId() {
		return applyId;
	}

	public void setApplyId(String applyId) {
		this.applyId = applyId;
	}

	public String getWebId() {
		return webId;
	}

	public void setWebId(String webId) {
		this.webId = webId;
	}

	public BaseUnitWeb() {
		super();
		// TODO Auto-generated constructor stub
	}

	public BaseUnitWeb(String applyId, String webId) {
		super();
		this.applyId = applyId;
		this.webId = webId;
	}

	public String getOldWebId() {
		return oldWebId;
	}

	public void setOldWebId(String oldWebId) {
		this.oldWebId = oldWebId;
	}
	

}
