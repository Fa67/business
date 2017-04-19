package com.eayun.unit.service.impl;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.eayun.common.util.BeanUtils;
import com.eayun.common.util.DateUtil;
import com.eayun.datacenter.ecmcservice.EcmcDataCenterService;
import com.eayun.notice.model.MessageUnitModel;
import com.eayun.notice.service.MessageCenterService;
import com.eayun.unit.dao.ApplyInfoDao;
import com.eayun.unit.dao.UnitDao;
import com.eayun.unit.dao.UnitWebDao;
import com.eayun.unit.dao.WebDataCenterIpDao;
import com.eayun.unit.dao.WebSiteInfoDao;
import com.eayun.unit.model.BaseApplyInfo;
import com.eayun.unit.model.BaseUnitInfo;
import com.eayun.unit.model.BaseUnitWeb;
import com.eayun.unit.model.BaseWebDataCenterIp;
import com.eayun.unit.model.BaseWebSiteInfo;
import com.eayun.unit.model.WebSiteIP;
import com.eayun.unit.service.EcscWebsiteService;

/**
 * @author jingang.liu@eayun.com
 * @Dadte 2016年12月22日
 */
@Service
@Transactional
public class EcscWebsiteServiceImpl implements EcscWebsiteService {

	@Autowired
	private WebSiteInfoDao websiteDao;
	@Autowired
	private ApplyInfoDao applyDao;
	@Autowired
	private UnitWebDao uwDao;

	@Autowired
	private UnitDao unitDao;
	@Autowired
    private WebDataCenterIpDao dcIPDao;
	@Autowired
	private MessageCenterService messageCenterService;
	@Autowired
	private EcmcDataCenterService ecmcDataCenterService;

	@Override
	public List<WebSiteIP> getUnitWebsite(String unitId) {
		List<BaseWebSiteInfo> weblist=websiteDao.getByUnitId(unitId);
		List<BaseWebDataCenterIp> ipList = null;
		List<WebSiteIP> websiteList = null;
		List<BaseWebDataCenterIp> newipList = null;
		 BaseWebDataCenterIp dcip= null;
		String dcname="";
		for (BaseWebSiteInfo web : weblist) {
			
			BaseWebSiteInfo newweb = (BaseWebSiteInfo) web.clone();
			WebSiteIP wsIP = new WebSiteIP();
			BeanUtils.copyPropertiesByModel(wsIP, newweb);
			ipList = dcIPDao.find(" from BaseWebDataCenterIp where webId = ? ", newweb.getWebId());
			websiteList = new ArrayList<WebSiteIP>();
			newipList = new ArrayList<>();
			for (BaseWebDataCenterIp ip : ipList) {
				

				//dcname = ecmcDataCenterService.getdatacenterName(ip.getDcId());
				dcip = (BaseWebDataCenterIp) ip.clone();
				//dcip.setDcId(dcname);
				newipList.add(dcip);
			}
			wsIP.setIpList(newipList);
			websiteList.add(wsIP);
		}
		
		
		return websiteList;
	}

	@Override
	public BaseWebSiteInfo addWebsite(Map<String, Object> parms) throws Exception {
		List<MessageUnitModel> listmodel = new ArrayList<MessageUnitModel>();
		MessageUnitModel model = new MessageUnitModel();
		BaseApplyInfo apply = new BaseApplyInfo();
		BaseUnitInfo unitinfo = unitDao.findOne(parms.get("unitId").toString());
		List<Map<String, Object>> list = (List<Map<String, Object>>) parms.get("webList");
		List<BaseWebSiteInfo> webinfo = new ArrayList<BaseWebSiteInfo>();
		BaseWebSiteInfo web = null;
		BaseWebDataCenterIp dcIP = null;
        List<Map<String, Object>> ipsmap = null;
		for (Map<String, Object> map : list) {
			web = new BaseWebSiteInfo();
			BeanUtils.mapToBean(web, map);
			web.setIsChange("1");
			web.setCrateTime(new Date());
			web.setUnitId(null);// 当审核通过后网站信息才关联到主体上
			web = websiteDao.save(web);// 添加网站

			webinfo.add(web);
			ipsmap = (List<Map<String, Object>>) map.get("ipList");
            for (Map<String, Object> ips : ipsmap) {
                dcIP = new BaseWebDataCenterIp();
                BeanUtils.mapToBean(dcIP, ips);
                dcIP.setWebId(web.getWebId());
                dcIPDao.save(dcIP);// 添加网站的IP集合
            }
		}

		apply.setUnitId(parms.get("unitId").toString());
		apply.setReturnType(null);// 添加时管局返回状态为空
		apply.setStatus(1);// 新增网站的状态不和主体同步，当审核通过后网站信息才关联到主体上
		apply.setRecordType(Integer.valueOf(parms.get("recordType").toString()));// 新增网站
		apply.setCreateTime(new Date());
		applyDao.saveEntity(apply);// 添加备案申请信息

		for (BaseWebSiteInfo w : webinfo) {
			BaseUnitWeb applyweb = new BaseUnitWeb();
			applyweb.setApplyId(apply.getApplyId());
			applyweb.setWebId(w.getWebId());
			uwDao.saveEntity(applyweb);// 添加申请表关联的网站信息
		}
		model.setOrgName(unitinfo.getCusOrg());
		model.setUnitName(unitinfo.getUnitName());
		model.setUnitFuzeName(unitinfo.getDutyName());
		model.setWebNo("");
		model.setRecordType("新增网站");
		model.setTime(DateUtil.dateToString(new Date()));
		listmodel.add(model);
		messageCenterService.newAddUnitTomail(listmodel);// 发送邮件

		return web;
	}

