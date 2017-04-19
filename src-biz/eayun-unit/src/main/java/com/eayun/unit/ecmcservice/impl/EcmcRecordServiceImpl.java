package com.eayun.unit.ecmcservice.impl;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.eayun.bean.FileIdOrName;
import com.eayun.bean.NewAccessExcel;
import com.eayun.bean.NewRecordExcel;
import com.eayun.bean.NewWebExcel;
import com.eayun.bean.UninWebApplyVoe;
import com.eayun.bean.UnitWebSVOE;
import com.eayun.bean.applyCountVoe;
import com.eayun.common.dao.QueryMap;
import com.eayun.common.dao.support.Page;
import com.eayun.common.exception.AppException;
import com.eayun.common.util.BeanUtils;
import com.eayun.common.util.DateUtil;
import com.eayun.datacenter.ecmcservice.EcmcDataCenterService;
import com.eayun.log.ecmcsevice.EcmcLogService;
import com.eayun.notice.model.MessageUnitModel;
import com.eayun.notice.service.MessageCenterService;
import com.eayun.unit.dao.ApplyInfoDao;
import com.eayun.unit.dao.UnitDao;
import com.eayun.unit.dao.UnitWebDao;
import com.eayun.unit.dao.WebDataCenterIpDao;
import com.eayun.unit.dao.WebSiteInfoDao;
import com.eayun.unit.ecmcservice.EcmcRecordService;
import com.eayun.unit.model.BaseApplyInfo;
import com.eayun.unit.model.BaseUnitInfo;
import com.eayun.unit.model.BaseUnitWeb;
import com.eayun.unit.model.BaseWebDataCenterIp;
import com.eayun.unit.model.BaseWebSiteInfo;
import com.eayun.unit.model.WebSiteIP;
import com.eayun.unit.service.EcscRecordService;

@Service
@Transactional
public class EcmcRecordServiceImpl implements EcmcRecordService {
	private final Log log = LogFactory.getLog(this.getClass());

	@Autowired
	private UnitDao unitDao;
	@Autowired
	private ApplyInfoDao applyInfoDao;

	@Autowired
	private EcmcLogService ecmclogservice;
	@Autowired
	private WebSiteInfoDao webSiteInfoDao;

	@Autowired
	private UnitWebDao unitWebDao;

	@Autowired
	private EcscRecordService recordservice;
	@Autowired
	private EcmcDataCenterService ecmcDataCenterService;

	@Autowired
	private MessageCenterService messageCenterService;
	@Autowired
	private WebDataCenterIpDao dcIPDao;

	@Override
	public Page getecmcrecordlist(QueryMap queryMap, String recordType, String status, String dcid, String queryName)
			throws AppException {
		Page page = null;
		int sum = 0;
		StringBuffer sql = new StringBuffer(
				" select  a.cusorg, a.unitname, a.dutyname ,a.recordtype, a.status, a.time, a.id , a.unitid from (select  i.cus_org as cusorg,i.unit_name as unitname,i.duty_name as dutyname");
		sql.append(
				" ,a.record_type as recordtype,a.status as status,a.create_time as time,a.apply_id as id ,i.unit_id as unitid");
		sql.append(" from apply_info a");
		sql.append(" left join unit_info i on a.unit_id=i.unit_id");

		sql.append(" where 1=1");
		List<String> params = new ArrayList<String>();

		if (!"".equals(dcid) && null != dcid) {
			sql.append(" and i.dc_id=?");
			params.add(dcid);
		}
		if (!"".equals(queryName) && null != queryName) {

			sql.append(" and i.cus_org like ?");
			params.add("%" + escapeSpecialChar(queryName) + "%");

		}
		if (!"".equals(queryName) && null != queryName) {

			sql.append(" or i.unit_name like ?");
			params.add("%" + escapeSpecialChar(queryName) + "%");

		}

		sql.append(" order by a.create_time desc) a ");

		if (!"".equals(status) && null != status) {
			sql.append(" where a.status=?");
			params.add(status);
			sum++;

		}
		if (!"".equals(recordType) && null != recordType) {
			if (sum != 0) {
				sql.append(" and  a.recordtype=?");
				params.add(recordType);
			} else {
				sql.append("  where  a.recordtype=?");
				params.add(recordType);
			}

		}

		page = unitDao.pagedNativeQuery(sql.toString(), queryMap, params.toArray());
		List list = (List) page.getResult();
		List<UninWebApplyVoe> newList = new ArrayList<UninWebApplyVoe>();
		for (int i = 0; i < list.size(); i++) {
			UninWebApplyVoe voe = new UninWebApplyVoe();
			Object[] obj = (Object[]) list.get(i);
			voe.setCusOrg(obj[0] == null ? "" : obj[0].toString());
			voe.setUnitName(obj[1] == null ? "" : obj[1].toString());

			voe.setDutyName(obj[2] == null ? "" : obj[2].toString());
			voe.setUnitType(com.eayun.common.constant.recordType.getName(obj[3] == null ? "" : obj[3].toString()));
			voe.setStatus(com.eayun.common.constant.UnitStatusType.getName(obj[4] == null ? "" : obj[4].toString()));

			voe.setTime(DateUtil.stringToDate(obj[5] == null ? "" : ObjectUtils.toString(obj[5])));
			voe.setId(obj[6] == null ? "" : obj[6].toString());
			voe.setWebRecordNo(this.queryWeb(obj[6] == null ? "" : obj[6].toString()));
			voe.setStatustype(obj[4] == null ? "" : obj[4].toString());
			voe.setUnitid(obj[7] == null ? "" : obj[7].toString());
			newList.add(voe);

		}
		if (newList.size() > 0) {
			page.setResult(newList);
		}
		return page;
	}

