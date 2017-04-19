package com.eayun.project.service.impl;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.Query;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.eayun.common.dao.QueryMap;
import com.eayun.common.dao.support.Page;
import com.eayun.common.util.BeanUtils;
import com.eayun.datacenter.model.BaseDcDataCenter;
import com.eayun.datacenter.model.DcDataCenter;
import com.eayun.datacenter.service.DataCenterService;
import com.eayun.project.dao.CloudProjectDao;
import com.eayun.project.service.ProjectService;
import com.eayun.virtualization.model.BaseCloudProject;
import com.eayun.virtualization.model.CloudProject;

/**
 * ProjectServiceImpl
 *                       
 * @Filename: ProjectServiceImpl.java
 * @Description: 
 * @Version: 1.0
 * @Author: chenhao
 * @Email: hao.chen@eayun.com
 * @History:<br>
 *<li>Date: 2015年9月1日</li>
 *<li>Version: 1.0</li>
 *<li>Content: create</li>
 *
 */
@Service
@Transactional
public class ProjectServiceImpl implements ProjectService {
    
    private static final Logger   log = LoggerFactory.getLogger(ProjectServiceImpl.class);
    
    @Autowired
    private CloudProjectDao cloudProjectDao;
    @Autowired
    private DataCenterService dataCenterService;
    @Override
    public List<CloudProject> getProjectListByDataCenter(String dcId) {
        List<BaseCloudProject> list = cloudProjectDao.getListByDataCenter(dcId);

        List<CloudProject> result = new ArrayList<CloudProject>();
        for (BaseCloudProject baseCloudProject : list) {
            CloudProject cloudProject = new CloudProject();
            BeanUtils.copyPropertiesByModel(cloudProject, baseCloudProject);

            result.add(cloudProject);
        }
        return result;
    }

    @Override
    public List<CloudProject> getProjectListByCustomer(String cusId) {
        log.info("根据所属客户查询项目列表");
        List<BaseCloudProject> baseCloudList = cloudProjectDao.getProjectListByCustomer(cusId);
        List<CloudProject> cloudProjectlList = new ArrayList<CloudProject>();
        
        List<DcDataCenter> dcList =  dataCenterService.getAllList();
        
        for(BaseCloudProject baseCloud : baseCloudList){
            CloudProject cloudProject = new CloudProject();
            BeanUtils.copyPropertiesByModel(cloudProject, baseCloud);
            for(DcDataCenter dc :dcList){
            	if(baseCloud.getDcId().equals(dc.getId())){
            		cloudProject.setDcName(dc.getName());
            		break;
            	}
            }
            cloudProjectlList.add(cloudProject);
        }
        return cloudProjectlList;
    }
    @Override
    //根据客户得到项目列表,将当前的项目放到首位。
    public List<BaseCloudProject> getListByCustomerAndPrjId(String cusId,String prjId) {
        log.info("根据所属客户查询项目列表");
        
        List<BaseCloudProject> baseProjectList= new ArrayList<BaseCloudProject>();
        /*开始*/
        List<BaseCloudProject> firstProjectList = cloudProjectDao.getProjectListByCusIdAndprjIdFirst(cusId, prjId);
        List<BaseCloudProject> secondProjectList = cloudProjectDao.getProjectListByCusIdAndprjIdSecond(cusId, prjId);
        baseProjectList.add(firstProjectList.get(0));
        for(BaseCloudProject base :secondProjectList){
        	baseProjectList.add(base);
        }
       
        return baseProjectList;
    }


    @Override
    public CloudProject findProject(String projectId) {
        BaseCloudProject baseCloudProject = cloudProjectDao.findOne(projectId);
        if(baseCloudProject != null){
            CloudProject cloudProject = new CloudProject();
            BeanUtils.copyPropertiesByModel(cloudProject, baseCloudProject);
            BaseDcDataCenter baseDc =dataCenterService.getById(cloudProject.getDcId());
            cloudProject.setDcName(baseDc.getName());
            return cloudProject;
        }
        
        return null;
    }

