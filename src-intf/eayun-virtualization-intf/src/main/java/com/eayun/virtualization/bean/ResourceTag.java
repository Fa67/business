package com.eayun.virtualization.bean;

/**
 * 用于展现当前资源已标记的标签类别名称、标签名称
 *                       
 * @Filename: ResourceTag.java
 * @Description: 
 * @Version: 1.0
 * @Author: fan.zhang
 * @Email: fan.zhang@eayun.com
 * @History:<br>
 *<li>Date: 2015年11月17日</li>
 *<li>Version: 1.0</li>
 *<li>Content: create</li>
 *
 */
public class ResourceTag {
    private String tagGroupId;
    private String tagId;
    private String tagGroupName;
    private String tagName;

    public String getTagGroupId() {
        return tagGroupId;
    }

    public void setTagGroupId(String tagGroupId) {
        this.tagGroupId = tagGroupId;
    }

    public String getTagId() {
        return tagId;
    }

    public void setTagId(String tagId) {
        this.tagId = tagId;
    }

    public String getTagGroupName() {
        return tagGroupName;
    }

    public void setTagGroupName(String tagGroupName) {
        this.tagGroupName = tagGroupName;
    }

    public String getTagName() {
        return tagName;
    }

    public void setTagName(String tagName) {
        this.tagName = tagName;
    }

}