	private String escapeSpecialChar(String str) {
		if (StringUtils.isNotBlank(str)) {
			String[] specialChars = { "\\", "$", "(", ")", "*", "+", ".", "[", "]", "?", "^", "{", "}", "|", "%" };
			for (String key : specialChars) {
				if (str.contains(key)) {
					str = str.replace(key, "/" + key);
				}
			}
		}
		return str;
	}

	private String queryWeb(String id) {
		StringBuffer sb = new StringBuffer("");
		if (!"".equals(id) && null != id) {
			List<BaseUnitWeb> list = unitWebDao.getWebIdByUnitid(id);

			for (int i = 0; i < list.size(); i++) {
				BaseUnitWeb web = list.get(i);
				BaseWebSiteInfo webinfo = webSiteInfoDao.findOne(web.getWebId());
				if (null != webinfo && null != webinfo.getWebRecordNo()) {
					sb.append(webinfo.getWebRecordNo() + ";<br>");
				}

			}
		}

		return sb.toString();

	}

	@Override
	public Object getecmcrecordcount() throws AppException {
		StringBuffer sql = new StringBuffer("select ");
		sql.append(" sum(if((a.record_type=1 and a.status=1), 1, 0)) as shouci_chu,");
		sql.append(" sum(if((a.record_type=1 and a.status=3), 1, 0)) as shouci_chuw,");
		sql.append(" sum(if((a.record_type=1 and a.status=2), 1, 0)) as shouci_fu,");
		sql.append(" sum(if((a.record_type=1 and a.status=5), 1, 0)) as shouci_fuw,");
		sql.append(" sum(if((a.record_type=1 and a.status=4), 1, 0)) as shouci_upgj,");
		sql.append(" sum(if((a.record_type=1 and a.status=7), 1, 0)) as shouci_upgjw,");
		sql.append(" sum(if((a.record_type=1 and a.status=8), 1, 0)) as shouci_upgjs,");
		sql.append(" sum(if((a.record_type=2 and a.status=1), 1, 0)) as new_chu,");
		sql.append(" sum(if((a.record_type=2 and a.status=3), 1, 0)) as new_chuw,");
		sql.append(" sum(if((a.record_type=2 and a.status=2), 1, 0)) as new_fu,");
		sql.append(" sum(if((a.record_type=2 and a.status=5), 1, 0)) as new_fuw,");
		sql.append(" sum(if((a.record_type=2 and a.status=4), 1, 0)) as new_upgj,");
		sql.append(" sum(if((a.record_type=2 and a.status=7), 1, 0)) as new_upgjw,");
		sql.append(" sum(if((a.record_type=2 and a.status=8), 1, 0)) as new_upgjs,");
		sql.append(" sum(if((a.record_type=3 and a.status=1), 1, 0)) as newimpl_chu,");
		sql.append(" sum(if((a.record_type=3 and a.status=3), 1, 0)) as newimpl_chuw,");
		sql.append(" sum(if((a.record_type=3 and a.status=2), 1, 0)) as newimpl_fu,");
		sql.append(" sum(if((a.record_type=3 and a.status=5), 1, 0)) as newimpl_fuw,");
		sql.append(" sum(if((a.record_type=3 and a.status=4), 1, 0)) as newimpl_upgj,");
		sql.append(" sum(if((a.record_type=3 and a.status=7), 1, 0)) as newimpl_upgjw,");
		sql.append(" sum(if((a.record_type=3 and a.status=8), 1, 0)) as newimpl_upgjs,");
		sql.append(" sum(if((a.record_type=4 and a.status=1), 1, 0)) as biangen_chu,");
		sql.append(" sum(if((a.record_type=4 and a.status=3), 1, 0)) as biangen_chuw,");
		sql.append(" sum(if((a.record_type=4 and a.status=2), 1, 0)) as biangen_fu,");
		sql.append(" sum(if((a.record_type=4 and a.status=5), 1, 0)) as biangen_fuw,");
		sql.append(" sum(if((a.record_type=4 and a.status=4), 1, 0)) as biangen_upgj,");
		sql.append(" sum(if((a.record_type=4 and a.status=7), 1, 0)) as biangen_upgjw,");
		sql.append(" sum(if((a.record_type=4 and a.status=8), 1, 0)) as biangen_upgjs");
		sql.append(" from  apply_info a");
		List list = unitDao.createSQLNativeQuery(sql.toString(), null).getResultList();
		applyCountVoe voe = new applyCountVoe();
		if (list.size() > 0) {

			Object[] obj = (Object[]) list.get(0);

			voe.setShoucidengdaichushen(getObjtoString(obj[0]));
			voe.setShoucichushenweitongguo(getObjtoString(obj[1]));
			voe.setShoucidengdaifushen(getObjtoString(obj[2]));
			voe.setShoucifushenweitongguo(getObjtoString(obj[3]));
			voe.setShoucidaishangbaoguanju(getObjtoString(obj[4]));
			voe.setShouciguanjutuihui(getObjtoString(obj[5]));
			voe.setShoucibeianchenggong(getObjtoString(obj[6]));

			voe.setNewwebdengdaichushen(getObjtoString(obj[7]));
			voe.setNewwebchushenweitongguo(getObjtoString(obj[8]));
			voe.setNewwebdengdaifushen(getObjtoString(obj[9]));
			voe.setNewwebfushenweitongguo(getObjtoString(obj[10]));
			voe.setNewwebdaishangbaoguanju(getObjtoString(obj[11]));
			voe.setNewwebguanjutuihui(getObjtoString(obj[12]));
			voe.setNewwebbeianchenggong(getObjtoString(obj[13]));

			voe.setImpldengdaichushen(getObjtoString(obj[14]));
			voe.setImplchushenweitongguo(getObjtoString(obj[15]));
			voe.setImpldengdaifushen(getObjtoString(obj[16]));
			voe.setImplfushenweitongguo(getObjtoString(obj[17]));
			voe.setImpldaishangbaoguanju(getObjtoString(obj[18]));
			voe.setImplguanjutuihui(getObjtoString(obj[19]));
			voe.setImplbeianchenggong(getObjtoString(obj[20]));

			voe.setBiangendengdaichushen(getObjtoString(obj[21]));
			voe.setBiangenchushenweitongguo(getObjtoString(obj[22]));
			voe.setBiangendengdaifushen(getObjtoString(obj[23]));
			voe.setBiangenfushenweitongguo(getObjtoString(obj[24]));
			voe.setBiangendaishangbaoguanju(getObjtoString(obj[25]));
			voe.setBiangenguanjutuihui(getObjtoString(obj[26]));
			voe.setBiangenbeianchenggong(getObjtoString(obj[27]));
		}

		List listcount = new ArrayList();
		listcount.add(voe);
		return listcount;
	}

