package com.eayun.virtualization.controller;

import java.util.ArrayList;
import java.util.List;
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
import com.eayun.virtualization.model.CloudOrderVolume;
import com.eayun.virtualization.model.CloudProject;
import com.eayun.virtualization.model.CloudVolume;
import com.eayun.virtualization.model.CloudVolumeType;
import com.eayun.virtualization.service.VolumeService;
import com.eayun.virtualization.service.VolumeTypeService;

@Controller
@RequestMapping("/cloud/volume")
@Scope("prototype")
public class VolumeController extends BaseController {
    private static final Logger log = LoggerFactory.getLogger(VolumeController.class);
    @Autowired
    private VolumeService diskService;  
    @Autowired
    private LogService logService;
    @Autowired
    private VolumeTypeService volumeTypeService;
   


   /**查询云硬盘列表
    * @author Chengxiaodong
    * @param request
    * @param datacenterId
    * @param projectId
    * @param name
    * @param page
    * @return 
    */
   
    @RequestMapping(value= "/getVolumeList" , method = RequestMethod.POST)
    @ResponseBody
    public String getVolumeList(HttpServletRequest request,Page page,@RequestBody ParamsMap map){
        try {
        	log.info("查询云硬盘列表开始");
            String prjId = map.getParams().get("prjId").toString();
            String dcId=map.getParams().get("dcId").toString();
            String queryName=map.getParams().get("name").toString();
            String queryType=map.getParams().get("queryType").toString();
            String isDeleted=map.getParams().get("isDeleted").toString();
            String volStatus = map.getParams().get("status") != null ? "" + map.getParams().get("status") : "";
            int pageSize = map.getPageSize();
            int pageNumber = map.getPageNumber();
            
            QueryMap queryMap=new QueryMap();
            queryMap.setPageNum(pageNumber);
            queryMap.setCURRENT_ROWS_SIZE(pageSize);
            page=diskService.getVolumeList(page,prjId,dcId,queryName,queryType,isDeleted,volStatus,queryMap);
        } catch (Exception e) {
             log.error(e.getMessage(),e);
        }
        return JSONObject.toJSONString(page);
       
    }
    

    
    /**
     * 查询云硬盘详情
     * @author Chengxiaodong
     * @param request
     * @param map
     * @return
     */
    @RequestMapping(value= "/getVolumeById" , method = RequestMethod.POST)
    @ResponseBody
    public String getVolumeById(HttpServletRequest request,@RequestBody Map map){
    	CloudVolume volume=null;
    	try{
    		log.info("查询云硬盘详情开始");
    		String dcId=map.get("dcId")!=null?map.get("dcId").toString():"";
    		String prjId =map.get("prjId")!=null?map.get("prjId").toString():"";
    		String volId=map.get("volId").toString();
    		volume=diskService.getVolumeById(dcId,prjId,volId);
    	}catch(Exception e){
    		log.error(e.getMessage(),e);
    	}
    	return JSONObject.toJSONString(volume);
    	
    }
    
    
    /**
     * 验证重名
     * @author Chengxiaodong
     * @param request
     * @param map
     * @return
     * @throws Exception 
     */
    @RequestMapping(value= "/getVolumeByName" , method = RequestMethod.POST)
    @ResponseBody
    public String getVolumeByName(HttpServletRequest request,@RequestBody CloudVolume volume) throws Exception{
		boolean isTrue=false;
    	try{
    		log.info("验证云硬盘重名开始");
    		isTrue=diskService.getVolumeByName(volume);
    	}catch(Exception e){
    		throw e;
    	}
		return JSONObject.toJSONString(isTrue);
    	
    }
    
    
    
    
    /**
     * <p>购买云硬盘并提交<p>
     * 
     * @author chengxiaodong
     * 
     * @param request
     * @param orderVolume
     * @return
     * @throws Exception 
     */
    @RequestMapping(value= "/buyVolume" , method = RequestMethod.POST)
    @ResponseBody
    public String buyVolumes(HttpServletRequest request, @RequestBody CloudOrderVolume orderVolume) throws Exception{
    	log.info("购买云硬盘");
    	JSONObject json  = new JSONObject();
    	try{
    		SessionUserInfo sessionUser = (SessionUserInfo) request.getSession().getAttribute(ConstantClazz.SYS_SESSION_USERINFO);
    		String message=diskService.buyVolumes(orderVolume, sessionUser);
    		if(StringUtils.isEmpty(message)){
    			json.put("respCode", ConstantClazz.SUCCESS_CODE_ADD);
    			json.put("orderNo", orderVolume.getOrderNo());
    		}else{
    			json.put("respCode", ConstantClazz.WARNING_CODE);
        		json.put("message", message);
    		}
    		
    	}catch(Exception e){
    		if("余额不足".equals(e.getMessage())){
				json.put("respCode", ConstantClazz.WARNING_CODE);
    			json.put("message", "CHANGE_OF_BALANCE");
			}
    		else{
    			json.put("respCode", ConstantClazz.ERROR_CODE);
    			json.put("orderNo", orderVolume.getOrderNo());
    		}
			log.error(e.getMessage(),e);
    	}
    	return json.toJSONString();
    }
    
    
    
    
    
