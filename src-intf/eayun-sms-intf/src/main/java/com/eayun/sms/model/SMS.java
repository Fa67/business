package com.eayun.sms.model;

import java.util.List;

public class SMS extends BaseSMS {

    private static final long serialVersionUID = -2979456170864774412L;
    
    private List<String> mobilesList;

    public List<String> getMobilesList() {
        return mobilesList;
    }

    public void setMobilesList(List<String> mobilesList) {
        this.mobilesList = mobilesList;
    }

}
