package com.eayun.work.ecmccontroller;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import com.eayun.virtualization.model.CloudProject;
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
import com.eayun.common.dao.support.Page;
import com.eayun.common.exception.AppException;
import com.eayun.common.model.EayunResponseJson;
import com.eayun.customer.model.User;
import com.eayun.ecmcuser.model.EcmcSysUser;
import com.eayun.log.ecmcsevice.EcmcLogService;
import com.eayun.sys.model.SysDataTree;
import com.eayun.work.ecmcservice.EcmcWorkorderService;
import com.eayun.work.model.WorkOpinion;
import com.eayun.work.model.WorkReport;
import com.eayun.work.model.Workorder;

@Controller
@RequestMapping("/ecmc/workorder")
@Scope("prototype")
public class EcmcWorkorderController extends BaseController {
	@Autowired
	private EcmcWorkorderService ecmcWorkService;
	@Autowired
	private EcmcLogService ecmcLogService;
	/**
     * 获取类型
     * @param request
     * @return
     */
	@RequestMapping(value="/getdatatree",method = RequestMethod.POST)
    @ResponseBody
	public String getDataTree(HttpServletRequest request,@RequestBody Map<String,String> map) {
		List<SysDataTree> dataTreeList=ecmcWorkService.getDataTree(map.get("parentId"));
		EayunResponseJson reJson = new EayunResponseJson();
		reJson.setRespCode(ConstantClazz.SUCCESS_CODE);
		reJson.setData(dataTreeList);
		return JSONObject.toJSONString(reJson);
	}
	 /**
     * 添加普通工单
     * @param request
     * @return
     * @throws Exception 
     * @throws AppException
     * @throws IOException 
     */
    @RequestMapping(value="/addworkorder",method = RequestMethod.POST)
    @ResponseBody
	public String addWorkorder(HttpServletRequest request,@RequestBody Workorder workorder) throws Exception {
    	EayunResponseJson reJson = new EayunResponseJson();
		try {
			workorder=ecmcWorkService.addWorkorder(workorder);
			ecmcLogService.addLog("新增工单", ConstantClazz.LOG_TYPE_WORKORDER, workorder.getWorkTitle(), null, 1, workorder.getWorkId(), null);
			reJson.setRespCode(ConstantClazz.SUCCESS_CODE);
			reJson.setData(workorder);
		} catch (Exception e) {
			reJson.setRespCode(ConstantClazz.ERROR_CODE);
			ecmcLogService.addLog("新增工单", ConstantClazz.LOG_TYPE_WORKORDER, workorder.getWorkTitle(),null, 0, workorder.getWorkId(), e);
			throw e;
		}
		return JSONObject.toJSONString(reJson);
	}
    /**
     * 根据工单id查询工单
     * @param request
     * @param map
     * @return
     */
    @RequestMapping(value="/findworkbyworkid",method = RequestMethod.POST)
    @ResponseBody
	public String findWorkByWorkId(HttpServletRequest request,@RequestBody Map<String,String> map) {
    	Workorder workorder = ecmcWorkService.findWorkByWorkId(map.get("workId"));
    	EayunResponseJson reJson = new EayunResponseJson();
		reJson.setRespCode(ConstantClazz.SUCCESS_CODE);
		reJson.setData(workorder);
		return JSONObject.toJSONString(reJson);
	}
    /**
     * 根据工单id查询回复列表
     * @param request
     * @param map
     * @return
     */
    @RequestMapping(value="/getworkopinionlist",method = RequestMethod.POST)
    @ResponseBody
	public String getWorkOpinionList(HttpServletRequest request,@RequestBody Map<String,String> map) {
    	List<WorkOpinion> workOpinionList = ecmcWorkService.getWorkOpinionList(map.get("workId"));
    	EayunResponseJson reJson = new EayunResponseJson();
		reJson.setRespCode(ConstantClazz.SUCCESS_CODE);
		reJson.setData(workOpinionList);
		return JSONObject.toJSONString(reJson);
	}
    /**
     * 查询未完成工单列表
     * @param request
     * @param paramsMap
     * @return
     */
    @RequestMapping(value="/getnotdoneworklist",method = RequestMethod.POST)
    @ResponseBody
	public String getNotDoneWorkList(HttpServletRequest request,Page page,@RequestBody ParamsMap paramsMap) {
    	page=ecmcWorkService.getNotDoneWorkList(page,paramsMap);
		return JSONObject.toJSONString(page);
	}
    /**
     * 查询已完成工单列表
     * @param request
     * @param page
     * @param paramsMap
     * @return
     */
    @RequestMapping(value="/getdoneworklist",method = RequestMethod.POST)
    @ResponseBody
	public String getDoneWorkList(HttpServletRequest request,Page page,@RequestBody ParamsMap paramsMap) {
		page=ecmcWorkService.getDoneWorkList(page,paramsMap);
		return JSONObject.toJSONString(page);
	}
    /**
     * 修改工单标题、内容
     * @param request
     * @param map
     * @return
     * @throws Exception 
     */
    @RequestMapping(value="/updateecmcworkorder",method = RequestMethod.POST)
    @ResponseBody
	public String updateEcmcWorkorder(HttpServletRequest request,@RequestBody Map<String, String> map) throws Exception {
		EayunResponseJson reJson = new EayunResponseJson();
		try {
			Workorder workorder=ecmcWorkService.updateEcmcWorkorder(map);
			reJson.setRespCode(ConstantClazz.SUCCESS_CODE);
			reJson.setData(workorder);
			ecmcLogService.addLog("编辑工单", ConstantClazz.LOG_TYPE_WORKORDER, workorder.getWorkTitle(), null, 1, workorder.getWorkId(), null);
		} catch (Exception e) {
			reJson.setRespCode(ConstantClazz.ERROR_CODE);
			ecmcLogService.addLog("编辑工单", ConstantClazz.LOG_TYPE_WORKORDER,map.get("workTitle"),null, 0, map.get("workId"), e);
			throw e;
		}
		return JSONObject.toJSONString(reJson);
	}
	/**
	 * 修改工单级别
	 * @param map
	 * @return
	 * @throws Exception 
	 */
    @RequestMapping(value="/updateecmcworkforworklevel",method = RequestMethod.POST)
    @ResponseBody
	public String updateEcmcWorkForWorkLevel(HttpServletRequest request,@RequestBody Map<String, String> map) throws Exception {
		EayunResponseJson reJson = new EayunResponseJson();
		try {
			Workorder workorder=ecmcWorkService.updateEcmcWorkForWorkLevel(map);
			reJson.setRespCode(ConstantClazz.SUCCESS_CODE);
			reJson.setData(workorder);
			ecmcLogService.addLog("更改级别", ConstantClazz.LOG_TYPE_WORKORDER, workorder.getWorkTitle(), null, 1, workorder.getWorkId(), null);
		} catch (Exception e) {
			reJson.setRespCode(ConstantClazz.ERROR_CODE);
			ecmcLogService.addLog("更改级别", ConstantClazz.LOG_TYPE_WORKORDER,map.get("workTitle"),null, 0, map.get("workId"), e);
			throw e;
		}
		return JSONObject.toJSONString(reJson);
	}
    /**
     * 添加回复
     * @param request
     * @param workOpinion
     * @return
     * @throws Exception 
     */
    @RequestMapping(value="/addecmcworkopinion",method = RequestMethod.POST)
    @ResponseBody
	public String addEcmcWorkopinion(HttpServletRequest request,@RequestBody WorkOpinion workOpinion) throws Exception {
		EayunResponseJson reJson = new EayunResponseJson();
		String title = workOpinion.getWorkTitle();
		String workId =workOpinion.getWorkId();
		try {
			workOpinion = ecmcWorkService.addEcmcWorkopinion(workOpinion);
			if(workOpinion==null){
				reJson.setRespCode(ConstantClazz.WARNING_CODE);
				reJson.setMessage("客户已经改变工单状态，请刷新后再试。");
				ecmcLogService.addLog("回复失败", ConstantClazz.LOG_TYPE_WORKORDER,title,null, 0, workId, new Exception("客户已经改变工单状态，请刷新后再试。"));
			}else{
				reJson.setRespCode(ConstantClazz.SUCCESS_CODE);
				reJson.setData(workOpinion);
				ecmcLogService.addLog(workOpinion.getLogName(), ConstantClazz.LOG_TYPE_WORKORDER, title, null, 1, workId, null);
			}
		} catch (Exception e) {
			reJson.setRespCode(ConstantClazz.ERROR_CODE);
			if(workOpinion != null){
			    ecmcLogService.addLog(workOpinion.getLogName(), ConstantClazz.LOG_TYPE_WORKORDER,title,null, 0, workId, e);
			}
			throw e;
		}
		return JSONObject.toJSONString(reJson);
	}
    /**
     * 受理工单
     * @param request
     * @param map
     * @return
     * @throws Exception 
     */
    @RequestMapping(value="/acceptancework",method = RequestMethod.POST)
    @ResponseBody
    public String acceptanceWork(HttpServletRequest request,@RequestBody Map<String,String> map) throws Exception{
		EayunResponseJson reJson = new EayunResponseJson();
		try {
			Workorder workorder=ecmcWorkService.acceptanceWork(map);
			if(workorder==null){
				reJson.setRespCode(ConstantClazz.WARNING_CODE);
				reJson.setMessage("当前工单已被受理，请刷新后再试。");
				ecmcLogService.addLog("受理工单", ConstantClazz.LOG_TYPE_WORKORDER,map.get("workTitle"),null, 0, map.get("workId"), new Exception("当前工单已被受理，请刷新后再试。"));
			}else{
				reJson.setRespCode(ConstantClazz.SUCCESS_CODE);
				reJson.setData(workorder);
				ecmcLogService.addLog("受理工单", ConstantClazz.LOG_TYPE_WORKORDER, workorder.getWorkTitle(), null, 1, workorder.getWorkId(), null);
			}
		} catch (Exception e) {
			reJson.setRespCode(ConstantClazz.ERROR_CODE);
			ecmcLogService.addLog("受理工单", ConstantClazz.LOG_TYPE_WORKORDER,map.get("workTitle"),null, 0, map.get("workId"), e);
			throw e;
		}
		return JSONObject.toJSONString(reJson);
    }
    /**
     * 求助、转办工单
     * @param request
     * @param map
     * @return
     * @throws Exception 
     */
    @RequestMapping(value="/truntootheruser",method = RequestMethod.POST)
    @ResponseBody
	public String trunToOtherUser(HttpServletRequest request,@RequestBody Map<String, String> map) throws Exception {
		EayunResponseJson reJson = new EayunResponseJson();
		try {
			Workorder workorder = ecmcWorkService.trunToOtherUser(map);
			reJson.setRespCode(ConstantClazz.SUCCESS_CODE);
			reJson.setData(workorder);
			ecmcLogService.addLog("更换受理工程师", ConstantClazz.LOG_TYPE_WORKORDER, workorder.getWorkTitle(), null, 1, workorder.getWorkId(), null);
		} catch (Exception e) {
			reJson.setRespCode(ConstantClazz.ERROR_CODE);
			ecmcLogService.addLog("更换受理工程师", ConstantClazz.LOG_TYPE_WORKORDER,map.get("workTitle"),null, 0, map.get("workId"), e);
			throw e;
		}
		return JSONObject.toJSONString(reJson);
	}
    /**
     * 审核通过
     * @param request
     * @param map
     * @return
     * @throws Exception 
     */
    @RequestMapping(value="/auditpasswork",method = RequestMethod.POST)
    @ResponseBody
	public String auditPassWork(HttpServletRequest request,@RequestBody Map<String, String> map) throws Exception {
		EayunResponseJson reJson = new EayunResponseJson();
		try {
			Workorder workorder = ecmcWorkService.auditPassWork(map);
			reJson.setRespCode(ConstantClazz.SUCCESS_CODE);
			reJson.setData(workorder);
			ecmcLogService.addLog("审核通过", ConstantClazz.LOG_TYPE_WORKORDER, workorder.getWorkTitle(), null, 1, workorder.getWorkId(), null);
		} catch (Exception e) {
			reJson.setRespCode(ConstantClazz.ERROR_CODE);
			ecmcLogService.addLog("审核通过", ConstantClazz.LOG_TYPE_WORKORDER,map.get("workTitle"),null, 0, map.get("workId"), e);
			throw e;
		}
		return JSONObject.toJSONString(reJson);
	}
    /**
     * 审核不通过
     * @param request
     * @param map
     * @return
     * @throws Exception 
     */
    @RequestMapping(value="/auditnotpasswork",method = RequestMethod.POST)
    @ResponseBody
	public String auditNotPassWork(HttpServletRequest request,@RequestBody Map<String, String> map) throws Exception {
		EayunResponseJson reJson = new EayunResponseJson();

		try {
			Workorder workorder = ecmcWorkService.auditNotPassWork(map);
			reJson.setRespCode(ConstantClazz.SUCCESS_CODE);
			reJson.setData(workorder);
			ecmcLogService.addLog("审核未通过", ConstantClazz.LOG_TYPE_WORKORDER, workorder.getWorkTitle(), null, 1, workorder.getWorkId(), null);
		} catch (Exception e) {
			reJson.setRespCode(ConstantClazz.ERROR_CODE);
			ecmcLogService.addLog("审核未通过", ConstantClazz.LOG_TYPE_WORKORDER,map.get("workTitle"),null, 0, map.get("workId"), e);
			throw e;
		}
		return JSONObject.toJSONString(reJson);
	}
    /**
     * 获取所有责任人的工单处理情况
     * @param request
     * @param map
     * @return
     */
    @RequestMapping(value="/countalluseracceptworkorder",method = RequestMethod.POST)
    @ResponseBody
	public String countAllUserAcceptWorkorder(HttpServletRequest request,@RequestBody Map<String, String> map) {
		List<WorkReport> list=ecmcWorkService.countAllUserAcceptWorkorder(map);
		EayunResponseJson reJson = new EayunResponseJson();
		reJson.setRespCode(ConstantClazz.SUCCESS_CODE);
		reJson.setData(list);
		return JSONObject.toJSONString(reJson);
	}
    /**
     * 获取指定责任人的工单处理情况
     * @param request
     * @param map
     * @return
     */
    @RequestMapping(value="/countuseracceptworkorder",method = RequestMethod.POST)
    @ResponseBody
	public String countUserAcceptWorkorder(HttpServletRequest request,@RequestBody Map<String, String> map) {
    	List<WorkReport> list=ecmcWorkService.countUserAcceptWorkorder(map);
    	EayunResponseJson reJson = new EayunResponseJson();
		reJson.setRespCode(ConstantClazz.SUCCESS_CODE);
		reJson.setData(list);
		return JSONObject.toJSONString(reJson);
	}
    /**
     * 获取所有的管理员和运维人员
     * @param request
     * @param map
     * @return
     */
    @RequestMapping(value="/getecmcadminandcpis",method = RequestMethod.POST)
    @ResponseBody
	public String getEcmcAdminAndCpis(HttpServletRequest request,@RequestBody Map<String,String> map) {
		List<EcmcSysUser> ecmcList=ecmcWorkService.getEcmcAdminAndCpis(map.get("userName"));
		EayunResponseJson reJson = new EayunResponseJson();
		reJson.setRespCode(ConstantClazz.SUCCESS_CODE);
		reJson.setData(ecmcList);
		return JSONObject.toJSONString(reJson);
	}
    /**
     * 查询工单所有的责任人
     * @param request
     * @param map
     * @return
     */
    @RequestMapping(value="/getworkheadlist" ,method=RequestMethod.POST)
    @ResponseBody
    public String getWorkHeadList(HttpServletRequest request,@RequestBody Map<String,String> map){
    	List<EcmcSysUser> list = ecmcWorkService.getWorkHeadList(map.get("type"),map.get("parentId"));
    	EayunResponseJson reJson = new EayunResponseJson();
		reJson.setRespCode(ConstantClazz.SUCCESS_CODE);
		reJson.setData(list);
		return JSONObject.toJSONString(reJson);
    }
	/**
	 * 获取可被求助的工程师
	 */
	@RequestMapping(value="/gettruetoother" ,method=RequestMethod.POST)
	@ResponseBody
	public String getTrueToOther(HttpServletRequest request){
		List<EcmcSysUser> list = ecmcWorkService.getEcmcAdminAndCpis();
		EayunResponseJson reJson = new EayunResponseJson();
		reJson.setRespCode(ConstantClazz.SUCCESS_CODE);
		reJson.setData(list);
		return JSONObject.toJSONString(reJson);
	}
    /**
     * 获取完成状态
     * @param request
     * @return
     */
    @RequestMapping(value="/getdoneflaglist",method = RequestMethod.POST)
    @ResponseBody
    public String getDoneFlagList(HttpServletRequest request){
    	List<SysDataTree> list= ecmcWorkService.getDoneFlagList();
    	EayunResponseJson reJson = new EayunResponseJson();
		reJson.setRespCode(ConstantClazz.SUCCESS_CODE);
		reJson.setData(list);
		return JSONObject.toJSONString(reJson);
    }
    /**
     * 获取未完成状态
     * @param request
     * @return
     */
    @RequestMapping(value="/getnodoneflaglist",method = RequestMethod.POST)
    @ResponseBody
    public String getNoDoneFlagList(HttpServletRequest request){
    	List<SysDataTree> list= ecmcWorkService.getNoDoneFlagList();
    	EayunResponseJson reJson = new EayunResponseJson();
		reJson.setRespCode(ConstantClazz.SUCCESS_CODE);
		reJson.setData(list);
		return JSONObject.toJSONString(reJson);
    }
    /**
     * 获取全部状态
     * @param request
     * @return
     */
    @RequestMapping(value="/getworkflaglist",method = RequestMethod.POST)
    @ResponseBody
    public String getWorkFlagList(HttpServletRequest request){
    	List<SysDataTree> list= ecmcWorkService.getWorkFlagList();
    	EayunResponseJson reJson = new EayunResponseJson();
		reJson.setRespCode(ConstantClazz.SUCCESS_CODE);
		reJson.setData(list);
		return JSONObject.toJSONString(reJson);
    }

