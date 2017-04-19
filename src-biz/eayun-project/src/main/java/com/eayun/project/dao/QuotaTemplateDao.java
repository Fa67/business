package com.eayun.project.dao;

import org.springframework.data.jpa.repository.Query;
import com.eayun.common.dao.IRepository;
import com.eayun.virtualization.model.BaseQuotaTemplate;

public interface QuotaTemplateDao extends IRepository<BaseQuotaTemplate, String> {
    
    /**
     * 统计重复的qtName数量
     * @param qtId
     * @param qtName
     * @return
     */
    @Query("select count(qt.qtId) from BaseQuotaTemplate qt"
            + " where binary(qt.qtName) = ?2"
            + " and (?1 = null or ?1 = '' or qt.qtId <> ?1))")
    public int countMultiQtName(String qtId, String qtName);
    
}
