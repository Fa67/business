package com.eayun.datacenter.ecmccontroller;

import java.util.List;
import java.util.Map;

import org.apache.commons.collections.MapUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
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
import com.eayun.common.sync.SyncProgressUtil;
import com.eayun.common.util.BeanUtils;
import com.eayun.datacenter.ecmcservice.EcmcDataCenterService;
import com.eayun.datacenter.model.BaseDcDataCenter;
import com.eayun.ecmcuser.model.BaseEcmcSysUser;
import com.eayun.ecmcuser.util.EcmcSessionUtil;
import com.eayun.log.ecmcsevice.EcmcLogService;
import com.eayun.schedule.service.DatacenterSyncService;


@Controller
@RequestMapping("/ecmc/physical/datacenter")
@Scope("prototype")
public class EcmcDataCenterController {
	private final Log log = LogFactory.getLog(this.getClass());
	
	@Autowired
	private EcmcDataCenterService ecmcDataCenterService;
	
	@Autowired
	private DatacenterSyncService datacenterSyncService;
	
	@Autowired
	private EcmcLogService ecmcLogService;
	
	@Autowired
	private SyncProgressUtil syncProgressUtil;
	/**
	 * 获取数据中心
	 * */
	@SuppressWarnings("unchecked")
    @RequestMapping("/querydatacenter")
	@ResponseBody
	public Object getDataCenterList(@RequestBody Map<String,Object> requestMap)throws AppException{
		
		
		Page page=null;
		try{
			
			Map<String,Object> map=(Map<String,Object>)requestMap.get("params");
			String dataCenterName=MapUtils.getString(map, "dcName");
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
            page=ecmcDataCenterService.query(dataCenterName, queryMap);
			}
		catch(AppException e){
			log.error(e,e);
		}
		 return page;
		
		
	}
	
	
	/**
	 * 获取数据中心详情(修改前查询)*/
	@RequestMapping("/querybyid")
	@ResponseBody
	public Object getdatacenterbyid(@RequestBody Map<String,Object> requestMap)throws AppException{
		EayunResponseJson resp=new EayunResponseJson();
		
			String id=MapUtils.getString(requestMap, "dataCenterId");
			resp.setRespCode(ConstantClazz.SUCCESS_CODE);
			
			resp.setData(ecmcDataCenterService.querybyid(id));
		
		
		 return resp;
		
		
	}
	
	/**添加数据中心校验数据中心链接
	 * 
	 */
	@SuppressWarnings("unchecked")
    @RequestMapping("/createdatacenter")
	@ResponseBody
	public Object createDataCenter(@RequestBody Map<String,Object> requestMap)throws AppException{
		EayunResponseJson resp=new EayunResponseJson();
		
		boolean flag=false;
		String msg="";
		//try {
			
			
			BaseDcDataCenter model=new BaseDcDataCenter();
			BeanUtils.mapToBean(model,(Map<String,Object>)requestMap.get("datacentermodel"));
			 Map<String,Object> map=(Map<String,Object>)requestMap.get("datacentermodel");
				model.setVCenterUsername(map.get("vcenterUsername").toString()); 
				model.setVCenterPassword(map.get("vcenterPassword").toString()); 
			//String state =MapUtils.getString(requestMap, "state");
			flag = ecmcDataCenterService.checkDataCenterLinked(model);
			if(!flag){
				msg="数据中心链接校验失败，请确认数据中心配置是否正确！";
			}
			if(flag){
				
				BaseEcmcSysUser user=EcmcSessionUtil.getUser();
				if(user!=null){
					
					try{
						ecmcDataCenterService.add(model,user.getAccount());
						resp.setRespCode(ConstantClazz.SUCCESS_CODE);
						msg="数据中创建成功！";
						ecmcLogService.addLog("新增数据中心", "数据中心", model.getName(), null, 1, model.getId(), null);
						}catch(Exception e){
						    log.error(e.getMessage(),e);
							resp.setRespCode(ConstantClazz.ERROR_CODE);
							msg="数据中创建失败！";
							ecmcLogService.addLog("新增数据中心", "数据中心", model.getName(), null, 0, model.getId(), e);
						}
					
				}else{
					try{
						ecmcDataCenterService.add(model,"");
						resp.setRespCode(ConstantClazz.SUCCESS_CODE);
						msg="数据中创建成功！";
						ecmcLogService.addLog("新增数据中心", "数据中心", model.getName(), null, 1, model.getId(), null);
					}catch(Exception e){
					    log.error(e.getMessage(),e);
						resp.setRespCode(ConstantClazz.ERROR_CODE);
						msg="数据中创建失败！";
						ecmcLogService.addLog("新增数据中心", "数据中心", model.getName(), null, 0, model.getId(), e);
					}
					
				}
				
				
			}
			
			resp.setData(flag);
			resp.setMessage(msg);
		
		return resp;
		
	}

	
	/**
	 * 添加时判断数据中心名称是否存才
	 * */
	@RequestMapping("/checkNameExist")
	@ResponseBody
	public Object checkNameExist(@RequestBody Map<String,Object> requestMap)throws AppException{
		EayunResponseJson resp=new EayunResponseJson();
		
		 String name=MapUtils.getString(requestMap, "dcName");
		  
		 resp.setData(ecmcDataCenterService.checkNameExist(name));
		
		return resp;
	}
	