    @Override
    public List<CloudProject> getAllProjects() {
        List<BaseCloudProject> list = cloudProjectDao.getAllList();
        List<CloudProject> result = new ArrayList<CloudProject>();
        for (BaseCloudProject baseCloudProject : list) {
            CloudProject cloudProject = new CloudProject();
            BeanUtils.copyPropertiesByModel(cloudProject, baseCloudProject);

            result.add(cloudProject);
        }
        return result;
    }

    @Override
    public boolean findProByDcId(String cusId, String dcId) {
        int count = cloudProjectDao.findProByDcId(cusId, dcId);
        if(count > 0){
            return true;
        }else{
            return false; 
        }
    }

    @Override
    public List<CloudProject> getProjectListByUser(String cusId , String userId) {
        List<BaseCloudProject> baselist = cloudProjectDao.getAllList();
        StringBuffer hql = new StringBuffer("select bc from BaseCloudProject bc , BaseUserPrj bu where bc.projectId = bu.projectId "
                + "and bc.customerId = ? and bu.userId =?");
        List<String> list = new ArrayList<String>();
        list.add(cusId);
        list.add(userId);
        baselist = cloudProjectDao.find(hql.toString(), list.toArray());
        List<CloudProject> projectList = new ArrayList<CloudProject>();
        for(BaseCloudProject baseProject : baselist){
            CloudProject cloudProject = new CloudProject();
            BeanUtils.copyPropertiesByModel(cloudProject, baseProject);
            projectList.add(cloudProject);
        }
        return projectList;
    }

    @Override
    public List<String> getProNameListByUser(boolean isAdmin , String cusId , String userId) {
        List<String> proNameList = new ArrayList<String>();
        if(isAdmin){
            /*proNameList = cloudProjectDao.getProNameListByCusId(cusId);*/
        	StringBuffer hql = new StringBuffer("select dc.name from BaseCloudProject bc , BaseDcDataCenter dc where bc.dcId = dc.id and bc.customerId = ?");
            List<String> paramlist = new ArrayList<String>();
            paramlist.add(cusId);
            proNameList = cloudProjectDao.find(hql.toString(), paramlist.toArray());
        }else{
            StringBuffer hql = new StringBuffer("select dc.name from BaseCloudProject bc , BaseUserPrj bu , BaseDcDataCenter dc where bc.projectId = bu.projectId "
                    + "and bc.dcId = dc.id and bc.customerId = ? and bu.userId =?");
            List<String> paramlist = new ArrayList<String>();
            paramlist.add(cusId);
            paramlist.add(userId);
            proNameList = cloudProjectDao.find(hql.toString(), paramlist.toArray());
        }
        return proNameList;
    }

	@Override
	public void save(BaseCloudProject project) {
		cloudProjectDao.save(project);
	}

