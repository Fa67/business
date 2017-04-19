package com.eayun.virtualization.ecmccontroller;

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
import com.eayun.common.controller.BaseController;
import com.eayun.common.dao.ParamsMap;
import com.eayun.common.dao.QueryMap;
import com.eayun.common.dao.support.Page;
import com.eayun.common.exception.AppException;
import com.eayun.common.filter.SessionUserInfo;
import com.eayun.common.model.EayunResponseJson;
import com.eayun.ecmcuser.model.BaseEcmcSysUser;
import com.eayun.ecmcuser.util.EcmcSessionUtil;
import com.eayun.log.ecmcsevice.EcmcLogService;
import com.eayun.log.service.LogService;
import com.eayun.virtualization.ecmcservice.EcmcCloudSnapshotService;
import com.eayun.virtualization.ecmcservice.EcmcCloudVolumeService;
import com.eayun.virtualization.model.BaseCloudSnapshot;
import com.eayun.virtualization.model.BaseCloudVolume;
import com.eayun.virtualization.model.CloudSnapshot;
import com.eayun.virtualization.model.CloudVolume;
import com.eayun.virtualization.service.SnapshotService;
import com.eayun.virtualization.service.VolumeService;

@Controller
@RequestMapping("/ecmc/cloud/snapshot")
@Scope("prototype")
public class EcmcCloudSnapshotController {
	private static final Logger log = LoggerFactory.getLogger(EcmcCloudSnapshotController.class);
	@Autowired
	private EcmcCloudSnapshotService ecmcSnapService;
	@Autowired
	private EcmcLogService ecmcLogService;

	/**
	 * 查询硬盘备份列表
	 * 
	 * @param request
	 * @param datacenterId
	 * @param projectId
	 * @param name
	 * @param page
	 * @return
	 */

	@RequestMapping(value = "/getsnapshotlist", method = RequestMethod.POST)
	@ResponseBody
	public String getSnapshotList(HttpServletRequest request, Page page,
			@RequestBody ParamsMap map) {
		try {
			log.info("查询云硬盘备份列表开始");
			String dcId = map.getParams().get("dcId") != null ? map.getParams().get("dcId").toString() : null;
			String prjId = map.getParams().get("prjId") != null ? map.getParams().get("prjId").toString() : null;
			String queryName = map.getParams().get("queryName")!=null?map.getParams().get("queryName").toString():null;
			String queryType = map.getParams().get("queryType")!=null?map.getParams().get("queryType").toString():null;
			String isDeleted = map.getParams().get("isDeleted") != null ? map.getParams().get("isDeleted").toString() : null;
			int pageSize = map.getPageSize();
			int pageNumber = map.getPageNumber();

			QueryMap queryMap = new QueryMap();
			queryMap.setPageNum(pageNumber);
			queryMap.setCURRENT_ROWS_SIZE(pageSize);
			page = ecmcSnapService.getSnapshotList(page, prjId, dcId, queryName,queryType,queryMap,isDeleted);
		} catch (Exception e) {
			log.error(e.toString(),e);
		}
		return JSONObject.toJSONString(page);

	}

	/**
	 * 验证名称唯一
	 * 
	 * @param request
	 * @param map
	 * @return
	 * @author chengxiaodong
	 */
	@RequestMapping(value = "/getsnapbyname", method = RequestMethod.POST)
	@ResponseBody
	public String getSnapByName(HttpServletRequest request,
			@RequestBody CloudSnapshot snap) {
		boolean isTrue = false;
		EayunResponseJson json = new EayunResponseJson();
		try {
			log.info("云硬盘备份验证重名开始");
			isTrue = ecmcSnapService.getSnapByName(snap);
			json.setData(isTrue);
			json.setRespCode(ConstantClazz.SUCCESS_CODE);
		} catch (AppException e) {
			json.setRespCode(ConstantClazz.ERROR_CODE);
			throw e;
		}
		return JSONObject.toJSONString(json);
	}

