package com.eayun.database.instance.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.eayun.common.ConstantClazz;
import com.eayun.common.controller.BaseController;
import com.eayun.common.dao.ParamsMap;
import com.eayun.common.dao.QueryMap;
import com.eayun.common.dao.support.Page;
import com.eayun.common.exception.AppException;
import com.eayun.common.filter.SessionUserInfo;
import com.eayun.common.model.EayunResponseJson;
import com.eayun.database.instance.model.CloudOrderRDSInstance;
import com.eayun.database.instance.model.CloudRDSInstance;
import com.eayun.database.instance.service.RDSInstanceService;
import com.eayun.log.service.LogService;
import com.eayun.price.bean.ParamBean;
import com.eayun.price.bean.PriceDetails;
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

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;

/**
 * 云数据库实例Controller
 *                       
 * @Filename: RDSInstanceController.java
 * @Description: 
 * @Version: 1.0
 * @Author: LiuZhuangzhuang
 * @Email: zhuangzhuang.liu@eayun.com
 * @History:<br>
 *<li>Date: 2017年2月22日</li>
 *<li>Version: 1.0</li>
 *<li>Content: create</li>
 *
 */
@Controller
@RequestMapping("/rds/instance")
@Scope("prototype")
public class RDSInstanceController extends BaseController {
	
	private static final Logger log = LoggerFactory.getLogger(RDSInstanceController.class);
    @Autowired
    private RDSInstanceService rdsInstanceService;
    @Autowired
    private LogService logService;
    
    /**
     * 获取当前客户下的所有数据库实例
     * @param request
     * @param page
     * @param map -- 业务请求参数
     * @return
     * @throws Exception
     */
    @RequestMapping(value= "/getList" , method = RequestMethod.POST)
    @ResponseBody
    public String getList(HttpServletRequest request,Page page,@RequestBody ParamsMap map) throws Exception{
    	log.info("查询当前项目下的数据库实例");
    	try {
    		SessionUserInfo sessionUser = (SessionUserInfo) request.getSession().getAttribute(ConstantClazz.SYS_SESSION_USERINFO);
    		int pageSize = map.getPageSize();
    		int pageNumber = map.getPageNumber();
    		
    		QueryMap queryMap=new QueryMap();
    		queryMap.setPageNum(pageNumber);
    		queryMap.setCURRENT_ROWS_SIZE(pageSize);
    		
    		page = rdsInstanceService.getList(page,map,sessionUser,queryMap);
    	} catch (Exception e) {
    		throw e;
    	}
    	return JSONObject.toJSONString(page);
    }
    
    /**
     * 根据数据库实例ID获取详情
     * @param request
     * @param rdsId -- 数据库实例ID
     * @return
     * @throws Exception
     */
    @RequestMapping(value="/getRdsById")
    @ResponseBody
    public String getRdsById(HttpServletRequest request, @RequestBody String rdsId) throws Exception {
    	JSONObject json  = new JSONObject();
    	log.info("获取数据库实例详情");
    	CloudRDSInstance rds=null;
    	try{
    		rds = rdsInstanceService.getRdsById(rdsId);
    		if(null != rds){
    			json.put("data", rds);
    			json.put("respCode", ConstantClazz.SUCCESS_CODE);
    		}else{
    			json.put("respCode", ConstantClazz.ERROR_CODE);
    		}
    	}catch(Exception e){
        	json.put("respCode", ConstantClazz.ERROR_CODE);
        	log.error(e.getMessage(),e);
        	throw e;
        }
        return json.toJSONString();
    }
    
    @RequestMapping(value="/checkRdsNameExist")
    @ResponseBody
    public String checkRdsNameExist(HttpServletRequest request,@RequestBody Map<String,String> map){
    	boolean bool = rdsInstanceService.checkRdsNameExist(map.get("rdsId"),map.get("rdsName"),map.get("prjId"));
        return JSONObject.toJSONString(bool);
    }
	
