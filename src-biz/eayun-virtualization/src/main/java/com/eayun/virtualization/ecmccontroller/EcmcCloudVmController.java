package com.eayun.virtualization.ecmccontroller;

import java.util.ArrayList;
import java.util.HashMap;
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
import com.eayun.common.model.EayunResponseJson;
import com.eayun.ecmcuser.model.BaseEcmcSysUser;
import com.eayun.ecmcuser.util.EcmcSessionUtil;
import com.eayun.log.ecmcsevice.EcmcLogService;
import com.eayun.sys.model.SysDataTree;
import com.eayun.virtualization.ecmcservice.EcmcCloudVmService;
import com.eayun.virtualization.model.CloudImage;
import com.eayun.virtualization.model.CloudProject;
import com.eayun.virtualization.model.CloudSecurityGroup;
import com.eayun.virtualization.model.CloudSubNetWork;
import com.eayun.virtualization.model.CloudVm;

/**
 * ECMC 运维云主机接口
 * 
 * @Filename: EcmcCloudVmController.java
 * @Description: 
 * @Version: 1.0
 * @Author: duanbinbin
 * @Email: binbin.duan@eayun.com
 * @History:<br>
 *<li>Date: 2016年4月20日</li>
 *<li>Version: 1.0</li>
 *<li>Content: create</li>
 *
 */
@Controller
@RequestMapping("/ecmc/cloud/vm")
@Scope("prototype")
public class EcmcCloudVmController {
	public final static Logger log = LoggerFactory.getLogger(EcmcCloudVmController.class);

	@Autowired
	private EcmcCloudVmService ecmcCloudVmService;
	
	@Autowired
    private EcmcLogService ecmcLogService;
	
