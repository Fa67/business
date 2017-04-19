package com.eayun.bean;

import com.eayun.common.tools.ExcelTitle;

/**
 * @author jingang.liu@eayun.com
 * @Dadte 2017年3月16日
 */
public class NewRecordExcel {

    @ExcelTitle(name="备案状态（新增不用填写）")
    private String status;
    
    @ExcelTitle(name="备案所属用户（备案记录所属系统用户）")
    private String RecordCusName;
    
    @ExcelTitle(name="主体备案号（未备案不用填写）")
    private String recordNo;
    
    @ExcelTitle(name="备案ID号（未备案不用填写）")
    private String recordId;
    
    @ExcelTitle(name="主办单位或主办人名称（必填）")
    private String headName;
    
    @ExcelTitle(name="投资人或主管单位（必填）")
    private String unitName;
    
    @ExcelTitle(name="单位性质（必填）")
    private String unitNature;
    
    @ExcelTitle(name="主办单位所在省（必填）")
    private String province;
    
    @ExcelTitle(name="主办单位所在市（必填）")
    private String city;
    
    @ExcelTitle(name="主办单位所在县（必填）")
    private String county;
    
    @ExcelTitle(name="主办单位详细通信地址（必填）")
    private String unitAddress;
    
    @ExcelTitle(name="主办人的证件类型（必填）")
    private String certificateType;
    
    @ExcelTitle(name="证件号码（必填）")
    private String certificateNo;
    
    @ExcelTitle(name="证件住所（必填）")
    private String certificateAddress;
    
    @ExcelTitle(name="报备方式（必填:代为报备，自行报备）")
    private String recordWay;
    
    @ExcelTitle(name="备注")
    private String remark;
    
    @ExcelTitle(name="负责人姓名（个人时选填）")
    private String dutyName;
    
    @ExcelTitle(name="办公室电话（个人时选填）")
    private String phone;
    
    @ExcelTitle(name="手机号码（必填）")
    private String dutyPhone;
    
    @ExcelTitle(name="电子邮件地址（必填）")
    private String dutyEmail;
    
    @ExcelTitle(name="MSN账号")
    private String msn;
    
    @ExcelTitle(name="QQ账号")
    private String qq;
    
    @ExcelTitle(name="负责人证件类型")
    private String dutyCertificateType;
    
    @ExcelTitle(name="负责人证件号码")
    private String dutyCertificateNo;
    
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

    public String getRecordId() {
        return recordId;
    }

    public void setRecordId(String recordId) {
        this.recordId = recordId;
    }

    public String getHeadName() {
        return headName;
    }

    public void setHeadName(String headName) {
        this.headName = headName;
    }

    public String getUnitName() {
        return unitName;
    }

    public void setUnitName(String unitName) {
        this.unitName = unitName;
    }

    public String getUnitNature() {
        return unitNature;
    }

    public void setUnitNature(String unitNature) {
        this.unitNature = unitNature;
    }

    public String getProvince() {
        return province;
    }

    public void setProvince(String province) {
        this.province = province;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getCounty() {
        return county;
    }

    public void setCounty(String county) {
        this.county = county;
    }

    public String getUnitAddress() {
        return unitAddress;
    }

    public void setUnitAddress(String unitAddress) {
        this.unitAddress = unitAddress;
    }

    public String getCertificateType() {
        return certificateType;
    }

    public void setCertificateType(String certificateType) {
        this.certificateType = certificateType;
    }

    public String getCertificateNo() {
        return certificateNo;
    }

    public void setCertificateNo(String certificateNo) {
        this.certificateNo = certificateNo;
    }

    public String getCertificateAddress() {
        return certificateAddress;
    }

    public void setCertificateAddress(String certificateAddress) {
        this.certificateAddress = certificateAddress;
    }

    public String getRecordWay() {
        return recordWay;
    }

    public void setRecordWay(String recordWay) {
        this.recordWay = recordWay;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    public String getDutyName() {
        return dutyName;
    }

    public void setDutyName(String dutyName) {
        this.dutyName = dutyName;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getDutyPhone() {
        return dutyPhone;
    }

    public void setDutyPhone(String dutyPhone) {
        this.dutyPhone = dutyPhone;
    }

    public String getDutyEmail() {
        return dutyEmail;
    }

    public void setDutyEmail(String dutyEmail) {
        this.dutyEmail = dutyEmail;
    }

    public String getMsn() {
        return msn;
    }

    public void setMsn(String msn) {
        this.msn = msn;
    }

    public String getQq() {
        return qq;
    }

    public void setQq(String qq) {
        this.qq = qq;
    }

    public String getDutyCertificateType() {
        return dutyCertificateType;
    }

    public void setDutyCertificateType(String dutyCertificateType) {
        this.dutyCertificateType = dutyCertificateType;
    }

    public String getDutyCertificateNo() {
        return dutyCertificateNo;
    }

    public void setDutyCertificateNo(String dutyCertificateNo) {
        this.dutyCertificateNo = dutyCertificateNo;
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