    @RequestMapping(value="/modifyRdsInstance")
    @ResponseBody
    public String modifyRdsInstance(HttpServletRequest request,@RequestBody CloudRDSInstance cloudRdsInstance){
    	log.info("编辑云数据库");
    	JSONObject json = new JSONObject ();
        try{
        	rdsInstanceService.modifyRdsInstance(cloudRdsInstance);
        	
        	logService.addLog("编辑云数据库",  
        			ConstantClazz.LOG_TYPE_RDS, 
        			cloudRdsInstance.getRdsName(),
        			cloudRdsInstance.getPrjId(),  
        			ConstantClazz.LOG_STATU_SUCCESS,null);
        	json.put("data", cloudRdsInstance);
        	json.put("respCode", ConstantClazz.SUCCESS_CODE);
        }catch(AppException e){
        	logService.addLog("编辑云数据库",  
        			ConstantClazz.LOG_TYPE_HOST, 
        			cloudRdsInstance.getRdsName(), 
        			cloudRdsInstance.getPrjId(), 
        			ConstantClazz.LOG_STATU_ERROR,e);
        	json.put("respCode", ConstantClazz.ERROR_CODE);
        	log.error(e.getMessage(),e);
        	throw e;
        }
        catch(Exception e){
        	logService.addLog("编辑云数据库",  
        			ConstantClazz.LOG_TYPE_HOST, 
        			cloudRdsInstance.getRdsName(), 
        			cloudRdsInstance.getPrjId(),  
        			ConstantClazz.LOG_STATU_ERROR,e);
        	json.put("respCode", ConstantClazz.ERROR_CODE);
        	log.error(e.getMessage(),e);
        	throw e;
        }
		return json.toJSONString();
    }
    
    @RequestMapping(value = "/restart" , method = RequestMethod.POST)
    @ResponseBody
    public String  restart(HttpServletRequest request,@RequestBody CloudRDSInstance cloudRdsInstance){
    	log.info("重启数据库实例");
    	JSONObject json = new JSONObject ();
    	try{
    		rdsInstanceService.restart(cloudRdsInstance);
        	logService.addLog("重启",  ConstantClazz.LOG_TYPE_RDS, cloudRdsInstance.getRdsName(), cloudRdsInstance.getPrjId(),  ConstantClazz.LOG_STATU_SUCCESS,null);
        	json.put("respCode", ConstantClazz.SUCCESS_CODE);
        }catch(AppException e){
            log.error(e.getMessage(),e);
            logService.addLog("重启",  ConstantClazz.LOG_TYPE_RDS, cloudRdsInstance.getRdsName(), cloudRdsInstance.getPrjId(),  ConstantClazz.LOG_STATU_ERROR,e);
        	json.put("respCode", ConstantClazz.ERROR_CODE);
        	throw e;
        }
    	catch(Exception e){
        	logService.addLog("重启",  ConstantClazz.LOG_TYPE_RDS, cloudRdsInstance.getRdsName(), cloudRdsInstance.getPrjId(),  ConstantClazz.LOG_STATU_ERROR,e);
        	json.put("respCode", ConstantClazz.ERROR_CODE);
        	log.error(e.getMessage(),e);
        	throw e;
        }
		return json.toJSONString();
    }
    
