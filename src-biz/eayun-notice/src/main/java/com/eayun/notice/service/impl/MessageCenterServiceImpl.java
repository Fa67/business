package com.eayun.notice.service.impl;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import com.eayun.common.util.StringUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.eayun.common.constant.EcmcRoleIds;
import com.eayun.common.constant.TransType;
import com.eayun.common.exception.AppException;
import com.eayun.common.tools.DictUtil;
import com.eayun.common.util.BeanUtils;
import com.eayun.common.util.DateUtil;
import com.eayun.customer.filter.SystemConfig;
import com.eayun.customer.model.BaseUser;
import com.eayun.customer.model.Customer;
import com.eayun.customer.serivce.CustomerService;
import com.eayun.mail.service.MailService;
import com.eayun.news.ecmcnewsservice.EcmcNewsService;
import com.eayun.news.model.BaseNewsSend;
import com.eayun.notice.dao.NoticeDao;
import com.eayun.notice.model.MessageCloudDataBaseDeletedFailModel;
import com.eayun.notice.model.MessageCloudDataBaseRollBackModel;
import com.eayun.notice.model.MessageEcscToMailEcmc;
import com.eayun.notice.model.MessageExpireRenewResourcesModel;
import com.eayun.notice.model.MessageOperateModel;
import com.eayun.notice.model.MessageOrderResourceNotice;
import com.eayun.notice.model.MessagePayAsYouGoResourcesStopModel;
import com.eayun.notice.model.MessagePayUpperLimitResourcesStopModel;
import com.eayun.notice.model.MessageResourcesExpiredModel;
import com.eayun.notice.model.MessageResourcesStopModel;
import com.eayun.notice.model.MessageStackSynFailResour;
import com.eayun.notice.model.MessageUserResour;
import com.eayun.notice.model.MessageUnitModel;
import com.eayun.notice.service.MessageCenterService;
import com.eayun.order.dao.OrderDao;
import com.eayun.order.model.BaseOrder;
import com.eayun.order.model.Order;
import com.eayun.order.service.OrderService;
import com.eayun.project.service.ProjectService;
import com.eayun.sms.service.SMSService;
import com.eayun.sys.model.SysDataTree;
import com.eayun.virtualization.model.CloudProject;

@Service
@Transactional
public class MessageCenterServiceImpl implements MessageCenterService {

	private static final Logger log = LoggerFactory.getLogger(MessageCenterServiceImpl.class);

	private static Map<String, String> urlMap = null;

	private static StringBuffer ecscMailHtml = null;
	private static StringBuffer ecmcMailHtml = null;
	private static StringBuffer OpMailHtml = null;

	public static Map<String, String> getUrlMap() {
		if (urlMap == null) {
			SystemConfig xml = new SystemConfig();
			urlMap = xml.findNodeMap();
		}
		return urlMap;
	}

	@Autowired
	private EcmcNewsService ecmcNewsService;
	@Autowired
	private NoticeDao noticeDao;
	@Autowired
	private SMSService SMSService;
	@Autowired
	private MailService mailService;

	@Autowired
	private OrderService orderSerive;
	@Autowired
	private OrderDao orderDao;
	@Autowired
	private CustomerService userservice;
	@Autowired
	private ProjectService projectService;

	@Override
	public void expireRenewMessage(List<MessageExpireRenewResourcesModel> resourList, String cusId)
			throws AppException {
		BaseNewsSend news = new BaseNewsSend();
		String tr = "<tr><th width='20%' style='padding-left:10px;'>资源类型</th> <th width='50%'>资源名称</th> <th width='30%'>资源到期时间</th></tr>";
		String td = "<tr><td style='padding-left:10px;'>resourcesType</td><td >resourcesName</td> <td >expireDate</td></tr>";
		String tds = "<tr><td >resourcesType</td><td >resourcesName</td> <td >expireDate</td></tr>";
		StringBuffer bs = new StringBuffer("");
		String strContent = null;
		String title = "资源到期续费通知";
		try {
			strContent = String.valueOf(getHtmlEmail());
		} catch (Exception e1) {
			log.error(e1.getMessage(), e1);
			throw new AppException("获取邮件页面失败");
		}
		for (int i = 0; i < resourList.size(); i++) {
			MessageExpireRenewResourcesModel model = resourList.get(i);
			String resourcesType = model.getResourcesType();
			String resourcesName = model.getResourcesName();
			Date expireDate = model.getExpireDate();
			td = td.replaceAll("resourcesType", resourcesType);
			td = td.replaceAll("resourcesName",
					resourcesName.length() > 20 ? resourcesName.substring(0, 17) + "..." : resourcesName);
			td = td.replaceAll("expireDate", DateUtil.dateToString(expireDate));

			bs.append(td);
			td = tds;
		}
		strContent = strContent.replace("{trcontent}", tr);
		strContent = strContent.replace("{content}", bs.toString());
		// 查询邮件地址，短信地址语句
		List<BaseUser> list = noticeDao.getPhoneandEmail(true, cusId);

		List<String> mobiles = new ArrayList<String>();
		List<String> mails = new ArrayList<String>();
		String userstr = "";
		for (int i = 0; i < list.size(); i++) {
			BaseUser user = list.get(i);
			userstr = user.getUserPerson();
			if (user.getIsMailValid()) {
				mails.add(user.getUserEmail());
			}
			if (user.getIsPhoneValid()) {
				mobiles.add(user.getUserPhone());
			}

		}

		strContent = strContent.replace("{userName}", userstr);
		strContent = strContent.replace("{tablecontent}", "欢迎您使用易云公有云服务，<br>您有资源即将到期，为确保正常使用，请及时续费。");

		news.setSendDate(new Date());// 当前时间
		news.setSendPerson("易云公有云运营团队");
		news.setNewsTitle(title);
		news.setMemo("<p class='(ey-font-size-bigger)' >您有资源即将到期，为确保正常使用，请及时续费。</p>"
				+ "<table class='ey-table  ey-table-auto ' style='margin-top:30px' > <thead><tr>" + tr
				+ "</tr></thead><tbody><tr>" + bs + "</tr></tbody></table>");
		news.setRecType("2");// 客户下的超级管理员
		news.setIsSended("2");// 设置发送状态 2未发
		news.setSended(1);
		news.setCusId(cusId);
		news.setIs_syssend("1");
		ecmcNewsService.save(news);

		try {
			if (mobiles.size() != 0) {
				SMSService.send("尊敬的客户：您有[" + resourList.size() + "]个资源即将到期，为确保正常使用，请及时续费，如有问题请致电400-606-6396。",
						mobiles);// 短信

			}
			if (mails.size() != 0) {
				mailService.send("【易云】" + title, strContent, mails);// 邮件

			}
		} catch (Exception e) {
			throw new AppException(e.toString());
		}

	}

	@Override
	public void resourExpiredMessage(List<MessageResourcesExpiredModel> resourList, String cusid) throws AppException {
		BaseNewsSend news = new BaseNewsSend();
		String tr = "<tr><th width='20%'>资源类型</th> <th width='50%'>资源名称</th> <th width='30%'>资源停用时间</th></tr>";
		String td = "<tr><td >resourcesType</td><td >resourcesName</td> <td >expireDate</td></tr>";
		String tds = "<tr><td >resourcesType</td><td >resourcesName</td> <td >expireDate</td></tr>";
		StringBuffer bs = new StringBuffer("");
		String strContent = null;
		String title = "资源到期通知";
		try {
			strContent = String.valueOf(getHtmlEmail());
		} catch (Exception e1) {
			log.error(e1.getMessage(), e1);
			throw new AppException("获取邮件页面失败");
		}
		String stopdate = null;
		for (int i = 0; i < resourList.size(); i++) {
			MessageResourcesExpiredModel model = resourList.get(i);
			String resourcesType = model.getResourcesType();
			String resourcesName = model.getResourcesName();
			Date expireDate = model.getExpireDate();
			td = td.replaceAll("resourcesType", resourcesType);
			td = td.replaceAll("resourcesName",
					resourcesName.length() > 20 ? resourcesName.substring(0, 17) + "..." : resourcesName);
			td = td.replaceAll("expireDate", DateUtil.dateToString(expireDate));
			stopdate = DateUtil.dateToString(expireDate);

			bs.append(td);
			td = tds;
		}
		strContent = strContent.replace("{trcontent}", tr);
		strContent = strContent.replace("{content}", bs.toString());
		// 查询邮件地址，短信地址语句
		List<BaseUser> list = noticeDao.getPhoneandEmail(true, cusid);

		List<String> mobiles = new ArrayList<String>();
		List<String> mails = new ArrayList<String>();
		String userstr = "";
		for (int i = 0; i < list.size(); i++) {
			BaseUser user = list.get(i);
			userstr = user.getUserPerson();
			if (user.getIsMailValid()) {
				mails.add(user.getUserEmail());
			}
			if (user.getIsPhoneValid()) {
				mobiles.add(user.getUserPhone());
			}
		}

		strContent = strContent.replace("{userName}", userstr);
		strContent = strContent.replace("{tablecontent}",
				"欢迎您使用易云公有云服务，<br>您有资源已到期，即将被停用，目前您还可以继续使用，但部分操作不再支持，为保证您的正常使用，请及时续费，否则资源将于" + stopdate + "被停用。");

		news.setSendDate(new Date());// 当前时间
		news.setSendPerson("易云公有云运营团队");
		news.setNewsTitle(title);
		news.setMemo("<p class='(ey-font-size-bigger)'>您有资源已到期，即将被停用，目前您还可以继续使用，但部分操作不再支持，为保证您的正常使用，请及时续费，否则资源将于"
				+ stopdate + "被停用。</p>" + "<table class='ey-table  ey-table-auto' style='margin-top:30px'> <thead><tr>"
				+ tr + "</tr></thead><tbody><tr>" + bs + "</tr></tbody></table>");
		news.setRecType("2");// 客户下的管理员
		news.setIsSended("2");// 设置发送状态 2未发
		news.setSended(1);
		news.setCusId(cusid);
		news.setIs_syssend("1");
		ecmcNewsService.save(news);
		MessageResourcesExpiredModel model = resourList.get(0);
		String sms = "尊敬的客户：您有[" + resourList.size() + "]个资源因到期未续费将于" + DateUtil.dateToString(model.getExpireDate())
				+ "被停用，为确保正常使用，请及时续费，如有问题请致电400-606-6396。";

		try {
			if (mobiles.size() != 0) {
				SMSService.send(sms, mobiles);// 短信

			}
			if (mails.size() != 0) {

				mailService.send("【易云】" + title, strContent, mails);// 邮件
			}
		} catch (Exception e) {
			throw new AppException(e.toString());
		}

	}

