package com.eayun.virtualization.controller;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.eayun.common.ConstantClazz;
import com.eayun.common.controller.BaseController;
import com.eayun.common.dao.ParamsMap;
import com.eayun.common.dao.support.Page;
import com.eayun.common.filter.SessionUserInfo;
import com.eayun.common.model.EayunResponseJson;
import com.eayun.log.service.LogService;
import com.eayun.virtualization.model.CloudOrderVpn;
import com.eayun.virtualization.model.CloudVpn;
import com.eayun.virtualization.service.CloudOrderVpnService;
import com.eayun.virtualization.service.VpnService;

@Controller
@RequestMapping("/cloud/vpn")
@Scope("prototype")
public class VpnController extends BaseController {

    private static final Logger log = LoggerFactory.getLogger(VpnController.class);
    @Autowired
    private LogService logService;
    @Autowired
    private VpnService vpnService;
    @Autowired
    private CloudOrderVpnService orderVpnService;
    /**
     * 获取vpn列表数据的控制器
     * @author gaoxiang
     * @param request
     * @param page
     * @param paramsMap
     * @return
     */
    @RequestMapping(value = "/getvpnlist")
    @ResponseBody
    public String getVpnList(HttpServletRequest request,Page page, @RequestBody ParamsMap paramsMap) {
        page = vpnService.getVpnList(page, paramsMap);
        return JSONObject.toJSONString(page);
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
    @RequestMapping(value = "/getvpninfo")
    @ResponseBody
    public String getVpnInfo(HttpServletRequest request, @RequestBody Map<String, String> map) {
        String vpnId = map.get("vpnId").toString();
        CloudVpn cloudVpn = vpnService.getVpnInfo(vpnId);
        return JSONObject.toJSONString(cloudVpn);
    }
    /**
     * 提交购买vpn订单的控制器
     * @author gaoxiang
     * @param request
     * @param cloudOrderVpn
     * @return
     * @throws Exception
     */
    @RequestMapping(value = "/buyvpn")
    @ResponseBody
    public String buyVpn(HttpServletRequest request, @RequestBody CloudOrderVpn cloudOrderVpn) throws Exception {
        SessionUserInfo sessionUser = (SessionUserInfo) request.getSession().getAttribute(ConstantClazz.SYS_SESSION_USERINFO);
        JSONObject json = new JSONObject();
        String errMsg = new String();
        try {
            errMsg = vpnService.buyVpn(cloudOrderVpn, sessionUser);
            if (!StringUtils.isEmpty(errMsg) && null != errMsg) {
                json.put("respCode", ConstantClazz.WARNING_CODE);
                json.put("respMsg", errMsg);
            } else {
                json.put("respCode", ConstantClazz.SUCCESS_CODE);
                json.put("orderNo", cloudOrderVpn.getOrderNo());
            }
        } catch (Exception e) {
            if ("余额不足".equals(e.getMessage())) {
                json.put("respCode", ConstantClazz.WARNING_CODE);
                json.put("respMsg", "CHANGE_OF_BALANCE");
            } else {
                json.put("respCode", ConstantClazz.ERROR_CODE);
                json.put("respMsg", e.getMessage());
            }
            log.error(e.getMessage(), e);
        }
        return json.toJSONString();
    }
    /*@RequestMapping(value = "/createvpn")
    @ResponseBody
    public String createVpn(HttpServletRequest request, @RequestBody CloudVpn cloudVpn) {
        cloudVpn = vpnService.createVpn(cloudVpn);
        return JSONObject.toJSONString(cloudVpn);
    }*/
    
    @RequestMapping(value = "/updatevpn")
    @ResponseBody
    public String updateVpn(HttpServletRequest request, @RequestBody CloudVpn cloudVpn) {
        JSONObject json = new JSONObject();
        try {
            cloudVpn = vpnService.updateVpn(cloudVpn);
            json.put("respData", cloudVpn != null);
            if (cloudVpn != null) {
                logService.addLog("编辑IPSec VPN服务", 
                        ConstantClazz.LOG_TYPE_VPN, 
                        cloudVpn.getVpnName(), 
                        cloudVpn.getPrjId(), 
                        ConstantClazz.LOG_STATU_SUCCESS, 
                        null);
            }
        } catch (Exception e) {
            logService.addLog("编辑IPSec VPN服务", 
                    ConstantClazz.LOG_TYPE_VPN, 
                    cloudVpn.getVpnName(), 
                    cloudVpn.getPrjId(), 
                    ConstantClazz.LOG_STATU_ERROR, 
                    e);
            throw e;
        }
        return json.toJSONString();
    }
    
    @RequestMapping(value = "/deletevpn")
    @ResponseBody
    public String deleteVpn(HttpServletRequest request, @RequestBody CloudOrderVpn cloudOrderVpn) {
        SessionUserInfo sessionUser = (SessionUserInfo) request.getSession().getAttribute(ConstantClazz.SYS_SESSION_USERINFO);
        cloudOrderVpn.setCusId(sessionUser.getCusId());
        JSONObject json = new JSONObject();
        boolean flag = false;
        try {
            flag = vpnService.deleteVpn(cloudOrderVpn);
            json.put("respData", flag);
            if (flag) {
                logService.addLog("删除IPSec VPN服务", 
                        ConstantClazz.LOG_TYPE_VPN, 
                        cloudOrderVpn.getVpnName(), 
                        cloudOrderVpn.getPrjId(), 
                        ConstantClazz.LOG_STATU_SUCCESS, 
                        null);
            }
        } catch (Exception e) {
            logService.addLog("删除IPSec VPN服务", 
                    ConstantClazz.LOG_TYPE_VPN, 
                    cloudOrderVpn.getVpnName(), 
                    cloudOrderVpn.getPrjId(), 
                    ConstantClazz.LOG_STATU_ERROR, 
                    e);
            throw e;
        }
        return json.toJSONString();
    }
    
    @RequestMapping(value = "/querynetworkbyprjid", method = RequestMethod.POST)
    @ResponseBody
    public String queryNetworkByPrjId(HttpServletRequest request, @RequestBody Map<String, String> map){
        String prjId = map.get("prjId").toString();
        List<Map<String, Object>> list = vpnService.getCloudNetworkList(prjId);
        return JSONObject.toJSONString(list);
    }
    
    @RequestMapping(value = "/getvpnquotasbyprjid", method = RequestMethod.POST)
    @ResponseBody
    public String getVpnQuotasByPrjId(HttpServletRequest request, @RequestBody Map<String, String> map) {
        JSONObject json = new JSONObject();
        String prjId = map.get("prjId").toString();
        int quota = vpnService.getVpnQuotasByPrjId(prjId);
        json.put("quota", quota);
        return json.toString();
    }

    @RequestMapping(value = "/checkVpnOrderExist", method = RequestMethod.POST)
    @ResponseBody
    public String checkVpnOrderExist (HttpServletRequest request, @RequestBody String vpnId) throws Exception{
        log.info("检查当前是否已存在私有网络续费或变配的未完成订单");
        JSONObject json = new JSONObject();
        try{
            boolean flag = vpnService.checkVpnOrderExist(vpnId);
            json.put("flag", flag);
        }catch(Exception e){
            throw e;
        }
        return json.toJSONString();
    }
    
    @RequestMapping(value = "/getprice", method = RequestMethod.POST)
    @ResponseBody
    public String getPrice(HttpServletRequest request, @RequestBody CloudOrderVpn cloudOrderVpn) {
        EayunResponseJson json = new EayunResponseJson();
        try {
            BigDecimal price = vpnService.getPrice(cloudOrderVpn);
            json.setRespCode(ConstantClazz.SUCCESS_CODE);
            json.setData(price);
        } catch (Exception e) {
            log.error(e.toString(), e);
            json.setRespCode(ConstantClazz.ERROR_CODE);
            json.setMessage(e.getMessage());
        }
        return JSON.toJSONString(json);
    }
    /**
     * VPN续费开始
     * @author zhangfan
     * @param request
     * @param map
     * @return
     * @throws Exception
     */
    @RequestMapping(value = "/renewvpn", method = RequestMethod.POST)
    @ResponseBody
    public String renewVpn(HttpServletRequest request, @RequestBody Map map) throws Exception {
        log.info("续费VPN开始");
        SessionUserInfo sessionUser = (SessionUserInfo) request.getSession().getAttribute(ConstantClazz.SYS_SESSION_USERINFO);
        JSONObject json = new JSONObject();
        Map<String, String> respMap;
        try {
            respMap = vpnService.renewVpn(sessionUser, map);
            if(respMap.containsKey(ConstantClazz.WARNING_CODE)){
                json.put("respCode", ConstantClazz.WARNING_CODE);
                json.put("message", respMap.get(ConstantClazz.WARNING_CODE));
            }else if(respMap.containsKey(ConstantClazz.SUCCESS_CODE)){
                json.put("respCode", ConstantClazz.SUCCESS_CODE);
                json.put("message", respMap.get(ConstantClazz.SUCCESS_CODE));
                json.put("orderNo", respMap.get("orderNo"));
            }
        } catch(Exception e) {
            log.error("续费VPN异常",e);
        }
        return json.toJSONString();
    }
    /**
     * 根据订单编号查询订单信息
     * @author gaoxiang
     * @param request
     * @param map
     * @return
     */
    @RequestMapping(value = "/getordervpnbyorderno", method = RequestMethod.POST)
    @ResponseBody
    public String getOrderVpnByOrderNo(HttpServletRequest request, @RequestBody Map<String, String> map) {
        String orderNo = map.get("orderNo");
        EayunResponseJson json = new EayunResponseJson();
        CloudOrderVpn vpn = orderVpnService.getOrderVpnByOrderNo(orderNo);
        json.setRespCode(ConstantClazz.SUCCESS_CODE);
        json.setData(vpn);
        return JSONObject.toJSONString(json);
    }
}
