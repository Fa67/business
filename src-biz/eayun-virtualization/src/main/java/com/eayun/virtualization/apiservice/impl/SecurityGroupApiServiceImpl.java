package com.eayun.virtualization.apiservice.impl;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.persistence.Query;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.eayun.common.constant.ApiInstanceConstant;
import com.eayun.common.exception.AppException;
import com.eayun.common.util.StringUtil;
import com.eayun.eayunstack.service.OpenstackVmService;
import com.eayun.virtualization.apiservice.SecurityGroupApiService;
import com.eayun.virtualization.baseservice.BaseSecurityGroupService;
import com.eayun.virtualization.dao.CloudSecurityGroupDao;
import com.eayun.virtualization.dao.CloudSecurityGroupRuleDao;
import com.eayun.virtualization.model.BaseCloudSecurityGroup;
import com.eayun.virtualization.model.BaseCloudVmSgroup;
import com.eayun.virtualization.model.CloudSecurityGroup;
import com.eayun.virtualization.model.CloudSecurityGroupRule;
import com.eayun.virtualization.model.CloudVm;

@Service
@Transactional
public class SecurityGroupApiServiceImpl extends BaseSecurityGroupService
			implements SecurityGroupApiService{

	@Autowired private OpenstackVmService openstackVmService;
	
	@Autowired private CloudSecurityGroupRuleDao securityGroupRuleDao;
	
	@Autowired
	private CloudSecurityGroupDao securityGroupDao;
	
	
	
	
	@Override
	@Transactional(noRollbackFor=AppException.class)
	public void instanceJoinSecurityGroup(CloudVm cloudvm, CloudSecurityGroup cloudSecurityGroup) throws AppException {
			try {
				List<BaseCloudSecurityGroup> toBeAddList =new ArrayList<BaseCloudSecurityGroup>();
				BaseCloudSecurityGroup  basecsg=new BaseCloudSecurityGroup();
				basecsg.setSgId(cloudSecurityGroup.getSgId());
				toBeAddList.add(basecsg);
				openstackVmService.editVmSecurityGroup(cloudvm, toBeAddList,null);//底层安全组添加云主机
				BaseCloudVmSgroup vsg=new BaseCloudVmSgroup(cloudvm.getVmId(),cloudSecurityGroup.getSgId());
				securityGroupRuleDao.merge(vsg);//添加安全组云主机关联表数据
			} catch (AppException e) {
				throw e;
			}
			
			
	}

	@Override
	@Transactional(noRollbackFor=AppException.class)
	public void instanceLeaveSecurityGroup(CloudVm cloudvm, CloudSecurityGroup cloudSecurityGroup) throws AppException {
			try{
				List<BaseCloudSecurityGroup> toBeDelList =new ArrayList<BaseCloudSecurityGroup>();
				BaseCloudSecurityGroup  basecsg=new BaseCloudSecurityGroup();
				basecsg.setSgId(cloudSecurityGroup.getSgId());
				toBeDelList.add(basecsg);
				openstackVmService.editVmSecurityGroup(cloudvm, null,toBeDelList);//底层安全组移除云主机
				securityGroupRuleDao.deletedvmsgroup( cloudvm.getVmId(),cloudSecurityGroup.getSgId());
				}catch(AppException e){
					throw e;
				}
		
		
	}

	@Override
	public CloudSecurityGroup getSecurityGroupBySgId(String securityGroupId) throws AppException {
		StringBuffer sql = new StringBuffer();
		sql.append(" select cs.sg_id,cp.customer_id ");
		sql.append(" from cloud_securitygroup cs  ");
		sql.append(" LEFT JOIN cloud_project cp on cs.prj_id = cp.prj_id ");
		sql.append(" where sg_id = ? ");
		Query result = securityGroupRuleDao.createSQLNativeQuery(sql.toString(), new Object[]{securityGroupId});
		List resultList = result.getResultList();
		if(resultList != null && resultList.size() > 0){
			Object [] obj = (Object[]) resultList.get(0);
			CloudSecurityGroup cloudSecurityGroup = new CloudSecurityGroup();
			cloudSecurityGroup.setSgId(String.valueOf(obj[0]));
			cloudSecurityGroup.setCusId(String.valueOf(obj[1]));
			return cloudSecurityGroup;
		}
		return null;
	}

	@Override
	public List<CloudSecurityGroup> getGroupList(String dcId, String cusId, String [] securityGroupId, 
			String searchWord, String offset, String limit) throws AppException {
		int index = 0;
		Object[] args = new Object[securityGroupId == null ? 10:securityGroupId.length + 10];
		StringBuffer sql = new StringBuffer();
		sql.append(" select csg.sg_id, ");
		sql.append(" csg.sg_name, ");
		sql.append(" csg.sg_description, ");
		sql.append(" csg.default_group, ");
//		sql.append(" csg.dc_id, ");
		sql.append(" dd.api_dc_code, ");
		sql.append(" csg.create_time ");
		sql.append(" from cloud_securitygroup csg ");
		sql.append(" LEFT JOIN cloud_project cp on csg.prj_id = cp.prj_id ");
		sql.append(" LEFT JOIN dc_datacenter dd on csg.dc_id = dd.id ");
		sql.append(" where cp.customer_id = ? and cp.dc_id = ? ");
		args[index++] = cusId;
		args[index++] = dcId;
		if (!StringUtil.isEmpty(searchWord)) {
			sql.append(" and binary (case csg.sg_name  when  binary 'default' then '默认安全组' else csg.sg_name end) like ? ");
			args[index++] = "%" + searchWord + "%";
		}
		if(securityGroupId != null && securityGroupId.length > 0){
			sql.append(" and ( ");
			sql.append(" csg.sg_id = ? ");
			args[index++] = securityGroupId[0];
			if(securityGroupId.length > 1){
				for(int i=1; i < securityGroupId.length; i++){
					sql.append(" or csg.sg_id = ? ");
					args[index++] = securityGroupId[i];
				}
			}
			sql.append(" ) ");
		}
		sql.append(" order by IF (ISNULL(csg.create_time),1,0), csg.create_time desc ");
		sql.append("LIMIT ? OFFSET ?");
		args[index++] = Integer.valueOf(limit);
		args[index++] = Integer.valueOf(offset) * Integer.valueOf(limit);
		Object[] params = new Object[index];
		System.arraycopy(args, 0, params, 0, index);
		javax.persistence.Query query = securityGroupRuleDao.createSQLNativeQuery(sql.toString(), params);
		List list = query.getResultList();
		if(!list.isEmpty() && list.size() > 0){
			List<CloudSecurityGroup> securityGroupList = new ArrayList<CloudSecurityGroup>();
			for(int i = 0; i < list.size(); i++){
				int ind = 0;
				Object [] obj = (Object [])list.get(i);
				CloudSecurityGroup sg = new CloudSecurityGroup();
				sg.setSgId(String.valueOf(obj[ind++]));
				sg.setSgName(String.valueOf(obj[ind++]));
				sg.setSgDescription(null != obj[ind++]?String.valueOf(obj[ind-1]):"");
				sg.setDefaultGroup(null != obj[ind++]?String.valueOf(obj[ind-1]):"");
				sg.setDcId(null != obj[ind++]?String.valueOf(obj[ind-1]):"");
				sg.setCreateTime((Date)obj[ind++]);
				securityGroupList.add(sg);
			}
			return securityGroupList;
		}
		return null;
	}

	@Override
	public List<BaseCloudVmSgroup> getVmSgroupByVmIdAndSecurityGropId(String vmId, String securityGroupId) throws AppException {
		if(StringUtil.isEmpty(vmId) && StringUtil.isEmpty(securityGroupId))
			return null;
		String where = "cmsg.vm_id = ?";
		Object [] object = new Object[2];
		object[0] = vmId;
		int ind = 1;
		if(StringUtil.isEmpty(vmId)){
			where = "cmsg.sg_id = ?";
			object[0] = securityGroupId;
		}
		if(!StringUtil.isEmpty(vmId) && !StringUtil.isEmpty(securityGroupId)){
			where = "cmsg.vm_id = ? and cmsg.sg_id = ?";
			object[0] = vmId;
			object[1] = securityGroupId;
			ind++;
		}
		StringBuffer sql = new StringBuffer();
		sql.append(" select cmsg.sg_id,cmsg.vm_id ");
		sql.append(" from cloud_vmsecuritygroup cmsg LEFT JOIN cloud_vm cv on cmsg.vm_id = cv.vm_id");
		sql.append(" where " + where);
		sql.append(" and cv.is_deleted != '1' and cv.is_visable = '1' ");
		Object [] params = new Object[ind];
		System.arraycopy(object, 0, params, 0, ind);
		Query result = securityGroupRuleDao.createSQLNativeQuery(sql.toString(), params);
		List resultList = result.getResultList();
		if(resultList != null && resultList.size() > 0){
			List<BaseCloudVmSgroup> list = new ArrayList<BaseCloudVmSgroup>();
			for(int i = 0; i < resultList.size(); i++){
				Object [] obj = (Object[]) resultList.get(i);
				BaseCloudVmSgroup sg = new BaseCloudVmSgroup();
				sg.setSgId(String.valueOf(obj[0]));
				sg.setVmId(String.valueOf(obj[1]));
				list.add(sg);
			}
			return list;
		}
		return null;
	}

	@Override
	public List<CloudSecurityGroupRule> getGroupRuleListByGroupId(String securityGroupRuleId) throws AppException {

		List<CloudSecurityGroupRule> listRule = new ArrayList<CloudSecurityGroupRule>();
		List<Object> list = new ArrayList<Object>();

		StringBuffer sql = new StringBuffer();
		sql.append(" select gr.sgr_id as sgrId, ");
		sql.append(" gr.direction as direction, ");
		sql.append(" gr.ethertype as ethertype, ");
		sql.append(" gr.protocol as protocol, ");
		sql.append(" gr.port_rangemin as portRangeMin, ");
		sql.append(" gr.port_rangemax as portRangeMax, ");
		sql.append(" gr.remote_ipprefix as remoteIpPrefix, ");
		sql.append(" gr.protocol_expand as protocolExpand, ");
		sql.append(" gr.icmp as icmp,  ");
		sql.append(" gr.remote_groupid as remoteGroupid  ");
		sql.append(" from cloud_grouprule gr ");
		sql.append(" where 1=1 and gr.ethertype='IPv4' ");

		if (!StringUtil.isEmpty(securityGroupRuleId) && !"null".equals(securityGroupRuleId)) {
			sql.append(" and gr.sg_id = ? ");
			list.add(securityGroupRuleId);
		}

		sql.append(" order by gr.create_time desc ");

		javax.persistence.Query query = securityGroupRuleDao.createSQLNativeQuery(sql.toString(), list.toArray());
		List listResult = query.getResultList();
		if(!listResult.isEmpty() && listResult.size() > 0){
			for (int i = 0; i < listResult.size(); i++) {
				int ind = 0;
				Object[] objs = (Object[]) listResult.get(i);
				CloudSecurityGroupRule groupRule = new CloudSecurityGroupRule();
				groupRule.setSgrId(String.valueOf(objs[ind++]));
				groupRule.setDirection(null != objs[ind++]?String.valueOf(objs[ind-1]):"");
				groupRule.setEthertype(null != objs[ind++]?String.valueOf(objs[ind-1]):"");
				groupRule.setProtocol(null != objs[ind++]?String.valueOf(objs[ind-1]):"");
				groupRule.setPortRangeMin(null != objs[ind++]?String.valueOf(objs[ind-1]):"");
				groupRule.setPortRangeMax(null != objs[ind++]?String.valueOf(objs[ind-1]):"");
				groupRule.setRemoteIpPrefix(null != objs[ind++]?String.valueOf(objs[ind-1]):"");
				groupRule.setProtocolExpand(null != objs[ind++]?String.valueOf(objs[ind-1]):"");
				groupRule.setIcMp(null != objs[ind++]?String.valueOf(objs[ind-1]):"");
				groupRule.setRemoteGroupId(null != objs[ind++]?String.valueOf(objs[ind-1]):"");
				listRule.add(groupRule);
			}
			return listRule;
		}
		return null;
	}

	/**
	 * 查询项目下默认安全组类型的安全组<br>
	 * -------------------------------------
	 * 
	 * @author zhouhaitao
	 * @param prjId				项目ID
	 * @param defaultType		默认类型
	 * @return
	 */
	public String querySecurityGroupByDefaultAndPrjId(String prjId,String defaultType){
		String sgId = null ;
		StringBuffer sql = new StringBuffer();
		sql.append("				SELECT                        ");
		sql.append("					csg.sg_id,                ");
		sql.append("					csg.sg_name               ");
		sql.append("				FROM                          ");
		sql.append("					cloud_securitygroup csg   ");
		sql.append("				WHERE                         ");
		sql.append("					csg.prj_id = ?            ");
		sql.append("				AND csg.default_group = ?     ");
		sql.append("				AND csg.sg_name = ?           ");
		String sgName = ApiInstanceConstant.SECURITYGROUP_DEFAULT_NAME;
		if(ApiInstanceConstant.SECURITYGROUP_LINUX.equals(defaultType)){
			sgName = ApiInstanceConstant.SECURITYGROUP_LINUX_NAME;
		}
		else if(ApiInstanceConstant.SECURITYGROUP_WINDOWS.equals(defaultType)){
			sgName = ApiInstanceConstant.SECURITYGROUP_WINDOWS_NAME;
		}
		
		Query query = securityGroupDao.createSQLNativeQuery(sql.toString(), new Object []{prjId,"defaultGroup",sgName});
		@SuppressWarnings("rawtypes")
		List resultList = query.getResultList();
		if(resultList != null && resultList.size() == 1){
			Object [] objs = (Object []) resultList.get(0);
			sgId = String.valueOf(objs[0]);
		}
		return sgId;
	}
}
