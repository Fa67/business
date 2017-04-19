package com.eayun.virtualization.service;


import java.util.List;

import com.alibaba.fastjson.JSONObject;
import com.eayun.common.exception.AppException;
import com.eayun.common.filter.SessionUserInfo;
import com.eayun.virtualization.model.BaseCloudLdMember;
import com.eayun.virtualization.model.CloudLdMember;
import com.eayun.virtualization.model.CloudLdPool;

/**
 * MemberService
 * 
 * @Filename: MemberService.java
 * @Description:
 * @Version: 1.0
 * @Author: liyanchao
 * @Email: yanchao.li@eayun.com
 * @History:<br> <li>Date: 2015年11月12日</li> <li>Version: 1.0</li> <li>Content:
 *               create</li>
 * 
 */
public interface MemberService {
	/*
	 *根据prjId查询个数 
	 */
	public int getCountByPrjId(String prjId);
	
	/**
	 * 查询负载均衡器下的成员信息
	 * 
	 * @author zhouhaitao
	 * @param poolId
	 * @return
	 * @throws Exception
	 */
	public List<CloudLdMember> getMemberList(String poolId,String checkRole) throws Exception;
	
	/**
	 * 添加成员
	 * 
	 * @author zhouhaitao
	 * @param pool
	 * @param sessionUser
	 */
	public List<CloudLdMember> addMember(CloudLdPool pool , SessionUserInfo sessionUser) throws AppException ;
	
	/**
	 * 修改成员
	 * 
	 * @author zhouhaitao
	 * @param member
	 * @return
	 * @throws AppException
	 */
	public CloudLdMember update(CloudLdMember member) throws Exception;
	
	/**
	 * 删除成员
	 * 
	 * @author zhouhaitao
	 * @param member
	 * @throws Exception 
	 */
	public void deleteMember(CloudLdMember member);
	
	/**
	 * 同步修改成员信息
	 * 
	 * @author zhouhaitao
	 * @param cloudLdm
	 * @return
	 */
	public boolean updateLdMember(CloudLdMember cloudLdm);
	
	/**
	 * 校验成员是否存在
	 * 
	 * @author zhouhaitao
	 * @param cloudLdm
	 * @return
	 */
	public boolean checkMemberExsit(CloudLdMember cloudLdm);
	
	/**
	 * 查询子网下的主机信息
	 * 
	 * @author zhouhaitao
	 * @param cloudLdm
	 * @return
	 * @throws Exception
	 */
	public List<CloudLdMember> getMemeberListBySubnet(CloudLdMember cloudLdm) throws Exception;
	
	/**
	 * 删除主机时 级联删除成员信息
	 * 
	 * @author zhouhaitao
	 * @param vmId
	 */
	public void deleteMemberByVm(String vmId);

	/**
	 * 更新节点状态以及是否为流量承担者
	 * @param poolId
	 * @throws Exception
     */
	public void changeMembersStatusFromPoolByOpenstack(String poolId) throws Exception;

	/**
	 * 更新节点状态以及是否为流量承担者
	 * @param poolId
	 * @throws Exception
	 */
	public void changeMembersStatusFromPoolByDb(String poolId) throws Exception;
	
	/**
	 * 更新节点状态以及是否为流量承担者(解除健康检查时)
	 * @param poolId
	 * @throws Exception
	 */
	public void changeMembersStatus(String poolId) throws Exception;
	/**
	 * 成员计划任务中修改成员状态
	 * @param baseCloudLdMember
	 * @throws Exception
	 */
	public void updateMember(BaseCloudLdMember baseCloudLdMember) throws Exception;
	public List<CloudLdMember> getMemberListByPool(String poolId) throws Exception;
	/**
	 * 获取底层指定ID的资源<br>
	 * ------------------
	 * 
	 * @param value
	 * @param json
	 * 
	 * @return
	 */
	public JSONObject get(JSONObject valueJson) throws Exception;
	/**
	 * 修改负载均衡成员信息
	 * @param cloudVm
	 * @return
	 */
	public CloudLdMember  updateMember(CloudLdMember cloudLdm);
	
}