	/**
	 * 获取客服状态
	 * @param request
	 * @param map
     * @return
     */
    @RequestMapping(value="/getworkflaglistforordinary",method = RequestMethod.POST)
    @ResponseBody
    public String getWorkFlagListForOrdinary(HttpServletRequest request ,@RequestBody Map<String,String> map){
    	List<SysDataTree> list = ecmcWorkService.getWorkFlagListForOrdinary(map.get("workFalg"));
    	EayunResponseJson reJson = new EayunResponseJson();
		reJson.setRespCode(ConstantClazz.SUCCESS_CODE);
		reJson.setData(list);
		return JSONObject.toJSONString(reJson);
    }
    /**
     * 获取工单待处理条数
     * @param request
     * @return
     */
    @RequestMapping(value="/getWorkCountForFlag",method = RequestMethod.POST)
    @ResponseBody
    public String getWorkCountForFlag(HttpServletRequest request,@RequestBody Map<String,String> map){
    	int count = ecmcWorkService.getWorkCountForFlag(map);
    	EayunResponseJson reJson = new EayunResponseJson();
		reJson.setRespCode(ConstantClazz.SUCCESS_CODE);
		reJson.setData(count);
		return JSONObject.toJSONString(reJson);
    }
	/**
	 * 得到用户的联系方式
	 * @param request
	 * @return
	 * @throws Exception
	 */
	@RequestMapping(value="/finduserbyuserid",method = RequestMethod.POST)
	@ResponseBody
	public String findUserByUserid(HttpServletRequest request,@RequestBody Map<String,String> map) throws Exception{
		User user=ecmcWorkService.findUserByUserid(map.get("userId"));
		EayunResponseJson reJson = new EayunResponseJson();
		reJson.setRespCode(ConstantClazz.SUCCESS_CODE);
		reJson.setData(user);
		return JSONObject.toJSONString(reJson);
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
		CloudProject cloudProject = ecmcWorkService.getStatisticsByWorkId(workId);
		return JSONObject.toJSONString(cloudProject);
	}
}