    /**
     * 删除数据库实例
     * @param request
     * @param cloudRdsInstance
     * @return
     * @throws Exception
     */
    @RequestMapping(value="/deleteRdsInstance")
    @ResponseBody
    public String deleteRdsInstance(HttpServletRequest request,@RequestBody CloudRDSInstance cloudRdsInstance) 
    		throws Exception{
    	log.info("删除数据库实例");
    	SessionUserInfo sessionUser = (SessionUserInfo) request.getSession().getAttribute(ConstantClazz.SYS_SESSION_USERINFO);
    	EayunResponseJson responseJson = new EayunResponseJson();
    	try{
    		responseJson = rdsInstanceService.deleteRdsInstance(cloudRdsInstance, sessionUser);
    		if(null == responseJson){
    			responseJson = new EayunResponseJson();
    			responseJson.setRespCode(ConstantClazz.SUCCESS_CODE);
    			logService.addLog("删除",
    					ConstantClazz.LOG_TYPE_RDS, 
    					cloudRdsInstance.getRdsName(), 
    					cloudRdsInstance.getPrjId(),  
    					ConstantClazz.LOG_STATU_SUCCESS,null);
    		}else{
				logService.addLog("删除",
						ConstantClazz.LOG_TYPE_RDS,
						cloudRdsInstance.getRdsName(),
						cloudRdsInstance.getPrjId(),
						ConstantClazz.LOG_STATU_ERROR,null);
			}
    	}catch(AppException e){
    		logService.addLog("删除",
    				ConstantClazz.LOG_TYPE_RDS, 
    				cloudRdsInstance.getRdsName(), 
    				cloudRdsInstance.getPrjId(),  
    				ConstantClazz.LOG_STATU_ERROR,e);
    		throw e;
    	}catch(Exception e){
    		logService.addLog("删除数据库实例",  
    				ConstantClazz.LOG_TYPE_RDS, 
    				cloudRdsInstance.getRdsName(), 
    				cloudRdsInstance.getPrjId(),  
    				ConstantClazz.LOG_STATU_ERROR,e);
    		log.error(e.getMessage(),e);
    		throw e;
    	}
    	return JSONObject.toJSONString(responseJson);
    }
    
    @RequestMapping(value="/detachReplica")
    @ResponseBody
    public String detachReplica(HttpServletRequest request,@RequestBody CloudRDSInstance cloudRdsInstance) {
    	log.info("从库升级为主库");
    	JSONObject json = new JSONObject ();
    	try{
    		rdsInstanceService.detachReplica(cloudRdsInstance);
    		logService.addLog("升为主库",
    				ConstantClazz.LOG_TYPE_RDS, 
    				cloudRdsInstance.getRdsName(), 
    				cloudRdsInstance.getPrjId(),  
    				ConstantClazz.LOG_STATU_SUCCESS,null);
    		json.put("respCode", ConstantClazz.SUCCESS_CODE);
    	}catch(AppException e){
    		logService.addLog("升为主库",
    				ConstantClazz.LOG_TYPE_RDS, 
    				cloudRdsInstance.getRdsName(), 
    				cloudRdsInstance.getPrjId(),  
    				ConstantClazz.LOG_STATU_ERROR,e);
    		json.put("respCode", ConstantClazz.ERROR_CODE);
    		throw e;
    	}catch(Exception e){
    		logService.addLog("升为主库",
    				ConstantClazz.LOG_TYPE_RDS, 
    				cloudRdsInstance.getRdsName(), 
    				cloudRdsInstance.getPrjId(),  
    				ConstantClazz.LOG_STATU_ERROR,e);
    		json.put("respCode", ConstantClazz.ERROR_CODE);
    		log.error(e.getMessage(),e);
    		throw e;
    	}
    	return json.toJSONString();
    }
    
    /**
     * 获取价格
     * @param request
     * @param paramBean
     * @return
     */
    @RequestMapping(value="/getPriceDetails")
    @ResponseBody
    public String getPriceDetails(HttpServletRequest request, @RequestBody ParamBean paramBean){
    	log.info("获取数据库实例的价格");
    	 EayunResponseJson json = new EayunResponseJson();
         try {
        	 PriceDetails priceDetails = rdsInstanceService.getPriceDetails(paramBean);
             json.setRespCode(ConstantClazz.SUCCESS_CODE);
             json.setData(priceDetails);
         } catch (Exception e) {
             log.error(e.toString(),e);
             json.setRespCode(ConstantClazz.ERROR_CODE);
             json.setMessage(e.getMessage());
         }
         return JSON.toJSONString(json);
    }
    