    /**
     * <p>扩容云硬盘并提交<p>
     * 
     * @author chengxiaodong
     * 
     * @param request
     * @param orderVolume
     * @return
     * @throws Exception 
     */
    @RequestMapping(value= "/extendVolume" , method = RequestMethod.POST)
    @ResponseBody
    public String extendVolumes(HttpServletRequest request, @RequestBody CloudOrderVolume orderVolume) throws Exception{
    	log.info("扩容云硬盘");
    	String errorMassage=null;
    	JSONObject json  = new JSONObject();
    	try{
    		SessionUserInfo sessionUser = (SessionUserInfo) request.getSession().getAttribute(ConstantClazz.SYS_SESSION_USERINFO);
    		errorMassage=diskService.extendVolume(orderVolume, sessionUser);
    		
    		if(StringUtils.isEmpty(errorMassage)){
    			json.put("respCode", ConstantClazz.SUCCESS_CODE_ADD);
        		json.put("orderNo", orderVolume.getOrderNo());
        		
        		logService.addLog("云硬盘扩容",ConstantClazz.LOG_TYPE_DISK,orderVolume.getVolName(),orderVolume.getPrjId(),ConstantClazz.LOG_STATU_SUCCESS,null);
    		}else{
    			json.put("respCode", ConstantClazz.WARNING_CODE);
        		json.put("message", errorMassage);
    		}
    		
    	}catch(Exception e){
    		if("余额不足".equals(e.getMessage())){
				json.put("respCode", ConstantClazz.WARNING_CODE);
    			json.put("message", "CHANGE_OF_BALANCE");
			}
    		else{
    			json.put("respCode", ConstantClazz.ERROR_CODE);
    			json.put("orderNo", orderVolume.getOrderNo());
    		}
        	log.error(e.getMessage(),e);
        	logService.addLog("云硬盘扩容",ConstantClazz.LOG_TYPE_DISK,orderVolume.getVolName(),orderVolume.getPrjId(),ConstantClazz.LOG_STATU_ERROR,e);
    	}
    	return json.toJSONString();
    }
    
    

    
    
    
    /**
     * 删除云硬盘
     * @author Chengxiaodong
     * @param request
     * @param map
     * @return
     */
    @RequestMapping(value= "/deleteVolume" , method = RequestMethod.POST)
    @ResponseBody
    public String deleteVolume(HttpServletRequest request,@RequestBody CloudVolume vol){
    	boolean isTrue=false;
    	String doName="";
    	try{
    	  log.info("删除云硬盘开始");
    	  if("2".equals(vol.getIsDeleted())){
  			doName="删除云硬盘";
          }else{
          	doName="销毁云硬盘";
          }
    	  SessionUserInfo sessionUser = (SessionUserInfo) request.getSession().getAttribute(ConstantClazz.SYS_SESSION_USERINFO);
          isTrue=diskService.deleteVolume(vol,sessionUser);
          logService.addLog(doName,ConstantClazz.LOG_TYPE_DISK,vol.getVolName(),vol.getPrjId(),ConstantClazz.LOG_STATU_SUCCESS,null);
    	}catch(AppException e){
    	    log.error(e.getMessage(),e);
    	   logService.addLog(doName,ConstantClazz.LOG_TYPE_DISK,vol.getVolName(),vol.getPrjId(),ConstantClazz.LOG_STATU_ERROR,e);
    	   throw e;
    	}
    	return JSONObject.toJSONString(isTrue);
    	
    }
    
    
    /**
     * 编辑云硬盘
     * @author chengxiaodong
     * @param request
     * @param map
     * @return
     */
    @RequestMapping(value= "/updateVolume" , method = RequestMethod.POST)
    @ResponseBody
     public String updataVolume(HttpServletRequest request,@RequestBody CloudVolume vol)throws AppException{
    	boolean isTrue=false;
    	 try{
    		 log.info("编辑云硬盘开始");
        	 isTrue=diskService.updateVolume(vol);
        	 logService.addLog("编辑云硬盘",ConstantClazz.LOG_TYPE_DISK,vol.getVolName(),vol.getPrjId(),ConstantClazz.LOG_STATU_SUCCESS,null);
    	 }catch(AppException e){
    	     log.error(e.getMessage(),e);
    		 logService.addLog("编辑云硬盘",ConstantClazz.LOG_TYPE_DISK,vol.getVolName(),vol.getPrjId(),ConstantClazz.LOG_STATU_ERROR,e);
    		 throw e;
    	 }
	 return JSONObject.toJSONString(isTrue);
    	 
     }
    
    
    /**
     * 挂载云硬盘
     * @author Chengxiaodong
     * @param request
     * @param map
     * @return
     */
    @RequestMapping(value= "/bindVolume" , method = RequestMethod.POST)
    @ResponseBody
     public String bindVolume(HttpServletRequest request,@RequestBody CloudVolume vol)throws AppException{
    	boolean isTrue=false;
    	 try{
    		 log.info("挂载云硬盘开始");
        	 isTrue=diskService.bindVolume(vol);
        	 logService.addLog("挂载云硬盘",ConstantClazz.LOG_TYPE_DISK,vol.getVolName(),vol.getPrjId(),ConstantClazz.LOG_STATU_SUCCESS,null);
    	 }catch(AppException e){
    	     log.error(e.getMessage(),e);
    		 logService.addLog("挂载云硬盘",ConstantClazz.LOG_TYPE_DISK,vol.getVolName(),vol.getPrjId(),ConstantClazz.LOG_STATU_ERROR,e);
    		 throw e;
    	 }
	 return JSONObject.toJSONString(isTrue);
    	 
     }
    
    
    /**
     * 解绑云硬盘
     * @author Chengxiaodong
     * @param request
     * @param map
     * @return
     */
    @RequestMapping(value= "/debindVolume" , method = RequestMethod.POST)
    @ResponseBody
     public String debindVolume(HttpServletRequest request,@RequestBody CloudVolume vol)throws AppException{
    	boolean isTrue=false;
    	 try{
    		 log.info("解绑云硬盘开始");
        	 isTrue=diskService.debindVolume(vol);
        	 logService.addLog("解绑云硬盘",ConstantClazz.LOG_TYPE_DISK,vol.getVolName(),vol.getPrjId(),ConstantClazz.LOG_STATU_SUCCESS,null);
    	 }catch(AppException e){
    	     log.error(e.getMessage(),e);
    		 logService.addLog("解绑云硬盘",ConstantClazz.LOG_TYPE_DISK,vol.getVolName(),vol.getPrjId(),ConstantClazz.LOG_STATU_ERROR,e);
    		 throw e;
    	 }
	 return JSONObject.toJSONString(isTrue);
    	 
     }
    
