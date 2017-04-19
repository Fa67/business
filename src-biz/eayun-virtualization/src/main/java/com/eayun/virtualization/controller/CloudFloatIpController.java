package com.eayun.virtualization.controller;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import com.eayun.virtualization.model.*;
import com.eayun.common.constant.PayType;
import com.eayun.common.filter.SessionUserInfo;
import com.eayun.common.util.StringUtil;
import com.eayun.virtualization.model.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.alibaba.fastjson.JSONObject;
import com.eayun.common.ConstantClazz;
import com.eayun.common.controller.BaseController;
import com.eayun.common.dao.ParamsMap;
import com.eayun.common.dao.QueryMap;
import com.eayun.common.dao.support.Page;
import com.eayun.common.filter.SessionUserInfo;
import com.eayun.common.model.EayunResponseJson;
import com.eayun.common.util.StringUtil;
import com.eayun.log.service.LogService;
import com.eayun.virtualization.model.BaseCloudFloatIp;
import com.eayun.virtualization.model.BaseCloudNetwork;
import com.eayun.virtualization.model.BaseCloudSubNetWork;
import com.eayun.virtualization.model.CloudFloatIp;
import com.eayun.virtualization.model.CloudOrderFloatIp;
import com.eayun.virtualization.model.CloudProject;
import com.eayun.virtualization.service.CloudFloatIpService;

/**
 * 公网IP接口
 *
 * @Filename: FloatIpController.java
 * @Description:
 * @Version: 1.0
 * @Author: duanbinbin
 * @Email: binbin.duan@eayun.com
 * @History:<br> <li>Date: 2015年11月3日</li>
 * <li>Version: 1.0</li>
 * <li>Content: create</li>
 */
@Controller
@RequestMapping("/cloud/floatip")
@Scope("prototype")
public class CloudFloatIpController extends BaseController {
    private static final Logger log = LoggerFactory.getLogger(CloudFloatIpController.class);

    @Autowired
    private CloudFloatIpService floatIpService;

    @Autowired
    private LogService logService;

    @RequestMapping(value = "/getIpList", method = RequestMethod.POST)
    @ResponseBody
    public String getLogList(HttpServletRequest request, Page page, @RequestBody ParamsMap map) throws Exception {
        log.info("获取公网IP列表开始");
        String projectId = map.getParams().get("projectId").toString();
        int pageSize = map.getPageSize();
        int pageNumber = map.getPageNumber();

        QueryMap queryMap = new QueryMap();
        queryMap.setPageNum(pageNumber);
        queryMap.setCURRENT_ROWS_SIZE(pageSize);
        page = floatIpService.getListByPrj(page, projectId, queryMap);
        return JSONObject.toJSONString(page);
    }

    /**
     * 查询项目下的FloatIp的配额和使用情况
     *
     * @param request
     * @param cloudFloatIp
     * @return
     * @throws Exception
     */
    @RequestMapping(value = "/queryFloatIpQuatoByPrj", method = RequestMethod.POST)
    @ResponseBody
    public String queryFloatIpQuatoByPrj(HttpServletRequest request, @RequestBody CloudFloatIp cloudFloatIp) throws Exception {
        log.info("查询项目下的FloatIp的配额和使用情况");
        CloudProject project = null;
        try {
            project = floatIpService.queryFloatIpQuatoByPrj(cloudFloatIp.getPrjId());
        } catch (Exception e) {
            log.error("查询项目下的FloatIp的配额和使用情况失败", e);
            throw e;
        }
        return JSONObject.toJSONString(project);
    }

    /**
     * 申请公网IP
     *
     * @param request
     * @param cloudFloatIp
     * @return
     * @throws Exception
     */
    @RequestMapping(value = "/createFloatIp", method = RequestMethod.POST)
    @ResponseBody
    public String createFloatIp(HttpServletRequest request, @RequestBody CloudFloatIp cloudFloatIp) throws Exception {
        log.info("申请弹性公网IP");
        JSONObject json = new JSONObject();
        CloudFloatIp floatip = null;
        try {
            floatip = floatIpService.createFloatIp(cloudFloatIp);
            json.put("data", floatip);
            json.put("respCode", ConstantClazz.SUCCESS_CODE_ADD);
            logService.addLog("申请", ConstantClazz.LOG_TYPE_FLOATIP, floatip.getFloIp(), cloudFloatIp.getPrjId(),
                    ConstantClazz.LOG_STATU_SUCCESS, null);
        } catch (Exception e) {
            json.put("respCode", ConstantClazz.ERROR_CODE);
            logService.addLog("申请", ConstantClazz.LOG_TYPE_FLOATIP, null, cloudFloatIp.getPrjId(),
                    ConstantClazz.LOG_STATU_ERROR, null);
            log.error("申请弹性公网IP失败", e);
            throw e;
        }
        return json.toJSONString();
    }

