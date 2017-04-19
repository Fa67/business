package com.eayun.database.configgroup.model.configgroup;

import java.io.Serializable;
import java.util.Map;

/**
 * Created by Administrator on 2017/2/28.
 */
public class ConfigurationGroup implements Serializable {

    private String id ;
    private Map<String, Object> values ;
    private String datastore_version_id ;
    private String datastore_name ;
    private String datastore_version_name ;
    private String created ;
    private String updated ;
    private String description ;
    private String name ;
    private Integer instance_count ;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Map<String, Object> getValues() {
        return values;
    }

    public void setValues(Map<String, Object> values) {
        this.values = values;
    }

    public String getDatastore_version_id() {
        return datastore_version_id;
    }

    public void setDatastore_version_id(String datastore_version_id) {
        this.datastore_version_id = datastore_version_id;
    }

    public String getDatastore_name() {
        return datastore_name;
    }

    public void setDatastore_name(String datastore_name) {
        this.datastore_name = datastore_name;
    }

    public String getDatastore_version_name() {
        return datastore_version_name;
    }

    public void setDatastore_version_name(String datastore_version_name) {
        this.datastore_version_name = datastore_version_name;
    }

    public String getCreated() {
        return created;
    }

    public void setCreated(String created) {
        this.created = created;
    }

    public String getUpdated() {
        return updated;
    }

    public void setUpdated(String updated) {
        this.updated = updated;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getInstance_count() {
        return instance_count;
    }

    public void setInstance_count(Integer instance_count) {
        this.instance_count = instance_count;
    }
}