	@Override
	public void resourStopMessage(List<MessageResourcesStopModel> resourList, String cusid) throws AppException {
		BaseNewsSend news = new BaseNewsSend();
		String tr = "<tr><th width='20%' style='padding-left:10px;'>资源类型</th> <th width='50%'>资源名称</th> <th width='30%'>资源停用时间</th></tr>";
		String td = "<tr><td style='padding-left:10px;'>resourcesType</td><td >resourcesName</td> <td >expireDate</td></tr>";
		String tds = "<tr><td style='padding-left:10px;'>resourcesType</td><td >resourcesName</td> <td >expireDate</td></tr>";
		StringBuffer bs = new StringBuffer("");
		String strContent = null;
		String title = "资源到期停用通知";
		try {
			strContent = String.valueOf(getHtmlEmail());
		} catch (Exception e1) {
			log.error(e1.getMessage(), e1);
			throw new AppException("获取邮件页面失败");
		}
		for (int i = 0; i < resourList.size(); i++) {
			MessageResourcesStopModel model = resourList.get(i);
			String resourcesType = model.getResourcesType();
			String resourcesName = model.getResourcesName();
			Date expireDate = model.getExpireDate();
			td = td.replaceAll("resourcesType", resourcesType);
			td = td.replaceAll("resourcesName",
					resourcesName.length() > 20 ? resourcesName.substring(0, 17) + "..." : resourcesName);
			td = td.replaceAll("expireDate", DateUtil.dateToString(expireDate));
			bs.append(td);
			td = tds;
		}
		strContent = strContent.replace("{trcontent}", tr);
		strContent = strContent.replace("{content}", bs.toString());
		// 查询邮件地址，短信地址语句
		List<BaseUser> list = noticeDao.getPhoneandEmail(true, cusid);

		List<String> mobiles = new ArrayList<String>();
		List<String> mails = new ArrayList<String>();
		String userstr = "";
		for (int i = 0; i < list.size(); i++) {
			BaseUser user = list.get(i);
			userstr = user.getUserPerson();
			if (user.getIsMailValid()) {
				mails.add(user.getUserEmail());
			}
			if (user.getIsPhoneValid()) {
				mobiles.add(user.getUserPhone());
			}
		}

		strContent = strContent.replace("{userName}", userstr);
		strContent = strContent.replace("{tablecontent}", "欢迎您使用易云公有云服务，<br>您有资源因到期未续费，已被停用。");

		news.setSendDate(new Date());// 当前时间
		news.setSendPerson("易云公有云运营团队");
		news.setNewsTitle(title);
		news.setMemo("<p class='(ey-font-size-bigger)'>您有资源因到期未续费，已被停用。</p>"
				+ "<table class='ey-table  ey-table-auto' style='margin-top:30px'> <thead><tr>" + tr
				+ "</tr></thead><tbody><tr>" + bs + "</tr></tbody></table>");
		news.setRecType("2");// 客户下的管理员
		news.setIsSended("2");// 设置发送状态 2未发
		news.setSended(1);
		news.setCusId(cusid);
		news.setIs_syssend("1");
		ecmcNewsService.save(news);
		MessageResourcesStopModel model = resourList.get(0);
		String sms = "尊敬的客户：您有[" + resourList.size() + "]个资源因到期未续费，已于" + DateUtil.dateToString(model.getExpireDate())
				+ "被停用，如有问题请致电400-606-6396。";

		try {
			if (mobiles.size() != 0) {
				SMSService.send(sms, mobiles);// 短信

			}
			if (mails.size() != 0) {

				mailService.send("【易云】" + title, strContent, mails);// 邮件
			}
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			throw new AppException(e.toString());
		}
	}

	@Override
	public void payAsYouGoResourStopMessage(List<MessagePayAsYouGoResourcesStopModel> resourList, String cusid)
			throws AppException {

		BaseNewsSend news = new BaseNewsSend();
		String tr = "<tr><th width='20%' style='padding-left:10px;'>资源类型</th> <th width='50%'>资源名称</th> <th width='30%'>资源停用时间</th></tr>";
		String td = "<tr><td style='padding-left:10px;'>resourcesType</td><td >resourcesName</td> <td >expireDate</td></tr>";
		String tds = "<tr><td style='padding-left:10px;'>resourcesType</td><td >resourcesName</td> <td >expireDate</td></tr>";
		StringBuffer bs = new StringBuffer("");
		String strContent = null;
		String title = "按需付费资源欠费已达上限通知";
		try {
			strContent = String.valueOf(getHtmlEmail());
		} catch (Exception e1) {
			throw new AppException("获取邮件页面失败");
		}
		for (int i = 0; i < resourList.size(); i++) {
			MessagePayAsYouGoResourcesStopModel model = resourList.get(i);
			String resourcesType = model.getResourcesType();
			String resourcesName = model.getResourcesName();
			Date expireDate = model.getExpireDate();
			td = td.replaceAll("resourcesType", resourcesType);
			td = td.replaceAll("resourcesName",
					resourcesName.length() > 20 ? resourcesName.substring(0, 17) + "..." : resourcesName);
			td = td.replaceAll("expireDate", DateUtil.dateToString(expireDate));
			bs.append(td);
			td = tds;
		}
		strContent = strContent.replace("{trcontent}", tr);
		strContent = strContent.replace("{content}", bs.toString());
		// 查询邮件地址，短信地址语句
		List<BaseUser> list = noticeDao.getPhoneandEmail(true, cusid);

		// List<String> mobiles=new ArrayList();
		List<String> mails = new ArrayList<String>();
		String userstr = "";
		for (int i = 0; i < list.size(); i++) {
			BaseUser user = list.get(i);
			userstr = user.getUserPerson();
			if (user.getIsMailValid()) {
				mails.add(user.getUserEmail());
			}

		}

		strContent = strContent.replace("{userName}", userstr);
		strContent = strContent.replace("{tablecontent}", "欢迎您使用易云公有云服务，<br>您有资源因欠费已达上限将被停用，为确保正常使用，请及时充值或联系客服。");

		news.setSendDate(new Date());// 当前时间
		news.setSendPerson("易云公有云运营团队");
		news.setNewsTitle(title);
		news.setMemo("<p class='(ey-font-size-bigger)'>您有资源因欠费已达上限即将被停用，为确保正常使用，请及时充值或联系客服。</p><br>"
				+ "<table class='ey-table  ey-table-auto' style='margin-top:30px'> <thead><tr>" + tr
				+ "</tr></thead><tbody><tr>" + bs + "</tr></tbody></table>");
		news.setRecType("2");// 客户下的管理员
		news.setIsSended("2");// 设置发送状态 2未发
		news.setSended(1);
		news.setCusId(cusid);
		news.setIs_syssend("1");
		ecmcNewsService.save(news);

		try {
			if (mails.size() != 0) {
				mailService.send("【易云】" + title, strContent, mails);// 邮件

			}
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			throw new AppException(e.toString());
		}

	}

	@Override
	public void payUpperLimitResourStopMessage(List<MessagePayUpperLimitResourcesStopModel> resourList, String cusid)
			throws AppException {
		BaseNewsSend news = new BaseNewsSend();
		String tr = "<tr><th width='20%' style='padding-left:10px;'>资源类型</th> <th width='50%'>资源名称</th> <th width='30%'>资源停用时间</th></tr>";
		String td = "<tr><td style='padding-left:10px;'>resourcesType</td><td >resourcesName</td> <td >expireDate</td></tr>";
		String tds = "<tr><td style='padding-left:10px;'>resourcesType</td><td >resourcesName</td> <td >expireDate</td></tr>";
		StringBuffer bs = new StringBuffer("");
		String strContent = null;
		String title = "按需付费资源停用通知";
		try {
			strContent = String.valueOf(getHtmlEmail());
		} catch (Exception e1) {
			log.error(e1.getMessage(), e1);
			throw new AppException("获取邮件页面失败");
		}
		for (int i = 0; i < resourList.size(); i++) {
			MessagePayUpperLimitResourcesStopModel model = resourList.get(i);
			String resourcesType = model.getResourcesType();
			String resourcesName = model.getResourcesName();
			Date expireDate = model.getExpireDate();
			td = td.replaceAll("resourcesType", resourcesType);
			td = td.replaceAll("resourcesName",
					resourcesName.length() > 20 ? resourcesName.substring(0, 17) + "..." : resourcesName);
			td = td.replaceAll("expireDate", DateUtil.dateToString(expireDate));
			bs.append(td);
			td = tds;
		}
		strContent = strContent.replace("{trcontent}", tr);
		strContent = strContent.replace("{content}", bs.toString());
		// 查询邮件地址，短信地址语句
		List<BaseUser> list = noticeDao.getPhoneandEmail(true, cusid);

		List<String> mobiles = new ArrayList<String>();
		List<String> mails = new ArrayList<String>();
		String userstr = "";
		for (int i = 0; i < list.size(); i++) {
			BaseUser user = list.get(i);
			userstr = user.getUserPerson();
			if (user.getIsMailValid()) {
				mails.add(user.getUserEmail());
			}
			if (user.getIsPhoneValid()) {
				mobiles.add(user.getUserPhone());
			}
		}

		strContent = strContent.replace("{userName}", userstr);
		strContent = strContent.replace("{tablecontent}", "欢迎您使用易云公有云服务，<br>您有资源因账户欠费，已被停用，为确保正常使用，请及时充值或联系客服。");

		news.setSendDate(new Date());// 当前时间
		news.setSendPerson("易云公有云运营团队");
		news.setNewsTitle(title);
		news.setMemo("<p class='(ey-font-size-bigger)'>您有资源因账户欠费，已被停用,为确保正常使用，请及时充值或联系客服。</p><br>"
				+ "<table class='ey-table  ey-table-auto' style='margin-top:30px'> <thead><tr>" + tr
				+ "</tr></thead><tbody><tr>" + bs + "</tr></tbody></table>");
		news.setRecType("2");// 客户下的管理员
		news.setIsSended("2");// 设置发送状态 2未发
		news.setSended(1);
		news.setCusId(cusid);
		news.setIs_syssend("1");
		ecmcNewsService.save(news);
		MessagePayUpperLimitResourcesStopModel model = resourList.get(0);
		String sms = "尊敬的客户：您有[" + resourList.size() + "]个资源因账户欠费，已于" + DateUtil.dateToString(model.getExpireDate())
				+ "被停用，如有问题请致电400-606-6396。";

		try {
			if (mobiles.size() != 0) {

				SMSService.send(sms, mobiles);// 短信
			}
			if (mails.size() != 0) {

				mailService.send("【易云】" + title, strContent, mails);// 邮件
			}
		} catch (Exception e) {
			throw new AppException(e.toString());
		}

	}