    /**
     * 释放公网IP
     *
     * @param request
     * @param cloudFloatIp
     * @return
     * @throws Exception
     */
    @RequestMapping(value = "/releaseFloatIp", method = RequestMethod.POST)
    @ResponseBody
    public String releaseFloatIp(HttpServletRequest request, @RequestBody CloudFloatIp cloudFloatIp) throws Exception {
        log.info("释放弹性公网IP");
        JSONObject json = new JSONObject();
        CloudFloatIp floatip = null;
        try {
            floatip = floatIpService.deleteFloatIp(cloudFloatIp,null);
            json.put("data", floatip);
            json.put("respCode", ConstantClazz.SUCCESS_CODE_DELETE);
            logService.addLog("释放", ConstantClazz.LOG_TYPE_FLOATIP, floatip.getFloIp(), floatip.getPrjId(),
                    ConstantClazz.LOG_STATU_SUCCESS, null);
        } catch (Exception e) {
            json.put("respCode", ConstantClazz.ERROR_CODE);
            if(floatip!=null){
                logService.addLog("释放", ConstantClazz.LOG_TYPE_FLOATIP, floatip.getFloIp(), floatip.getPrjId(),
                    ConstantClazz.LOG_STATU_ERROR, null);
            }
            log.error("释放弹性公网IP失败", e);
            throw e;
        }
        return json.toJSONString();
    }

    /**
     * 查询项目下的网络列表
     *
     * @param request
     * @param prjId
     * @return
     * @throws Exception
     */
    @RequestMapping(value = "/getNetworkByPrj", method = RequestMethod.POST)
    @ResponseBody
    public String getNetworkByPrj(HttpServletRequest request, @RequestBody String prjId) throws Exception {
        log.info("查询项目下的网络列表");
        List<BaseCloudNetwork> netList;
        try {
            netList = floatIpService.getNetworkByPrj(prjId);
        } catch (Exception e) {
            log.error("查询项目下的网络列表失败", e);
            throw e;
        }
        return JSONObject.toJSONString(netList);
    }

    /**
     * 查询网络下的子网
     *
     * @param request
     * @param netId
     * @return
     * @throws Exception
     */
    @RequestMapping(value = "/getSubnetByNetwork", method = RequestMethod.POST)
    @ResponseBody
    public String getSubnetByNetwork(HttpServletRequest request, @RequestBody String netId) throws Exception {
        log.info("查询网络下的子网列表");
        List<BaseCloudSubNetWork> netList;
        try {
            netList = floatIpService.getSubnetByNetwork(netId);
        } catch (Exception e) {
            log.error("查询网络下的子网列表失败", e);
            throw e;
        }
        return JSONObject.toJSONString(netList);
    }

    /**
     * 查询子网下指定的资源
     *
     * @param request
     * @param cloudFloatIp
     * @return
     * @throws Exception
     */
    @RequestMapping(value = "/getResourceBySubnet", method = RequestMethod.POST)
    @ResponseBody
    public String getResourceBySubnet(HttpServletRequest request, @RequestBody CloudFloatIp cloudFloatIp) throws Exception {
        log.info("查询子网下的资源列表");
        List<CloudFloatIp> netList;
        try {
            netList = floatIpService.getResourceBySubnet(cloudFloatIp);
        } catch (Exception e) {
            log.error("查询子网下的资源列表失败", e);
            throw e;
        }
        return JSONObject.toJSONString(netList);
    }


    /**
     * 绑定
     *
     * @param request
     * @return
     * @throws Exception
     */
    @RequestMapping(value = "/bindResource", method = RequestMethod.POST)
    @ResponseBody
    public String bindResource(HttpServletRequest request, @RequestBody CloudFloatIp cloudFloatIp) throws Exception {
        log.info("公网IP绑定资源开始");
        JSONObject json = new JSONObject();
        CloudFloatIp cloudFloat = null;
        try {
            cloudFloat = floatIpService.bindResource(cloudFloatIp);
            logService.addLog("绑定", ConstantClazz.LOG_TYPE_FLOATIP, cloudFloatIp.getFloIp(), cloudFloatIp.getPrjId(), ConstantClazz.LOG_STATU_SUCCESS, null);
            json.put("data", cloudFloat);
            json.put("respCode", ConstantClazz.SUCCESS_CODE_OP);
        } catch (Exception e) {
            logService.addLog("绑定", ConstantClazz.LOG_TYPE_FLOATIP, cloudFloatIp.getFloIp(), cloudFloatIp.getPrjId(), ConstantClazz.LOG_STATU_ERROR, e);
            json.put("respCode", ConstantClazz.ERROR_CODE);
            throw e;
        }
        return json.toJSONString();
    }