	@Override
	public int deleteWebsite(String webId) throws Exception {
		return websiteDao.createSQLNativeQuery("delete from website_info where web_id = ? ", webId).executeUpdate();
	}

	@Override
	public BaseWebSiteInfo copyWebsite(String webId) throws Exception {
		
		BaseWebSiteInfo web = websiteDao.findOne(webId);
		BaseApplyInfo apply = new BaseApplyInfo();
		apply.setUnitId(web.getUnitId());
		web.setUnitId(null);// 当审核通过后网站信息才关联到主体上
		web.setIsChange("1");
		BaseWebSiteInfo newweb = websiteDao.save(web);
		apply.setReturnType(null);// 添加时管局返回状态为空
		apply.setStatus(1);// 新增网站的状态不和主体同步，当审核通过后网站信息才关联到主体上
		apply.setRecordType(4);// 变更备案
		apply.setCreateTime(new Date());
		applyDao.saveEntity(apply);// 添加备案申请信息
		BaseUnitWeb applyweb = new BaseUnitWeb();
		applyweb.setApplyId(apply.getApplyId());
		applyweb.setWebId(newweb.getWebId());
		applyweb.setOldWebId(web.getWebId());// 只有变更备案时这项才不为空
		uwDao.saveEntity(applyweb);// 添加申请表关联的网站信息
		BaseUnitInfo unitinfo = unitDao.findOne(web.getUnitId());

		

		return newweb;
	}

	@Override
	public List<BaseWebSiteInfo> changeWebsite(Map<String, Object> parms) throws Exception {
		List<MessageUnitModel> listmodel = new ArrayList<MessageUnitModel>();
		MessageUnitModel model = new MessageUnitModel();
	
		BaseApplyInfo apply = new BaseApplyInfo();
		apply.setUnitId(parms.get("unitId").toString());
		BaseUnitInfo unitinfo=unitDao.findOne(apply.getUnitId());
		apply.setReturnType(null);// 添加时管局返回状态为空
		apply.setStatus(1);// 新增网站的状态不和主体同步，当审核通过后网站信息才关联到主体上
		apply.setRecordType(4);// 变更网站
		apply.setCreateTime(new Date());
		applyDao.saveEntity(apply);// 添加备案申请信息

		List<Map<String, Object>> list = (List<Map<String, Object>>) parms.get("webList");
		List<BaseWebSiteInfo> weblist = new ArrayList<BaseWebSiteInfo>();
		BaseWebSiteInfo web = null;
		BaseWebSiteInfo newweb = null;
		BaseUnitWeb applyweb = null;
		BaseWebDataCenterIp dcIP = null;
        List<Map<String, Object>> ipsmap = null;
		for (Map<String, Object> map : list) {
			applyweb = new BaseUnitWeb();
			newweb = new BaseWebSiteInfo();
			BeanUtils.mapToBean(newweb, map);
			web = websiteDao.findOne(newweb.getWebId());
			web.setIsChange("2");
			websiteDao.saveOrUpdate(web);// 修改旧网站信息
			applyweb.setOldWebId(web.getWebId());
			// newweb = (BaseWebSiteInfo)web.clone();
			newweb.setWebId(null);
			newweb.setUnitId(null);// 当审核通过后网站信息才关联到主体上
			newweb.setIsChange("1");
			newweb.setCrateTime(new Date());
			newweb = websiteDao.save(newweb);// 添加新网站信息网站
			ipsmap = (List<Map<String, Object>>) map.get("ipList");
            for (Map<String, Object> ips : ipsmap) {
                dcIP = new BaseWebDataCenterIp();
                BeanUtils.mapToBean(dcIP, ips);
                dcIP.setWebId(newweb.getWebId());
                dcIP.setId(null);
                dcIPDao.save(dcIP);// 添加网站的IP集合
            }
			applyweb.setApplyId(apply.getApplyId());
			applyweb.setWebId(newweb.getWebId());
			weblist.add(newweb);
			uwDao.saveEntity(applyweb);// 添加申请表关联的网站信息
			model.setOrgName(unitinfo.getCusOrg());
			model.setUnitName(unitinfo.getUnitName());
			model.setUnitFuzeName(unitinfo.getDutyName());
			model.setWebNo(web.getWebRecordNo());
			model.setTime(DateUtil.dateToString(new Date()));
			model.setRecordType("变更备案");
			listmodel.add(model);
			messageCenterService.newAddUnitTomail(listmodel);// 发送邮件
		}
		return weblist;
	}

	@Override
	public BaseWebSiteInfo updateWebsite(BaseWebSiteInfo web) throws Exception {
		websiteDao.saveOrUpdate(web);// 修改网站
		return web;
	}

	@Override
	public boolean checkFloatIpWebSite(String floIp) throws Exception {
		List<BaseWebSiteInfo> list = websiteDao.getByServiceIP(floIp);
		if (!list.isEmpty()) {
			return true;
		}
		return false;
	}

}
