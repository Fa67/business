package com.eayun.physical.ecmccontroller;

import java.util.Map;

import org.apache.commons.collections.MapUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.alibaba.fastjson.JSONObject;
import com.eayun.common.ConstantClazz;
import com.eayun.common.dao.QueryMap;
import com.eayun.common.dao.support.Page;
import com.eayun.common.exception.AppException;
import com.eayun.common.model.EayunResponseJson;
import com.eayun.common.util.BeanUtils;
import com.eayun.ecmcuser.model.BaseEcmcSysUser;
import com.eayun.ecmcuser.util.EcmcSessionUtil;
import com.eayun.log.ecmcsevice.EcmcLogService;
import com.eayun.physical.ecmcservice.EcmcFirewallService;
import com.eayun.physical.ecmcvoe.DcFirewallVOE;
import com.eayun.physical.model.BaseDcFirewall;


@Controller
@RequestMapping("ecmc/physical/firewall")
public class EcmcFirewallController {
	private final Log log = LogFactory.getLog(EcmcFirewallController.class);
	

	@Autowired
	private EcmcFirewallService firewallservice;

	@Autowired
	private EcmcLogService ecmcLogService;
	
	/**
	 * 防火墙页面
	 * 
	 * @param request
	 * @param page
	 * @return
	 */
	@SuppressWarnings("unchecked")
    @RequestMapping("queryfirewall")
	@ResponseBody
	public Object query(@RequestBody Map<String,Object> requestMap)
			throws AppException{
	//	Map<String, Object> retrunJson = new HashMap<String, Object>();

			Map<String,Object> map=(Map<String,Object>)requestMap.get("params");
			
			String dcId = MapUtils.getString(map, "dataCenterId");
			String name=MapUtils.getString(map, "firewallName");
			int pageSize=MapUtils.getIntValue(requestMap, "pageSize");
			int pageNo=MapUtils.getIntValue(requestMap, "pageNumber");
			QueryMap queryMap=new QueryMap();
			if(pageNo==0){
				 queryMap.setPageNum(1);
			}else{
				queryMap.setPageNum(pageNo);
			}
			 if(pageSize==0){
				 queryMap.setCURRENT_ROWS_SIZE(20);
			 }else{
			 queryMap.setCURRENT_ROWS_SIZE(pageSize);
			 }
			Page page=firewallservice.query(name, dcId , queryMap);
			log.info(JSONObject.toJSON(page));// 这里使用阿里巴巴的json，可以得到anjularjs识别日期格式
			return  page;

	
	}
	
