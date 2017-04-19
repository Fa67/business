package com.eayun.virtualization.ecmccontroller;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import com.eayun.common.dao.QueryMap;
import com.eayun.common.dao.support.Page;
import com.eayun.common.filter.SessionUserInfo;
import com.eayun.ecmcuser.model.BaseEcmcSysUser;
import com.eayun.ecmcuser.util.EcmcSessionUtil;
import com.eayun.log.ecmcsevice.EcmcLogService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.alibaba.fastjson.JSONObject;
import com.eayun.common.ConstantClazz;
import com.eayun.common.dao.ParamsMap;
import com.eayun.common.exception.AppException;
import com.eayun.common.model.EayunResponseJson;
import com.eayun.common.util.BeanUtils;
import com.eayun.virtualization.ecmcservice.EcmcLBPoolService;
import com.eayun.virtualization.ecmcvo.CloudLdpoolVoe;
import com.eayun.virtualization.model.BaseCloudLdPool;
import com.eayun.virtualization.model.CloudLdPool;

/**
 * ecmc 负载均衡 池
 */
@Controller
@RequestMapping("/ecmc/virtual/loadbalance/pool")
@Scope("prototype")
public class EcmcLBPoolController {
	private static final Log log = LogFactory.getLog(EcmcLBPoolController.class);
	
	@Autowired
	private EcmcLBPoolService ecmcLBPoolService;

    @Autowired
    private EcmcLogService ecmcLogService;
	
	/**
	 * 查询所有资源池的信息
	 * @param request
	 * 
	 * @return
	 * @throws AppException
	 */
	@RequestMapping("/querypool")
	@ResponseBody
	public Object queryPool(HttpServletRequest request, @RequestBody ParamsMap map) throws Exception{
		log.info("查询负载均衡器的列表");
		String dcId = "";
		String poolName="";
		String cusOrg = "";
		String prjName = "";
		Page page  = null;
		try {
			if(map.getParams().containsKey("dcId")){
				dcId=map.getParams().get("dcId").toString();
			}
			if(map.getParams().containsKey("poolName")){
				poolName=map.getParams().get("poolName").toString();
			}
			if(map.getParams().containsKey("cusOrg")) {
				cusOrg = map.getParams().get("cusOrg").toString();
			}
			if(map.getParams().containsKey("prjName")){
				prjName=map.getParams().get("prjName").toString();
			}
			int pageSize = map.getPageSize();
			int pageNumber = map.getPageNumber();
			QueryMap queryMap = new QueryMap();
			queryMap.setPageNum(pageNumber);
			queryMap.setCURRENT_ROWS_SIZE(pageSize);

			page  = ecmcLBPoolService.getPoolList(dcId,poolName,cusOrg,prjName,queryMap);
		}catch (Exception e) {
			throw e;
		}
		return JSONObject.toJSONString(page);
	}

	@RequestMapping(value= "/createBalancer" , method = RequestMethod.POST)
	@ResponseBody
	public String createBalancer(HttpServletRequest request,@RequestBody Map map) throws Exception {
		log.info("ECMC创建负载均衡器开始");
		JSONObject json = new JSONObject ();;
		CloudLdPool cloudLdPool = null;
		SessionUserInfo sessionUser = null;
		CloudLdPool pool = new CloudLdPool();
        String dcId = map.get("dcId")==null?"":map.get("dcId").toString();
        String prjId =  map.get("prjId")==null?"":map.get("prjId").toString();
        String lbMethod =  map.get("lbMethod")==null?"":map.get("lbMethod").toString();
        String limitNum =  map.get("limitNum")==null?"":map.get("limitNum").toString();
        String poolName =  map.get("poolName")==null?"":map.get("poolName").toString();
        String poolProtocol =  map.get("poolProtocol")==null?"":map.get("poolProtocol").toString();
        String port =  map.get("port")==null?"":map.get("port").toString();
        String subnetId =  map.get("subnetId")==null?"":map.get("subnetId").toString();
        try{

			pool.setDcId(dcId);
			pool.setPrjId(prjId);
			pool.setLbMethod(lbMethod);
			pool.setConnectionLimit(Long.valueOf(limitNum));
			pool.setPoolName(poolName);
			pool.setPoolProtocol(poolProtocol);
			pool.setVipPort(Long.valueOf(port));
			pool.setSubnetId(subnetId);

			cloudLdPool = ecmcLBPoolService.createBalancer(pool);
			json.put("data",cloudLdPool);
			json.put("respCode",ConstantClazz.SUCCESS_CODE);
            ecmcLogService.addLog("创建负载均衡器", ConstantClazz.LOG_TYPE_POOL, poolName, prjId, 1, cloudLdPool.getPoolId(), null);
		}catch(Exception e){
			json.put("respCode",ConstantClazz.ERROR_CODE);
            ecmcLogService.addLog("创建负载均衡器", ConstantClazz.LOG_TYPE_POOL, poolName, prjId, 0, "----", e);
			throw e;
		}
		return json.toJSONString();
	}

