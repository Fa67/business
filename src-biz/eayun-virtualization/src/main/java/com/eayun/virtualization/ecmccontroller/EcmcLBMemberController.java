package com.eayun.virtualization.ecmccontroller;


import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import com.alibaba.dubbo.common.json.JSONObject;
import com.eayun.common.filter.SessionUserInfo;
import com.eayun.log.ecmcsevice.EcmcLogService;
import com.eayun.virtualization.model.BaseCloudLdMember;
import com.eayun.virtualization.model.CloudLdPool;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.eayun.common.ConstantClazz;
import com.eayun.common.dao.ParamsMap;
import com.eayun.common.exception.AppException;
import com.eayun.common.model.EayunResponseJson;
import com.eayun.virtualization.ecmcservice.EcmcLBMemberService;
import com.eayun.virtualization.ecmcvo.CloudLdmemberVoe;
import com.eayun.virtualization.ecmcvo.CreateLBMemberVO;
import com.eayun.virtualization.ecmcvo.UpdateLBMemberVo;
import com.eayun.virtualization.model.CloudLdMember;

@Controller
@RequestMapping("/ecmc/virtual/loadbalance/member")
@Scope("prototype")
public class EcmcLBMemberController {
	private static final Log log = LogFactory.getLog(EcmcLBMemberController.class);
	
	@Autowired
	private EcmcLBMemberService ecmcLBMemberService;

    @Autowired
    private EcmcLogService ecmcLogService;
	
	
	/**
	 * 查询所有成员的信息
	 * @param request
	 * @return
	 * @throws AppException
	 */
	@RequestMapping("/querymember")
	@ResponseBody
	public Object queryMember(HttpServletRequest request, @RequestBody ParamsMap paramsMap) throws AppException{
		log.info("查询LB成员开始");
		return ecmcLBMemberService.queryMember(paramsMap);
	}
	
	/**
	 * 查询指定id的成员的信息
	 * @param request
	 * @return
	 * @throws AppException
	 */
	@RequestMapping("/getmemberbyid")
	@ResponseBody
	public Object getMemberById(HttpServletRequest request, @RequestBody Map<String, String> params) throws AppException{
		log.info("查询单个LB成员开始");
		return ecmcLBMemberService.getMemberById(params.get("memberId"));
	}
	
	@RequestMapping("/createmember")
	@ResponseBody
	public Object createMember(HttpServletRequest request, @RequestBody Map map) throws AppException{
		log.info("创建LB成员开始");
		EayunResponseJson reJson = new EayunResponseJson();
		try {
			List<BaseCloudLdMember> list = ecmcLBMemberService.createMember(map);
			reJson.setRespCode(ConstantClazz.SUCCESS_CODE);
			reJson.setData(list);
		}catch (AppException e){
			reJson.setRespCode(ConstantClazz.ERROR_CODE);
			throw e;
		}
		;return reJson;

	}

//	/**
//	 * 删除成员的信息
//	 * @param request
//	 * @return
//	 * @throws AppException
//	 */
//	@RequestMapping("/deletemember")
//	@ResponseBody
//	public Object deleteMember(HttpServletRequest request, @RequestBody Map<String, String> params) throws AppException{
//		log.info("删除LB成员开始");
//		EayunResponseJson delJson = new EayunResponseJson();
//		try {
//			delJson.setData(ecmcLBMemberService.deleteMember(params.get("memberId")));
//			delJson.setRespCode(ConstantClazz.SUCCESS_CODE);
//		}catch (AppException e) {
//			throw e;
//		} catch (Exception e) {
//			log.error(e.toString(),e);
//			throw new AppException("error.globe.system", e);
//		}
//		return delJson;
//	}
	
