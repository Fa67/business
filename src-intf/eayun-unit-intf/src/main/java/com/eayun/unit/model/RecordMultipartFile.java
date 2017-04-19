package com.eayun.unit.model;

import java.io.Serializable;

import org.springframework.web.multipart.MultipartFile;

/**
 * @author jingang.liu@eayun.com
 * @Dadte 2016年12月22日
 */
public class RecordMultipartFile implements Serializable{

	private static final long serialVersionUID = 1051726467352422776L;
	private MultipartFile multipartfile;
	private String type;
	public MultipartFile getMultipartfile() {
		return multipartfile;
	}
	public void setMultipartfile(MultipartFile multipartfile) {
		this.multipartfile = multipartfile;
	}
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
	
}
