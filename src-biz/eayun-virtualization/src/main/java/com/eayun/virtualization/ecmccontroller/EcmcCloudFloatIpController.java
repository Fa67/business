package com.eayun.virtualization.ecmccontroller;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.alibaba.fastjson.JSONObject;
import com.eayun.common.ConstantClazz;
import com.eayun.common.dao.ParamsMap;
import com.eayun.common.dao.QueryMap;
import com.eayun.common.dao.support.Page;
import com.eayun.common.exception.AppException;
import com.eayun.common.model.EayunResponseJson;
import com.eayun.common.util.BeanUtils;
import com.eayun.log.ecmcsevice.EcmcLogService;
import com.eayun.project.ecmcservice.EcmcProjectService;
import com.eayun.virtualization.ecmcservice.EcmcCloudFloatIPService;
import com.eayun.virtualization.model.BaseCloudFloatIp;
import com.eayun.virtualization.model.BaseCloudNetwork;
import com.eayun.virtualization.model.BaseCloudSubNetWork;
import com.eayun.virtualization.model.CloudFloatIp;
import com.eayun.virtualization.model.CloudProject;

/**
 * @author jingang.liu@eayun.com
 * @Dadte 2016年4月7日
 */
@Controller
@RequestMapping("/ecmc/virtual/floatip")
public class EcmcCloudFloatIpController {

	public final static Logger log = LoggerFactory.getLogger(EcmcCloudFloatIpController.class);
	
	@Autowired
	private EcmcCloudFloatIPService cloudfloatipservice;
    @Autowired
    private EcmcLogService ecmcLogService;
	@Autowired
    private EcmcProjectService projectservice;