	@Override
	public List updaterecord(String id, String status, String status1) throws AppException {
		BaseApplyInfo model = applyInfoDao.findOne(id);
		if (model == null) {
            throw new AppException("修改对象无效.");
        }
		List<FileIdOrName> list = new ArrayList<FileIdOrName>();
		List<MessageUnitModel> listmg = new ArrayList<MessageUnitModel>();
		MessageUnitModel modelmg = new MessageUnitModel();
		BaseUnitInfo unitinfo = unitDao.findOne(model.getUnitId());
		String webdo = "";
		try {
			model.setStatus(Integer.valueOf(status));
			if (null != status1 && "".equals(status1)) {
				model.setReturnType(Integer.valueOf(status1));
			}
			if(!"4".equals(model.getRecordType().toString())){
				List<BaseUnitWeb> unitwebs = unitWebDao.getWebIdByUnitid(id);
				for (int i = 0; i < unitwebs.size(); i++) {

					String oldwebid = unitwebs.get(i).getWebId();
					BaseWebSiteInfo olwmodel = null;
					if (null != oldwebid && !"".equals(oldwebid)) {
						olwmodel = webSiteInfoDao.findOne(oldwebid);
						webdo = olwmodel.getDomainName() ;
					}

				}
			}
			if ("4".equals(model.getRecordType().toString())) {
				List<BaseUnitWeb> unitwebs = unitWebDao.getWebIdByUnitid(id);
				for (int i = 0; i < unitwebs.size(); i++) {
					webdo =webSiteInfoDao.findOne(unitwebs.get(i).getWebId()).getDomainName();
					String oldwebid = unitwebs.get(i).getOldWebId();
					BaseWebSiteInfo olwmodel = null;
					if (null != oldwebid && !"".equals(oldwebid)) {
						olwmodel = webSiteInfoDao.findOne(oldwebid);
						
					}

					if (null != olwmodel && "3".equals(model.getStatus().toString())) {

						olwmodel.setIsChange("1");
						webSiteInfoDao.saveOrUpdate(olwmodel);
					}
				}
			}

			applyInfoDao.saveOrUpdate(model);
			modelmg.setUnitName(unitinfo.getUnitName());
			modelmg.setDomainName(webdo);
			modelmg.setRecordType(com.eayun.common.constant.recordType.getName(String.valueOf(model.getRecordType())));
			modelmg.setStatus(com.eayun.common.constant.UnitStatusType.getName(String.valueOf(model.getStatus())));
			modelmg.setTime(DateUtil.dateToString(model.getCreateTime()));
			listmg.add(modelmg);
			if (!"1".equals(status) && !"7".equals(status) && !"8".equals(status)) {

				messageCenterService.unitStatusToMailAndSms(unitinfo.getDutyPhone(), unitinfo.getDutyEmail(),
						unitinfo.getDutyName(), listmg);

			}

			ecmclogservice.addLog(com.eayun.common.constant.recordupdateType.getName(status), "备案",
					com.eayun.common.constant.recordType.getName(model.getRecordType().toString()), null, 1, id, null);

		} catch (Exception e) {
			ecmclogservice.addLog(com.eayun.common.constant.recordupdateType.getName(status), "备案",
					com.eayun.common.constant.recordType.getName(model.getRecordType().toString()), null, 0, id, e);
			throw new AppException("修改备案异常", e);

		}

		return list;
	}