    /**
     * 
     * @param request
     * @param vmId
     * @return
     * @throws AppException
     */
    @RequestMapping(value= "/queryVolumesByVm" , method = RequestMethod.POST)
    @ResponseBody
     public String queryVolumesByVm(HttpServletRequest request,@RequestBody String vmId)throws AppException{
    	List<CloudVolume> volList = new ArrayList<CloudVolume>();
    	log.info("查询指定云主机绑定的云硬盘列表");
    	try{
    		volList = diskService.queryVolumesByVm(vmId);
    	}catch(Exception e){
    		log.error(e.getMessage(),e);
    		throw e;
    	}
    	return JSONObject.toJSONString(volList);
    }
    
    /**
     * 根据与主机Id查询其下云硬盘个数
     * @param request
     * @param vmId
     * @return
     * @throws Exception
     */
    @RequestMapping(value= "/queryVolumeCountByVmId" , method = RequestMethod.POST)
    @ResponseBody
     public String getCountByVnId(HttpServletRequest request,@RequestBody String vmId)throws Exception{
    	log.info("查询指定云主机绑定的云硬盘个数");
    	int volCount=0;
    	try{
    		volCount = diskService.getCountByVnId(vmId);
    	}catch(Exception e){
    		log.error(e.getMessage(),e);
    		throw e;
    	}
    	return JSONObject.toJSONString(volCount);
    }
    