	@Override
	public void balanLackceMessage(String cusid) throws AppException {

		List<BaseUser> list = noticeDao.getPhoneandEmail(true, cusid);
		List<String> mobiles = new ArrayList<String>();
		List<String> mails = new ArrayList<String>();
		for (int i = 0; i < list.size(); i++) {
			BaseUser user = list.get(i);

			if (user.getIsPhoneValid()) {
				mobiles.add(user.getUserPhone());
			}
		}
		try {
			if (mobiles.size() != 0) {

				SMSService.send("尊敬的客户：您的账户余额已不多，为确保正常使用，请及时充值，如有问题请致电400-606-6396。", mobiles);// 短信
			}

		} catch (Exception e) {
			throw new AppException(e.toString());
		}

	}

	@Override
	public void accountArrearsMessage(String cusid, int resourcesCount) throws AppException {
		String countent = "尊敬的客户：您有[" + resourcesCount + "]个资源因欠费将被停用，为确保正常使用，请及时充值，如有问题请致电400-606-6396。";

		List<BaseUser> list = noticeDao.getPhoneandEmail(true, cusid);
		List<String> mobiles = new ArrayList<String>();
		List<String> mails = new ArrayList<String>();
		for (int i = 0; i < list.size(); i++) {
			BaseUser user = list.get(i);

			if (user.getIsPhoneValid()) {
				mobiles.add(user.getUserPhone());
			}
		}
		try {
			if (mobiles.size() != 0) {

				SMSService.send(countent, mobiles);// 短信
			}

		} catch (Exception e) {
			throw new AppException(e.toString());
		}

	}

	@Override
	public void accountPayMessage(String cusid, BigDecimal payMoney, BigDecimal balance) throws AppException {

		String balancestr = new DecimalFormat("0.000").format(balance);
		balancestr = balancestr.substring(0, balancestr.length() - 1);

		String countent = "尊敬的客户：系统已为您的账户充值￥" + new DecimalFormat("0.00").format(payMoney) + "，当前余额为￥" + balancestr
				+ "，请登录管理控制台查看，如有问题请致电400-606-6396。";
		List<BaseUser> list = noticeDao.getPhoneandEmail(true, cusid);
		List<String> mobiles = new ArrayList<String>();
		List<String> mails = new ArrayList<String>();
		for (int i = 0; i < list.size(); i++) {
			BaseUser user = list.get(i);

			if (user.getIsPhoneValid()) {
				mobiles.add(user.getUserPhone());
			}
		}
		try {
			if (mobiles.size() != 0) {

				SMSService.send(countent, mobiles);// 短信
			}

		} catch (Exception e) {
			throw new AppException(e.toString());
		}

	}

	@Override
	public void accountFrozenMessage(String cusid) throws AppException {

		String countent = "尊敬的客户：您的账户已被冻结，无法登录管理控制台且资源无法使用，如有问题请致电400-606-6396。";
		List<BaseUser> list = noticeDao.getPhoneandEmail(true, cusid);
		List<String> mobiles = new ArrayList<String>();
		List<String> mails = new ArrayList<String>();
		for (int i = 0; i < list.size(); i++) {
			BaseUser user = list.get(i);

			if (user.getIsPhoneValid()) {
				mobiles.add(user.getUserPhone());
			}
		}
		try {
			if (mobiles.size() != 0) {

				SMSService.send(countent, mobiles);// 短信
			}

		} catch (Exception e) {
			throw new AppException(e.toString());
		}

	}

	@Override
	public void accountRecoveryMessage(String cusid) throws AppException {

		String countent = "尊敬的客户：您的账户已解冻，请登录管理控制台查看，如有问题请致电400-606-6396。";
		List<BaseUser> list = noticeDao.getPhoneandEmail(true, cusid);
		List<String> mobiles = new ArrayList<String>();
		List<String> mails = new ArrayList<String>();
		for (int i = 0; i < list.size(); i++) {
			BaseUser user = list.get(i);

			if (user.getIsPhoneValid()) {
				mobiles.add(user.getUserPhone());
			}
		}
		try {
			if (mobiles.size() != 0) {

				SMSService.send(countent, mobiles);// 短信
			}

		} catch (Exception e) {
			throw new AppException(e.toString());
		}

	}

	@Override
	public void newOrderMessage(Order order) throws AppException {

		String tr = "<tr><th style='padding-left:10px;'>订单编号</th> <th >产品</th> <th>类型</th><th>状态</th><th>创建时间</th><th>金额</th></tr>";
		String td = "<tr><td style='padding-left:10px;'>orderid</td><td >resourcesName</td> <td >type</td><td >stat</td><td >date</td><td class='ey-color-red'>money</td></tr>";
		StringBuffer bs = new StringBuffer("");
		String strContent = null;
		String title = "新订单提醒通知";
		try {
			strContent = String.valueOf(getHtmlEmail());
		} catch (Exception e1) {
			log.error(e1.getMessage(), e1);
			throw new AppException("获取邮件页面失败");
		}

		String orderid = order.getOrderNo();
		String resourcesName = order.getProdName();
		String ordertype = com.eayun.common.constant.OrderType.getName(order.getOrderType());

		String stat = order.getOrderState();
		String stattype = com.eayun.common.constant.OrderStateType.getName(stat);

		String type = ordertype;

		Date date = order.getCreateTime();
		BigDecimal money = order.getPaymentAmount();
		td = td.replaceAll("orderid", orderid);
		td = td.replaceAll("resourcesName",
				resourcesName.length() > 20 ? resourcesName.substring(0, 17) + "..." : resourcesName);
		td = td.replaceAll("type", type);
		td = td.replaceAll("stat", stattype);
		td = td.replaceAll("date", DateUtil.dateToString(date));
		td = td.replaceAll("money", "￥  " + new DecimalFormat("0.00").format(money));

		bs.append(td);

		strContent = strContent.replace("{trcontent}", tr);
		strContent = strContent.replace("{content}", bs.toString());
		// 查询邮件地址，短信地址语句
		String sql = " select mail from ecmc_sys_user where id in(select user_id from ecmc_sys_userrole where role_id in('"
				+ EcmcRoleIds.ADMIN + "','" + EcmcRoleIds.BUSINESS + "'))";
		List<Object> list = noticeDao.createSQLNativeQuery(sql, null).getResultList();

		List<String> mails = new ArrayList<String>();
		for (int i = 0; i < list.size(); i++) {
			Object mail = (Object) list.get(i);
			if (null != mail) {
				mails.add(mail.toString());
			}

		}
		String name = resourcesName.length() > 20 ? resourcesName.substring(0, resourcesName.length() - 3) + "..."
				: resourcesName;
		strContent = strContent.replace("{userName}", "用户");
		strContent = strContent.replace("{tablecontent}",
				"新的订单：" + orderid + "  " + name + "于" + DateUtil.dateToString(date) + "已提交成功，请登录ECMC查看！");

		try {
			mailService.send("【易云】" + title, strContent, mails);// 邮件
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			throw new AppException(e.toString());
		}

	}

	@Override
	public void addResourFailMessage(String orderNo, String cusId) throws AppException {
		Order order = orderSerive.getOrderWithoutValidate(orderNo);

		String tr = "支付详情：<br><br>订单编号 :   " + orderNo + "<br>"
				+ "<br><th >产品名称</th> <th>产品数量</th><th style='width:40%; line-height:24px;'>具体配置</th><th>购买周期</th><th>付款方式</th><th>金额</th>";
		String td = "<td >resourcesName</td> <td >count</td><td >peizhi</td><td >date</td><td>paytype</td><td class='ey-color-red' >money</td>";
		StringBuffer bs = new StringBuffer("");
		String strContent = null;

		try {
			strContent = String.valueOf(getHtmlEmail());
		} catch (Exception e1) {
			log.error(e1.getMessage(), e1);
			throw new AppException("获取邮件页面失败");
		}
		BaseNewsSend news = new BaseNewsSend();
		String title = "订单处理失败通知";
		String name = order.getProdName().length() > 20 ? order.getProdName().substring(0, 17) + "..."
				: order.getProdName();

		td = td.replaceAll("resourcesName", name);
		td = td.replaceAll("count", order.getProdCount() + "");
		td = td.replaceAll("peizhi", order.getProdConfig());
		String year = order.getBuyCycle() / 12 == 0 ? "" : order.getBuyCycle() / 12 + "年";
		String month = order.getBuyCycle() % 12 == 0 ? "" : order.getBuyCycle() % 12 + "个月";

		td = td.replaceAll("date", order.getPayType().equals("2") ? "--" : year + month);
		td = td.replaceAll("paytype", com.eayun.common.constant.PayType.getName(order.getPayType()));
		String money = new DecimalFormat("0.00").format(order.getPaymentAmount());
		td = td.replaceAll("money", "￥  " + money);

		bs.append(td);

		// 查询邮件地址，短信地址语句
		List<BaseUser> list = noticeDao.getPhoneandEmail(true, order.getCusId());
		strContent = strContent.replace("{userName}", list.get(0).getUserPerson());
		strContent = strContent.replace("{tablecontent}",
				"欢迎您使用易云公有云服务,<br>您的订单<a> " + order.getOrderNo() + "</a><a>" + name + "</a> 因资源处理失败，已于"
						+ DateUtil.dateToString(order.getCanceledTime())
						+ "被取消<br>，系统稍后会将已经扣除的费用返还到您的账户中，请注意查收！<br><br>若有需要，请稍后重新购买，给您带来不便，敬请谅解！");

		news.setSendDate(new Date());// 当前时间
		news.setSendPerson("易云公有云运营团队");
		news.setNewsTitle(title);

		news.setMemo("<p class='(ey-font-size-bigger)'>您的订单<a> " + order.getOrderNo() + "</a><a>" + name
				+ "</a>， 因资源处理失败，已于" + DateUtil.dateToString(order.getCanceledTime())
				+ "被取消,系统稍后会将已经扣除的费用返还到您的账户中，请注意查收！<br><br>若有需要，请稍后重新购买，给您带来不便，敬请谅解！</p><br>"
				+ "<table class='ey-table  ey-table-auto' style='margin-top:30px'> <thead><tr>" + tr
				+ "</tr></thead><tbody><tr>" + bs + "</tr></tbody></table>");
		news.setRecType("2");// 指定客户下的所有人
		news.setIsSended("2");// 设置发送状态 2未发
		news.setSended(1);
		news.setCusId(cusId);
		news.setIs_syssend("1");
		ecmcNewsService.save(news);

	}

