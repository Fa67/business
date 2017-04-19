package com.eayun.physical.ecmccontroller;

import java.util.List;
import java.util.Map;

import org.apache.commons.collections.MapUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.alibaba.fastjson.JSON;
import com.eayun.common.ConstantClazz;
import com.eayun.common.dao.QueryMap;
import com.eayun.common.dao.support.Page;
import com.eayun.common.exception.AppException;
import com.eayun.common.model.EayunResponseJson;
import com.eayun.common.util.BeanUtils;
import com.eayun.ecmcuser.model.BaseEcmcSysUser;
import com.eayun.ecmcuser.util.EcmcSessionUtil;
import com.eayun.log.ecmcsevice.EcmcLogService;
import com.eayun.physical.ecmcservice.EcmcSwitchService;
import com.eayun.physical.ecmcvoe.DcSwitchVOE;
import com.eayun.physical.model.BaseDcSwitch;

@Controller
@RequestMapping("/ecmc/physical/switch")

	
	
public class EcmcSwitchController {
	
@SuppressWarnings("unused")
private final Log log = LogFactory.getLog(this.getClass());
	
	
	@Autowired
	private  EcmcSwitchService ecmcSwitchService;
	
	@Autowired 
	private EcmcLogService ecmcLogService;


	
	/**
	 * 获取交换机列表
	 * */
	@SuppressWarnings("unchecked")
    @RequestMapping("/queryswitch")
	@ResponseBody
	public Object query(@RequestBody Map<String,Object> requestMap)throws AppException{
		Page page=null;
	
			
			Map<String,Object> map=(Map<String,Object>)requestMap.get("params");
			String switchName=MapUtils.getString(map, "switchName");
			int pageSize=MapUtils.getIntValue(requestMap, "pageSize");
			int pageNo=MapUtils.getIntValue(requestMap, "pageNumber");
			String datacenterId=MapUtils.getString(map, "dataCenterId");
		
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
	
			page=ecmcSwitchService.query(datacenterId,switchName,queryMap);

		return page;
	}
	
	/**
	 * 删除交换机
	 * */
	@RequestMapping("/deleteswitch")
	@ResponseBody
	public Object delete(@RequestBody Map<String,Object> requestMap)throws AppException{
		EayunResponseJson del=new EayunResponseJson();
		
			
			
			String switchId=MapUtils.getString(requestMap, "switchId");
			DcSwitchVOE model=ecmcSwitchService.queryById(switchId);
			try{
			ecmcSwitchService.delete(switchId);
			
			del.setRespCode(ConstantClazz.SUCCESS_CODE);
			ecmcLogService.addLog("删除交换机", "交换机", model.getName(), null, 1, switchId, null);
			}catch(Exception e){
				ecmcLogService.addLog("删除交换机", "交换机", model.getName(), null, 0, switchId, e);
				throw new AppException("删除交换机异常",e);
			}

		
		return del;
	}
	
	/**
	 * 根据交换机ID获取交换机数据
	 * */
	@RequestMapping("/queryById")
	@ResponseBody
	public Object queryById(@RequestBody Map<String,Object> requestMap)throws AppException{
		EayunResponseJson resp=new EayunResponseJson();
		
			String switchId = MapUtils.getString(requestMap, "switchId");
			resp.setData(ecmcSwitchService.queryById(switchId));

		
		return resp;
	}
	

	/**
	 * 更新交换机
	 * */
	@SuppressWarnings("unchecked")
    @RequestMapping("/updateswitch")
	@ResponseBody
	public Object update(@RequestBody Map<String,Object> requestMap)throws AppException{
			EayunResponseJson resp=new EayunResponseJson();
		
			BaseDcSwitch model=new BaseDcSwitch();
			BeanUtils.mapToBean(model,(Map<String,Object>)requestMap.get("switchmodel"));
			
			Map<String,Object> map=(Map<String,Object>)requestMap.get("switchmodel");
			
			String state = MapUtils.getString(map, "state");
			try{
			ecmcSwitchService.update(model,state);
			resp.setRespCode(ConstantClazz.SUCCESS_CODE);
			ecmcLogService.addLog("更新交换机", "交换机", model.getName(), null, 1, model.getId(), null);
			}catch(Exception e){
				ecmcLogService.addLog("更新交换机", "交换机", model.getName(), null, 0, model.getId(), null);
				throw new AppException("更新交换机异常",e);
			}

		
		return JSON.toJSONString(resp);
	}
	
	/**
	 * 验证名称重复
	 * */
	@RequestMapping("/checkNameExist")
	@ResponseBody
	public Object checkName(@RequestBody Map<String,Object> requestMap)throws AppException{
		boolean fag=false;
		EayunResponseJson resp=new EayunResponseJson();
		
		   String name=MapUtils.getString(requestMap, "switchName");
		   String dataCenterId=MapUtils.getString(requestMap, "dataCenterId");
		   String id = MapUtils.getString(requestMap, "id");
		 
		   if(id!=null){
			   List<BaseDcSwitch>list = ecmcSwitchService.checkNameExist(name, dataCenterId,id);
			   		if(list.size()==0){
			   			fag=true;
			   		}
		   }else{
			   List<BaseDcSwitch>list= ecmcSwitchService.checkNameExist(name, dataCenterId);
			   if(list.size()==0){
		   			fag=true;
		   		}
		   }

		
		resp.setData(fag);
		return resp;
	}
	
	/**
	 * 新增交换机
	 * */
	@SuppressWarnings("unchecked")
    @RequestMapping("/addswitch")
	@ResponseBody
	public Object add(@RequestBody Map<String,Object> requestMap)throws AppException{
			EayunResponseJson resp=new EayunResponseJson();
		
			
			BaseEcmcSysUser user=EcmcSessionUtil.getUser();
			
			BaseDcSwitch model=new BaseDcSwitch();
			Map<String,Object> map=(Map<String,Object>)requestMap.get("switchmodel");
			BeanUtils.mapToBean(model,map);
			String state=MapUtils.getString(map, "state");
			try{
			if(user!=null){
				ecmcSwitchService.addswitch(model,user.getAccount(),state);
			}else{
				ecmcSwitchService.addswitch(model,"",state);
			}
			ecmcLogService.addLog("新增交换机", "交换机", model.getName(), null, 1, model.getId(), null);
			}catch(Exception e){
				ecmcLogService.addLog("新增交换机", "交换机", model.getName(), null, 0, model.getId(), null);
				throw new AppException("新增交换机异常",e);
			}
			resp.setRespCode(ConstantClazz.SUCCESS_CODE);

		
		return JSON.toJSONString(resp);
	}
	

}
