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
@Table(name = "sys_taggroup")
public class BaseTagGroup implements Serializable {

    private static final long serialVersionUID = 2969105118715519250L;
    @Id
    @GeneratedValue(generator = "system-uuid")
    @GenericGenerator(name = "system-uuid", strategy = "uuid")
    @Column(name = "tgrp_id", unique = true, nullable = false, length = 32)
    private String            id;

    @Column(name = "tgrp_name", length = 64)
    private String            name;

    @Column(name = "tgrp_abbr", length = 32)
    private String            abbreviation;                           //abbreviation of name

    @Column(name = "tgrp_description", length = 100)
    private String            description;

    @Column(name = "tgrp_enabled", length = 1)
    private String         enabled;

    @Column(name = "tgrp_unique", length = 1)
    private String         unique;

    @Column(name = "tgrp_creatorid", length = 32)
    private String            creatorId;

    @Temporal(value = TemporalType.TIMESTAMP)
    @Column(name = "tgrp_createdate")
    private Date              createDate;

    @Column(name = "tgrp_cusid", length = 32)
    private String            cusId;
    
    @Column(name = "tgrp_restype", length = 32)
    private String            resType;//可标注的资源类型，为空表示该标签类别下的标签可标记任何资源的类型。

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

    public String getAbbreviation() {
        return abbreviation;
    }

    public void setAbbreviation(String abbreviation) {
        this.abbreviation = abbreviation;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getEnabled() {
        return enabled;
    }

    public void setEnabled(String enabled) {
        this.enabled = enabled;
    }

    public String getUnique() {
        return unique;
    }

    public void setUnique(String unique) {
        this.unique = unique;
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

    public String getCusId() {
        return cusId;
    }

    public void setCusId(String cusId) {
        this.cusId = cusId;
    }

    public String getResType() {
        return resType;
    }

    public void setResType(String resType) {
        this.resType = resType;
    }

}