	/**
	 * 创建云硬盘快照
	 * 
	 * @param request
	 * @param map
	 * @return
	 * @author chengxiaodong
	 */
	@RequestMapping(value = "/addsnapshot", method = RequestMethod.POST)
	@ResponseBody
	public String addSnapshot(HttpServletRequest request,
			@RequestBody CloudSnapshot snap) {
		BaseCloudSnapshot snapshot = null;
		EayunResponseJson json = new EayunResponseJson();
		try {
			log.info("创建云硬盘快照开始");
			snapshot = ecmcSnapService.addSnapshot(snap);
			json.setData(snapshot);
			json.setRespCode(ConstantClazz.SUCCESS_CODE);
			ecmcLogService.addLog("创建云硬盘快照",ConstantClazz.LOG_TYPE_DISKSNAPSHOT, snapshot.getSnapName(),snapshot.getPrjId(), 1, snapshot.getSnapId(), null);
		} catch (AppException e) {
			json.setRespCode(ConstantClazz.ERROR_CODE);
			ecmcLogService.addLog("创建云硬盘快照",ConstantClazz.LOG_TYPE_DISKSNAPSHOT, snap.getSnapName(),snap.getPrjId(), 0, snap.getSnapId(), e);
			throw e;
		}
		return JSONObject.toJSONString(json);
	}

	/**
	 * 删除云硬盘备份
	 * 
	 * @param request
	 * @param map
	 * @return
	 */
	@RequestMapping(value = "/deletesnap", method = RequestMethod.POST)
	@ResponseBody
	public String deleteSnapshot(HttpServletRequest request,
			@RequestBody CloudSnapshot snap) {
		boolean isTrue = false;
		String doName="";
		EayunResponseJson json = new EayunResponseJson();
		try {
			log.info("删除云硬盘备份开始");
			if("2".equals(snap.getIsDeleted())){
    			doName="删除云硬盘备份";
            }else{
            	doName="销毁云硬盘备份";
            }
			BaseEcmcSysUser user = EcmcSessionUtil.getUser();
			isTrue = ecmcSnapService.deleteSnapshot(snap,user);
			json.setData(isTrue);
			json.setRespCode(ConstantClazz.SUCCESS_CODE);
			ecmcLogService.addLog(doName,ConstantClazz.LOG_TYPE_DISKSNAPSHOT, snap.getSnapName(),snap.getPrjId(), 1, snap.getSnapId(), null);
		} catch (AppException e) {
			json.setRespCode(ConstantClazz.ERROR_CODE);
			ecmcLogService.addLog(doName,ConstantClazz.LOG_TYPE_DISKSNAPSHOT, snap.getSnapName(),snap.getPrjId(), 0, snap.getSnapId(), e);
			throw e;
		}
		return JSONObject.toJSONString(json);
	}

	/**
	 * 编辑云硬盘备份
	 * 
	 * @param request
	 * @param map
	 * @return
	 */
	@RequestMapping(value = "/updatesnapshot", method = RequestMethod.POST)
	@ResponseBody
	public String updateSnapshot(HttpServletRequest request,
			@RequestBody CloudSnapshot snap) throws AppException {
		boolean isTrue = false;
		EayunResponseJson json = new EayunResponseJson();
		try {
			log.info("编辑云硬盘备份开始");
			isTrue = ecmcSnapService.updateSnapshot(snap);
			json.setData(isTrue);
			json.setRespCode(ConstantClazz.SUCCESS_CODE);
			ecmcLogService.addLog("编辑云硬盘备份",ConstantClazz.LOG_TYPE_DISKSNAPSHOT, snap.getSnapName(),snap.getPrjId(), 1, snap.getSnapId(), null);
		} catch (AppException e) {
			json.setRespCode(ConstantClazz.ERROR_CODE);
			ecmcLogService.addLog("编辑云硬盘备份",ConstantClazz.LOG_TYPE_DISKSNAPSHOT, snap.getSnapName(),snap.getPrjId(), 0, snap.getSnapId(), e);
			throw e;
		}
		return JSONObject.toJSONString(json);

	}

