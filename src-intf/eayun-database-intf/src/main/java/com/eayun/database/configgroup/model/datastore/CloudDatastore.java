package com.eayun.database.configgroup.model.datastore;

import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import java.io.Serializable;

/**
 * Created by Administrator on 2017/2/24.
 */
@Entity
@Table(name = "cloud_datastore")
public class CloudDatastore implements Serializable {

    @Id
    @Column(name = "id", unique = true, nullable = false, length = 100)
    private String id ;    //Datastore ID编号

    @Column(name = "name", length = 100)
    private String name;   //Datastore 名称，对应数据库的名称

    @Column(name = "dc_id", length = 32)
    private String dcId ;   //数据中心ID编号

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDcId() {
        return dcId;
    }

    public void setDcId(String dcId) {
        this.dcId = dcId;
    }

    @Override
    public String toString() {
        return "CloudDatastore{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                '}';
    }
}