	/**
	 * 修改时判断数据中心名称是否存才
	 * */
	@RequestMapping("/checkNameExistOfEdit")
	@ResponseBody
	public Object checkNameExistOfEdit(@RequestBody Map<String,Object> requestMap)throws AppException{
		EayunResponseJson resp=new EayunResponseJson();
		
		   String name = MapUtils.getString(requestMap, "dcName");//数据中心名称
		   String id = MapUtils.getString(requestMap, "dataCenterId");
		   resp.setData(ecmcDataCenterService.checkNameExist(name, id));
		
		return resp;
	}
	
	/**
	 * 数据中心已经容纳机柜的数量
	 * @param request
	 * @return
	 */
	@RequestMapping("/checkCabinetNum")
	@ResponseBody
	public Object checkCabinetNum(@RequestBody Map<String,Object> requestMap)throws AppException{
		EayunResponseJson resp=new EayunResponseJson();
		
			String id=MapUtils.getString(requestMap, "dataCenterId");//数据中心id
		
			
			int count=ecmcDataCenterService.queryDatacentercabinetNum(id);
			resp.setData(count);
			return resp;
		
	}

	/**
	 * 判断数据中心是否可用
	 * @param request
	 * @return
	 * @throws AppException
	 */
	@SuppressWarnings("unchecked")
    @RequestMapping("/updatedatacenter")
	@ResponseBody
	public Object checkDataCenterCanUse(@RequestBody Map<String,Object> requestMap)throws AppException{
		EayunResponseJson resp=new EayunResponseJson();
		BaseDcDataCenter model=new BaseDcDataCenter();
		BeanUtils.mapToBean(model,(Map<String,Object>)requestMap.get("datacentermodel"));
		 Map<String,Object> map=(Map<String,Object>)requestMap.get("datacentermodel");
		model.setVCenterUsername(map.get("vcenterUsername").toString()); 
		model.setVCenterPassword(map.get("vcenterPassword").toString()); 
		
		
			
				boolean fag=ecmcDataCenterService.checkDataCenterLinkeddcid(model);
				if(!fag){
					resp.setMessage("数据中心链接校验失败，请确认数据中心配置是否正确！");
					resp.setRespCode(ConstantClazz.ERROR_CODE);
					ecmcLogService.addLog("更新数据中心", "数据中心", model.getName(), null, 0, model.getId(), null);
					return resp;
				}
				resp.setMessage("修改成功");
				resp.setRespCode(ConstantClazz.SUCCESS_CODE);
				ecmcLogService.addLog("更新数据中心", "数据中心", model.getName(), null, 1, model.getId(), null);
			
				
				
			
			
			//resp.setData(result);
		
		return resp;
		
		}
	
	
	
	/**
	 * 添加时判断数据中心管理节点IP是否存才
	 * */
	@SuppressWarnings("rawtypes")
    @RequestMapping("/checkDcAddressExist")
	@ResponseBody
	public Object checkDcAddressExist(@RequestBody Map<String,Object> requestMap)throws AppException{
		EayunResponseJson resp=new EayunResponseJson();
		
		   String ip=MapUtils.getString(requestMap, "ipAddress");
		 
		   List list=ecmcDataCenterService.queryip(ip);
		   if(list.size()==0){
			   resp.setData(true);
		   }else{
			   resp.setData(false);
		   }
		
		return resp;
	}
	
	
	/**
	 * 删除数据中心
	 * */
	@RequestMapping("/deletedatacenter")
	@ResponseBody
	public Object deletedatacenter(@RequestBody Map<String,Object> requestMap)throws AppException{
		EayunResponseJson resp=new EayunResponseJson();
		
			String dataCenterId=MapUtils.getString(requestMap, "dataCenterId");
			
			try{
				ecmcDataCenterService.delete(dataCenterId);
			ecmcLogService.addLog("删除数据中心", "数据中心", "数据中心", null, 1, dataCenterId, null);
			}catch(Exception e){
			    log.error(e.getMessage(),e);
				ecmcLogService.addLog("删除数据中心", "数据中心", "数据中心", null, 0, dataCenterId, e);
			}
			resp.setRespCode(ConstantClazz.SUCCESS_CODE);
		
		
		return resp;
	}
	
