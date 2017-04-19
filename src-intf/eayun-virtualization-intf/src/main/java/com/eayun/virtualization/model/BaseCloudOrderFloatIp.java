package com.eayun.virtualization.model;

import java.math.BigDecimal;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import org.hibernate.annotations.GenericGenerator;

/**
 * 浮动订单IP
 *
 * @Filename: BaseCloudOrderFloatIp.java
 * @Description:
 * @Version: 1.0
 * @Author: 陈鹏飞
 * @Email: pengfei.chen@eayun.com
 * @History:<br> <li>Date: 2016年8月4日</li>
 * <li>Version: 1.0</li>
 * <li>Content: create</li>
 */
@Entity
@Table(name = "cloudorder_floatip")
public class BaseCloudOrderFloatIp implements java.io.Serializable {

    /**
     * 
     */
    private static final long serialVersionUID = -270842827244053425L;

    /**
     * Comment for <code>serialVersionUID</code>
     */

    @Id
    @GeneratedValue(generator = "system-uuid")
    @GenericGenerator(name = "system-uuid", strategy = "uuid")
    @Column(name = "cof_id", unique = true, nullable = false, length = 32)
    private String cofId;                   //业务id

    @Column(name = "flo_id", length = 2000)
    private String floId;                   //浮动ip的id

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "create_time", length = 19)
    private Date createTime;                //创建时间
    @Column(name = "prj_id", length = 32)
    private String prjId;                   //项目id
    @Column(name = "dc_id", length = 32)
    private String dcId;                    //数据中心id
    @Column(name = "product_count")
    private int productCount;               //创建数量
    @Column(name = "cre_user", length = 32)
    private String creUser;                 //创建人id
    @Column(name = "order_no", length = 18)
    private String orderNo;                 //订单标号
    @Column(name = "buy_cycle")
    private int buyCycle;                   //时长（包年包月使用）
    @Column(name = "order_type", length = 1)
    private String orderType;               //订单类型(操作)
    @Column(name = "pay_type", length = 1)
    private String payType;                 //付款类型
    @Column(name = "price", length = 16)
    private BigDecimal price;               //总价
    @Column(name = "cus_id", length = 100)
    private String cusId;                   //客户id
    
    public String getCofId() {
        return cofId;
    }

    public void setCofId(String cofId) {
        this.cofId = cofId;
    }

    public String getFloId() {
        return floId;
    }

    public void setFloId(String floId) {
        this.floId = floId;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public String getPrjId() {
        return prjId;
    }

    public void setPrjId(String prjId) {
        this.prjId = prjId;
    }

    public String getDcId() {
        return dcId;
    }

    public void setDcId(String dcId) {
        this.dcId = dcId;
    }

    public int getProductCount() {
        return productCount;
    }

    public void setProductCount(int productCount) {
        this.productCount = productCount;
    }

    public String getCreUser() {
        return creUser;
    }

    public void setCreUser(String creUser) {
        this.creUser = creUser;
    }

    public String getOrderNo() {
        return orderNo;
    }

    public void setOrderNo(String orderNo) {
        this.orderNo = orderNo;
    }

    public int getBuyCycle() {
        return buyCycle;
    }

    public void setBuyCycle(int buyCycle) {
        this.buyCycle = buyCycle;
    }

    public String getOrderType() {
        return orderType;
    }

    public void setOrderType(String orderType) {
        this.orderType = orderType;
    }

    public String getPayType() {
        return payType;
    }

    public void setPayType(String payType) {
        this.payType = payType;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public String getCusId() {
        return cusId;
    }

    public void setCusId(String cusId) {
        this.cusId = cusId;
    }
}