	@Override
	public void delecteResourFailMessage(List<MessageOrderResourceNotice> orderRe, String orderNo) throws AppException {
		String tr = "<tr> <th style='padding-left:10px; width:100px;' >订单编号</th> <th style='width:200px'>订单名称</th><th  style='width:200px'>取消时间</th><th  style='width:200px'>资源类型</th><th  style='width:300px'>删除异常资源名称</th><th  style='width:200px'>删除异常资源id</th></tr>";
		String td = "<tr><td style='padding-left:10px; width:100px;'>orderno</td><td style='width:200px' >ordername</td><td style='width:200px'>backdate</td><td style='width:200px'>restype</td><td style='width:300px'>resname</td><td style='width:200px'>resid</td></tr>";
		String tds = "<tr><td style='padding-left:10px; width:100px;'>orderno</td><td style='width:200px'>ordername</td><td style='width:200px'>backdate</td><td style='width:200px'>restype</td><td style='width:300px'>resname</td><td style='width:200px'>resid</td></tr>";
		StringBuffer bs = new StringBuffer("");
		String strContent = null;

		try {
			strContent = String.valueOf(getHtmlEmail());
		} catch (Exception e1) {
			throw new AppException("获取邮件页面失败");
		}

		BaseOrder baseOrder = orderDao.findByOrderNo(orderNo);

		Order order = new Order();
		BeanUtils.copyPropertiesByModel(order, baseOrder);
		Customer user = userservice.findCustomerById(order.getCusId().toString());

		for (int i = 0; i < orderRe.size(); i++) {
			MessageOrderResourceNotice model = orderRe.get(i);
			String resourcesType = model.getResourceType();
			String resourcesName = model.getResourceName();
			String name = order.getProdName().length() > 20 ? order.getProdName().substring(0, 17) + "..."
					: order.getProdName();
			// td = td.replaceAll("personName", user.getCusOrg());
			td = td.replaceAll("orderno", order.getOrderNo());
			td = td.replaceAll("ordername", name);
			// td = td.replaceAll("commitdate",
			// DateUtil.dateToString(order.getCreateTime()));
			td = td.replaceAll("backdate", DateUtil.dateToString(order.getCanceledTime()));
			td = td.replaceAll("restype", resourcesType);
			td = td.replaceAll("resname",
					resourcesName.length() > 20 ? resourcesName.substring(0, 17) + "..." : resourcesName);
			td = td.replaceAll("resid", model.getResourceId());
			bs.append(td);
			td = tds;

		}
		String username = "";

		if (user != null) {
			username = user.getCusOrg();
		}

		strContent = strContent.replace("{trcontent}", tr);
		strContent = strContent.replace("{content}", bs.toString());
		strContent = strContent.replace("{userName}", "用户");
		strContent = strContent.replace("{tablecontent}", username + "的资源创建失败，底层删除操作发生异常，为确保客户配额资源的正常使用，请及时处理。<br>");

		StringBuffer sql = new StringBuffer(" select mail from ecmc_sys_user where id in");
		sql.append("(select user_id from ecmc_sys_userrole where role_id in");
		sql.append("(?,?) )");
		Object[] obj = { EcmcRoleIds.ADMIN, EcmcRoleIds.OPERATION };

		List<String> list = noticeDao.createSQLNativeQuery(sql.toString(), obj).getResultList();

		try {
			mailService.send("【易云】底层资源删除失败通知", strContent, list);
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			throw new AppException(e.toString());
		} // 邮件

	}

	@Override
	public void userResourRecoveryFail(List<MessageUserResour> userResour) throws AppException {
		String tr = "<tr>  <th style='padding-left:10px;'>资源类型</th><th>恢复异常的资源名称</th><th>恢复异常的资源id</th></tr>";
		String td = "<tr> <td style='padding-left:10px;'>resourtype</td><td >resourname</td><td>resourid</td></tr>";
		String tds = "<tr> <td style='padding-left:10px;'>resourtype</td><td >resourname</td><td>resourid</td></tr>";
		StringBuffer bs = new StringBuffer("");
		String strContent = null;
		String title = "资源恢复失败通知";
		try {
			strContent = String.valueOf(getHtmlEmail());
		} catch (Exception e1) {
			log.error(e1.getMessage(), e1);
			throw new AppException("获取邮件页面失败");
		}
		String username = "";
		String time = "";
		for (int j = 0; j < userResour.size(); j++) {
			MessageUserResour user = userResour.get(j);
			time = DateUtil.dateToString(user.getRecoveryTime());
			td = td.replaceAll("CusOrg", user.getCusName());

			td = td.replaceAll("resourtype", user.getResourType());
			td = td.replaceAll("resourname", user.getResourname().length() > 20
					? user.getResourname().substring(0, 17) + "..." : user.getResourname());
			td = td.replaceAll("resourid", user.getResourId());

			bs.append(td);
			td = tds;
			username = user.getCusName();
		}
		strContent = strContent.replace("{userName}", "用户");
		strContent = strContent.replace("{tablecontent}", username + "的资源于" + time + "恢复失败，为确保客户资源的正常使用，请及时处理。");

		strContent = strContent.replace("{trcontent}", tr);
		strContent = strContent.replace("{content}", bs.toString());
		// 查询邮件地址，短信地址语句
		StringBuffer sql = new StringBuffer(" select mail from ecmc_sys_user where id in");
		sql.append("(select user_id from ecmc_sys_userrole where role_id in");
		sql.append("(?,?) )");
		Object[] obj = { EcmcRoleIds.ADMIN, EcmcRoleIds.OPERATION };

		List<String> list = noticeDao.createSQLNativeQuery(sql.toString(), obj).getResultList();
		List<String> mails = new ArrayList<String>();
		for (int i = 0; i < list.size(); i++) {
			Object mail = (Object) list.get(i);
			if (null != mail) {
				mails.add(mail.toString());
			}

		}

		try {
			mailService.send("【易云】" + title, strContent, mails);// 邮件
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			throw new AppException(e.toString());
		}

	}

	@Override
	public void stackSynFail(List<MessageStackSynFailResour> stackResour) throws AppException {
		String tr = "<tr> <th style='padding-left:10px;'>资源类型</th><th>已删除的资源名称</th><th>已删除的资源id</th></tr>";
		String td = "<tr> <td style='padding-left:10px;' >resourtype</td><td >resourname</td><td>resourid</td></tr>";
		String tds = "<tr> <td style='padding-left:10px;'>resourtype</td><td >resourname</td><td>resourid</td></tr>";
		StringBuffer bs = new StringBuffer("");
		String strContent = null;
		String title = "底层同步资源不一致通知";
		try {
			strContent = String.valueOf(getHtmlEmail());
		} catch (Exception e1) {
			log.error(e1.getMessage(), e1);
			throw new AppException("获取邮件页面失败");
		}
		// String username = "";
		String time = "";
		for (int j = 0; j < stackResour.size(); j++) {
			MessageStackSynFailResour stackSyn = stackResour.get(j);
			
			time = DateUtil.dateToString(stackSyn.getSynTime());
			td = td.replaceAll("resourtype", null==stackSyn.getResourtype()?"":stackSyn.getResourtype());
			td = td.replaceAll("resourname", null==stackSyn.getResourName()?"":stackSyn.getResourName().length() > 20
					? stackSyn.getResourName().substring(0, 17) + "..." : stackSyn.getResourName());

			td = td.replaceAll("resourid", null==stackSyn.getResourID()?"":stackSyn.getResourID());

			bs.append(td);
			td = tds;
			// username = user.getCusOrg();
		}
		strContent = strContent.replace("{userName}", "用户");
		strContent = strContent.replace("{tablecontent}",
				"客户的资源在EayunStack环境中被删除，导致于" + time + "同步数据时清除掉了公有云平台中的这些资源，为避免纠纷，请查看日志及时处理。 ");

		strContent = strContent.replace("{trcontent}", tr);
		strContent = strContent.replace("{content}", bs.toString());
		// 查询邮件地址，短信地址语句
		StringBuffer sql = new StringBuffer(" select mail from ecmc_sys_user where id in");
		sql.append("(select user_id from ecmc_sys_userrole where role_id in");
		sql.append("(?,?) )");
		Object[] obj = { EcmcRoleIds.ADMIN, EcmcRoleIds.OPERATION };

		List<String> list = noticeDao.createSQLNativeQuery(sql.toString(), obj).getResultList();
		List<String> mails = new ArrayList<String>();
		for (int i = 0; i < list.size(); i++) {
			Object mail = (Object) list.get(i);
			if (null != mail) {
				mails.add(mail.toString());
			}

		}

		try {
			mailService.send("【易云】" + title, strContent, mails);// 邮件
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			throw new AppException(e.toString());
		}

	}
	
	
	