    /**
     * 获取版本列表
     * @param request
     * @return
     */
    @RequestMapping(value="/getVersionList")
    @ResponseBody
    public String getVersionList(HttpServletRequest request, @RequestBody String dcId){
    	log.info("获取数据库版本列表");
    	List<Map<String, String>> versionList = null;
    	EayunResponseJson json = new EayunResponseJson();
    	try{
    		versionList = rdsInstanceService.getVersionList(dcId);
    		if(versionList != null){
    			json.setRespCode(ConstantClazz.SUCCESS_CODE);
    			json.setData(versionList);
    		}
    	}catch (Exception e) {
            log.error(e.toString(),e);
            json.setRespCode(ConstantClazz.ERROR_CODE);
            json.setMessage(e.getMessage());
        }
    	return JSON.toJSONString(json);
    }
    
    /**
     * 
     * @param request
     * @param map
     * @return
     */
    @RequestMapping(value="/getConfigList")
    @ResponseBody
    public String getConfigList(HttpServletRequest request, @RequestBody Map<String,String> map){
    	log.info("获取数据库配置列表");
    	return JSON.toJSONString(rdsInstanceService.getConfigList(map.get("prjId"), map.get("versionId")));
    }
    
    /**
     * 修改配置文件
     * @param request
     * @param cloudRdsInstance
     * @return
     */
    @RequestMapping(value="/modifyConfiguration")
    @ResponseBody
    public String modifyConfiguration(HttpServletRequest request,@RequestBody CloudRDSInstance cloudRdsInstance) {
		EayunResponseJson json = new EayunResponseJson();
    	try{
    		json = rdsInstanceService.modifyRdsInstanceConfiguraion(cloudRdsInstance);
    		logService.addLog("更改配置文件",
    				ConstantClazz.LOG_TYPE_RDS, 
    				cloudRdsInstance.getRdsName(), 
    				cloudRdsInstance.getPrjId(),  
    				ConstantClazz.LOG_STATU_SUCCESS,null);
    	}catch(Exception e){
    		json.setRespCode(ConstantClazz.ERROR_CODE);
    		logService.addLog("更改配置文件",
    				ConstantClazz.LOG_TYPE_RDS, 
    				cloudRdsInstance.getRdsName(), 
    				cloudRdsInstance.getPrjId(),  
    				ConstantClazz.LOG_STATU_ERROR,e);
    		throw e;
    	}
    	return JSON.toJSONString(json);
    }
    
    @RequestMapping(value="/buyInstance")
    @ResponseBody
    public String buyInstance(HttpServletRequest request,@RequestBody CloudOrderRDSInstance cloudOrderRdsInstance){
    	log.info("购买云数据库实例");
    	JSONObject json  = new JSONObject();
    	try{
    		SessionUserInfo sessionUser = (SessionUserInfo) request.getSession().getAttribute(ConstantClazz.SYS_SESSION_USERINFO);
    		String errMsg = rdsInstanceService.buyRDSInstance(cloudOrderRdsInstance, sessionUser);
    		if(StringUtils.isEmpty(errMsg)){
    			json.put("respCode", ConstantClazz.SUCCESS_CODE_ADD);
    			json.put("orderNo", cloudOrderRdsInstance.getOrderNo());
    		}
    		else{
    			json.put("respCode", ConstantClazz.ERROR_CODE);
    			json.put("message", errMsg);
    		}
    	}catch(Exception e){
    		if("余额不足".equals(e.getMessage())){
				json.put("respCode", ConstantClazz.ERROR_CODE);
    			json.put("message", "CHANGE_OF_BALANCE");
			}
    		else{
    			json.put("respCode", ConstantClazz.ERROR_CODE);
    			json.put("orderNo", cloudOrderRdsInstance.getOrderNo());
    		}
			log.error(e.getMessage(),e);
    	}
    	return json.toJSONString();
    }
    