	/**
	 * 打开云主机控制台
	 * 返回的是一个URL
	 * @Author: duanbinbin
	 * @param request
	 * @param cloudVm
	 * @return
	 *<li>Date: 2016年4月22日</li>
	 */
	@RequestMapping(value = "/consolevm" , method = RequestMethod.POST)
    @ResponseBody
    public String  consoleVm(HttpServletRequest request,@RequestBody CloudVm cloudVm){
    	log.info("打开云主机控制台开始");
    	EayunResponseJson json = new EayunResponseJson();
    	String vmConsoleUrl = "" ;
    	try {
			vmConsoleUrl = ecmcCloudVmService.ConsoleUrl(cloudVm);
			
			json.setData(vmConsoleUrl);
			json.setRespCode(ConstantClazz.SUCCESS_CODE);
		} catch(AppException e){
        	json.setRespCode(ConstantClazz.ERROR_CODE);
        	log.error(e.toString(),e);
        	throw e;
    	} catch (Exception e) {
    		json.setRespCode(ConstantClazz.ERROR_CODE);
        	log.error(e.toString(),e);
        	throw e;
		}
    	return JSONObject.toJSONString(json);
    	
    }
	/**
	 * 关闭云主机
	 * @Author: duanbinbin
	 * @param request
	 * @param cloudVm
	 * @return
	 *<li>Date: 2016年4月22日</li>
	 */
	@RequestMapping(value = "/shutdownvm" , method = RequestMethod.POST)
    @ResponseBody
    public String  shutdownVm(HttpServletRequest request,@RequestBody CloudVm cloudVm){
    	log.info("关闭云主机开始");
    	EayunResponseJson json = new EayunResponseJson();
    	try {
			ecmcCloudVmService.shutdownVm(cloudVm);
			json.setRespCode(ConstantClazz.SUCCESS_CODE);
			ecmcLogService.addLog("关闭云主机", ConstantClazz.LOG_TYPE_HOST, cloudVm.getVmName(), cloudVm.getPrjId(), 1, cloudVm.getVmId(), null);
		} catch(AppException e){
        	json.setRespCode(ConstantClazz.ERROR_CODE);
        	log.error(e.toString(),e);
        	ecmcLogService.addLog("关闭云主机", ConstantClazz.LOG_TYPE_HOST, cloudVm.getVmName(), cloudVm.getPrjId(), 0, cloudVm.getVmId(), e);
        	throw e;
    	} catch (Exception e) {
    		json.setRespCode(ConstantClazz.ERROR_CODE);
        	log.error(e.toString(),e);
        	ecmcLogService.addLog("关闭云主机", ConstantClazz.LOG_TYPE_HOST, cloudVm.getVmName(), cloudVm.getPrjId(), 0, cloudVm.getVmId(), e);
        	throw e;
		}
    	return JSONObject.toJSONString(json);
    	
    }
	/**
	 * 启动云主机
	 * @Author: duanbinbin
	 * @param request
	 * @param cloudVm
	 * @return
	 *<li>Date: 2016年4月22日</li>
	 */
	@RequestMapping(value = "/startvm" , method = RequestMethod.POST)
    @ResponseBody
    public String  startVm(HttpServletRequest request,@RequestBody CloudVm cloudVm){
    	log.info("启动云主机开始");
    	EayunResponseJson json = new EayunResponseJson();
    	try {
			ecmcCloudVmService.startVm(cloudVm);
			json.setRespCode(ConstantClazz.SUCCESS_CODE);
			ecmcLogService.addLog("启动云主机", ConstantClazz.LOG_TYPE_HOST, cloudVm.getVmName(), cloudVm.getPrjId(), 1, cloudVm.getVmId(), null);
		} catch(AppException e){
        	json.setRespCode(ConstantClazz.ERROR_CODE);
        	log.error(e.toString(),e);
        	ecmcLogService.addLog("启动云主机", ConstantClazz.LOG_TYPE_HOST, cloudVm.getVmName(), cloudVm.getPrjId(), 0, cloudVm.getVmId(), e);
        	throw e;
    	} catch (Exception e) {
    		json.setRespCode(ConstantClazz.ERROR_CODE);
        	log.error(e.toString(),e);
        	ecmcLogService.addLog("启动云主机", ConstantClazz.LOG_TYPE_HOST, cloudVm.getVmName(), cloudVm.getPrjId(), 0, cloudVm.getVmId(), e);
        	throw e;
		}
    	return JSONObject.toJSONString(json);
    	
    }
	/**
	 * 重启云主机
	 * 即原来的软重启
	 * 硬重启不要了
	 * @Author: duanbinbin
	 * @param request
	 * @param cloudVm
	 * @return
	 *<li>Date: 2016年4月22日</li>
	 */
	@RequestMapping(value = "/restartvm" , method = RequestMethod.POST)
    @ResponseBody
    public String  restartVm(HttpServletRequest request,@RequestBody CloudVm cloudVm){
    	log.info("重启云主机开始");
    	EayunResponseJson json = new EayunResponseJson();
    	try {
			ecmcCloudVmService.restartVm(cloudVm);
			json.setRespCode(ConstantClazz.SUCCESS_CODE);
			ecmcLogService.addLog("重启云主机", ConstantClazz.LOG_TYPE_HOST, cloudVm.getVmName(), cloudVm.getPrjId(), 1, cloudVm.getVmId(), null);
		} catch(AppException e){
        	json.setRespCode(ConstantClazz.ERROR_CODE);
        	log.error(e.toString(),e);
        	ecmcLogService.addLog("重启云主机", ConstantClazz.LOG_TYPE_HOST, cloudVm.getVmName(), cloudVm.getPrjId(), 0, cloudVm.getVmId(), e);
        	throw e;
    	} catch (Exception e) {
    		json.setRespCode(ConstantClazz.ERROR_CODE);
        	log.error(e.toString(),e);
        	ecmcLogService.addLog("重启云主机", ConstantClazz.LOG_TYPE_HOST, cloudVm.getVmName(), cloudVm.getPrjId(), 0, cloudVm.getVmId(), e);
        	throw e;
		}
    	return JSONObject.toJSONString(json);
    	
    }
	/**
	 * 挂起云主机
	 * @Author: duanbinbin
	 * @param request
	 * @param cloudVm
	 * @return
	 *<li>Date: 2016年4月22日</li>
	 */
	@RequestMapping(value = "/suspendvm" , method = RequestMethod.POST)
    @ResponseBody
    public String  suspendVm(HttpServletRequest request,@RequestBody CloudVm cloudVm){
    	log.info("挂起云主机开始");
    	EayunResponseJson json = new EayunResponseJson();
    	try {
			ecmcCloudVmService.suspendVm(cloudVm);
			json.setRespCode(ConstantClazz.SUCCESS_CODE);
			ecmcLogService.addLog("挂起云主机", ConstantClazz.LOG_TYPE_HOST, cloudVm.getVmName(), cloudVm.getPrjId(), 1, cloudVm.getVmId(), null);
		} catch(AppException e){
        	json.setRespCode(ConstantClazz.ERROR_CODE);
        	log.error(e.toString(),e);
        	ecmcLogService.addLog("挂起云主机", ConstantClazz.LOG_TYPE_HOST, cloudVm.getVmName(), cloudVm.getPrjId(), 0, cloudVm.getVmId(), e);
        	throw e;
    	} catch (Exception e) {
    		json.setRespCode(ConstantClazz.ERROR_CODE);
        	log.error(e.toString(),e);
        	ecmcLogService.addLog("挂起云主机", ConstantClazz.LOG_TYPE_HOST, cloudVm.getVmName(), cloudVm.getPrjId(), 0, cloudVm.getVmId(), e);
        	throw e;
		}
    	return JSONObject.toJSONString(json);
    	
    }
	/**
	 * 恢复云主机
	 * @Author: duanbinbin
	 * @param request
	 * @param cloudVm
	 * @return
	 *<li>Date: 2016年4月22日</li>
	 */
	@RequestMapping(value = "/resumevm" , method = RequestMethod.POST)
    @ResponseBody
    public String  resumeVm(HttpServletRequest request,@RequestBody CloudVm cloudVm){
    	log.info("恢复云主机开始");
    	EayunResponseJson json = new EayunResponseJson();
    	try {
			ecmcCloudVmService.resumeVm(cloudVm);
			json.setRespCode(ConstantClazz.SUCCESS_CODE);
			ecmcLogService.addLog("恢复云主机", ConstantClazz.LOG_TYPE_HOST, cloudVm.getVmName(), cloudVm.getPrjId(), 1, cloudVm.getVmId(), null);
		} catch(AppException e){
        	json.setRespCode(ConstantClazz.ERROR_CODE);
        	log.error(e.toString(),e);
        	ecmcLogService.addLog("恢复云主机", ConstantClazz.LOG_TYPE_HOST, cloudVm.getVmName(), cloudVm.getPrjId(), 0, cloudVm.getVmId(), e);
        	throw e;
    	} catch (Exception e) {
    		json.setRespCode(ConstantClazz.ERROR_CODE);
        	log.error(e.toString(),e);
        	ecmcLogService.addLog("恢复云主机", ConstantClazz.LOG_TYPE_HOST, cloudVm.getVmName(), cloudVm.getPrjId(), 0, cloudVm.getVmId(), e);
        	throw e;
		}
    	return JSONObject.toJSONString(json);
    	
    }
	/**
	 * 创建自定义镜像
	 * @Author: duanbinbin
	 * @param request
	 * @param cloudVm
	 * @return
	 *<li>Date: 2016年4月22日</li>
	 */
	@RequestMapping(value = "/createsnapshot" , method = RequestMethod.POST)
    @ResponseBody
    public String  createSnapshot(HttpServletRequest request,@RequestBody CloudVm cloudVm){
    	log.info("创建自定义镜像开始");
    	EayunResponseJson json = new EayunResponseJson();
    	BaseEcmcSysUser sysUser = EcmcSessionUtil.getUser();
    	try {
			ecmcCloudVmService.createSnapshot(sysUser,cloudVm);
			json.setRespCode(ConstantClazz.SUCCESS_CODE);
			ecmcLogService.addLog("创建自定义镜像", ConstantClazz.LOG_TYPE_HOST, cloudVm.getVmName(), cloudVm.getPrjId(), 1, cloudVm.getVmId(), null);
		}catch (AppException e) {
			json.setRespCode(ConstantClazz.ERROR_CODE);
			log.error(e.toString(),e);
			ecmcLogService.addLog("创建自定义镜像", ConstantClazz.LOG_TYPE_HOST, cloudVm.getVmName(), cloudVm.getPrjId(), 0, cloudVm.getVmId(), e);
	        throw e;
	    } catch (Exception e) {
	    	json.setRespCode(ConstantClazz.ERROR_CODE);
			log.error(e.toString(),e);
			ecmcLogService.addLog("创建自定义镜像", ConstantClazz.LOG_TYPE_HOST, cloudVm.getVmName(), cloudVm.getPrjId(), 0, cloudVm.getVmId(), e);
	    	throw e;
		}
    	return JSONObject.toJSONString(json);
    	
    }
	/**
	 * 删除云主机
	 * @Author: duanbinbin
	 * @param request
	 * @param cloudVm
	 * @return
	 *<li>Date: 2016年4月22日</li>
	 * @throws Exception 
	 */
	@RequestMapping(value = "/deletevm" , method = RequestMethod.POST)
    @ResponseBody
    public String  deleteVm(HttpServletRequest request,@RequestBody CloudVm cloudVm) throws Exception{
    	log.info("删除云主机开始");
    	String opName = "删除云主机";
    	EayunResponseJson json = new EayunResponseJson();
    	BaseEcmcSysUser sysUser = EcmcSessionUtil.getUser();
    	try {
    		if("2".equals(cloudVm.getDeleteType())){
        		opName = "销毁云主机";
        	}
			ecmcCloudVmService.deleteVm(sysUser,cloudVm);
			json.setRespCode(ConstantClazz.SUCCESS_CODE);
			ecmcLogService.addLog(opName, ConstantClazz.LOG_TYPE_HOST, cloudVm.getVmName(), cloudVm.getPrjId(), 1, cloudVm.getVmId(), null);
		}catch (AppException e) {
			json.setRespCode(ConstantClazz.ERROR_CODE);
			log.error(e.toString(),e);
			ecmcLogService.addLog(opName, ConstantClazz.LOG_TYPE_HOST, cloudVm.getVmName(), cloudVm.getPrjId(), 0, cloudVm.getVmId(), e);
	        throw e;
	    } catch (Exception e) {
	    	json.setRespCode(ConstantClazz.ERROR_CODE);
			log.error(e.toString(),e);
			ecmcLogService.addLog(opName, ConstantClazz.LOG_TYPE_HOST, cloudVm.getVmName(), cloudVm.getPrjId(), 0, cloudVm.getVmId(), e);
	    	throw e;
		}
    	return JSONObject.toJSONString(json);
    	
    }
	
