package com.eayun.project.ecmcservice;

import java.util.List;

import com.eayun.common.dao.QueryMap;
import com.eayun.common.dao.support.Page;
import com.eayun.virtualization.model.BaseQuotaTemplate;

public interface EcmcQuotaTemplateService {
	
	/**
	 * 创建模板
	 * @param quotaTemplate 模板实体
	 * @return 包含ID的模板实体
	 */
	public BaseQuotaTemplate addQuotaTemplate(BaseQuotaTemplate quotaTemplate);
	
	/**
	 * 修改模板
	 * @param quotaTemplate 模板实体
	 */
	public void modifyQuotaTemplate(BaseQuotaTemplate quotaTemplate);
	
	/**
	 * 删除模板
	 * @param qtId 模板ID
	 */
	public void delQuotaTemplate(String qtId);
	
	/**
	 * 模板详情
	 * @param qtId 模板ID
	 * @return 模板实体
	 */
	public BaseQuotaTemplate getQuotaTemplate(String qtId);
	
	/**
	 * 获取所有模板，用于创建项目和修改项目
	 * @return 模板实体集合
	 */
	public List<BaseQuotaTemplate> getAllQuotaTemplate();
	
	/**
	 * 分页获取模板列表
	 * @return 分页集合
	 */
	public Page getQuotaTemplateList(QueryMap queryMap, String templateName);
	
	/**
	 * 查询配额模板是否重名
	 * @param qtId
	 * @param qtName
	 * @return
	 */
	public boolean hasTemplateByQtName(String qtId, String qtName);

}
