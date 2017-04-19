package com.eayun.virtualization.model;

public class Tag extends BaseTag {

    private static final long serialVersionUID = 549091942482332942L;

    private String            tgCreator;                             //创建人
    private long              tgResNum;                              //已标记资源数量
    private String            tgCreateDate;                          //创建时间

    public String getTgCreator() {
        return tgCreator;
    }

    public void setTgCreator(String tgCreator) {
        this.tgCreator = tgCreator;
    }

    public long getTgResNum() {
        return tgResNum;
    }

    public void setTgResNum(long tgResNum) {
        this.tgResNum = tgResNum;
    }

    public String getTgCreateDate() {
        return tgCreateDate;
    }

    public void setTgCreateDate(String tgCreateDate) {
        this.tgCreateDate = tgCreateDate;
    }
}
