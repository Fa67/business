package com.eayun.database.configgroup.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.eayun.common.dao.ParamsMap;
import com.eayun.common.dao.QueryMap;
import com.eayun.common.dao.support.Page;
import com.eayun.common.util.BeanUtils;
import com.eayun.common.util.StringUtil;
import com.eayun.customer.ecmcservice.EcmcCustomerService;
import com.eayun.database.configgroup.dao.RdsconfigfileDao;
import com.eayun.database.configgroup.dao.RdsconfigparamsDao;
import com.eayun.database.configgroup.model.configfile.CloudRdsconfigfile;
import com.eayun.database.configgroup.model.configfile.CloudRdsconfigparams;
import com.eayun.database.configgroup.model.configfile.Rdsconfigfile;
import com.eayun.database.configgroup.model.configgroup.ConfigurationGroup;
import com.eayun.database.configgroup.service.EcmcRdsConfigurationService;
import com.eayun.datacenter.service.DataCenterService;
import com.eayun.eayunstack.service.OpenstackConfigurationGroupService;
import com.eayun.project.ecmcservice.EcmcProjectService;
import com.eayun.virtualization.model.BaseCloudProject;
import com.eayun.virtualization.model.CloudProject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.*;

/**
 * Created by Administrator on 2017/3/22.
 */
@Service
@Transactional
public class EcmcRdsConfigurationServiceImpl extends BaseRdsConfigurationService implements EcmcRdsConfigurationService {

    @Autowired
    private RdsconfigfileDao rdsconfigfileDao ;
    @Autowired
    private DataCenterService dataCenterService ;
    @Autowired
    private OpenstackConfigurationGroupService groupService ;
    @Autowired
    private RdsconfigparamsDao rdsconfigparamsDao ;
    @Autowired
    private EcmcProjectService ecmcProjectService ;
    @Autowired
    private EcmcCustomerService ecmcCustomerService ;
    private static final String CONFIGURATION_TAG = "configuration" ;
    private static Logger logger = LoggerFactory.getLogger(EcmcRdsConfigurationServiceImpl.class);

    /**
     * ECMC端展示默认配置文件
     * @param page
     * @param paramsMap
     * @return
     * @throws Exception
     */
    @SuppressWarnings("unchecked")

    public Page ecmcListDefaultConfigFile(Page page, ParamsMap paramsMap) throws Exception {
        String dcId =            String.valueOf(paramsMap.getParams().get("dcId"));
        String dcVersion =       String.valueOf(paramsMap.getParams().get("dcVersion"));
        String fileNameKeyword = String.valueOf(paramsMap.getParams().get("fileNameKeyword"));
        String searchType =      String.valueOf(paramsMap.getParams().get("searchType"));
        QueryMap queryMap = new QueryMap();
        int pageSize = paramsMap.getPageSize();
        int pageNumber = paramsMap.getPageNumber();
        queryMap.setPageNum(pageNumber);
        queryMap.setCURRENT_ROWS_SIZE(pageSize);
        StringBuilder hql = new StringBuilder();
        hql.append(" from CloudRdsconfigfile c where c.configType = '1' ");
        List<String> values = new ArrayList<String>();
        if (!StringUtil.isEmpty(dcId)) {
            hql.append(" and c.configDatacenterid = ? ");
            values.add(dcId);
        }
        if (!StringUtil.isEmpty(dcVersion)) {
            hql.append(" and c.configVersion = ? ");
            values.add(dcVersion);
        }
        if (!StringUtil.isEmpty(fileNameKeyword)) {
            if ("configName".equals(searchType)) {
                hql.append(" and ( c.configName like ? ) ");
                values.add("%" + fileNameKeyword + "%");
            }
            if ("configFileVersion".equals(searchType)){
                hql.append(" and ( c.configVersionname like ? ) ");
                values.add("%" + fileNameKeyword + "%");
            }
        }
        hql.append(" order by c.configDate desc ") ;
        Page p = rdsconfigfileDao.pagedQuery(hql.toString(), queryMap, values.toArray()) ;
        List<CloudRdsconfigfile> rdsconfigfiles = (List<CloudRdsconfigfile>) p.getResult();
        //缓存数据中心名称信息
        Map<String, String> dcName = new HashMap<>() ;
        for (int i = 0 ; i < rdsconfigfiles.size() ; i++){
            CloudRdsconfigfile cloudRdsconfigfile = rdsconfigfiles.get(i) ;
            Rdsconfigfile rdsconfigfile = new Rdsconfigfile() ;
            try {
                BeanUtils.copyProperties(rdsconfigfile, cloudRdsconfigfile);
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
            }
            if (dcName.get(cloudRdsconfigfile.getConfigDatacenterid()) == null){
                dcName.put(cloudRdsconfigfile.getConfigDatacenterid(), dataCenterService.getById(cloudRdsconfigfile.getConfigDatacenterid()).getName()) ;
            }
            rdsconfigfile.setDcName(dcName.get(cloudRdsconfigfile.getConfigDatacenterid()));
            rdsconfigfiles.set(i, rdsconfigfile) ;
        }
        return p ;
    }

