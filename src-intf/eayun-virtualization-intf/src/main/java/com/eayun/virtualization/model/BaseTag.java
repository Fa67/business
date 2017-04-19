package com.eayun.virtualization.model;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import org.hibernate.annotations.GenericGenerator;

@Entity
@Table(name = "sys_tag")
public class BaseTag implements Serializable {

    private static final long serialVersionUID = -1315661010833258727L;
    @Id
    @GeneratedValue(generator = "system-uuid")
    @GenericGenerator(name = "system-uuid", strategy = "uuid")
    @Column(name = "tg_id", unique = true, nullable = false, length = 32)
    private String            id;
    @Column(name = "tg_groupid", length = 32)
    private String            groupId;
    @Column(name = "tg_name", length = 64)
    private String            name;
    @Column(name = "tg_description", length = 100)
    private String            description;
    @Column(name = "tg_creatorid", length = 32)
    private String            creatorId;
    @Temporal(value = TemporalType.TIMESTAMP)
    @Column(name = "tg_createdate")
    private Date              createDate;
    public String getId() {
        return id;
    }
    public void setId(String id) {
        this.id = id;
    }
    public String getGroupId() {
        return groupId;
    }
    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
    public String getDescription() {
        return description;
    }
    public void setDescription(String description) {
        this.description = description;
    }
    public String getCreatorId() {
        return creatorId;
    }
    public void setCreatorId(String creatorId) {
        this.creatorId = creatorId;
    }
    public Date getCreateDate() {
        return createDate;
    }
    public void setCreateDate(Date createDate) {
        this.createDate = createDate;
    }
}