    /**
     * 查看数据库配额
     * @param request
     * @param cloudOrderRdsInstance
     * @return
     */
    @RequestMapping(value="/checkInstanceQuota")
    @ResponseBody
    public String checkInstanceQuota(HttpServletRequest request,@RequestBody CloudOrderRDSInstance cloudOrderRdsInstance){
    	log.info("查看实例配额是否充足");
    	EayunResponseJson json = new EayunResponseJson();
    	String errMsg = null;
    	errMsg = rdsInstanceService.checkInstanceQuota(cloudOrderRdsInstance);
    	if(null != errMsg){
    		json.setRespCode(ConstantClazz.ERROR_CODE);
    		json.setMessage(errMsg);
    	}else{
    		json.setRespCode(ConstantClazz.SUCCESS_CODE);
    	}
    	return JSON.toJSONString(json);
    }
    
    /**
     * 获取包年包月资源剩余天数-- 用于升级时计算价格
     * @param request
     * @param rdsId
     * 				-- 数据库实例ID
     * @return
     */
    @RequestMapping(value="/queryRdsInstanceChargeById")
    @ResponseBody
    public String queryRdsInstanceChargeById(HttpServletRequest request,@RequestBody String rdsId){
    	log.info("获取包年包月资源剩余天数");
    	EayunResponseJson json = new EayunResponseJson();
    	try{
    		CloudRDSInstance cloudRdsInstance = rdsInstanceService.queryRdsInstanceChargeById(rdsId);
    		json.setRespCode(ConstantClazz.SUCCESS_CODE);
    		json.setData(cloudRdsInstance.getCycleCount());
    	}catch (Exception e){
    		json.setRespCode(ConstantClazz.ERROR_CODE);
    		log.error(e.getMessage(),e);
    	}
    	return JSON.toJSONString(json);
    }
    /**
     * 实例升级
     * @param request
     * @param cloudOrderRdsInstance
     * @return
     */
    @RequestMapping(value="/resizeInstance")
    @ResponseBody
    public String resizeInstance(HttpServletRequest request,@RequestBody CloudOrderRDSInstance cloudOrderRdsInstance){
    	log.info("数据库实例的升降规格操作");
    	EayunResponseJson json = new EayunResponseJson();
    	SessionUserInfo sessionUser = (SessionUserInfo) request.getSession().getAttribute(ConstantClazz.SYS_SESSION_USERINFO);
    	try{
    		String errMsg = rdsInstanceService.resizeRdsInstance(cloudOrderRdsInstance, sessionUser);
    		if(null != errMsg){
    			json.setRespCode(ConstantClazz.ERROR_CODE);
    			json.setMessage(errMsg);
    		}else {
    			json.setRespCode(ConstantClazz.SUCCESS_CODE);
    			json.setData(cloudOrderRdsInstance.getOrderNo());
    		}
    		logService.addLog("升降规格",  
    				ConstantClazz.LOG_TYPE_RDS, 
    				cloudOrderRdsInstance.getRdsName(), 
    				cloudOrderRdsInstance.getPrjId(),  
    				ConstantClazz.LOG_STATU_SUCCESS,null);
    	}catch (Exception e){
    		json.setRespCode(ConstantClazz.ERROR_CODE);
			json.setMessage(cloudOrderRdsInstance.getOrderNo());
			logService.addLog("升降规格",  
    				ConstantClazz.LOG_TYPE_RDS, 
    				cloudOrderRdsInstance.getRdsName(), 
    				cloudOrderRdsInstance.getPrjId(),  
    				ConstantClazz.LOG_STATU_ERROR, e);
			log.error(e.getMessage(),e);
    	}
    	return JSON.toJSONString(json);
    }
    
