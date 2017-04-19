package com.eayun.database.configgroup.model.datastore;

import java.io.Serializable;
import java.util.List;

/**
 * Created by Administrator on 2017/2/24.
 */
public class Datastores implements Serializable {

    private List<Datastore> datastores ;

    public List<Datastore> getDatastores() {
        return datastores;
    }

    public void setDatastores(List<Datastore> datastores) {
        this.datastores = datastores;
    }
}