    /**
     * 解绑
     *
     * @param request
     * @param cloudFloatIp
     * @return
     * @throws Exception
     */
    @RequestMapping(value = "/unbundingResource", method = RequestMethod.POST)
    @ResponseBody
    public String unbundingResource(HttpServletRequest request, @RequestBody CloudFloatIp cloudFloatIp) throws Exception {
        log.info("公网IP解绑开始");
        JSONObject json = new JSONObject();
        CloudFloatIp cloudFloat = null;
        try {
            cloudFloat = floatIpService.unbundingResource(cloudFloatIp);
            logService.addLog("解绑", ConstantClazz.LOG_TYPE_FLOATIP, cloudFloatIp.getFloIp(), cloudFloatIp.getPrjId(), ConstantClazz.LOG_STATU_SUCCESS, null);
            json.put("data", cloudFloat);
            json.put("respCode", ConstantClazz.SUCCESS_CODE_OP);
        } catch (Exception e) {
            logService.addLog("解绑", ConstantClazz.LOG_TYPE_FLOATIP, cloudFloatIp.getFloIp(), cloudFloatIp.getPrjId(), ConstantClazz.LOG_STATU_ERROR, e);
            json.put("respCode", ConstantClazz.ERROR_CODE);
            throw e;
        }
        return json.toJSONString();
    }

    /**
     * 查询未绑定资源的公网IP
     *
     * @param request
     * @param cloudFloatIp
     * @return
     * @throws Exception
     */
    @RequestMapping(value = "/getUnBindFloatIp", method = RequestMethod.POST)
    @ResponseBody
    public String getUnBindFloatIp(HttpServletRequest request, @RequestBody CloudFloatIp cloudFloatIp) throws Exception {
        log.info("查询未绑定资源的公网IP");
        List<BaseCloudFloatIp> list = null;
        try {
            list = floatIpService.getUnBindFloatIp(cloudFloatIp.getPrjId());
        } catch (Exception e) {
            log.error("查询未绑定资源的公网IP失败", e);
            throw e;
        }
        return JSONObject.toJSONString(list);
    }
    //----------------------2016.8.11--陈鹏飞---

    /**
     * g购买弹性公网ip
     *
     * @param request
     * @param cloudOrderFloatIp
     * @return
     * @throws Exception
     */
    @RequestMapping(value = "/buyfloatip", method = RequestMethod.POST)
    @ResponseBody
    public String buyFloatIp(HttpServletRequest request, @RequestBody CloudOrderFloatIp cloudOrderFloatIp) throws Exception {
        log.info("弹性公网ip购买");
        SessionUserInfo sessionUser = (SessionUserInfo) request.getSession().getAttribute(ConstantClazz.SYS_SESSION_USERINFO);
        String userId = sessionUser.getUserId();
        String cusId = sessionUser.getCusId();
        cloudOrderFloatIp.setCusId(cusId);
        cloudOrderFloatIp.setCreUser(userId);
        JSONObject json = new JSONObject();
        try {
            cloudOrderFloatIp = floatIpService.buyFloatIp(cloudOrderFloatIp, true);
            if (cloudOrderFloatIp.getCloudFloatIpList()!=null && cloudOrderFloatIp.getCloudFloatIpList().size()>0
                    && !StringUtil.isEmpty(cloudOrderFloatIp.getCloudFloatIpList().get(0).getErrMsg())) {
                json.put("respCode", ConstantClazz.ERROR_CODE);
                json.put("btnFlag", cloudOrderFloatIp.getCloudFloatIpList().get(0).getBtnFlag());
                json.put("message", cloudOrderFloatIp.getCloudFloatIpList().get(0).getErrMsg());
            }else{
                json.put("respCode", ConstantClazz.SUCCESS_CODE_ADD);
                json.put("orderNo", cloudOrderFloatIp.getOrderNo());
            }
            return json.toString();
        }catch (Exception e){
            json.put("respCode", ConstantClazz.ERROR_CODE);
            json.put("btnFlag", cloudOrderFloatIp.getCloudFloatIpList().get(0).getBtnFlag());
            json.put("message", cloudOrderFloatIp.getCloudFloatIpList().get(0).getErrMsg());
            return json.toString();
        }
    }