	/**
	 * 获取浮动ip列表
	 * @param request
	 * @param datacenterId
	 * @param projectId
	 * @param name
	 * @param page
	 * @return
	 * @throws AppException
	 */
	@RequestMapping("/getFloatIpList")
	@ResponseBody
	public Object list(HttpServletRequest request,Page page,@RequestBody ParamsMap mapparams) throws AppException {
		log.info("获取浮动ip列表");
		String datacenterId = mapparams.getParams().get("datacenterId") == null ? null : mapparams.getParams().get("datacenterId").toString();
		String projectId = mapparams.getParams().get("projectId") ==null ? null : mapparams.getParams().get("projectId").toString();
		String name = mapparams.getParams().get("name") ==null ? null : mapparams.getParams().get("name").toString();
		String ip = mapparams.getParams().get("ip") ==null ? null : mapparams.getParams().get("ip").toString();
		String prjName = mapparams.getParams().get("prjName") ==null ? null : mapparams.getParams().get("prjName").toString();
		String cusName = mapparams.getParams().get("cusName") ==null ? null : mapparams.getParams().get("cusName").toString();
		String[] pns = null;
		if(prjName!=null && !"".equals(prjName)){
			pns = prjName.split(",");
		}
		String[] cns = null;
		if(cusName!=null && !"".equals(cusName)){
			cns = cusName.split(",");
		}
        int pageSize = mapparams.getPageSize();
        int pageNumber = mapparams.getPageNumber();
        QueryMap queryMap=new QueryMap();
        queryMap.setPageNum(pageNumber);
        queryMap.setCURRENT_ROWS_SIZE(pageSize);
		try {
			page = cloudfloatipservice.getFloatlist(name, datacenterId, projectId, page,queryMap,ip,pns,cns);
		} catch (AppException e) {
			log.error(e.toString(),e);
			throw e;
		} catch (Exception e) {
			log.error(e.toString(),e);
		}
		return page;
	}
	/**
     * 浮动IP绑定云主机
     * @param request
     * @param map
     * @return
     * @throws Exception
     */
    @RequestMapping(value = "/binDingVm")
    @ResponseBody
    public Object binDingVm(HttpServletRequest request, @RequestBody CloudFloatIp cloudFloatIp) throws AppException {
        log.info("浮动IP绑定云主机开始");
        EayunResponseJson res = new EayunResponseJson();
        CloudFloatIp cloudFloat = null;
        BaseCloudFloatIp basefloatIp = new BaseCloudFloatIp();
        try {
            basefloatIp = cloudfloatipservice.getById(cloudFloatIp.getFloId());
            if(basefloatIp==null){
            	 res.setRespCode(ConstantClazz.ERROR_CODE);
                 res.setMessage("绑定时浮动IP未找到");
                 return res;
            }
            basefloatIp.setResourceId(cloudFloatIp.getResourceId());
            basefloatIp.setResourceType(cloudFloatIp.getResourceType());
            BeanUtils.copyPropertiesByModel(cloudFloatIp, basefloatIp);
        	if("vm".equals(cloudFloatIp.getResourceType())){
        		cloudFloat = cloudfloatipservice.binDingVmIp(cloudFloatIp);
        		ecmcLogService.addLog("绑定云主机",ConstantClazz.LOG_TYPE_FLOATIP, cloudFloatIp.getFloIp(),cloudFloat.getPrjId(), 1,cloudFloatIp.getFloId(),null);
        	}
        	if("lb".equals(cloudFloatIp.getResourceType())){
        		cloudFloat = cloudfloatipservice.binLb(cloudFloatIp);
        		ecmcLogService.addLog("绑定负载均衡",ConstantClazz.LOG_TYPE_FLOATIP, cloudFloatIp.getFloIp(),cloudFloat.getPrjId(), 1,cloudFloatIp.getFloId(),null);
        	}
            res.setRespCode(ConstantClazz.SUCCESS_CODE);
            res.setData(cloudFloat);
        } catch (AppException e) {
            log.error("绑定资源失败", e);
            if("vm".equals(cloudFloatIp.getResourceType())){
        		ecmcLogService.addLog("绑定云主机",ConstantClazz.LOG_TYPE_FLOATIP, basefloatIp.getFloIp(),basefloatIp.getPrjId(), 0,cloudFloatIp.getFloId(),e);
        	}
        	if("lb".equals(cloudFloatIp.getResourceType())){
        		ecmcLogService.addLog("绑定负载均衡",ConstantClazz.LOG_TYPE_FLOATIP, basefloatIp.getFloIp(),basefloatIp.getPrjId(), 0,cloudFloatIp.getFloId(),e);
        	}

        	throw e;
        } catch (Exception e) {
        	if("vm".equals(cloudFloatIp.getResourceType())){
        		ecmcLogService.addLog("绑定云主机",ConstantClazz.LOG_TYPE_FLOATIP, basefloatIp.getFloIp(),basefloatIp.getPrjId(), 0,cloudFloatIp.getFloId(),e);
        	}
        	if("lb".equals(cloudFloatIp.getResourceType())){
        		ecmcLogService.addLog("绑定负载均衡",ConstantClazz.LOG_TYPE_FLOATIP, basefloatIp.getFloIp(),basefloatIp.getPrjId(), 0,cloudFloatIp.getFloId(),e);
        	}
            log.error("绑定资源失败", e);
            res.setRespCode(ConstantClazz.ERROR_CODE);
            res.setMessage("绑定资源失败:"+e.getMessage());
        }
        return res;
    }
    /**
     * 解绑
     * @param request
     * @param cloudFloatIp
     * @return
     * @throws Exception
     */
    @RequestMapping(value = "/unBinDingVm")
    @ResponseBody
    public Object unBinDingVm(HttpServletRequest request, @RequestBody CloudFloatIp cloudFloatIp) throws AppException {
        log.info("浮动IP解绑开始");
        EayunResponseJson res = new EayunResponseJson();
        CloudFloatIp cloudFloat = null;
        try {
        	 BaseCloudFloatIp basefloatIp = cloudfloatipservice.getById(cloudFloatIp.getFloId());
             if(basefloatIp==null){
            	 res.setRespCode(ConstantClazz.ERROR_CODE);
                 res.setMessage("解绑时浮动IP未找到");
                 return res;
             }
             basefloatIp.setResourceId(cloudFloatIp.getResourceId());
             basefloatIp.setResourceType(cloudFloatIp.getResourceType());
             BeanUtils.copyPropertiesByModel(cloudFloatIp, basefloatIp);
        	if("vm".equals(cloudFloatIp.getResourceType())){
        		cloudFloat = cloudfloatipservice.unBinDingVmIp(cloudFloatIp);
            	ecmcLogService.addLog("解除绑定",ConstantClazz.LOG_TYPE_FLOATIP, cloudFloat.getFloIp(),cloudFloat.getPrjId(), 1,cloudFloatIp.getFloId(),null);
        	}
        	if("lb".equals(cloudFloatIp.getResourceType())){
        		cloudFloatIp.setPortId(null);
        		cloudFloatIp.setResourceId(null);
        		cloudFloatIp.setResourceType(null);
        		cloudFloat = cloudfloatipservice.binLb(cloudFloatIp);
        		ecmcLogService.addLog("解除绑定",ConstantClazz.LOG_TYPE_FLOATIP, cloudFloat.getFloIp(),cloudFloat.getPrjId(), 1,cloudFloatIp.getFloId(),null);
        	}
            res.setRespCode(ConstantClazz.SUCCESS_CODE);
            res.setData(cloudFloat);
        } catch (AppException e) {
            log.error("解绑资源失败", e);
        	ecmcLogService.addLog("解除绑定",ConstantClazz.LOG_TYPE_FLOATIP, cloudFloatIp.getFloIp(),cloudFloatIp.getPrjId(), 0,cloudFloatIp.getFloId(),e);

            throw e;
        } catch (Exception e) {
        	ecmcLogService.addLog("解除绑定",ConstantClazz.LOG_TYPE_FLOATIP, cloudFloatIp.getFloIp(),cloudFloatIp.getPrjId(), 0,cloudFloatIp.getFloId(),e);
            log.error("解绑资源失败", e);
            res.setRespCode(ConstantClazz.ERROR_CODE);
            res.setMessage("解绑资源失败:"+e.getMessage());
        }
        return res;
    }
    
