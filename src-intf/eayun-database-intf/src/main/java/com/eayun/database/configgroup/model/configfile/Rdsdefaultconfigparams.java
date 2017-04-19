package com.eayun.database.configgroup.model.configfile;

/**
 * Created by Administrator on 2017/3/7.
 */
public class Rdsdefaultconfigparams extends CloudRdsdefaultconfigparams {

    private String currentParamValue ;

    public String getCurrentParamValue() {
        return currentParamValue;
    }

    public void setCurrentParamValue(String currentParamValue) {
        this.currentParamValue = currentParamValue;
    }
}
