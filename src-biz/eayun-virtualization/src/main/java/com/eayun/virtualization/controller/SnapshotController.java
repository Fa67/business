package com.eayun.virtualization.controller;

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
import com.eayun.log.service.LogService;
import com.eayun.virtualization.model.BaseCloudSnapshot;
import com.eayun.virtualization.model.BaseCloudVolume;
import com.eayun.virtualization.model.CloudOrderSnapshot;
import com.eayun.virtualization.model.CloudOrderVolume;
import com.eayun.virtualization.model.CloudProject;
import com.eayun.virtualization.model.CloudSnapshot;
import com.eayun.virtualization.model.CloudVolume;
import com.eayun.virtualization.service.SnapshotService;
import com.eayun.virtualization.service.VolumeService;

@Controller
@RequestMapping("/cloud/snapshot")
@Scope("prototype")
public class SnapshotController extends BaseController{
    private static final Logger log = LoggerFactory.getLogger(SnapshotController.class);

    @Autowired
    private SnapshotService snapService; 
    @Autowired
    private VolumeService volumeService;
    @Autowired
    private LogService logService;
   

    
    
    /**
     * <p>购买云硬盘备份并提交<p>
     * 
     * @author chengxiaodong
     * 
     * @param request
     * @param orderSnap
     * @return
     * @throws Exception 
     */
    @RequestMapping(value= "/buySnapshot" , method = RequestMethod.POST)
    @ResponseBody
    public String buySnapshot(HttpServletRequest request, @RequestBody CloudOrderSnapshot orderSnap) throws Exception{
    	log.info("购买备份");
    	JSONObject json  = new JSONObject();
    	try{
    		SessionUserInfo sessionUser = (SessionUserInfo) request.getSession().getAttribute(ConstantClazz.SYS_SESSION_USERINFO);
    		String message=snapService.buySnapshot(orderSnap, sessionUser);
    		json.put("respCode", ConstantClazz.SUCCESS_CODE_ADD);
    		
    		if(StringUtils.isEmpty(message)){
    			json.put("respCode", ConstantClazz.SUCCESS_CODE_ADD);
    			json.put("orderNo", orderSnap.getOrderNo());
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
    			json.put("orderNo", orderSnap.getOrderNo());
    		}
			log.error(e.getMessage(),e);
    	}
    	return json.toJSONString();
    }
    
    
    /**
     * <p>回滚云硬盘<p>
     * 
     * @author chengxiaodong
     * 
     * @param request
     * @param orderVolume
     * @return
     * @throws Exception 
     */
    @RequestMapping(value= "/rollbackvolume" , method = RequestMethod.POST)
    @ResponseBody
    public String rollBackVolume(HttpServletRequest request, @RequestBody CloudSnapshot snapshot) throws Exception{
    	log.info("回滚云硬盘");
    	JSONObject json  = new JSONObject();
    	try{
    		SessionUserInfo sessionUser = (SessionUserInfo) request.getSession().getAttribute(ConstantClazz.SYS_SESSION_USERINFO);
    		snapService.rollBackVolume(snapshot, sessionUser);
    		json.put("respCode", ConstantClazz.SUCCESS_CODE_ADD);
    		logService.addLog("回滚云硬盘",ConstantClazz.LOG_TYPE_DISK,snapshot.getSnapName(),snapshot.getPrjId(),ConstantClazz.LOG_STATU_SUCCESS,null);
    	}catch(Exception e){
    		logService.addLog("回滚云硬盘",ConstantClazz.LOG_TYPE_DISK,snapshot.getSnapName(),snapshot.getPrjId(),ConstantClazz.LOG_STATU_ERROR,e);
    		json.put("respCode", ConstantClazz.ERROR_CODE);
    		log.error(e.toString(),e);
    		throw e;
    	}
    	return json.toJSONString();
    }
    
    

   /**查询硬盘备份列表
    * @param request
    * @param datacenterId
    * @param projectId
    * @param name
    * @param page
    * @return
    */
   
    @RequestMapping(value= "/getSnapshotList" , method = RequestMethod.POST)
    @ResponseBody
    public String getSnapshotList(HttpServletRequest request,Page page,@RequestBody ParamsMap map){
        try {
        	log.info("查询云硬盘备份列表开始");
        	 SessionUserInfo sessionUser = (SessionUserInfo) request.getSession().getAttribute(ConstantClazz.SYS_SESSION_USERINFO);
            
        	String dcId=map.getParams().get("dcId").toString();
            String prjId = map.getParams().get("prjId").toString();
            String snapName=map.getParams().get("name").toString();
            String isDeleted=map.getParams().get("isDeleted").toString();
            int pageSize = map.getPageSize();
            int pageNumber = map.getPageNumber();
            
            QueryMap queryMap=new QueryMap();
            queryMap.setPageNum(pageNumber);
            queryMap.setCURRENT_ROWS_SIZE(pageSize);
            page=snapService.getSnapshotList(page,prjId,dcId,snapName,isDeleted,queryMap);
        } catch (Exception e) {
            log.error(e.toString(),e);
        }
        return JSONObject.toJSONString(page);
       
    }
    
    
    
