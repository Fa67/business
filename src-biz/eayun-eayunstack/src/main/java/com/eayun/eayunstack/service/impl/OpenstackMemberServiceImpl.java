package com.eayun.eayunstack.service.impl;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSONObject;
import com.eayun.common.exception.AppException;
import com.eayun.datacenter.model.BaseDcDataCenter;
import com.eayun.eayunstack.model.Member;
import com.eayun.eayunstack.service.OpenstackMemberService;
import com.eayun.eayunstack.util.OpenstackUriConstant;
import com.eayun.eayunstack.util.RestTokenBean;
import com.eayun.virtualization.model.BaseCloudLdMember;

@Service
public class OpenstackMemberServiceImpl extends
		OpenstackBaseServiceImpl<Member> implements OpenstackMemberService {
	private static final Log log = LogFactory
			.getLog(OpenstackMemberServiceImpl.class);

	private void initData(Member data, JSONObject object) {

	}

	/**
	 * 获取指定数据中心的指定项目下的成员列表
	 */
	public List<Member> list(String datacenterId, String projectId)
			throws AppException {
		List<Member> list = null;
		list = this.listAll(datacenterId);
		if (list != null && list.size() > 0) {
			List<Member> temp = new ArrayList<Member>();
			for (Member member : list) {
				if (member.getTenant_id().equals(projectId)) {
					temp.add(member);
				}
			}
			list = temp;
		}

		return list;
	}

	/**
	 * 成员列表查询所有
	 */
	@Override
	public List<Member> listAll(String datacenterId) throws AppException {
		List<Member> list = null;
		RestTokenBean restTokenBean = getRestTokenBean(datacenterId,
				OpenstackUriConstant.NETWORK_SERVICE_URI);
		restTokenBean.setUrl(OpenstackUriConstant.MEMBER_URI);
		List<JSONObject> result = restService.list(restTokenBean,
				OpenstackUriConstant.MEMBER_DATA_NAMES);
		if (result != null && result.size() > 0) {
			for (JSONObject jsonObject : result) {
				if (list == null) {
					list = new ArrayList<Member>();
				}
				Member data = restService.json2bean(jsonObject, Member.class);
				initData(data, jsonObject);
				list.add(data);
			}
		}

		return list;
	}

	/**
	 * 根据id查询成员某一条数据
	 */
	public Member getById(String datacenterId, String projectId, String id)
			throws AppException {
		Member object = null;
		RestTokenBean restTokenBean = getRestTokenBean(datacenterId,
				OpenstackUriConstant.NETWORK_SERVICE_URI);
		JSONObject json = restService.getById(restTokenBean,
				OpenstackUriConstant.MEMBER_URI + "/",
				OpenstackUriConstant.MEMBER_DATA_NAME, id);
		if (json != null) {
			object = restService.json2bean(json, Member.class);
			initData(object, json);
		}

		return object;
	}

	/**
	 * 创建成员
	 * 
	 * @param datacenterId
	 * @param projectId
	 * @param data
	 *            json字符串，包含待创建的成员的具体配置信息
	 * @return
	 * @throws AppException
	 */
	public Member create(String datacenterId, String projectId, JSONObject data)
			throws AppException {
		Member object = null;
		RestTokenBean restTokenBean = getRestTokenBean(datacenterId, projectId,
				OpenstackUriConstant.NETWORK_SERVICE_URI);
		restTokenBean.setUrl(OpenstackUriConstant.MEMBER_URI);
		JSONObject result = restService.create(restTokenBean,
				OpenstackUriConstant.MEMBER_DATA_NAME, data);
		object = restService.json2bean(result, Member.class);

		return object;
	}

	/**
	 * 删除成员
	 * 
	 * @param datacenterId
	 * @param projectId
	 * @param id
	 * @return
	 * @throws AppException
	 */
	public boolean delete(String datacenterId, String projectId, String id)
			throws AppException {
		RestTokenBean restTokenBean = getRestTokenBean(datacenterId,
				OpenstackUriConstant.NETWORK_SERVICE_URI);
		restTokenBean.setUrl(OpenstackUriConstant.MEMBER_URI + "/" + id);
		return restService.delete(restTokenBean);
	}

	/**
	 * 修改成员
	 * 
	 * @param datacenterId
	 * @param projectId
	 * @param data
	 *            修改成员的配置信息
	 * @param id
	 * @return
	 * @throws AppException
	 */
	public Member update(String datacenterId, String projectId,
			JSONObject data, String id) throws AppException {
		Member object = null;
		RestTokenBean restTokenBean = getRestTokenBean(datacenterId,
				OpenstackUriConstant.NETWORK_SERVICE_URI);
		restTokenBean.setUrl(OpenstackUriConstant.MEMBER_URI + "/" + id);
		JSONObject result = restService.update(restTokenBean,
				OpenstackUriConstant.MEMBER_DATA_NAME, data);
		object = restService.json2bean(result, Member.class);
		return object;
	}
	
	public JSONObject get (String dcId,String fwId) throws Exception{
		RestTokenBean restTokenBean = getRestTokenBean(dcId,
				OpenstackUriConstant.NETWORK_SERVICE_URI);
		return restService.getJSONById(restTokenBean, OpenstackUriConstant.MEMBER_URI+"/", fwId);
	}
	
	/**                                                                                                         
	 * 获取底层数据中心下的                                                               
	 * -----------------                                                                                      
	 * @author zhouhaitao                                                                                     
	 * @param dataCenter                                                                                      
	 *                                                                                                        
	 * @return                                                                                                
	 *                                                                                                        
	 */                                                                                                       
	public List<BaseCloudLdMember> getStackList(BaseDcDataCenter dataCenter) {                                  
		List <BaseCloudLdMember> list = new ArrayList<BaseCloudLdMember>();                                         
		RestTokenBean restTokenBean = getRestTokenBean(dataCenter.getId(),                                              
				OpenstackUriConstant.NETWORK_SERVICE_URI);                                                          
		restTokenBean.setUrl(OpenstackUriConstant.MEMBER_URI);                                           
		List<JSONObject> result = restService.list(restTokenBean,OpenstackUriConstant.MEMBER_DATA_NAMES);
                                                                                                            
		if (result != null && result.size() > 0) {                                                              
			for (JSONObject jsonObject : result) {                                                                
				Member data = restService.json2bean(jsonObject,                                             
						Member.class);                                                                            
				 BaseCloudLdMember ccn=new BaseCloudLdMember(data,dataCenter.getId());                                 
					list.add(ccn);                                                                                    
				}                                                                                                   
			}                                                                                                     
		return list;                                                                                            
	}                                                                                                         
}
