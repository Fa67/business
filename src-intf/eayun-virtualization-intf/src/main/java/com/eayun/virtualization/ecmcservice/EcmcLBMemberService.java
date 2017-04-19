package com.eayun.virtualization.ecmcservice;

import java.util.List;
import java.util.Map;

import com.eayun.common.dao.ParamsMap;
import com.eayun.common.dao.support.Page;
import com.eayun.common.exception.AppException;
import com.eayun.virtualization.ecmcvo.CloudLdmemberVoe;
import com.eayun.virtualization.ecmcvo.CreateLBMemberVO;
import com.eayun.virtualization.ecmcvo.UpdateLBMemberVo;
import com.eayun.virtualization.model.BaseCloudLdMember;
import com.eayun.virtualization.model.CloudLdMember;
import com.eayun.virtualization.model.CloudLdPool;

public interface EcmcLBMemberService {

	/**
	 * 查询成员
	 * @param paramsMap
	 * @return
	 * @throws AppException
	 */
	public Page queryMember(ParamsMap paramsMap) throws AppException;
	
	CloudLdmemberVoe getMemberById(String memberId);
	
	BaseCloudLdMember createMember(CreateLBMemberVO member) throws AppException;
	
	CloudLdmemberVoe updateMember(UpdateLBMemberVo vo) throws AppException;
	
	boolean deleteMember(String memberId) throws AppException;
	/**
	 * 判断成员Port是否重复
	 * @param address
	 * @param protocolPort
	 * @param memberId
	 * @return
	 * @throws AppException
	 */
	public boolean checkMemberPort(String address, Long protocolPort, String memberId) throws AppException;
	/**
	 * 查询子网下的主机信息
	 * @param cloudLdm
	 * @return
	 * @throws Exception
	 */
	public List<CloudLdMember> getMemeberListBySubnet(CloudLdMember cloudLdm) throws Exception;

	public void deleteMemberByVm(String vmId);


	@SuppressWarnings("rawtypes")
    public List<BaseCloudLdMember> createMember(Map map) throws AppException;

	public List<CloudLdMember> addMember(CloudLdPool cloudLdPool) throws AppException;

	public boolean checkMemberExsit(CloudLdMember member) throws AppException;

    public List<CloudLdMember> getMemberList(String poolId,String checkRole) throws AppException;

    public CloudLdMember update(CloudLdMember member) throws Exception;

    public void deleteMember(CloudLdMember member) throws AppException;
    
    public void deleteMemberWithoutStack(CloudLdMember cloudLdMember) throws Exception;

	public void changeMembersStatus(String poolId) throws Exception;
}