	@Override
	public void deletedrecord(String id) throws AppException {
		BaseApplyInfo model = applyInfoDao.findOne(id);
		if (model == null) {
			throw new AppException("删除对象无效.");
		}
		try {
			String type = String.valueOf(model.getRecordType());

			if ("1".equals(type)) {
				this.deletebyid(id);// 删除关系及网站

				unitDao.delete(model.getUnitId());// 删除主体信息

			} else if ("2".equals(type)) {
				this.deletebyid(id);

			} else if ("3".equals(type)) {
				this.deletebyid(id);// 删除关系及网站
				unitDao.delete(model.getUnitId());// 删除主体信息

			} else if ("4".equals(type)) {

				this.deletebyid(id);// 删除关系及网站
			}
			applyInfoDao.delete(id);
			ecmclogservice.addLog("删除备案", "备案",
					com.eayun.common.constant.recordType.getName(model.getRecordType().toString()), null, 1, id, null);

		} catch (Exception e) {
			ecmclogservice.addLog("删除备案", "备案",
					com.eayun.common.constant.recordType.getName(model.getRecordType().toString()), null, 0, id, e);
			throw new AppException("删除备案异常", e);
		}

	}

	private void deletebyid(String id) {
		List<BaseUnitWeb> list = unitWebDao.getWebIdByUnitid(id);
		for (int i = 0; i < list.size(); i++) {
			String webid = list.get(i).getWebId();
			log.info("查询删除网站信息开始，网站ID：" + webid);
			BaseWebSiteInfo model = webSiteInfoDao.findOne(webid);
			if (null != model) {
				webSiteInfoDao.delete(model);// 删除网站信息
				dcIPDao.deleteWebDataCenterIp(webid);
			}
			String oldwebid = list.get(i).getOldWebId();
			BaseWebSiteInfo olwmodel = null;
			if (null != oldwebid && !"".equals(oldwebid)) {
				olwmodel = webSiteInfoDao.findOne(oldwebid);
			}

			if (null != olwmodel) {
				olwmodel.setIsChange("1");
				webSiteInfoDao.saveOrUpdate(olwmodel);
			}
			unitWebDao.delete(list.get(i));// 删除关系

		}

	}

	@Override
	public UnitWebSVOE getbyid(String id) throws AppException {
		BaseApplyInfo info = applyInfoDao.findOne(id);
		List<BaseUnitWeb> list = unitWebDao.getWebIdByUnitid(id);
		Integer recordtype = info.getRecordType();

		UnitWebSVOE model = new UnitWebSVOE();
		BaseUnitInfo unitinfo = unitDao.findOne(info.getUnitId());
		if (unitinfo != null) {

			BeanUtils.copyPropertiesByModel(model, unitinfo);
		}

		model.setRecordType(recordtype);
		model.setStatus(info.getStatus());

		List<BaseWebDataCenterIp> ipList = null;
		List<BaseWebDataCenterIp> newipList = null;
		List<WebSiteIP> websiteList = null;
		BaseWebDataCenterIp dcip = null;
		for (int i = 0; i < list.size(); i++) {
			String webid = list.get(i).getWebId();
			BaseWebSiteInfo webinfo = null;
			BaseWebSiteInfo web = null;
			if (!"".equals(webid)) {
				webinfo = webSiteInfoDao.findOne(webid);
			}
			if (null != webinfo) {

				String dcname = "";
				web = (BaseWebSiteInfo) webinfo.clone();
				WebSiteIP wsIP = new WebSiteIP();
				BeanUtils.copyPropertiesByModel(wsIP, web);
				ipList = dcIPDao.find(" from BaseWebDataCenterIp where webId = ? ", webid);
				websiteList = new ArrayList<WebSiteIP>();
				newipList = new ArrayList<>();
				for (BaseWebDataCenterIp ip : ipList) {
					dcname = ecmcDataCenterService.getdatacenterName(ip.getDcId());
					dcip = (BaseWebDataCenterIp) ip.clone();
					dcip.setDcId(dcname);
					newipList.add(dcip);
				}
				wsIP.setIpList(newipList);
				websiteList.add(wsIP);
				// webs.add(websiteList);
			}

		}
		model.setWebs(websiteList);
		if (null != model.getUnitArea()) {

			model.setUnitArea(recordservice.getAreaName(model.getUnitArea()));
			model.setUnitNaturestr(com.eayun.common.constant.UnitNatureType.getName(model.getUnitNature().toString()));
			model.setCertificateTypestr(
					com.eayun.common.constant.UnitcertificateType.getName(model.getCertificateType().toString()));
			model.setDutyCertificateTypestr(
					com.eayun.common.constant.DutyCertificateType.getName(model.getDutyCertificateType().toString()));
			model.setApplyId(id);
		}

		return model;

	}

