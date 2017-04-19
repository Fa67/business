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

import com.eayun.common.ConstantClazz;
import com.eayun.common.dao.QueryMap;
import com.eayun.common.dao.support.Page;
import com.eayun.common.exception.AppException;
import com.eayun.common.model.EayunResponseJson;
import com.eayun.common.util.BeanUtils;
import com.eayun.ecmcuser.model.BaseEcmcSysUser;
import com.eayun.ecmcuser.util.EcmcSessionUtil;
import com.eayun.log.ecmcsevice.EcmcLogService;
import com.eayun.physical.ecmcservice.EcmcServerService;
import com.eayun.physical.ecmcvoe.DcServerVOE;
import com.eayun.physical.model.BaseDcServer;
import com.eayun.physical.model.DcServerModel;

@Controller
@RequestMapping("/ecmc/physical/server")

public class EcmcServerController {
	
	@SuppressWarnings("unused")
    private final Log log = LogFactory.getLog(this.getClass());
	@Autowired
	private EcmcServerService serverservice;
	@Autowired
	private EcmcLogService  ecmcLogService;
	
	/**
	 * 查询物理服务器列表
	 * @param datacenterId 数据中心id
	 * @param type :查询条件（1:服务器；2:型号；3:机柜）
	 * @param anyName 服务器名称
	 * @return
	 * @throws AppException
	 */
	@SuppressWarnings("unchecked")
    @RequestMapping("/queryserver")
	@ResponseBody
	public Object query(@RequestBody Map<String,Object> requestMap) throws AppException{
		Page page=null;
//		try {
		
 			Map<String,Object> map=(Map<String,Object>)requestMap.get("params");
			
			String dcId = MapUtils.getString(map, "dataCenterId");
			String type=MapUtils.getString(map, "type");
			String anyName=MapUtils.getString(map, "anyName");
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
			
			
			 page=serverservice.queryserver(dcId, type, anyName, queryMap) ;
			

		return page;
	}
	
	/**
	 * 添加或编辑是判断服务器名称是否存在
	 * */
	@RequestMapping("/checkNameExist")
	@ResponseBody
	public Object checkNameExist(@RequestBody Map<String,Object> requestMap)throws AppException{
		EayunResponseJson resp=new EayunResponseJson();
	
		   String name=MapUtils.getString(requestMap, "serverName");
		   String datacenterid=MapUtils.getString(requestMap, "dataCenterId");
		   String id=MapUtils.getString(requestMap, "serverId");
		   List<BaseDcServer> list= serverservice.querybyid(datacenterid, id, name);
		   if(list==null||list.size()==0){
			   resp.setData(true) ;
		   }else{
			   resp.setData(false) ;
		   }
		  

		return resp;
	}
	
	
	/**
	 * 查询物理服务器型号列表
	 * @param datacenterId 数据中心id
	 * @param anyName 服务器名称
	 * @return
	 * @throws AppException
	 */
	@RequestMapping("/queryByServerModel")
	@ResponseBody
	public Object queryByServerModel() throws AppException{
		EayunResponseJson resp=new EayunResponseJson();
		
			resp.setData(serverservice.queryByServerModel());
		

		return resp;
	}
	
	/**
	 * 根据服务器型号id获取服务器模板数据
	 * @param id 服务器型号id
	 * @param request
	 * @return
	 * @throws Exception 
	 */
	@RequestMapping("/getByServerModelId")
	@ResponseBody
	public Object getByServerModelId(@RequestBody Map<String,Object> requestMap) throws AppException{
		EayunResponseJson resp=new EayunResponseJson();
		
		
			String serverModelId=MapUtils.getString(requestMap, "serverModelId");
			DcServerModel model=serverservice.getByServerModel(serverModelId);
			resp.setData(model);
			

		return resp;
	}
	
	
	
