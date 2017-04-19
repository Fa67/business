package com.eayun.sms.ecmcservice.impl;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import net.sf.json.JSONObject;

import org.apache.commons.lang.ObjectUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.alibaba.dubbo.common.logger.Logger;
import com.alibaba.dubbo.common.logger.LoggerFactory;
import com.eayun.common.dao.QueryMap;
import com.eayun.common.dao.support.Page;
import com.eayun.common.util.StringUtil;
import com.eayun.mail.model.EcmcSmsModel;
import com.eayun.sms.dao.SMSDao;
import com.eayun.sms.ecmcservice.EcmcSmsService;
import com.eayun.sms.model.SMS;
import com.eayun.sms.service.SMSService;

@Transactional
@Service
public class EcmcSmsServiceImpl implements EcmcSmsService {
	private static final Logger log = LoggerFactory
			.getLogger(EcmcSmsServiceImpl.class);

	@Autowired
	private SMSDao smsDao;

	@Autowired
	private SMSService smsService;

	@SuppressWarnings({ "rawtypes", "unchecked" })
    @Override
	public Page getSmsList(Page page, Date beginTime, Date endTime,
			String mobile, String status, QueryMap queryMap) throws Exception {
		log.info("获取短信列表");
		List<Object> list = new ArrayList<Object>();
		StringBuffer sql = new StringBuffer();

		sql.append("select ");
		sql.append("	sms_id as id ");
		sql.append("	,sms_inserttime as insertTime ");
		sql.append("	,sms_updatetime as updateTime ");
		sql.append("	,sms_status as status ");
		sql.append("	,sms_cust as customerId ");
		sql.append("	,sms_proj as projectId ");
		sql.append("	,p.prj_name as projectName ");
		sql.append("	,sms_biz as biz ");
		sql.append("	,sms_sent as sent ");
		sql.append("	,sms_oversent as overSent ");
		sql.append("	,sms_detail as detail ");
		sql.append("from  ");
		sql.append("	sys_sms ");
		sql.append("left join ");
		sql.append("	cloud_project p on p.prj_id = sms_proj ");
		sql.append("where ");
		sql.append("	1=1 ");
		if (null != beginTime) {
			sql.append("and sms_updatetime >= ? ");
			list.add(beginTime);
		}
		if (null != endTime) {
			sql.append("and sms_updatetime <= ? ");
			list.add(endTime);
		}
		if (null != mobile && mobile != "") {
			sql.append("and sms_detail like ? ");
			list.add("%[%" + mobile + "%]%");
		}
		if (null != status && status != "") {
			if (!status.equals("--")) {
				if (status.equals("1")) {
					sql.append("and (sms_status = '1'");
					sql.append("or sms_status = '101')");
				} else {
					sql.append("and sms_status = ?");
					list.add(status);
				}
			} else {
				sql.append("and (sms_status = '0' ");
				sql.append("or sms_status = '-1' ");
				sql.append("or sms_status = '-2' ");
				sql.append("or sms_status = '-3' ");
				sql.append("or sms_status = '-4' ");
				sql.append("or sms_status = '-5' ");
				sql.append("or sms_status = '-6' ");
				sql.append("or sms_status = '-7' ");
				sql.append("or sms_status = '-8' ");
				sql.append("or sms_status = '-9' ");
				sql.append("or sms_status = '-10' ");
				sql.append("or sms_status = '-11' ");
				sql.append("or sms_status = '100')");
			}
		}
		sql.append(" order by sms_inserttime desc, sms_updatetime desc");
		page = smsDao
				.pagedNativeQuery(sql.toString(), queryMap, list.toArray());
		List smsList = (List) page.getResult();
		EcmcSmsModel sms = null;
		for (int i = 0; i < smsList.size(); i++) {
			Object[] objs = (Object[]) smsList.get(i);
			sms = new EcmcSmsModel();
			sms.setId(String.valueOf(objs[0]));
			sms.setInsertTime((Date) objs[1]);
			sms.setUpdateTime((Date) objs[2]);

			sms.setStatus(transferSmsStatus(String.valueOf(objs[3])));
			sms.setCustomerId(null != objs[4] ? String.valueOf(objs[4]) : "");
			sms.setProjectId(null != objs[5] ? String.valueOf(objs[5]) : "");
			sms.setProjectName(null != objs[6] ? String.valueOf(objs[6]) : "");
			sms.setBiz(null != objs[7] ? String.valueOf(objs[7]) : "");
			sms.setSent(Integer.valueOf(objs[8].toString()));
			sms.setOverSent(Integer.valueOf(objs[9].toString()));
			String detail = String.valueOf(objs[10]);
			/*
			 * JSONObject object = JSONObject.fromObject(sms.getDetail());
			 * String str = object.get("m").toString();
			 */
			sms.setMobilesList(getSmsMobilesList(detail));
			smsList.set(i, sms);
		}
		return page;
	}

