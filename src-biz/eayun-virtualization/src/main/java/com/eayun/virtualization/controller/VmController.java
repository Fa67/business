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
import com.eayun.common.dao.ParamsMap;
import com.eayun.common.dao.QueryMap;
import com.eayun.common.dao.support.Page;
import com.eayun.common.exception.AppException;
import com.eayun.common.filter.SessionUserInfo;
import com.eayun.common.model.EayunResponseJson;
import com.eayun.log.service.LogService;
import com.eayun.price.bean.ParamBean;
import com.eayun.price.bean.PriceDetails;
import com.eayun.sys.model.SysDataTree;
import com.eayun.virtualization.model.BaseCloudSecurityGroup;
import com.eayun.virtualization.model.BaseCloudSubNetWork;
import com.eayun.virtualization.model.BaseCloudVm;
import com.eayun.virtualization.model.CloudImage;
import com.eayun.virtualization.model.CloudNetWork;
import com.eayun.virtualization.model.CloudOrderVm;
import com.eayun.virtualization.model.CloudProject;
import com.eayun.virtualization.model.CloudSecretKey;
import com.eayun.virtualization.model.CloudSecurityGroup;
import com.eayun.virtualization.model.CloudSubNetWork;
import com.eayun.virtualization.model.CloudVm;
import com.eayun.virtualization.model.CloudVolume;
import com.eayun.virtualization.service.VmService;
/**
 * 云主机操作
 * @author zhouhaitao
 *
 */
@Controller
@RequestMapping("/cloud/vm")
@Scope("prototype")
public class VmController {
    private static final Logger log = LoggerFactory.getLogger(VmController.class);
    @Autowired
    private VmService vmService;  
    @Autowired
    private LogService logService;
    
    /**
     * 云业务点击左侧树“云主机”，查询数据中心--项目的方法
     * ----------------------
     * @author zhouhaitao
     * @param request
     * @return
     */
    @RequestMapping(value = "/getDatacenterProjectList" , method = RequestMethod.POST)
    @ResponseBody
    public String getDatacenterProjectList(HttpServletRequest request)throws Exception {
    	log.info("查询当前客户的数据中心-项目列表");
    	SessionUserInfo sessionUser = (SessionUserInfo) request.getSession().getAttribute(ConstantClazz.SYS_SESSION_USERINFO);
        List<CloudProject> projectList = new ArrayList<CloudProject>();
        try {
        	projectList = vmService.findDcAndPrj(sessionUser);
		} catch (Exception e) {
		    log.error(e.getMessage(),e);
			throw e;
		}
        
    	return JSONObject.toJSONString(projectList);
    }

    /**
     * 查询当前登录客户下的云主机
     * ---------------------
     * @author zhouhaitao
     * @param request
     * @param page
     * @param map 请求参数
     * @return
     */
    @RequestMapping(value= "/listVm" , method = RequestMethod.POST)
    @ResponseBody
    public String getVmlist(HttpServletRequest request,Page page,@RequestBody ParamsMap map) throws Exception{
    	try {
    		SessionUserInfo sessionUser = (SessionUserInfo) request.getSession().getAttribute(ConstantClazz.SYS_SESSION_USERINFO);
    		int pageSize = map.getPageSize();
    		int pageNumber = map.getPageNumber();
    		
    		QueryMap queryMap=new QueryMap();
    		queryMap.setPageNum(pageNumber);
    		queryMap.setCURRENT_ROWS_SIZE(pageSize);
    		
    		page=vmService.listVm(page,map,sessionUser,queryMap);
    	} catch (Exception e) {
    		throw e;
    	}
    	return JSONObject.toJSONString(page);
    	
    }
    
    
    /**
     * <p>校验云主机的受管IP占用情况<p>
     * 
     * @author zhouhaitao
     * 
     * @param request
     * @param page
     * @param vm
     * @return
     */
    @RequestMapping(value= "/checkVmIpUsed" , method = RequestMethod.POST)
    @ResponseBody
    public String checkVmIpUsed(HttpServletRequest request, @RequestBody CloudVm vm){
    	log.info("校验云主机受管子网IP的占用情况");
    	JSONObject json  = new JSONObject();
    	try{
    		boolean isVmIpUsed = vmService.checkVmIpUsed(vm);
    		json.put("data", isVmIpUsed);
    	}catch(Exception e){
    	    log.error(e.getMessage(),e);
    		throw e;
    	}
    	return json.toJSONString();
    }
    
    /**
     * <p>购买云主机并提交<p>
     * 
     * @author zhouhaitao
     * 
     * @param request
     * @param orderVm
     * @return
     * @throws Exception 
     */
    @RequestMapping(value= "/buyVm" , method = RequestMethod.POST)
    @ResponseBody
    public String buyVm(HttpServletRequest request, @RequestBody CloudOrderVm orderVm) throws Exception{
    	log.info("购买云主机");
    	JSONObject json  = new JSONObject();
    	try{
    		SessionUserInfo sessionUser = (SessionUserInfo) request.getSession().getAttribute(ConstantClazz.SYS_SESSION_USERINFO);
    		String errMsg = vmService.buyVm(orderVm, sessionUser);
    		if(StringUtils.isEmpty(errMsg)){
    			json.put("respCode", ConstantClazz.SUCCESS_CODE_ADD);
    			json.put("orderNo", orderVm.getOrderNo());
    		}
    		else{
    			json.put("respCode", ConstantClazz.WARNING_CODE);
    			json.put("message", errMsg);
    		}
    	}catch(Exception e){
    		if("余额不足".equals(e.getMessage())){
				json.put("respCode", ConstantClazz.WARNING_CODE);
    			json.put("message", "CHANGE_OF_BALANCE");
			}
    		else{
    			json.put("respCode", ConstantClazz.ERROR_CODE);
    			json.put("orderNo", orderVm.getOrderNo());
    		}
			log.error(e.getMessage(),e);
    	}
    	return json.toJSONString();
    }
    
    /**
     * 查询受管子网对应的主机列表
     * @author zhouhaitao
     * 
     * @param request
     * @param subnetId
     * 				受管子网ID
     * @return
     */
    @RequestMapping(value="/queryVmListBySubnetId", method = RequestMethod.POST)
    @ResponseBody
    public String queryVmListBySubnetId(HttpServletRequest request,@RequestBody String subnetId){
    	log.info("查询受管子网对应的主机列表");
    	JSONObject json = new JSONObject ();
    	try{
    		List<BaseCloudVm> vmList = vmService.queryVmListBySubnet(subnetId);
    		json.put("data", vmList);
    	}catch(Exception e){
    		throw e;
    	}
    	return json.toJSONString();
    };
    
    /**
     * 调整云主机
     * ------------------
     * @author zhouhaitao
     * @param request
     * @param cloudVm
     * @return
     * @throws Exception 
     */
    @RequestMapping(value = "/resizeVm" , method = RequestMethod.POST)
    @ResponseBody
    public String  resizeVm(HttpServletRequest request,@RequestBody CloudOrderVm cloudVm) throws Exception{
    	log.info("云主机升级配置");
    	JSONObject json = new JSONObject();
    	try{
    		SessionUserInfo sessionUser = (SessionUserInfo) request.getSession().getAttribute(ConstantClazz.SYS_SESSION_USERINFO);
    		String errMsg = vmService.resizeVm(cloudVm,sessionUser);
    		if(StringUtils.isEmpty(errMsg)){
    			json.put("respCode", ConstantClazz.SUCCESS_CODE_OP);
    			json.put("orderNo", cloudVm.getOrderNo());
    		}
    		else{
    			json.put("respCode", ConstantClazz.WARNING_CODE);
    			json.put("message", errMsg);
    		}
    	}catch(Exception e){
    		if("余额不足".equals(e.getMessage())){
				json.put("respCode", ConstantClazz.WARNING_CODE);
    			json.put("message", "CHANGE_OF_BALANCE");
			}
    		else{
    			json.put("respCode", ConstantClazz.ERROR_CODE);
    			json.put("orderNo", cloudVm.getOrderNo());
    		}
        	log.error(e.getMessage(),e);
    	}
    	return json.toJSONString();
    	
    }
    
