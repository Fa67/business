package com.eayun.project.ecmccontroller;

import java.util.List;
import java.util.Map;

import org.apache.commons.collections.MapUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.eayun.common.ConstantClazz;
import com.eayun.common.controller.BaseController;
import com.eayun.common.dao.ParamsMap;
import com.eayun.common.dao.QueryMap;
import com.eayun.common.model.EayunResponseJson;
import com.eayun.common.util.BeanUtils;
import com.eayun.log.ecmcsevice.EcmcLogService;
import com.eayun.project.ecmcservice.EcmcQuotaTemplateService;
import com.eayun.virtualization.model.BaseQuotaTemplate;

@Controller
@RequestMapping("/ecmc/quota/template")
public class EcmcQuotaTemplateController extends BaseController {
	
	@Autowired
	private EcmcQuotaTemplateService ecmcQuotaTemplateService;
	@Autowired
	private EcmcLogService ecmcLogService;
	
	@RequestMapping(value = "/addquotatemplate")
	@ResponseBody
	public Object addQuotaTemplate(@RequestBody Map<String, Object> requestMap) throws Exception {
		EayunResponseJson reJson = new EayunResponseJson();
		BaseQuotaTemplate baseTemplate = new BaseQuotaTemplate();
		BeanUtils.mapToBean(baseTemplate, requestMap);
		try {
			baseTemplate = ecmcQuotaTemplateService.addQuotaTemplate(baseTemplate);
			reJson.setRespCode(ConstantClazz.SUCCESS_CODE);
			reJson.setData(baseTemplate);
			ecmcLogService.addLog("创建配额模板", ConstantClazz.LOG_TYPE_QTEMPLATE, baseTemplate.getQtName(),
					baseTemplate.getQtId(), 1, baseTemplate.getQtId(), null);
			return reJson;
		} catch (Exception e) {
			ecmcLogService.addLog("创建配额模板", ConstantClazz.LOG_TYPE_QTEMPLATE, baseTemplate.getQtName(), null, 0, null,
					e);
			throw e;
		}
	}
	
	@RequestMapping(value = "/modifyquotatemplate")
	@ResponseBody
	public Object modifyQuotaTemplate(@RequestBody Map<String, Object> requestMap) throws Exception {
		EayunResponseJson reJson = new EayunResponseJson();
		BaseQuotaTemplate baseTemplate = new BaseQuotaTemplate();
		BeanUtils.mapToBean(baseTemplate, requestMap);
		try {
			ecmcQuotaTemplateService.modifyQuotaTemplate(baseTemplate);
			reJson.setRespCode(ConstantClazz.SUCCESS_CODE);
			ecmcLogService.addLog("编辑配额模板", ConstantClazz.LOG_TYPE_QTEMPLATE, baseTemplate.getQtName(),
					baseTemplate.getQtId(), 1, baseTemplate.getQtId(), null);
			return reJson;
		} catch (Exception e) {
			ecmcLogService.addLog("编辑配额模板", ConstantClazz.LOG_TYPE_QTEMPLATE, baseTemplate.getQtName(),
					baseTemplate.getQtId(), 0, baseTemplate.getQtId(), e);
			throw e;
		}
	}
	
	@RequestMapping(value = "/delquotatemplate")
	@ResponseBody
	public Object delQuotaTemplate(@RequestBody Map<String, Object> requestMap) throws Exception{
		EayunResponseJson reJson = new EayunResponseJson();
		String qtId = MapUtils.getString(requestMap, "qtId");
		BaseQuotaTemplate baseTemplate= ecmcQuotaTemplateService.getQuotaTemplate(qtId);
		if(baseTemplate != null){
			try {
				ecmcQuotaTemplateService.delQuotaTemplate(qtId);
				reJson.setRespCode(ConstantClazz.SUCCESS_CODE);
				ecmcLogService.addLog("删除配额模板", ConstantClazz.LOG_TYPE_QTEMPLATE, baseTemplate.getQtName(),
						baseTemplate.getQtId(), 1, baseTemplate.getQtId(), null);
			} catch (Exception e) {
				ecmcLogService.addLog("删除配额模板", ConstantClazz.LOG_TYPE_QTEMPLATE, baseTemplate.getQtName(),
						baseTemplate.getQtId(), 0, baseTemplate.getQtId(), e);
				throw e;
			}
		}
		return reJson;
	}
	
	@RequestMapping(value = "/getallquotatemplate")
	@ResponseBody
	public Object getAllQuotaTemplate(@RequestBody Map<String, Object> requestMap) throws Exception{
		EayunResponseJson reJson = new EayunResponseJson();
		try {
			List<BaseQuotaTemplate> templateList = ecmcQuotaTemplateService.getAllQuotaTemplate();
			reJson.setRespCode(ConstantClazz.SUCCESS_CODE);
			reJson.setData(templateList);
			return reJson;
		} catch (Exception e) {
			throw e;
		}
	}
	
	@RequestMapping(value = "/getquotatemplatelist")
	@ResponseBody
	public Object getQuotaTemplateList(@RequestBody ParamsMap paramsMap) throws Exception{
		QueryMap queryMap = new QueryMap();
		Map<String, Object> params = paramsMap.getParams();
		queryMap.setPageNum(paramsMap.getPageNumber() == null ? 1 : paramsMap.getPageNumber());
		if (paramsMap.getPageSize() != null) {
			queryMap.setCURRENT_ROWS_SIZE(paramsMap.getPageSize());
		}
		String templateName = MapUtils.getString(params, "templateName");
		return ecmcQuotaTemplateService.getQuotaTemplateList(queryMap, templateName);
	}
	
	@RequestMapping(value = "/getquotatemplate")
	@ResponseBody
	public Object getQuotaTemplate(@RequestBody Map<String, Object> requestMap) throws Exception{
		EayunResponseJson reJson = new EayunResponseJson();
		try {
			String qtId = MapUtils.getString(requestMap, "qtId");
			reJson.setRespCode(ConstantClazz.SUCCESS_CODE);
			reJson.setData(ecmcQuotaTemplateService.getQuotaTemplate(qtId));
			return reJson;
		} catch (Exception e) {
			throw e;
		}
	}
	
	@RequestMapping("/checkquotatemplatename")
	@ResponseBody
	public Object checkQuotaTemplateName(@RequestBody Map<String, String> paramsMap) throws Exception {
	    try {
	        EayunResponseJson reJson = new EayunResponseJson();
            reJson.setRespCode(ConstantClazz.SUCCESS_CODE);
            reJson.setData(ecmcQuotaTemplateService.hasTemplateByQtName(paramsMap.get("qtId"), paramsMap.get("qtName")));
            return reJson;
        } catch (Exception e) {
            throw e;
        }
	}
}
