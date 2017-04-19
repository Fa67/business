package com.eayun.virtualization.apiservice.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.eayun.common.annotation.ApiService;
import com.eayun.common.redis.JedisUtil;
import com.eayun.eayunstack.service.OpenstackMemberService;
import com.eayun.virtualization.apiservice.MemberApiService;
import com.eayun.virtualization.baseservice.BaseMemberService;
import com.eayun.virtualization.dao.CloudLdMemberDao;
import com.eayun.virtualization.model.BaseCloudLdMember;
import com.eayun.virtualization.model.CloudLdMember;

/**
 * 
 * 负载均衡(成员)API业务<br>
 * -----------------
 * @author chengxiaodong
 * @date 2016-12-2
 *
 */
@Service
@Transactional
public class MemberApiServiceImpl extends BaseMemberService implements MemberApiService {

	@Autowired
	private CloudLdMemberDao memberDao;
	@Autowired
	private OpenstackMemberService openStackService;
	@Autowired
	private JedisUtil jedisUtil;

	
	/**
	 * 删除主机时 级联删除成员信息
	 * @author chengxiaodong
	 * @param vmId
	 */
	public void deleteMemberByVm(String vmId){
		StringBuffer hql = new StringBuffer("from BaseCloudLdMember where vmId = ? ");
		
		List<BaseCloudLdMember> memList = memberDao.find(hql.toString(), new Object[]{vmId});
		if(null!=memList && memList.size()>0){
			for(BaseCloudLdMember member :memList){
				CloudLdMember ldMember = new CloudLdMember();
				ldMember.setDcId(member.getDcId());
				ldMember.setPrjId(member.getPrjId());
				ldMember.setMemberId(member.getMemberId());
				
				deleteMember(ldMember);
			}
		}
	}
	
	
	/**
	 * 删除成员
	 * @author chengxiaodong
	 * @param member
	 */
	public void deleteMember(CloudLdMember member){
		boolean flag = openStackService.delete(member.getDcId(), member.getPrjId(), member.getMemberId());
		if(flag){
			memberDao.delete(member.getMemberId());
		}
	}

}