    /**
     * 查询云主机详情
     * @param request
     * @param vmId
     * @return
     * @throws Exception
     */
    @RequestMapping(value = "/getVmById" , method = RequestMethod.POST)
    @ResponseBody
    public String getVmById(HttpServletRequest request,@RequestBody String vmId) throws Exception{
    	JSONObject json  = new JSONObject();
    	log.info("获取云主机详情");
    	CloudVm vm=null;
    	try{
    		vm=vmService.getById(vmId);
    		if(null!=vm){
    			json.put("data", vm);
    		}
    		json.put("respCode", ConstantClazz.SUCCESS_CODE_OP);
    	}catch(Exception e){
        	json.put("respCode", ConstantClazz.ERROR_CODE);
        	log.error(e.getMessage(),e);
        	throw e;
        }
        return json.toJSONString();	
    }
    
    /**
     * 修改子网
     * @param request
     * @param cloudVm
     * @return
     * @throws Exception
     */
    @RequestMapping(value = "/modifysubnet" , method = RequestMethod.POST)
    @ResponseBody
    public String modifySubnet(HttpServletRequest request,@RequestBody CloudVm cloudVm) throws Exception{
    	JSONObject json  = new JSONObject();
    	log.info("云主机修改子网");
    	try{
    		vmService.modifySubnet(cloudVm);
    		json.put("respCode", ConstantClazz.SUCCESS_CODE_OP);
    		logService.addLog("修改子网",  ConstantClazz.LOG_TYPE_HOST, cloudVm.getVmName(), cloudVm.getPrjId(),  ConstantClazz.LOG_STATU_SUCCESS,null);
    	}catch(Exception e){
    		json.put("respCode", ConstantClazz.ERROR_CODE);
    		logService.addLog("修改子网",  ConstantClazz.LOG_TYPE_HOST, cloudVm.getVmName(), cloudVm.getPrjId(),  ConstantClazz.LOG_STATU_ERROR,e);
    		log.error(e.getMessage(),e);
    		throw e;
    	}
    	return json.toJSONString();	
    }
    