    /**
     * 查询未绑定云主机的浮动IP
     * @param request
     * @param cloudFloatIp
     * @return
     * @throws Exception
     */
    @RequestMapping(value = "/getUnBindFloatIp")
    @ResponseBody
    public Object getUnBindFloatIp(HttpServletRequest request, @RequestBody CloudFloatIp cloudFloatIp) throws AppException {
        log.info("查询未绑定云主机的浮动IP");
        EayunResponseJson res = new EayunResponseJson();
        res.setRespCode(ConstantClazz.SUCCESS_CODE);
        res.setData(cloudfloatipservice.getUnBindFloatIp(cloudFloatIp.getPrjId()));
        return res;
    }
    /**
     * 分配浮动Ip给项目
     * @param request
     * @param datacenter
     * @param project
     * @param network
     * @return
     * @throws Exception 
     */
    @RequestMapping(value = "/allocateIp")
    @ResponseBody
	public Object allocateIp(HttpServletRequest request, @RequestBody Map<String, String> params) throws AppException{
		log.info("分配浮动Ip给项目");
		EayunResponseJson res = new EayunResponseJson();
		try {
			CloudFloatIp floatip = cloudfloatipservice.allocateIp(params.get("datacenter"), params.get("project"));
			res.setRespCode(ConstantClazz.SUCCESS_CODE);
			res.setData(floatip);
		} catch (AppException e) {
			throw e;
		} catch (Exception e) {
		    log.error(e.toString(),e);
			res.setRespCode(ConstantClazz.ERROR_CODE);
			res.setMessage(e.getMessage());
		}
		return res;
	}
    /**
	 * 释放浮动Ip
	 * 
	 * @param request
	 * @param reJson
	 * @return
	 * @throws AppException
	 */
	@RequestMapping("/deallocateFloatIp")
	@ResponseBody
	public Object deallocateFloatIp(@RequestBody Map<String, String> prames) throws AppException {
		EayunResponseJson res = new EayunResponseJson();
		String id = prames.get("id");
		try {
			// 待删除的id非空且执行删除操作成功
			if (id != null && !id.equals("") && cloudfloatipservice.deallocateFloatIp(prames.get("datacenterId"), prames.get("projectId"),id)) {
				// 删除成功标识
				res.setMessage("释放浮动IP成功");
				res.setRespCode(ConstantClazz.SUCCESS_CODE);
	            ecmcLogService.addLog("释放公网IP",ConstantClazz.LOG_TYPE_FLOATIP,prames.get("floIp"),prames.get("projectId"), 1, prames.get("id"),null);

			} else {
				// 删除失败标识
				res.setMessage("释放浮动IP失败");
				res.setRespCode(ConstantClazz.ERROR_CODE);
	          ecmcLogService.addLog("释放公网IP",ConstantClazz.LOG_TYPE_FLOATIP,prames.get("floIp"),prames.get("projectId"), 0, prames.get("id"),null);

			}
		}catch (AppException e) {
	       ecmcLogService.addLog("释放公网IP",ConstantClazz.LOG_TYPE_FLOATIP,prames.get("floIp"),prames.get("projectId"), 0, prames.get("id"),e);
        	throw e;
        } catch (Exception e) {
	          ecmcLogService.addLog("释放公网IP",ConstantClazz.LOG_TYPE_FLOATIP,prames.get("floIp"),prames.get("projectId"), 0, prames.get("id"),e);
		}
		
		return res;
	}
    /**
     * 批量分配浮动Ip给项目
     * @param num
     * @param datacenter
     * @param project
     * @param network
     * @return
     */
    @RequestMapping(value = "/allocateIptonum")
    @ResponseBody
	public Object allocateIp(@RequestBody Map<String, String> params) throws AppException{
		log.info("获取浮动ip列表");
		EayunResponseJson res = new EayunResponseJson();
		int num = Integer.parseInt(params.get("num"));
		List<CloudFloatIp> list = new ArrayList<CloudFloatIp>();
		CloudFloatIp floatip = new CloudFloatIp();
		try{
			for(int i=0;i<num;i++){
				floatip = cloudfloatipservice.allocateIp(params.get("datacenter"), params.get("project"));
				list.add(floatip);
	            ecmcLogService.addLog("分配公网IP",ConstantClazz.LOG_TYPE_FLOATIP,floatip.getFloIp(), floatip.getPrjId(), 1, floatip.getFloId(),null);
			}
			res.setRespCode(ConstantClazz.SUCCESS_CODE);
			res.setData(list);

		} catch (AppException e) {
            ecmcLogService.addLog("分配公网IP",ConstantClazz.LOG_TYPE_FLOATIP,floatip.getFloIp(), floatip.getPrjId(), 0, floatip.getFloId(),e);
			throw e;
		} catch (Exception e) {
            ecmcLogService.addLog("分配公网IP",ConstantClazz.LOG_TYPE_FLOATIP,floatip.getFloIp(), floatip.getPrjId(), 0, floatip.getFloId(),e);
			res.setRespCode(ConstantClazz.ERROR_CODE);
			res.setMessage(e.getMessage());
		}
		return res;
	}
    