	@Override
	public void ecscToMailEcmc(MessageEcscToMailEcmc model,String sendWho) throws AppException {
		String tr = "<tr> <th style='padding-left:10px;'>交易时间</th><th>收支类型</th><th>交易备注</th><th>交易金额</th><th>账户余额</th></tr>";
		String td = "<tr> <td style='padding-left:10px;' >paytime</td><td>shourutype</td><td>paydesc</td><td>paymoney</td><td>cusmoney</td></tr>";
		String tds = "<tr> <td style='padding-left:10px;' >paytime</td><td>shourutype</td><td>paydesc</td><td>paymoney</td><td>cusmoney</td></tr>";
		StringBuffer bs = new StringBuffer("");
		String strContent = null;
		String title = "";
		if(TransType.EXPEND.equals(sendWho)){
			 title = "客户使用支付宝购买服务提醒";
		}else{
			 title = "客户充值提醒";
			
		}
		
		String str=new DecimalFormat("0.000").format(model.getPayMoney());
		if(str.endsWith(str)){
			str= str.substring(0, str.length()-1);
			
			
		}
		try {
			strContent = String.valueOf(getHtmlEmail());
		} catch (Exception e1) {
			log.error(e1.getMessage(), e1);
			throw new AppException("获取邮件页面失败");
		}
		//String username = "";
		String time = "";
		if(null!=model){
			time = DateUtil.dateToString(model.getTransactionTime());
			
			td = td.replaceAll("paytime",time);
			
			
			td = td.replaceAll("shourutype",model.getShouruType());
			
			td = td.replaceAll("paydesc",model.getTransactiondesc());
			
			String balancestr=new DecimalFormat("0.000").format(model.getBalance());
			balancestr=balancestr.substring(0, balancestr.length()-1);
			
			
			td = td.replaceAll("paymoney",str);
			td = td.replaceAll("cusmoney",balancestr);

			bs.append(td);
			td = tds;
		}
			

			
			//username = user.getCusOrg();
		
		strContent = strContent.replace("{userName}", "用户");
		Customer cus=userservice.findCustomerById(model.getCusId());
		
	
		if(TransType.EXPEND.equals(sendWho)){
			strContent = strContent.replace("{tablecontent}",
					cus.getCusOrg()+ " 于 "+time+" 因使用支付宝购买服务，发生账户充值，成功充值￥"+str+",请登录ECMC查看！");
		}else{
			strContent = strContent.replace("{tablecontent}",
					cus.getCusOrg()+ " 于 "+time+" 发生账户充值，成功充值￥"+str+",请登录ECMC查看！");
		}
		

		strContent = strContent.replace("{trcontent}", tr);
		strContent = strContent.replace("{content}", bs.toString());
		// 查询邮件地址，短信地址语句
		StringBuffer sql = new StringBuffer(" select mail from ecmc_sys_user where id in");
		sql.append("(select user_id from ecmc_sys_userrole where role_id in");
		sql.append("(?,?,?) )");
		Object[] obj = { EcmcRoleIds.ADMIN, EcmcRoleIds.BUSINESS,EcmcRoleIds.CUSTOMER_SERVICE };

		List<String> list = noticeDao.createSQLNativeQuery(sql.toString(), obj).getResultList();
		List<String> mails = new ArrayList<String>();
		for (int i = 0; i < list.size(); i++) {
			Object mail = (Object) list.get(i);
			if (null != mail) {
				mails.add(mail.toString());
			}

		}

		try {
			mailService.send("【易云】" + title, strContent, mails);// 邮件
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			throw new AppException(e.toString());
		}

	}

	
	@Override
	public void ecmcDeductionFund(String cusid, BigDecimal deductionMoney, BigDecimal balance) throws AppException {

		String balancestr = new DecimalFormat("0.000").format(balance);
		balancestr = balancestr.substring(0, balancestr.length() - 1);

		String countent = "尊敬的客户：系统已为您的账户扣费￥" + new DecimalFormat("0.00").format(deductionMoney) + "，当前余额为￥"
				+ balancestr + "，请登录管理控制台查看，如有问题请致电400-606-6396。";
		List<BaseUser> list = noticeDao.getPhoneandEmail(true, cusid);
		List<String> mobiles = new ArrayList<String>();
		List<String> mails = new ArrayList<String>();
		for (int i = 0; i < list.size(); i++) {
			BaseUser user = list.get(i);

			if (user.getIsPhoneValid()) {
				mobiles.add(user.getUserPhone());
			}
		}
		try {
			if (mobiles.size() != 0) {

				SMSService.send(countent, mobiles);// 短信
			}

		} catch (Exception e) {
			throw new AppException(e.toString());
		}
	}

	@Override
	public void yesOpenReceipt(String cusId, BigDecimal money, String receiptType, String receiptRise, String address,
			String status, Date receiptTime) throws AppException {
		String tr = "<tr>  <th style='padding-left:10px;'>金额</th><th>发票类型</th><th>抬头</th><th>邮寄地址</th><th>状态</th></tr>";
		String td = "<tr> <td style='padding-left:10px;'>money</td><td >receiptType</td><td>receiptRise</td><td>address</td><td>status</td></tr>";
		String tds = "<tr> <td style='padding-left:10px;'>money</td><td >receiptType</td><td>receiptRise</td><td>address</td><td>status</td></tr>";
		StringBuffer bs = new StringBuffer("");
		String strContent = null;
		String title = "发票处理完成通知";
		try {
			strContent = String.valueOf(getHtmlEmail());
		} catch (Exception e1) {
			log.error(e1.getMessage(), e1);
			throw new AppException("获取邮件页面失败");
		}
		String username = "";

		String balancestr = new DecimalFormat("0.000").format(money);
		balancestr = balancestr.substring(0, balancestr.length() - 1);

		td = td.replaceAll("money", balancestr);
		td = td.replaceAll("receiptType", receiptType);
		td = td.replaceAll("receiptRise",  receiptRise.length() > 20
				? receiptRise.substring(0, 17) + "..." : receiptRise);
		td = td.replaceAll("address", address.length() > 20
				? address.substring(0, 17) + "..." : address);
		td = td.replaceAll("status", status);

		bs.append(td);
		Customer user = userservice.findCustomerById(cusId);
		username = user.getCusName();

		strContent = strContent.replace("{userName}", username);
		strContent = strContent.replace("{tablecontent}",
				"您于" + DateUtil.dateToString(receiptTime) + "申请的发票，已处理完成，请及时查收。");

		strContent = strContent.replace("{trcontent}", tr);
		strContent = strContent.replace("{content}", bs.toString());
		// 查询邮件地址，短信地址语句
		List<BaseUser> list = noticeDao.getPhoneandEmail(true, cusId);
		// List<String> mobiles = new ArrayList<String>();
		List<String> mails = new ArrayList<String>();
		for (int i = 0; i < list.size(); i++) {
			BaseUser baseuser = list.get(i);
			if (baseuser.getIsMailValid()) {
				mails.add(baseuser.getUserEmail());
			}

		}

		if (mails.size() > 0) {
			try {
				mailService.send("【易云】" + title, strContent, mails);
			} catch (Exception e) {
				throw new AppException(e.toString());

			}
		}
	}

	@Override
	public void ecmcCancelReceipt(String cusId, BigDecimal money, String receiptType, String receiptRise,
			String address, String status, Date receiptTime, String CancelReceiptRe) throws AppException {
	
		String tr = "<tr>  <th style='padding-left:10px;'>金额</th><th>发票类型</th><th>抬头</th><th>邮寄地址</th><th>状态</th></tr>";
		String td = "<tr> <td style='padding-left:10px;'>money</td><td >receiptType</td><td>receiptRise</td><td>address</td><td>status</td></tr>";
		String tds = "<tr> <td style='padding-left:10px;'>money</td><td >receiptType</td><td>receiptRise</td><td>address</td><td>status</td></tr>";
		StringBuffer bs = new StringBuffer("");
		String strContent = null;
		String title = "开票申请取消通知";
		try {
			strContent = String.valueOf(getHtmlEmail());
		} catch (Exception e1) {
			log.error(e1.getMessage(), e1);
			throw new AppException("获取邮件页面失败");
		}
		String username = "";

		String balancestr = new DecimalFormat("0.000").format(money);
		balancestr = balancestr.substring(0, balancestr.length() - 1);

		td = td.replaceAll("money", balancestr);
		td = td.replaceAll("receiptType", receiptType);
		td = td.replaceAll("receiptRise", receiptRise.length() > 20
				? receiptRise.substring(0, 17) + "..." : receiptRise);
		td = td.replaceAll("address", address.length() > 20
				? address.substring(0, 17) + "..." : address);
		td = td.replaceAll("status", status);

		bs.append(td);
		Customer user = userservice.findCustomerById(cusId);
		username = user.getCusName();

		strContent = strContent.replace("{userName}", username);
		strContent = strContent.replace("{tablecontent}",
				"您于" + DateUtil.dateToString(receiptTime) + "申请的发票，由于 "+CancelReceiptRe+" 被取消，请及时查收。");

		strContent = strContent.replace("{trcontent}", tr);
		strContent = strContent.replace("{content}", bs.toString());
		// 查询邮件地址，短信地址语句
		List<BaseUser> list = noticeDao.getPhoneandEmail(true, cusId);
		// List<String> mobiles = new ArrayList<String>();
		List<String> mails = new ArrayList<String>();
		for (int i = 0; i < list.size(); i++) {
			BaseUser baseuser = list.get(i);
			if (baseuser.getIsMailValid()) {
				mails.add(baseuser.getUserEmail());
			}

		}

		if (mails.size() > 0) {
			try {
				mailService.send("【易云】" + title, strContent, mails);
			} catch (Exception e) {
				throw new AppException(e.toString());

			}
		}
		
		
	}
	
	@Override
	public void newReceiptInfo(String cusId,BigDecimal money,String receiptType,String receiptRise,String address,String status,Date receiptTime) throws AppException {
		String tr = "<tr>  <th style='padding-left:10px;'>金额</th><th>发票类型</th><th>抬头</th><th>邮寄地址</th><th>状态</th></tr>";
		String td = "<tr> <td style='padding-left:10px;'>money</td><td >receiptType</td><td>receiptRise</td><td>address</td><td>status</td></tr>";
		String tds = "<tr> <td style='padding-left:10px;'>money</td><td >receiptType</td><td>receiptRise</td><td>address</td><td>status</td></tr>";
		StringBuffer bs = new StringBuffer("");
		String strContent = null;
		String title = "新增开票申请";
		try {
			strContent = String.valueOf(getHtmlEmail());
		} catch (Exception e1) {
			log.error(e1.getMessage(), e1);
			throw new AppException("获取邮件页面失败");
		}
		String username = "";

		String balancestr = new DecimalFormat("0.000").format(money);
		balancestr = balancestr.substring(0, balancestr.length() - 1);

		td = td.replaceAll("money", balancestr);
		td = td.replaceAll("receiptType", receiptType);
		td = td.replaceAll("receiptRise",  receiptRise.length() > 20
				? receiptRise.substring(0, 17) + "..." : receiptRise);
		td = td.replaceAll("address",  address.length() > 20
				? address.substring(0, 17) + "..." : address);
		td = td.replaceAll("status", status);

		bs.append(td);
		Customer user = userservice.findCustomerById(cusId);
		username = user.getCusName();

		strContent = strContent.replace("{userName}", "用户");
		strContent = strContent.replace("{tablecontent}",username+ "的 开票申请 "+ DateUtil.dateToString(receiptTime)+" 已提交成功，请登录ECMC查看！");

		strContent = strContent.replace("{trcontent}", tr);
		strContent = strContent.replace("{content}", bs.toString());
		StringBuffer sql = new StringBuffer(" select mail from ecmc_sys_user where id in");
		sql.append("(select user_id from ecmc_sys_userrole where role_id in");
		sql.append("(?) )");
		Object[] obj = { EcmcRoleIds.FINANCE};

		List<String> list = noticeDao.createSQLNativeQuery(sql.toString(), obj).getResultList();
		List<String> mails = new ArrayList<String>();
		for(int i=0;i<list.size();i++){
			if(null!=list.get(i)){
				mails.add(list.get(i));
			}
		}
		if (mails.size() > 0) {
			try {
				mailService.send("【易云】" + title, strContent, mails);
			} catch (Exception e) {
				throw new AppException(e.toString());

			}
		}
		
		
	}

	
	

