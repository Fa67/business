package com.eayun.monitor.model;

public class ContactGroup extends BaseContactGroup {

    private static final long serialVersionUID = -1763649260702315188L;
    
    private int contactNum;//组内联系人数量

    public int getContactNum() {
        return contactNum;
    }

    public void setContactNum(int contactNum) {
        this.contactNum = contactNum;
    }

}
