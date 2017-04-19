package com.eayun.physical.model;

public class DcServer extends BaseDcServer {

    private static final long serialVersionUID = -5151502176437395850L;

    private String            cabinetName;                             //机柜名称

    public String getCabinetName() {
        return cabinetName;
    }

    public void setCabinetName(String cabinetName) {
        this.cabinetName = cabinetName;
    }
}
