package com.eayun.unit.service.impl;

import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.ObjectUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.eayun.bean.UnitInfoWebsVoe;
import com.eayun.common.dao.QueryMap;
import com.eayun.common.dao.support.Page;
import com.eayun.common.util.BeanUtils;
import com.eayun.common.util.DateUtil;
import com.eayun.customer.dao.CustomerDao;
import com.eayun.customer.model.BaseCustomer;
import com.eayun.datacenter.ecmcservice.EcmcDataCenterService;
import com.eayun.file.service.FileService;
import com.eayun.notice.model.MessageUnitModel;
import com.eayun.notice.service.MessageCenterService;
import com.eayun.project.dao.CloudProjectDao;
import com.eayun.unit.dao.ApplyInfoDao;
import com.eayun.unit.dao.CloudAreaDao;
import com.eayun.unit.dao.UnitDao;
import com.eayun.unit.dao.UnitWebDao;
import com.eayun.unit.dao.WebDataCenterIpDao;
import com.eayun.unit.dao.WebSiteInfoDao;
import com.eayun.unit.model.ApplyWebs;
import com.eayun.unit.model.BaseApplyInfo;
import com.eayun.unit.model.BaseCloudArea;
import com.eayun.unit.model.BaseUnitInfo;
import com.eayun.unit.model.BaseUnitWeb;
import com.eayun.unit.model.BaseWebDataCenterIp;
import com.eayun.unit.model.BaseWebSiteInfo;
import com.eayun.unit.model.RecordMultipartFile;
import com.eayun.unit.model.WebSiteIP;
import com.eayun.unit.service.EcscRecordService;

/**
 * @author jingang.liu@eayun.com
 * @Dadte 2016年12月20日
 */
@Service
@Transactional
public class EcscRecordServiceImpl implements EcscRecordService {

	@Autowired
	private ApplyInfoDao applyDao;
	@Autowired
	private WebSiteInfoDao webDao;
	@Autowired
	private UnitWebDao uwDao;
	@Autowired
	private UnitDao unitDao;
	@Autowired
	private FileService fileservice;
	@Autowired
	private CloudAreaDao areaDao;
	@Autowired
	private EcmcDataCenterService ecmcDataCenterService;
	@Autowired
	private CloudProjectDao projectDao;
	@Autowired
	private CustomerDao cusDao;
	@Autowired
	private WebDataCenterIpDao dcIPDao;
	@Autowired
	private MessageCenterService messageCenterService;

	@Override
	public List<BaseCloudArea> getAreaList(String parentcode) {
		List<BaseCloudArea> areaList = new ArrayList<BaseCloudArea>();
		if (parentcode == null || "".equals(parentcode)) {
			areaList = areaDao.getParentArea();
		} else {
			areaList = areaDao.getArea(parentcode);
		}
		return areaList;
	}

	@Override
	public String getAreaName(String code) {
		BaseCloudArea county = areaDao.findOne(code);
		if (county == null)
			return null;
		String areaName = county.getCityName();
		if (county.getParentCode() != null) {
			BaseCloudArea city = areaDao.findOne(county.getParentCode());
			areaName = city.getCityName() + " " + areaName;
			if (city.getParentCode() != null) {
				BaseCloudArea area = areaDao.findOne(city.getParentCode());
				areaName = area.getCityName() + " " + areaName;
			}
		}
		return areaName;
	}

