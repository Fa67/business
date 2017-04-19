package com.eayun.bean;

import com.eayun.common.tools.ExcelTitle;

/**
 * @author jingang.liu@eayun.com
 * @Dadte 2017年3月16日
 */
public class NewWebExcel {

    @ExcelTitle(name="ICP密码")
    private String password;
    
    @ExcelTitle(name="备案状态")
    private String status;
    
    @ExcelTitle(name="所属用户")
    private String RecordCusName;
    
    @ExcelTitle(name="主体备案号")
    private String recordNo;
    
    @ExcelTitle(name="网站名称")
    private String webName;
    
    @ExcelTitle(name="网站备案号")
    private String webRecordNo;
    
    @ExcelTitle(name="网站ID")
    private String webId;
    
    @ExcelTitle(name="网站服务内容")
    private String serviceContent;
    
    @ExcelTitle(name="网站语言类别")
    private String webLanguage;
    
    @ExcelTitle(name="首页url")
    private String domainUrl;
    
    @ExcelTitle(name="域名")
    private String domainName;
    
    @ExcelTitle(name="备注信息")
    private String webRemark;
    
    @ExcelTitle(name="前置或专项审批内容类型")
    private String webSpecial;
    
    @ExcelTitle(name="前置审批号")
    private String specialNo;
    
    @ExcelTitle(name="审批文件扫描件")
    private String specialFile;
    
    @ExcelTitle(name="网站负责人姓名")
    private String webDutyName;
    
    @ExcelTitle(name="办公室电话")
    private String webPhone;
    
    @ExcelTitle(name="手机号码")
    private String webDutyPhone;
    
    @ExcelTitle(name="电子邮件地址")
    private String webDutyEmail;
    
    @ExcelTitle(name="MSN账号")
    private String webMSN;
    
    @ExcelTitle(name="QQ账号")
    private String webQQ;
    
    @ExcelTitle(name="负责人证件类型")
    private String webDutyCertificateType;
    
    @ExcelTitle(name="负责人证件号码")
    private String webDutyCertificateNo;
    
    @ExcelTitle(name="网站分布地点")
    private String webAddress;

    @ExcelTitle(name="网站接入方式")
    private String accessType;
    
    @ExcelTitle(name="IP地址段起始-IP地址段截至")
    private String IPPeriod;

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getRecordCusName() {
        return RecordCusName;
    }

    public void setRecordCusName(String recordCusName) {
        RecordCusName = recordCusName;
    }

    public String getRecordNo() {
        return recordNo;
    }

    public void setRecordNo(String recordNo) {
        this.recordNo = recordNo;
    }

    public String getWebName() {
        return webName;
    }

    public void setWebName(String webName) {
        this.webName = webName;
    }

    public String getWebRecordNo() {
        return webRecordNo;
    }

    public void setWebRecordNo(String webRecordNo) {
        this.webRecordNo = webRecordNo;
    }

    public String getWebId() {
        return webId;
    }

    public void setWebId(String webId) {
        this.webId = webId;
    }

    public String getServiceContent() {
        return serviceContent;
    }

    public void setServiceContent(String serviceContent) {
        this.serviceContent = serviceContent;
    }

    public String getWebLanguage() {
        return webLanguage;
    }

    public void setWebLanguage(String webLanguage) {
        this.webLanguage = webLanguage;
    }

    public String getDomainUrl() {
        return domainUrl;
    }

    public void setDomainUrl(String domainUrl) {
        this.domainUrl = domainUrl;
    }

    public String getDomainName() {
        return domainName;
    }

    public void setDomainName(String domainName) {
        this.domainName = domainName;
    }

    public String getWebRemark() {
        return webRemark;
    }

    public void setWebRemark(String webRemark) {
        this.webRemark = webRemark;
    }

    public String getWebSpecial() {
        return webSpecial;
    }

    public void setWebSpecial(String webSpecial) {
        this.webSpecial = webSpecial;
    }

    public String getSpecialNo() {
        return specialNo;
    }

    public void setSpecialNo(String specialNo) {
        this.specialNo = specialNo;
    }

    public String getSpecialFile() {
        return specialFile;
    }

    public void setSpecialFile(String specialFile) {
        this.specialFile = specialFile;
    }

    public String getWebDutyName() {
        return webDutyName;
    }

    public void setWebDutyName(String webDutyName) {
        this.webDutyName = webDutyName;
    }

    public String getWebPhone() {
        return webPhone;
    }

    public void setWebPhone(String webPhone) {
        this.webPhone = webPhone;
    }

    public String getWebDutyPhone() {
        return webDutyPhone;
    }

    public void setWebDutyPhone(String webDutyPhone) {
        this.webDutyPhone = webDutyPhone;
    }

    public String getWebDutyEmail() {
        return webDutyEmail;
    }

    public void setWebDutyEmail(String webDutyEmail) {
        this.webDutyEmail = webDutyEmail;
    }

    public String getWebMSN() {
        return webMSN;
    }

    public void setWebMSN(String webMSN) {
        this.webMSN = webMSN;
    }

    public String getWebQQ() {
        return webQQ;
    }

    public void setWebQQ(String webQQ) {
        this.webQQ = webQQ;
    }

    public String getWebDutyCertificateType() {
        return webDutyCertificateType;
    }

    public void setWebDutyCertificateType(String webDutyCertificateType) {
        this.webDutyCertificateType = webDutyCertificateType;
    }

    public String getWebDutyCertificateNo() {
        return webDutyCertificateNo;
    }

    public void setWebDutyCertificateNo(String webDutyCertificateNo) {
        this.webDutyCertificateNo = webDutyCertificateNo;
    }

    public String getWebAddress() {
        return webAddress;
    }

    public void setWebAddress(String webAddress) {
        this.webAddress = webAddress;
    }

    public String getAccessType() {
        return accessType;
    }

    public void setAccessType(String accessType) {
        this.accessType = accessType;
    }

    public String getIPPeriod() {
        return IPPeriod;
    }

    public void setIPPeriod(String iPPeriod) {
        IPPeriod = iPPeriod;
    }
    
    
}
