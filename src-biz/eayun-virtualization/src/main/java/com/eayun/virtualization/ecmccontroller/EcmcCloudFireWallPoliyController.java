package com.eayun.virtualization.ecmccontroller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.eayun.common.ConstantClazz;
import com.eayun.common.dao.ParamsMap;
import com.eayun.common.dao.QueryMap;
import com.eayun.common.dao.support.Page;
import com.eayun.common.exception.AppException;
import com.eayun.common.model.EayunResponseJson;
import com.eayun.eayunstack.model.FirewallPolicy;
import com.eayun.log.ecmcsevice.EcmcLogService;
import com.eayun.virtualization.ecmcservice.EcmcCloudFireWallPoliyService;
import com.eayun.virtualization.model.BaseCloudFwPolicy;
import com.eayun.virtualization.model.CloudFwPolicy;

/**
 * @author jingang.liu@eayun.com to beijing
 * @date 2016年4月12日
 */
@Controller
@RequestMapping("/ecmc/virtual/cloudfwpoliy")
public class EcmcCloudFireWallPoliyController {

	private static final Log log = LogFactory.getLog(EcmcCloudFireWallPoliyController.class);
	@Autowired
	private EcmcCloudFireWallPoliyService poliyservice;
	@Autowired
    private EcmcLogService logServer;
	
	/**
	 * 用于校验策略名称是否已经存在
	 * @param request
	 * @param page
	 * @return
	 * @throws AppException
	 */
	@RequestMapping("/checkFwPolicyName")
	@ResponseBody
	public Object checkFwPolicyName(@RequestBody Map<String, String> map)throws AppException{
		EayunResponseJson res = new EayunResponseJson();
		res.setRespCode(ConstantClazz.SUCCESS_CODE);
		res.setData(poliyservice.checkFwPolicyName(map.get("fwpName"),map.get("projectId"),map.get("datacenterId"),map.get("fwpId")));
		return res;
	}
	/**
	 * 分页查询防火墙策略的信息
	 * @param page
	 * @param mapparems
	 * @return
	 * @throws AppException
	 */
	@RequestMapping("/querylist")
	@ResponseBody
	public Object list(Page page,@RequestBody ParamsMap mapparems) throws AppException{
		String prjName = mapparems.getParams().get("prjName") == null ? null : mapparems.getParams().get("prjName").toString();
		String name = mapparems.getParams().get("name") == null ? null : mapparems.getParams().get("name").toString();
		String datacenterId = mapparems.getParams().get("datacenterId") == null ? null : mapparems.getParams().get("datacenterId").toString();
		String cusOrg = mapparems.getParams().get("cusOrg") == null ? null : mapparems.getParams().get("cusOrg").toString();
		QueryMap queryMap=new QueryMap();
        queryMap.setPageNum(mapparems.getPageNumber());
        queryMap.setCURRENT_ROWS_SIZE(mapparems.getPageSize());
		page = poliyservice.list(page, prjName, name,cusOrg,datacenterId, queryMap);
		return page;
	}
	