    /**
     * 查询云主机使用的镜像信息
     * @param request
     * 
     * @param imageId
     * @return
     * @throws Exception
     */
    @RequestMapping(value = "/getImageByVm" , method = RequestMethod.POST)
    @ResponseBody
    public String getImageByVm(HttpServletRequest request,@RequestBody String imageId) throws Exception{
    	JSONObject json  = new JSONObject();
    	log.info("查询云主机使用的镜像信息");
    	CloudImage image = null;
    	try{
    		image = vmService.getImageById(imageId);
    		json.put("data", image);
    	}catch(Exception e){
    		json.put("respCode", ConstantClazz.ERROR_CODE);
    		log.error(e.getMessage(),e);
    		throw e;
    	}
    	return json.toJSONString();	
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
    		CloudProject project = vmService.queryPrjQuato(prjId);
    		json.put("data", project);
    	}catch(Exception e){
    		throw e;
    	}
    	return json.toJSONString();
    }
    
    /**
     * 查询当前登录用户下回收站的云主机
     * ---------------------
     * @author zhouhaitao
     * @param request
     * @param page
     * @param map 请求参数
     * @return
     */
    @RequestMapping(value= "/getRecycleVmList" , method = RequestMethod.POST)
    @ResponseBody
    public String getRecycleVmList(HttpServletRequest request,Page page,@RequestBody ParamsMap map) throws Exception{
    	try {
    		SessionUserInfo sessionUser = (SessionUserInfo) request.getSession().getAttribute(ConstantClazz.SYS_SESSION_USERINFO);
    		int pageSize = map.getPageSize();
    		int pageNumber = map.getPageNumber();
    		
    		QueryMap queryMap=new QueryMap();
    		queryMap.setPageNum(pageNumber);
    		queryMap.setCURRENT_ROWS_SIZE(pageSize);
    		
    		page=vmService.getRecycleVmList(page,map,sessionUser,queryMap);
    	} catch (Exception e) {
    		throw e;
    	}
    	return JSONObject.toJSONString(page);
    	
    }
    
    /**
     * 删除云主机
     * ------------------
     * @author zhouhaitao
     * @param request
     * @param cloudVm
     * @return
     * @throws Exception 
     */
    @RequestMapping(value = "/deleteVm" , method = RequestMethod.POST)
    @ResponseBody
    public String  deleteVm(HttpServletRequest request,@RequestBody CloudVm cloudVm) throws Exception{
    	log.info("删除云主机");
    	SessionUserInfo sessionUser = null ;
    	JSONObject json = new JSONObject ();
    	String opName = "删除云主机";
    	try{
    		sessionUser = (SessionUserInfo)request.getSession().getAttribute(ConstantClazz.SYS_SESSION_USERINFO);
        	vmService.deleteVm(cloudVm,sessionUser);
        	if("2".equals(cloudVm.getDeleteType())){
        		opName = "销毁云主机";
        	}
        	logService.addLog(opName,  ConstantClazz.LOG_TYPE_HOST, cloudVm.getVmName(), cloudVm.getPrjId(),  ConstantClazz.LOG_STATU_SUCCESS,null);
        	json.put("respCode", ConstantClazz.SUCCESS_CODE_DELETE);
        }catch(AppException e){
            log.error(e.getMessage(),e);
            logService.addLog(opName,  ConstantClazz.LOG_TYPE_HOST, cloudVm.getVmName(), cloudVm.getPrjId(),  ConstantClazz.LOG_STATU_ERROR,e);
        	json.put("respCode", ConstantClazz.ERROR_CODE);
        	throw e;
        }
    	catch(Exception e){
        	logService.addLog(opName,  ConstantClazz.LOG_TYPE_HOST, cloudVm.getVmName(), cloudVm.getPrjId(),  ConstantClazz.LOG_STATU_ERROR,e);
        	json.put("respCode", ConstantClazz.ERROR_CODE);
        	log.error(e.getMessage(),e);
        	throw e;
        }
		return json.toJSONString();
    	
    }
    
    /**
     * 恢复云主机（回收站）
     * ------------------
     * @author zhouhaitao
     * @param request
     * @param cloudVm
     * @return
     * @throws Exception 
     */
    @RequestMapping(value = "/restoreVm" , method = RequestMethod.POST)
    @ResponseBody
    public String restoreVm(HttpServletRequest request,@RequestBody CloudVm cloudVm) throws Exception{
    	log.info("恢复回收站云主机");
    	SessionUserInfo sessionUser = null ;
    	JSONObject json = new JSONObject ();
    	try{
    		sessionUser = (SessionUserInfo)request.getSession().getAttribute(ConstantClazz.SYS_SESSION_USERINFO);
    		vmService.restoreVm(cloudVm,sessionUser);
    		
    		logService.addLog("恢复云主机",  ConstantClazz.LOG_TYPE_HOST, cloudVm.getVmName(), cloudVm.getPrjId(),  ConstantClazz.LOG_STATU_SUCCESS,null);
    		json.put("respCode", ConstantClazz.SUCCESS_CODE_OP);
    	}catch(AppException e){
    	    log.error(e.getMessage(),e);
    	    logService.addLog("恢复站云主机",  ConstantClazz.LOG_TYPE_HOST, cloudVm.getVmName(), cloudVm.getPrjId(),  ConstantClazz.LOG_STATU_ERROR,e);
    		json.put("respCode", ConstantClazz.ERROR_CODE);
    		throw e;
    	}
    	catch(Exception e){
    		logService.addLog("恢复云主机",  ConstantClazz.LOG_TYPE_HOST, cloudVm.getVmName(), cloudVm.getPrjId(),  ConstantClazz.LOG_STATU_ERROR,e);
    		json.put("respCode", ConstantClazz.ERROR_CODE);
    		log.error(e.getMessage(),e);
    		throw e;
    	}
    	return json.toJSONString();
    	
    }
    
    /**
     * 判断该云主机是够有正在升级或续费中的订单，
     * 若有返回 <code>true</code>;否则返回 <code>false</code> <br>
     * ------------------
     * @author zhouhaitao
     * @param request
     * @param vmId
     * @return
     * @throws Exception 
     */
    @RequestMapping(value = "/checkVmOrderExsit" , method = RequestMethod.POST)
    @ResponseBody
    public String checkVmOrderExsit(HttpServletRequest request,@RequestBody String vmId) throws Exception{
    	log.info("判断云主机是否有升级中或续费中的订单");
    	JSONObject json = new JSONObject ();
    	try{
    		boolean flag =  vmService.checkVmOrderExsit(vmId);
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
	 * 根据云主机ID查询云主机的计费队列需要的信息
	 * 
	 * @param vmId
	 * @return
	 */
    @RequestMapping(value = "/queryVmChargeById" , method = RequestMethod.POST)
    @ResponseBody
    public String queryVmChargeById(HttpServletRequest request,@RequestBody String vmId){
    	log.info("云主机的计费队列需要的信息");
    	JSONObject json = new JSONObject ();
    	try{
    		CloudVm cloudVm =  vmService.queryVmChargeById(vmId);
    		json.put("data", cloudVm);
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
     * 查询购买周期的类型
     * 
     * @param request
     * @return
     */
    @RequestMapping(value="queryBuyCycleType" , method = RequestMethod.POST)
    @ResponseBody
    public String queryBuyCycleType (HttpServletRequest request){
    	log.info("查询购买周期类型");
    	JSONObject json = new JSONObject();
    	try{
    		List<SysDataTree> cycleTypeList = vmService.queryBuyCycleType();
    		json.put("data", cycleTypeList);
    	}catch(Exception e){
    	    log.error(e.getMessage(),e);
    		throw e;
    	}
    	return json.toJSONString();
    };
    
    /**
     * 根据购买周期类型查询购买周期
     * 
     * @param request
     * @param nodeId
     * @return
     */
    @RequestMapping(value="queryBuyCycleList" , method = RequestMethod.POST)
    @ResponseBody
    public String queryBuyCycleList (HttpServletRequest request , @RequestBody String nodeId){
    	log.info("根据购买周期类型查询购买周期");
    	JSONObject json = new JSONObject();
    	try{
    		List<SysDataTree> cycleList = vmService.queryBuyCycleList(nodeId);
    		json.put("data", cycleList);
    	}catch(Exception e){
    	    log.error(e.getMessage(),e);
    		throw e;
    	}
    	return json.toJSONString();
    };
    
    /**
     * 根据订单编号查询云主机订单信息
     * 
     * @author zhouhaitao
     * @param request
     * @param orderNo
     * @return
     */
    @RequestMapping(value="queryCloudOrderByOrderNo" , method = RequestMethod.POST)
    @ResponseBody
    public String queryCloudOrderByOrderNo (HttpServletRequest request , @RequestBody String orderNo){
    	log.info("根据订单编号查询云主机订单信息");
    	JSONObject json = new JSONObject();
    	try{
    		CloudOrderVm cloudOrderVm = vmService.queryCloudOrderByOrderNo(orderNo);
    		json.put("data", cloudOrderVm);
    	}catch(Exception e){
    	    log.error(e.getMessage(),e);
    		throw e;
    	}
    	return json.toJSONString();
    };
    
    /**
     * 查询回收站云主机详情
     * @param request
     * @param vmId
     * @return
     * @throws Exception
     */
    @RequestMapping(value = "/getRecycleVmById" , method = RequestMethod.POST)
    @ResponseBody
    public String getRecycleVmById(HttpServletRequest request,@RequestBody String vmId) throws Exception{
    	JSONObject json  = new JSONObject();
    	log.info("获取回收站云主机详情");
    	CloudVm vm=null;
    	try{
    		vm=vmService.getRecycleVmById(vmId);
    		if(null!=vm){
    			json.put("data", vm);
    		}
    		json.put("respCode", ConstantClazz.SUCCESS_CODE_OP);
    	}catch(Exception e){
        	json.put("respCode", ConstantClazz.ERROR_CODE);
        	log.error(e.getMessage(),e);
        	throw e;
        }
        return json.toJSONString();	
    }
    
    
    
    /**
     * 查询云主机创建中的自定义镜像
     * @param request
     * @param vmId
     * @return
     * @throws Exception
     */
    @RequestMapping(value = "/checkCreatingImage" , method = RequestMethod.POST)
    @ResponseBody
    public String checkCreatingImage(HttpServletRequest request,@RequestBody String vmId) throws Exception{
    	JSONObject json  = new JSONObject();
    	log.info("查询云主机创建中的自定义镜像");
    	boolean isExist = false;
    	try{
    		isExist=vmService.checkCreatingImageCount(vmId);
    		json.put("data", isExist);
    		json.put("respCode", ConstantClazz.SUCCESS_CODE_OP);
    	}catch(Exception e){
    		json.put("respCode", ConstantClazz.ERROR_CODE);
    		log.error(e.getMessage(),e);
    		throw e;
    	}
    	return json.toJSONString();	
    }
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    /**查询云主机列表
     * @param request
     * @param datacenterId
     * @param projectId
     * @param name
     * @param page
     * @return
     */
    
     @RequestMapping(value= "/listVmForVol" , method = RequestMethod.POST)
     @ResponseBody
     public String listVmForVol(HttpServletRequest request,Page page,@RequestBody CloudVolume vol){
    	 List<CloudVm> vmList=null;
         try {
//         	 SessionUserInfo sessionUser = (SessionUserInfo) request.getSession().getAttribute(ConstantClazz.SYS_SESSION_USERINFO);
             
             vmList=vmService.getUnDeletedVmListByProject(vol.getPrjId());
         } catch (AppException e) {
             throw e;
         }
         return JSONObject.toJSONString(vmList);
        
     }
     
     
     /**
 	 * 查询项目下可绑定云硬盘的云主机列表
 	 * @Author: chengxiaodong
 	 * @param request
 	 * @param map
 	 * @return
 	 *<li>Date: 2016年5月11日</li>
     * @throws Exception 
 	 */
 	@SuppressWarnings("rawtypes")
	@RequestMapping(value = "/getcanbindcloudvmList" , method = RequestMethod.POST)
     @ResponseBody
     public String  getCanBindCloudVmList(HttpServletRequest request,@RequestBody Map map) throws Exception{
     	log.info("查询项目下可绑定云硬盘的云主机列表开始");
     	List<CloudVm> list=null;
     	String prjId = null != map.get("prjId")?map.get("prjId").toString():"";
     	try {
 			 list = vmService.getCanBindCloudVmList(prjId);
 		} catch (Exception e) {
     		throw e;
 		}
     	return JSONObject.toJSONString(list);
     	
     }
     
 	/**
     * 编辑云主机
     * ------------------
     * @author zhouhaitao
     * @param request
     * @param cloudVm
     * @return
     */
    @RequestMapping(value = "/modifyVm" , method = RequestMethod.POST)
    @ResponseBody
    public String  modifyVm(HttpServletRequest request,@RequestBody CloudVm cloudVm){
    	log.info("编辑云主机");
    	JSONObject json = new JSONObject ();
        try{
        	vmService.modifyVm(cloudVm);
        	
        	logService.addLog("编辑云主机",  ConstantClazz.LOG_TYPE_HOST, cloudVm.getVmName(), cloudVm.getPrjId(),  ConstantClazz.LOG_STATU_SUCCESS,null);
        	json.put("data", cloudVm);
        	json.put("respCode", ConstantClazz.SUCCESS_CODE_UPDATE);
        }catch(AppException e){
        	logService.addLog("编辑云主机",  ConstantClazz.LOG_TYPE_HOST, cloudVm.getVmName(), cloudVm.getPrjId(),  ConstantClazz.LOG_STATU_ERROR,e);
        	json.put("respCode", ConstantClazz.ERROR_CODE);
        	log.error(e.getMessage(),e);
        	throw e;
        }
        catch(Exception e){
        	logService.addLog("编辑云主机",  ConstantClazz.LOG_TYPE_HOST, cloudVm.getVmName(), cloudVm.getPrjId(),  ConstantClazz.LOG_STATU_ERROR,e);
        	json.put("respCode", ConstantClazz.ERROR_CODE);
        	log.error(e.getMessage(),e);
        	throw e;
        }
		return json.toJSONString();
    }
    
    
    


    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    /**
     * 启动云主机
     * ------------------
     * @author zhouhaitao
     * @param request
     * @param cloudVm
     * @return
     */
    @RequestMapping(value = "/restartVm" , method = RequestMethod.POST)
    @ResponseBody
    public String  restartVm(HttpServletRequest request,@RequestBody CloudVm cloudVm){
    	log.info("启动云主机");
    	JSONObject json = new JSONObject ();
    	try{
        	vmService.restartVm(cloudVm);
        	
        	logService.addLog("启动云主机",  ConstantClazz.LOG_TYPE_HOST, cloudVm.getVmName(), cloudVm.getPrjId(),  ConstantClazz.LOG_STATU_SUCCESS,null);
        	json.put("respCode", ConstantClazz.SUCCESS_CODE_OP);
        }catch(AppException e){
            log.error(e.getMessage(),e);
            logService.addLog("启动云主机",  ConstantClazz.LOG_TYPE_HOST, cloudVm.getVmName(), cloudVm.getPrjId(),  ConstantClazz.LOG_STATU_ERROR,e);
        	json.put("respCode", ConstantClazz.ERROR_CODE);
        	throw e;
        }
    	catch(Exception e){
        	logService.addLog("启动云主机",  ConstantClazz.LOG_TYPE_HOST, cloudVm.getVmName(), cloudVm.getPrjId(),  ConstantClazz.LOG_STATU_ERROR,e);
        	json.put("respCode", ConstantClazz.ERROR_CODE);
        	log.error(e.getMessage(),e);
        	throw e;
        }
		return json.toJSONString();
    	
    }
    
    /**
     * 关闭云主机
     * ------------------
     * @author zhouhaitao
     * @param request
     * @param cloudVm
     * @return
     */
    @RequestMapping(value = "/shutdownVm" , method = RequestMethod.POST)
    @ResponseBody
    public String  shutdownVm(HttpServletRequest request,@RequestBody CloudVm cloudVm){
    	log.info("关闭云主机");
    	JSONObject json = new JSONObject ();
    	try{
    		vmService.shutdownVm(cloudVm);
    		
    		logService.addLog("关闭云主机",  ConstantClazz.LOG_TYPE_HOST, cloudVm.getVmName(), cloudVm.getPrjId(),  ConstantClazz.LOG_STATU_SUCCESS,null);
    		json.put("respCode", ConstantClazz.SUCCESS_CODE_OP);
    	}catch(AppException e){
    		logService.addLog("关闭云主机",  ConstantClazz.LOG_TYPE_HOST, cloudVm.getVmName(), cloudVm.getPrjId(),  ConstantClazz.LOG_STATU_ERROR,e);
    		json.put("respCode", ConstantClazz.ERROR_CODE);
    		throw e;
    	}
    	catch(Exception e){
    		logService.addLog("关闭云主机",  ConstantClazz.LOG_TYPE_HOST, cloudVm.getVmName(), cloudVm.getPrjId(),  ConstantClazz.LOG_STATU_ERROR,e);
    		json.put("respCode", ConstantClazz.ERROR_CODE);
    		log.error(e.getMessage(),e);
    		throw e;
    	}
    	return json.toJSONString();
    	
    }
    
    /**
     * 软重启云主机
     * ------------------
     * @author zhouhaitao
     * @param request
     * @param cloudVm
     * @return
     */
    @RequestMapping(value = "/softRestartVm" , method = RequestMethod.POST)
    @ResponseBody
    public String  softRestartVm(HttpServletRequest request,@RequestBody CloudVm cloudVm){
    	log.info("软重启云主机");
    	JSONObject json = new JSONObject();
    	try{
    		vmService.softRestartVm(cloudVm);
    		
    		logService.addLog("软重启云主机",  ConstantClazz.LOG_TYPE_HOST, cloudVm.getVmName(), cloudVm.getPrjId(),  ConstantClazz.LOG_STATU_SUCCESS,null);
    		json.put("respCode", ConstantClazz.SUCCESS_CODE_OP);
    	}catch(AppException e){
    		logService.addLog("软重启云主机",  ConstantClazz.LOG_TYPE_HOST, cloudVm.getVmName(), cloudVm.getPrjId(),  ConstantClazz.LOG_STATU_ERROR,e);
        	json.put("respCode", ConstantClazz.ERROR_CODE);
        	log.error(e.getMessage(),e);
        	throw e;
    	}catch(Exception e){
    		logService.addLog("软重启云主机",  ConstantClazz.LOG_TYPE_HOST, cloudVm.getVmName(), cloudVm.getPrjId(),  ConstantClazz.LOG_STATU_ERROR,e);
        	json.put("respCode", ConstantClazz.ERROR_CODE);
        	log.error(e.getMessage(),e);
        	throw e;
    	}
    	return json.toJSONString();
    	
    }
    
    /**
     * 硬重启云主机
     * ------------------
     * @author zhouhaitao
     * @param request
     * @param cloudVm
     * @return
     */
    @RequestMapping(value = "/hardRestartVm" , method = RequestMethod.POST)
    @ResponseBody
    public String  hardRestartVm(HttpServletRequest request,@RequestBody CloudVm cloudVm){
    	log.info("硬重启云主机");
    	JSONObject json = new JSONObject();
    	try{
    		vmService.hardRestartVm(cloudVm);
    		
    		logService.addLog("硬重启云主机",  ConstantClazz.LOG_TYPE_HOST, cloudVm.getVmName(), cloudVm.getPrjId(),  ConstantClazz.LOG_STATU_SUCCESS,null);
    		json.put("respCode", ConstantClazz.SUCCESS_CODE_OP);
    	}catch(AppException e){
    		logService.addLog("硬重启云主机",  ConstantClazz.LOG_TYPE_HOST, cloudVm.getVmName(), cloudVm.getPrjId(),  ConstantClazz.LOG_STATU_ERROR,e);
        	json.put("respCode", ConstantClazz.ERROR_CODE);
        	log.error(e.getMessage(),e);
        	throw e;
    	}catch(Exception e){
    		logService.addLog("硬重启云主机",  ConstantClazz.LOG_TYPE_HOST, cloudVm.getVmName(), cloudVm.getPrjId(),  ConstantClazz.LOG_STATU_ERROR,e);
        	json.put("respCode", ConstantClazz.ERROR_CODE);
        	log.error(e.getMessage(),e);
        	throw e;
    	}
    	return json.toJSONString();
    	
    }
    
    /**
     * 创建云主机镜像
     * ------------------
     * @author zhouhaitao
     * @param request
     * @param cloudVm
     * @return
     */
    @RequestMapping(value = "/createSnapshot" , method = RequestMethod.POST)
    @ResponseBody
    public String  createSnapshot(HttpServletRequest request,@RequestBody CloudVm cloudVm){
    	log.info("创建云主机镜像");
    	JSONObject json = new JSONObject();
    	SessionUserInfo sessionUser= null;
    	try{
    		sessionUser = (SessionUserInfo) request.getSession().getAttribute(ConstantClazz.SYS_SESSION_USERINFO);
    		vmService.createSnapshot(cloudVm,sessionUser);
    		
    		logService.addLog("创建镜像",  ConstantClazz.LOG_TYPE_MIRROR, cloudVm.getImageName(), cloudVm.getPrjId(),  ConstantClazz.LOG_STATU_SUCCESS,null);
    		json.put("respCode", ConstantClazz.SUCCESS_CODE_OP);
    	}catch(AppException e){
    		logService.addLog("创建镜像",  ConstantClazz.LOG_TYPE_MIRROR, cloudVm.getImageName(), cloudVm.getPrjId(),  ConstantClazz.LOG_STATU_ERROR,e);
        	json.put("respCode", ConstantClazz.ERROR_CODE);
        	log.error(e.getMessage(),e);
        	throw e;
    	}catch(Exception e){
    		logService.addLog("创建镜像",  ConstantClazz.LOG_TYPE_MIRROR, cloudVm.getImageName(), cloudVm.getPrjId(),  ConstantClazz.LOG_STATU_ERROR,e);
        	json.put("respCode", ConstantClazz.ERROR_CODE);
        	log.error(e.getMessage(),e);
        	throw e;
    	}
    	return json.toJSONString();
    	
    }
    
    /**
     * 挂起云主机
     * ------------------
     * @author zhouhaitao
     * @param request
     * @param cloudVm
     * @return
     */
    @RequestMapping(value = "/suspendVm" , method = RequestMethod.POST)
    @ResponseBody
    public String  suspendVm(HttpServletRequest request,@RequestBody CloudVm cloudVm){
    	log.info("挂起云主机");
    	JSONObject json = new JSONObject();
    	try{
    		vmService.suspendVm(cloudVm);
    		
    		logService.addLog("挂起云主机",  ConstantClazz.LOG_TYPE_HOST, cloudVm.getVmName(), cloudVm.getPrjId(),  ConstantClazz.LOG_STATU_SUCCESS,null);
    		json.put("respCode", ConstantClazz.SUCCESS_CODE_OP);
    	}catch(AppException e){
    		logService.addLog("挂起云主机",  ConstantClazz.LOG_TYPE_HOST, cloudVm.getVmName(), cloudVm.getPrjId(),  ConstantClazz.LOG_STATU_ERROR,e);
        	json.put("respCode", ConstantClazz.ERROR_CODE);
        	log.error(e.getMessage(),e);
        	throw e;
    	}catch(Exception e){
    		logService.addLog("挂起云主机",  ConstantClazz.LOG_TYPE_HOST, cloudVm.getVmName(), cloudVm.getPrjId(),  ConstantClazz.LOG_STATU_ERROR,e);
        	json.put("respCode", ConstantClazz.ERROR_CODE);
        	log.error(e.getMessage(),e);
        	throw e;
    	}
    	return json.toJSONString();
    	
    }
    
    /**
     * 恢复云主机
     * ------------------
     * @author zhouhaitao
     * @param request
     * @param cloudVm
     * @return
     */
    @RequestMapping(value = "/resumeVm" , method = RequestMethod.POST)
    @ResponseBody
    public String  resumeVm(HttpServletRequest request,@RequestBody CloudVm cloudVm){
    	log.info("恢复云主机");
    	JSONObject json = new JSONObject();
    	try{
    		vmService.resumeVm(cloudVm);
    		
    		logService.addLog("恢复云主机",  ConstantClazz.LOG_TYPE_HOST, cloudVm.getVmName(), cloudVm.getPrjId(),  ConstantClazz.LOG_STATU_SUCCESS,null);
    		json.put("respCode", ConstantClazz.SUCCESS_CODE_OP);
    	}catch(AppException e){
    		logService.addLog("恢复云主机",  ConstantClazz.LOG_TYPE_HOST, cloudVm.getVmName(), cloudVm.getPrjId(),  ConstantClazz.LOG_STATU_ERROR,e);
        	json.put("respCode", ConstantClazz.ERROR_CODE);
        	log.error(e.getMessage(),e);
        	throw e;
    	}catch(Exception e){
    		logService.addLog("恢复云主机",  ConstantClazz.LOG_TYPE_HOST, cloudVm.getVmName(), cloudVm.getPrjId(),  ConstantClazz.LOG_STATU_ERROR,e);
        	json.put("respCode", ConstantClazz.ERROR_CODE);
        	log.error(e.getMessage(),e);
        	throw e;
    	}
    	return json.toJSONString();
    	
    }
    
    /**
     * 确认调整云主机
     * ------------------
     * @author zhouhaitao
     * @param request
     * @param cloudVm
     * @return
     */
    @RequestMapping(value = "/confirmResizeVm" , method = RequestMethod.POST)
    @ResponseBody
    public String  confirmResizeVm(HttpServletRequest request,@RequestBody CloudVm cloudVm){
    	log.info("确认调整云主机");
    	JSONObject json = new JSONObject();
    	try{
    		vmService.confirmResizeVm(cloudVm);
    		
    		logService.addLog("确认调整云主机",  ConstantClazz.LOG_TYPE_HOST, cloudVm.getVmName(), cloudVm.getPrjId(),  ConstantClazz.LOG_STATU_SUCCESS,null);
    		json.put("respCode", ConstantClazz.SUCCESS_CODE_OP);
    	}catch(AppException e){
    		logService.addLog("确认调整云主机",  ConstantClazz.LOG_TYPE_HOST, cloudVm.getVmName(), cloudVm.getPrjId(),  ConstantClazz.LOG_STATU_ERROR,e);
        	json.put("respCode", ConstantClazz.ERROR_CODE);
        	log.error(e.getMessage(),e);
        	throw e;
    	}catch(Exception e){
    		logService.addLog("确认调整云主机",  ConstantClazz.LOG_TYPE_HOST, cloudVm.getVmName(), cloudVm.getPrjId(),  ConstantClazz.LOG_STATU_ERROR,e);
        	json.put("respCode", ConstantClazz.ERROR_CODE);
        	log.error(e.getMessage(),e);
        	throw e;
    	}
    	return json.toJSONString();
    	
    }
    
    /**
     * 取消调整云主机
     * ------------------
     * @author zhouhaitao
     * @param request
     * @param cloudVm
     * @return
     */
    @RequestMapping(value = "/revertResizeVm" , method = RequestMethod.POST)
    @ResponseBody
    public String  revertResizeVm(HttpServletRequest request,@RequestBody CloudVm cloudVm){
    	log.info("取消调整云主机");
    	JSONObject json = new JSONObject();
    	try{
    		vmService.revertResizeVm(cloudVm);
    		
    		logService.addLog("取消调整云主机",  ConstantClazz.LOG_TYPE_HOST, cloudVm.getVmName(), cloudVm.getPrjId(),  ConstantClazz.LOG_STATU_SUCCESS,null);
    		json.put("respCode", ConstantClazz.SUCCESS_CODE_OP);
    	}catch(AppException e){
    		logService.addLog("取消调整云主机",  ConstantClazz.LOG_TYPE_HOST, cloudVm.getVmName(), cloudVm.getPrjId(),  ConstantClazz.LOG_STATU_ERROR,e);
        	json.put("respCode", ConstantClazz.ERROR_CODE);
        	log.error(e.getMessage(),e);
        	throw e;
    	}catch(Exception e){
    		logService.addLog("取消调整云主机",  ConstantClazz.LOG_TYPE_HOST, cloudVm.getVmName(), cloudVm.getPrjId(),  ConstantClazz.LOG_STATU_ERROR,e);
        	json.put("respCode", ConstantClazz.ERROR_CODE);
        	log.error(e.getMessage(),e);
        	throw e;
    	}
    	return json.toJSONString();
    	
    }
    
    /**
     * 打开云主机控制台
     * ------------------
     * @author zhouhaitao
     * @param request
     * @param cloudVm
     * @return
     */
    @RequestMapping(value = "/consoleVm" , method = RequestMethod.POST)
    @ResponseBody
    public String  consoleVm(HttpServletRequest request,@RequestBody CloudVm cloudVm){
    	log.info("打开云主机控制台");
    	JSONObject json = new JSONObject();
    	String vmConsoleUrl = "" ;
    	try{
    		vmConsoleUrl = vmService.consoleVm(cloudVm);
    		
    		logService.addLog("打开云主机控制台",  ConstantClazz.LOG_TYPE_HOST, cloudVm.getVmName(), cloudVm.getPrjId(),  ConstantClazz.LOG_STATU_SUCCESS,null);
    		json.put("respCode", ConstantClazz.SUCCESS_CODE_OP);
    		json.put("url", vmConsoleUrl);
    	}catch(AppException e){
    		logService.addLog("打开云主机控制台",  ConstantClazz.LOG_TYPE_HOST, cloudVm.getVmName(), cloudVm.getPrjId(),  ConstantClazz.LOG_STATU_ERROR,e);
        	json.put("respCode", ConstantClazz.ERROR_CODE);
        	log.error(e.getMessage(),e);
        	throw e;
    	}catch(Exception e){
    		logService.addLog("打开云主机控制台",  ConstantClazz.LOG_TYPE_HOST, cloudVm.getVmName(), cloudVm.getPrjId(),  ConstantClazz.LOG_STATU_ERROR,e);
        	json.put("respCode", ConstantClazz.ERROR_CODE);
        	log.error(e.getMessage(),e);
        	throw e;
    	}
    	return json.toJSONString();
    	
    }
    
    /**
     * 获取云主机日志
     * ------------------
     * @author zhouhaitao
     * @param request
     * @param cloudVm
     * @return
     */
    @RequestMapping(value = "/getLog" , method = RequestMethod.POST)
    @ResponseBody
    public String  getLog(HttpServletRequest request,@RequestBody CloudVm cloudVm){
    	log.info("获取云主机日志");
    	JSONObject json = new JSONObject();
    	String vmLogs = "" ;
    	try{
    		vmLogs = vmService.getVmLogs(cloudVm);
    		
    		json.put("logs", vmLogs);
    		json.put("respCode", ConstantClazz.SUCCESS_CODE_OP);
    	}catch(AppException e){
    		json.put("respCode", ConstantClazz.ERROR_CODE);
    		log.error(e.getMessage(),e);
    		throw e;
    	}catch(Exception e){
    		json.put("respCode", ConstantClazz.ERROR_CODE);
    		log.error(e.getMessage(),e);
    		throw e;
    	}
    	return json.toJSONString();
    	
    }
    
    /**
     * 编辑云主机安全组
     * ------------------
     * @author zhouhaitao
     * @param request
     * @param cloudVm
     * @return
     */
    @RequestMapping(value = "/editVmSecurityGroup" , method = RequestMethod.POST)
    @ResponseBody
    public String  editVmSecurityGroup(HttpServletRequest request,@RequestBody CloudVm cloudVm){
    	log.info("编辑云主机安全组");
    	JSONObject json = new JSONObject();
    	try{
    		vmService.editVmSecurityGroup(cloudVm);
    		
    		logService.addLog("编辑云主机安全组",  ConstantClazz.LOG_TYPE_HOST, cloudVm.getVmName(), cloudVm.getPrjId(),  ConstantClazz.LOG_STATU_SUCCESS,null);
    		json.put("respCode", ConstantClazz.SUCCESS_CODE_OP);
    	}catch(AppException e){
    		logService.addLog("编辑云主机安全组",  ConstantClazz.LOG_TYPE_HOST, cloudVm.getVmName(), cloudVm.getPrjId(),  ConstantClazz.LOG_STATU_ERROR,e);
        	json.put("respCode", ConstantClazz.ERROR_CODE);
        	log.error(e.getMessage(),e);
        	throw e;
    	}catch(Exception e){
    		logService.addLog("编辑云主机安全组",  ConstantClazz.LOG_TYPE_HOST, cloudVm.getVmName(), cloudVm.getPrjId(),  ConstantClazz.LOG_STATU_ERROR,e);
        	json.put("respCode", ConstantClazz.ERROR_CODE);
        	log.error(e.getMessage(),e);
        	throw e;
    	}
    	return json.toJSONString();
    	
    }
    
    
    /**
     * 校验云主机在数据中心维度下重名
     * ------------------
     * @author zhouhaitao
     * @param request
     * @param map
     * @return
     */
    @RequestMapping(value = "/checkVmExistByName" , method = RequestMethod.POST)
    @ResponseBody
    public String  checkVmExistByName(HttpServletRequest request,@RequestBody CloudVm cloudVm){
    	log.info("校验云主机名称重名");
    	boolean flag = false;
    	try{
    		flag = vmService.checkVmExistByName(cloudVm);
    	}
    	catch(AppException e){
    		throw e;
    	}catch(Exception e){
    		throw e;
    	}
    	return JSONObject.toJSONString(flag);
    	
    }
    
    /**
     * 获取当前客户创建的项目及项目使用情况信息（用户管理的项目）
     * ------------------
     * @author zhouhaitao
     * @param request
     * 
     * @return
     */
    @RequestMapping(value = "/getProListByCustomer" , method = RequestMethod.POST)
    @ResponseBody
    public String getProListByCustomer(HttpServletRequest request){
    	log.info("获取当前客户创建的项目及项目使用情况信息");
    	SessionUserInfo sessionUser =null;
    	List<CloudProject> list = new ArrayList<CloudProject>();
    	try{
    		sessionUser = (SessionUserInfo) request.getSession().getAttribute(ConstantClazz.SYS_SESSION_USERINFO);
    		list=vmService.getProListByCustomer(sessionUser);
    	}catch(AppException e){
    		throw e;
    	}catch(Exception e ){
    		throw new AppException("");
    	}
    	return JSONObject.toJSONString(list);
    }

    /**
     * 获取CPU配置信息列表
     * ------------------
     * @author zhouhaitao
     * @param request
     * 
     * @return
     */
    @RequestMapping(value = "/getCpuList" , method = RequestMethod.POST)
    @ResponseBody
    public String getCpuList(HttpServletRequest request){
    	log.info("获取CPU配置信息列表");
    	List<SysDataTree> list = new ArrayList<SysDataTree>();
    	try{
    		list=vmService.getCpuList();
    	}catch(AppException e){
    		throw e;
    	}catch(Exception e ){
    		throw new AppException("");
    	}
    	return JSONObject.toJSONString(list);
    }
    
    /**
     * 根据CPU核数获取内存配置信息列表
     * ------------------
     * @author zhouhaitao
     * @param request
     * 
     * @return
     */
    @RequestMapping(value = "/getRamListByCpu" , method = RequestMethod.POST)
    @ResponseBody
    public String getRamListByCpu(HttpServletRequest request,@RequestBody String cpuId){
    	log.info("根据CPU核数获取内存配置信息列表");
    	List<SysDataTree> list = new ArrayList<SysDataTree>();
    	try{
    		list=vmService.getRamListByCpu(cpuId);
    	}catch(AppException e){
    		throw e;
    	}catch(Exception e ){
    		throw new AppException("");
    	}
    	return JSONObject.toJSONString(list);
    }
    
    /**
     * 获取创建云主机的系统类型
     * ------------------
     * @author zhouhaitao
     * @param request
     * 
     * @return
     */
    @RequestMapping(value = "/getOsList" , method = RequestMethod.POST)
    @ResponseBody
    public String getOsList(HttpServletRequest request){
    	log.info("获取创建云主机的系统类型");
    	List<SysDataTree> list = new ArrayList<SysDataTree>();
    	try{
    		list=vmService.getOsList();
    	}catch(AppException e){
    		throw e;
    	}catch(Exception e ){
    		throw new AppException("");
    	}
    	return JSONObject.toJSONString(list);
    }
    
    /**
     * 根据系统类型获取操作系统列表
     * ------------------
     * @author zhouhaitao
     * @param request
     * 
     * @return
     */
    @RequestMapping(value = "/getSysTypeList" , method = RequestMethod.POST)
    @ResponseBody
    public String getSysTypeList(HttpServletRequest request,@RequestBody String osId){
    	log.info("根据系统类型获取操作系统列表");
    	List<SysDataTree> list = new ArrayList<SysDataTree>();
    	try{
    		list=vmService.getSysTypeList(osId);
    	}catch(AppException e){
    		throw e;
    	}catch(Exception e ){
    		throw new AppException("");
    	}
    	return JSONObject.toJSONString(list);
    }
    
    /**
     * 查询项目下的自定义镜像列表或数据中心下的公共镜像
     * ------------------
     * @author zhouhaitao
     * @param request
     * 
     * @return
     */
    @RequestMapping(value = "/getImageList" , method = RequestMethod.POST)
    @ResponseBody
    public String getImageList(HttpServletRequest request,@RequestBody CloudVm cloudVm){
    	log.info("查询镜像列表");
    	List<CloudImage> list = new ArrayList<CloudImage>();
    	try{
    		list=vmService.getImageList(cloudVm);
    	}catch(AppException e){
    		throw e;
    	}catch(Exception e ){
    		e.printStackTrace();
    		throw new AppException("");
    	}
    	return JSONObject.toJSONString(list);
    }
    
    /**
     * 查询项目下的网络列表 
     * ------------------
     * @author zhouhaitao
     * @param request
     * 
     * @return
     */
    @RequestMapping(value = "/getSubNetList" , method = RequestMethod.POST)
    @ResponseBody
    public String getSubNetList(HttpServletRequest request,@RequestBody String prjId){
    	log.info("查询项目下的子网列表");
    	List<BaseCloudSubNetWork> list = new ArrayList<BaseCloudSubNetWork>();
    	try{
    		list=vmService.getSubNetList(prjId);
    	}catch(AppException e){
    		throw e;
    	}catch(Exception e ){
    	    log.error(e.getMessage(),e);
    		throw new AppException("");
    	}
    	return JSONObject.toJSONString(list);
    }
    
    
    /**
     * 查询操作系统型号列表
     * ------------------
     * @author zhouhaitao
     * @param request
     * 
     * @return
     */
    @RequestMapping(value = "/getVmSysList" , method = RequestMethod.POST)
    @ResponseBody
    public String getVmSysList(HttpServletRequest request){
    	log.info("查询操作系统型号列表");
    	List<SysDataTree> list = new ArrayList<SysDataTree>();
    	try{
    		list=vmService.getVmSysList();
    	}catch(Exception e ){
    	    log.error(e.getMessage(),e);
    		throw e;
    	}
    	return JSONObject.toJSONString(list);
    }
    
    /**
     * 查询项目下的未关联云主机的安全组信息
     * ------------------
     * @author zhouhaitao
     * @param request
     * @param cloudVm
     * 
     * @return
     */
    @RequestMapping(value = "/getSecurityGroupByPrj" , method = RequestMethod.POST)
    @ResponseBody
    public String getSecurityGroupByPrj(HttpServletRequest request,@RequestBody CloudVm cloudVm){
    	log.info("查询项目下的未关联云主机的安全组信息");
    	List<BaseCloudSecurityGroup> list = new ArrayList<BaseCloudSecurityGroup>();
    	try{
    		list=vmService.getSecurityGroupByPrj(cloudVm);
    	}catch(Exception e ){
    	    log.error(e.getMessage(),e);
    		throw e;
    	}
    	return JSONObject.toJSONString(list);
    }
    
    /**
     * 查询项目下的已关联云主机的安全组信息
     * ------------------
     * @author zhouhaitao
     * @param request
     * 
     * @return
     */
    @RequestMapping(value = "/getSecurityGroupByVm" , method = RequestMethod.POST)
    @ResponseBody
    public String getSecurityGroupByVm(HttpServletRequest request,@RequestBody String vmId){
    	log.info("查询项目下的已关联云主机的安全组信息");
    	List<BaseCloudSecurityGroup> list = new ArrayList<BaseCloudSecurityGroup>();
    	try{
    		list=vmService.getSecurityGroupByVm(vmId);
    	}catch(Exception e ){
    		log.error(e.getMessage(),e);
    		throw e;
    	}
    	return JSONObject.toJSONString(list);
    }
    
    /**
     * 查询项目下的网络列表
     * s
     * @param request
     * @param prjId
     * @return
     */
    @RequestMapping(value="/queryNetListByPrjId", method = RequestMethod.POST)
    @ResponseBody
    public String queryNetListByPrjId(HttpServletRequest request,@RequestBody String prjId){
    	log.info("查询项目下的网络列表");
    	JSONObject json = new JSONObject ();
    	try{
    		List<CloudNetWork> netList = vmService.queryNetListByPrjId(prjId);
    		json.put("data", netList);
    	}catch(Exception e){
    		throw e;
    	}
    	return json.toJSONString();
    }
    
    /**
     * 查询项目下的安全组列表
     * 
     * @param request
     * @param prjId
     * @return
     */
    @RequestMapping(value="/querySgListByPrjId", method = RequestMethod.POST)
    @ResponseBody
    public String querySgListByPrjId(HttpServletRequest request,@RequestBody String prjId){
    	log.info("查询项目下的安全组列表");
    	JSONObject json = new JSONObject ();
    	try{
    		List<CloudSecurityGroup> sgList = vmService.querySgListByPrjId(prjId);
    		json.put("data", sgList);
    	}catch(Exception e){
    		throw e;
    	}
    	return json.toJSONString();
    }
    /**
     * 续费提交订单，校验是否可以创建订单
     * @param request
     * @param map
     * @return
     * @throws Exception 
     */
    @RequestMapping(value="/renewVmOrderConfirm", method = RequestMethod.POST)
    @ResponseBody
    public String renewVmOrderConfirm(HttpServletRequest request,@RequestBody Map<String,String> map) throws Exception{
    	log.info("主机续费提交订单校验开始");
    	SessionUserInfo sessionUser = (SessionUserInfo) request.getSession().getAttribute(ConstantClazz.SYS_SESSION_USERINFO);
    	String userId = sessionUser.getUserId();
    	String userName = sessionUser.getUserName();
    	String cusId = sessionUser.getCusId();
    	String opIp = sessionUser.getIP();
    	map.put("operatorIp", opIp);//操作者ip
    	
    	JSONObject json = new JSONObject ();
    	try{
    		json = vmService.renewVmOrderConfirm(map, userId, userName,cusId);
    	}catch(Exception e){
    		throw e;
    	}
    	return json.toJSONString();
    }
    /**
     * 续费弹出页点击确定时，校验是否存在未完成订单
     * @param request
     * @param vmId
     * @return
     * @throws Exception 
     */
    @RequestMapping(value = "/checkVmOrderExist" , method = RequestMethod.POST)
    @ResponseBody
    public String checkVmOrderExist(HttpServletRequest request, @RequestBody String vmId) throws Exception{
        log.info("检查当前是否已存在主机续费或变配的未完成订单");
        JSONObject json = new JSONObject();
        try{
            boolean flag = vmService.checkVmOrderExsit(vmId);
            json.put("flag", flag);
        }catch(Exception e){
            throw e;
        }
        return json.toJSONString();
    }
    
    /**
     * 根据网络ID查询子网信息
     * @param request
     * @param subnet
     * @return
     */
    @RequestMapping(value = "querySubnetByNet" , method = RequestMethod.POST)
    @ResponseBody
    public String querySubnetByNet(HttpServletRequest request , @RequestBody CloudSubNetWork subnet){
    	log.info("根据网络ID查询子网信息开始");
    	EayunResponseJson json = new EayunResponseJson();
        List<CloudSubNetWork> subnetList = new ArrayList<CloudSubNetWork>();
        try {
        	subnetList = vmService.querySubnetByNet(subnet);
        	json.setData(subnetList);
        	json.setRespCode(ConstantClazz.SUCCESS_CODE);
		} catch (Exception e) {
			json.setRespCode(ConstantClazz.ERROR_CODE);
			log.error(e.getMessage(),e);
		}
    	return JSONObject.toJSONString(json);
    }
    
    /**
     * 查询云主机的计费清单及总价
     * @param request
     * @param subnet
     * @return
     */
    @RequestMapping(value = "getPriceDetails" , method = RequestMethod.POST)
    @ResponseBody
    public String getPriceDetails (HttpServletRequest request , @RequestBody  ParamBean paramBean){
		log.info("返回总价和每一种计费单位的价钱（乘以批量数）");
		EayunResponseJson json = new EayunResponseJson();
		try {
			PriceDetails priceDetails= vmService.getPriceDetails(paramBean);
			json.setData(priceDetails);
		} catch (AppException e) {
		    log.error(e.toString(),e);
			json.setRespCode(ConstantClazz.ERROR_CODE);
        	json.setMessage(e.getMessage());
		} catch (Exception e) {
			json.setMessage(e.getMessage());
			log.error(e.toString(),e);
		}
		return JSONObject.toJSONString(json);
	
    }
    
    /**
     * 获取市场镜像的业务类型配置信息列表
     * ------------------
     * @author zhouhaitao
     * @param request
     * 
     * @return
     */
    @RequestMapping(value = "/getMarketImageTypeList" , method = RequestMethod.POST)
    @ResponseBody
    public String getMarketImageTypeList(HttpServletRequest request){
    	log.info("市场镜像的业务类型信息列表");
    	List<SysDataTree> list = new ArrayList<SysDataTree>();
    	try{
    		list=vmService.getMarketImageTypeList();
    	}catch(AppException e){
    		throw e;
    	}catch(Exception e ){
    		throw new AppException("");
    	}
    	return JSONObject.toJSONString(list);
    }
    
    /**
     * <p>查询项目下未关联制定云主机的SSH密钥列表</p>
     * ------------------
     * @author zhouhaitao
     * @param request
     * 
     * @return
     */
    @RequestMapping(value = "/getUnbindSecretkeyByPrj" , method = RequestMethod.POST)
    @ResponseBody
    public String getUnbindSecretkeyByPrj(HttpServletRequest request,@RequestBody CloudVm cloudVm){
    	log.info("查询项目下未关联制定云主机的SSH密钥列表");
    	List<CloudSecretKey> list = new ArrayList<CloudSecretKey>();
    	try{
    		list = vmService.getUnbindSecretkeyByPrj(cloudVm.getPrjId(),cloudVm.getVmId());
    	}catch(AppException e){
    		throw e;
    	}catch(Exception e ){
    		throw new AppException("");
    	}
    	return JSONObject.toJSONString(list);
    }
    
    /**
     * <p>查询云主机关联制定云主机的SSH密钥列表</p>
     * ------------------
     * @author zhouhaitao
     * @param request
     * 
     * @return
     */
    @RequestMapping(value = "/getBindSecretkeyByVm" , method = RequestMethod.POST)
    @ResponseBody
    public String getBindSecretkeyByVm(HttpServletRequest request,@RequestBody String vmId){
    	log.info("查询云主机关联制定云主机的SSH密钥列表");
    	List<CloudSecretKey> list = new ArrayList<CloudSecretKey>();
    	try{
    		list = vmService.getBindSecretkeyByVm(vmId);
    	}catch(AppException e){
    		throw e;
    	}catch(Exception e ){
    		throw new AppException("");
    	}
    	return JSONObject.toJSONString(list);
    }
    
    /**
     * <p>绑定/解绑SSH密钥</p>
     * ------------------
     * @author zhouhaitao
     * @param request
     * 
     * @return
     */
    @RequestMapping(value = "/editSecretKey" , method = RequestMethod.POST)
    @ResponseBody
    public String editSecretKey(HttpServletRequest request,@RequestBody CloudVm cloudVm){
    	log.info("绑定/解绑SSH密钥");
    	JSONObject json  = new JSONObject();
    	try{
    		vmService.editSecretKey(cloudVm);
    		json.put("respCode", ConstantClazz.SUCCESS_CODE_OP);
    		if(cloudVm.getSshAddCount() > 0){
    			logService.addLog("绑定SSH密钥",  ConstantClazz.LOG_TYPE_KEYPAIRS, cloudVm.getVmName(), cloudVm.getPrjId(),  ConstantClazz.LOG_STATU_SUCCESS,null);
    		}
    		if(cloudVm.getSshDelCount() > 0){
    			logService.addLog("解绑SSH密钥",  ConstantClazz.LOG_TYPE_KEYPAIRS, cloudVm.getVmName(), cloudVm.getPrjId(),  ConstantClazz.LOG_STATU_SUCCESS,null);
    		}
    	}catch(Exception e ){
    		if(cloudVm.getSshAddCount() > 0){
    			logService.addLog("绑定SSH密钥",  ConstantClazz.LOG_TYPE_KEYPAIRS, cloudVm.getVmName(), cloudVm.getPrjId(),  ConstantClazz.LOG_STATU_ERROR,e);
    		}
    		if(cloudVm.getSshDelCount() > 0){
    			logService.addLog("解绑SSH密钥",  ConstantClazz.LOG_TYPE_KEYPAIRS, cloudVm.getVmName(), cloudVm.getPrjId(),  ConstantClazz.LOG_STATU_ERROR,e);
    		}
    		log.error(e.getMessage(),e);
    		json.put("respCode", ConstantClazz.ERROR_CODE);
    		throw e;
    	}
    	return json.toJSONString();
    }
    
    /**
     * <p>查询云主机绑定的路由信息</p>
     * ------------------
     * @author zhouhaitao
     * @param request
     * 
     * @return
     */
    @RequestMapping(value = "/queryRouteInfoByVm" , method = RequestMethod.POST)
    @ResponseBody
    public String queryRouteInfoByVm(HttpServletRequest request,@RequestBody String vmId){
    	log.info("查询云主机绑定的路由信息");
    	JSONObject json  = new JSONObject();
    	try{
    		CloudVm cloudVm = vmService.queryRouteByVm(vmId);
    		json.put("data", cloudVm);
    	}catch(Exception e ){
    		log.error(e.getMessage(),e);
    		throw e;
    	}
    	return json.toJSONString();
    }
    
    /**
     * 
     * <p>修改云主机密码</p>
     * ----------------------------
     * @author zhouhaitao
     * 
     * @param request
     * @param cloudVm
     * @return
     */
    @RequestMapping(value="modifyPwd" , method = RequestMethod.POST)
    @ResponseBody
    public String modifyPwd (HttpServletRequest request,@RequestBody CloudVm cloudVm){
    	JSONObject json = new JSONObject();
    	try{
    		vmService.modifyPwd(cloudVm);
    		json.put("respCode", ConstantClazz.SUCCESS_CODE_OP);
			logService.addLog("修改密码",  ConstantClazz.LOG_TYPE_HOST, cloudVm.getVmName(), cloudVm.getPrjId(),  ConstantClazz.LOG_STATU_SUCCESS,null);
    	}catch(Exception e ){
			logService.addLog("修改密码",  ConstantClazz.LOG_TYPE_HOST, cloudVm.getVmName(), cloudVm.getPrjId(),  ConstantClazz.LOG_STATU_ERROR,e);
    		json.put("respCode", ConstantClazz.ERROR_CODE);
    		throw e;
    	}
    	return json.toJSONString();
    }
    
}
