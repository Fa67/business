package com.eayun.file.model;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

import org.hibernate.annotations.GenericGenerator;

/**
 * 系统文件存储记录              
 * @Filename: BaseFileBean.java
 * @Description: 
 * @Version: 1.0
 * @Author: duanbinbin
 * @Email: binbin.duan@eayun.com
 * @History:<br>
 *<li>Date: 2015年11月16日</li>
 *<li>Version: 1.0</li>
 *<li>Content: create</li>
 *
 */
@Entity
@Table(name = "sys_eayunfile")
public class BaseEayunFile  implements java.io.Serializable {
    /**
     *Comment for <code>serialVersionUID</code>
     */
    private static final long serialVersionUID = -8922587213101669985L;

    @Id
    @GeneratedValue(generator = "system-uuid")
    @GenericGenerator(name = "system-uuid", strategy = "uuid")
    @Column(name = "file_id", unique = true, nullable = false, length = 32)
    private String eayunFileId;          //ID
    
    @Column(name = "file_createdate")
    private Date fileCreatedate;    //上传时间
    
    @Column(name = "file_md5", length = 32)
    private String fileMD5;         //文件编码
    
    @Column(name = "file_groupname", length = 200)
    private String fileGroupname;   //group目录
    
    @Column(name = "file_path", length = 200)
    private String filePath;        //文件路径
    
    @Column(name = "file_name", length = 200)
    private String fileName;        //原文件名
    
    @Column(name = "file_type", length = 200)
    private String fileType;        //文件类型
    
    @Column(name = "file_username", length = 200)
    private String fileUserName;    //上传人账户名
    
    @Column(name = "file_size")
    private long fileSize;        //文件大小
    
    public String getEayunFileId() {
        return eayunFileId;
    }

    public void setEayunFileId(String eayunFileId) {
        this.eayunFileId = eayunFileId;
    }
    
    public Date getFileCreatedate() {
        return fileCreatedate;
    }

    public void setFileCreatedate(Date fileCreatedate) {
        this.fileCreatedate = fileCreatedate;
    }

    public String getFileMD5() {
        return fileMD5;
    }

    public void setFileCode(String fileMD5) {
        this.fileMD5 = fileMD5;
    }

    public String getFileGroupname() {
        return fileGroupname;
    }

    public void setFileGroupname(String fileGroupname) {
        this.fileGroupname = fileGroupname;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getFileType() {
        return fileType;
    }

    public void setFileType(String fileType) {
        this.fileType = fileType;
    }

    public String getFileUserName() {
        return fileUserName;
    }

    public void setFileUserName(String fileUserName) {
        this.fileUserName = fileUserName;
    }

    public long getFileSize() {
        return fileSize;
    }

    public void setFileSize(long fileSize) {
        this.fileSize = fileSize;
    }
    
    

}