	/**
	 * 获取邮件
	 * 
	 * @throws Exception
	 */

	private StringBuffer getHtmlEmail() throws Exception {

		return getEcscSysMailHtml();

	}

	/**
	 * 加载邮件发送模板页面（消息中心）
	 * 
	 * @return
	 */
	private StringBuffer getEcscSysMailHtml() throws Exception {
		getUrlMap();
		if (ecmcMailHtml == null) {
			initEcscSysMailHtml();
		}

		return ecscMailHtml;
	}

	private StringBuffer getOPMailHtml() throws Exception {
		getUrlMap();
		if (OpMailHtml == null) {
			initEcscSysMailHtml("str");
		}

		return OpMailHtml;
	}
	
	
	private void initEcscSysMailHtml() throws Exception {
		InputStream emailInput = MessageCenterServiceImpl.class.getResourceAsStream("/message_center_mail.html");
		InputStream db = MessageCenterServiceImpl.class.getResourceAsStream("/db.properties");

		BufferedReader br = null;
		try {
			Properties conf = new Properties();

			conf.load(db);
			ecscMailHtml = new StringBuffer();

			br = new BufferedReader(new InputStreamReader(emailInput, "utf-8"));
			String line = "";
			String mail = "";
			while ((line = br.readLine()) != null) {
				mail += line;
			}
			mail = mail.replace("{imgUrl}", conf.getProperty("imgUrl"));
			mail = mail.replace("{ecscUrl}", urlMap.get("ecscUrl"));
			ecscMailHtml.append(mail);

		} finally {
			if (br != null) {
				br.close();
			}
			if (db != null) {
				db.close();
			}
		}

	}
	private void initEcscSysMailHtml(String str) throws Exception {
		InputStream emailInput = MessageCenterServiceImpl.class.getResourceAsStream("/message_center_mailOperate.html");
		InputStream db = MessageCenterServiceImpl.class.getResourceAsStream("/db.properties");

		BufferedReader br = null;
		try {
			Properties conf = new Properties();

			conf.load(db);
			OpMailHtml = new StringBuffer();

			br = new BufferedReader(new InputStreamReader(emailInput, "utf-8"));
			String line = "";
			String mail = "";
			while ((line = br.readLine()) != null) {
				mail += line;
			}
			mail = mail.replace("{imgUrl}", conf.getProperty("imgUrl"));
			mail = mail.replace("{ecscUrl}", urlMap.get("ecscUrl"));
			OpMailHtml.append(mail);

		} finally {
			if (br != null) {
				br.close();
			}
			if (db != null) {
				db.close();
			}
		}

	}

	@Override
	public void cloudDataBaseBackupDeletedFail(String prjId, List<MessageCloudDataBaseDeletedFailModel> resourList)
			throws AppException {
		String tr = "<tr> <th style='padding-left:10px;'>资源类型</th><th>异常的备份名称</th><th>异常的备份ID</th><th>所属实例ID</th></tr>";
		String td = "<tr> <td style='padding-left:10px;' >resourcesType</td><td >resourcesName</td><td>resourcesId</td><td>instanceId</td></tr>";
		String tds =  "<tr> <td style='padding-left:10px;' >resourcesType</td><td >resourcesName</td><td>resourcesId</td><td>instanceId</td></tr>";
		StringBuffer bs = new StringBuffer("");
		String strContent = null;
		String title = "云数据库备份删除失败通知";
		try {
			strContent = String.valueOf(getHtmlEmail());
		} catch (Exception e1) {
			log.error(e1.getMessage(), e1);
			throw new AppException("获取邮件页面失败");
		}
		for (int i = 0; i < resourList.size(); i++) {
			MessageCloudDataBaseDeletedFailModel model = resourList.get(i);
			String resourcesType = model.getResourcesType();
			String resourcesName = model.getResourcesName();
			
			td = td.replaceAll("resourcesType", resourcesType);
			td = td.replaceAll("resourcesName",
					resourcesName.length() > 20 ? resourcesName.substring(0, 17) + "..." : resourcesName);
			td = td.replaceAll("resourcesId", model.getResourcesId());
			td = td.replaceAll("instanceId", model.getInstanceId());
		
			

			bs.append(td);
			td = tds;
		}
		strContent = strContent.replace("{trcontent}", tr);
		strContent = strContent.replace("{content}", bs.toString());
		// 查询邮件地址，短信地址语句
		CloudProject cp = projectService.findProject(prjId);
        String cusName="客户";
        if(cp != null && !StringUtil.isEmpty(cp.getCustomerId())){
            Customer customer= userservice.findCustomerById(cp.getCustomerId());
            cusName = customer.getCusOrg();
        }
		strContent = strContent.replace("{userName}", "用户");
		strContent = strContent.replace("{tablecontent}", cusName+"的云数据库备份文件删除操作发生异常，为确保客户备份文件的正常使用，请及时处理。");


		try {
			List<String> mails=this.getAdminAndOperationEmail();
			if (mails!=null&&mails.size() != 0) {
				mailService.send("【易云】" + title, strContent, mails);// 邮件

			}
		} catch (Exception e) {
			throw new AppException(e.toString());
		}

		
		
	}

	/**
	 * 获取管理员于运维邮件
	 * */
	private List<String> getAdminAndOperationEmail() {

		// 查询邮件地址，短信地址语句
		StringBuffer sql = new StringBuffer(" select mail from ecmc_sys_user where id in");
		sql.append("(select user_id from ecmc_sys_userrole where role_id in");
		sql.append("(?,?) )");
		Object[] obj = { EcmcRoleIds.ADMIN, EcmcRoleIds.OPERATION };

		List<String> list = noticeDao.createSQLNativeQuery(sql.toString(), obj).getResultList();
		return list;

	}

	@Override
	public void cloudDataBaseBackupNoStart(String projectid, String cloudDataBaseName, Date BackupStartTime)
			throws AppException {
		CloudProject cp=projectService.findProject(projectid);
		BaseNewsSend news = new BaseNewsSend();
		news.setSendDate(new Date());// 当前时间
		news.setSendPerson("易云公有云运营团队");
		news.setNewsTitle("【易云】云数据库自动备份未执行提醒");

		news.setMemo("<p class='(ey-font-size-bigger)'>您的云数据库实例自动备份未执行。</p><br>"
				+ "<p class='(ey-font-size-bigger)'>您的云数据库  "+cloudDataBaseName+" ，于"+DateUtil.dateToString(BackupStartTime)+"执行自动备份时，由于实例状态不满足备份要求，无法执行自动备份。</p><br>"
				+"<p class='(ey-font-size-bigger)'>给您带来不便，敬请谅解！</p>");
		news.setRecType("2");// 指定客户下的超级管理员
		news.setIsSended("2");// 设置发送状态 2未发
		news.setSended(1);
		news.setCusId(cp.getCustomerId());
		news.setIs_syssend("1");
		ecmcNewsService.save(news);
		
	}

	@Override
	public void addCloudDataBaseRootPassWord(String cusId, String cloudDataBaseName, String rootPass) throws AppException {
		BaseNewsSend news = new BaseNewsSend();
		news.setSendDate(new Date());// 当前时间
		news.setSendPerson("易云公有云运营团队");
		news.setNewsTitle("【易云】云数据库root密码生成提醒");

		news.setMemo(
				"<p class='(ey-font-size-bigger)'>您的云数据库root密码已生成。</p><br>" + "<p class='(ey-font-size-bigger)'>您的云数据库"
						+ cloudDataBaseName + "root密码为" + rootPass + ",为了您的数据安全，请妥善保管。</p><br>");
		news.setRecType("2");// 指定客户下的超级管理员
		news.setIsSended("2");// 设置发送状态 2未发
		news.setSended(1);
		news.setCusId(cusId);
		news.setIs_syssend("1");
		ecmcNewsService.save(news);
		List<BaseUser> list = noticeDao.getPhoneandEmail(true, cusId);
		List<String> mobiles = new ArrayList<String>();
		List<String> mails = new ArrayList<String>();
		for (int i = 0; i < list.size(); i++) {
			BaseUser user = list.get(i);
			if (user.getIsMailValid()) {
				mails.add(user.getUserEmail());
			}
			if (user.getIsPhoneValid()) {
				mobiles.add(user.getUserPhone());
			}

		}

		try {
			if (mobiles.size() != 0) {
				SMSService.send("尊敬的客户：系统已为您生成云数据库root密码" + rootPass + "，请妥善保管，如有问题请致电400-606-6396。", mobiles);
			} // 短信
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			throw new AppException(e.toString());

		}

	}

	@Override
	public void ResetcloudDataBaseRootPassWord(String cusId, String cloudDataBaseName)
			throws AppException {
		BaseNewsSend news = new BaseNewsSend();
		news.setSendDate(new Date());// 当前时间
		news.setSendPerson("易云公有云运营团队");
		news.setNewsTitle("【易云】云数据库root重置密码提醒");

		news.setMemo(
				"<p class='(ey-font-size-bigger)'>您的云数据库root密码已重置。</p><br>" + "<p class='(ey-font-size-bigger)'>您的云数据库"
						+ cloudDataBaseName + "下管理员账号root密码已重置成功,为了您的数据安全，请确认是已授权操作。</p><br>");
		news.setRecType("2");// 指定客户下的超级管理员
		news.setIsSended("2");// 设置发送状态 2未发
		news.setSended(1);
		news.setCusId(cusId);
		news.setIs_syssend("1");
		ecmcNewsService.save(news);
//		List<BaseUser> list = noticeDao.getPhoneandEmail(true, cusId);
//		List<String> mobiles = new ArrayList<String>();
//		List<String> mails = new ArrayList<String>();
//		for (int i = 0; i < list.size(); i++) {
//			BaseUser user = list.get(i);
//			if (user.getIsMailValid()) {
//				mails.add(user.getUserEmail());
//			}
//			if (user.getIsPhoneValid()) {
//				mobiles.add(user.getUserPhone());
//			}
//
//		}
//
//		try {
//			if (mobiles.size() != 0) {
//				SMSService.send("尊敬的客户：系统已为您重置云数据库root密码" + rootPass + "，请妥善保管，如有问题请致电400-606-6396。", mobiles);
//			} // 短信
//		} catch (Exception e) {
//			log.error(e.getMessage(), e);
//			throw new AppException(e.toString());
//
//		}

		
	}