	@Override
	public Page getrecordList(Page page, String recordType, String status, String cusId, QueryMap querymap) {
		StringBuffer sql = new StringBuffer();
		int index = 0;
		Object[] args = new Object[3];
		sql.append("select a.apply_id,u.unit_id,u.head_name,u.unit_name,a.record_type,a.status,a.create_time ");
		sql.append(" from apply_info a ");
		sql.append(" left join unit_info u on u.unit_id = a.unit_id ");
		sql.append(" where 1=1 ");
		if (null != cusId && !"".equals(cusId)) {
			sql.append(" and u.cus_id = ? ");
			args[index] = cusId;
			index++;
		}
		if (null != status && !"".equals(status)) {
			sql.append(" and a.status = ? ");
			args[index] = status;
			index++;
		}
		if (null != recordType && !"".equals(recordType)) {
			sql.append(" and a.record_type = ? ");
			args[index] = recordType;
			index++;
		}
		Object[] params = new Object[index];
		System.arraycopy(args, 0, params, 0, index);
		sql.append(" group by a.apply_id order by a.create_time desc ");
		page = applyDao.pagedNativeQuery(sql.toString(), querymap, params);
		List newlist = (List) page.getResult();
		Object[] objs = null;
		ApplyWebs applywebs = null;

		StringBuffer uawsql = new StringBuffer();
		uawsql.append(" select uaw.web_id from unit_apply_web uaw where uaw.apply_id = ? ");
		List<WebSiteIP> websiteList = null;
		List<BaseWebDataCenterIp> ipList = null;
		WebSiteIP wsIP = null;
		BaseWebSiteInfo WebSite = null;
		int size = newlist.size();
		for (int i = 0; i < size; i++) {
			objs = (Object[]) newlist.get(i);
			applywebs = new ApplyWebs();
			applywebs.setApplyId(ObjectUtils.toString(objs[0]));
			applywebs.setUnitId(ObjectUtils.toString(objs[1]));
			applywebs.setHeadName(ObjectUtils.toString(objs[2]));
			applywebs.setUnitName(ObjectUtils.toString(objs[3]));
			applywebs.setRecordType(Integer.parseInt(ObjectUtils.toString(objs[4])));
			applywebs.setStatus(Integer.parseInt(ObjectUtils.toString(objs[5])));
			applywebs.setCreateTime(((Date) objs[6]));
			List<String> listwebid = uwDao.createSQLNativeQuery(uawsql.toString(), ObjectUtils.toString(objs[0]))
					.getResultList();
			websiteList = new ArrayList<WebSiteIP>();
			for (String webid : listwebid) {
				WebSite = webDao.findOne(webid);
				wsIP = new WebSiteIP();
				BeanUtils.copyPropertiesByModel(wsIP, WebSite);
				ipList = dcIPDao.find(" from BaseWebDataCenterIp where webId = ? ", webid);
				wsIP.setIpList(ipList);
				websiteList.add(wsIP);
			}
			applywebs.setWebsiteList(websiteList);
			newlist.set(i, applywebs);
		}
		return page;
	}