	/**
	 * 查询当前云主机已经挂载的云硬盘数
	 * 包括系统盘
	 * @Author: duanbinbin
	 * @param request
	 * @param map
	 * @return
	 *<li>Date: 2016年4月22日</li>
	 * @throws Exception 
	 */
	@RequestMapping(value = "/getdiskcountbyvm" , method = RequestMethod.POST)
    @ResponseBody
    public String  getDiskCountByVm(HttpServletRequest request,@RequestBody Map map) throws Exception{
    	log.info("查询当前云主机已经挂载的云硬盘数开始");
    	String vmId = map.get("vmId").toString();
    	EayunResponseJson json = new EayunResponseJson();
    	try {
			int count = ecmcCloudVmService.getDiskCountByVm(vmId);
			json.setData(count);
			json.setRespCode(ConstantClazz.SUCCESS_CODE);
		} catch (Exception e) {
	    	json.setRespCode(ConstantClazz.ERROR_CODE);
			log.error(e.toString(),e);
			log.error(e.getMessage());
	    	throw e;
		}
    	return JSONObject.toJSONString(json);
    	
    }
	/**
	 * 批量挂载云硬盘
	 * 云主机连同系统盘在内最多只能挂载5个云硬盘
	 * @Author: duanbinbin
	 * @param request
	 * @param map
	 * @return
	 *<li>Date: 2016年4月22日</li>
	 */
	@SuppressWarnings("unchecked")
	@RequestMapping(value = "/bindbatchvolume" , method = RequestMethod.POST)
    @ResponseBody
    public String  bindBatchVolume(HttpServletRequest request , @RequestBody Map map){
    	log.info("批量挂载云硬盘开始");
    	EayunResponseJson json = new EayunResponseJson();
    	List<Map<String,String>> volList = (List<Map<String,String>>) map.get("volList");
    	Map<String ,Object> resultMap = new HashMap<String ,Object>();
    	int successCount = 0;
    	try {
    		resultMap = ecmcCloudVmService.bindBatchVolume(volList);
		}catch (AppException e) {
			log.error(e.getMessage());
	    } catch (Exception e) {
			log.error(e.getMessage());
		}
    	finally{
    		for(int i =0 ;i<volList.size();i++){
    			Map<String,String >data = volList.get(i);
    			String result = String.valueOf(data.get("isSuccess"));
    			if(!StringUtils.isEmpty(result) && "true".equals(result)){
    				successCount++;
    				ecmcLogService.addLog("挂载云硬盘", ConstantClazz.LOG_TYPE_DISK, data.get("volName"), data.get("prjId"), 1, data.get("volId"), null);
    			}
    			else{
    				ecmcLogService.addLog("挂载云硬盘", ConstantClazz.LOG_TYPE_DISK, data.get("volName"), data.get("prjId"), 0, data.get("volId"), null);
    			}
    		}
    		resultMap.put("suCount", successCount);
    		json.setData(resultMap);
			json.setRespCode(ConstantClazz.SUCCESS_CODE);
    	}
    	return JSONObject.toJSONString(json);
    	
    }
	/**
	 * 查询镜像列表
	 * 创建时调用
	 * @Author: duanbinbin
	 * @param request
	 * @param cloudVm
	 * @return
	 *<li>Date: 2016年4月22日</li>
	 */
	@RequestMapping(value = "/getimagelist" , method = RequestMethod.POST)
    @ResponseBody
    public String  getImageList(HttpServletRequest request,@RequestBody CloudVm cloudVm){
    	log.info("查询镜像列表开始");
    	EayunResponseJson json = new EayunResponseJson();
    	List<CloudImage> list = new ArrayList<CloudImage>();
    	try {
			list = ecmcCloudVmService.getImageList(cloudVm);
			json.setData(list);
			json.setRespCode(ConstantClazz.SUCCESS_CODE);
		}catch(AppException e){
			json.setRespCode(ConstantClazz.ERROR_CODE);
    		throw e;
    	} catch (Exception e) {
    		json.setRespCode(ConstantClazz.ERROR_CODE);
    		throw e;
		}
    	return JSONObject.toJSONString(json);
    	
    }
	/**
	 * 查询云主机日志，只是个字符串
	 * @Author: duanbinbin
	 * @param request
	 * @param cloudVm
	 * @return
	 *<li>Date: 2016年4月22日</li>
	 */
	@RequestMapping(value = "/getvmlog" , method = RequestMethod.POST)
    @ResponseBody
    public String  getVmLog(HttpServletRequest request,@RequestBody CloudVm cloudVm){
    	log.info("查询云主机日志开始");
    	String vmLogs = "" ;
    	EayunResponseJson json = new EayunResponseJson();
    	try {
    		vmLogs = ecmcCloudVmService.getVmLog(cloudVm);
    		json.setData(vmLogs);
			json.setRespCode(ConstantClazz.SUCCESS_CODE);
		} catch(AppException e){
        	json.setRespCode(ConstantClazz.ERROR_CODE);
        	log.error(e.getMessage());
        	log.error(e.toString(),e);
        	throw e;
    	} catch (Exception e) {
    		json.setRespCode(ConstantClazz.ERROR_CODE);
        	log.error(e.getMessage());
        	log.error(e.toString(),e);
        	throw e;
		}
    	return JSONObject.toJSONString(json);
    	
    }
	/**
	 * 项目下的子网列表
	 * 用于创建时选择
	 * @Author: duanbinbin
	 * @param request
	 * @param prjId
	 * @return
	 *<li>Date: 2016年4月22日</li>
	 */
	@RequestMapping(value = "/getsubnetlistbyprjid" , method = RequestMethod.POST)
    @ResponseBody
    public String  getSubNetListByPrjId(HttpServletRequest request , @RequestBody Map map){
    	log.info("查询项目下的子网列表开始");
    	String prjId = null == map.get("prjId")?"":map.get("prjId").toString();
    	EayunResponseJson json = new EayunResponseJson();
    	List<CloudSubNetWork> subList = new ArrayList<CloudSubNetWork>();
    	try {
			subList = ecmcCloudVmService.getSubNetListByPrjId(prjId);
			json.setData(subList);
			json.setRespCode(ConstantClazz.SUCCESS_CODE);
		}catch(AppException e){
			json.setRespCode(ConstantClazz.ERROR_CODE);
			log.error(e.toString(),e);
    		throw e;
    	} catch (Exception e) {
    		json.setRespCode(ConstantClazz.ERROR_CODE);
    		log.error(e.toString(),e);
    		throw e;
		}
    	return JSONObject.toJSONString(json);
    	
    }
	/**
	 * 查询云主机详情，进入详情页面
	 * 新增关联安全组名称列表，所属客户
	 * 去掉关联标签
	 * @Author: duanbinbin
	 * @param request
	 * @param map
	 * @return
	 *<li>Date: 2016年4月22日</li>
	 */
	@RequestMapping(value = "/getvmbyid" , method = RequestMethod.POST)
    @ResponseBody
    public String  getVmById(HttpServletRequest request , @RequestBody Map map){
    	log.info("获取云主机详情开始");
    	EayunResponseJson json = new EayunResponseJson();
    	String vmId = null == map.get("vmId")?"":map.get("vmId").toString();
    	try {
			CloudVm vm = ecmcCloudVmService.getVmById(vmId);
			if(vm != null){
				json.setData(vm);
				json.setRespCode(ConstantClazz.SUCCESS_CODE);
			}else{
				json.setRespCode(ConstantClazz.ERROR_CODE);
			}
    	} catch (Exception e) {
    		json.setRespCode(ConstantClazz.ERROR_CODE);
    		log.error(e.toString(),e);
//    		throw e;
		}
    	return JSONObject.toJSONString(json);
    	
    }
	/**
	 * 查询云主机列表
	 * @Author: duanbinbin
	 * @param dcId		数据中心id
	 * @param vmStatus	云主机状态（三项）
	 * @param sysType	操作系统
	 * @param timesort	到期时间排序
	 * @param queryType	输入框查询类型
	 * @param queryName	输入框查询内容
	 * @return
	 *<li>Date: 2016年4月22日</li>
	 */
	@RequestMapping(value = "/getvmpagelist" , method = RequestMethod.POST)
    @ResponseBody
    public String  getVmPageList(HttpServletRequest request,Page page,@RequestBody ParamsMap map){
    	log.info("查询云主机列表开始");
    	String dcId = "";
    	String vmStatus = "";
    	String sysType = "";
    	String timesort = "";
    	String queryType = "";
    	String queryName = "";
    	
    	if(null != map.getParams()){
    		dcId = null == map.getParams().get("dcId")?"":map.getParams().get("dcId").toString();
        	vmStatus = null == map.getParams().get("vmStatus")?"":map.getParams().get("vmStatus").toString();//“状态”下拉框中的值为：“运行中”、“已关机”、“故障”
        	sysType = null == map.getParams().get("sysType")?"":map.getParams().get("sysType").toString();
        	timesort = null == map.getParams().get("timesort")?"":map.getParams().get("timesort").toString();//按照到期时间的排序：ASC（最短），DESC（最长），""默认
        	queryType = null == map.getParams().get("queryType")?"":map.getParams().get("queryType").toString();
        	//四类：模糊查询：名称（vmName）、IP地址（包括内网和公网模糊查询）（ip）
        	//多选精确查询：项目（prjName）、客户（cusOrg）
        	queryName = null == map.getParams().get("queryName")?"":map.getParams().get("queryName").toString();
    	}
    	int pageSize = map.getPageSize();
        int pageNumber = map.getPageNumber();
        
        QueryMap queryMap=new QueryMap();
        queryMap.setPageNum(pageNumber);
        queryMap.setCURRENT_ROWS_SIZE(pageSize);
        
        try {
			page = ecmcCloudVmService.getVmPage(page,queryMap,dcId,vmStatus,sysType,timesort,queryType,queryName);
		} catch (Exception e) {
			log.error(e.toString(),e);
			throw e;
		}
    	return JSONObject.toJSONString(page);
    	
    }
	/**
	 * 编辑云主机，可编辑的信息是名称和描述，描述不用去底层
	 * @Author: duanbinbin
	 * @param request
	 * @param cloudVm
	 * @return
	 *<li>Date: 2016年4月22日</li>
	 */
	@RequestMapping(value = "/updatevm" , method = RequestMethod.POST)
    @ResponseBody
    public String  updateVm(HttpServletRequest request,@RequestBody CloudVm cloudVm){
    	log.info("编辑云主机开始");
    	EayunResponseJson json = new EayunResponseJson();
    	try {
			ecmcCloudVmService.updateVm(cloudVm);
			json.setRespCode(ConstantClazz.SUCCESS_CODE);
		}catch (AppException e) {
			json.setRespCode(ConstantClazz.ERROR_CODE);
			log.error(e.toString(),e);
			log.error(e.getMessage());
	        throw e;
	    } catch (Exception e) {
	    	json.setRespCode(ConstantClazz.ERROR_CODE);
			log.error(e.toString(),e);
			log.error(e.getMessage());
	    	throw e;
		}
    	return JSONObject.toJSONString(json);
    	
    }
	/**
	 * 确定编辑云主机的安全组
	 * @Author: duanbinbin
	 * @param request
	 * @param cloudVm
	 * @return
	 *<li>Date: 2016年4月22日</li>
	 */
	@RequestMapping(value = "/editvmsecuritygroup" , method = RequestMethod.POST)
    @ResponseBody
    public String  editVmSecurityGroup(HttpServletRequest request,@RequestBody CloudVm cloudVm){
    	log.info("编辑云主机安全组开始");
    	EayunResponseJson json = new EayunResponseJson();
    	try {
			ecmcCloudVmService.editVmSecurityGroup(cloudVm);
			json.setRespCode(ConstantClazz.SUCCESS_CODE);
			ecmcLogService.addLog("编辑云主机安全组", ConstantClazz.LOG_TYPE_HOST, cloudVm.getVmName(), cloudVm.getPrjId(), 1, cloudVm.getVmId(), null);
		}catch(AppException e){
			json.setRespCode(ConstantClazz.ERROR_CODE);
			log.error(e.toString(),e);
			ecmcLogService.addLog("编辑云主机安全组", ConstantClazz.LOG_TYPE_HOST, cloudVm.getVmName(), cloudVm.getPrjId(), 0, cloudVm.getVmId(), e);
    		throw e;
    	} catch (Exception e) {
    		json.setRespCode(ConstantClazz.ERROR_CODE);
    		log.error(e.toString(),e);
    		ecmcLogService.addLog("编辑云主机安全组", ConstantClazz.LOG_TYPE_HOST, cloudVm.getVmName(), cloudVm.getPrjId(), 0, cloudVm.getVmId(), e);
    		throw e;
		}
    	return JSONObject.toJSONString(json);
    	
    }
	@RequestMapping(value = "/unbindipbyvmid" , method = RequestMethod.POST)
    @ResponseBody
    public String  unBindIpByVmId(HttpServletRequest request,@RequestBody CloudVm cloudVm){
    	log.info("云主机解绑弹性公网IP");
    	EayunResponseJson json = new EayunResponseJson();
    	try {
			ecmcCloudVmService.unBindIpByVmId(cloudVm);
			json.setRespCode(ConstantClazz.SUCCESS_CODE);
			ecmcLogService.addLog("解绑弹性公网IP", ConstantClazz.LOG_TYPE_HOST, cloudVm.getVmName(), cloudVm.getPrjId(), 1, cloudVm.getVmId(), null);
		}catch(AppException e){
			json.setRespCode(ConstantClazz.ERROR_CODE);
			log.error(e.toString(),e);
			ecmcLogService.addLog("解绑弹性公网IP", ConstantClazz.LOG_TYPE_HOST, cloudVm.getVmName(), cloudVm.getPrjId(), 0, cloudVm.getVmId(), e);
    		throw e;
    	} catch (Exception e) {
    		json.setRespCode(ConstantClazz.ERROR_CODE);
    		log.error(e.toString(),e);
    		ecmcLogService.addLog("解绑弹性公网IP", ConstantClazz.LOG_TYPE_HOST, cloudVm.getVmName(), cloudVm.getPrjId(), 0, cloudVm.getVmId(), e);
    		throw e;
		}
    	return JSONObject.toJSONString(json);
    }
	/**
	 * 项目下未关联云主机的安全组
	 * @Author: duanbinbin
	 * @param request
	 * @param map
	 * @return
	 *<li>Date: 2016年4月22日</li>
	 */
	@RequestMapping(value = "/getsecuritybyprjnovm" , method = RequestMethod.POST)
    @ResponseBody
    public String  getSecurityByPrjNoVm(HttpServletRequest request,@RequestBody Map map){
    	log.info("查询项目下未关联某一云主机的安全组信息开始");
    	EayunResponseJson json = new EayunResponseJson();
    	String vmId = map.get("vmId").toString();
    	String prjId = map.get("prjId").toString();
    	List<CloudSecurityGroup> list = new ArrayList<CloudSecurityGroup>();
    	try {
			list = ecmcCloudVmService.getSecurityByPrjNoVm(vmId,prjId);
			json.setData(list);
			json.setRespCode(ConstantClazz.SUCCESS_CODE);
		} catch (Exception e) {
    		json.setRespCode(ConstantClazz.ERROR_CODE);
    		log.error(e.toString(),e);
    		throw e;
		}
    	return JSONObject.toJSONString(json);
    	
    }
	/**
	 * 该云主机已关联的安全组
	 * @Author: duanbinbin
	 * @param request
	 * @param map
	 * @return
	 *<li>Date: 2016年4月22日</li>
	 */
	@RequestMapping(value = "/getsecuritygroupbyvm" , method = RequestMethod.POST)
    @ResponseBody
    public String  getSecurityGroupByVm(HttpServletRequest request,@RequestBody Map map){
    	log.info("查询已关联某云主机的安全组列表信息开始");
    	EayunResponseJson json = new EayunResponseJson();
    	String vmId = map.get("vmId").toString();
    	String prjId = map.get("prjId").toString();
    	List<CloudSecurityGroup> list = new ArrayList<CloudSecurityGroup>();
    	try {
			list = ecmcCloudVmService.getSecurityGroupByVm(vmId,prjId);
			json.setData(list);
			json.setRespCode(ConstantClazz.SUCCESS_CODE);
		} catch (Exception e) {
    		json.setRespCode(ConstantClazz.ERROR_CODE);
    		log.error(e.toString(),e);
    		throw e;
		}
    	return JSONObject.toJSONString(json);
    	
    }
	/**
	 * 查询所有云主机状态
	 * 表头查询是需要三个状态：
	 * “运行中”/ACTIVE
	 * “已关机”/SHUTOFF
	 * “故障”/ERROR
	 * @Author: duanbinbin
	 * @param request
	 * @return
	 *<li>Date: 2016年4月22日</li>
	 */
	@RequestMapping(value = "/getvmstatuslist" , method = RequestMethod.POST)
    @ResponseBody
    public String  getVmStatusList(HttpServletRequest request){
    	log.info("查询云主机状态列表");
    	List<SysDataTree> list = new ArrayList<SysDataTree>();
    	EayunResponseJson json = new EayunResponseJson();
    	try{
    		list=ecmcCloudVmService.getVmStatusList();
    		json.setData(list);
			json.setRespCode(ConstantClazz.SUCCESS_CODE);
		} catch (Exception e) {
    		json.setRespCode(ConstantClazz.ERROR_CODE);
    		log.error(e.toString(),e);
    		throw e;
		}
    	return JSONObject.toJSONString(json);
    	
    }
	/**
	 * 查询所有创建云主机使用的操作系统
	 * 如：Win7、Centos...等
	 * @Author: duanbinbin
	 * @param request
	 * @return
	 *<li>Date: 2016年4月22日</li>
	 */
	@RequestMapping(value = "/getallvmsyslist" , method = RequestMethod.POST)
    @ResponseBody
    public String  getAllVmSysList(HttpServletRequest request){
    	log.info("查询所有的操作系统型号列表");
    	List<SysDataTree> list = new ArrayList<SysDataTree>();
    	EayunResponseJson json = new EayunResponseJson();
    	try{
    		list=ecmcCloudVmService.getAllVmSysList();
    		json.setData(list);
			json.setRespCode(ConstantClazz.SUCCESS_CODE);
		} catch (Exception e) {
    		json.setRespCode(ConstantClazz.ERROR_CODE);
    		log.error(e.toString(),e);
    		throw e;
		}
    	return JSONObject.toJSONString(json);
    	
    }
	/**
	 * 查询一个类型下的操作系统列表
	 * 如Linux下的操作系统列表
	 * @Author: duanbinbin
	 * @param request
	 * @param map
	 * @return
	 *<li>Date: 2016年4月22日</li>
	 */
	@RequestMapping(value = "/getsystypelistbyos" , method = RequestMethod.POST)
    @ResponseBody
    public String  getSysTypeListByOs(HttpServletRequest request,@RequestBody Map map){
    	log.info("根据某一系统类型获取操作系统列表开始");
    	String osId = null != map.get("osId")?map.get("osId").toString():"";
    	List<SysDataTree> list = new ArrayList<SysDataTree>();
    	EayunResponseJson json = new EayunResponseJson();
    	try{
    		list=ecmcCloudVmService.getSysTypeListByOs(osId);
    		json.setData(list);
			json.setRespCode(ConstantClazz.SUCCESS_CODE);
		} catch (Exception e) {
    		json.setRespCode(ConstantClazz.ERROR_CODE);
    		log.error(e.toString(),e);
    		throw e;
		}
    	return JSONObject.toJSONString(json);
    	
    }
	/**
	 * 获取系统类型，如Linux、Windows、其他
	 * @Author: duanbinbin
	 * @param request
	 * @return
	 *<li>Date: 2016年4月22日</li>
	 */
	@RequestMapping(value = "/getoslist" , method = RequestMethod.POST)
    @ResponseBody
    public String  getOsList(HttpServletRequest request){
    	log.info("获取系统类型开始");
    	List<SysDataTree> list = new ArrayList<SysDataTree>();
    	EayunResponseJson json = new EayunResponseJson();
    	try{
    		list=ecmcCloudVmService.getOsList();
    		json.setData(list);
			json.setRespCode(ConstantClazz.SUCCESS_CODE);
		} catch (Exception e) {
    		json.setRespCode(ConstantClazz.ERROR_CODE);
    		log.error(e.toString(),e);
    		throw e;
		}
    	return JSONObject.toJSONString(json);
    	
    }
	/**
	 * 获取CPU配置信息列表
	 * @Author: duanbinbin
	 * @param request
	 * @return
	 *<li>Date: 2016年4月22日</li>
	 */
	@RequestMapping(value = "/getcpulist" , method = RequestMethod.POST)
    @ResponseBody
    public String  getCpuList(HttpServletRequest request){
    	log.info("获取CPU配置信息列表开始");
    	List<SysDataTree> list = new ArrayList<SysDataTree>();
    	EayunResponseJson json = new EayunResponseJson();
    	try{
    		list=ecmcCloudVmService.getCpuList();
    		json.setData(list);
			json.setRespCode(ConstantClazz.SUCCESS_CODE);
		} catch (Exception e) {
    		json.setRespCode(ConstantClazz.ERROR_CODE);
    		log.error(e.toString(),e);
    		throw e;
		}
    	return JSONObject.toJSONString(json);
    	
    }
	/**
	 * 获取CPU下的内存配置信息列表
	 * @Author: duanbinbin
	 * @param request
	 * @param map
	 * @return
	 *<li>Date: 2016年4月22日</li>
	 */
	@RequestMapping(value = "/getramlistbycpu" , method = RequestMethod.POST)
    @ResponseBody
    public String  getRamListByCpu(HttpServletRequest request,@RequestBody Map map){
    	log.info("根据CPU信息获取内存配置信息列表开始");
    	String cpuId = map.get("cpuId").toString();
    	List<SysDataTree> list = new ArrayList<SysDataTree>();
    	EayunResponseJson json = new EayunResponseJson();
    	try{
    		list=ecmcCloudVmService.getRamListByCpu(cpuId);
    		json.setData(list);
			json.setRespCode(ConstantClazz.SUCCESS_CODE);
		} catch (Exception e) {
    		json.setRespCode(ConstantClazz.ERROR_CODE);
    		log.error(e.toString(),e);
    		throw e;
		}
    	return JSONObject.toJSONString(json);
    	
    }
	/**
	 * 项目下校验云主机是否重名
	 * true:可继续向下操作，即无重名
	 * false:不可继续向下操作，即有重名
	 * @Author: duanbinbin
	 * @param request
	 * @param cloudVm
	 * @return
	 *<li>Date: 2016年4月21日</li>
	 */
	@RequestMapping(value = "/checkvmname" , method = RequestMethod.POST)
    @ResponseBody
    public String  checkVmName(HttpServletRequest request,@RequestBody CloudVm cloudVm){
    	log.info("校验项目下云主机名称重名开始");
    	EayunResponseJson json = new EayunResponseJson();
    	try {
			boolean isok = ecmcCloudVmService.checkVmName(cloudVm);
			json.setData(isok);
			json.setRespCode(ConstantClazz.SUCCESS_CODE);
		} catch (Exception e) {
    		json.setRespCode(ConstantClazz.ERROR_CODE);
    		log.error(e.toString(),e);
    		throw e;
		}
    	return JSONObject.toJSONString(json);
    	
    }
	/**
	 * 查询项目下可绑定云硬盘的云主机列表
	 * @Author: duanbinbin
	 * @param request
	 * @param map
	 * @return
	 *<li>Date: 2016年4月28日</li>
	 */
	@RequestMapping(value = "/getcanbindcloudvmList" , method = RequestMethod.POST)
    @ResponseBody
    public String  getCanBindCloudVmList(HttpServletRequest request,@RequestBody Map map){
    	log.info("查询项目下可绑定云硬盘的云主机列表开始");
    	String prjId = null != map.get("prjId")?map.get("prjId").toString():"";
    	EayunResponseJson json = new EayunResponseJson();
    	try {
			List<CloudVm> list = ecmcCloudVmService.getCanBindCloudVmList(prjId);
			json.setData(list);
			json.setRespCode(ConstantClazz.SUCCESS_CODE);
		} catch (Exception e) {
    		json.setRespCode(ConstantClazz.ERROR_CODE);
    		log.error(e.toString(),e);
    		throw e;
		}
    	return JSONObject.toJSONString(json);
    	
    }
	/**
	 * 根据数据中心ID查询下属项目列表及配额信息
	 * 用于主机创建查询项目时
	 * @Author: duanbinbin
	 * @param request
	 * @param map
	 * @return
	 * @throws Exception
	 *<li>Date: 2016年5月3日</li>
	 */
	@RequestMapping(value = "/getprolistbydcid" , method = RequestMethod.POST)
    @ResponseBody
    public String getproListByDcId(HttpServletRequest request , @RequestBody Map map)throws Exception {
    	log.info("根据数据中心ID查询下属项目列表及配额信息开始");
    	EayunResponseJson json = new EayunResponseJson();
    	String dcId = map.get("dcId").toString();
        List<CloudProject> prjList = new ArrayList<CloudProject>();
        try {
        	prjList = ecmcCloudVmService.getproListByDcId(dcId);
        	json.setData(prjList);
        	json.setRespCode(ConstantClazz.SUCCESS_CODE);
		} catch (Exception e) {
			json.setRespCode(ConstantClazz.ERROR_CODE);
			log.error(e.toString(),e);
		}
    	return JSONObject.toJSONString(json);
    }
	
