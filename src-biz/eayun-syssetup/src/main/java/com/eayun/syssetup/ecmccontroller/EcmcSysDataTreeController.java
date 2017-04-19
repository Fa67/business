package com.eayun.syssetup.ecmccontroller;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.MapUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.eayun.common.ConstantClazz;
import com.eayun.common.controller.BaseController;
import com.eayun.common.dao.ParamsMap;
import com.eayun.common.dao.QueryMap;
import com.eayun.common.exception.AppException;
import com.eayun.common.model.EayunResponseJson;
import com.eayun.common.util.BeanUtils;
import com.eayun.log.ecmcsevice.EcmcLogService;
import com.eayun.syssetup.ecmcservice.EcmcSysDataTreeService;
import com.eayun.syssetup.model.EcmcSysDataTree;


@Controller
@RequestMapping("/ecmc/system/enum")
@Scope("prototype")
public class EcmcSysDataTreeController extends BaseController {

	private static final Logger log = LoggerFactory.getLogger(EcmcSysDataTreeController.class);

	@Autowired
	private EcmcSysDataTreeService ecmcSysDataTreeService;
	@Autowired
	private EcmcLogService ecmclogservice;

	@RequestMapping(value = "/createdatatree")
	@ResponseBody
	public Object createDataTree(@RequestBody Map<String, Object> requestMap) throws AppException{
		log.info("创建数据字典节点");
		EayunResponseJson reJson = new EayunResponseJson();
		EcmcSysDataTree ecmcDataTree = new EcmcSysDataTree();
		BeanUtils.mapToBean(ecmcDataTree, requestMap);
		try {
			reJson.setData(ecmcSysDataTreeService.createDataTree(ecmcDataTree));
			reJson.setRespCode(ConstantClazz.SUCCESS_CODE);
			ecmclogservice.addLog("创建数据字典", "数据字典", "创建数据字典", null, 1, ecmcDataTree.getNodeId(), null);
		} catch (Exception e) {
			reJson.setRespCode(ConstantClazz.ERROR_CODE);
			ecmclogservice.addLog("创建数据字典", "数据字典", "创建数据字典", null, 0, ecmcDataTree.getNodeId(), e);
			throw new AppException("创建数据字典失败");
		}
		return reJson;
	}

	@RequestMapping(value = "/deletedatatree")
	@SuppressWarnings("unchecked")
	@ResponseBody
	public Object deleteDataTree(@RequestBody Map<String, Object> requestMap) {
		log.info("删除数据字典节点");
		EayunResponseJson reJson = new EayunResponseJson();
		String[] ids = MapUtils.getString(requestMap, "ids").split(",");
		List<String> nodeIds = CollectionUtils.arrayToList(ids);
		try {
			if (nodeIds != null && nodeIds.size() > 0) {
				ecmcSysDataTreeService.delDataTrees(nodeIds);
				reJson.setRespCode(ConstantClazz.SUCCESS_CODE);
				ecmclogservice.addLog("删除数据字典", "数据字典", "删除数据字典", null, 1, null, null);
			}
		} catch (Exception e) {
			reJson.setRespCode(ConstantClazz.ERROR_CODE);
			ecmclogservice.addLog("删除数据字典", "数据字典", "删除数据字典", null, 0, null, e);
			throw new AppException("删除数据字典失败");
		}
		return reJson;
	}

	@RequestMapping(value = "/modifydatatree")
	@ResponseBody
	public Object modifyDataTree(@RequestBody Map<String, Object> requestMap) {
		log.info("修改数据字典节点");
		EayunResponseJson reJson = new EayunResponseJson();
		EcmcSysDataTree ecmcDataTree = new EcmcSysDataTree();
		BeanUtils.mapToBean(ecmcDataTree, requestMap);
		reJson.setRespCode(ecmcSysDataTreeService.updateDataTree(ecmcDataTree) ? ConstantClazz.SUCCESS_CODE
				: ConstantClazz.ERROR_CODE);
		ecmclogservice.addLog("修改数据字典", "数据字典", "修改数据字典", null, 1, null, null);
		return reJson;
	}