	/**
	 * 修改负载均衡器
	 *
	 * @author zhouhaitao
	 * @param request
	 * @param pool
	 * @return
	 */
	@RequestMapping(value= "/updateBalancer" , method = RequestMethod.POST)
	@ResponseBody
	public String updateBalancer(HttpServletRequest request,@RequestBody CloudLdPool pool) throws Exception {
		log.info("ECMC编辑负载均衡器开始");
		JSONObject json = new JSONObject ();;
		CloudLdPool cloudLdPool = null;
		try{
			cloudLdPool = ecmcLBPoolService.updateBalancer(pool);
			json.put("data",cloudLdPool);
			json.put("respCode",ConstantClazz.SUCCESS_CODE);
            ecmcLogService.addLog("编辑负载均衡器", ConstantClazz.LOG_TYPE_POOL, cloudLdPool.getPoolName(), cloudLdPool.getPrjId(), 1, cloudLdPool.getPoolId(), null);
		}catch(Exception e){
            json.put("respCode",ConstantClazz.ERROR_CODE);
            if(cloudLdPool!=null){
                ecmcLogService.addLog("编辑负载均衡器", ConstantClazz.LOG_TYPE_POOL, cloudLdPool.getPoolName(), cloudLdPool.getPrjId(), 0, cloudLdPool.getPoolId(), e);
            }
            throw e;
		}

		return json.toJSONString();
	}

	/**
	 * 绑定监控功能
	 * 
	 * @return
	 * @throws AppException
	 */
	@RequestMapping("/bindhealthmonitor")
	@ResponseBody
	public Object bindHealthMonitor(HttpServletRequest request, @RequestBody Map<String, String> params) throws AppException{
		log.info("ECMC绑定健康检查开始");
		String poolId = params.get("poolId");
		String healthMonitorId = params.get("healthMonitorId");
		
		EayunResponseJson resp = new EayunResponseJson();
		try {
			boolean result  = ecmcLBPoolService.bindHealthMonitor(poolId, healthMonitorId);
			if (result) {
				resp.setRespCode(ConstantClazz.SUCCESS_CODE);
				resp.setData(ecmcLBPoolService.getById(poolId));
			} else {
				resp.setRespCode(ConstantClazz.ERROR_CODE);
			}
		}catch (AppException e) {
			throw e;
		} catch (Exception e) {
			log.error(e, e);
			throw new AppException("error.globe.system", e);
		}
		return resp;
	}