	@Override
	public void recordRe(UnitWebSVOE voe) throws AppException {
		String id = voe.getApplyId();
		BaseApplyInfo model = applyInfoDao.findOne(id);
		BaseUnitInfo unit = null;
		if (model == null) {
			throw new AppException("传入对象无效.");
		}
		try {
			model.setStatus(voe.getStatus());
			if (null != voe.getReturnType() && voe.getReturnType() > 0) {
				model.setReturnType(voe.getReturnType());
			}
			if (null != voe.getRecordNo() && !"".equals(voe.getRecordNo())) {
				unit = unitDao.findOne(model.getUnitId());
				unit.setRecordNo(voe.getRecordNo());
				unitDao.saveOrUpdate(unit);
			}

			// 判断备案类型
			for (int i = 0; i < voe.getWebs().size(); i++) {
				List list = voe.getWebs();

				BaseWebSiteInfo web = new BaseWebSiteInfo();
				BeanUtils.mapToBean(web, (Map) list.get(i));
				if (model.getRecordType().toString().equals("2")) {

					if (model.getStatus().toString().equals("8")) {
						// BaseUnitWeb web=unitWebDao.findOne(id);
						BaseWebSiteInfo webinfo = webSiteInfoDao.findOne(web.getWebId());

						webinfo.setWebRecordNo(web.getWebRecordNo());
						webinfo.setUnitId(model.getUnitId());
						webSiteInfoDao.saveOrUpdate(webinfo);
					}

				}

				if (model.getRecordType().toString().equals("4")) {
					if (model.getStatus().toString().equals("8")) {
						// BaseUnitWeb web=unitWebDao.findOne(id);
						String webid = web.getWebId();
						System.out.println(webid);
						BaseWebSiteInfo webinfo = webSiteInfoDao.findOne(webid);
						webinfo.setWebRecordNo(web.getWebRecordNo());
						webinfo.setUnitId(model.getUnitId());
						webSiteInfoDao.saveOrUpdate(webinfo);
						BaseUnitWeb webunit = unitWebDao.getolWeb(id, webid);
						BaseWebSiteInfo deleteweb = webSiteInfoDao.findOne(webunit.getOldWebId());
						webSiteInfoDao.delete(deleteweb);
						BaseUnitWeb deletewebunit = unitWebDao.getWeb(webunit.getOldWebId());
						unitWebDao.delete(deletewebunit);
					}

				} else {
					// List<BaseUnitWeb> web=unitWebDao.getWebIdByUnitid(id);

					BaseWebSiteInfo webinfo = webSiteInfoDao.findOne(web.getWebId());

					webinfo.setWebRecordNo(web.getWebRecordNo());
					webinfo.setUnitId(model.getUnitId());
					webSiteInfoDao.saveOrUpdate(webinfo);
					// webSiteInfoDao.delete(web.getOldWebId());
				}
			}
			applyInfoDao.saveOrUpdate(model);

			ecmclogservice.addLog(com.eayun.common.constant.recordupdateType.getName(String.valueOf(voe.getStatus())),
					"备案", com.eayun.common.constant.recordType.getName(model.getRecordType().toString()), null, 1, id,
					null);

		} catch (Exception e) {

			ecmclogservice.addLog(com.eayun.common.constant.recordupdateType.getName(String.valueOf(voe.getStatus())),
					"备案", com.eayun.common.constant.recordType.getName(model.getRecordType().toString()), null, 0, id,
					e);
			throw new AppException("管局结果异常", e);
		}
	}

	@Override
	public List updaterecord(String id) throws AppException {
		BaseApplyInfo model = null;
		List<FileIdOrName> list = new ArrayList<FileIdOrName>();
		File file = null;
		try {
			model = applyInfoDao.findOne(id);

			FileIdOrName fileids = new FileIdOrName();
			FileIdOrName fileids1 = new FileIdOrName();
			BaseUnitInfo info = unitDao.findOne(model.getUnitId());
			fileids.setId(info.getBusinessFileId());
			fileids.setName("营业执照");
			list.add(fileids);

			fileids1.setId(info.getDutyFileId());
			fileids1.setName("主体负责人");
			list.add(fileids1);

			List<BaseUnitWeb> webs = unitWebDao.getWebIdByUnitid(model.getApplyId());
			for (int i = 0; i < webs.size(); i++) {
				FileIdOrName fileid = new FileIdOrName();
				FileIdOrName fileid2 = new FileIdOrName();
				FileIdOrName fileid3 = new FileIdOrName();
				BaseUnitWeb web = webs.get(i);
				BaseWebSiteInfo webstie = webSiteInfoDao.findOne(web.getWebId());
				if (null != webstie.getDomainFileId() && !"".equals(webstie.getDomainFileId())) {
					fileid.setId(webstie.getDomainFileId());
					fileid.setName("网站域名证书 (" + webstie.getDomainName() + ")");
					list.add(fileid);
				}

				if (null != webstie.getDutyFileId() && !"".equals(webstie.getDutyFileId())) {
					fileid2.setId(webstie.getDutyFileId());
					fileid2.setName("网站负责人证件照(" + webstie.getDomainName() + ")");
					list.add(fileid2);
				}
				if (null != webstie.getSpecialFileId() && !"".equals(webstie.getSpecialFileId())) {
					fileid3.setId(webstie.getSpecialFileId());
					fileid3.setName("前置专项审批照(" + webstie.getDomainName() + ")");
					list.add(fileid3);
				}

			}

			return list;

		} catch (Exception e) {

			throw new AppException("查询下载文件出现异常", e);

		}

	}

	@Override
	public BaseUnitInfo getUnitInfo(String id) throws AppException {

		return unitDao.findOne(applyInfoDao.findOne(id).getUnitId());
	}

	private String getObjtoString(Object obj) {
		String str = "0";
		if (null == obj || "".equals(obj)) {
			return str;
		} else {
			return obj.toString();
		}

	}

	@Override
	public BaseUnitInfo updatedetail(BaseUnitInfo model) throws AppException {

		String recordNo = model.getRecordNo();
		String unitId = model.getUnitId();

		unitDao.updatedetail(recordNo, unitId);

		return unitDao.getUnitInfoByid(unitId);

	}