	@Override
	public void delete(String id) {
		cloudProjectDao.delete(id);
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<BaseCloudProject> find(String hql, Object[] args) {
		return cloudProjectDao.find(hql, args);
	}

	@Override
	public void execSQL(String sql, Object[] args) {
		cloudProjectDao.execSQL(sql, args);
	}

    @Override
    public List<CloudProject> getListByCusAndPrj(String cusId, String prjId) {
        List<BaseCloudProject> baselist = cloudProjectDao.getProjectListByCusIdAndprjIdFirst(cusId,prjId);
        List<CloudProject> projectList = new ArrayList<CloudProject>();
        for(BaseCloudProject basepro : baselist){
            CloudProject pro = new CloudProject();
            BeanUtils.copyPropertiesByModel(pro, basepro);
            projectList.add(pro);
        }
        return projectList;
    }

    @Override
    public List<CloudProject> getListByPrj(String prjId) {
        StringBuffer sql = new StringBuffer();
        sql.append("from BaseCloudProject as r where 1 = 1");
        List<Object> listParams = new ArrayList<Object>();
        if (!"null".equals(prjId) && null != prjId&& !"".equals(prjId)&& !"undefined".equals(prjId)) {
            sql.append(" and r.projectId = ? ");
            listParams.add(prjId);
        }
         List<BaseCloudProject> list =  cloudProjectDao.find(sql.toString(), listParams.toArray());
         List<CloudProject> projectList = new ArrayList<CloudProject>();
         for(BaseCloudProject basepro : list){
             CloudProject pro = new CloudProject();
             BeanUtils.copyPropertiesByModel(pro, basepro);
             projectList.add(pro);
         }
        return projectList;
    }

	public Query createSQLNativeQuery(String sql , Object [] args){
		return cloudProjectDao.createSQLNativeQuery(sql, args);
	}
	
	public Page pagedNativeQuery(String sql ,QueryMap queryMap,Object [] args){
		return cloudProjectDao.pagedNativeQuery(sql, queryMap, args);
	}
	
	/**
	 * 根据数据中心Id和客户ID查询项目<br>
	 * -------------------------
	 * @author zhouhaitao
	 * @param dcId 			数据中心ID
	 * @param cusId			客户ID
	 * 
	 * @return
	 */
	public CloudProject queryProjectByDcAndCus(String dcId,String cusId){
		List<BaseCloudProject> projectList = cloudProjectDao.getProjectByDcIdAndCusId(dcId, cusId);
		if(null != projectList && projectList.size() >0){
			BaseCloudProject baseProject = projectList.get(0);
			CloudProject project = new CloudProject();
            BeanUtils.copyPropertiesByModel(project, baseProject);
            return project;
		}
		return null;
	}
	
	/**
	 * 查询项目下的配额信息和已使用配额信息（仅限云主机使用涉及的配额信息及订单中的资源）<br>
	 * ---------------------------------
	 * @author zhouhaitao
	 * 
	 * @param prjId
	 * @return
	 */
	public CloudProject queryProjectQuotaAndUsedQuotaForVm(String prjId){
		CloudProject project = new CloudProject();
		StringBuffer sql = new StringBuffer();

		sql.append("			SELECT                                                          	   		");
		sql.append("				cp.prj_id,                                                    	   		");
		sql.append("				cp.host_count,                                                	   		");
		sql.append("				cp.cpu_count,                                                 	   		");
		sql.append("				cp.memory,                                                    	   		");
		sql.append("				cp.disk_count,                                                	   		");
		sql.append("				cp.disk_capacity,                                             	   		");
		sql.append("				cp.outerip,                                                   	   		");
		sql.append("				vm.usedHostCount,                                             	   		");
		sql.append("				vm.usedCpu,                                                   	   		");
		sql.append("				vm.usedRam,                                                   	   		");
		sql.append("				vol.usedVolumeCount as usedVolumeCount1,                        		");
		sql.append("				vol.usedVolumeCapacity as usedVolumeCapacity1,                 	   		");
		sql.append("				floatip.usedFloatipCount as usedFloatipCount1,                       	");
		sql.append("				ordervm.usedHostCount as usedHostCount1,                            	");
		sql.append("				ordervm.usedCpu as usedCpu2,                                     		");
		sql.append("				ordervm.usedRam as usedRam2,                                   	   		");
		sql.append("				ordervm.usedDisk as usedDisk2,                                   	   	");
		sql.append("				ordervol.usedVolumeCount as usedVolumeCount2,                        	");
		sql.append("				ordervol.usedVolumeCapacity as usedVolumeCapacity,               		");
		sql.append("				orderfloatip.usedFloatipCount as     usedFloatipCount                	");
		sql.append("			FROM                                                            	   		");
		sql.append("				cloud_project cp                                              	   		");
		sql.append("			LEFT JOIN (                                                     	   		");
		sql.append("				SELECT                                                        	   		");
		sql.append("					vm.prj_id,                                                  	  	");
		sql.append("					count(1) AS usedHostCount,                                   		");
		sql.append("					sum(flavor.flavor_vcpus) AS usedCpu,                       	   		");
		sql.append("					sum(flavor.flavor_ram) AS usedRam                          	   		");
		sql.append("				FROM                                                          	   		");
		sql.append("					cloud_vm vm                                                         ");
		sql.append("				LEFT JOIN cloud_flavor flavor ON flavor.flavor_id = vm.flavor_id 		");
		sql.append("				AND flavor.dc_id = vm.dc_id                                   	   		");
		sql.append("				WHERE                                                         	   		");
		sql.append("					vm.prj_id = ?                                               	  	");
		sql.append("				AND vm.is_deleted = '0'                                       	   		");
		sql.append("				AND vm.is_visable = '1'                                       	   		");
		sql.append("			) vm ON cp.prj_id = vm.prj_id                                   	   		");
		sql.append("			LEFT JOIN (                                                     	   		");
		sql.append("				SELECT                                                        	   		");
		sql.append("					ordervm.prj_id,                                             		");
		sql.append("					sum(ordervm.count) AS usedHostCount,                     	   		");
		sql.append("					sum(ordervm.cpu) AS usedCpu,                                        ");
		sql.append("					sum(ordervm.ram) AS usedRam,                    		            ");
		sql.append("					sum(ordervm.disk) AS usedDisk                  	   	                ");
		sql.append("				FROM	(                                                      	   		");
		sql.append("			SELECT                                                                      ");
		sql.append("				cov.prj_id,                                                             ");
		sql.append("				cov.order_no,                                                           ");
		sql.append("				cov.order_type,                                                         ");
		sql.append("				CASE cov.order_type                                                     ");
		sql.append("			WHEN '2' THEN                                                               ");
		sql.append("				0                                                                       ");
		sql.append("			ELSE                                                                        ");
		sql.append("				cov.count                                                               ");
		sql.append("			END AS count,                                                               ");
		sql.append("				CASE cov.order_type                                                     ");
		sql.append("			WHEN '2' THEN                                                               ");
		sql.append("				(cov.cpu - cf.flavor_vcpus)                                             ");
		sql.append("			ELSE                                                                        ");
		sql.append("				cov.count*cov.cpu                                                       ");
		sql.append("			END AS cpu,                                                                 ");
		sql.append("			 CASE cov.order_type                                                        ");
		sql.append("			WHEN '2' THEN                                                               ");
		sql.append("				(cov.ram - cf.flavor_ram)                                               ");
		sql.append("			ELSE                                                                        ");
		sql.append("				cov.count*cov.ram                                                       ");
		sql.append("			END AS ram,                                                                 ");
		sql.append("			 CASE cov.order_type                                                        ");
		sql.append("			WHEN '2' THEN                                                               ");
		sql.append("				0                                                                       ");
		sql.append("			ELSE                                                                        ");
		sql.append("				cov.count*cov.disk                                                      ");
		sql.append("			END AS disk                                                                 ");
		sql.append("			FROM                                                                        ");
		sql.append("				cloudorder_vm cov                                                       ");
		sql.append("			LEFT JOIN cloud_vm vm ON cov.vm_id = vm.vm_id                               ");
		sql.append("			LEFT JOIN cloud_flavor cf ON vm.flavor_id = cf.flavor_id                    ");
		sql.append("					)ordervm                                     	   		            ");
		sql.append("				LEFT JOIN order_info info ON info.order_no = ordervm.order_no 	   		");
		sql.append("				WHERE                                                       	   		");
		sql.append("					ordervm.prj_id = ?                                         	   		");
		sql.append("				AND (                                                         	   		");
		sql.append("					info.order_state = '1'                                        		");
		sql.append("					OR info.order_state = '2'                                   		");
		sql.append("				)                                                             	   		");
		sql.append("				AND (                                                         	   		");
		sql.append("					ordervm.order_type = '0'                                       		");
		sql.append("					OR ordervm.order_type = '2'                                   		");
		sql.append("				)                                                             	   		");
		sql.append("			) ordervm ON ordervm.prj_id = cp.prj_id                         	   		");
		sql.append("			LEFT JOIN (                                                     	   		");
		sql.append("				SELECT                                                        	   		");
		sql.append("					cfl.prj_id,                                                 	  	");
		sql.append("					count(1) usedFloatipCount                                   	  	");
		sql.append("				FROM                                                          	   		");
		sql.append("					cloud_floatip cfl                                           	  	");
		sql.append("				WHERE                                                         	   		");
		sql.append("					cfl.is_visable = '1'                                         		");
		sql.append("				AND cfl.is_deleted = '0'                                      	   		");
		sql.append("				AND cfl.prj_id = ?                                           	   		");
		sql.append("			) floatip ON floatip.prj_id = cp.prj_id                         	   		");
		sql.append("			LEFT JOIN (                                                     	   		");
		sql.append("				SELECT                                                        	   		");
		sql.append("					cof.prj_id,                                                 	  	");
		sql.append("					sum(cof.product_count) AS usedFloatipCount                  	   	");
		sql.append("				FROM                                                          	   		");
		sql.append("					cloudorder_floatip cof                                      	   	");
		sql.append("				LEFT JOIN order_info info ON info.order_no = cof.order_no     	   		");
		sql.append("				WHERE                                                         	   		");
		sql.append("					cof.prj_id = ?                                               	   	");
		sql.append("				AND (                                                         	   		");
		sql.append("					info.order_state = '1'                                      	   	");
		sql.append("					OR info.order_state = '2'                                   	   	");
		sql.append("				)                                                             	   		");
		sql.append("				AND (                                                         	   		");
		sql.append("					cof.order_type = '0'                                        		");
		sql.append("					OR cof.order_type = '2'                                     		");
		sql.append("				)                                                             	   		");
		sql.append("			) orderfloatip ON orderfloatip.prj_id = cp.prj_id               	   		");
		sql.append("			LEFT JOIN (                                                     	   		");
		sql.append("				SELECT                                                        	   		");
		sql.append("					cvol.prj_id,                                                	   	");
		sql.append("					count(1) AS usedVolumeCount,                                	   	");
		sql.append("					sum(cvol.vol_size) AS usedVolumeCapacity                    	   	");
		sql.append("				FROM                                                          	   		");
		sql.append("					cloud_volume cvol                                           	   	");
		sql.append("				WHERE                                                         	   		");
		sql.append("					cvol.is_visable = '1'                                       	   	");
		sql.append("				AND (cvol.is_deleted = '0' or cvol.is_deleted = '2')                    ");
		sql.append("				AND cvol.prj_id = ?                                           	   		");
		sql.append("			) vol ON vol.prj_id = cp.prj_id                                 	   		");
		sql.append("			LEFT JOIN (                                                     	   		");
		sql.append("				SELECT                                                        	   		");
		sql.append("					ordervol.prj_id,                                            	  	");
		sql.append("					sum(ordervol.vol_number) AS usedVolumeCount,                 		");
		sql.append("					sum(ordervol.vol_size) AS usedVolumeCapacity    ");
		sql.append("				FROM  (                                                        	   		");
		sql.append("				SELECT                                               					");
		sql.append("					clov.prj_id,                                       					");
		sql.append("					clov.order_type,                                   					");
		sql.append("					clov.order_no,                                     					");
		sql.append("					CASE clov.order_type                               					");
		sql.append("					WHEN 2 THEN 0                                     					");
		sql.append("					ELSE clov.vol_number                           						");
		sql.append("					END AS vol_number,                             						");
		sql.append("					CASE clov.order_type                           						");
		sql.append("					WHEN 2 THEN clov.vol_size - cv.vol_size            					");
		sql.append("					ELSE clov.vol_number * clov.vol_size               					");
		sql.append("					END AS vol_size                                    					");
		sql.append("				FROM cloudorder_volume clov                          					");
		sql.append("				LEFT JOIN cloud_volume cv ON cv.vol_id = clov.vol_id 					");
		sql.append("					  ) as ordervol                                                		");
		sql.append("				LEFT JOIN order_info info ON info.order_no = ordervol.order_no	   		");
		sql.append("				WHERE                                                         	   		");
		sql.append("					ordervol.prj_id = ?                                      	   		");
		sql.append("				AND (                                                         	   		");
		sql.append("					info.order_state = '1'                                     	   		");
		sql.append("					OR info.order_state = '2'                                  	   		");
		sql.append("				)                                                             	   		");
		sql.append("				AND (                                                         	   		");
		sql.append("					ordervol.order_type = '0'                                        	");
		sql.append("					OR ordervol.order_type = '2'                                     	");
		sql.append("				)                                                             	   		");
		sql.append("			) ordervol ON ordervol.prj_id = cp.prj_id                       	   		");
		sql.append("			WHERE                                                           	   		");
		sql.append("				cp.prj_id = ?                                               	   		");

		javax.persistence.Query query = cloudProjectDao.createSQLNativeQuery(sql.toString(),
				new Object[] { prjId, prjId, prjId, prjId, prjId, prjId, prjId });

		@SuppressWarnings("rawtypes")
		List result = query.getResultList();
		if (result != null && result.size() == 1) {
			int index = 0;
			Object[] objs = (Object[]) result.get(0);

			project.setProjectId(String.valueOf(objs[index++]));
			project.setHostCount(Integer.parseInt(objs[index++] != null ? String.valueOf(objs[index - 1]) : "0"));
			project.setCpuCount(Integer.parseInt(objs[index++] != null ? String.valueOf(objs[index - 1]) : "0"));
			project.setMemory(1024*Integer.parseInt(objs[index++] != null ? String.valueOf(objs[index - 1]) : "0"));
			project.setDiskCount(Integer.parseInt(objs[index++] != null ? String.valueOf(objs[index - 1]) : "0"));
			project.setDiskCapacity(Integer.parseInt(objs[index++] != null ? String.valueOf(objs[index - 1]) : "0"));
			project.setOuterIP(Integer.parseInt(objs[index++] != null ? String.valueOf(objs[index - 1]) : "0"));
			project.setUsedVmCount(Integer.parseInt(objs[index++] != null ? String.valueOf(objs[index - 1]) : "0"));
			project.setUsedCpuCount(Integer.parseInt(objs[index++] != null ? String.valueOf(objs[index - 1]) : "0"));
			project.setUsedRam(Integer.parseInt(objs[index++] != null ? String.valueOf(objs[index - 1]) : "0"));
			project.setDiskCountUse(Integer.parseInt(objs[index++] != null ? String.valueOf(objs[index - 1]) : "0"));
			project.setUsedDiskCapacity(
					Integer.parseInt(objs[index++] != null ? String.valueOf(objs[index - 1]) : "0"));
			project.setOuterIPUse(Integer.parseInt(objs[index++] != null ? String.valueOf(objs[index - 1]) : "0"));
			int orderHostCount = Integer.parseInt(objs[index++] != null ? String.valueOf(objs[index - 1]) : "0");
			int orderCpuCount = Integer.parseInt(objs[index++] != null ? String.valueOf(objs[index - 1]) : "0");
			int orderRamCount = Integer.parseInt(objs[index++] != null ? String.valueOf(objs[index - 1]) : "0");
			int orderDiskCount = Integer.parseInt(objs[index++] != null ? String.valueOf(objs[index - 1]) : "0");
			int orderVolumeCount = Integer.parseInt(objs[index++] != null ? String.valueOf(objs[index - 1]) : "0") +orderHostCount;
			int orderVolCapacityCount = Integer.parseInt(objs[index++] != null ? String.valueOf(objs[index - 1]) : "0") +orderDiskCount;
			int orderFloatIpCount = Integer.parseInt(objs[index++] != null ? String.valueOf(objs[index - 1]) : "0");

			project.setUsedVmCount(project.getUsedVmCount() + orderHostCount);
			project.setUsedCpuCount(project.getUsedCpuCount() + orderCpuCount);
			project.setUsedRam(project.getUsedRam() + orderRamCount);
			project.setDiskCountUse(project.getDiskCountUse() + orderVolumeCount);
			project.setUsedDiskCapacity(project.getUsedDiskCapacity() + orderVolCapacityCount);
			project.setOuterIPUse(project.getOuterIPUse() + orderFloatIpCount);
		}

		return project;
	}
}
