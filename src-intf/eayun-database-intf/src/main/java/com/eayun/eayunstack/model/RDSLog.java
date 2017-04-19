package com.eayun.eayunstack.model;

/**
 * Trove日志的实体类（用户操作底层的返回数据）
 * @author zhouhaitao
 *
 */
public class RDSLog {
	private String status;
	private String container;
	private String name;
	private String metafile;
	private String prefix;
	private String published;
	private String type;
	private String pending;
	
	public String getStatus() {
		return status;
	}
	public void setStatus(String status) {
		this.status = status;
	}
	public String getContainer() {
		return container;
	}
	public void setContainer(String container) {
		this.container = container;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getMetafile() {
		return metafile;
	}
	public void setMetafile(String metafile) {
		this.metafile = metafile;
	}
	public String getPrefix() {
		return prefix;
	}
	public void setPrefix(String prefix) {
		this.prefix = prefix;
	}
	public String getPublished() {
		return published;
	}
	public void setPublished(String published) {
		this.published = published;
	}
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
	public String getPending() {
		return pending;
	}
	public void setPending(String pending) {
		this.pending = pending;
	}
}