	/**
	 * 删除负载均衡器的信息
	 *
	 * @author zhouhaitao
	 * @param request
	 * @param pool
	 * @return
	 * @throws AppException
	 */
	@RequestMapping(value = "/deleteBalancer" , method = RequestMethod.POST)
	@ResponseBody
	public String deleteBalancer(HttpServletRequest request,@RequestBody CloudLdPool pool) throws Exception {
		log.info("ECMC删除负载均衡器开始");
		JSONObject json = new JSONObject();
		try {
			ecmcLBPoolService.deleteBalancer(pool);
			json.put("respCode", ConstantClazz.SUCCESS_CODE);
            ecmcLogService.addLog("删除负载均衡器", ConstantClazz.LOG_TYPE_POOL, pool.getPoolName(), pool.getPrjId(), 1, pool.getPoolId(), null);
		}catch (Exception e) {
			json.put("respCode",ConstantClazz.ERROR_CODE);
            ecmcLogService.addLog("删除负载均衡器", ConstantClazz.LOG_TYPE_POOL, pool.getPoolName(), pool.getPrjId(), 0, pool.getPoolId(), e);
			throw e;
		}
		return json.toJSONString();
	}
	
	
	/**
	 * 查询指定id的资源池的信息
	 * @param request
	 * @return
	 * @throws AppException
	 */
	@RequestMapping("/getpoolbyid")
	@ResponseBody
	public Object getPoolById(HttpServletRequest request, @RequestBody String poolId) throws AppException{
		CloudLdpoolVoe result=null;
		try {
			result=ecmcLBPoolService.getById(poolId);
			log.info("返回结果："+JSONObject.toJSON(result).toString());
		}catch (AppException e) {
			throw e;
		}catch (Exception e) {
			log.error(e, e);
			throw new AppException("error.globe.system", e);
		}finally{
			
		}
		return result;
	}

    /**
     * 查询负载均衡器的详情
     *
     * @author zhouhaitao
     * @param poolId
     * @return
     * @throws AppException
     */
    @RequestMapping(value = "/getLoadBalanceById" , method = RequestMethod.POST)
    @ResponseBody
    public String getLoadBalanceById(HttpServletRequest request, @RequestBody Map map)throws Exception{
        log.info("查询负载均衡器的详情");
        JSONObject json = new JSONObject();
        CloudLdPool pool = null ;
        String poolId = map.get("poolId")==null?"":map.get("poolId").toString();
        try{
            pool  =ecmcLBPoolService.getLoadBalanceById(poolId);
            json.put("data", pool);
        }catch(Exception e){
            throw e;
        }
        return json.toJSONString();
    }
	
	/**
	 * 创建资源池的信息
	 * @param request
	 * @return
	 * @throws AppException
	 */
	@RequestMapping("/createpool")
	@ResponseBody
	public Object createPool(HttpServletRequest request, @RequestBody BaseCloudLdPool pool) throws AppException{
		EayunResponseJson respJson = new EayunResponseJson();
		try {
			CloudLdpoolVoe result = ecmcLBPoolService.createPool(pool); 
			if (result!=null) {
				respJson.setRespCode(ConstantClazz.SUCCESS_CODE);
				respJson.setData(result);
			}
		}catch (AppException e) {
			if(null!=e.getArgsMessage()&&e.getArgsMessage().length>0){
				throw e;
			}else if(null!=e.getErrorMessage()){
				respJson.setRespCode(ConstantClazz.ERROR_CODE);
				respJson.setMessage("该资源池在当前数据中心已存在!");
			}
		} catch (Exception e) {
			log.error(e, e);
			throw new AppException("error.globe.system", e);
		}
		return respJson;
	}
	
	
	/**
	 * 删除资源池的信息
	 * @param request
	 * @return
	 * @throws AppException
	 */
	@RequestMapping("/deletepool")
	@ResponseBody
	public Object deletepool(HttpServletRequest request, @RequestBody Map<String, String> params) throws AppException{
		log.info("删除资源池开始");
		EayunResponseJson respJSON = new EayunResponseJson();
		try {
			boolean isDeleted = ecmcLBPoolService.delete(params.get("poolId"));
			respJSON.setRespCode(isDeleted ? ConstantClazz.SUCCESS_CODE : ConstantClazz.ERROR_CODE);
		}catch (AppException e) {
			throw e;
		} catch (Exception e) {
			log.error(e, e);
			throw new AppException("error.globe.system", e);
		}
		return respJSON;
	}
	
	
	/**
	 * 修改资源池的信息
	 * @param request
	 * @return
	 * @throws AppException
	 */
	@RequestMapping("/updatepool")
	@ResponseBody
	public Object updatePool(HttpServletRequest request, @RequestBody Map<String, Object> map,CloudLdPool pool) throws AppException{
		EayunResponseJson respJson = new EayunResponseJson();
		try {
			BeanUtils.mapToBean(pool, map);
			CloudLdpoolVoe result= ecmcLBPoolService.update(pool);
			if (result!=null) {
				respJson.setRespCode(ConstantClazz.SUCCESS_CODE);
				respJson.setData(result);
			}else{
				respJson.setRespCode(ConstantClazz.ERROR_CODE);
			}
		}catch (AppException e) {
			if(null!=e.getArgsMessage()&&e.getArgsMessage().length>0){
				throw e;
			}else if(null!=e.getErrorMessage()){
				respJson.setRespCode(ConstantClazz.ERROR_CODE);
				respJson.setMessage("该资源池在当前数据中心已存在!");
			}
		} catch (Exception e) {
		    log.error(e.toString(),e);
			throw new AppException("error.globe.system", e);
		}

		return respJson;
	}
	
