package com.eayun.work.model;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

import org.hibernate.annotations.GenericGenerator;

/**
 * 
 *                       
 * @Filename: BaseWorkFile.java
 * @Description: 
 * @Version: 1.0
 * @Author: 陈鹏飞
 * @Email: pengfei.chen@eayun.com
 * @History:<br>
 *<li>Date: 2015年11月10日</li>
 *<li>Version: 1.0</li>
 *<li>Content: create</li>
 *
 */
@Entity
@Table(name = "work_file")
public class BaseWorkFile implements Serializable{
    /**
     *Comment for <code>serialVersionUID</code>
     */
    private static final long serialVersionUID = -7769999998684360659L;
    @Id
    @GeneratedValue(generator = "system-uuid")
    @GenericGenerator(name = "system-uuid", strategy = "uuid")
    @Column(name = "file_id", unique = true, nullable = false, length = 32)
    private String fileId;
    @Column(name="opinion_id" ,length=32)
    private String opinionId;
    @Column(name="sacc_id" ,length=32)
    private String saccId;
    public String getFileId() {
        return fileId;
    }
    public void setFileId(String fileId) {
        this.fileId = fileId;
    }
    
    public String getOpinionId() {
        return opinionId;
    }
    public void setOpinionId(String opinionId) {
        this.opinionId = opinionId;
    }
    public String getSaccId() {
        return saccId;
    }
    public void setSaccId(String saccId) {
        this.saccId = saccId;
    }
    
    
}
