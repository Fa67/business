package com.eayun.virtualization.ecmccontroller;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

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
import com.eayun.common.dao.ParamsMap;
import com.eayun.common.dao.QueryMap;
import com.eayun.common.dao.support.Page;
import com.eayun.common.exception.AppException;
import com.eayun.common.filter.SessionUserInfo;
import com.eayun.common.model.EayunResponseJson;
import com.eayun.ecmcuser.model.BaseEcmcSysUser;
import com.eayun.ecmcuser.util.EcmcSessionUtil;
import com.eayun.log.ecmcsevice.EcmcLogService;
import com.eayun.virtualization.ecmcservice.EcmcCloudVolumeService;
import com.eayun.virtualization.model.BaseCloudVolume;
import com.eayun.virtualization.model.CloudVolume;

/**
 * @author
 *
 */
@Controller
@RequestMapping("/ecmc/cloud/volume")
@Scope("prototype")
public class EcmcCloudVolumeController {
	
	private static final Logger log = LoggerFactory.getLogger(EcmcCloudVolumeController.class);
	
	@Autowired
	private EcmcCloudVolumeService ecmcCloudVolumeService;
	@Autowired
	private EcmcLogService ecmcLogService;
	
	
	/**
	 * 查询云硬盘列表
	 * 
	 * @author Chengxiaodong
	 * @param request
	 * @param datacenterId
	 * @param projectId
	 * @param name
	 * @param page
	 * @return
	 * @throws Exception 
	 */
	@RequestMapping(value = "/getvolumelist", method = RequestMethod.POST)
	@ResponseBody
	public String getVolumeList(HttpServletRequest request, Page page,
			@RequestBody ParamsMap map) throws Exception{
		try {
			log.info("查询云硬盘列表开始");
			String prjId =map.getParams().get("prjId")!=null? map.getParams().get("prjId").toString():null;
			String dcId = map.getParams().get("dcId")!=null?map.getParams().get("dcId").toString():null;
			String queryName = map.getParams().get("queryName")!=null?map.getParams().get("queryName").toString():null;
			String queryType = map.getParams().get("queryType")!=null?map.getParams().get("queryType").toString():null;
			int pageSize = map.getPageSize();
			int pageNumber = map.getPageNumber();

			QueryMap queryMap = new QueryMap();
			queryMap.setPageNum(pageNumber);
			queryMap.setCURRENT_ROWS_SIZE(pageSize);
			page = ecmcCloudVolumeService.getVolumeList(page, prjId, dcId,
					queryName, queryType, queryMap);
		} catch (Exception e) {
			log.error(e.toString(),e);
			throw e;
		}
		return JSONObject.toJSONString(page);

	}

