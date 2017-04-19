package com.eayun.database.configgroup.model.configfile;

import com.alibaba.fastjson.JSONArray;

/**
 * Created by Administrator on 2017/3/6.
 */
public class Rdsconfigfile extends CloudRdsconfigfile {

    private JSONArray applyInstances ;  //对应该配置文件应用到的实例
    private String applyInstancesStringFormat ;
    private String versionName ;
    private String dcName ;

    private String projectName ;
    private String cusName ;

    public JSONArray getApplyInstances() {
        return applyInstances;
    }

    public void setApplyInstances(JSONArray applyInstances) {
        this.applyInstances = applyInstances;
        this.applyInstancesStringFormat = fromApplyInstances() ;
    }

    public String getApplyInstancesStringFormat() {
        return applyInstancesStringFormat;
    }

    public void setApplyInstancesStringFormat(String applyInstancesStringFormat) {
        this.applyInstancesStringFormat = applyInstancesStringFormat;
    }

    public String getVersionName() {
        return versionName;
    }

    public String getDcName() {
        return dcName;
    }

    public void setDcName(String dcName) {
        this.dcName = dcName;
    }

    public void setVersionName(String versionName) {
        this.versionName = versionName;
    }

    public String fromApplyInstances(){
        if (applyInstances == null){
            return "" ;
        }else {
            StringBuilder stringBuilder = new StringBuilder() ;
            for (int i=0 ; i<applyInstances.size() ; i++){
                stringBuilder.append(String.valueOf(applyInstances.getJSONObject(i).get("name"))) ;
                stringBuilder.append(" ; ") ;
            }
            String origin = stringBuilder.toString() ;
            if (origin == null || "".equals(origin.trim())){
                return "" ;
            }else {
                return origin.substring(0, origin.length()-2) ;
            }
        }
    }

    public String getProjectName() {
        return projectName;
    }

    public void setProjectName(String projectName) {
        this.projectName = projectName;
    }

    public String getCusName() {
        return cusName;
    }

    public void setCusName(String cusName) {
        this.cusName = cusName;
    }
}