	/**
	 * 检查数据中心是否可以删除
	 * */
	@RequestMapping("/checkcannotdelete")
	@ResponseBody
	public Object checkCannotDelete(@RequestBody Map<String,Object> requestMap)throws AppException{
		EayunResponseJson resp=new EayunResponseJson();
	
			String dataCenterId=MapUtils.getString(requestMap, "dataCenterId");
		boolean fag=	ecmcDataCenterService.checkDataCenterRemoveCannot(dataCenterId);
		resp.setRespCode(ConstantClazz.SUCCESS_CODE);
		resp.setData(fag);
		if(!fag){
			resp.setData(fag);
			resp.setRespCode(ConstantClazz.ERROR_CODE);
			resp.setMessage("该数据中心下面存在机柜，不能删除！");
		}
		
		
		return resp;
	}
	
	
	
	
	/**
	 * 获取所有数据中心列表
	 * */
	@RequestMapping("/getlistdatacenter")
	@ResponseBody
	public Object getAllListDataCenter()throws AppException{
		EayunResponseJson resp=new EayunResponseJson();
		
			resp.setData(ecmcDataCenterService.getAllList());
			return resp;
			
		
		
	}
	
	
	/**
	 * 同步数据中心
	 * 
	 * */
	@RequestMapping("/syndatacenter")
	@ResponseBody
	public Object syndatacenter(@RequestBody Map<String,Object> requestMap)throws AppException{
		EayunResponseJson resp=new EayunResponseJson();
		final String dataCenterId=MapUtils.getString(requestMap, "dataCenterId");
		try{
//    		datacenterSyncService.syncDatacenter(dataCenterId);
		    if(!syncProgressUtil.isSyncing(dataCenterId)){
		        new Thread(new Runnable() {
	                public void run() {
	                    datacenterSyncService.syncDatacenter(dataCenterId);
	                }
	            }).start();
		    }
		    Object syncProgress = syncProgressUtil.getSyncProgress(dataCenterId);
    		resp.setRespCode(ConstantClazz.SUCCESS_CODE);
    		resp.setData(syncProgress);
    		resp.setMessage("同步数据中心成功");
    		ecmcLogService.addLog("同步数据中心", "数据中心", "数据中心", null, 1, dataCenterId, null);
		
		}catch(Exception e){
			
			resp.setRespCode(ConstantClazz.ERROR_CODE);
			resp.setMessage("同步数据中心失败");
			ecmcLogService.addLog("同步数据中心", "数据中心", "数据中心", null, 0, dataCenterId, e);
			throw e;
		}
		return resp;
	}
	
	/**
	 * 获取同步进度
	 * @param requestMap
	 * @return
	 * @throws AppException
	 */
	@RequestMapping("/getsyncprogress")
    @ResponseBody
    public Object getSyncProgress(@RequestBody Map<String,Object> requestMap)throws AppException{
        EayunResponseJson resp=new EayunResponseJson();
        String dcId=MapUtils.getString(requestMap, "dcId");
        try{
            Object syncProgress = syncProgressUtil.getSyncProgress(dcId);
            resp.setRespCode(ConstantClazz.SUCCESS_CODE);
            resp.setData(syncProgress);
        }catch(Exception e){
            resp.setRespCode(ConstantClazz.ERROR_CODE);
            log.error(e.getMessage(), e);
        }
        return resp;
    }
	
	@RequestMapping("/checkapidccode")
	@ResponseBody
	public String checkApiDcCode(@RequestBody Map map)throws AppException{
		log.info("校验数据中心Region标识是否唯一");
		EayunResponseJson resp=new EayunResponseJson();
		String apiDcCode = null == map.get("apiDcCode")?"":map.get("apiDcCode").toString();
		String dcId = null == map.get("dcId")?"":map.get("dcId").toString();
		try{
			boolean isok = ecmcDataCenterService.checkApiDcCode(apiDcCode,dcId);
			resp.setData(isok);
			resp.setRespCode(ConstantClazz.SUCCESS_CODE);
		}catch(Exception e){
			resp.setRespCode(ConstantClazz.ERROR_CODE);
		}
		return JSONObject.toJSONString(resp);
	}
}
	
	