	/**
	 * 查询云硬盘详情
	 * 
	 * @author Chengxiaodong
	 * @param request
	 * @param map
	 * @return
	 * @throws Exception 
	 */
	@RequestMapping(value = "/getvolumebyid", method = RequestMethod.POST)
	@ResponseBody
	public String getVolumeById(HttpServletRequest request, @RequestBody Map<String,String> map) throws Exception {
		CloudVolume volume = null;
		EayunResponseJson json = new EayunResponseJson();
		try {
			log.info("查询云硬盘详情开始");
			String volId = map.get("volId")!=null?map.get("volId").toString():null;
			volume = ecmcCloudVolumeService.getVolumeById(volId);
			if(volume != null) {
				json.setData(volume);
				json.setRespCode(ConstantClazz.SUCCESS_CODE);
			}else {
				json.setRespCode(ConstantClazz.ERROR_CODE);
			}
		} catch (Exception e) {
			json.setMessage(e.getMessage());
			log.error(e.toString(),e);
			throw e;
		}
		return JSONObject.toJSONString(json);

	}
	
	
	/**
     * 验证重名
     * @author Chengxiaodong
     * @param request
     * @param map
     * @return
	 * @throws Exception 
     */
    @RequestMapping(value= "/getvolumebyname" , method = RequestMethod.POST)
    @ResponseBody
    public String getVolumeByName(HttpServletRequest request,@RequestBody Map<String,String> map) throws Exception{
		boolean isTrue=false;
		EayunResponseJson json = new EayunResponseJson();
    	try{
    		log.info("验证云硬盘重名开始");
    		isTrue=ecmcCloudVolumeService.getVolumeByName(map);
    		json.setData(isTrue);
			json.setRespCode(ConstantClazz.SUCCESS_CODE);
    	}catch(Exception e){
    		json.setRespCode(ConstantClazz.ERROR_CODE);
    		throw e;
    	}
		return JSONObject.toJSONString(json);
    	
    }
    
    
    /**
     * 创建云硬盘
     * @author Chengxiaodong
     * @param request
     * @param map
     * @return
     */
    @RequestMapping(value= "/addvolume" , method = RequestMethod.POST)
    @ResponseBody
    public String addVolume(HttpServletRequest request,@RequestBody CloudVolume volume){
	    	BaseCloudVolume vol=null;
	    	BaseEcmcSysUser user = EcmcSessionUtil.getUser();
	    	EayunResponseJson json = new EayunResponseJson();
    	try{
    		log.info("创建云硬盘开始");
	  	  	String addName =user.getAccount();
	        String dcId=volume.getDcId();
	        String prjId=volume.getPrjId();
	        String from=volume.getDiskFrom();
	        String volName=volume.getVolName();
	        int size=volume.getVolSize();
	        String description=volume.getVolDescription();
            vol=ecmcCloudVolumeService.createVolume(dcId,prjId,addName,from,volName,size,description);
            json.setData(vol);
			json.setRespCode(ConstantClazz.SUCCESS_CODE);
			ecmcLogService.addLog("创建云硬盘", ConstantClazz.LOG_TYPE_DISK, volName, prjId, 1, vol.getVolId(), null);
    	}catch(AppException e){
    		json.setRespCode(ConstantClazz.ERROR_CODE);
    		ecmcLogService.addLog("创建云硬盘", ConstantClazz.LOG_TYPE_DISK, volume.getVolName(), volume.getPrjId(), 0, volume.getVolId(), e);
            throw e;
    	}
        return JSONObject.toJSONString(json);
    }
    
	
	
	
	/**
     * 删除云硬盘
     * @author Chengxiaodong
     * @param request
     * @param map
     * @return
     */
    @RequestMapping(value= "/deletevolume" , method = RequestMethod.POST)
    @ResponseBody
    public String deleteVolume(HttpServletRequest request,@RequestBody CloudVolume vol){
    	boolean isTrue=false;
    	BaseEcmcSysUser user = EcmcSessionUtil.getUser();
    	EayunResponseJson json = new EayunResponseJson();
    	String busiName = null;
    	try{
    	  log.info("删除云硬盘开始");
    	  if("2".equals(vol.getIsDeleted())){
    		  busiName = "删除云硬盘";
          }else{
        	  busiName = "销毁云硬盘";
          }
          isTrue=ecmcCloudVolumeService.deleteVolume(vol,user);
          json.setData(isTrue);
		  json.setRespCode(ConstantClazz.SUCCESS_CODE);
		  ecmcLogService.addLog(busiName, ConstantClazz.LOG_TYPE_DISK, vol.getVolName(), vol.getPrjId(), 1, vol.getVolId(), null);
    	}catch(AppException e){
    	  json.setRespCode(ConstantClazz.ERROR_CODE);
    	  ecmcLogService.addLog(busiName, ConstantClazz.LOG_TYPE_DISK, vol.getVolName(), vol.getPrjId(), 0, vol.getVolId(), e);
    	  throw e;
    	}
    	return JSONObject.toJSONString(json);
    	
    }
    
    
    
