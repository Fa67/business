package com.eayun.mail.service.impl;


import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.eayun.common.dao.ParamsMap;
import com.eayun.common.dao.QueryMap;
import com.eayun.common.dao.support.Page;
import com.eayun.common.util.BeanUtils;
import com.eayun.common.util.DateUtil;
import com.eayun.mail.dao.MailDao;
import com.eayun.mail.model.BaseMail;
import com.eayun.mail.model.EcmcMailBll;
import com.eayun.mail.service.EcmcMailBllService;
import com.eayun.mail.service.MailService;
import com.eayun.sys.model.SysDataTree;

@Service
@Transactional
public class EcmcMailBllServiceImpl implements EcmcMailBllService{
	@Autowired
	private MailDao mailDao;
	@Autowired
	private MailService mailService;
	
	private Map<String,String> getMailStatusMap(){
		Map<String,String> statusMap=new HashMap<String,String>();
		statusMap.put("0", "发送中");
		statusMap.put("1", "成功");
		statusMap.put("2", "失败");
		return statusMap;
	}
	@Override
	@SuppressWarnings("unchecked")
	public Page getMailList(Page page,ParamsMap paramsMap){
		StringBuffer strb = new StringBuffer();
		strb.append("from BaseMail where 1=1");
		List<Object> list = new ArrayList<Object>();
		Object bTime=paramsMap.getParams().get("beginTime");
		Object eTime=paramsMap.getParams().get("endTime");
		Object mailTitle = paramsMap.getParams().get("title");
		Date beginTime = bTime==null?null:DateUtil.timestampToDate(String.valueOf(bTime)); //开始时间
		Date endTime = eTime==null?null:DateUtil.timestampToDate(String.valueOf(eTime));//结束时间
		String status = (String) paramsMap.getParams().get("status");
		if(beginTime!=null){
			strb.append(" and insertTime >= ?");
			list.add(beginTime);
		}
		if(endTime!=null){
			strb.append(" and insertTime <= ?");
			list.add(endTime);

		}
		if(mailTitle != null) {
			strb.append(" and mail_detail like ? ");
			list.add("%\"title\":\"%" + mailTitle + "%\",\"links%");
		}
		if(!StringUtils.isEmpty(status)){
			strb.append(" and status = ?");
			list.add(status);
		}
		strb.append(" order by status , updateTime desc");
		QueryMap queryMap = new QueryMap();
		int pageSize = paramsMap.getPageSize();
		int pageNumber = paramsMap.getPageNumber();
		queryMap.setPageNum(pageNumber);
		queryMap.setCURRENT_ROWS_SIZE(pageSize);;
		
		page = mailDao.pagedQuery(strb.toString(),queryMap,list.toArray());
		List<BaseMail> baseMailList =  (List<BaseMail>) page.getResult();
		List<EcmcMailBll> mailList =new ArrayList<EcmcMailBll>();
		Map<String,String> statusMap=this.getMailStatusMap();
		for (BaseMail baseMail : baseMailList) {
			EcmcMailBll mail = new EcmcMailBll();
			BeanUtils.copyPropertiesByModel(mail, baseMail);
			//获取标题和邮件接收者。
			String str = mail.getDetail();
			String title = new String();
			int titleIndex = str.lastIndexOf("\"title\":");
			int linksIndex = str.lastIndexOf("\"links\":[");
			if (-1 != titleIndex && -1 != linksIndex) {
			    title = str.substring(titleIndex + 8, linksIndex - 1).replace("\"", "");
			} else {
			    title = "";
			}
            List<String> userMail = new ArrayList<String>();
            String emailStr = null;
            if (-1 != linksIndex) {
                emailStr =str.substring(linksIndex + 9, str.length() - 3).replace("\"", "");
            }
            if(emailStr!=null && emailStr.length()>0){
            	String str1[] =emailStr.split(",");
            	userMail= java.util.Arrays.asList(str1);
            }
			mail.setTitle(title);
			mail.setUserMailList(userMail);
			mail.setStatusName(statusMap.get(mail.getStatus()));
			mail.setCause(mail.getCause());
			mailList.add(mail);
		}
		page.setResult(mailList);
		return page;
	}
	@Override
	@SuppressWarnings("rawtypes")
	public List<SysDataTree> getMailStatusList(){
		List<SysDataTree> list = new ArrayList<SysDataTree>();
		Iterator it = this.getMailStatusMap().entrySet().iterator();
		while(it.hasNext()){
			SysDataTree sys = new SysDataTree();
			Entry entry = (Entry) it.next();
			sys.setNodeId(String.valueOf(entry.getKey()));
			sys.setNodeName(String.valueOf(entry.getValue()));
			list.add(sys);
		}
		return list;
	}
	@Override
	public boolean sendMailByUser(String mailId,List<String> userMailList) throws Exception{
		BaseMail baseMail=mailDao.findOne(mailId);
		String str = baseMail.getDetail();
		String title = new String();
		String content = new String();
		int titleIndex = str.lastIndexOf("\"title\":");
		if (-1 != titleIndex) {
		    title = str.substring(titleIndex + 8, str.lastIndexOf("\"links\":[")-1).replace("\"", "");
		    content = str.substring(str.lastIndexOf("\"content\":\"")+11, str.lastIndexOf("\",\"title\":")).replaceAll("\\\\", "");
		} else {
		    title = "";
		    content = str.substring(str.lastIndexOf("\"content\":\"")+11, str.lastIndexOf("\",\"links\":")).replaceAll("\\\\", "");
		}
//		String content = str.substring(str.lastIndexOf("\"content\":\"")+11, str.lastIndexOf("\",\"title\":")).replaceAll("\\\\", "");
		boolean bool=mailService.send(title, content, userMailList);
		return bool;
	}
}