	/**
	 * 绑定公网IP
	 * @param params
	 * @return
	 * @throws AppException
	 */
	@RequestMapping("/bindfloatip")
	@ResponseBody
	public Object bindFloatIp(@RequestBody Map<String, String> params) throws AppException{
		EayunResponseJson responseJson = new EayunResponseJson();
		responseJson.setData(ecmcLBPoolService.bindFloatIp(params.get("poolId"), params.get("floatId"), params.get("vipId")));
		responseJson.setRespCode(ConstantClazz.SUCCESS_CODE);
		return responseJson;
	}
	
	/**
	 * 解绑公网IP
	 * @param params
	 * @return
	 * @throws AppException
	 */
	@RequestMapping("/unbindfloatip")
	@ResponseBody
	public Object unbindFloatIp(@RequestBody Map<String, String> params) throws AppException{
		EayunResponseJson responseJson = new EayunResponseJson();
		responseJson.setData(ecmcLBPoolService.unbindFloatIp(params.get("poolId"), params.get("floatId")));
		responseJson.setRespCode(ConstantClazz.SUCCESS_CODE);
		return responseJson;
	}
	
	/**
	 * 校验资源池名称是否重复
	 * @param params
	 * @return
	 * @throws AppException
	 */
	@RequestMapping("/checkpoolname")
	@ResponseBody
	public Object checkPoolName(@RequestBody Map<String, String> params) throws AppException{
		try {
			EayunResponseJson resultJson = new EayunResponseJson();
			resultJson.setData(ecmcLBPoolService.checkPoolName(params.get("prjId"), params.get("poolName"), params.get("poolId")));
			resultJson.setRespCode(ConstantClazz.SUCCESS_CODE);
			return resultJson;
		} catch (Exception e) {
			throw new AppException("error.globe.system", e);
		}
	}
	
	/**
	 * 子网下未绑定floatIp的资源池
	 * @param params
	 * @return
	 * @throws AppException
	 */
	@RequestMapping("/getnotbindfloatippools")
	@ResponseBody
	public Object getNotbindFloatIpPools(@RequestBody Map<String, String> params) throws AppException{
		EayunResponseJson resultJson = new EayunResponseJson();
		resultJson.setData(ecmcLBPoolService.getNotbindFloatIpPools(params.get("subnetId")));
		resultJson.setRespCode(ConstantClazz.SUCCESS_CODE);
		return resultJson;
	}
	
	/**
	 * 查询项目下资源池
	 * @param params
	 * @return
	 * @throws AppException
	 */
	@RequestMapping("/getpoollistbyprjid")
	@ResponseBody
	public Object getPoolListByPrjId(@RequestBody Map<String, String> params) throws AppException{
		EayunResponseJson resultJson = new EayunResponseJson();
		resultJson.setData(ecmcLBPoolService.getPoolList(params.get("dcId"), params.get("prjId")));
		resultJson.setRespCode(ConstantClazz.SUCCESS_CODE);
		return resultJson;
	}
}