	@Override
	public boolean updatedetail(UnitWebSVOE model) throws AppException {
		List list = model.getWebs();
		Boolean fag = false;
		for (int i = 0; i < list.size(); i++) {
			BaseWebSiteInfo web = new BaseWebSiteInfo();
			BeanUtils.mapToBean(web, (Map) list.get(i));
			BaseWebSiteInfo webby = webSiteInfoDao.findOne(web.getWebId());
			if (!webby.getWebRecordNo().equals(web.getWebRecordNo())) {
				webby.setWebRecordNo(web.getWebRecordNo());
				webSiteInfoDao.save(webby);
				fag = true;
			}

		}

		return fag;
	}

	private String recursiveReplace(String str) {
		if (str != null && str != "") {
			if (str.indexOf(",,") < 0) {
				if (str.length() > 1) {
					String strx = str.substring(0, 1);
					if (",".equals(strx)) {
						str = str.substring(1, str.length());
					}
				}
				return str;
			} else {
				str = str.replace(",,", ",");
				return recursiveReplace(str);
			}
		} else {
			return "";
		}
	}

	/**
	 * @author jingang.liu@eayun.com
	 * @Dadte 2017年3月16日
	 *        <p>
	 *        V1.1导出备案信息
	 *        </p>
	 */
	@Override
	public List<NewRecordExcel> getNewRecordExcel(String applyId) {
		List<NewRecordExcel> listrecordexcel = new ArrayList<>();
		NewRecordExcel recordexcel = new NewRecordExcel();
		BaseApplyInfo info = applyInfoDao.findOne(applyId);// 申请信息
		List<BaseUnitWeb> list = unitWebDao.getWebIdByUnitid(applyId);// 申请的网站ID集合
		BaseUnitInfo unitinfo = unitDao.findOne(info.getUnitId());// 主体信息
		BaseWebSiteInfo webinfo = null;
		List<BaseWebSiteInfo> webs = new ArrayList<>();
		for (BaseUnitWeb webId : list) {
			if (!"".equals(webId.getWebId())) {
				webinfo = webSiteInfoDao.findOne(webId.getWebId());
				webs.add(webinfo);
			}
		}
		String areaname = recordservice.getAreaName(unitinfo.getUnitArea());// 获取区域名称
		String status = "";
		switch (info.getStatus()) {
		case 1:
			status = "等待初审";
			break;
		case 2:
			status = "初审通过";
			break;
		case 3:
			status = "初审未通过";
			break;
		case 4:
			status = "复审通过";
			break;
		case 5:
			status = "复审未通过";
			break;
		case 6:
			status = "管局审核";
			break;
		case 7:
			status = "管局未通过";
			break;
		case 8:
			status = "备案成功";
			break;
		default:
			status = "等待初审";
			break;
		}
		recordexcel.setStatus(status);
		recordexcel.setRecordCusName(unitinfo.getCusOrg());
		recordexcel.setRecordNo(unitinfo.getRecordNo());
		recordexcel.setRecordId("");
		recordexcel.setHeadName(unitinfo.getHeadName());
		recordexcel.setUnitName(unitinfo.getUnitName());
		String unitNature = "";
		switch (unitinfo.getUnitNature()) {
		case 1:
			unitNature = "个人";
			break;
		case 2:
			unitNature = "企业";
			break;
		case 3:
			unitNature = "政府机关";
			break;
		case 4:
			unitNature = "事业单位";
			break;
		case 5:
			unitNature = "社会群体";
			break;
		case 6:
			unitNature = "军队";
			break;
		default:
			unitNature = "个人";
			break;
		}
		recordexcel.setUnitNature(unitNature);
		recordexcel.setProvince(areaname.split(" ")[0]);
		recordexcel.setCity(areaname.split(" ")[1]);
		recordexcel.setCounty(areaname.split(" ")[2]);
		recordexcel.setUnitAddress(unitinfo.getUnitAddress());
		String certificateType = "";
		switch (unitinfo.getCertificateType()) {
		case 1:
			certificateType = "工商营业执照";
			break;
		case 2:
			certificateType = "组织机构代码证书";
			break;
		default:
			certificateType = "工商营业执照";
			break;
		}
		recordexcel.setCertificateType(certificateType);
		recordexcel.setCertificateNo(unitinfo.getCertificateNo());
		recordexcel.setCertificateAddress(unitinfo.getCertificateAddress());
		recordexcel.setRecordWay("代为报备");
		recordexcel.setRemark(unitinfo.getRemark());
		recordexcel.setDutyName(unitinfo.getDutyName());
		recordexcel.setPhone(unitinfo.getPhone());
		recordexcel.setDutyPhone(unitinfo.getDutyPhone());
		recordexcel.setDutyEmail(unitinfo.getDutyEmail());
		recordexcel.setMsn("");
		recordexcel.setQq(unitinfo.getDutyQQ());
		String dutyCertificateType = "";
		switch (unitinfo.getDutyCertificateType()) {
		case 1:
			dutyCertificateType = "身份证";
			break;
		case 2:
			dutyCertificateType = "护照";
			break;
		case 3:
			dutyCertificateType = "台胞证";
			break;
		case 4:
			dutyCertificateType = "军官证";
			break;
		default:
			dutyCertificateType = "身份证";
			break;
		}
		recordexcel.setDutyCertificateType(dutyCertificateType);
		recordexcel.setDutyCertificateNo(unitinfo.getDutyCertificateNo());
		for (BaseWebSiteInfo website : webs) {// 备案1.1版本调整为一次备案一个网站所有这个循环只有一次
			recordexcel.setWebName(website.getWebName());
			recordexcel.setWebRecordNo(website.getWebRecordNo());
			recordexcel.setWebId("");
			recordexcel.setServiceContent(recursiveReplace(website.getServiceContent()));
			recordexcel.setWebLanguage(recursiveReplace(website.getWebLanguage()));
			recordexcel.setDomainUrl(website.getDomainUrl());
			recordexcel.setDomainName(website.getDomainName());
			recordexcel.setWebRemark(website.getRemark());
			String webSpecial = "";
			if (website.getWebSpecial() != null) {
				switch (website.getWebSpecial()) {
				case 1:
					webSpecial = "新闻";
					break;
				case 2:
					webSpecial = "出版";
					break;
				case 3:
					webSpecial = "教育";
					break;
				case 4:
					webSpecial = "医疗保健";
					break;
				case 5:
					webSpecial = "药品和医疗器械";
					break;
				case 6:
					webSpecial = "文化";
					break;
				case 7:
					webSpecial = "广播电影电视节目";
					break;
				default:
					webSpecial = "新闻";
					break;
				}
			}
			recordexcel.setWebSpecial(webSpecial);
			recordexcel.setSpecialNo(website.getSpecialNo());
			recordexcel.setSpecialFile("");
			recordexcel.setWebDutyName(website.getWebDutyName());
			recordexcel.setWebPhone(website.getPhone());
			recordexcel.setWebDutyPhone(website.getDutyPhone());
			recordexcel.setWebDutyEmail(website.getDutyEmail());
			recordexcel.setWebMSN("");
			recordexcel.setWebQQ(website.getDutyQQ());
			String webDutyCertificateType = "";
			switch (website.getDutyCertificateType()) {
			case 1:
				webDutyCertificateType = "身份证";
				break;
			case 2:
				webDutyCertificateType = "护照";
				break;
			case 3:
				webDutyCertificateType = "台胞证";
				break;
			case 4:
				webDutyCertificateType = "军官证";
				break;
			default:
				webDutyCertificateType = "身份证";
				break;
			}
			recordexcel.setWebDutyCertificateType(webDutyCertificateType);
			recordexcel.setWebDutyCertificateNo(website.getDutyCertificateNo());
			recordexcel.setAccessType("虚拟主机");
			List<BaseWebDataCenterIp> ipList = dcIPDao.find(" from BaseWebDataCenterIp where webId = ? ", website.getWebId());
			String ipPeriod = "";
			String webAddress = "";
			for(BaseWebDataCenterIp ip : ipList){
			    ipPeriod += ip.getIp() + "-" + ip.getIp() + ";";
			    if (null != ip.getDcId()) {
                    String dcname = ecmcDataCenterService.getProvinces(ip.getDcId());// 获取数据中心分布地点
                    webAddress += dcname + ";";
                }
			}
			recordexcel.setWebAddress(webAddress);
			recordexcel.setIPPeriod(ipPeriod);
		}

		listrecordexcel.add(recordexcel);
		return listrecordexcel;
	}