    /**
     * 获取弹性公网ip总价
     *
     * @param request
     * @param cloudOrderFloatIp 弹性公网ip订单对象
     * @return
     */
    @RequestMapping(value = "/getprice", method = RequestMethod.POST)
    @ResponseBody
    public String getPrice(HttpServletRequest request, @RequestBody CloudOrderFloatIp cloudOrderFloatIp) {
        EayunResponseJson json = new EayunResponseJson();
        try {
            BigDecimal price = floatIpService.getPrice(cloudOrderFloatIp);
            json.setRespCode(ConstantClazz.SUCCESS_CODE);
            json.setData(price);
        } catch (Exception e) {
            log.error(e.toString(),e);
            json.setRespCode(ConstantClazz.ERROR_CODE);
            json.setMessage(e.getMessage());
        }
        return JSONObject.toJSONString(json);
    }

    /**
     * 根据订单编号获取弹性公网订单对象
     *
     * @param request
     * @param orderNo
     * @return
     */
    @RequestMapping(value = "/getcloudorderbyorderno", method = RequestMethod.POST)
    @ResponseBody
    public String getCloudOrderByOrderNo(HttpServletRequest request, @RequestBody String orderNo) {
        CloudOrderFloatIp cloudOrderFloatIp = floatIpService.getCloudOrderByOrderNo(orderNo);
        return JSONObject.toJSONString(cloudOrderFloatIp);
    }

    /**
     * 验证指定公网ip是否有续费订单
     *
     * @param request
     * @param floId
     * @return
     */
    @RequestMapping(value = "/checkFloatIpOrderExist", method = RequestMethod.POST)
    @ResponseBody
    public String checkFloatIpOrderExist(HttpServletRequest request, @RequestBody String floId) {
        boolean bool = floatIpService.checkFloatIpOrderExist(floId);
        JSONObject json = new JSONObject();
        json.put("flag", bool);
        return json.toString();
    }

    /**
     * 续费提交订单，校验是否可以创建订单
     *
     * @param request
     * @param map
     * @return
     * @throws Exception
     */
    @RequestMapping(value = "/renewFloatIpOrderConfirm", method = RequestMethod.POST)
    @ResponseBody
    public String renewFloatIpOrderConfirm(HttpServletRequest request, @RequestBody Map<String, String> map) throws Exception {
        log.info("公网ip续费提交订单校验开始");
        SessionUserInfo sessionUser = (SessionUserInfo) request.getSession().getAttribute(ConstantClazz.SYS_SESSION_USERINFO);
        String userId = sessionUser.getUserId();
        String userName = sessionUser.getUserName();
        String cusId = sessionUser.getCusId();
        String opIp = sessionUser.getIP();
    	map.put("operatorIp", opIp);//操作者ip

        JSONObject json = new JSONObject();
        try {
            json = floatIpService.renewFloatIpOrderConfirm(map, userId, userName, cusId);
        } catch (Exception e) {
            throw e;
        }
        return json.toJSONString();
    }

    /**
     * 查询项目可用配额数
     *
     * @param request
     * @param prjId
     * @return
     */
    @RequestMapping(value = "/findfloipsurplus", method = RequestMethod.POST)
    @ResponseBody
    public String findFloIpSurplus(HttpServletRequest request, @RequestBody String prjId) {
        EayunResponseJson json = new EayunResponseJson();
        int ipSur = floatIpService.findFloIpSurplus(prjId);
        json.setRespCode(ConstantClazz.SUCCESS_CODE);
        json.setData(ipSur);
        return JSONObject.toJSONString(json);
    }
    
    @RequestMapping(value = "/getorderfloatipbyorderno", method = RequestMethod.POST)
    @ResponseBody
    public String getOrderFloatIpByOrderNo(HttpServletRequest request, @RequestBody Map<String, String> map) {
        EayunResponseJson json = new EayunResponseJson();
        String orderNo = map.get("orderNo").toString();
        CloudOrderFloatIp cloudOrderFloatIp = floatIpService.getOrderFloatIpByOrderNo(orderNo);
        json.setRespCode(ConstantClazz.SUCCESS_CODE);
        json.setData(cloudOrderFloatIp);
        return JSONObject.toJSONString(json);
    }
    
    @RequestMapping(value = "/checkflowebsite", method = RequestMethod.POST)
    @ResponseBody
    public String checkFloWebSite(HttpServletRequest request, @RequestBody Map map) throws Exception {
    	log.info("检查此公网IP是否已绑定备案服务 已绑定：true 未绑定：false");
    	EayunResponseJson json = new EayunResponseJson();
        String floIp = map.get("floIp").toString();
        try {
            boolean isok = floatIpService.checkFloWebSite(floIp);
            json.setData(isok);
            json.setRespCode(ConstantClazz.SUCCESS_CODE);
        } catch (Exception e) {
        	json.setRespCode(ConstantClazz.ERROR_CODE);
        	log.error(e.toString(),e);
	        throw e;
        }
        return JSONObject.toJSONString(json);
    }
}
