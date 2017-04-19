package com.eayun.virtualization.model;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

import org.hibernate.annotations.GenericGenerator;

@Entity
@Table(name = "sys_tagresource")
public class BaseTagResource implements Serializable {

    private static final long serialVersionUID = 7553367166766604384L;
    @Id
    @GeneratedValue(generator = "system-uuid")
    @GenericGenerator(name = "system-uuid", strategy = "uuid")
    @Column(name = "tgres_id", unique = true, nullable = false, length = 32)
    private String            id;
    @Column(name = "tgres_resourceid", length = 64)
    private String            resourceId;
    @Column(name = "tgres_tagid", length = 32)
    private String            tagId;
    @Column(name = "tgres_projectid", length = 32)
    private String            projectId;
    @Column(name = "tgres_resourcetype", length = 32)
    private String            resourceType;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getResourceId() {
        return resourceId;
    }

    public void setResourceId(String resourceId) {
        this.resourceId = resourceId;
    }

    public String getTagId() {
        return tagId;
    }

    public void setTagId(String tagId) {
        this.tagId = tagId;
    }

    public String getProjectId() {
        return projectId;
    }

    public void setProjectId(String projectId) {
        this.projectId = projectId;
    }

    public String getResourceType() {
        return resourceType;
    }

    public void setResourceType(String resourceType) {
        this.resourceType = resourceType;
    }
}
