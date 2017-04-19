package com.eayun.work.model;

import org.springframework.web.multipart.MultipartFile;

import com.eayun.file.model.EayunFile;

public class WorkFile extends BaseWorkFile {

    /**
     *Comment for <code>serialVersionUID</code>
     */
    private static final long serialVersionUID = -2681332867113778692L;
    
    private MultipartFile multipartFile;
    private EayunFile eayunFile;
    
    public MultipartFile getMultipartFile() {
        return multipartFile;
    }
    public void setMultipartFile(MultipartFile multipartFile) {
        this.multipartFile = multipartFile;
    }
	public EayunFile getEayunFile() {
		return eayunFile;
	}
	public void setEayunFile(EayunFile eayunFile) {
		this.eayunFile = eayunFile;
	}
    
    
}