    /**
     * 查询当前项目下未被使用的云硬盘
     * @param request
     * @param map
     * @return
     * @throws Exception 
     */
    @RequestMapping(value= "/getUnUsedVolumes" , method = RequestMethod.POST)
    @ResponseBody
    public String getUnUsedVolumes(HttpServletRequest request,@RequestBody Map<String,String> map) throws Exception{
    	List<CloudVolume> volumes=new ArrayList<CloudVolume>();
        try {
        	log.info("查询未使用云硬盘开始");
            String prjId = map.get("prjId").toString();
            String dcId=map.get("dcId").toString();
            volumes=diskService.getUnUsedVolumeList(prjId,dcId);
        } catch (Exception e) {
             log.error(e.getMessage(),e);
             throw e;
        }
        return JSONObject.toJSONString(volumes);
       
    }
    /**
     * 批量挂载云硬盘
     * @param request
     * @param map
     * @return
     * @throws Exception 
     */
    @RequestMapping(value= "/bindVolumes" , method = RequestMethod.POST)
    @ResponseBody
    public Object batchBindVolumes(HttpServletRequest request,@RequestBody Map<String,Object> map) throws Exception{
    	log.info("批量挂载云硬盘开始");
    	String vmId = (String) map.get("vmId");
    	String prjId = (String) map.get("prjId");
    	String dcId = (String) map.get("dcId");
		@SuppressWarnings("unchecked")
		List<Map<String,String>> disks = (List<Map<String,String>>)map.get("disks");
    	boolean isTrue=false;
    	int successCount = 0;
		EayunResponseJson res = new EayunResponseJson();
		try{
			for(Map<String,String> disk:disks){
				CloudVolume vol = new CloudVolume();
				try{
					vol.setVmId(vmId);
					vol.setPrjId(prjId);
					vol.setDcId(dcId);
					vol.setVolId(disk.get("volId"));
					vol.setVolName(disk.get("volName"));
					isTrue = diskService.bindVolume(vol);
					if(isTrue){
						successCount++;
					}
					logService.addLog("挂载云硬盘",ConstantClazz.LOG_TYPE_DISK,vol.getVolName(),vol.getPrjId(),ConstantClazz.LOG_STATU_SUCCESS,null);
				}catch(Exception e){
					log.error(e.getMessage(),e);
					logService.addLog("挂载云硬盘",ConstantClazz.LOG_TYPE_DISK,vol.getVolName(),vol.getPrjId(),ConstantClazz.LOG_STATU_ERROR,e);
					throw e;
				}
			}
		}catch(Exception e){
			log.error(e.getMessage(),e);
			res.setRespCode(ConstantClazz.ERROR_CODE);
			res.setData(successCount);
		}
    	if(res.getRespCode()==null){
    		res.setRespCode(ConstantClazz.SUCCESS_CODE);
    	}
    	res.setData(successCount);
		return res;
    }
    
    
    /**
     * 查询当前项目下云硬盘配额
     * @param request
     * @param map
     * @return
     * @throws Exception 
     */
    @RequestMapping(value= "/checkVolumeQuota" , method = RequestMethod.POST)
    @ResponseBody
    public String checkVolumeQuota(HttpServletRequest request,@RequestBody CloudOrderVolume volumeOrder) throws Exception{
    	String errMessage=null;
        try {
        	log.info("验证云硬盘配额开始");
            errMessage=diskService.checkVolumeQuota(volumeOrder);
        } catch (Exception e) {
             log.error(e.getMessage(),e);
             throw e;
        }
        return JSONObject.toJSONString(errMessage);
       
    }
    