	/**
	 * 保存
	 * @param request
	 * @param dcServer
	 * @param reJson
	 * @return
	 * @throws AppException 
	 */
	@SuppressWarnings("unchecked")
    @RequestMapping("/saveserver")
	@ResponseBody
	public Object save(@RequestBody Map<String,Object> requestMap) throws AppException{
		
		EayunResponseJson resp=new EayunResponseJson();
		
		
			BaseDcServer model=new BaseDcServer();
			Map<String,Object> map=(Map<String,Object>)requestMap.get("servermodel");
			BeanUtils.mapToBean(model,map );
			String state=MapUtils.getString(map, "state");//获取机柜的位置
			BaseEcmcSysUser user=EcmcSessionUtil.getUser();
		
			try{
			if(user!=null){
				serverservice.saveServer(model,user.getAccount(),state);//保存物理服务器
			}else{
				serverservice.saveServer(model,"",state);//保存物理服务器
			}
			ecmcLogService.addLog("新增服务器", "服务器", model.getName(), null, 1, model.getId(), null);
			}catch(Exception e){
				ecmcLogService.addLog("新增服务器", "服务器", model.getName(), null, 0, model.getId(), e);
				throw new AppException("新增服务器异常",e);
			}
			
			resp.setRespCode(ConstantClazz.SUCCESS_CODE);

		return resp;
}


	/**
	 * 删除
	 * @param request
	 * @param idStr
	 * @param delJson
	 * @return
	 * @throws AppException
	 */
	@RequestMapping("/deleteserver")
	@ResponseBody
	public Object delete(@RequestBody Map<String,Object> requestMap
			) throws AppException{
		EayunResponseJson resp =new EayunResponseJson();
		
			
			String idStr=MapUtils.getString(requestMap, "idstr");
			 if(idStr!=null && !"".equals(idStr)){
				 DcServerVOE model=serverservice.getByDcServerId(idStr);
				 
				 try{
					 serverservice.deleteserver(idStr);
					 //删除机柜
					 serverservice.deletecabinetrf(idStr);
					
					 resp.setRespCode(ConstantClazz.SUCCESS_CODE);
					 ecmcLogService.addLog("删除服务器", "服务器", model.getName(), null, 1, idStr, null);
				 }catch(Exception e){
					 ecmcLogService.addLog("删除服务器", "服务器", model.getName(), null, 0, idStr, e);
						throw new AppException("删除服务器异常",e);
				 }
				
			 }

		 return resp;
	}
	
	
	/**
	 * 根据id获取服务器模板数据
	 * @param request
	 * @param dcServer
	 * @return
	 * @throws AppException
	 */
	@RequestMapping("/getByDcServerId")
	@ResponseBody
	public Object getByDcServerId(@RequestBody Map<String,Object> requestMap) throws AppException{
		EayunResponseJson resp =new EayunResponseJson();
		
			String id=MapUtils.getString(requestMap, "serverId");
			resp.setData(serverservice.getByDcServerId(id));

		 return resp;
	}
	
	/**
	 * 修改物理服务器
	 * @param request
	 * @param dcServer
	 * @param reJson
	 * @return
	 * @throws Exception 
	 */
	@SuppressWarnings("unchecked")
    @RequestMapping("/updateserver")
	@ResponseBody
	public Object update(@RequestBody Map<String,Object> requestMap) throws AppException{
		EayunResponseJson reJson=new EayunResponseJson();
		
		
			BaseDcServer model=new BaseDcServer();
			Map<String,Object>map=(Map<String,Object>)requestMap.get("servermodel");
			String state=MapUtils.getString(map, "state");//获取机柜的位置
			BeanUtils.mapToBean(model, map);
			try{
			serverservice.update(model,state);
			reJson.setRespCode(ConstantClazz.SUCCESS_CODE);
			 ecmcLogService.addLog("修改服务器", "服务器", model.getName(), null, 1, model.getId(), null);
			}catch(Exception e){
				ecmcLogService.addLog("修改服务器", "服务器", model.getName(), null, 0, model.getId(), e);
					throw new AppException("修改服务器异常",e);
			 }

		 return reJson;
	}
	



}