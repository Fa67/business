package com.eayun.virtualization.model;

public class CloudFloatIp extends BaseCloudFloatIp {

    /**
     * Comment for <code>serialVersionUID</code>
     */
    private static final long serialVersionUID = 3533303922324608053L;

    private String vmName;  //云主机名称
    private String netName; //网络名称
    private String prjName; //项目名称
    private String dcName;  //数据中心名称
    private String vmIp;    //内网IP
    private String tagsName;//已绑定标签名称
    private String resNameForRenew;

    private String tagName;//因加pop框增添字段
    private String cusId;
    private String subnetIp;



    private String chargeStateName;
    private String resourceName;
    private String portId;
    private String cusName; //客户名称

    public String getErrMsg() {
        return errMsg;
    }

    public void setErrMsg(String errMsg) {
        this.errMsg = errMsg;
    }

    public String getBtnFlag() {
        return btnFlag;
    }

    public void setBtnFlag(String btnFlag) {
        this.btnFlag = btnFlag;
    }

    private String errMsg;
    private String btnFlag;

    public String getCusName() {
        return cusName;
    }

    public void setCusName(String cusName) {
        this.cusName = cusName;
    }

    public String getSubnetIp() {
        return subnetIp;
    }

    public void setSubnetIp(String subnetIp) {
        this.subnetIp = subnetIp;
    }

    public String getPortId() {
        return portId;
    }

    public void setPortId(String portId) {
        this.portId = portId;
    }

    public String getVmName() {
        return vmName;
    }

    public void setVmName(String vmName) {
        this.vmName = vmName;
    }

    public String getNetName() {
        return netName;
    }

    public void setNetName(String netName) {
        this.netName = netName;
    }

    public String getPrjName() {
        return prjName;
    }

    public void setPrjName(String prjName) {
        this.prjName = prjName;
    }

    public String getDcName() {
        return dcName;
    }

    public void setDcName(String dcName) {
        this.dcName = dcName;
    }

    public String getTagsName() {
        return tagsName;
    }

    public void setTagsName(String tagsName) {
        this.tagsName = tagsName;
    }

    public String getResNameForRenew() {
        return resNameForRenew;
    }

    public void setResNameForRenew(String resNameForRenew) {
        this.resNameForRenew = resNameForRenew;
    }

    public String getTagName() {
        return tagName;
    }

    public void setTagName(String tagName) {
        this.tagName = tagName;
    }

    public String getVmIp() {
        return vmIp;
    }

    public void setVmIp(String vmIp) {
        this.vmIp = vmIp;
    }

    public String getResourceName() {
        return resourceName;
    }

    public void setResourceName(String resourceName) {
        this.resourceName = resourceName;
    }

    public String getChargeStateName() {
        return chargeStateName;
    }

    public void setChargeStateName(String chargeStateName) {
        this.chargeStateName = chargeStateName;
    }

    public String getCusId() {
        return cusId;
    }

    public void setCusId(String cusId) {
        this.cusId = cusId;
    }
}
