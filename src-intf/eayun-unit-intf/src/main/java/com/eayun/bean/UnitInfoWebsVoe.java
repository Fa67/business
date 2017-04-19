package com.eayun.bean;

import java.util.List;

import com.eayun.unit.model.BaseUnitInfo;
import com.eayun.unit.model.WebSiteIP;

public class UnitInfoWebsVoe extends BaseUnitInfo {
	private List<WebSiteIP> webs;

	public List<WebSiteIP> getWebs() {
		return webs;
	}

	public void setWebs(List<WebSiteIP> webs) {
		this.webs = webs;
	}


}