	@Override
	public void cloudDataBaseNoAdminPassWordUpdate(String cusId, String cloudDataBaseName, String cloudDataUserName)
			throws AppException {
		BaseNewsSend news = new BaseNewsSend();
		news.setSendDate(new Date());// 当前时间
		news.setSendPerson("易云公有云运营团队");
		news.setNewsTitle("【易云】云数据库普通账号密码修改提醒");

		news.setMemo(
				"<p class='(ey-font-size-bigger)'>您的云数据库普通账号密码已修改。</p><br>" + "<p class='(ey-font-size-bigger)'>您的云数据库"
						+ cloudDataBaseName + "下"+cloudDataUserName+" 的密码已修改成功,为了您的数据安全，请确认是已授权操作。</p><br>");
		news.setRecType("2");// 指定客户下的超级管理员
		news.setIsSended("2");// 设置发送状态 2未发
		news.setSended(1);
		news.setCusId(cusId);
		news.setIs_syssend("1");
		ecmcNewsService.save(news);
		
	}

	@Override
	public void cloudDataBaseRollBackFail(String cusId, List<MessageCloudDataBaseRollBackModel> resourList)
			throws AppException {
		String tr = "<tr> <th style='padding-left:10px;'>订单编号</th><th>订单名称</th><th>订单取消时间</th><th>资源类型</th><th>资源名称</th><th>资源ID</th></tr>";
		String td = "<tr> <td style='padding-left:10px;' >orderNo</td><td >orderName</td><td>ordertime</td><td>resourceTypeName</td><td>cloudDataName</td><td>cloudDataId</td></tr>";
		String tds = "<tr> <td style='padding-left:10px;' >orderNo</td><td >orderName</td><td>ordertime</td><td>resourceTypeName</td><td>cloudDataName</td><td>cloudDataId</td></tr>";
		StringBuffer bs = new StringBuffer("");
		String strContent = null;
		String title = "云数据库实例升降级回滚失败通知";
		try {
			strContent = String.valueOf(getHtmlEmail());
		} catch (Exception e1) {
			log.error(e1.getMessage(), e1);
			throw new AppException("获取邮件页面失败");
		}
		String name="";
		for (int i = 0; i < resourList.size(); i++) {
			MessageCloudDataBaseRollBackModel model = resourList.get(i);
			String orderNo = model.getOrderNo();
			String orderName = model.getOrderName();
			String time=DateUtil.dateToString(model.getOrderCancelTime());
			String cloudDataBaseName=model.getCloudDataBaseName();
			String cloudDataBaseId=model.getCloudDataId();
			String resourceTypeName = model.getResourceTypeName();
			td = td.replaceAll("orderNo", orderNo);
			td = td.replaceAll("orderName",
					orderName.length() > 20 ? orderName.substring(0, 17) + "..." : orderName);
			td = td.replaceAll("resourceTypeName", resourceTypeName);
			td = td.replaceAll("cloudDataName",
					cloudDataBaseName.length() > 20 ? cloudDataBaseName.substring(0, 17) + "..." : cloudDataBaseName);
			td = td.replaceAll("cloudDataId", cloudDataBaseId);
			
			td = td.replaceAll("ordertime", time);

			bs.append(td);
			td = tds;
			name=cloudDataBaseName;
		}
		strContent = strContent.replace("{trcontent}", tr);
		strContent = strContent.replace("{content}", bs.toString());
		// 查询邮件地址，短信地址语句
		Customer Cususer= userservice.findCustomerById(cusId);

		

		strContent = strContent.replace("{userName}", "用户");
		strContent = strContent.replace("{tablecontent}", Cususer.getCusOrg()+"的云数据库实例"+name+"升降级失败，底层回滚操作发生异常，为确保客户的实例规格恢复，请及时处理。 ");


		try {
			List<String> mails=this.getAdminAndOperationEmail();
			if (mails!=null&&mails.size() != 0) {
				mailService.send("【易云】" + title, strContent, mails);// 邮件

			}
		} catch (Exception e) {
			throw new AppException(e.toString());
		}

		
		
	}

	@Override
	public void unitStatusToMailAndSms(String phone, String mail,String userName, List<MessageUnitModel> model) throws AppException {
		String tr = "<tr> <th style='padding-left:10px;'>主体单位</th><th>网站域名</th><th>备案类型</th><th>当前进度</th><th>创建时间</th></tr>";
		String td = "<tr> <td style='padding-left:10px;' >unitName</td><td style='line-height: 12px;'>domainName</td><td>recordType</td><td>status</td><td>time</td></tr>";
		String tds = "<tr> <td style='padding-left:10px;' >unitName</td><td style='line-height: 12px;'>domainName</td><td>recordType</td><td>status</td><td>time</td></tr>";
		StringBuffer bs = new StringBuffer("");
		String strContent = null;
		String title = "备案状态变更通知";
		try {
			strContent = String.valueOf(getHtmlEmail());
		} catch (Exception e1) {
			log.error(e1.getMessage(), e1);
			throw new AppException("获取邮件页面失败");
		}
		String name="";
		for (int i = 0; i < model.size(); i++) {
			MessageUnitModel unitmodel = model.get(i);
			String unitName = unitmodel.getUnitName();
			String domainName = unitmodel.getDomainName();
			String time=unitmodel.getTime();
			String recordType=unitmodel.getRecordType();
			String status=unitmodel.getStatus();
		
			td = td.replaceAll("unitName", unitName.length() > 20 ? unitName.substring(0, 17) + "..." : unitName);
			td = td.replaceAll("domainName",websToStringAndBR(domainName));
			td = td.replaceAll("time", time);
			td = td.replaceAll("recordType",recordType);
			td = td.replaceAll("status", status);
			
			

			bs.append(td);
			td = tds;
			
		}
		SysDataTree tree=DictUtil.getDataTreeByNodeId("0020001");
		String []treemails= tree.getPara1().split(";");
		String str="<p><br>如有问题，请及时联系备案专员<br>备案专员邮箱:"+treeToString(treemails)+"<br>联系电话：086-010-6066396<br>QQ:1064127799<p>";
		strContent = strContent.replace("{trcontent}", tr);
		strContent = strContent.replace("{content}", bs.toString());
		
		

		strContent = strContent.replace("{userName}", userName);
		strContent = strContent.replace("{tablecontent}", "您的备案信息，状态已变更");
		strContent = strContent.replace("</table>","</table>"+str);//将其他内容写在table外面
		//strContent = strContent.replace("{texts}", str);


		try {
			List<String> mails=new ArrayList<>();
			List<String> mobiles=new ArrayList<>();
			mails.add(mail);
			mobiles.add(phone);
			if (mails!=null&&mails.size() != 0) {
				mailService.send("【易云】" + title, strContent, mails);// 邮件

			}
			if (mobiles!=null&&mobiles.size() != 0) {
				SMSService.send("尊敬的客户：您有["+model.size()+"]条备案信息的状态，已发生变更，您可以在管理控制台查看详情，如有问题请致电400-606-6396", mobiles);
			} // 短信
		} catch (Exception e) {
			throw new AppException(e.toString());
		}

		
	}
	
	private String websToStringAndBR(String webs){
		String returnstr="";
		if("".equals(webs)){
			return "";
		}
		String[] trre=webs.split(";");
		
		for(int i=0;i<trre.length;i++){
			if(null!=trre[i]){
				returnstr=returnstr+"<div>"+trre[i]+";</div>";
			}
		}
		return returnstr;
		
	}
	
	private String  treeToString(String []treemails){
		String mails="";
		for(String str:treemails){
			mails+=str+" ";
		}
		return mails;
		
	}
	
	
	
				@Override
			    public void newAddUnitTomail(List<MessageUnitModel> model)
			            throws AppException {
			        String tr = "<tr> <th style='padding-left:10px;'>客户名称</th><th>主办单位名称</th><th>网站备案号</th><th>主体负责人</th><th>备案类型</th></tr>";
			        String td = "<tr> <td style='padding-left:10px;' >orgName</td><td >unitName</td><td>webNo</td><td>unitFuzeName</td><td>recordType</td></tr>";
			        String tds = "<tr> <td style='padding-left:10px;' >orgName</td><td >unitName</td><td>webNo</td><td>unitFuzeName</td><td>recordType</td></tr>";
			        StringBuffer bs = new StringBuffer("");
			        String strContent = null;
			        String title = "备案申请";
			        try {
			            strContent = String.valueOf(getHtmlEmail());
			        } catch (Exception e1) {
			            log.error(e1.getMessage(), e1);
			            throw new AppException("获取邮件页面失败");
			        }
			        String name="";
			        String time="";
			        for (int i = 0; i < model.size(); i++) {
			            MessageUnitModel unitmodel = model.get(i);
			            String orgName = unitmodel.getOrgName();
			            String unitName = unitmodel.getUnitName();
			            String webNo=unitmodel.getWebNo();
			            String unitFuzeName=unitmodel.getUnitFuzeName();
			            String recordType=unitmodel.getRecordType();
			        
			            td = td.replaceAll("orgName", orgName.length() > 20 ? orgName.substring(0, 17) + "..." : orgName);
			            td = td.replaceAll("unitName",unitName.length() > 20 ? unitName.substring(0, 17) + "..." : unitName);
			            td = td.replaceAll("webNo", webNo);
			            td = td.replaceAll("unitFuzeName",unitFuzeName);
			            td = td.replaceAll("recordType", recordType);
			            name=unitName;
			            
			            time=unitmodel.getTime();
			            bs.append(td);
			            td = tds;
			            
			        }
			        SysDataTree tree=DictUtil.getDataTreeByNodeId("0020001");
			        String []treemails= tree.getPara1().split(";");
			        //String str="<p><br>如有问题，请及时联系备案专员<br>备案专员邮箱: "+treeToString(treemails)+"<br>联系电话： 086-010-6066396<br>QQ: 1064127799<p>";
			        strContent = strContent.replace("{trcontent}", tr);
			        strContent = strContent.replace("{content}", bs.toString());
			        
			        
			
			        strContent = strContent.replace("{userName}", "用户");
			        strContent = strContent.replace("{tablecontent}", "新的备案申请：  "+name+" 于 "+time+" 已提交备案申请，请登陆ECMC查看！");
			        //strContent = strContent.replace("</table>","</table>"+str);//将其他内容写在table外面
			        //strContent = strContent.replace("{texts}", str);
			
			
			        try {
			            List<String> mails=new ArrayList<>();
			            Collections.addAll(mails, treemails);
			            
			            if (mails!=null&&mails.size() != 0) {
			                mailService.send("【易云】" + title, strContent, mails);// 邮件
			
			            }
			            
			        } catch (Exception e) {
			            throw new AppException(e.toString());
			        }
			
			        
			    }