    /**
     * 检查外部网络的可用公网IP数量是否小于填写数量
     * @param params
     * @return
     * @throws Exception 
     */
    @RequestMapping(value = "/checkeIpNow")
    @ResponseBody
    public Object checkeIpNow(@RequestBody Map<String, String> params) throws Exception{
    	log.info("检查外部网络的可用公网IP数量是否小于填写数量");
    	EayunResponseJson res = new EayunResponseJson();
		int remainnum = cloudfloatipservice.getCountByPro(params.get("prjId"));//查询已经使用的IP数
		CloudProject cp = (CloudProject)projectservice.getProjectById(params.get("prjId"));//查询项目IP配额数
		String input = params.get("ipnum");
		if(cp!=null && !"".equals(input)){
			if((cp.getOuterIP()-remainnum)>=Long.parseLong(params.get("ipnum"))){//外部网络的可用公网IP数量小于填写数量大于填写数量
				res.setRespCode(ConstantClazz.SUCCESS_CODE);
				res.setData(true);
			}else{
				res.setRespCode(ConstantClazz.SUCCESS_CODE);
				res.setData(false);
			}
		}else{
			res.setRespCode(ConstantClazz.ERROR_CODE);
			res.setMessage("项目不存在，请核定prjId");
		}
		return res;
    }
    /**
     * 查询剩余可用IP数
     * @param prjId
     * @return
     * @throws Exception 
     */
    @RequestMapping(value = "/remainnum")
    @ResponseBody
    public Object remainnum(@RequestBody Map<String, String> mapprames) throws Exception{
    	log.info("查询剩余可用IP数");
    	EayunResponseJson res = new EayunResponseJson();
    	int remainnum = cloudfloatipservice.getCountByPro(mapprames.get("prjId"));//查询已经使用的IP数
    	CloudProject cp = (CloudProject)projectservice.getProjectById(mapprames.get("prjId"));//查询项目IP配额数
		if(cp!=null){
			res.setRespCode(ConstantClazz.SUCCESS_CODE);
			res.setData(cp.getOuterIP()-remainnum);
		}else{
			res.setRespCode(ConstantClazz.ERROR_CODE);
			res.setMessage("项目不存在，请核定prjId");
		}
		return res;
    }
    /**
     * 查询未绑定浮动ip的云主机列表
     * @param prames
     * @return
     * @throws Exception
     */
    @RequestMapping(value = "/getVmBySubNetWork")
    @ResponseBody
    public Object getVmBySubNetWork(@RequestBody Map<String, String> prames) throws AppException{
    	EayunResponseJson res = new EayunResponseJson();
    	res.setRespCode(ConstantClazz.SUCCESS_CODE);
		res.setData(cloudfloatipservice.getVmBySubNetWork(prames.get("subnetworkId")));
    	return res;
    }