	/**
	 * 查询所有防火墙策略的信息，用于在防火墙的前台页面以下拉框形式展示
	 * @param prjid
	 * @return
	 * @throws AppException
	 */
	@RequestMapping("/queryIdandName")
	@ResponseBody
	public Object queryIdandName(@RequestBody Map<String, String> map)throws AppException{
		EayunResponseJson res = new EayunResponseJson();
		res.setRespCode(ConstantClazz.SUCCESS_CODE);
		res.setData(poliyservice.queryIdandName(map.get("prjid")));
		return res;
	}
	/**查询prjId查询防火墙策略
	    * @author Chengxiaodong
	    * @param request
	    * @param datacenterId
	    * @param projectId
	    * @param name
	    * @param page
	    * @return
	    */
	    @RequestMapping(value= "/getFwpListByPrjId" , method = RequestMethod.POST)
	    @ResponseBody
	    public Object getFwpListByPrjId(HttpServletRequest request,Page page,@RequestBody Map map){
	    	List<CloudFwPolicy> listFwp=null;
	        try {
	        	log.info("查询防火墙列表开始");
	            String prjId = map.get("prjId").toString();
	            String dcId=map.get("dcId").toString();
	            listFwp=poliyservice.getFwpListByPrjId(dcId,prjId);
	        } catch (AppException e) {
	            throw e;
	        }
	        return listFwp;
	       
	    }
	    /**
	     * 编辑页面专用
	     * 
	     * ***/
	    @RequestMapping(value= "/getPolicyListByDcIdPrjId" , method = RequestMethod.POST)
	    @ResponseBody
	    public Object getPolicyListByDcIdPrjId(HttpServletRequest request,@RequestBody Map map){
	    	List<CloudFwPolicy> listFwp=null;
	        try {
	        	log.info("查询防火墙列表开始");
	            String prjId = map.get("prjId").toString();
	            String dcId=map.get("dcId").toString();
	            listFwp=poliyservice.getPolicyListByDcIdPrjId(dcId,prjId);
	        } catch (AppException e) {
	            throw e;
	        }
	        return listFwp;
	       
	    }
	    
	    
	    
	    
	/**
	 * 获取指定Id的防火墙策略
	 * @param id
	 * @return
	 */
	@RequestMapping("/queryById")
	@ResponseBody
	public Object queryById(@RequestBody Map<String, String> map) throws AppException{
		EayunResponseJson res = new EayunResponseJson();
		res.setRespCode(ConstantClazz.SUCCESS_CODE);
		res.setData(poliyservice.getById(map.get("id")));
		return res;
	}
	/**
	 * 创建防火墙策略
	 * @param parmes
	 * @return
	 * @throws AppException
	 */
	@RequestMapping("/createPolicy")
	@ResponseBody
	public Object createPolicy(@RequestBody Map<String, String> parmes) throws AppException{
		FirewallPolicy result=null;
		EayunResponseJson res = new EayunResponseJson();
		try {
			result = poliyservice.create(parmes);
			if (result!=null) {
				res.setRespCode(ConstantClazz.SUCCESS_CODE);
				res.setData(result);
				logServer.addLog("创建防火墙策略",ConstantClazz.LOG_TYPE_FIREPOLICY, parmes.get("name"),parmes.get("projectId"), 1,result.getId(),null);
			}
			
		} catch (AppException e) {
			res.setRespCode(ConstantClazz.ERROR_CODE);
			logServer.addLog("创建防火墙策略",ConstantClazz.LOG_TYPE_FIREPOLICY, parmes.get("name"),parmes.get("projectId"), 0,null,e);
			throw e;
		}
		
		return res;
	}
	/**
	 * 修改页面查询已用的防火墙规则
	 */
	@RequestMapping("/getByFwrId")
	@ResponseBody
	public Object getByFwrId(@RequestBody Map<String, String> map) throws AppException{
		EayunResponseJson res = new EayunResponseJson();
		res.setRespCode(ConstantClazz.SUCCESS_CODE);
		res.setData(poliyservice.getByFwrId(map.get("fwpid")));
		return res;
	}
	/**
	 * 修改页面查询未被策略使用的防火墙规则
	 */
	@RequestMapping("/getByFwrIdList")
	@ResponseBody
	public Object getByFwrIdList(@RequestBody Map<String, String> map) throws AppException{
		EayunResponseJson res = new EayunResponseJson();
		res.setRespCode(ConstantClazz.SUCCESS_CODE);
		res.setData(poliyservice.getByFwrIdList(map.get("projectId")));
		return res;
	}
	/**
	 * 修改防火墙策略
	 * @param parmes
	 * @return
	 * @throws AppException
	 */
	@RequestMapping("/updatePolicy")
	@ResponseBody
	public Object updatePolicy(@RequestBody Map<String, String> parmes) throws AppException{
		EayunResponseJson res = new EayunResponseJson();
		FirewallPolicy result = new FirewallPolicy();
		try {
			result  = poliyservice.update(parmes);
			logServer.addLog("修改防火墙策略",ConstantClazz.LOG_TYPE_FIREPOLICY, parmes.get("name"),parmes.get("projectId"), 1,parmes.get("id"),null);
			if (null != result) {
				res.setRespCode(ConstantClazz.SUCCESS_CODE);
				res.setData(result);
			}
		} catch (AppException e) {
			logServer.addLog("修改防火墙策略",ConstantClazz.LOG_TYPE_FIREPOLICY, parmes.get("name"),parmes.get("projectId"), 0,parmes.get("id"),e);
			throw e;
			
		}
		
		return res;
	}
	/**
	 * 删除防火墙策略
	 * @param swpId
	 * @return
	 * @throws AppException
	 */
	@RequestMapping("/deletePolicy")
	@ResponseBody
	public Object deletePolicy(@RequestBody Map<String, String> mapprames) throws AppException{
		EayunResponseJson res = new EayunResponseJson();
		boolean result = false;
		BaseCloudFwPolicy  policy = poliyservice.getById(mapprames.get("fwpId"));
		try {
			result = poliyservice.deletePolicy(mapprames.get("datacenterId"),mapprames.get("projectId"),mapprames.get("fwpId"));
			if (result) {
				res.setRespCode(ConstantClazz.SUCCESS_CODE);
				res.setData(result);
			}
			logServer.addLog("删除防火墙策略",ConstantClazz.LOG_TYPE_FIREPOLICY, policy.getFwpName(),policy.getPrjId(), 1,policy.getFwpId(),null);
		} catch (Exception e) {
			logServer.addLog("删除防火墙策略",ConstantClazz.LOG_TYPE_FIREPOLICY, policy.getFwpName(),policy.getPrjId(), 0,policy.getFwpId(),e);
			throw e;
		}
		return res;
	}
	
	
	/**
     * 编辑防火墙策略规则
     * @author chengxiaodong
     * @param request
     * @param map
     * @return
     */
    @RequestMapping(value= "/editFwRule")
    @ResponseBody
     public Object toDoFwRule(HttpServletRequest request,@RequestBody CloudFwPolicy fwp)throws AppException{
    	EayunResponseJson res = new EayunResponseJson();
    	boolean isTrue=false;
    	BaseCloudFwPolicy  policy = poliyservice.getById(fwp.getFwpId());
    	 try{
    		 log.info("管理防火墙策略规则");
        	 isTrue=poliyservice.toDoFwRule(fwp);
        	 res.setRespCode(ConstantClazz.SUCCESS_CODE);
        	 res.setData(isTrue);
        	 logServer.addLog("管理防火墙策略规则",ConstantClazz.LOG_TYPE_FIREPOLICY, policy.getFwpName(),policy.getPrjId(), 1,policy.getFwpId(),null);
    	 }catch(AppException e){
    		 logServer.addLog("管理防火墙策略规则",ConstantClazz.LOG_TYPE_FIREPOLICY, policy.getFwpName(),policy.getPrjId(), 0,policy.getFwpId(),e);
        	 throw e;
    	 }
    	 return res;
     }
    /**
     * 调整防火墙策略规则的优先级
     * @param request
     * @param fwp
     * @param local
     * @param target
     * @param reference
     * @return
     * @throws AppException
     */
    @RequestMapping(value= "/updateRuleSequence")
    @ResponseBody
    public Object updateFwR(HttpServletRequest request,@RequestBody Map<String, Object> prames)throws AppException{
    	EayunResponseJson res = new EayunResponseJson();
    	log.info("调整防火墙策略规则的优先级");
    	boolean isTrue=false;
    	CloudFwPolicy fwp = new CloudFwPolicy();
    	Map<String, String> map = new HashMap<>();
    	try{
    		map = (Map<String, String>)prames.get("fwp");
    		fwp.setFwpId(map.get("id").toString());
    		fwp.setFwpName(map.get("name").toString());
    		fwp.setPrjId(map.get("projectId").toString());
    		fwp.setDcId(map.get("datacenterId").toString());
    		String local = prames.get("local").toString();
    		String target = prames.get("target").toString();
    		String reference = prames.get("reference").toString();
    		isTrue = poliyservice.updateRuleSequence(fwp, local, target, reference);
    		res.setRespCode(ConstantClazz.SUCCESS_CODE);
   	 		res.setData(isTrue);
   	 		logServer.addLog("调整优先级",ConstantClazz.LOG_TYPE_FIRERULE, prames.get("fwrName").toString(),fwp.getPrjId(), 1,target,null);
    	}catch(AppException e){
    		logServer.addLog("调整优先级",ConstantClazz.LOG_TYPE_FIRERULE, prames.get("fwrName").toString(),fwp.getPrjId(), 0,prames.get("target").toString(),e);
    		throw e;
    	}
    	return res;
    }
}