    /**
     * 编辑云硬盘
     * @author chengxiaodong
     * @param request
     * @param map
     * @return
     */
    @RequestMapping(value= "/updatevolume" , method = RequestMethod.POST)
    @ResponseBody
     public String updataVolume(HttpServletRequest request,@RequestBody CloudVolume vol)throws AppException{
    	boolean isTrue=false;
    	EayunResponseJson json = new EayunResponseJson();
    	 try{
    		 log.info("编辑云硬盘开始");
        	 isTrue=ecmcCloudVolumeService.updateVolume(vol);
        	 json.setData(isTrue);
   		     json.setRespCode(ConstantClazz.SUCCESS_CODE);
   		     ecmcLogService.addLog("编辑云硬盘", ConstantClazz.LOG_TYPE_DISK, vol.getVolName(), vol.getPrjId(), 1, vol.getVolId(), null);
    	 }catch(AppException e){
    		 json.setRespCode(ConstantClazz.ERROR_CODE);
    		 ecmcLogService.addLog("编辑云硬盘", ConstantClazz.LOG_TYPE_DISK, vol.getVolName(), vol.getPrjId(), 0, vol.getVolId(), e);
    		 throw e;
    	 }
	 return JSONObject.toJSONString(json);
    	 
     }
    
    
    
    /**
     * 挂载云硬盘
     * @author Chengxiaodong
     * @param request
     * @param map
     * @return
     */
    @RequestMapping(value= "/bindvolume" , method = RequestMethod.POST)
    @ResponseBody
     public String bindVolume(HttpServletRequest request,@RequestBody Map<String,String> map)throws AppException{
    	boolean isTrue=false;
    	EayunResponseJson json = new EayunResponseJson();
    	 try{
    		 log.info("挂载云硬盘开始");
    		 String dcId=map.get("dcId").toString();
    		 String prjId=map.get("prjId").toString();
    		 String vmId=map.get("vmId").toString();
    		 String volId=map.get("volId").toString();
        	 isTrue=ecmcCloudVolumeService.bindVolume(dcId,prjId,vmId,volId);
        	 json.setData(isTrue);
   		     json.setRespCode(ConstantClazz.SUCCESS_CODE);
   		     ecmcLogService.addLog("挂载云硬盘", ConstantClazz.LOG_TYPE_DISK, map.get("volName")!=null?map.get("volName").toString():null, prjId, 1, volId, null);
    	 }catch(AppException e){
    		 json.setRespCode(ConstantClazz.ERROR_CODE);
    		 ecmcLogService.addLog("挂载云硬盘", ConstantClazz.LOG_TYPE_DISK, map.get("volName")!=null?map.get("volName").toString():null, map.get("prjId")!=null?map.get("prjId").toString():null, 0, map.get("volId")!=null?map.get("volId").toString():null, e);
    		 throw e;
    	 }
	 return JSONObject.toJSONString(json);
    	 
     }
    
    
    
    /**
     * 解绑云硬盘
     * @author Chengxiaodong
     * @param request
     * @param map
     * @return
     */
    @RequestMapping(value= "/debindvolume" , method = RequestMethod.POST)
    @ResponseBody
     public String debindVolume(HttpServletRequest request,@RequestBody Map<String,String> map)throws AppException{
    	boolean isTrue=false;
    	EayunResponseJson json = new EayunResponseJson();
    	 try{
    		 log.info("解绑云硬盘开始");
    		 String dcId=map.get("dcId").toString();
    		 String prjId=map.get("prjId").toString();
    		 String vmId=map.get("vmId").toString();
    		 String volId=map.get("volId").toString();
        	 isTrue=ecmcCloudVolumeService.debindVolume(dcId,prjId,vmId,volId);
        	 json.setData(isTrue);
   		     json.setRespCode(ConstantClazz.SUCCESS_CODE);
   		  ecmcLogService.addLog("解绑云硬盘", ConstantClazz.LOG_TYPE_DISK, map.get("volName")!=null?map.get("volName").toString():null, prjId, 1, volId, null);
    	 }catch(AppException e){
    		 json.setRespCode(ConstantClazz.ERROR_CODE);
    		 ecmcLogService.addLog("解绑云硬盘", ConstantClazz.LOG_TYPE_DISK, map.get("volName")!=null?map.get("volName").toString():null, map.get("prjId")!=null?map.get("prjId").toString():null, 0, map.get("volId")!=null?map.get("volId").toString():null, e);
    		 throw e;
    	 }
	 return JSONObject.toJSONString(json);
    	 
     }
    
    
    /**
     * 查询指定云主机绑定的云硬盘
     * @param request
     * @param vmId
     * @return
     * @throws AppException
     */
    @RequestMapping(value= "/queryvolumesbyvm" , method = RequestMethod.POST)
    @ResponseBody
     public String queryVolumesByVm(HttpServletRequest request,@RequestBody Map<String,String> map)throws Exception{
    	String vmId=map.get("vmId");
    	EayunResponseJson json = new EayunResponseJson();
    	List<CloudVolume> volList = new ArrayList<CloudVolume>();
    	log.info("查询指定云主机绑定的云硬盘列表");
    	try{
    		volList = ecmcCloudVolumeService.queryVolumesByVm(vmId);
    		json.setData(volList);
  		    json.setRespCode(ConstantClazz.SUCCESS_CODE);
    	}catch(Exception e){
    		json.setRespCode(ConstantClazz.ERROR_CODE);
    		log.error(e.toString(),e);
    		throw e;
    	}
    	return JSONObject.toJSONString(json);
    }
    
    
    
