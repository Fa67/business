package com.eayun.database.configgroup.model.datastore;

import java.util.List;

/**
 * Created by Administrator on 2017/2/24.
 */
public class Datastore extends CloudDatastore {

    private List<DatastoreVersion> versions ;
    private List<Link> links ;

    public List<DatastoreVersion> getVersions() {
        return versions;
    }

    public void setVersions(List<DatastoreVersion> versions) {
        this.versions = versions;
    }

    public List<Link> getLinks() {
        return links;
    }

    public void setLinks(List<Link> links) {
        this.links = links;
    }
    
    /**
     * 对格式进行转换，以符合前台显示
     * @param type
     * @return
     */
    public static String getTypeName(String type) {
		switch (type) {
		case "mysql":
			return "MySQL";
		default:
			return "";
		}
    }
}