    /**
     * 验证名称唯一
     * @param request
     * @param map
     * @return
     * @author chengxiaodong
     */
    @RequestMapping(value= "/getSnapByName" , method = RequestMethod.POST)
    @ResponseBody
    public String getSnapByName(HttpServletRequest request,@RequestBody CloudSnapshot snap){
    	boolean isTrue=false;
    	try{
    	log.info("云硬盘备份验证重名开始");
    	isTrue=snapService.getSnapByName(snap);
    	}catch(AppException e){
            throw e;
    	}
        return JSONObject.toJSONString(isTrue);
    }
  
    
  
    
    
   /**
     * 删除云硬盘备份
     * @param request
     * @param map
     * @return
     */
    @RequestMapping(value= "/deleteSnap" , method = RequestMethod.POST)
    @ResponseBody
    public String deleteSnapshot(HttpServletRequest request,@RequestBody CloudSnapshot snap){
    	boolean isTrue=false;
    	String doName="";
    	try{
    		log.info("删除云硬盘备份开始");
    		if("2".equals(snap.getIsDeleted())){
    			doName="删除云硬盘备份";
            }else{
            	doName="销毁云硬盘备份";
            }
    	    SessionUserInfo sessionUser = (SessionUserInfo) request.getSession().getAttribute(ConstantClazz.SYS_SESSION_USERINFO);
            isTrue=snapService.deleteSnapshot(snap,sessionUser);
            logService.addLog(doName,ConstantClazz.LOG_TYPE_DISKSNAPSHOT,snap.getSnapName(),snap.getPrjId(),ConstantClazz.LOG_STATU_SUCCESS,null);
    	}catch(AppException e){
    		logService.addLog(doName,ConstantClazz.LOG_TYPE_DISKSNAPSHOT,snap.getSnapName(),snap.getPrjId(),ConstantClazz.LOG_STATU_ERROR,e);
    		throw e;
    	}
		
    	return JSONObject.toJSONString(isTrue);
    	
    }
    
   

    
  /**
     * 编辑云硬盘备份
     * @param request
     * @param map
     * @return
     */
    @RequestMapping(value= "/updateSnapshot" , method = RequestMethod.POST)
    @ResponseBody
     public String updateSnapshot(HttpServletRequest request,@RequestBody CloudSnapshot snap)throws AppException{
    	boolean isTrue=false;
    	 try{
    		 log.info("编辑云硬盘备份开始");
        	 isTrue=snapService.updateSnapshot(snap);
        	 logService.addLog("编辑云硬盘备份",ConstantClazz.LOG_TYPE_DISKSNAPSHOT,snap.getSnapName(),snap.getPrjId(),ConstantClazz.LOG_STATU_SUCCESS,null);
    	 }catch(AppException e){
    		 logService.addLog("编辑云硬盘备份",ConstantClazz.LOG_TYPE_DISKSNAPSHOT,snap.getSnapName(),snap.getPrjId(),ConstantClazz.LOG_STATU_ERROR,e);
    		 throw e;
    	 }
	 return JSONObject.toJSONString(isTrue);
    	 
     }
    
   
    
    
    
    
    /**查询硬盘备份列表(云硬盘详情页)
     * @param request
     * @param datacenterId
     * @param projectId
     * @param name
     * @param page
     * @return
     */
    
