package com.eayun.database.configgroup.model.datastore;

import com.alibaba.fastjson.JSONObject;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.io.Serializable;

/**
 * Created by Administrator on 2017/2/24.
 */
@Entity
@Table(name = "cloud_datastoreversion")
public class CloudDatastoreVersion implements Serializable {

    @Id
    @Column(name = "id", unique = true, nullable = false, length = 100)
    private String id ;         //Datastore Version ID编号

    @Column(name = "name", length = 100)
    private String name;        //Datastore Version名称，对应数据库版本的名称

    @Column(name = "image", length = 100)
    private String image;       //Datastore Version版本镜像信息

    @Column(name = "datastore_id", length = 100)
    private String datastoreId; //该Datastore Version对应的Datastore ID编号

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

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getDatastoreId() {
        return datastoreId;
    }

    public void setDatastoreId(String datastoreId) {
        this.datastoreId = datastoreId;
    }

    @Override
    public String toString() {
        return "CloudDatastoreVersion{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", image='" + image + '\'' +
                ", datastoreId='" + datastoreId + '\'' +
                '}';
    }

}