	/**
	 * 修改成员的信息
	 * @param request
	 * @return
	 * @throws AppException
	 */
	@RequestMapping("/updatemember")
	@ResponseBody
	public Object updateMember(HttpServletRequest request, @RequestBody UpdateLBMemberVo vo) throws AppException{
		log.info("修改LB成员开始");
		EayunResponseJson reJson = new EayunResponseJson();
		try {
			CloudLdmemberVoe result = ecmcLBMemberService.updateMember(vo);
			if (result!=null) {
				reJson.setRespCode(ConstantClazz.SUCCESS_CODE);
				reJson.setData(result);
			}else{
				reJson.setRespCode(ConstantClazz.ERROR_CODE);
			}
		}catch (AppException e) {
			throw e;
		} catch (Exception e) {
			log.error(e.toString(),e);
			throw new AppException("error.globe.system", e);
		}
		return reJson;
	}
	/**
	 * 添加/修改成员时判断主机+端口是否重复
	 * @param params
	 * @return
	 * @throws AppException
	 */
	@RequestMapping("/checkmemberport")
	@ResponseBody
	public Object checkMemberPort(@RequestBody Map<String, String> params) throws AppException {
		log.info("验证主机端口是否重复开始");
		EayunResponseJson responseJson = new EayunResponseJson();
		boolean exists = ecmcLBMemberService.checkMemberPort(params.get("address"), Long.valueOf(params.get("protocolPort")), params.get("memberId"));
		responseJson.setData(exists);
		responseJson.setRespCode(ConstantClazz.SUCCESS_CODE);
		return responseJson;
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
		com.alibaba.fastjson.JSONObject json =new com.alibaba.fastjson.JSONObject();
		List<CloudLdMember> memberList = new ArrayList<CloudLdMember>();
		String poolName = cloudLdPool.getPoolName()==null?"----":cloudLdPool.getPoolName();
		String prjId = cloudLdPool.getPrjId()==null?"----":cloudLdPool.getPrjId();
		String poolId = cloudLdPool.getPoolId()==null?"----":cloudLdPool.getPoolId();
		try {
			memberList = ecmcLBMemberService.addMember(cloudLdPool);
			json.put("data", memberList);
			json.put("respCode", ConstantClazz.SUCCESS_CODE);
            ecmcLogService.addLog("添加成员", ConstantClazz.LOG_TYPE_MEMBER, poolName, prjId, 1, poolId, null);
		}catch (Exception e) {
            json.put("respCode", ConstantClazz.ERROR_CODE);
            ecmcLogService.addLog("添加成员", ConstantClazz.LOG_TYPE_MEMBER, poolName, prjId, 0, poolId, e);
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
		com.alibaba.fastjson.JSONObject json = new com.alibaba.fastjson.JSONObject();
		try {
			flag = ecmcLBMemberService.checkMemberExsit(member);
			json.put("data", flag);
		}catch (Exception e) {
			throw e;
		}
		return json.toJSONString();
	}

	@RequestMapping("/getMemeberListBySubnet")
	@ResponseBody
	public String getMemeberListBySubnet(HttpServletRequest request,@RequestBody CloudLdMember member) throws Exception{
		log.info("查询子网的主机对象");
		com.alibaba.fastjson.JSONObject json = new com.alibaba.fastjson.JSONObject();
		List<CloudLdMember> list = new ArrayList<CloudLdMember>();
		try {
			list = ecmcLBMemberService.getMemeberListBySubnet(member);
			json.put("data", list);
		}catch (Exception e) {
			throw e;
		}
		return json.toJSONString();
	}

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
        com.alibaba.fastjson.JSONObject json = new com.alibaba.fastjson.JSONObject();
        try {
        	String poolId=(String) map.get("poolId");
        	String checkRole=(String) map.get("checkRole");
            list = ecmcLBMemberService.getMemberList(poolId,checkRole);
            json.put("data", list);
        }catch (Exception e) {
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
        com.alibaba.fastjson.JSONObject json = new com.alibaba.fastjson.JSONObject();
        CloudLdMember result=null;
		String poolName = member.getPoolName()==null?"----":member.getPoolName();
		String prjId = member.getPrjId()==null?"----":member.getPrjId();
		String poolId = member.getPoolId()==null?"----":member.getPoolId();
        try {
            result = ecmcLBMemberService.update(member);
            json.put("data", result);
            json.put("respCode", ConstantClazz.SUCCESS_CODE);
            ecmcLogService.addLog("编辑成员", ConstantClazz.LOG_TYPE_MEMBER, poolName, prjId, 1, poolId, null);
        }catch (Exception e) {
            json.put("respCode", ConstantClazz.ERROR_CODE);
            ecmcLogService.addLog("编辑成员", ConstantClazz.LOG_TYPE_MEMBER, poolName, prjId, 0, poolId, e);
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
        com.alibaba.fastjson.JSONObject json = new com.alibaba.fastjson.JSONObject();
		String poolName = member.getPoolName()==null?"----":member.getPoolName();
		String prjId = member.getPrjId()==null?"----":member.getPrjId();
		String poolId = member.getPoolId()==null?"----":member.getPoolId();
        try {
            ecmcLBMemberService.deleteMember(member);
            json.put("respCode", ConstantClazz.SUCCESS_CODE);
            ecmcLogService.addLog("移除成员", ConstantClazz.LOG_TYPE_MEMBER, poolName, prjId, 1, poolId, null);
        }catch (Exception e) {
            json.put("respCode", ConstantClazz.ERROR_CODE);
            ecmcLogService.addLog("移除成员", ConstantClazz.LOG_TYPE_MEMBER, poolName, prjId, 0, poolId, e);
            throw e;
        }
        return json.toJSONString();
    }


}
