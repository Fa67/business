package com.eayun.physical.ecmccontroller;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.apache.commons.collections.MapUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.propertyeditors.CustomDateEditor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.InitBinder;
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
import com.eayun.physical.ecmcservice.EcmcStorageService;
import com.eayun.physical.ecmcvoe.DcStorageVOE;
import com.eayun.physical.model.BaseDcStorage;


@Controller
@RequestMapping("/ecmc/physical/storage")
public class EcmcStorageController {
	private final Log log = LogFactory.getLog(this.getClass());
	
	@Autowired
	private EcmcStorageService storageservice;

	@Autowired
	private EcmcLogService  ecmcLogService;

	
	/**
	 * 将日期由字符串格式转换成Date类型
	 * @param binder
	 */
	@InitBinder    
	public void initBinder(WebDataBinder binder) {    
	        SimpleDateFormat dateFormat = new SimpleDateFormat("EEE MMM d yyyy HH:mm:ss",Locale.ENGLISH);    
	        dateFormat.setLenient(false);
	        binder.registerCustomEditor(Date.class, new CustomDateEditor(dateFormat, false));    
	}
	
	
	/**
	 * 存储页面
	 * 
	 * @param request
	 * @param page
	 * @return
	 */
	@SuppressWarnings("unchecked")
    @RequestMapping("queryDcStorage")
	@ResponseBody
	public Object queryDcStorage(@RequestBody Map<String,Object> requestMap)
			throws AppException{
		Page page=null;
		
			
			Map<String,Object> map=(Map<String,Object>)requestMap.get("params");
			
			String dcId = MapUtils.getString(map, "dataCenterId");
			String name=MapUtils.getString(map, "storageName");
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
			 page=storageservice.querybystoragelist(dcId, name,queryMap);

		log.info(JSONObject.toJSON(page));// 这里使用阿里巴巴的json，可以得到anjularjs识别日期格式
		return page;	
	}
	
	/**
	 * 通过id获取详细信息
	 * 
	 * @param request
	 * @param page
	 * @return
	 */
	@RequestMapping("queryDcStorageById")
	@ResponseBody
	public Object queryDcStorageById(@RequestBody Map<String,Object> requestMap)
			throws AppException{
		EayunResponseJson resp=new EayunResponseJson();
		
			String id=MapUtils.getString(requestMap, "storageId");
			
			resp.setData(storageservice.queryDcStorageById(id));

		log.info(JSONObject.toJSON(resp));// 这里使用阿里巴巴的json，可以得到anjularjs识别日期格式
		return resp;
		
	}	
	
	/**
	 * 创建存储
	 * 
	 * @param request
	 * @param page
	 * @return
	 */
	@SuppressWarnings("unchecked")
    @RequestMapping("queryDcStorageCreate")
	@ResponseBody
	public Object queryDcStorageCreate(@RequestBody Map<String,Object> requestMap)
			throws AppException{
		EayunResponseJson respJson=new EayunResponseJson();
		
			BaseEcmcSysUser user=EcmcSessionUtil.getUser();
			
			Map<String,Object> map=(Map<String,Object>)requestMap.get("storagemodel");
			String state = MapUtils.getString(map, "state");
			BaseDcStorage model=new BaseDcStorage();
			BeanUtils.mapToBean(model,map );
			try{
			if(user!=null){
				storageservice.queryDcStorageCreate(model,state,user.getAccount());
			}else{
				storageservice.queryDcStorageCreate(model,state,"");
			}
			
			respJson.setMessage("存储添加成功");
        	respJson.setRespCode(ConstantClazz.SUCCESS_CODE);
        	ecmcLogService.addLog("新增存储", "存储", model.getName(), null, 1, model.getId(), null);
			}catch(Exception e){
				ecmcLogService.addLog("新增存储", "存储", model.getName(), null, 0, model.getId(), e);
				throw new AppException("新增存储异常",e);
			}
	
		return respJson;
	}
	
	/**
	 * 编辑存储
	 * 
	 * @param request
	 * @param page
	 * @return
	 */
	@SuppressWarnings("unchecked")
    @RequestMapping("queryDcStorageUpdate")
	@ResponseBody
	public Object queryDcStorageUpdate(@RequestBody Map<String,Object> requestMap)
			throws AppException{
		EayunResponseJson respJson=new EayunResponseJson();
		
			Map<String,Object>map=(Map<String,Object>)requestMap.get("storagemodel");
			String state = MapUtils.getString(map, "state");
			BaseDcStorage model=new BaseDcStorage();
			BeanUtils.mapToBean(model,map );
			try{
			storageservice.queryDcStorageUpdate(model,state);
			respJson.setMessage("存储编辑成功");
			respJson.setRespCode(ConstantClazz.SUCCESS_CODE);
			ecmcLogService.addLog("更新存储", "存储", model.getName(), null, 1, model.getId(), null);
			}catch(Exception e){
				ecmcLogService.addLog("更新存储", "存储", model.getName(), null, 0, model.getId(), e);
				throw new AppException("更新存储异常",e);
			}
	
		return respJson;
	}
	/**
	 * 删除存储
	 * 
	 * @param request
	 * @param page
	 * @return
	 */
	@RequestMapping("queryDcStorageDel")
	@ResponseBody
	public Object queryDcStorageDel(@RequestBody Map<String,Object> requestMap)throws AppException{
		EayunResponseJson delJson =new EayunResponseJson();
		
			String id=MapUtils.getString(requestMap, "storageId");
			DcStorageVOE model=storageservice.queryDcStorageById(id);
			try{
			storageservice.queryDcStorageDel(id);
			
        	delJson.setRespCode(ConstantClazz.SUCCESS_CODE);
        	ecmcLogService.addLog("删除存储", "存储", model.getName(), null, 1, id, null);
			}catch(Exception e){
				ecmcLogService.addLog("删除存储", "存储", model.getName(), null, 0, id, e);
				throw new AppException("删除存储异常",e);
			}

		return delJson;
	}

	
	/**
	 * 添加时判断存储名称是否存才
	 * */
	@RequestMapping("/checkNameExist")
	@ResponseBody
	public Object checkNameExist(@RequestBody Map<String,Object> requestMap)throws AppException{
		EayunResponseJson respJson=new EayunResponseJson();
	
		   String name=MapUtils.getString(requestMap, "storageName");
		   String dataCenterId=MapUtils.getString(requestMap, "dataCenterId");
		   List<BaseDcStorage> list=storageservice.checkNameExist(name, dataCenterId);
		  
		   if(list.size()>0){
			   respJson.setData(false);
		   }else{
			   respJson.setData(true);
		   }
		 

		return respJson;
	}
	
	/**
	 * 修改时判断存储名称是否存才
	 * */
	@RequestMapping("/checkNameExistOfEdit")
	@ResponseBody
	public Object checkNameExistOfEdit(@RequestBody Map<String,Object> requestMap)throws AppException{
		EayunResponseJson respJson=new EayunResponseJson();
		   String name = MapUtils.getString(requestMap, "storageName");
		   String id = MapUtils.getString(requestMap, "storageId");
		   String dataCenterId = MapUtils.getString(requestMap, "dataCenterId");
 		   List<BaseDcStorage> list=  storageservice.checkNameExistOfEdit(name, id, dataCenterId);
		   if(list.size()>0){
			   respJson.setData(false);
		   }else{
			   respJson.setData(true);
		   }

		return respJson;
	}
}