     @RequestMapping(value= "/getSnapListByVolId" , method = RequestMethod.POST)
     @ResponseBody
     public String getSnapListByVolId(HttpServletRequest request,Page page,@RequestBody Map map){
    	 List<CloudSnapshot> snapList=null;
         try {
             String volId=map.get("volId").toString();
             snapList=snapService.getSnapListByVolId(volId);
             
         } catch (Exception e) {
             log.error(e.toString(),e);
         }
         return JSONObject.toJSONString(snapList);
        
     }
    
     
     
     
     /**
      * 查询项目的配额和已使用信息
      * @param request
      * @param prjId
      * @return
      */
     @RequestMapping(value="/getProjectjQuotaById", method = RequestMethod.POST)
     @ResponseBody
     public String getProjectQuota(HttpServletRequest request,@RequestBody String prjId){
     	log.info("查询项目的配额和已使用信息");
     	JSONObject json = new JSONObject ();
     	try{
     		CloudProject project = snapService.queryProjectQuotaAndUsed(prjId);
     		json.put("data", project);
     	}catch(Exception e){
     		throw e;
     	}
     	return json.toJSONString();
     }
     
     
     /**
      * 查询当前登录用户下回收站的备份
      * ---------------------
      * @author chengxiaodong
      * @param request
      * @param page
      * @param map 请求参数
      * @return
      */
     @RequestMapping(value= "/getRecycleSnapList" , method = RequestMethod.POST)
     @ResponseBody
     public String getRecycleSnapList(HttpServletRequest request,Page page,@RequestBody ParamsMap map) throws Exception{
     	try {
     		SessionUserInfo sessionUser = (SessionUserInfo) request.getSession().getAttribute(ConstantClazz.SYS_SESSION_USERINFO);
     		int pageSize = map.getPageSize();
     		int pageNumber = map.getPageNumber();
     		
     		QueryMap queryMap=new QueryMap();
     		queryMap.setPageNum(pageNumber);
     		queryMap.setCURRENT_ROWS_SIZE(pageSize);
     		
     		page=snapService.getRecycleSnapList(page,map,sessionUser,queryMap);
     	} catch (Exception e) {
     		throw e;
     	}
     	return JSONObject.toJSONString(page);
     	
     }
    
     
     
     /**
      * 恢复snapshot（回收站）
      * ------------------
      * @author chengxiaodong
      * @param request
      * @param cloudSnapshot
      * @return
      * @throws Exception 
      */
     @RequestMapping(value = "/recoverSnapshot" , method = RequestMethod.POST)
     @ResponseBody
     public String recoverSnapshot(HttpServletRequest request,@RequestBody CloudSnapshot cloudSnap) throws Exception{
     	log.info("恢复回收站备份");
     	SessionUserInfo sessionUser = null ;
     	JSONObject json = new JSONObject ();
     	try{
     		sessionUser = (SessionUserInfo)request.getSession().getAttribute(ConstantClazz.SYS_SESSION_USERINFO);
     		snapService.recoverSnapshot(cloudSnap.getSnapId(),sessionUser);
     		logService.addLog("恢复云硬盘备份",  ConstantClazz.LOG_TYPE_DISKSNAPSHOT, cloudSnap.getSnapName(), cloudSnap.getPrjId(),  ConstantClazz.LOG_STATU_SUCCESS,null);
     		json.put("respCode", ConstantClazz.SUCCESS_CODE_OP);
     	}catch(Exception e){
     		logService.addLog("恢复云硬盘备份",  ConstantClazz.LOG_TYPE_DISKSNAPSHOT, cloudSnap.getSnapName(), cloudSnap.getPrjId(),  ConstantClazz.LOG_STATU_ERROR,e);
     		json.put("respCode", ConstantClazz.ERROR_CODE);
     		throw e;
     	}
     	
     	return json.toJSONString();
     	
     }
     
     
     /**
      * 根据订单编号查询备份订单信息
      * 
      * @authorchengxiaodong
      * @param request
      * @param orderNo
      * @return
     * @throws Exception 
      */
     @RequestMapping(value="queryCloudOrderByOrderNo" , method = RequestMethod.POST)
     @ResponseBody
     public String queryCloudOrderByOrderNo (HttpServletRequest request , @RequestBody String orderNo) throws Exception{
     	log.info("根据订单编号查询备份订单信息");
     	JSONObject json = new JSONObject();
     	try{
     		CloudOrderSnapshot cloudOrderSnaphsot = snapService.queryCloudOrderByOrderNo(orderNo);
     		json.put("data", cloudOrderSnaphsot);
     	}catch(Exception e){
     	   log.error(e.toString(),e);
     		throw e;
     	}
     	return json.toJSONString();
     };
     
     
     
     
     /**
      * 根据id查询备份
      * 
      * @authorchengxiaodong
      * @param request
      * @param orderNo
      * @return
     * @throws Exception 
      */
     @RequestMapping(value="getSnapshotById" , method = RequestMethod.POST)
     @ResponseBody
     public String getSnapshotById (HttpServletRequest request , @RequestBody String snapId) throws Exception{
     	log.info("根据备份Id查询备份");
     	JSONObject json = new JSONObject();
     	try{
     		CloudSnapshot cloudSnapshot = snapService.getSnapshotById(snapId);
     		json.put("data", cloudSnapshot);
     	}catch(Exception e){
     	   log.error(e.toString(),e);
     		throw e;
     	}
     	return json.toJSONString();
     };
     
     
     
   
    

}
