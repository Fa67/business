package com.eayun.charge.model;

import java.util.Date;

import com.eayun.price.bean.ParamBean;

public class ChargeRecord extends BaseChargeRecord {
    /**
     *Comment for <code>serialVersionUID</code>
     */
    private static final long serialVersionUID = -1155545082742533786L;
    /**
     * 计费参数
     */
    private ParamBean param;
    /**
     * 操作时间。<br/>具体指：删除、限制资源服务、恢复资源服务、客户解冻、资源放入回收站及还原的时间
     */
    private Date opTime;

    public Date getOpTime() {
        return opTime;
    }

    public void setOpTime(Date opTime) {
        this.opTime = opTime;
    }

    public ParamBean getParam() {
        return param;
    }

    public void setParam(ParamBean param) {
        this.param = param;
    }
}