	/**
	 * 通过id获取model
	 * 
	 * @param request
	 * @param page
	 * @return
	 */
	@RequestMapping("queryById")
	@ResponseBody
	public Object queryById(@RequestBody Map<String,Object> requestMap)
			throws AppException{
		EayunResponseJson respJson=new EayunResponseJson();
		
			String id=MapUtils.getString(requestMap, "firewallId");
			
			
			respJson.setData(firewallservice.queryById(id));

		log.info(JSONObject.toJSON(respJson));// 这里使用阿里巴巴的json，可以得到anjularjs识别日期格式
		return respJson;
		
	}	
	

	
	/**
	 * 创建防火墙
	 * 
	 * @param request
	 * @param page
	 * @return
	 */
	@SuppressWarnings("unchecked")
    @RequestMapping("createfirewall")
	@ResponseBody
	public Object create(@RequestBody Map<String,Object> requestMap)
			throws AppException{
		EayunResponseJson respJson=new EayunResponseJson();
		
			BaseEcmcSysUser user=EcmcSessionUtil.getUser();
			
			Map<String,Object> map=(Map<String,Object>)requestMap.get("firewallmodel");
			String state =MapUtils.getString(map, "state");
			BaseDcFirewall model=new BaseDcFirewall();
			BeanUtils.mapToBean(model,map);
			
			try{
				if(user!=null){
				firewallservice.createfirewall(model,state,user.getAccount());
				}else{
				firewallservice.createfirewall(model,state,"");
				}
				ecmcLogService.addLog("新增防火墙", "防火墙", model.getName(), null, 1, model.getId(), null);
			}catch(Exception e){
				ecmcLogService.addLog("新增防火墙", "防火墙", model.getName(), null, 0, model.getId(), e);
				throw new AppException("新增防火墙异常",e);
			}
			
			
			respJson.setMessage("防火墙添加成功");
        	respJson.setRespCode(ConstantClazz.SUCCESS_CODE);
		
		return respJson;
	}
	
	
	/**
	 * 编辑防火墙
	 * 
	 * @param request
	 * @param page
	 * @return
	 */
	@SuppressWarnings("unchecked")
    @RequestMapping("updatefirewall")
	@ResponseBody
	public Object queryDcStorageUpdate(@RequestBody Map<String,Object> requestMap)
			throws AppException{
		EayunResponseJson respJson=new EayunResponseJson();
		
			Map<String,Object> map=(Map<String,Object>)requestMap.get("firewallmodel");
			String state = MapUtils.getString(map, "state");
			BaseDcFirewall model=new BaseDcFirewall();
			BeanUtils.mapToBean(model,map);
			try{
			firewallservice.updatefirewall(model,state);
			ecmcLogService.addLog("更新防火墙", "防火墙", model.getName(), null, 1, model.getId(), null);
			}catch(Exception e){
				ecmcLogService.addLog("更新防火墙", "防火墙", model.getName(), null, 0, model.getId(), null);
				throw new AppException("更新防火墙异常",e);
			}
			respJson.setMessage("防火墙编辑成功");
			respJson.setRespCode(ConstantClazz.SUCCESS_CODE);

		return respJson;
	}

	/**
	 * 删除防火墙
	 * 
	 * @param request
	 * @param page
	 * @return
	 */
	@RequestMapping("deletefirewall")
	@ResponseBody
	public Object Del(@RequestBody Map<String,Object> requestMap)throws AppException{
		EayunResponseJson delJson=new EayunResponseJson();
		
			String id=MapUtils.getString(requestMap, "firewallId");
			DcFirewallVOE model=firewallservice.queryById(id);
		try{
			firewallservice.delete(id);
			
			ecmcLogService.addLog("删除防火墙", "防火墙", model.getName(), null,1, id, null);
		}catch(Exception e){
			ecmcLogService.addLog("删除防火墙", "防火墙", model.getName(), null,0, id, e);
			throw new AppException("删除防火墙异常",e);
		}
			
        	delJson.setRespCode(ConstantClazz.SUCCESS_CODE);
		

		return delJson;
	}
	

	

	
	/**
	 * 添加时判断防火墙名称是否存才
	 * */
	@RequestMapping("/checkNameExist")
	@ResponseBody
	public Object checkNameExist(@RequestBody Map<String,Object> requestMap)throws AppException{
		EayunResponseJson resp=new EayunResponseJson();
		
		   String name=MapUtils.getString(requestMap, "firewallName");
		   String dataCenterId=MapUtils.getString(requestMap, "dataCenterId");
		  
		   resp.setData(firewallservice.addcheckNameExist(name, dataCenterId));

		return resp;
	}
	
	/**
	 * 修改时判断防火墙名称是否存才
	 * */
	@RequestMapping("/checkNameExistOfEdit")
	@ResponseBody
	public Object checkNameExistOfEdit(@RequestBody Map<String,Object> requestMap)throws AppException{
		EayunResponseJson resp=new EayunResponseJson();

		  
		   String name=MapUtils.getString(requestMap, "firewallName");
		   String dataCenterId=MapUtils.getString(requestMap, "dataCenterId");
		   String firewallId=MapUtils.getString(requestMap, "firewallId");
		
		 
		  
			   resp.setData(firewallservice.updatecheckNameExist(name, firewallId, dataCenterId));
		 
			   

		return resp;
	}

}