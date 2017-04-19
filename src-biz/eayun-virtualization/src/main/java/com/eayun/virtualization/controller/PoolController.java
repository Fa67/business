package com.eayun.virtualization.controller;

import java.math.BigDecimal;
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

import com.alibaba.fastjson.JSONObject;
import com.eayun.common.ConstantClazz;
import com.eayun.common.controller.BaseController;
import com.eayun.common.dao.ParamsMap;
import com.eayun.common.dao.QueryMap;
import com.eayun.common.dao.support.Page;
import com.eayun.common.exception.AppException;
import com.eayun.common.filter.SessionUserInfo;
import com.eayun.common.model.EayunResponseJson;
import com.eayun.log.service.LogService;
import com.eayun.virtualization.model.CloudLdPool;
import com.eayun.virtualization.model.CloudOrderLdPool;
import com.eayun.virtualization.model.CloudOrderNetWork;
import com.eayun.virtualization.service.CloudOrderLdPoolService;
import com.eayun.virtualization.service.PoolService;

@Controller
@RequestMapping("/cloud/loadbalance/pool")
@Scope("prototype")
public class PoolController extends BaseController{
	private static final Logger log = LoggerFactory.getLogger(PoolController.class);
	
	@Autowired
	private LogService logService;
	@Autowired
	private PoolService poolService;
	@Autowired
	private CloudOrderLdPoolService orderPoolService;
	
	/**
	 * 查询负载均衡器的列表
	 * 
	 * @author zhouhaitao
	 * @param request
	 * @param page
	 * @param map
	 * @return
	 * @throws AppException
	 */
	@RequestMapping(value= "/getPoolList" , method = RequestMethod.POST)
	@ResponseBody                     
	public String getPoolList(HttpServletRequest request,Page page, @RequestBody ParamsMap map) throws Exception{
		log.info("查询负载均衡器的列表");
		String poolName="";
		try {
			 String dcId=map.getParams().get("dcId").toString();
			 String prjId = map.getParams().get("prjId").toString();
			 if(map.getParams().containsKey("name")){
				 poolName=map.getParams().get("name").toString();
			 }
			int pageSize = map.getPageSize();
			int pageNumber = map.getPageNumber();
			QueryMap queryMap = new QueryMap();
			queryMap.setPageNum(pageNumber);
			queryMap.setCURRENT_ROWS_SIZE(pageSize);
			
			page  = poolService.getPoolList(page,dcId,prjId,poolName,queryMap);
		}catch (Exception e) {
			throw e;
		}
		return JSONObject.toJSONString(page);
	}
	