    @RequestMapping(value="/checkRdsInstanceOrderExsit")
    @ResponseBody
    public String checkRdsInstanceOrderExsit(HttpServletRequest request,@RequestBody Map<String,String> map){
    	 log.info("检查当前是否已存在数据库实例续费或变配的未完成订单");
    	EayunResponseJson json = new EayunResponseJson();
		Boolean resize = new Boolean(map.get("isResize"));
		Boolean renew = new Boolean(map.get("isRenew"));
		String rdsId = map.get("rdsId");
    	try{
    		boolean flag = rdsInstanceService.checkRdsInstanceOrderExsit(rdsId, resize, renew);
    		if(flag){
    			json.setRespCode(ConstantClazz.ERROR_CODE);
    			json.setMessage("资源正在调整中或您有未完成的订单，请稍后再试。");
    		}else{
    			json.setRespCode(ConstantClazz.SUCCESS_CODE);
    		}
    	}catch(Exception e){
    		json.setRespCode(ConstantClazz.ERROR_CODE);
    		log.error(e.getMessage(),e);
    	}
    	return JSON.toJSONString(json);
    }
    
    /**
     * 数据库实例续费
     * @param request
     * @param map
     * @return
     */
    @RequestMapping(value="/renewInstance")
    @ResponseBody
    public String renewInstance(HttpServletRequest request,@RequestBody Map<String,String> map){
    	log.info("数据库实例续费");
    	SessionUserInfo sessionUser = (SessionUserInfo) request.getSession().getAttribute(ConstantClazz.SYS_SESSION_USERINFO);
    	JSONObject json = new JSONObject ();
    	try{
    		json = rdsInstanceService.renewRdsOrderConfirm(map, sessionUser);
    	}catch(Exception e){
    		log.error(e.getMessage(),e);
    	}
    	return json.toJSONString();
    }
    
    @RequestMapping(value = "/getinstancebyid")
    @ResponseBody
    public String getInstanceById (HttpServletRequest request,@RequestBody Map<String,String> map) {
        EayunResponseJson json = new EayunResponseJson();
        String rdsId = map.get("rdsId");
        try {
            CloudRDSInstance instance = rdsInstanceService.getInstanceByRdsId(rdsId);
            if (instance != null) {
                json.setRespCode(ConstantClazz.SUCCESS_CODE);
                json.setData(instance);
            } else {
                json.setRespCode(ConstantClazz.WARNING_CODE);
                json.setMessage("the instance is not exist");
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        return JSONObject.toJSONString(json);
    }
    /**
     * 根据订单编号获取实例的订单信息--由于重新下单
     * @param request
     * @param orderNo
     * 				-- 订单编号
     * @return
     */
    @RequestMapping(value="/getInstanceByOrderNo")
    @ResponseBody
    public String getInstanceByOrderNo(HttpServletRequest request,@RequestBody String orderNo){
    	log.info("根据订单编号获取云数据库实例订单信息");
    	EayunResponseJson json = new EayunResponseJson();
    	try{
    		CloudOrderRDSInstance order = rdsInstanceService.getInstanceByOrderNo(orderNo);
    		json.setRespCode(ConstantClazz.SUCCESS_CODE);
    		json.setData(order);
    	}catch(Exception e){
    		json.setRespCode(ConstantClazz.ERROR_CODE);
    	    log.error(e.getMessage(),e);
    		throw e;
    	}
    	return JSON.toJSONString(json);
    }

	/**
	 * 根据实例ID获取该实例的规格和数据盘大小信息
	 * @param request
	 * @param rdsId
     * @return
     */
	@RequestMapping(value="/getStandardByRdsId")
	@ResponseBody
	public String getStandardByRdsId(HttpServletRequest request,@RequestBody String rdsId){
		log.info("根据实例ID获取实例的规格和数据盘大小");
		EayunResponseJson json = new EayunResponseJson();
		Map<String, Integer> map = null;
		try{
			map = rdsInstanceService.getStandardByRdsId(rdsId);
			json.setRespCode(ConstantClazz.SUCCESS_CODE);
			json.setData(map);
		}catch(Exception e){
			log.error(e.getMessage(),e);
			throw e;
		}
		return JSON.toJSONString(json);
	}
}