	@RequestMapping(value = "/syncdatatree")
	@ResponseBody
	public Object syncDataTree() throws AppException{
		log.info("同步数据字典");
		EayunResponseJson reJson = new EayunResponseJson();
		if(ecmcSysDataTreeService.syncDataTree()){
			reJson.setRespCode(ConstantClazz.SUCCESS_CODE);
			ecmclogservice.addLog("同步数据字典", "数据字典", "同步数据字典", null, 1, null, null);
		}else{
			reJson.setRespCode(ConstantClazz.ERROR_CODE);
			ecmclogservice.addLog("同步数据字典", "数据字典", "同步数据字典", null, 0, null, null);
		}
		return reJson;
	}

	@RequestMapping(value = "/getdatatreelist")
	@ResponseBody
	public Object getDataTreeList(@RequestBody ParamsMap paramsMap) {
		log.info("获取数据字典集合");
		QueryMap queryMap = new QueryMap();
		Map<String, Object> params = paramsMap.getParams();
		queryMap.setPageNum(paramsMap.getPageNumber() == null ? 1 : paramsMap.getPageNumber());
		if (paramsMap.getPageSize() != null) {
			queryMap.setCURRENT_ROWS_SIZE(paramsMap.getPageSize());
		}
		String parentId = MapUtils.getString(params, "parentId");
		String nodeName = MapUtils.getString(params, "nodeName");
		String nodeId = MapUtils.getString(params, "nodeId");
		try {
			return ecmcSysDataTreeService.getDataTreeList(nodeId, nodeName, parentId, queryMap);
		} catch (Exception e) {
		    log.error(e.getMessage(),e);
		}
		return null;
	}

	@RequestMapping(value = "/sortdatatree")
	@ResponseBody
	public Object sortDataTree(@RequestBody Map<String, Object> requestMap) {
		log.info("数据字典节点排序");
		EayunResponseJson reJson = new EayunResponseJson();
		ArrayList<String> nodeIds = (ArrayList<String>)MapUtils.getObject(requestMap, "ids");
		ArrayList<Integer> nodeSorts = (ArrayList<Integer>)MapUtils.getObject(requestMap, "sorts");
		reJson.setRespCode(ecmcSysDataTreeService.sortDataTree(nodeIds, nodeSorts) ? ConstantClazz.SUCCESS_CODE
				: ConstantClazz.ERROR_CODE);
		return reJson;
	}

	@RequestMapping(value = "/getdatatreebyid")
	@ResponseBody
	public Object getDataTreeById(@RequestBody Map<String, Object> requestMap) {
		log.info("根据ID查询数据字典");
		String nodeId = MapUtils.getString(requestMap, "nodeId");
		EayunResponseJson reJson = new EayunResponseJson();
		try {
			reJson.setData(ecmcSysDataTreeService.getDataTreeById(nodeId));
			reJson.setRespCode(ConstantClazz.SUCCESS_CODE);
		} catch (Exception e) {
		    log.error(e.getMessage(),e);
			reJson.setRespCode(ConstantClazz.ERROR_CODE);
		}
		return reJson;
	}
	
	@RequestMapping(value = "/getdatatreenav")
	@ResponseBody
	public Object getDataTreeNav(){
		log.info("查询数据字典导航树");
		EayunResponseJson reJson = new EayunResponseJson();
		try {
			reJson.setData(ecmcSysDataTreeService.getDataTreeNav());
			reJson.setRespCode(ConstantClazz.SUCCESS_CODE);
		} catch (Exception e) {
		    log.error(e.getMessage(),e);
			reJson.setRespCode(ConstantClazz.ERROR_CODE);
		}
		return reJson;
	}
	
	@RequestMapping(value = "/getdatatreechildren")
	@ResponseBody
	public Object getDataTreeChildren(@RequestBody Map<String, Object> requestMap) {
		log.info("根据ID查询子节点，用于排序");
		EayunResponseJson reJson = new EayunResponseJson();
		String parentId = MapUtils.getString(requestMap, "parentId");
		try {
			reJson.setData(ecmcSysDataTreeService.getDataTreeChildren(parentId));
			reJson.setRespCode(ConstantClazz.SUCCESS_CODE);
		} catch (Exception e) {
			log.info("获取节点的子节点发生异常：", e.getMessage());
			reJson.setRespCode(ConstantClazz.ERROR_CODE);
		}
		return reJson;
	}

}