    /**
     * 续费提交订单，校验是否可以创建订单
     * @param request
     * @param map
     * @return
     * @throws Exception 
     */
    @RequestMapping(value="/renewVolumeOrderConfirm", method = RequestMethod.POST)
    @ResponseBody
    public String renewVolumeOrderConfirm(HttpServletRequest request,@RequestBody Map<String,String> map) throws Exception{
    	log.info("云硬盘续费提交订单校验开始");

    	SessionUserInfo sessionUser = (SessionUserInfo) request.getSession().getAttribute(ConstantClazz.SYS_SESSION_USERINFO);
    	String userId = sessionUser.getUserId();
    	String userName = sessionUser.getUserName();
    	String cusId = sessionUser.getCusId();
    	String opIp = sessionUser.getIP();
    	map.put("operatorIp", opIp);//操作者ip
    	
    	JSONObject json = new JSONObject ();
    	try{
    		json = diskService.renewVolumeOrderConfirm(map, userId, userName,cusId);
    	}catch(Exception e){
    		throw e;
    	}
    	return json.toJSONString();
    }


    /**
     * 判断是否有正在升级或续费中的订单，
     * 若有返回 <code>true</code>;否则返回 <code>false</code> <br>
     * ------------------
     * @author chengxiaodong
     * @param request
     * @param volId
     * @return
     * @throws Exception
     */
    @RequestMapping(value = "/checkVolOrderExsit" , method = RequestMethod.POST)
    @ResponseBody
    public String checkVmOrderExsit(HttpServletRequest request,@RequestBody String volId) throws Exception{
    	log.info("判断云硬盘是否有升级中或续费中的订单");
    	JSONObject json = new JSONObject ();
    	try{
    		boolean flag =diskService.checkVolOrderExsit(volId);
    		json.put("data", flag);
    	}catch(AppException e){
    		throw e;
    	}
    	catch(Exception e){
    		log.error(e.getMessage(),e);
    		throw e;
    	}
    	return json.toJSONString();

    }
    /**
     * 续费弹出页点击确定时，校验是否存在未完成订单
     * @param request
     * @param volId
     * @return
     * @throws Exception
     */
    @RequestMapping(value = "/checkVolumeOrderExist" , method = RequestMethod.POST)
    @ResponseBody
    public String checkVolumeOrderExist(HttpServletRequest request, @RequestBody String volId) throws Exception{
        log.info("检查当前是否已存在硬盘续费或变配的未完成订单");
        JSONObject json = new JSONObject();
        try{
            boolean flag = diskService.checkVolOrderExsit(volId);
            json.put("flag", flag);
        }catch(Exception e){
            throw e;
        }
        return json.toJSONString();
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
    		SessionUserInfo sessionUser = (SessionUserInfo) request.getSession().getAttribute(ConstantClazz.SYS_SESSION_USERINFO);
    		int pageSize = map.getPageSize();
    		int pageNumber = map.getPageNumber();
    		
    		QueryMap queryMap=new QueryMap();
    		queryMap.setPageNum(pageNumber);
    		queryMap.setCURRENT_ROWS_SIZE(pageSize);
    		
    		page=diskService.getRecycleVolList(page,map,sessionUser,queryMap);
    	} catch (Exception e) {
    		throw e;
    	}
    	return JSONObject.toJSONString(page);
    	
    }
    
    
    
    /**
     * 恢复volume（回收站）
     * ------------------
     * @author chengxiaodong
     * @param request
     * @param cloudVolume
     * @return
     * @throws Exception 
     */
    @RequestMapping(value = "/recoverVolume" , method = RequestMethod.POST)
    @ResponseBody
    public String recoverVolume(HttpServletRequest request,@RequestBody CloudVolume cloudVol) throws Exception{
    	log.info("恢复回收站云硬盘");
    	SessionUserInfo sessionUser = null ;
    	JSONObject json = new JSONObject ();
    	try{
    		sessionUser = (SessionUserInfo)request.getSession().getAttribute(ConstantClazz.SYS_SESSION_USERINFO);
    		diskService.recoverVolume(cloudVol.getVolId(),sessionUser);
    		logService.addLog("恢复云硬盘",  ConstantClazz.LOG_TYPE_DISK, cloudVol.getVolName(), cloudVol.getPrjId(),  ConstantClazz.LOG_STATU_SUCCESS,null);
    		json.put("respCode", ConstantClazz.SUCCESS_CODE_OP);
    	}catch(Exception e){
    	    log.error(e.getMessage(),e);
    	    logService.addLog("恢复云硬盘",  ConstantClazz.LOG_TYPE_DISK, cloudVol.getVolName(), cloudVol.getPrjId(),  ConstantClazz.LOG_STATU_ERROR,e);
    		json.put("respCode", ConstantClazz.ERROR_CODE);
    		throw e;
    	}
    	
    	return json.toJSONString();
    	
    }
    
    
    /**
     * 根据订单编号查询云硬盘订单信息
     * 
     * @authorchengxiaodong
     * @param request
     * @param orderNo
     * @return
     */
    @RequestMapping(value="queryCloudOrderByOrderNo" , method = RequestMethod.POST)
    @ResponseBody
    public String queryCloudOrderByOrderNo (HttpServletRequest request , @RequestBody String orderNo){
    	log.info("根据订单编号查询云硬盘订单信息");
    	JSONObject json = new JSONObject();
    	try{
    		CloudOrderVolume cloudOrderVolume = diskService.queryCloudOrderByOrderNo(orderNo);
    		json.put("data", cloudOrderVolume);
    	}catch(Exception e){
    		log.error(e.getMessage(),e);
    		throw e;
    	}
    	return json.toJSONString();
    };
    
    
    /**
   	 * 根据云硬盘ID查询云硬盘的计费队列需要的信息
   	 * 
   	 * @param volId
   	 * @return
   	 */
       @RequestMapping(value = "/queryVolChargeById" , method = RequestMethod.POST)
       @ResponseBody
       public String queryVolChargeById(HttpServletRequest request,@RequestBody String volId){
       	log.info("云硬盘的计费队列需要的信息");
       	JSONObject json = new JSONObject ();
       	try{
       		CloudVolume cloudVolume =  diskService.queryVolChargeById(volId);
       		json.put("data", cloudVolume);
       	}catch(AppException e){
       		throw e;
       	}
       	catch(Exception e){
       		log.error(e.getMessage(),e);
       		throw e;
       	}
       	return json.toJSONString();
       }
       

       /**
        * 查找指定数据中心下的已经启用的云硬盘类型
        * ----------------------
        * @author chengxiaodong
        * @param request
        * @return
        */
       @RequestMapping(value = "/getVolumeTypesByDcId" , method = RequestMethod.POST)
       @ResponseBody
       public String getVolumeTypesByDcId(HttpServletRequest request,@RequestBody String dcId)throws Exception {
       	log.info("查询指定数据中心下启用的云硬盘类型");
           List<CloudVolumeType> volumeTypeList = new ArrayList<CloudVolumeType>();
        try {
        	   volumeTypeList = volumeTypeService.getVolumeTypeList(dcId);
   		} catch (Exception e) {
   		    log.error(e.getMessage(),e);
   			throw e;
   		}
           
       	return JSONObject.toJSONString(volumeTypeList);
       }
       
       /**
        * 查找指定数据中心下的指定的云硬盘类型
        * ----------------------
        * @author chengxiaodong
        * @param request
        * @return
        */
       @RequestMapping(value = "/getVolumeTypesByTypeId" , method = RequestMethod.POST)
       @ResponseBody
       public String getVolumeTypesByTypeId(HttpServletRequest request,@RequestBody Map<String,String> map)throws Exception {
       	log.info("查询指定数据中心下指定的云硬盘类型");
           CloudVolumeType volumeType = null;
        try {
        	String dcId = map.get("dcId");
        	String typeId = map.get("typeId");
        	volumeType = volumeTypeService.getVolumeTypeById(dcId,typeId);
   		} catch (Exception e) {
   		    log.error(e.getMessage(),e);
   			throw e;
   		}
           
       	return JSONObject.toJSONString(volumeType);
       }
       
       
       /**
        * 查询指定云主机的系统盘信息
        * @author Chengxiaodong
        * @param request
        * @param map
        * @return
        */
       @RequestMapping(value= "/getSysVolumeByVmId" , method = RequestMethod.POST)
       @ResponseBody
       public String getSysVolumeByVmId(HttpServletRequest request,@RequestBody String vmId){
       	CloudVolume volume=null;
       	try{
       		log.info("查询云主机系统盘详情开始");
       		volume=diskService.getSysVolumeByVmId(vmId);
       	}catch(Exception e){
       		log.error(e.getMessage(),e);
       	}
       	return JSONObject.toJSONString(volume);
       	
       }

}