	/**
     * 验证重名 创建、编辑时
     * 
     * @author zhouhaitao
     * @param request
     * @param pool
     * @return
     */
    @RequestMapping(value= "/checkPoolNameExsit" , method = RequestMethod.POST)
    @ResponseBody
    public String checkPoolNameExsit(HttpServletRequest request,@RequestBody CloudLdPool pool) throws Exception{
		boolean isTrue=false;
    	try{
    		isTrue=poolService.checkPoolNameExsit(pool);
    	}catch(Exception e){
    		
    		throw e;
    	}
		return JSONObject.toJSONString(isTrue);
    	
    }
    /**
     * 新购负载均衡器
     * 
     * @author gaoxiang
     * @param request
     * @param cloudOrderPool
     * @return
     * @throws Exception
     */
    @RequestMapping(value = "/buybalancer", method = RequestMethod.POST)
    @ResponseBody
    public String buyBalancer(HttpServletRequest request, @RequestBody CloudOrderLdPool cloudOrderPool) throws Exception {
    	SessionUserInfo sessionUser = (SessionUserInfo) request.getSession().getAttribute(ConstantClazz.SYS_SESSION_USERINFO);
    	JSONObject json = new JSONObject();
    	String respMsg = new String();
    	try {
    		respMsg = poolService.buyBalancer(cloudOrderPool, sessionUser);
    		if (StringUtils.isEmpty(respMsg)) {
    		    json.put("respCode", ConstantClazz.SUCCESS_CODE);
    		    json.put("orderNo", cloudOrderPool.getOrderNo());
    		} else {
    		    json.put("respCode", ConstantClazz.WARNING_CODE);
    		    json.put("respMsg", respMsg);
    		}
    	} catch(Exception e) {
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
    /**
     * 创建负载均衡器
     * 
     * @author zhouhaitao
     * @param request
     * @param pool
     * @return
     */
    /*@RequestMapping(value= "/createBalancer" , method = RequestMethod.POST)
    @ResponseBody
    public String createBalancer(HttpServletRequest request,@RequestBody CloudLdPool pool){
    	JSONObject json = new JSONObject ();;
    	CloudLdPool cloudLdPool = null;
    	SessionUserInfo sessionUser = null;
    	try{
    		sessionUser = (SessionUserInfo) request.getSession().getAttribute(ConstantClazz.SYS_SESSION_USERINFO);
    		cloudLdPool = poolService.createBalancer(pool ,sessionUser);
    		json.put("data",cloudLdPool);
    		json.put("respCode",ConstantClazz.SUCCESS_CODE_ADD);
    		logService.addLog( "创建负载均衡器", ConstantClazz.LOG_TYPE_POOL, pool.getPoolName(), pool.getPrjId(),
					ConstantClazz.LOG_STATU_SUCCESS, null);
    	}catch(Exception e){
    		json.put("respCode",ConstantClazz.ERROR_CODE);
    		logService.addLog( "创建负载均衡器", ConstantClazz.LOG_TYPE_POOL, pool.getPoolName(), pool.getPrjId(),
    				ConstantClazz.LOG_STATU_ERROR, e);
    		throw e;
    	}
    	
    	return json.toJSONString();
    }*/
    /**
     * 更改最大连接数负载均衡器
     * @author gaoxiang
     * @param request
     * @param cloudOrderPool
     * @return
     * @throws Exception
     */
    @RequestMapping(value = "/changebalancer", method = RequestMethod.POST)
    @ResponseBody
    public String changeBalancer(HttpServletRequest request, @RequestBody CloudOrderLdPool cloudOrderPool) throws Exception {
    	SessionUserInfo sessionUser = (SessionUserInfo) request.getSession().getAttribute(ConstantClazz.SYS_SESSION_USERINFO);
    	JSONObject json = new JSONObject();
    	String respMsg = new String();
    	try {
    		respMsg = poolService.changeBalancer(cloudOrderPool, sessionUser);
    		if (StringUtils.isEmpty(respMsg)) {
    		    json.put("respCode", ConstantClazz.SUCCESS_CODE);
    		    json.put("orderNo", cloudOrderPool.getOrderNo());
    		} else {
    		    json.put("respCode", ConstantClazz.WARNING_CODE);
    		    json.put("respMsg", respMsg);
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
    /**
     * 修改负载均衡器名称
     * @author gaoxiang
     * @param request
     * @param pool
     * @return
     */
    @RequestMapping(value = "/updatebalancername" , method = RequestMethod.POST)
    @ResponseBody
    public String updateBalancerName(HttpServletRequest request, @RequestBody CloudLdPool pool) {
        JSONObject json = new JSONObject();
        try {
            pool = poolService.updateBalancerName(pool);
            json.put("respCode", ConstantClazz.SUCCESS_CODE);
            json.put("data", pool);
            logService.addLog( "编辑负载均衡器", ConstantClazz.LOG_TYPE_POOL, pool.getPoolName(), pool.getPrjId(),
                    ConstantClazz.LOG_STATU_SUCCESS, null);
        } catch (Exception e) {
            json.put("respCode", ConstantClazz.ERROR_CODE);
            logService.addLog( "编辑负载均衡器", ConstantClazz.LOG_TYPE_POOL, pool.getPoolName(), pool.getPrjId(),
                    ConstantClazz.LOG_STATU_ERROR, e);
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
    /*@RequestMapping(value= "/updateBalancer" , method = RequestMethod.POST)
    @ResponseBody
    public String updateBalancer(HttpServletRequest request,@RequestBody CloudLdPool pool){
    	JSONObject json = new JSONObject ();;
    	CloudLdPool cloudLdPool = null;
    	try{
    		cloudLdPool = poolService.updateBalancer(pool);
    		json.put("data",cloudLdPool);
    		json.put("respCode",ConstantClazz.SUCCESS_CODE_UPDATE);
    		logService.addLog( "编辑负载均衡器", ConstantClazz.LOG_TYPE_POOL, pool.getPoolName(), pool.getPrjId(),
    				ConstantClazz.LOG_STATU_SUCCESS, null);
    	}catch(Exception e){
    		json.put("respCode",ConstantClazz.ERROR_CODE);
    		logService.addLog( "编辑负载均衡器", ConstantClazz.LOG_TYPE_POOL, pool.getPoolName(), pool.getPrjId(),
    				ConstantClazz.LOG_STATU_ERROR, e);
    		throw e;
    	}
    	
    	return json.toJSONString();
    }*/
	
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
	public String deleteBalancer(HttpServletRequest request,@RequestBody CloudLdPool pool) throws AppException{
	    SessionUserInfo sessionUser = (SessionUserInfo) request.getSession().getAttribute(ConstantClazz.SYS_SESSION_USERINFO);
	    pool.setCusId(sessionUser.getCusId());
		JSONObject json = new JSONObject();
		try {
			poolService.deleteBalancer(pool);
			json.put("respCode", ConstantClazz.SUCCESS_CODE_DELETE);
			logService.addLog("删除负载均衡器",ConstantClazz.LOG_TYPE_POOL,  pool.getPoolName(), pool.getPrjId(),
					ConstantClazz.LOG_STATU_SUCCESS, null);
		}catch (Exception e) {
			json.put("respCode",ConstantClazz.ERROR_CODE);
			logService.addLog("删除负载均衡器",ConstantClazz.LOG_TYPE_POOL,  pool.getPoolName(), pool.getPrjId(),
					ConstantClazz.LOG_STATU_ERROR, e);
			throw e;
		}
		return json.toJSONString();
	}
    
	@RequestMapping(value = "/renewbalancer", method = RequestMethod.POST)
	@ResponseBody
	public String renewBalancer(HttpServletRequest request, @RequestBody Map map) throws Exception {
        log.info("续费负载均衡器开始");
        //todo 在map中把计费因子传入，重新计算价格，并进行支付，潜在trick-是否应当将前台页面判断使用余额支付金额与账户余额的大小关系判断放到后台？
        //假设前台已经做过了判断，且保证调用这个接口时，重新计算的应付金额、使用余额支付的金额和客户账户余额是关系正常的，先直接往后走
		SessionUserInfo sessionUser = (SessionUserInfo) request.getSession().getAttribute(ConstantClazz.SYS_SESSION_USERINFO);
		JSONObject json = new JSONObject();
        Map<String, String> respMap;
		try {
            respMap = poolService.renewBalancer(sessionUser, map);
            if(respMap.containsKey(ConstantClazz.WARNING_CODE)){
                json.put("respCode", ConstantClazz.WARNING_CODE);
                json.put("message", respMap.get(ConstantClazz.WARNING_CODE));
            }else if(respMap.containsKey(ConstantClazz.SUCCESS_CODE)){
                json.put("respCode", ConstantClazz.SUCCESS_CODE);
                json.put("message", respMap.get(ConstantClazz.SUCCESS_CODE));
                json.put("orderNo", respMap.get("orderNo"));
            }
            json.put("connections",respMap.get("connections"));
		} catch(Exception e) {
            log.error("续费负载均衡器失败", e);
		}
		return json.toJSONString();
	}
	/**
	 * 获取价格的前台接口
	 * @author gaoxiang
	 * @param request
	 * @param cloudOrderPool
	 * @return
	 */
	@RequestMapping(value = "/getprice", method = RequestMethod.POST)
	@ResponseBody
	public String getPrice(HttpServletRequest request, @RequestBody CloudOrderLdPool cloudOrderPool) {
	    EayunResponseJson json = new EayunResponseJson();
	    try {
	        BigDecimal price = poolService.getPrice(cloudOrderPool);
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
	 * 查询负载均衡器的详情
	 * 
	 * @author zhouhaitao
	 * @param poolId
	 * @return
	 * @throws AppException
	 */
	@RequestMapping(value = "/getLoadBalanceById" , method = RequestMethod.POST)
	@ResponseBody
	public String getLoadBalanceById(HttpServletRequest request, @RequestBody String poolId)throws Exception{
		log.info("查询负载均衡器的详情");
		JSONObject json = new JSONObject();
		CloudLdPool pool = null ;
		try{
			pool  =poolService.getLoadBalanceById(poolId);
			json.put("data", pool);
		}catch(Exception e){
			throw e;
		}
		return json.toJSONString();
	}

    @RequestMapping(value = "/checkLbOrderExist" , method = RequestMethod.POST)
    @ResponseBody
    public String checkLbOrderExist(HttpServletRequest request, @RequestBody String poolId) throws Exception{
        log.info("检查当前是否已存在负载均衡续费或变配的未完成订单");
        JSONObject json = new JSONObject();
        try{
            boolean flag = poolService.checkLbOrderExist(poolId);
            json.put("flag", flag);
        }catch(Exception e){
            throw e;
        }
        return json.toJSONString();
    }

	/**
	 * 获取项目下负载均衡配额
	 * @author gaoxiang
	 * @param request
	 * @param map
	 * @return
	 */
	@RequestMapping(value = "/getpoolquotasbyprjid", method = RequestMethod.POST)
	@ResponseBody
	public String getPoolQuotasByPrjId(HttpServletRequest request, @RequestBody Map<String ,String> map) {
	    JSONObject json = new JSONObject();
	    String prjId = map.get("prjId");
	    int quotas = poolService.getPoolQuotasByPrjId(prjId);
	    json.put("quotas", quotas);
	    return json.toJSONString();
	}
	/**
     * 根据订单编号查询订单信息
     * @author gaoxiang
     * @param request
     * @param map
     * @return
     */
    @RequestMapping(value = "/getorderldpoolbyorderno", method = RequestMethod.POST)
    @ResponseBody
    public String getOrderLdPoolByOrderNo(HttpServletRequest request, @RequestBody Map<String, String> map) {
        String orderNo = map.get("orderNo");
        EayunResponseJson json = new EayunResponseJson();
        CloudOrderLdPool pool = orderPoolService.getOrderLdPoolByOrderNo(orderNo);
        json.setRespCode(ConstantClazz.SUCCESS_CODE);
        json.setData(pool);
        return JSONObject.toJSONString(json);
    }
}
