package com.eayun.database.configgroup.model.datastore;

import java.io.Serializable;

/**
 * Created by Administrator on 2017/2/24.
 */
public class Link implements Serializable {

    private String ref ;
    private String href ;

    public String getRef() {
        return ref;
    }

    public void setRef(String ref) {
        this.ref = ref;
    }

    public String getHref() {
        return href;
    }

    public void setHref(String href) {
        this.href = href;
    }

    @Override
    public String toString() {
        return "Link{" +
                "ref='" + ref + '\'' +
                ", href='" + href + '\'' +
                '}';
    }
}