	/**
	 * 根据云硬盘快照创建云硬盘
	 * 
	 * @param request
	 * @param map
	 * @return
	 */
	@RequestMapping(value = "/addvolume", method = RequestMethod.POST)
	@ResponseBody
	public String addVolumeBySnapshot(HttpServletRequest request,
			@RequestBody CloudVolume vol) throws AppException {
		BaseEcmcSysUser user = EcmcSessionUtil.getUser();
		EayunResponseJson json = new EayunResponseJson();
		BaseCloudVolume volume=null;
		try {
			log.info("创建云硬盘开始");
			String createUser = user.getAccount();
			volume= ecmcSnapService.addVolumeBySnapshot(vol, createUser);
			json.setData(volume);
			json.setRespCode(ConstantClazz.SUCCESS_CODE);
			ecmcLogService.addLog("创建云硬盘", ConstantClazz.LOG_TYPE_DISK,volume.getVolName(), volume.getPrjId(), 1, volume.getVolId(), null);
		} catch (AppException e) {
			json.setRespCode(ConstantClazz.ERROR_CODE);
			ecmcLogService.addLog("创建云硬盘", ConstantClazz.LOG_TYPE_DISK,vol.getVolName(), vol.getPrjId(), 0, vol.getVolId(), e);
			throw e;
		}
		return JSONObject.toJSONString(json);

	}

	/**
	 * 查询硬盘备份列表(云硬盘详情页)
	 * 
	 * @param request
	 * @param datacenterId
	 * @param projectId
	 * @param name
	 * @param page
	 * @return
	 */

	@RequestMapping(value = "/getsnaplistbyvolid", method = RequestMethod.POST)
	@ResponseBody
	public String getSnapListByVolId(HttpServletRequest request,
			@RequestBody Map map) {
		List<CloudSnapshot> snapList = null;
		try {
			String volId = map.get("volId") != null ? map.get("volId").toString() : null;
			snapList = ecmcSnapService.getSnapListByVolId(volId);
		} catch (Exception e) {
			log.error(e.toString(),e);
		}
		return JSONObject.toJSONString(snapList);

	}
	/**
     * <p>回滚云硬盘<p>
     * 
     * 
     * @param request
     * @param snapshot
     * @return
     * @throws Exception 
     */
    @RequestMapping(value= "/rollbackvolume" , method = RequestMethod.POST)
    @ResponseBody
    public String rollBackVolume(HttpServletRequest request, @RequestBody CloudSnapshot snapshot) throws Exception{
    	log.info("回滚云硬盘");
    	JSONObject json  = new JSONObject();
    	try{
    		ecmcSnapService.rollBackVolume(snapshot);
			ecmcLogService.addLog("回滚云硬盘", 
					ConstantClazz.LOG_TYPE_DISKSNAPSHOT,
					snapshot.getSnapName(), 
					snapshot.getPrjId(), 
					1, 
					snapshot.getSnapId(), 
					null);
    		json.put("respCode", ConstantClazz.SUCCESS_CODE_ADD);
    	}catch(Exception e){
    		ecmcLogService.addLog("回滚云硬盘", 
    				ConstantClazz.LOG_TYPE_DISKSNAPSHOT, 
    				snapshot.getSnapName(), 
    				snapshot.getPrjId(), 
    				0, 
    				snapshot.getSnapId(), 
    				e);
    		json.put("respCode", ConstantClazz.ERROR_CODE);
    		log.error(e.toString(),e);
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
    		BaseEcmcSysUser user = EcmcSessionUtil.getUser();
    		int pageSize = map.getPageSize();
    		int pageNumber = map.getPageNumber();
    		
    		QueryMap queryMap=new QueryMap();
    		queryMap.setPageNum(pageNumber);
    		queryMap.setCURRENT_ROWS_SIZE(pageSize);
    		
    		page = ecmcSnapService.getRecycleSnapList(page,map,user,queryMap);
    	} catch (Exception e) {
    		throw e;
    	}
    	return JSONObject.toJSONString(page);
    	
    }
}
