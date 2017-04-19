package com.eayun.database.configgroup.service.impl;

import com.eayun.common.dao.ParamsMap;
import com.eayun.common.dao.QueryMap;
import com.eayun.common.dao.support.Page;
import com.eayun.common.util.BeanUtils;
import com.eayun.common.util.StringUtil;
import com.eayun.database.configgroup.dao.RdsconfigfileDao;
import com.eayun.database.configgroup.model.configfile.CloudRdsconfigfile;
import com.eayun.database.configgroup.model.configfile.Rdsconfigfile;
import com.eayun.database.configgroup.service.RdsConfigurationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Administrator on 2017/3/22.
 */
@Service
@Transactional
public class RdsConfigurationServiceImpl extends BaseRdsConfigurationService implements RdsConfigurationService {

    @Autowired
    private RdsconfigfileDao rdsconfigfileDao ;
    private static Logger logger = LoggerFactory.getLogger(RdsConfigurationServiceImpl.class);

    /**
     * ECSC端展示配置文件信息
     * @param page
     * @param paramsMap
     * @return
     * @throws Exception
     */
    @SuppressWarnings("unchecked")

    public Page ecscListConfigFile(Page page, ParamsMap paramsMap) throws Exception {
        String projectId =       String.valueOf(paramsMap.getParams().get("projectId"));
        String dcId      =       String.valueOf(paramsMap.getParams().get("dcId"));
        String dbVersion =       String.valueOf(paramsMap.getParams().get("dbVersion"));
        String fileNameKeyword = String.valueOf(paramsMap.getParams().get("fileNameKeyword"));
        String fileType =        String.valueOf(paramsMap.getParams().get("fileType"));
        String searchType =      String.valueOf(paramsMap.getParams().get("searchType"));
        QueryMap queryMap = new QueryMap();
        int pageSize = paramsMap.getPageSize();
        int pageNumber = paramsMap.getPageNumber();
        queryMap.setPageNum(pageNumber);
        queryMap.setCURRENT_ROWS_SIZE(pageSize);
        StringBuilder hql = new StringBuilder();
        List<String> values = new ArrayList<String>();
        if (StringUtil.isEmpty(fileType) || "0".equals(fileType)) {
            hql.append(" from CloudRdsconfigfile c where (( c.configType = '1' and c.configDatacenterid = ? ) or ( 1=1 ");
            values.add(dcId);
            if (!StringUtil.isEmpty(projectId)) {
                hql.append(" and c.configProjectid = ? ");
                values.add(projectId);
            }
            hql.append(" and c.configType = '2' )) ");
        }else if ("1".equals(fileType)){
            hql.append(" from CloudRdsconfigfile c where (( c.configType = '1' and c.configDatacenterid = ? )) ");
            values.add(dcId);
        }else {
            hql.append(" from CloudRdsconfigfile c where (( 1=1 ");
            if (!StringUtil.isEmpty(projectId)) {
                hql.append(" and c.configProjectid = ? ");
                values.add(projectId);
            }
            hql.append(" and c.configType = '2' )) ");
        }
        if ((!StringUtil.isEmpty(dbVersion)) && (!"0".equals(dbVersion))) {
            hql.append(" and c.configVersion = ? ");
            values.add(dbVersion);
        }
        if (!StringUtil.isEmpty(fileNameKeyword)){
            if ("configFileNameKey".equals(searchType)){
                hql.append(" and ( c.configName like ? ) ");
                values.add("%" + fileNameKeyword + "%");
            }
            if ("configFileVersion".equals(searchType)){
                hql.append(" and ( c.configVersionname like ? ) ");
                values.add("%" + fileNameKeyword + "%");
            }
        }
        hql.append(" order by c.configType asc , c.configDate desc ") ;
        Page p = rdsconfigfileDao.pagedQuery(hql.toString(), queryMap, values.toArray()) ;
        List<CloudRdsconfigfile> rdsconfigfiles = (List<CloudRdsconfigfile>) p.getResult();
        for (int i = 0 ; i < rdsconfigfiles.size() ; i++){
            CloudRdsconfigfile cloudRdsconfigfile = rdsconfigfiles.get(i) ;
            Rdsconfigfile rdsconfigfile = new Rdsconfigfile() ;
            try {
                BeanUtils.copyProperties(rdsconfigfile, cloudRdsconfigfile);
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
            }
            if ("2".equals(cloudRdsconfigfile.getConfigType())) {
                rdsconfigfile.setApplyInstances(queryInstanceNamesByConfig(cloudRdsconfigfile.getConfigId()));
            }else {
                rdsconfigfile.setApplyInstances(queryInstanceNamesByDefaultConfig(cloudRdsconfigfile.getConfigVersion(), projectId));
            }
            rdsconfigfiles.set(i, rdsconfigfile) ;
        }
        return p ;
    }
}