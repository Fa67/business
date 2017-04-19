package com.eayun.work.controller;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import com.eayun.virtualization.model.CloudProject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import com.alibaba.fastjson.JSONObject;
import com.eayun.common.ConstantClazz;
import com.eayun.common.controller.BaseController;
import com.eayun.common.dao.ParamsMap;
import com.eayun.common.dao.support.Page;
import com.eayun.common.exception.AppException;
import com.eayun.common.filter.SessionUserInfo;
import com.eayun.common.util.BeanUtils;
import com.eayun.log.service.LogService;
import com.eayun.sys.model.SysDataTree;
import com.eayun.work.model.WorkFile;
import com.eayun.work.model.WorkOpinion;
import com.eayun.work.model.WorkQuota;
import com.eayun.work.model.Workorder;
import com.eayun.work.service.WorkorderService;

@Controller
@RequestMapping("/sys/work")
@Scope("prototype")
public class WorkorderController extends BaseController{
	
	private static final Logger log = LoggerFactory.getLogger(WorkorderController.class);
	
	@Autowired
	private WorkorderService workService;
	
	@Autowired
	private LogService logService;
	
	/**
     * 查询所有工单
     * @param request
     * @return
     * @throws AppException
     * @throws IOException 
     */
    @RequestMapping(value="/getWorkorderList" ,method = RequestMethod.POST)
    @ResponseBody
    public String getWorkorderList(HttpServletRequest request,Page page,@RequestBody ParamsMap paramsMap) {
	    SessionUserInfo sessionUser = (SessionUserInfo) request.getSession().getAttribute(ConstantClazz.SYS_SESSION_USERINFO);
        String userId = sessionUser.getUserId();
        paramsMap.getParams().put("userId", userId);
        page=workService.getWorkorderList(page,paramsMap);
    	return JSONObject.toJSONString(page);
    }
    /**
     * 获取类型()
     * @param request
     * @return
     * @throws AppException
     * @throws IOException 
     */
    @RequestMapping(value="/getDataTree",method = RequestMethod.POST)
    @ResponseBody
    public String getDataTree(HttpServletRequest request,@RequestBody String parentId) {
    	List<SysDataTree> dataTreeList=workService.getDataTree(parentId);
    	return JSONObject.toJSONString(dataTreeList);
    }
    /**
     * 统计待操作工单数
     * @param request
     * @return
     * @throws AppException
     * @throws IOException 
     */
    @RequestMapping(value="/unHandleWorkCount",method = RequestMethod.POST)
    @ResponseBody
    public String unHandleWorkCount(HttpServletRequest request,@RequestBody Map<String,String> map) {
        int count=workService.unHandleWorkCount(map);
        return String.valueOf(count);
    }
    /**
     * 添加配额类工单
     * @param request
     * @param map
     * @return
     * @throws Exception
     */
    @RequestMapping(value="/addQuotaWork",method = RequestMethod.POST)
    @ResponseBody
    public String addQuotaWork(HttpServletRequest request,@RequestBody Map<String,Object> map) throws Exception{
        SessionUserInfo sessionUser = (SessionUserInfo) request.getSession().getAttribute(ConstantClazz.SYS_SESSION_USERINFO);
        String userId = sessionUser.getUserId();
        String cusId = sessionUser.getCusId();
        Workorder workorder = new Workorder();
        BeanUtils.copyPropertiesByModel(workorder, map.get("workorder"));
        workorder.setWorkCreUser(userId);
        workorder.setWorkApplyUser(userId);
        workorder.setApplyCustomer(cusId);
        workorder.setWorkApplyUserName(sessionUser.getUserName());
        WorkQuota workQuota = new WorkQuota();
        BeanUtils.copyPropertiesByModel(workQuota, map.get("workQuota"));
        try {
			workorder=workService.addQuotaWorkorder(workorder, workQuota);
			logService.addLog("申请配额", "配额信息", null, workQuota.getPrjId(),ConstantClazz.LOG_STATU_SUCCESS, null);
			logService.addLog("创建工单", "工单", workorder.getWorkTitle(), null,ConstantClazz.LOG_STATU_SUCCESS, null);
        } catch (Exception e) {
			logService.addLog("申请配额", "配额信息", null, workQuota.getPrjId(),ConstantClazz.LOG_STATU_ERROR, e);
			logService.addLog("创建工单", "工单", workorder.getWorkTitle(), null,ConstantClazz.LOG_STATU_ERROR, e);
			log.error(e.toString(), e);
		}
        return JSONObject.toJSONString(workorder);
    }
    /**
     * 添加普通工单
     * @param request
     * @return
     * @throws Exception 
     * @throws AppException
     * @throws IOException 
     */
    @RequestMapping(value="/addWorkorder",method = RequestMethod.POST)
    @ResponseBody
    public String addWorkorder(HttpServletRequest request,@RequestBody Map<String,Object> map) throws Exception{
        SessionUserInfo sessionUser = (SessionUserInfo) request.getSession().getAttribute(ConstantClazz.SYS_SESSION_USERINFO);
        String userId = sessionUser.getUserId();
        String cusId = sessionUser.getCusId();
        String userName = sessionUser.getUserName();
        Workorder workorder = new Workorder();
        BeanUtils.copyPropertiesByModel(workorder, map.get("workorder"));
        workorder.setWorkCreUser(userId);
        workorder.setWorkApplyUser(userId);
        workorder.setApplyCustomer(cusId);
        workorder.setWorkApplyUserName(userName);
        try {
			workService.addWorkorder(workorder);
			logService.addLog("创建工单", "工单", workorder.getWorkTitle(), null,ConstantClazz.LOG_STATU_SUCCESS, null);
        } catch (Exception e) {
			logService.addLog("创建工单", "工单", workorder.getWorkTitle(), null,ConstantClazz.LOG_STATU_ERROR, e);
			log.error(e.toString(), e);
		}
        return JSONObject.toJSONString(workorder);
    }
    /**
     * 修改工单状态
     * @param request
     * @return
     * @throws Exception 
     */
    @RequestMapping(value="/updateWorkorderForFalg",method = RequestMethod.POST)
    @ResponseBody
    public String updateWorkorderForFalg(HttpServletRequest request,@RequestBody Workorder workorder) throws Exception{
    	try{
    		workorder = workService.updateWorkorderForFalg(workorder);
    		//取消工单操作
    		if(workorder.getWorkEcscFalg().equals("7")){
    			logService.addLog("取消工单", 
            			ConstantClazz.LOG_TYPE_WORKORDER, 
            			workorder.getWorkTitle(), 
            			null, 
            			ConstantClazz.LOG_STATU_SUCCESS, 
            			null);
    		}
    	} catch(Exception e) {
    		//取消工单操作
    		if(workorder.getWorkEcscFalg().equals("7")){
    			logService.addLog("取消工单", 
            			ConstantClazz.LOG_TYPE_WORKORDER, 
            			workorder.getWorkTitle(), 
            			null,
            			ConstantClazz.LOG_STATU_SUCCESS, 
            			e);
    		}
    		throw e;
    	}
        return JSONObject.toJSONString(workorder);
    }
    /**
     * 根据工单id查询工单
     * @param request
     * @return
     */
    @RequestMapping(value="/findWorkByWorkId",method = RequestMethod.POST)
    @ResponseBody
    public String findWorkByWorkId(HttpServletRequest request,@RequestBody Map<String,String> map){
        String workId=map.get("workId");
    	Workorder workorder= workService.findWorkorderByWorkId(workId);
    	return JSONObject.toJSONString(workorder);
    }
    /**
     * 新增回复
     * @param request
     * @return
     * @throws Exception 
     */
    @RequestMapping(value="/addWorkOpinion",method = RequestMethod.POST)
    @ResponseBody
    public String addWorkOpinion(HttpServletRequest request,@RequestBody Map<String,Object> map) throws Exception{
        String content = String.valueOf(map.get("content"));
        Workorder workorder = new Workorder();
        BeanUtils.copyPropertiesByModel(workorder, map.get("workorder"));
        WorkOpinion workOpinion= new WorkOpinion();
        try{
        	workOpinion=workService.addWorkOpinion(workorder,content);
        	if(workorder.getLogType().equals("2")){
        		logService.addLog("反馈工单", 
        				ConstantClazz.LOG_TYPE_WORKORDER, 
        				workorder.getWorkTitle(), 
        				null, 
        				ConstantClazz.LOG_STATU_SUCCESS, 
        				null);
        	}else if(workorder.getLogType().equals("3")){
        		logService.addLog("确认工单", 
        				ConstantClazz.LOG_TYPE_WORKORDER, 
        				workorder.getWorkTitle(), 
        				null, 
        				ConstantClazz.LOG_STATU_SUCCESS, 
        				null);
        	}else if(workorder.getLogType().equals("4")){
        		logService.addLog("评价工单", 
        				ConstantClazz.LOG_TYPE_WORKORDER, 
        				workorder.getWorkTitle(), 
        				null, 
        				ConstantClazz.LOG_STATU_SUCCESS, 
        				null);
        	}
        }catch(Exception e){
        	if(workorder.getLogType().equals("2")){
        		logService.addLog("反馈工单", 
        				ConstantClazz.LOG_TYPE_WORKORDER, 
        				workorder.getWorkTitle(), 
        				null, 
        				ConstantClazz.LOG_STATU_ERROR, 
        				null);
        	}else if(workorder.getLogType().equals("3")){
        		logService.addLog("确认工单", 
        				ConstantClazz.LOG_TYPE_WORKORDER, 
        				workorder.getWorkTitle(), 
        				null, 
        				ConstantClazz.LOG_STATU_ERROR, 
        				null);
        	}else if(workorder.getLogType().equals("4")){
        		logService.addLog("评价工单", 
        				ConstantClazz.LOG_TYPE_WORKORDER, 
        				workorder.getWorkTitle(), 
        				null, 
        				ConstantClazz.LOG_STATU_ERROR, 
        				null);
        	}
        	throw e;
        }
        return JSONObject.toJSONString(workOpinion);
    }
    /**
     * 获取回复列表
     * @param request
     * @return
     */
    @RequestMapping(value="/getWorkOpinionList",method = RequestMethod.POST)
    @ResponseBody
    public String getWorkOpinionList(HttpServletRequest request,@RequestBody Map<String,String> map){
        String workId=map.get("workId");
        List<WorkOpinion> workOpinionList = workService.getWorkOpinionList(workId);
        return JSONObject.toJSONString(workOpinionList);
    }
    /**
     * 投诉
     * @param request
     * @param map
     * @return
     * @throws Exception 
     */
    @RequestMapping(value="/updateWorkFlowForFc",method = RequestMethod.POST)
    @ResponseBody
    public String updateWorkFlowForFc(HttpServletRequest request,@RequestBody Map<String,String> map) throws Exception{
    	String workId=map.get("workId");
        //用于记录日志操作
        String workTitle = map.get("workTitle") == null ? "" : map.get("workTitle").toString();
        Workorder workorder= new Workorder();
        try{
        	workorder=workService.updateWorkForFc(workId);
        	logService.addLog("一键投诉", 
        			ConstantClazz.LOG_TYPE_WORKORDER, 
        			workTitle, 
        			null,
        			ConstantClazz.LOG_STATU_SUCCESS, 
        			null);
        }catch(Exception e){
        	logService.addLog("一键投诉", 
        			ConstantClazz.LOG_TYPE_WORKORDER, 
        			workTitle, 
        			null,
        			ConstantClazz.LOG_STATU_ERROR, 
        			e);
        	throw e;
        }
        return JSONObject.toJSONString(workorder);
    }
    /**
     * 添加附件
     * @param request
     * @return
     * @throws Exception 
     */
    @RequestMapping(value="/addWorkFile",method = RequestMethod.POST)
    @ResponseBody
    public String addWorkFile(MultipartHttpServletRequest request) throws Exception{
    	SessionUserInfo sessionUser = (SessionUserInfo) request.getSession().getAttribute(ConstantClazz.SYS_SESSION_USERINFO);
        String userId = sessionUser.getUserId();
        Iterator<String> itr=request.getFileNames();
        if(itr==null || !itr.hasNext()){
        	return "";
        }
        List<WorkFile> fileList=workService.addWorkFile(itr,request,userId);    
        return JSONObject.toJSONString(fileList);
    }
    /**
     * 得到当前登陆用户的联系方式
     * @param request
     * @return
     * @throws Exception 
     */
    @RequestMapping(value="/getUserInfo",method = RequestMethod.POST)
    @ResponseBody
    public String getUserInfo(HttpServletRequest request) throws Exception{
        SessionUserInfo sessionUser = (SessionUserInfo) request.getSession().getAttribute(ConstantClazz.SYS_SESSION_USERINFO);   
        SessionUserInfo sessionUser1=workService.getUserInfo(sessionUser);
        return JSONObject.toJSONString(sessionUser1);
    }
    /**
     * 获取待处理，待反馈，待评价的工单条数
     * @param request
     * @param map
     * @return
     */
    @RequestMapping(value="/unHandleWorkNum",method = RequestMethod.POST)
    @ResponseBody
    public String unHandleWorkNum(HttpServletRequest request,@RequestBody Map<String,String> map){
    	List<Workorder> workList = workService.unHandleWorkNum(map);
    	return JSONObject.toJSONString(workList);
    }

    /**
     * 根据工单ID获取配额类工单的配额信息
     * @param request
     * @param workId
     * @return
     */
    @RequestMapping(value="/getStatisticsByWorkId")
    @ResponseBody
    public String getStatisticsByWorkId(HttpServletRequest request,@RequestBody String workId){
        CloudProject cloudProject = workService.getStatisticsByWorkId(workId);
        return JSONObject.toJSONString(cloudProject);
    }
}