	/**
	 * @author jingang.liu@eayun.com
	 * @Dadte 2017年3月16日
	 *        <p>
	 *        V1.1导出备案信息
	 *        </p>
	 */
	@Override
	public List<NewAccessExcel> getNewAccessExcel(String applyId) {
		List<NewAccessExcel> listaccressexcel = new ArrayList<>();
		NewAccessExcel accressexcel = new NewAccessExcel();
		BaseApplyInfo info = applyInfoDao.findOne(applyId);// 申请信息
		List<BaseUnitWeb> list = unitWebDao.getWebIdByUnitid(applyId);// 申请的网站ID集合
		BaseUnitInfo unitinfo = unitDao.findOne(info.getUnitId());// 主体信息
		BaseWebSiteInfo webinfo = null;
		BaseWebSiteInfo web = null;
		List<BaseWebSiteInfo> webs = new ArrayList<>();
		for (BaseUnitWeb webId : list) {
			if (!"".equals(webId.getWebId())) {
				webinfo = webSiteInfoDao.findOne(webId.getWebId());
				webs.add(webinfo);
			}
		}
		String status = "";
		switch (info.getStatus()) {
		case 1:
			status = "等待初审";
			break;
		case 2:
			status = "初审通过";
			break;
		case 3:
			status = "初审未通过";
			break;
		case 4:
			status = "复审通过";
			break;
		case 5:
			status = "复审未通过";
			break;
		case 6:
			status = "管局审核";
			break;
		case 7:
			status = "管局未通过";
			break;
		case 8:
			status = "备案成功";
			break;
		default:
			status = "等待初审";
			break;
		}
		accressexcel.setStatus(status);
		accressexcel.setRecordCusName(unitinfo.getCusOrg());
		for (BaseWebSiteInfo website : webs) {// 备案1.1版本调整为一次备案一个网站所有这个循环只有一次
			accressexcel.setWebRecordNo(website.getWebRecordNo());
			accressexcel.setPassword(website.getWebPassword());
			accressexcel.setAccessType("虚拟主机");
			List<BaseWebDataCenterIp> ipList = dcIPDao.find(" from BaseWebDataCenterIp where webId = ? ", website.getWebId());
            String ipPeriod = "";
            String webAddress = "";
            for(BaseWebDataCenterIp ip : ipList){
                ipPeriod += ip.getIp() + "-" + ip.getIp() + ";";
                if (null != ip.getDcId()) {
                    String dcname = ecmcDataCenterService.getProvinces(ip.getDcId());// 获取数据中心分布地点
                    webAddress += dcname + ";";
                }
            }
            accressexcel.setWebAddress(webAddress);
			accressexcel.setIPPeriod(ipPeriod);
		}

		listaccressexcel.add(accressexcel);
		return listaccressexcel;
	}