	@Override
	public ApplyWebs getApplyOneDetail(String applyId) throws Exception {
		ApplyWebs applywebs = new ApplyWebs();
		BaseApplyInfo apply = applyDao.findOne(applyId);
		if (apply == null) {
			return null;
		}
		applywebs.setApplyId(apply.getApplyId());
		applywebs.setUnitId(apply.getUnitId());
		applywebs.setStatus(apply.getStatus());
		applywebs.setRecordType(apply.getRecordType());
		applywebs.setCreateTime(apply.getCreateTime());
		applywebs.setReturnType(apply.getReturnType());

		BaseUnitInfo unit = unitDao.findOne(apply.getUnitId());// 当前申请的主体信息
		String areaName = getAreaName(unit.getUnitArea());// 查询区域名称
		applywebs.setUnitName(unit.getUnitName());
		applywebs.setHeadName(unit.getHeadName());
		applywebs.setUnitNature(unit.getUnitNature());
		applywebs.setUnitArea(areaName);
		applywebs.setUnitAddress(unit.getUnitAddress());
		applywebs.setCertificateType(unit.getCertificateType());
		applywebs.setCertificateNo(unit.getCertificateNo());
		applywebs.setCertificateAddress(unit.getCertificateAddress());
		applywebs.setDutyName(unit.getDutyName());
		applywebs.setDutyCertificateType(unit.getDutyCertificateType());
		applywebs.setDutyCertificateNo(unit.getDutyCertificateNo());
		applywebs.setPhone(unit.getPhone());
		applywebs.setDutyPhone(unit.getDutyPhone());
		applywebs.setDutyEmail(unit.getDutyEmail());
		applywebs.setDutyQQ(unit.getDutyQQ());
		applywebs.setRemark(unit.getRemark());
		applywebs.setCreateTime(unit.getCreateTime());
		applywebs.setUpdateTime(unit.getUpdateTime());
		applywebs.setRecordType(unit.getRecordType());
		applywebs.setRecordNo(unit.getRecordNo());
		applywebs.setRecordPassWord(unit.getRecordPassWord());
		applywebs.setCusId(unit.getCusId());
		applywebs.setCusOrg(unit.getCusOrg());
		applywebs.setBusinessFileId(unit.getBusinessFileId());
		applywebs.setDutyFileId(unit.getDutyFileId());

		StringBuffer uawsql = new StringBuffer();
		uawsql.append(" select uaw.web_id from unit_apply_web uaw where uaw.apply_id = ? ");
		List<WebSiteIP> websiteList = new ArrayList<WebSiteIP>();
	
        List<BaseWebDataCenterIp> ipList = null;
        List<BaseWebDataCenterIp> newipList = null;
        BaseWebDataCenterIp dcip= null;
        WebSiteIP wsIP = null;
		BaseWebSiteInfo WebSite = null;
		List<String> listwebid = uwDao.createSQLNativeQuery(uawsql.toString(), applyId).getResultList();
		String dcname = "";
		for (String webid : listwebid) {
			WebSite = webDao.findOne(webid);
			wsIP = new WebSiteIP();
            BeanUtils.copyPropertiesByModel(wsIP, WebSite);
            ipList = dcIPDao.find(" from BaseWebDataCenterIp where webId = ? ", webid);
            newipList = new ArrayList<>();
            for(BaseWebDataCenterIp ip : ipList){
                dcname = ecmcDataCenterService.getdatacenterName(ip.getDcId());
                dcip = (BaseWebDataCenterIp)ip.clone();
                dcip.setDcId(dcname);
                newipList.add(dcip);
            }
            wsIP.setIpList(newipList);
			websiteList.add(wsIP);
		}
		applywebs.setWebsiteList(websiteList);// 当前申请的网站集合
		return applywebs;
	}

	@Override
	public int selectCount(String cusId, Integer status) {
		StringBuffer sql = new StringBuffer();
		int index = 0;
		Object[] args = new Object[2];
		sql.append("select count(a.apply_id) ");
		sql.append(" from apply_info a ");
		sql.append(" left join unit_info u on u.unit_id = a.unit_id ");
		sql.append(" where 1=1 ");
		if (null != cusId && !"".equals(cusId)) {
			sql.append(" and u.cus_id = ? ");
			args[index] = cusId;
			index++;
		}
		if (null != status && status > 0) {
			sql.append(" and a.status = ? ");
			args[index] = status;
			index++;
		}
		Object[] params = new Object[index];
		System.arraycopy(args, 0, params, 0, index);
		List list = applyDao.createSQLNativeQuery(sql.toString(), params).getResultList();
		if (list == null || list.size() <= 0) {
			return 0;
		}
		return Integer.parseInt(list.get(0).toString());
	}

