package com.eayun.virtualization.ecmccontroller;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.alibaba.fastjson.JSONObject;
import com.eayun.common.ConstantClazz;
import com.eayun.common.controller.BaseController;
import com.eayun.common.dao.ParamsMap;
import com.eayun.common.dao.support.Page;
import com.eayun.common.util.BeanUtils;
import com.eayun.log.ecmcsevice.EcmcLogService;
import com.eayun.virtualization.ecmcservice.EcmcVpnService;
import com.eayun.virtualization.model.CloudOrderVpn;
import com.eayun.virtualization.model.CloudVpn;

@Controller
@RequestMapping("/ecmc/cloud/vpn")
@Scope("prototype")
public class EcmcVpnController extends BaseController {

    private static final Logger log = LoggerFactory.getLogger(EcmcVpnController.class);
    @Autowired
    private EcmcVpnService vpnService;
    @Autowired
    private EcmcLogService ecmcLogService;
    
    @RequestMapping(value = "/getvpnlist")
    @ResponseBody
    public String getVpnList(HttpServletRequest request,Page page, @RequestBody ParamsMap paramsMap) {
        page = vpnService.getVpnList(page, paramsMap);
        return JSONObject.toJSONString(page);
    }
    @RequestMapping(value = "/getvpninfo")
    @ResponseBody
    public String getVpnInfo(HttpServletRequest request, @RequestBody Map<String, String> map) {
        String vpnId = map.get("vpnId").toString();
        CloudVpn cloudVpn = vpnService.getVpnInfo(vpnId);
        return JSONObject.toJSONString(cloudVpn);
    }
    @RequestMapping(value = "/deletevpn")
    @ResponseBody
    public String deleteVpn(HttpServletRequest request, @RequestBody Map<String, String> map){
    	//移除创建时间字段，避免因该字段值为null导致转化cloudOrderVpn失败
    	map.remove("createTime");
    	String prjId = map.get("prjId");
    	String vpnId = map.get("vpnId");
    	String vpnName = map.get("vpnName");
    	CloudOrderVpn cloudOrderVpn = new CloudOrderVpn();
    	BeanUtils.mapToBean(cloudOrderVpn, map);
    	try {
    	    vpnService.deleteVpn(cloudOrderVpn);
    	    ecmcLogService.addLog("删除IPSec VPN服务", ConstantClazz.LOG_TYPE_VPN, 
    	            vpnName, prjId, 1, vpnId, null);
    	} catch (Exception e) {
    	    ecmcLogService.addLog("删除IPSec VPN服务", ConstantClazz.LOG_TYPE_VPN, 
                    vpnName, prjId, 0, vpnId, e);
    	    log.error(e.toString(),e);
    	    throw e;
    	}
        return JSONObject.toJSONString("");
    }
    /**
     * vpn修改操作
     * @param request
     * @param cloudVpn
     * @return
     */
    @RequestMapping(value = "/updatevpn")
    @ResponseBody
    public String updateVpn(HttpServletRequest request, @RequestBody CloudVpn cloudVpn){
        try {
            cloudVpn = vpnService.updateVpn(cloudVpn);
            ecmcLogService.addLog("编辑IPSec VPN服务", ConstantClazz.LOG_TYPE_VPN, 
                    cloudVpn.getVpnName(), cloudVpn.getPrjId(), 1, cloudVpn.getVpnId(), null);
        } catch (Exception e) {
            ecmcLogService.addLog("编辑IPSec VPN服务", ConstantClazz.LOG_TYPE_VPN, 
                    cloudVpn.getVpnName(), cloudVpn.getPrjId(), 0, cloudVpn.getVpnId(), e);
            log.error(e.toString(),e);
            throw e;
        }
        return JSONObject.toJSONString("");
    }
    /**
     * 校验vpn名称重复的控制器
     * @author gaoxiang
     * @param request
     * @param map
     * @return
     */
    @RequestMapping(value = "/checkvpnnameexist")
    @ResponseBody
    public String checkVpnNameExist(HttpServletRequest request, @RequestBody Map<String, String> map) throws Exception {
        JSONObject json = new JSONObject();
        try {
            boolean flag = vpnService.checkVpnNameExist(map);
            json.put("respCode", ConstantClazz.SUCCESS_CODE);
            json.put("respData", flag);
        } catch (Exception e) {
            json.put("respCode", ConstantClazz.ERROR_CODE);
            throw e;
        }
        return json.toJSONString();
    }
}