    /**
     * 根据与主机Id查询其下云硬盘个数
     * @param request
     * @param vmId
     * @return
     * @throws Exception
     */
    @RequestMapping(value= "/queryvolumecountbyvmid" , method = RequestMethod.POST)
    @ResponseBody
     public String getCountByVnId(HttpServletRequest request,@RequestBody Map<String,String> map)throws Exception{
    	log.info("查询指定云主机绑定的云硬盘个数");
    	EayunResponseJson json = new EayunResponseJson();
    	int volCount=0;
    	try{
    		String vmId=map.get("vmId").toString();
    		volCount = ecmcCloudVolumeService.getCountByVnId(vmId);
    		json.setData(volCount);
  		    json.setRespCode(ConstantClazz.SUCCESS_CODE);
    	}catch(Exception e){
    		json.setRespCode(ConstantClazz.ERROR_CODE);
    		log.error(e.toString(),e);
    		throw e;
    	}
    	return JSONObject.toJSONString(json);
    }
    
    
    /**
	 * 查询当前项目下未挂载的数据盘列表
	 * 用于挂载云硬盘
	 * @Author: duanbinbin
	 * @param request
	 * @param cloudVm
	 * @return
	 *<li>Date: 2016年4月22日</li>
	 */
	@RequestMapping(value = "/getunbinddisk" , method = RequestMethod.POST)
    @ResponseBody
    public String  getUnBindDisk(HttpServletRequest request,@RequestBody Map map){
    	log.info("查询当前项目下未挂载的数据盘开始");
    	EayunResponseJson json = new EayunResponseJson();
    	try {
    		String prjId=map.get("prjId").toString();
			List<CloudVolume> volList = ecmcCloudVolumeService.getUnBindDisk(prjId);
			json.setData(volList);
			json.setRespCode(ConstantClazz.SUCCESS_CODE);
		} catch (Exception e) {
	    	json.setRespCode(ConstantClazz.ERROR_CODE);
			log.error(e.toString(),e);
	    	throw e;
		}
    	return JSONObject.toJSONString(json);
    	
    }
	/**
     * 查询当前登录用户下回收站的volume
     * ---------------------
     * @author chengxiaodong
     * @param request
     * @param page
     * @param map 请求参数
     * @return
     */
    @RequestMapping(value= "/getRecycleVolList" , method = RequestMethod.POST)
    @ResponseBody
    public String getRecycleVolList(HttpServletRequest request,Page page,@RequestBody ParamsMap map) throws Exception{
    	try {
    		BaseEcmcSysUser user = EcmcSessionUtil.getUser();
    		int pageSize = map.getPageSize();
    		int pageNumber = map.getPageNumber();
    		
    		QueryMap queryMap=new QueryMap();
    		queryMap.setPageNum(pageNumber);
    		queryMap.setCURRENT_ROWS_SIZE(pageSize);
    		
    		page=ecmcCloudVolumeService.getRecycleVolList(page,map,user,queryMap);
    	} catch (Exception e) {
    		throw e;
    	}
    	return JSONObject.toJSONString(page);
    	
    }
	
}