	/**
	 * 短信状态转义接口
	 * 
	 * @param statusCode
	 * @return
	 */
	private String transferSmsStatus(String statusCode) {
		String status = new String();
		switch (statusCode) {
		case "000":
			status = "发送中";
			break;
		case "100":
			status = "项目配额不足不发送";
			break;
		case "101":
			status = "超配额已发送";
			break;
		case "1":
			status = "发送成功";
			break;
		case "0":
			status = "帐户格式不正确";
			break;
		case "-1":
			status = "服务器拒绝";
			break;
		case "-2":
			status = "密钥不正确";
			break;
		case "-3":
			status = "密钥已锁定";
			break;
		case "-4":
			status = "参数不正确";
			break;
		case "-5":
			status = "无此账户";
			break;
		case "-6":
			status = "账户已锁定或已过期";
			break;
		case "-7":
			status = "账户未开启接口发送";
			break;
		case "-8":
			status = "不可使用该通道组";
			break;
		case "-9":
			status = "账户余额不足";
			break;
		case "-10":
			status = "账户余额不足";
			break;
		case "-11":
			status = "扣费失败";
			break;
		default:
			status = "其他状态";
		}
		return status;
	}

	/**
	 * 通过短信内容后去短信发送的实际信息
	 * 
	 * @param detail
	 * @return
	 */
	private String getSmsDetail(String detail) {
		JSONObject object = JSONObject.fromObject(detail);
		return object.get("c").toString();
	}

	/**
	 * 通过短信内容获取短信发送的手机号码
	 * 
	 * @param detail
	 * @return
	 */
	private List<String> getSmsMobilesList(String detail) {
		JSONObject object = JSONObject.fromObject(detail);
		List<String> list = new ArrayList<String>();
		String str = object.get("m").toString().substring(1, object.get("m").toString().length() - 1);
		if (!StringUtil.isEmpty(str)) {
		    String[] array = str.split(",");
		    for (int i = 0; i < array.length; i++) {
		        if (array[i] != null && !array[i].equals("null")) {
		            list.add(array[i].substring(1, array[i].length() - 1));
		        } else {
		            list.add("无发送号码");
		        }
		    }
		}
		return list;
	}

	@Override
	public boolean createSms(SMS sms) throws Exception {
		return smsService.send(sms.getDetail(), sms.getMobilesList());
	}

	@SuppressWarnings("rawtypes")
    @Override
	public boolean resendSms(String id) throws Exception {
		StringBuffer sql = new StringBuffer();
		sql.append("select ");
		sql.append("	sms_id as id ");
		sql.append("	,sms_detail as detail ");
		sql.append("	,sms_cust as customerId ");
		sql.append("	,sms_proj as projectId ");
		sql.append("	,sms_biz as biz ");
		sql.append("from sys_sms ");
		sql.append("where ");
		sql.append("	sms_id = ? ");
		List<String> params = new ArrayList<>();
		params.add(id);
		List list = smsDao.createSQLNativeQuery(sql.toString(),
				params.toArray()).getResultList();
		if (list != null && list.size() > 0) {
			SMS sms = new SMS();
			Object[] objs = (Object[]) list.get(0);
			sms.setId(ObjectUtils.toString(objs[0]));
			String detail = ObjectUtils.toString(objs[1]);
			sms.setCustomerId(ObjectUtils.toString(objs[2]));
			sms.setProjectId(ObjectUtils.toString(objs[3]));
			sms.setBiz(ObjectUtils.toString(objs[4]));
			sms.setDetail(getSmsDetail(detail));
			sms.setMobilesList(getSmsMobilesList(detail));
			if ((sms.getBiz() != null && !sms.getBiz().equals(""))
					|| (sms.getCustomerId() != "null" && !sms.getCustomerId()
							.equals(""))
					|| (sms.getProjectId() != "null" && !sms.getProjectId()
							.equals(""))) {
				smsService.send(sms.getDetail(), sms.getMobilesList(),
						sms.getCustomerId(), sms.getProjectId(), sms.getBiz());
			} else {
				smsService.send(sms.getDetail(), sms.getMobilesList());
			}
			return true;
		}
		return false;
	}
}
