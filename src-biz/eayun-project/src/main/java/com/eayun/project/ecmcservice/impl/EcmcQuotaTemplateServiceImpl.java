package com.eayun.project.ecmcservice.impl;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.commons.collections.IteratorUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.eayun.common.dao.QueryMap;
import com.eayun.common.dao.support.Page;
import com.eayun.common.util.StringUtil;
import com.eayun.project.dao.QuotaTemplateDao;
import com.eayun.project.ecmcservice.EcmcQuotaTemplateService;
import com.eayun.virtualization.model.BaseQuotaTemplate;

@Service
@Transactional
public class EcmcQuotaTemplateServiceImpl implements EcmcQuotaTemplateService {
	
	@Autowired
	private QuotaTemplateDao quotaTemplateDao;

	@Override
	public BaseQuotaTemplate addQuotaTemplate(BaseQuotaTemplate quotaTemplate) {
		try {
			quotaTemplate.setCreateTime(new Date());
			quotaTemplate = quotaTemplateDao.save(quotaTemplate);
			return quotaTemplate;
		} catch (Exception e) {
			throw e;
		}
	}

	@Override
	public void modifyQuotaTemplate(BaseQuotaTemplate quotaTemplate) {
		try {
			quotaTemplateDao.saveOrUpdate(quotaTemplate);
		} catch (Exception e) {
			throw e;
		}
	}

	@Override
	public void delQuotaTemplate(String qtId) {
		try {
			quotaTemplateDao.delete(qtId);
		} catch (Exception e) {
			throw e;
		}
	}

	@Override
	public BaseQuotaTemplate getQuotaTemplate(String qtId) {
		try {
			return quotaTemplateDao.findOne(qtId);
		} catch (Exception e) {
			throw e;
		}
	}

	@Override
	public List<BaseQuotaTemplate> getAllQuotaTemplate() {
		try {
			return IteratorUtils.toList(quotaTemplateDao.findAll().iterator());
		} catch (Exception e) {
			throw e;
		}
	}

	@Override
	public Page getQuotaTemplateList(QueryMap queryMap, String templateName) {
		StringBuffer hql = new StringBuffer();
		List<String> params = new ArrayList<String>();
		hql.append("FROM BaseQuotaTemplate WHERE 1=1 ");
		if (!StringUtil.isEmpty(templateName)) {
			hql.append("AND qtName like ? escape '/' ");
			params.add("%" + escapeSpecialChar(templateName) + "%");
		}
		hql.append("ORDER BY createTime DESC ");
		Page page = quotaTemplateDao.pagedQuery(hql.toString(), queryMap, params.toArray());
		return page;
	}
	
	public boolean hasTemplateByQtName(String qtId, String qtName) {
	    return quotaTemplateDao.countMultiQtName(qtId, qtName) > 0 ? true : false;
	}
	
	private String escapeSpecialChar(String str){
		if (StringUtils.isNotBlank(str)) {
			String[] specialChars = { "\\", "$", "(", ")", "*", "+", ".", "[", "]", "?", "^", "{", "}", "|", "%", "_" };
			for (String key : specialChars) {
				if (str.contains(key)) {
					str = str.replace(key, "/" + key);
				}
			}
		}
		return str;
	}

}