	/**
	 * 查询网络下的子网列表
	 * 
	 * @author zhouhaitao
	 * @param request
	 * @param subnet
	 * @return
	 */
	@RequestMapping(value = "querySubnetByNet" ,method = RequestMethod.POST)
	@ResponseBody
	public String querySubnetByNet(HttpServletRequest request, @RequestBody CloudSubNetWork subnet){
		log.info("根据网络ID查询子网信息开始");
    	EayunResponseJson json = new EayunResponseJson();
        List<CloudSubNetWork> subnetList = new ArrayList<CloudSubNetWork>();
        try {
        	subnetList = ecmcCloudVmService.querySubnetByNet(subnet);
        	json.setData(subnetList);
        	json.setRespCode(ConstantClazz.SUCCESS_CODE);
		} catch (Exception e) {
			json.setRespCode(ConstantClazz.ERROR_CODE);
			log.error(e.toString(),e);
		}
    	return JSONObject.toJSONString(json);
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
    		ecmcCloudVmService.modifySubnet(cloudVm);
    		json.put("respCode", ConstantClazz.SUCCESS_CODE);
    		ecmcLogService.addLog("修改子网",  ConstantClazz.LOG_TYPE_HOST, cloudVm.getVmName(), cloudVm.getPrjId(),1,cloudVm.getVmId(),null);
    	}catch(Exception e){
    		json.put("respCode", ConstantClazz.ERROR_CODE);
    		json.put("message", e.getMessage());
    		log.error(e.getMessage());
    		ecmcLogService.addLog("修改子网",  ConstantClazz.LOG_TYPE_HOST, cloudVm.getVmName(), cloudVm.getPrjId(),0,cloudVm.getVmId(),e);
    		log.error(e.toString(),e);
    		throw e;
    	}
    	return json.toJSONString();	
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
    		boolean isVmIpUsed = ecmcCloudVmService.checkVmIpUsed(vm);
    		json.put("data", isVmIpUsed);
    		json.put("respCode", ConstantClazz.SUCCESS_CODE);
    	}catch(Exception e){
    		json.put("respCode", ConstantClazz.ERROR_CODE);
    		log.error(e.toString(),e);
    		throw e;
    	}
    	return json.toJSONString();
    }
	
    /**
	 * 查询云主机列表
	 * @param dcId		数据中心id
	 * @param vmStatus	云主机状态
	 * @param queryType	输入框查询类型
	 * @param queryName	输入框查询内容
	 * @return
	 *<li>Date: 2016年4月22日</li>
	 */
	@RequestMapping(value = "/getrecyclevmpagelist" , method = RequestMethod.POST)
    @ResponseBody
    public String  getRecycleVmPageList(HttpServletRequest request,Page page,@RequestBody ParamsMap map){
    	log.info("查询云主机列表开始");
    	String dcId = "";
    	String vmStatus = "";
    	String sysType = "";
    	String timesort = "";
    	String queryType = "";
    	String queryName = "";
    	
    	if(null != map.getParams()){
    		dcId = null == map.getParams().get("dcId")?"":map.getParams().get("dcId").toString();
        	vmStatus = null == map.getParams().get("vmStatus")?"":map.getParams().get("vmStatus").toString();//“状态”下拉框中的值为：“运行中”、“已关机”、“故障”
        	sysType = null == map.getParams().get("sysType")?"":map.getParams().get("sysType").toString();
        	timesort = null == map.getParams().get("timesort")?"":map.getParams().get("timesort").toString();//按照到期时间的排序：ASC（最短），DESC（最长），""默认
        	queryType = null == map.getParams().get("queryType")?"":map.getParams().get("queryType").toString();
        	queryName = null == map.getParams().get("queryName")?"":map.getParams().get("queryName").toString();
    	}
    	int pageSize = map.getPageSize();
        int pageNumber = map.getPageNumber();
        
        QueryMap queryMap=new QueryMap();
        queryMap.setPageNum(pageNumber);
        queryMap.setCURRENT_ROWS_SIZE(pageSize);
        
        try {
			page = ecmcCloudVmService.getRecycleVmPage(page,queryMap,dcId,vmStatus,queryType,queryName);
		} catch (Exception e) {
			log.error(e.toString(),e);
			throw e;
		}
    	return JSONObject.toJSONString(page);
    	
    }
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
    	CloudVm vm = null;
    	try{
    		vm = ecmcCloudVmService.getRecycleVmById(vmId);
    		if(null!=vm){
    			json.put("data", vm);
    		}
    		json.put("respCode", ConstantClazz.SUCCESS_CODE);
    	}catch(Exception e){
        	json.put("respCode", ConstantClazz.ERROR_CODE);
        	log.error(e.getMessage());
        	log.error(e.toString(),e);
        	throw e;
        }
        return json.toJSONString();	
    }
    
    /**
     * 云主机状态同步
     * @param request
     * @param cloudVm
     * @return
     * @throws Exception
     */
    @RequestMapping(value = "/refreshstatus" , method = RequestMethod.POST)
    @ResponseBody
    public String refreshStatus(HttpServletRequest request,@RequestBody CloudVm cloudVm) throws Exception{
    	JSONObject json  = new JSONObject();
    	log.info("云主机状态同步");
    	try{
    		ecmcCloudVmService.refreshStatus(cloudVm.getVmId());
    		json.put("respCode", ConstantClazz.SUCCESS_CODE);
    		ecmcLogService.addLog("状态恢复",  ConstantClazz.LOG_TYPE_HOST, cloudVm.getVmName(), cloudVm.getPrjId(),1,cloudVm.getVmId(),null);
    	}catch(Exception e){
    		json.put("respCode", ConstantClazz.ERROR_CODE);
    		ecmcLogService.addLog("状态恢复",  ConstantClazz.LOG_TYPE_HOST, cloudVm.getVmName(), cloudVm.getPrjId(),0,cloudVm.getVmId(),e);
    		log.error(e.getMessage());
    		throw e;
    	}
    	return json.toJSONString();	
    }
    
    /**
     * 查询云主机的状态
     * @param request
     * @param vmId
     * @return
     * @throws Exception
     */
    @RequestMapping(value = "/get" , method = RequestMethod.POST)
    @ResponseBody
    public String get (HttpServletRequest request , @RequestBody String vmId) throws Exception {
    	JSONObject json  = new JSONObject();
    	log.info("查询云主机");
    	try{
    		CloudVm cloudVm = ecmcCloudVmService.get(vmId);
    		json.put("respCode", ConstantClazz.SUCCESS_CODE);
    		json.put("data", cloudVm);
    	}catch(Exception e){
    		json.put("respCode", ConstantClazz.ERROR_CODE);
    		log.error(e.getMessage());
    		throw e;
    	}
    	return json.toJSONString();	
    }
}