				@Override
				public void OperateMail(MessageOperateModel model) throws AppException {
					String url="";
					 StringBuffer bs = new StringBuffer("");
				        String strContent = null;
				        String title = "公有云平台运营情况汇总";
				        try {
				            strContent = String.valueOf(getOPMailHtml());
				        	url=urlMap.get("ecmcUrl")+"/ecmc/system/notice/getMailImg.do?fileid=";
				        } catch (Exception e1) {
				            log.error(e1.getMessage(), e1);
				            throw new AppException("获取邮件页面失败");
				        }
				      
				            
				        
				        SysDataTree tree=DictUtil.getDataTreeByNodeId("0020002");
				        String []treemails= tree.getPara1().split(";");
				        //String str="<p><br>如有问题，请及时联系备案专员<br>备案专员邮箱: "+treeToString(treemails)+"<br>联系电话： 086-010-6066396<br>QQ: 1064127799<p>";
				        strContent = strContent.replace("{onedaynewincome}",BigToString(model.getOnedaynewincome()));
				        strContent = strContent.replace("{onedayneworder}",String.valueOf(model.getOnedayneworder()));
				        strContent = strContent.replace("{onedaycus}",String.valueOf(model.getOnedaycus()));
				        strContent = strContent.replace("{onedaycusname}",String.valueOf(model.getOnedaycusname()));
				        strContent = strContent.replace("{onedaycusmoney}",BigToString(model.getOnedaycusmoney()));
				        strContent = strContent.replace("{onedayRecusname}",String.valueOf(model.getOnedayRecusname()));
				        strContent = strContent.replace("{onedayRecusmoney}",BigToString(model.getOnedayRecusmoney()));
				        
				        
				        strContent = strContent.replace("{onedaynewvmcount}",String.valueOf(model.getOnedaynewvmcount()));
				        strContent = strContent.replace("{onedaynewvolumecount}",String.valueOf(model.getOnedaynewvolumecount()));
				        strContent = strContent.replace("{onedaynewbackups}",String.valueOf(model.getOnedaynewbackups()));
				        strContent = strContent.replace("{onedaynewbalanc}",String.valueOf(model.getOnedaynewbalanc()));
				        strContent = strContent.replace("{onedaynewvpn}",String.valueOf(model.getOnedaynewvpn()));
				        strContent = strContent.replace("{onedaynewmysql}",String.valueOf(model.getOnedaynewmysql()));
				        /**
				         * 控制颜色
				         * */
				        strContent = strContent.replace("{coloronedaynewvmcount}",model.getOnedaynewvmcount()==0?"color-gray":"color-blue");
				        strContent = strContent.replace("{coloronedaynewbackups}",model.getOnedaynewbackups()==0?"color-gray":"color-blue");
				        strContent = strContent.replace("{coloronedaynewvpn}",model.getOnedaynewvpn()==0?"color-gray":"color-blue");
				        strContent = strContent.replace("{coloronedaynewvolumecount}",model.getOnedaynewvolumecount()==0?"color-gray":"color-blue");
				        strContent = strContent.replace("{coloronedaynewbalanc}",model.getOnedaynewbalanc()==0?"color-gray":"color-blue");
				        strContent = strContent.replace("{coloronedaynewmysql}",model.getOnedaynewmysql()==0?"color-gray":"color-blue");
				        
				        
				        strContent = strContent.replace("{alldaynewincome}",BigToString(model.getAlldaynewincome()));
				        strContent = strContent.replace("{alldayneworder}",String.valueOf(model.getAlldayneworder()));
				        strContent = strContent.replace("{alldaycus}",String.valueOf(model.getAlldaycus()));
				        strContent = strContent.replace("{alldaycusname}",String.valueOf(model.getAlldaycusname()));
				        strContent = strContent.replace("{alldaycusmoney}",BigToString(model.getAlldaycusmoney()));
				        strContent = strContent.replace("{alldayRecusname}",String.valueOf(model.getAlldayRecusname()));
				        strContent = strContent.replace("{alldayRecusmoney}",BigToString(model.getAlldayRecusmoney()));
				        strContent = strContent.replace("{alldayVmcusname}",String.valueOf(model.getAlldayVmcusname()));
				        strContent = strContent.replace("{alldayVmcount}",String.valueOf(model.getAlldayVmcount()));
				        strContent = strContent.replace("{alldayIPcusname}",String.valueOf(model.getAlldayIPcusname()));
				        strContent = strContent.replace("{alldayIPcount}",String.valueOf(model.getAlldayIPcount()));
				        strContent = strContent.replace("{alldaycpu}",String.valueOf(model.getAlldaycpu()));
				        strContent = strContent.replace("{alldaymemory}",String.valueOf(model.getAlldaymemory()));
				        strContent = strContent.replace("{alldaydisk}",String.valueOf(model.getAlldaydisk()));
				        strContent = strContent.replace("{alldayip}",String.valueOf(model.getAlldayip()));
				        strContent = strContent.replace("{alldayvmcount}",String.valueOf(model.getAlldayVmcount()));
				        strContent = strContent.replace("{alldaydatacount}",String.valueOf(model.getAlldaydatacount()));
				        strContent = strContent.replace("{alldaydatabackupcount}",String.valueOf(model.getAlldaydatabackupcount()));
				        strContent = strContent.replace("{alldaybalanccount}",String.valueOf(model.getAlldaybalanccount()));
				        strContent = strContent.replace("{alldayVPNcount}",String.valueOf(model.getAlldayVPNcount()));
				        strContent = strContent.replace("{alldayobject}",String.valueOf(model.getAlldayobject()));
				        strContent = strContent.replace("{alldaymysqlcount}",String.valueOf(model.getAlldaymysqlcount()));
				        strContent = strContent.replace("{allmoneyimg}",url+model.getAlldayfileidmoney());
				        strContent = strContent.replace("{allorderimg}",url+model.getAlldayfileidorder());
				        strContent = strContent.replace("{onemoneyimg}",url+model.getOnedayfileidmoney());
				        strContent = strContent.replace("{oneorderimg}",url+model.getOnedayfileidorder());
				      
//				        strContent = strContent.replace("{coloralldaycpu}",model.getOnedaynewmysql()==0?"color-gray":"color-blue");
//				        strContent = strContent.replace("{coloralldaymemory}",model.getOnedaynewmysql()==0?"color-gray":"color-blue");
//				        strContent = strContent.replace("{coloralldaydisk}",model.getOnedaynewmysql()==0?"color-gray":"color-blue");
//				        strContent = strContent.replace("{coloralldayip}",model.getOnedaynewmysql()==0?"color-gray":"color-blue");
//				        strContent = strContent.replace("{coloralldayvmcount}",model.getOnedaynewmysql()==0?"color-gray":"color-blue");
//				        strContent = strContent.replace("{coloralldaydatacount}",model.getOnedaynewmysql()==0?"color-gray":"color-blue");
//				        strContent = strContent.replace("{coloralldaydatabackupcount}",model.getOnedaynewmysql()==0?"color-gray":"color-blue");
//				        strContent = strContent.replace("{coloralldaybalanccount}",model.getOnedaynewmysql()==0?"color-gray":"color-blue");
//				        strContent = strContent.replace("{coloralldayVPNcount}",model.getOnedaynewmysql()==0?"color-gray":"color-blue");
//				        strContent = strContent.replace("{coloralldayobject}",model.getOnedaynewmysql()==0?"color-gray":"color-blue");
//				        strContent = strContent.replace("{coloralldaymysqlcount}",model.getOnedaynewmysql()==0?"color-gray":"color-blue");
				       
				
				        strContent = strContent.replace("{userName}", "用户");
				        strContent = strContent.replace("{oneday}",model.getDayTime()+"<span class='color-blue'>全天</span>公有云平台运营数据见下表，如有疑问，请登陆公有云运维中心查看。");
				        strContent = strContent.replace("{allday}", "截至<span class='color-blue'>"+model.getDayTime()+"00:00:00"+"</span>，公有云平台累计运营数据见下表：");
				
				        try {
				            List<String> mails=new ArrayList<>();
				            Collections.addAll(mails, treemails);
				            
				            if (mails!=null&&mails.size() != 0) {
				                mailService.send("【易云】" + title, strContent, mails);// 邮件
				
				            }
				            
				        } catch (Exception e) {
				            throw new AppException(e.toString());
				        }
					
					
					
				}
				
	private String BigToString(BigDecimal money) {
		String balancestr = new DecimalFormat("0.000").format(money);
		balancestr = balancestr.substring(0, balancestr.length() - 1);
		return balancestr;
	}

	private String setcolor(Integer one, Integer tow, String who) {

		if (who.equals("noobj")) {// cpu 内存 磁盘
			NumberFormat numberFormat = NumberFormat.getInstance();
			numberFormat.setMaximumFractionDigits(2);
			String result = numberFormat.format((float) one / (float) tow * 100);
			if (Integer.valueOf(result) >= 80) {
				return "color-error";
			} else {
				return "color-blue";
			}
		} else if (who.equals("obj")) {
			NumberFormat numberFormat = NumberFormat.getInstance();
			numberFormat.setMaximumFractionDigits(3);
			String result = numberFormat.format((double) one / (double) tow * 100);
			if (Double.valueOf(result) >= 80) {
				return "color-error";
			} else {
				return "color-blue";
			}
		} else {
			if (one < tow) {

				return "color-error";
			} else {
				return "color-blue";
			}

		}
	}

}