	@RequestMapping(value = "/getNetworkByPrj" , method = RequestMethod.POST)
	@ResponseBody
	public String getNetworkByPrj(HttpServletRequest request, @RequestBody Map map) throws Exception {
		log.info("查询项目下的网络列表");
		List<BaseCloudNetwork> netList;
		try {
			String prjId = map.get("prjId").toString();
			netList = cloudfloatipservice.getNetworkByPrj(prjId);
		} catch (Exception e) {
			log.error("查询项目下的网络列表失败", e);
			throw e;
		}
		return JSONObject.toJSONString(netList);
	}

	@RequestMapping(value = "/getSubnetByNetwork" , method = RequestMethod.POST)
	@ResponseBody
	public String getSubnetByNetId(HttpServletRequest request, @RequestBody Map map) throws Exception {
		log.info("查询网络下的子网列表");
		List<BaseCloudSubNetWork> netList;
		try {
			String netId = map.get("netId").toString();
			netList = cloudfloatipservice.getSubnetByNetId(netId);
		} catch (Exception e) {
			log.error("查询网络下的子网列表失败", e);
			throw e;
		}
		return JSONObject.toJSONString(netList);
	}

	/**
	 * 绑定
	 * @param request
	 * @param map
	 * @return
	 * @throws Exception
	 */
	@RequestMapping(value = "/bindResource" , method = RequestMethod.POST)
	@ResponseBody
	public String bindResource(HttpServletRequest request, @RequestBody CloudFloatIp cloudFloatIp) throws Exception {
		log.info("浮动IP绑定资源开始");
		JSONObject json = new JSONObject ();
		CloudFloatIp cloudFloat = null;
		try {
			cloudFloat = cloudfloatipservice.bindResource(cloudFloatIp);
			json.put("data", cloudFloat);
			json.put("respCode", ConstantClazz.SUCCESS_CODE);
            ecmcLogService.addLog("绑定公网IP",ConstantClazz.LOG_TYPE_FLOATIP,cloudFloatIp.getFloIp(), cloudFloatIp.getPrjId(), 1, cloudFloatIp.getFloId(),null);
		} catch (Exception e) {
            json.put("respCode", ConstantClazz.ERROR_CODE);
            ecmcLogService.addLog("绑定公网IP",ConstantClazz.LOG_TYPE_FLOATIP,cloudFloatIp.getFloIp(), cloudFloatIp.getPrjId(), 0, cloudFloatIp.getFloId(),e);
            throw e;
		}
		return json.toJSONString();
	}

	/**
	 * 解绑
	 * @param request
	 * @param cloudFloatIp
	 * @return
	 * @throws Exception
	 */
	@RequestMapping(value = "/unbundingResource" , method = RequestMethod.POST)
	@ResponseBody
	public String unbundingResource(HttpServletRequest request, @RequestBody CloudFloatIp cloudFloatIp) throws Exception {
		log.info("浮动IP解绑开始");
		JSONObject json = new JSONObject ();
		CloudFloatIp cloudFloat = null;
		try {
			cloudFloat = cloudfloatipservice.unbundingResource(cloudFloatIp);
			json.put("data", cloudFloat);
			json.put("respCode", ConstantClazz.SUCCESS_CODE);
            ecmcLogService.addLog("解除绑定",ConstantClazz.LOG_TYPE_FLOATIP,cloudFloatIp.getFloIp(), cloudFloatIp.getPrjId(), 1, cloudFloatIp.getFloId(),null);
		} catch (Exception e) {
			json.put("respCode", ConstantClazz.ERROR_CODE);
            ecmcLogService.addLog("解除绑定",ConstantClazz.LOG_TYPE_FLOATIP,cloudFloatIp.getFloIp(), cloudFloatIp.getPrjId(), 0, cloudFloatIp.getFloId(),e);
			throw e;
		}
		return json.toJSONString();
	}
    
	@RequestMapping(value = "/checkflowebsite", method = RequestMethod.POST)
    @ResponseBody
    public String checkFloWebSite(HttpServletRequest request, @RequestBody Map map) throws Exception {
		log.info("检查此公网IP是否已绑定备案服务 已绑定：true 未绑定：false");
    	EayunResponseJson json = new EayunResponseJson();
        String floIp = map.get("floIp").toString();
        try {
            boolean isok = cloudfloatipservice.checkFloWebSite(floIp);
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