    /**
     * 更新默认配置文件
     * @param configGroupId
     * @param editParams
     * @throws Exception
     */

    public void updateDefaultConfigFile(String configGroupId, String editParams) throws Exception {
        CloudRdsconfigfile cloudRdsconfigfile = rdsconfigfileDao.findOne(configGroupId);
        ConfigurationGroup group = JSONObject.parseObject(groupService.createConfigurationGroup(cloudRdsconfigfile.getConfigDatacenterid(), dataCenterService.getById(cloudRdsconfigfile.getConfigDatacenterid()).getOsAdminProjectId(), getConfigurationGroupDatastoreByVersion(cloudRdsconfigfile.getConfigVersion()), cloudRdsconfigfile.getConfigName(), changeStoreParamValueToOpenStackConfigurationValue(editParams)).getJSONObject(CONFIGURATION_TAG).toJSONString(), ConfigurationGroup.class);
        //将当前的默认配置文件修改为“历史版本的数据库默认配置文件”
        int row = rdsconfigfileDao.updateDefaultConfigFileState(configGroupId);
        CloudRdsconfigfile cloudRdsconfigfile1 = new CloudRdsconfigfile();
        try {
            BeanUtils.copyProperties(cloudRdsconfigfile1, cloudRdsconfigfile);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
        cloudRdsconfigfile1.setConfigId(group.getId());
        cloudRdsconfigfile1.setConfigType("1");
        //修改默认配置文件内容的时候不对应修改其的配置文件创建时间
//        cloudRdsconfigfile1.setConfigDate(new Date());
        rdsconfigfileDao.save(cloudRdsconfigfile1);
        CloudRdsconfigparams cloudRdsconfigparams = new CloudRdsconfigparams() ;
        cloudRdsconfigparams.setConfigId(group.getId());
        cloudRdsconfigparams.setConfigContent(editParams);
        rdsconfigparamsDao.save(cloudRdsconfigparams);
    }

    /**
     * ECMC端展示客户配置文件信息
     * @param page
     * @param paramsMap
     * @return
     * @throws Exception
     */
    @SuppressWarnings("unchecked")

    public Page ecmcListCusConfigFile(Page page, ParamsMap paramsMap) throws Exception {
        //数据中心
        String dcId =            String.valueOf(paramsMap.getParams().get("dcId"));
        //数据库版本信息
        String dcVersion =       String.valueOf(paramsMap.getParams().get("dcVersion"));
        //查询内容关键字
        String fileNameKeyword = String.valueOf(paramsMap.getParams().get("fileNameKeyword"));
        //关键字查询类型
        String searchType =      String.valueOf(paramsMap.getParams().get("searchType"));//查询关键字的数据类别类型
        QueryMap queryMap = new QueryMap();
        int pageSize = paramsMap.getPageSize();
        int pageNumber = paramsMap.getPageNumber();
        queryMap.setPageNum(pageNumber);
        queryMap.setCURRENT_ROWS_SIZE(pageSize);
        StringBuilder hql = new StringBuilder();
        hql.append( " select f " +
                " from CloudRdsconfigfile as f, BaseCloudProject as p, BaseCustomer as c " +
                " where f.configType = '2' " +
                " and f.configProjectid = p.projectId " +
                " and p.customerId = c.cusId " );
        List<String> values = new ArrayList<String>();
        if (!StringUtil.isEmpty(dcId)) {
            hql.append(" and f.configDatacenterid = ? ");
            values.add(dcId);
        }
        if (!StringUtil.isEmpty(dcVersion)) {
            hql.append(" and f.configVersion = ? ");
            values.add(dcVersion);
        }
        if (!StringUtil.isEmpty(fileNameKeyword)) {
            if ("configName".equals(searchType)) {
                hql.append(" and ( f.configName like ? ) ");
                values.add("%" + fileNameKeyword + "%");
            }
            if ("configFileVersion".equals(searchType)) {
                hql.append(" and ( f.configVersionname like ? ) ");
                values.add("%" + fileNameKeyword + "%");
            }
            if ("project".equals(searchType)) {
                hql.append(" and ( p.prjName like ? ) ");
                values.add("%" + fileNameKeyword + "%");
            }
            if ("cus".equals(searchType)) {
                hql.append(" and ( c.cusOrg like ? ) ");
                values.add("%" + fileNameKeyword + "%");
            }
        }
        hql.append(" order by f.configDate desc ") ;
        Page p = rdsconfigfileDao.pagedQuery(hql.toString(), queryMap, values.toArray()) ;
        List<CloudRdsconfigfile> rdsconfigfiles = (List<CloudRdsconfigfile>) p.getResult() ;
        //数据中心名称缓存
        Map<String, String> dcName = new HashMap<>() ;
        //项目名称缓存
        Map<String, String> projectName = new HashMap<>() ;
        //客户名称缓存
        Map<String, String> cusName = new HashMap<>() ;
        for (int i = 0 ; i < rdsconfigfiles.size() ; i++){
            //定义配置文件对象存储信息
            CloudRdsconfigfile cloudRdsconfigfile = rdsconfigfiles.get(i) ;
            Rdsconfigfile rdsconfigfile = new Rdsconfigfile() ;
            try {
                BeanUtils.copyProperties(rdsconfigfile, cloudRdsconfigfile);
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
            }
            rdsconfigfile.setApplyInstances(queryInstanceNamesByConfig(cloudRdsconfigfile.getConfigId()));
            try {
                if (dcName.get(cloudRdsconfigfile.getConfigDatacenterid()) == null){
                    dcName.put(cloudRdsconfigfile.getConfigDatacenterid(), dataCenterService.getById(cloudRdsconfigfile.getConfigDatacenterid()).getName()) ;
                }
                rdsconfigfile.setDcName(dcName.get(cloudRdsconfigfile.getConfigDatacenterid()));
                CloudProject project = ecmcProjectService.getProjectById(cloudRdsconfigfile.getConfigProjectid()) ;
                if (projectName.get(cloudRdsconfigfile.getConfigProjectid()) == null){
                    projectName.put(cloudRdsconfigfile.getConfigProjectid(), project.getPrjName()) ;
                }
                rdsconfigfile.setProjectName(projectName.get(cloudRdsconfigfile.getConfigProjectid()));
                if (cusName.get(project.getCustomerId()) == null){
                    cusName.put(project.getCustomerId(), ecmcCustomerService.getCustomerById(project.getCustomerId()).getCusOrg()) ;
                }
                rdsconfigfile.setCusName(cusName.get(project.getCustomerId()));
            }catch (Exception e){
                logger.error(e.getMessage(), e);
            }
            rdsconfigfiles.set(i, rdsconfigfile) ;
        }
        return p ;
    }

    public List<BaseCloudProject> queryProjectInformationsByDatacenter(String dcId) throws Exception {
        return ecmcProjectService.getByDataCenterId(dcId);
    }

}