	@Override
	public BaseApplyInfo addfirstrecord(Map<String, Object> maps) throws Exception {
		List<MessageUnitModel> listmodel = new ArrayList<MessageUnitModel>();
		MessageUnitModel model = new MessageUnitModel();
		BaseUnitWeb applyweb = null;
		BaseUnitInfo unitinfo = new BaseUnitInfo();
		unitinfo.setUnitName((String) maps.get("unitName"));
		unitinfo.setUnitNature(
				Integer.parseInt((maps.get("unitNature") == null ? "1" : (String) maps.get("unitNature"))));
		unitinfo.setHeadName((String) maps.get("headName"));
		unitinfo.setUnitArea((String) maps.get("unitArea"));
		unitinfo.setUnitAddress((String) maps.get("unitAddress"));
		unitinfo.setCertificateType(
				Integer.parseInt(maps.get("certificateType") == null ? "1" : (String) maps.get("certificateType")));
		unitinfo.setCertificateNo((String) maps.get("certificateNo"));
		unitinfo.setCertificateAddress((String) maps.get("certificateAddress"));
		unitinfo.setDutyName((String) maps.get("dutyName"));
		unitinfo.setDutyCertificateType(Integer
				.parseInt(maps.get("dutyCertificateType") == null ? "1" : (String) maps.get("dutyCertificateType")));
		unitinfo.setDutyCertificateNo((String) maps.get("dutyCertificateNo"));
		unitinfo.setPhone((String) maps.get("phone"));
		unitinfo.setDutyPhone((String) maps.get("dutyPhone"));
		unitinfo.setDutyEmail((String) maps.get("dutyEmail"));
		unitinfo.setDutyQQ((String) maps.get("dutyQQ"));
		unitinfo.setRemark((String) maps.get("remark"));
		unitinfo.setCreateTime(new Date());
		unitinfo.setUpdateTime(null);
		unitinfo.setRecordType(1);// 首次备案
		unitinfo.setRecordNo((String) maps.get("recordNo"));
		unitinfo.setCusId((String) maps.get("cusId"));
		unitinfo.setCusOrg((String) maps.get("cusOrg"));
		unitinfo.setDutyFileId((String) maps.get("dutyFileId"));// 主体负责人证件照
		unitinfo.setBusinessFileId((String) maps.get("businessFileId"));// 营业执照
		unitDao.saveEntity(unitinfo);// 添加主体信息

		BaseApplyInfo apply = new BaseApplyInfo();
		apply.setUnitId(unitinfo.getUnitId());
		apply.setReturnType(null);// 添加时管局返回状态为空
		apply.setStatus(1);// 待初审
		apply.setRecordType(1);// 首次备案
		apply.setCreateTime(new Date());
		applyDao.saveEntity(apply);// 添加备案申请信息
		String messagwebno = "";
		List<Map<String, Object>> listmap = (List<Map<String, Object>>) maps.get("webList");
		BaseWebSiteInfo website = null;
		BaseWebDataCenterIp dcIP = null;
		List<Map<String, Object>> ipsmap = null;
		for (Map<String, Object> map : listmap) {
			website = new BaseWebSiteInfo();
			BeanUtils.mapToBean(website, map);
			website.setUnitId(unitinfo.getUnitId());
			website.setProgress(1);// 待初审
			website.setIsChange("1");
			website.setCrateTime(new Date());
			webDao.saveEntity(website);// 添加网站信息
			applyweb = new BaseUnitWeb();
			applyweb.setApplyId(apply.getApplyId());
			applyweb.setWebId(website.getWebId());
			uwDao.saveEntity(applyweb);// 添加申请表关联的网站信息
			String webno = website.getWebRecordNo();
			if (!"".equals(webno) && null != webno) {
				messagwebno += webno + ";<br>";
			}

			ipsmap = (List<Map<String, Object>>) map.get("ipList");
			for (Map<String, Object> ips : ipsmap) {
				dcIP = new BaseWebDataCenterIp();
				BeanUtils.mapToBean(dcIP, ips);
				dcIP.setWebId(website.getWebId());
				dcIPDao.save(dcIP);// 添加网站的IP集合
			}
		}
		model.setOrgName(unitinfo.getCusOrg());
		model.setUnitName(unitinfo.getUnitName());
		model.setUnitFuzeName(unitinfo.getDutyName());
		model.setWebNo(messagwebno);
		model.setRecordType("首次备案");
		model.setTime(DateUtil.dateToString(unitinfo.getCreateTime()));
		listmodel.add(model);
		messageCenterService.newAddUnitTomail(listmodel);// 发送邮件
		return apply;
	}

	@Override
	public void deletefirstrecord(String id) throws Exception {
		BaseApplyInfo apply = applyDao.findOne(id);
		StringBuffer deletewebsql = new StringBuffer();
		deletewebsql.append(" delete from website_info where unit_id = ? ");
		webDao.createSQLNativeQuery(deletewebsql.toString(), apply.getUnitId()).executeUpdate();// 删除网站
		unitDao.delete(apply.getUnitId());// 删除主体信息

		uwDao.createSQLNativeQuery(" delete from unit_apply_web where apply_id = ? ", apply.getUnitId())
				.executeUpdate();// 删除申请表关联的网站信息
		applyDao.delete(apply);// 删除申请表
	}

