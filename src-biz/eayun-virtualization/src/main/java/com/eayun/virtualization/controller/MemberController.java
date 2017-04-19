package com.eayun.virtualization.controller;


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
import org.springframework.web.bind.annotation.ResponseBody;

import com.alibaba.fastjson.JSONObject;
import com.eayun.common.ConstantClazz;
import com.eayun.common.controller.BaseController;
import com.eayun.common.filter.SessionUserInfo;
import com.eayun.log.service.LogService;
import com.eayun.virtualization.model.CloudLdMember;
import com.eayun.virtualization.model.CloudLdPool;
import com.eayun.virtualization.service.MemberService;

@Controller
@RequestMapping("/cloud/loadbalance/member")
@Scope("prototype")
public class MemberController extends BaseController{
private static final Logger log = LoggerFactory.getLogger(MemberController.class);
	@Autowired
	private LogService logService;
	@Autowired
	private MemberService memberService;
	
	/**
	 * 查询负载均衡器下查询成员列表信息
	 * 
	 * @author zhouhaitao
	 * @param request
	 * @param member
	 * @return
	 * @throws Exception
	 */
	@RequestMapping("/getMemberList")
	@ResponseBody                 
	public String getMemberList(HttpServletRequest request,@RequestBody Map map) throws Exception{
		log.info("查询负载均衡器下的成员列表");
		List<CloudLdMember> list = new ArrayList<CloudLdMember>();
		JSONObject json = new JSONObject();
		try {
			String poolId=(String) map.get("poolId");
        	String checkRole=(String) map.get("checkRole");
			list = memberService.getMemberList(poolId,checkRole);
			json.put("data", list);
		}catch (Exception e) {
			throw e;
		}
		return json.toJSONString();
	}
	
	/**
	 * 增加成员
	 * 
	 * @author zhouhaitao
	 * @param request
	 * @param cloudLdPool
	 * @return
	 * @throws AppException
	 */
	@RequestMapping("/addMember")
	@ResponseBody            
	public String addMember(HttpServletRequest request,@RequestBody CloudLdPool cloudLdPool) throws Exception{
		log.info("添加成员");
		JSONObject json =new JSONObject();
		SessionUserInfo sessionUser = null;
		List<CloudLdMember> memberList = new ArrayList<CloudLdMember>();
		try {
			sessionUser = (SessionUserInfo) request.getSession().getAttribute(ConstantClazz.SYS_SESSION_USERINFO);
			memberList = memberService.addMember(cloudLdPool,sessionUser); 
			logService.addLog("添加成员",ConstantClazz.LOG_TYPE_POOL,  cloudLdPool.getPoolName(), cloudLdPool.getPrjId(),
					ConstantClazz.LOG_STATU_SUCCESS, null);
			json.put("data", memberList);
			json.put("respCode", ConstantClazz.SUCCESS_CODE_ADD);
		}catch (Exception e) {
			logService.addLog("添加成员",ConstantClazz.LOG_TYPE_POOL,  cloudLdPool.getPoolName(), cloudLdPool.getPrjId(),
					ConstantClazz.LOG_STATU_ERROR, e);
			json.put("respCode", ConstantClazz.ERROR_CODE);
			throw e;
		}

		return json.toJSONString();
	}
	
	/**
	 * 修改成员的信息
	 * 
	 * @author zhouhaitao
	 * @param request
	 * @param member
	 * @return
	 * @throws Exception
	 */
	@RequestMapping("/updateMember")
	@ResponseBody             
	public String updateMember(HttpServletRequest request,@RequestBody CloudLdMember member) throws Exception{
		log.info("修改成员信息");
		JSONObject json = new JSONObject();
		CloudLdMember result=null;
		try {
			result = memberService.update(member);
			logService.addLog("编辑成员", ConstantClazz.LOG_TYPE_POOL,  member.getPoolName(), member.getPrjId(),
					ConstantClazz.LOG_STATU_SUCCESS, null);
			json.put("data", result);
			json.put("respCode", ConstantClazz.SUCCESS_CODE_UPDATE);
		}catch (Exception e) {
			logService.addLog("编辑成员", ConstantClazz.LOG_TYPE_POOL,  member.getPoolName(), member.getPrjId(),
					ConstantClazz.LOG_STATU_ERROR, e);
			json.put("respCode", ConstantClazz.ERROR_CODE);
			throw e;
		}
		return json.toJSONString();
	}
	
	
	/**
	 * 删除成员
	 * 
	 * @author zhouhaitao
	 * @param request
	 * @param member
	 * @return
	 * @throws Exception
	 */    
	@RequestMapping("/deleteMember")
	@ResponseBody 
	public String deleteMember(HttpServletRequest request,@RequestBody CloudLdMember member)throws Exception {
		JSONObject json = new JSONObject();
		try {
			memberService.deleteMember(member);
			logService.addLog("移除成员",ConstantClazz.LOG_TYPE_POOL, member.getPoolName(), member.getPrjId(),
					ConstantClazz.LOG_STATU_SUCCESS, null);
			json.put("respCode", ConstantClazz.SUCCESS_CODE_DELETE);
		}catch (Exception e) {
			logService.addLog("移除成员",ConstantClazz.LOG_TYPE_POOL, member.getPoolName(), member.getPrjId(),
					ConstantClazz.LOG_STATU_ERROR, e);
			json.put("respCode", ConstantClazz.ERROR_CODE);
			throw e;
		}
		return json.toJSONString();
	}
	
	/**
	 * 校验成员是否存在
	 * 
	 * @author zhouhaitao
	 * @param request
	 * @param member
	 * @return
	 * @throws Exception
	 */    
	@RequestMapping("/checkMemberExsit")
	@ResponseBody 
	public String checkMemberExsit(HttpServletRequest request,@RequestBody CloudLdMember member)throws Exception {
		log.info("校验成员是否存在！");
		boolean flag =false ;
		JSONObject json = new JSONObject();
		try {
			flag = memberService.checkMemberExsit(member);
			json.put("data", flag);
		}catch (Exception e) {
			throw e;
		}
		return json.toJSONString();
	}
	
	/**
	 * 查询子网的主机对象
	 * 
	 * @author zhouhaitao
	 * @param request
	 * @param member
	 * @return
	 * @throws Exception
	 */    
	@RequestMapping("/getMemeberListBySubnet")
	@ResponseBody 
	public String getMemeberListBySubnet(HttpServletRequest request,@RequestBody CloudLdMember member) throws Exception{
		log.info("查询子网的主机对象");
		JSONObject json = new JSONObject();
		List<CloudLdMember> list = new ArrayList<CloudLdMember>();
		try {
			list = memberService.getMemeberListBySubnet(member);
			json.put("data", list);
		}catch (Exception e) {
			throw e;
		}
		return json.toJSONString();
	}
	
}
