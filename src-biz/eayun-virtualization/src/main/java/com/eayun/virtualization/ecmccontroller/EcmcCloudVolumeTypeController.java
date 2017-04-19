package com.eayun.virtualization.ecmccontroller;

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
import com.eayun.common.model.EayunResponseJson;
import com.eayun.log.ecmcsevice.EcmcLogService;
import com.eayun.virtualization.ecmcservice.EcmcCloudVolumeTypeService;
import com.eayun.virtualization.model.CloudImage;
import com.eayun.virtualization.model.CloudVolumeType;



/**
 * @author
 *
 */
@Controller
@RequestMapping("/ecmc/cloud/volumetype")
@Scope("prototype")
public class EcmcCloudVolumeTypeController {
	
private static final Logger log = LoggerFactory.getLogger(EcmcCloudVolumeTypeController.class);
	
	@Autowired
	private EcmcCloudVolumeTypeService ecmcCloudVolumeTypeService;
	@Autowired
	private EcmcLogService ecmcLogService;
	
	
	/**
	 * 查询云硬盘类型列表
	 * 
	 * @author Chengxiaodong
	 * @param request
	 * @param datacenterId
	 * @param page
	 * @return
	 * @throws Exception 
	 */
	@RequestMapping(value = "/getvolumetypelist", method = RequestMethod.POST)
	@ResponseBody
	public String getVolumeList(HttpServletRequest request, Page page,
			@RequestBody ParamsMap map) throws Exception{
		try {
			log.info("查询云硬盘列表开始");
			String dcId = map.getParams().get("dcId")!=null?map.getParams().get("dcId").toString():null;
			int pageSize = map.getPageSize();
			int pageNumber = map.getPageNumber();

			QueryMap queryMap = new QueryMap();
			queryMap.setPageNum(pageNumber);
			queryMap.setCURRENT_ROWS_SIZE(pageSize);
			page = ecmcCloudVolumeTypeService.getVolumeTypeList(page,dcId,queryMap);
		} catch (Exception e) {
			log.error(e.toString(),e);
			throw e;
		}
		return JSONObject.toJSONString(page);

	}
	
	
	@RequestMapping(value = "/updatetype", method = RequestMethod.POST)
	@ResponseBody
	public String updateVolumeType(HttpServletRequest request, @RequestBody CloudVolumeType cloudVolumeType) throws Exception{
		log.info("编辑云硬盘类型开始");
    	EayunResponseJson json = new EayunResponseJson();
    	try {
    		ecmcCloudVolumeTypeService.updateVolumeType(cloudVolumeType);
			json.setRespCode(ConstantClazz.SUCCESS_CODE);
			ecmcLogService.addLog("编辑", ConstantClazz.LOG_TYPE_VOLUME_TYPE, cloudVolumeType.getVolumeTypeAs(), null, 1, cloudVolumeType.getId(), null);
		}catch (AppException e) {
			json.setRespCode(ConstantClazz.ERROR_CODE);
			log.error(e.toString(),e);
			ecmcLogService.addLog("编辑", ConstantClazz.LOG_TYPE_VOLUME_TYPE, cloudVolumeType.getVolumeTypeAs(), null, 0, cloudVolumeType.getId(), e);
	        throw e;
	    } catch (Exception e) {
	    	json.setRespCode(ConstantClazz.ERROR_CODE);
			log.error(e.toString(),e);
			ecmcLogService.addLog("编辑", ConstantClazz.LOG_TYPE_VOLUME_TYPE, cloudVolumeType.getVolumeTypeAs(), null, 0, cloudVolumeType.getId(), e);
	    	throw e;
		}
    	return JSONObject.toJSONString(json);
	}
	
	
	/**
	 * 启用或停用云硬盘类型
	 * @param request
	 * @param cloudVolumeType
	 * @return
	 * @throws Exception
	 */
	@RequestMapping(value = "/changeuse", method = RequestMethod.POST)
	@ResponseBody
	public String changeUse(HttpServletRequest request, @RequestBody CloudVolumeType cloudVolumeType) throws Exception{
		log.info("启用/停用云硬盘类型");
    	EayunResponseJson json = new EayunResponseJson();
    	String volTypeName=cloudVolumeType.getVolumeTypeAs();
    	String isUse=cloudVolumeType.getIsUse();
    	String operat="启用";
    	if(null!=isUse&&!"".equals(isUse)){
    		if("2".equals(isUse)){
    			operat="停用";
    		}
    	}
    	try {
    		ecmcCloudVolumeTypeService.changeUse(cloudVolumeType);
			json.setRespCode(ConstantClazz.SUCCESS_CODE);
			ecmcLogService.addLog(operat, ConstantClazz.LOG_TYPE_VOLUME_TYPE, volTypeName, null, 1, cloudVolumeType.getId(), null);
		} catch (Exception e) {
	    	json.setRespCode(ConstantClazz.ERROR_CODE);
			log.error(e.toString(),e);
			ecmcLogService.addLog(operat, ConstantClazz.LOG_TYPE_VOLUME_TYPE, volTypeName, null, 0, cloudVolumeType.getId(), e);
	    	throw e;
		}
    	return JSONObject.toJSONString(json);
	}
	

}
