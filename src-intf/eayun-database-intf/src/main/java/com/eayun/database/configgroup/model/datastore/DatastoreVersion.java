package com.eayun.database.configgroup.model.datastore;

import java.util.List;

/**
 * Created by Administrator on 2017/2/24.
 */
public class DatastoreVersion extends CloudDatastoreVersion {

    private String packages ;
    private String active ;
    private List<Link> links ;

    private String dataVersionName;	//名称（数据库类型+版本号）
    
    public String getPackages() {
        return packages;
    }

    public void setPackages(String packages) {
        this.packages = packages;
    }

    public String getActive() {
        return active;
    }

    public void setActive(String active) {
        this.active = active;
    }

    public List<Link> getLinks() {
        return links;
    }

    public void setLinks(List<Link> links) {
        this.links = links;
    }

	public String getDataVersionName() {
		return dataVersionName;
	}

	public void setDataVersionName(String dataVersionName) {
		this.dataVersionName = dataVersionName;
	}
    
    
}
