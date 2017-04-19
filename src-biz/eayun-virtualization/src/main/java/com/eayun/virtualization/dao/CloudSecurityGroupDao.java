package com.eayun.virtualization.dao;



import java.util.List;
import java.util.Map;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.eayun.common.dao.IRepository;
import com.eayun.virtualization.model.BaseCloudProject;
import com.eayun.virtualization.model.BaseCloudSecurityGroup;
import com.eayun.virtualization.model.BaseCloudVmSgroup;

public interface CloudSecurityGroupDao extends IRepository<BaseCloudSecurityGroup, String> {
	
	 @Query("select count(*) from BaseCloudSecurityGroup t where t.prjId= ? ")
	 public int getCountByPrjId(String prjId);
	 
	 /**
	  * 根据项目ID查询安全组
	  * @author zengbo
	 * @param prjId
	 * @return
	 */
	@Query("from BaseCloudSecurityGroup where prjId = ?")
	 public List<BaseCloudSecurityGroup> findByPrjId(String prjId);
	
	@Query("select count(*) from BaseCloudSecurityGroup sg where 1=1"
			+ " and (:dcId = null or sg.dcId = :dcId)"
			+ " and (:prjId = null or sg.prjId = :prjId)"
			+ " and (:sgName = null or binary(sg.sgName) = :sgName)"
			+ "and (:sgId = null or :sgId = '' or sg.sgId <> :sgId)")
	public int countBySecurityGroupName(@Param("dcId") String dcId,@Param("prjId") String prjId,@Param("sgName") String sgName,@Param("sgId") String sgId);
	
	@Query("from BaseCloudSecurityGroup where 1=1"
			+ " and (:dcId = null or dcId = :dcId)"
			+ " and (:prjId = null or prjId = :prjId)"
			+ " order by createTime desc")
	public List<BaseCloudSecurityGroup> findByDcIdAndPrjIdDesc(@Param("dcId") String dcId,@Param("prjId") String prjId);
	
	@Query("select new map(sg.sgId as sgId,sg.sgName as sgName,sg.createName as createName,sg.dcId as dcId"
			+ " ,sg.prjId as prjId,sg.sgDescription as sgDescription,sg.defaultGroup as defaultGroup"
			+ " ,dc.name as dcName, cp.prjName as prjName,sg.createTime as createTime)"
			+ " from BaseCloudSecurityGroup sg, BaseDcDataCenter dc, BaseCloudProject cp"
			+ " where sg.dcId = dc.id and sg.prjId = cp.projectId and sg.sgId = ?")
	public Map<String, Object> findTop1BySgId(String sgId);
	@Query("from BaseCloudSecurityGroup where sgId = ?")
	public BaseCloudSecurityGroup getGroupBySgId(String sgId);
	
	
	@Query("from BaseCloudVmSgroup where   sgId=?")
	public List<BaseCloudVmSgroup> getVmBysgId(String sgid);
	
	@Query("from BaseCloudProject ")
	public List<BaseCloudProject> getListProject();
	@Query("from BaseCloudSecurityGroup where defaultGroup='defaultGroup'")
	public List<BaseCloudSecurityGroup> getsgList();
	@Modifying
	@Query("update BaseCloudSecurityGroup set sgDescription='出方向允许所有出站流量；入方向仅允许来自其他与默认安全组相关联的云主机的入站流量。' where sg_name='default' and default_group='defaultGroup'")
	public void updatesg_de();
}
