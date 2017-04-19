package com.eayun.virtualization.model;

public class TagResource extends BaseTagResource {

    private static final long serialVersionUID = -3165000845410500039L;

    private String            resourceName;                             //资源名称
    private String            resourceTypeName;                         //资源类型
    private String            tagName;                                  //标签名称
    private String            projectName;                              //项目名称
    public String getResourceName() {
        return resourceName;
    }
    public void setResourceName(String resourceName) {
        this.resourceName = resourceName;
    }
    public String getResourceTypeName() {
        return resourceTypeName;
    }
    public void setResourceTypeName(String resourceTypeName) {
        this.resourceTypeName = resourceTypeName;
    }
    public String getProjectName() {
        return projectName;
    }
    public void setProjectName(String projectName) {
        this.projectName = projectName;
    }
    public String getTagName() {
        return tagName;
    }
    public void setTagName(String tagName) {
        this.tagName = tagName;
    }
    
}