	/**
	 * @author jingang.liu@eayun.com
	 * @Dadte 2017年3月16日
	 *        <p>
	 *        V1.1导出备案信息
	 *        </p>
	 */
	@Override
	public List<NewWebExcel> getNewWebExcel(String applyId) {
		List<NewWebExcel> listwebexcel = new ArrayList<>();
		NewWebExcel webexcel = new NewWebExcel();
		BaseApplyInfo info = applyInfoDao.findOne(applyId);// 申请信息
		List<BaseUnitWeb> list = unitWebDao.getWebIdByUnitid(applyId);// 申请的网站ID集合
		BaseUnitInfo unitinfo = unitDao.findOne(info.getUnitId());// 主体信息
		BaseWebSiteInfo webinfo = null;
		List<BaseWebSiteInfo> webs = new ArrayList<>();
		for (BaseUnitWeb webId : list) {
			if (!"".equals(webId.getWebId())) {
				webinfo = webSiteInfoDao.findOne(webId.getWebId());
				webs.add(webinfo);
			}
		}
		String status = "";
		switch (info.getStatus()) {
		case 1:
			status = "等待初审";
			break;
		case 2:
			status = "初审通过";
			break;
		case 3:
			status = "初审未通过";
			break;
		case 4:
			status = "复审通过";
			break;
		case 5:
			status = "复审未通过";
			break;
		case 6:
			status = "管局审核";
			break;
		case 7:
			status = "管局未通过";
			break;
		case 8:
			status = "备案成功";
			break;
		default:
			status = "等待初审";
			break;
		}
		webexcel.setStatus(status);
		webexcel.setRecordCusName(unitinfo.getCusOrg());
		webexcel.setRecordNo(unitinfo.getRecordNo());
		for (BaseWebSiteInfo website : webs) {// 备案1.1版本调整为一次备案一个网站所有这个循环只有一次
			webexcel.setWebName(website.getWebName());
			webexcel.setWebRecordNo(website.getWebRecordNo());
			webexcel.setWebId("");
			webexcel.setServiceContent(recursiveReplace(website.getServiceContent()));
			webexcel.setWebLanguage(recursiveReplace(website.getWebLanguage()));
			webexcel.setDomainUrl(website.getDomainUrl());
			webexcel.setDomainName(website.getDomainName());
			webexcel.setWebRemark(website.getRemark());
			String webSpecial = "";
			if (website.getWebSpecial() != null) {
				switch (website.getWebSpecial()) {
				case 1:
					webSpecial = "新闻";
					break;
				case 2:
					webSpecial = "出版";
					break;
				case 3:
					webSpecial = "教育";
					break;
				case 4:
					webSpecial = "医疗保健";
					break;
				case 5:
					webSpecial = "药品和医疗器械";
					break;
				case 6:
					webSpecial = "文化";
					break;
				case 7:
					webSpecial = "广播电影电视节目";
					break;
				default:
					webSpecial = "新闻";
					break;
				}
			}
			webexcel.setWebSpecial(webSpecial);
			webexcel.setSpecialNo(website.getSpecialNo());
			webexcel.setSpecialFile("");
			webexcel.setWebDutyName(website.getWebDutyName());
			webexcel.setWebPhone(website.getPhone());
			webexcel.setWebDutyPhone(website.getDutyPhone());
			webexcel.setWebDutyEmail(website.getDutyEmail());
			webexcel.setWebMSN("");
			webexcel.setWebQQ(website.getDutyQQ());
			String webDutyCertificateType = "";
			switch (website.getDutyCertificateType()) {
			case 1:
				webDutyCertificateType = "身份证";
				break;
			case 2:
				webDutyCertificateType = "护照";
				break;
			case 3:
				webDutyCertificateType = "台胞证";
				break;
			case 4:
				webDutyCertificateType = "军官证";
				break;
			default:
				webDutyCertificateType = "身份证";
				break;
			}
			webexcel.setWebDutyCertificateType(webDutyCertificateType);
			webexcel.setWebDutyCertificateNo(website.getDutyCertificateNo());
			webexcel.setPassword(website.getWebPassword());
			webexcel.setAccessType("虚拟主机");
			List<BaseWebDataCenterIp> ipList = dcIPDao.find(" from BaseWebDataCenterIp where webId = ? ", website.getWebId());
            String ipPeriod = "";
            String webAddress = "";
            for(BaseWebDataCenterIp ip : ipList){
                ipPeriod += ip.getIp() + "-" + ip.getIp() + ";";
                if (null != ip.getDcId()) {
                    String dcname = ecmcDataCenterService.getProvinces(ip.getDcId());// 获取数据中心名称
                    webAddress += dcname + ";";
                }
            }
            webexcel.setWebAddress(webAddress);
			webexcel.setIPPeriod(ipPeriod);
		}

		listwebexcel.add(webexcel);
		return listwebexcel;
	}

	
	
}