	@Override
	public List<Map<String, String>> uploadRecordFile(List<RecordMultipartFile> multipartFiles, String userId) {
		List<Map<String, String>> fileids = new ArrayList<Map<String, String>>();
		RecordMultipartFile file = null;
		try {
			Map<String, String> map = null;
			for (int i = 0; i < multipartFiles.size(); i++) {
				file = new RecordMultipartFile();
				file = multipartFiles.get(i);
				// 调用上传接口上传附件
				String fileId = fileservice.uploadFile(file.getMultipartfile(), userId);
				map = new HashMap<String, String>();
				map.put(file.getType(), fileId);// 对应的文件ID
				fileids.add(map);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return fileids;
	}

	@Override
	public InputStream downloadFile(String fileId) throws Exception {
		return fileservice.downloadFile(fileId);
	}

	@Override
	public boolean deleteFile(String fileId) throws Exception {
		return fileservice.deleteFile(fileId);
	}

	public static String encode(String enc) {
		byte[] b = enc.getBytes(charset);
		for (int i = 0, size = b.length; i < size; i++) {
			for (byte keyBytes0 : keyBytes) {
				b[i] = (byte) (b[i] ^ keyBytes0);
			}
		}
		return new String(b);
	}

	private static final String key0 = "FECOI()*&<MNCXZPKL";
	private static final Charset charset = Charset.forName("UTF-8");
	private static byte[] keyBytes = key0.getBytes(charset);

	public static String decode(String dec) {
		byte[] e = dec.getBytes(charset);
		byte[] dee = e;
		for (int i = 0, size = e.length; i < size; i++) {
			for (byte keyBytes0 : keyBytes) {
				e[i] = (byte) (dee[i] ^ keyBytes0);
			}
		}
		return new String(e);
	}

	public static void main(String[] args) {
		String s = "4545 45";
		String enc = encode(s);
		String dec = decode(enc);
		System.out.println(enc);
		System.out.println(dec);
	}

	@Override
	public BaseApplyInfo addAccessRecord(Map<String, Object> maps) throws Exception {
		List<MessageUnitModel> listmodel = new ArrayList<MessageUnitModel>();
		MessageUnitModel model = new MessageUnitModel();
		BaseUnitInfo unitinfo = new BaseUnitInfo();
		unitinfo.setUnitName((String) maps.get("unitName"));
		unitinfo.setUnitNature(
				Integer.parseInt((maps.get("unitNature") == null ? "1" : (String) maps.get("unitNature"))));
		unitinfo.setHeadName((String) maps.get("headName"));
		unitinfo.setUnitArea((String) maps.get("unitArea"));
		unitinfo.setUnitAddress((String) maps.get("unitAddress"));
		unitinfo.setCertificateType(
				Integer.parseInt(maps.get("certificateType") == null ? "1" : (String) maps.get("certificateType")));
		unitinfo.setCertificateNo((String) maps.get("certificateNo"));
		unitinfo.setCertificateAddress((String) maps.get("certificateAddress"));
		unitinfo.setDutyName((String) maps.get("dutyName"));
		unitinfo.setDutyCertificateType(Integer
				.parseInt(maps.get("dutyCertificateType") == null ? "1" : (String) maps.get("dutyCertificateType")));
		unitinfo.setDutyCertificateNo((String) maps.get("dutyCertificateNo"));
		unitinfo.setPhone((String) maps.get("phone"));
		unitinfo.setDutyPhone((String) maps.get("dutyPhone"));
		unitinfo.setDutyEmail((String) maps.get("dutyEmail"));
		unitinfo.setDutyQQ((String) maps.get("dutyQQ"));
		unitinfo.setRemark((String) maps.get("remark"));
		unitinfo.setCreateTime(new Date());
		unitinfo.setUpdateTime(null);
		unitinfo.setRecordType(3);// 新增接入
		unitinfo.setCusId((String) maps.get("cusId"));
		unitinfo.setCusOrg((String) maps.get("cusOrg"));
		unitinfo.setDutyFileId((String) maps.get("dutyFileId"));// 主体负责人证件照
		unitinfo.setBusinessFileId((String) maps.get("businessFileId"));// 营业执照
		unitinfo.setRecordNo((String) maps.get("recordNo"));// 备案号
		unitinfo.setRecordPassWord((String) maps.get("recordPassWord"));// 管局密码
		unitDao.saveEntity(unitinfo);// 添加主体信息

		BaseApplyInfo apply = new BaseApplyInfo();
		apply.setUnitId(unitinfo.getUnitId());
		apply.setReturnType(null);// 添加时管局返回状态为空
		apply.setStatus(1);// 待初审
		apply.setRecordType(3);// 新增接入
		apply.setCreateTime(new Date());
		applyDao.saveEntity(apply);// 添加备案申请信息

		BaseUnitWeb applyweb = null;
		List<Map<String, Object>> listmap = (List<Map<String, Object>>) maps.get("webList");// 只有web_service和service_ip和dc_id
		BaseWebSiteInfo website = null;
		BaseWebDataCenterIp dcIP = null;
		List<Map<String, Object>> ipsmap = null;
		String messagwebno = "";
		for (Map<String, Object> map : listmap) {
			website = new BaseWebSiteInfo();
			BeanUtils.mapToBean(website, map);
			website.setUnitId(unitinfo.getUnitId());
			website.setProgress(1);// 待初审
			website.setIsChange("1");
			website.setCrateTime(new Date());
			webDao.saveEntity(website);// 添加网站信息
			applyweb = new BaseUnitWeb();
			applyweb.setApplyId(apply.getApplyId());
			applyweb.setWebId(website.getWebId());
			uwDao.saveEntity(applyweb);// 添加申请表关联的网站信息

			ipsmap = (List<Map<String, Object>>) map.get("ipList");
			for (Map<String, Object> ips : ipsmap) {
				dcIP = new BaseWebDataCenterIp();
				BeanUtils.mapToBean(dcIP, ips);
				dcIP.setWebId(website.getWebId());
				dcIPDao.save(dcIP);// 添加网站的IP集合
			}
			String webno = website.getWebRecordNo();
			if (!"".equals(webno)) {
				messagwebno += webno + ";<br>";
			}
		}
		model.setOrgName(unitinfo.getCusOrg());
		model.setUnitName(unitinfo.getUnitName());
		model.setUnitFuzeName(unitinfo.getDutyName());
		model.setWebNo(messagwebno);
		model.setRecordType("新增接入");
		model.setTime(DateUtil.dateToString(new Date()));
		listmodel.add(model);
		messageCenterService.newAddUnitTomail(listmodel);// 发送邮件
		return apply;
	}

	@Override
	public Page getrecordListapply(String cusid, QueryMap qm) throws Exception {
		// List<BaseUnitInfo> infolist=unitDao.getUnitInfoList(cusid);
		List<UnitInfoWebsVoe> returnvoe = new ArrayList<UnitInfoWebsVoe>();
		List<BaseWebSiteInfo> weblist = null;
		//List<WebSiteIP> newweblist = null;
		List<BaseWebDataCenterIp> newipList = null;
		List<BaseWebDataCenterIp> ipList = null;
		List<WebSiteIP> websiteList = null;
		StringBuffer hql = new StringBuffer();
		hql.append("from BaseUnitInfo where cusId=? and recordNo is not null order by createTime desc");
		List list = new ArrayList();
		list.add(cusid);
		Page page = unitDao.pagedQuery(hql.toString(), qm, list.toArray());
		List<BaseUnitInfo> infolist = (List<BaseUnitInfo>) page.getResult();

		String dcname = "";
		BaseWebDataCenterIp dcip = null;
		for (int in = 0; in < infolist.size(); in++) {
			UnitInfoWebsVoe model = new UnitInfoWebsVoe();
			BeanUtils.copyPropertiesByModel(model, infolist.get(in));
			weblist = webDao.getByUnitId(infolist.get(in).getUnitId());
			
			for (BaseWebSiteInfo web : weblist) {
				dcname = ecmcDataCenterService.getdatacenterName(web.getDcID());
				BaseWebSiteInfo newweb = (BaseWebSiteInfo) web.clone();
				WebSiteIP wsIP = new WebSiteIP();
				BeanUtils.copyPropertiesByModel(wsIP, newweb);
				ipList = dcIPDao.find(" from BaseWebDataCenterIp where webId = ? ", newweb.getWebId());
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
			}
			String areaName = getAreaName(infolist.get(in).getUnitArea());// 查询区域名称
			model.setUnitArea(areaName);
			model.setWebs(websiteList);
			returnvoe.add(model);
		}

		page.setResult(returnvoe);

		return page;
	}

	@Override
	public List<String> getFloatIp(String cusId, String resource_type, String dc_Id) throws Exception {
		StringBuffer sql = new StringBuffer();
		List<String> params = new ArrayList<String>();
		params.add(cusId);
		params.add(resource_type);
		params.add(dc_Id);
		sql.append(" SELECT f.flo_ip FROM cloud_floatip f ");
		if ("vm".equals(resource_type)) {
			sql.append(" LEFT JOIN cloud_vm vm ON vm.vm_id = f.resource_id ");// 关联云主机
		} else {
			sql.append(" LEFT JOIN cloud_ldpool ld ON ld.pool_id = f.resource_id ");// 关联负载均衡
		}
		sql.append(" WHERE f.is_deleted = '0' AND f.charge_state = 0 AND f.prj_id IN ");// 公网IP没有被删除且正常状态
		sql.append(" ( SELECT prj_id FROM cloud_project WHERE customer_id = ? ) ");// 公网IP的想项目属于当前客户
		//select w.service_ip from website_info w left join unit_apply_web uaw on uaw.web_id = w.web_id left join apply_info a on a.apply_id = uaw.apply_id WHERE a.`status`<>3 and w.service_ip is not null
		sql.append(" AND f.flo_ip not in (select w.ip from unit_web_datacenter w left join unit_apply_web uaw on uaw.web_id = w.web_id left join apply_info a on a.apply_id = uaw.apply_id WHERE a.`status`<>3 and (w.ip is not null and w.ip<>'')) ");// 排除已经备案的Ip但不包含备案初审未通过的IP
		sql.append(" AND f.is_visable = '1' AND f.resource_type =? AND f.dc_Id = ? ");// 公网IP有效和关联的类型和数据中心
		if ("vm".equals(resource_type)) {
			sql.append(" AND vm.charge_state = 0 ");// 云主机有效
		} else {
			sql.append(" AND ld.charge_state = 0 ");// 负载均衡有效
		}
		List<String> ips = projectDao.createSQLNativeQuery(sql.toString(), params.toArray()).getResultList();
		return ips;
	}

	@Override
	public String getCusEmail(String cusId) throws Exception {
		BaseCustomer cus = cusDao.findOne(cusId);
		if (cus != null) {
			return cus.getCusEmail();
		} else {
			return null;
		}
	}

	@Override
	public BaseUnitInfo getUnitOneDetail(String unitId) throws Exception {

		return unitDao.getUnitInfoByid(unitId);
	}

	@Override
	public boolean deleteWebsiteByIP(String IP) throws Exception {
		StringBuffer hql = new StringBuffer();
		hql.append("delete from unit_web_datacenter where ip = ? ");
		int result = webDao.createSQLNativeQuery(hql.toString(), IP).executeUpdate();// 删除网站ip
		if (result > 0) {
			return true;
		}
		return false;
	}

	@Override
	public List<BaseWebDataCenterIp> getWebDataCenterIp(String IP) {
		return dcIPDao.getByServiceIP(IP);
	}

}
