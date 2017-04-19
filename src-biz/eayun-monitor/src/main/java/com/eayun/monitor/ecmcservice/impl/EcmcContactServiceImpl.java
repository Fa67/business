package com.eayun.monitor.ecmcservice.impl;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.Query;

import com.eayun.monitor.model.*;
import com.fasterxml.jackson.databind.util.BeanUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.eayun.common.dao.QueryMap;
import com.eayun.common.dao.support.Page;
import com.eayun.common.exception.AppException;
import com.eayun.monitor.dao.EcmcContactDao;
import com.eayun.monitor.dao.EcmcContactGroupDao;
import com.eayun.monitor.dao.EcmcContactGroupDetailDao;
import com.eayun.monitor.ecmcservice.EcmcContactService;
import com.eayun.monitor.service.ContactService;

@Service
@Transactional
@SuppressWarnings({ "unchecked", "rawtypes" })
public class EcmcContactServiceImpl implements EcmcContactService {

	private static final Logger log = LoggerFactory.getLogger(EcmcContactServiceImpl.class);

	@Autowired
	private ContactService contectService;
	@Autowired
	private EcmcContactDao ecmcContactDao;
	@Autowired
	private EcmcContactGroupDao ecmcContactGroupDao;
	@Autowired
	private EcmcContactGroupDetailDao ecmcContactGroupDetailDao;

	@Override
	public Page getPagedContactList(String name, String type, String cusId, Page page, QueryMap queryMap)
			throws AppException {
		if (type.equals("1")) {
			// 查询客户自建的联系人
			return contectService.getPagedContactList(cusId, name, page, queryMap);
		} else {
			// 查询运维创建的联系人
			return getPagedDevOpsContactList(name, page, queryMap);
		}
	}

	/**
	 * 获取运维创建联系人列表
	 * 
	 * @param name
	 * @param page
	 * @param queryMap
	 * @return
	 */
	private Page getPagedDevOpsContactList(String name, Page page, QueryMap queryMap) {
		page = ecmcContactDao.pagedNativeQuery("select mc_id, mc_name, mc_phone, mc_email, mc_smsnotify, mc_mailnotify "
				+ "from ecmc_contact where binary mc_name like '%" + name + "%' "
				+ "order by CONVERT( mc_name USING gbk ) COLLATE gbk_chinese_ci ASC", queryMap);
		List newList = (List) page.getResult();
		for (int i = 0; i < newList.size(); i++) {
			Object[] objs = (Object[]) newList.get(i);
			EcmcContact ecmcContact = new EcmcContact();
			String id = String.valueOf(objs[0]);
			ecmcContact.setId(id);
			ecmcContact.setName(String.valueOf(objs[1]));
			ecmcContact.setPhone(String.valueOf(objs[2]));
			ecmcContact.setEmail(String.valueOf(objs[3]));
			ecmcContact.setSmsNotify(String.valueOf(objs[4]));
			ecmcContact.setMailNotify(String.valueOf(objs[5]));
			ecmcContact.setLinkedToAlarmRule(isContactLinkedToAlarmRule(id));

			newList.set(i, ecmcContact);
		}
		return page;
	}

	/**
	 * 判断联系人是否关联报警规则
	 * 
	 * @param id
	 *            联系人ID
	 * @return
	 */
	private boolean isContactLinkedToAlarmRule(String id) {
		boolean isLinkedToAlarmRule = false;
		StringBuffer sb = new StringBuffer();
		sb.append("select * from ecmc_alarmcontact where ac_contactid=?");
		Query query = ecmcContactDao.createSQLNativeQuery(sb.toString(), id);
		List list = query.getResultList();
		// 如果报警联系人中存在该联系人，即联系人关联了报警规则，则不可删除
		if (list.size() > 0) {
			isLinkedToAlarmRule = true;
		}
		return isLinkedToAlarmRule;
	}

	@Override
	public EcmcContact addContact(EcmcContact ecmcContact) throws AppException {
		BaseEcmcContact baseEcmcContact = new BaseEcmcContact();
		BeanUtils.copyProperties(ecmcContact, baseEcmcContact);
		baseEcmcContact = ecmcContactDao.save(baseEcmcContact);
		BeanUtils.copyProperties(baseEcmcContact, ecmcContact);
		return ecmcContact;

	}

	@Override
	public EcmcContact editContact(EcmcContact ecmcContact) throws AppException {
		BaseEcmcContact baseEcmcContact = new BaseEcmcContact();
		BeanUtils.copyProperties(ecmcContact, baseEcmcContact);
		ecmcContactDao.saveOrUpdate(baseEcmcContact);
		BeanUtils.copyProperties(baseEcmcContact, ecmcContact);
		return ecmcContact;
	}

	@Override
	public void deleteContact(String contactId) throws AppException {
		BaseEcmcContact baseEcmcContact = ecmcContactDao.findOne(contactId);
		// 首先删除ecmc_contactgroupdetail中的该联系人的数据
		StringBuffer sb = new StringBuffer();
		sb.append(" from BaseEcmcContactGroupDetail where contactId=?");
		List<BaseEcmcContactGroupDetail> detailList = ecmcContactGroupDetailDao.find(sb.toString(), contactId);
		for (BaseEcmcContactGroupDetail baseEcmcContactGroupDetail : detailList) {
			ecmcContactGroupDetailDao.delete(baseEcmcContactGroupDetail);
		}
		ecmcContactDao.delete(baseEcmcContact);
	}

	@Override
	public void updateNoticeMethod(String type, String isChecked, String contactId) throws AppException{
		BaseEcmcContact baseEcmcContact = ecmcContactDao.findOne(contactId);
		if (type.equals("sms")) {
			baseEcmcContact.setSmsNotify(isChecked);
		} else if (type.equals("email")) {
			baseEcmcContact.setMailNotify(isChecked);
		}
		ecmcContactDao.saveOrUpdate(baseEcmcContact);
	}

	@Override
	public boolean checkContactName(String contactId, String contactName) throws AppException {
        StringBuffer sb = new StringBuffer();
        sb.append(" from BaseEcmcContact where name = ? ");
        List<String> param = new ArrayList<String>();
        param.add(contactName);
        if(contactId!=null){
            sb.append(" and id<>?");
            param.add(contactId);
        }

        List<BaseEcmcContact> ctcList = ecmcContactDao.find(sb.toString(), param.toArray());
        if (ctcList.isEmpty()) {
            return true;
        } else {
            return false;
        }
		
	}

	@Override
	public List<EcmcContactGroup> getContactGroupList() throws AppException{
		StringBuffer sb = new StringBuffer();
		sb.append("select mg_id, mg_name  from ecmc_contactgroup ORDER BY CONVERT( mg_name USING gbk ) COLLATE gbk_chinese_ci ASC");
		Query query = ecmcContactGroupDao.createSQLNativeQuery(sb.toString());
		List list = query.getResultList();
		List<EcmcContactGroup> ctcGrpList = new ArrayList<EcmcContactGroup>();
		for(int i=0; i<list.size(); i++){
			Object[] objs = (Object[]) list.get(i);
            BaseEcmcContactGroup baseCtg = new BaseEcmcContactGroup();
            baseCtg.setId(String.valueOf(objs[0]));
            baseCtg.setName(String.valueOf(objs[1]));
            EcmcContactGroup ctcGrp = new EcmcContactGroup();
            BeanUtils.copyProperties(baseCtg, ctcGrp);
            int num = getCtcNumInGroup(baseCtg.getId());
            ctcGrp.setContactNum(num);
            ctcGrpList.add(ctcGrp);
		}
		return ctcGrpList;
	}
	
	private int getCtcNumInGroup(String groupId) {
        StringBuffer sb = new StringBuffer();
        sb.append("from BaseEcmcContactGroupDetail where groupId=?");
        List<BaseEcmcContactGroupDetail> baseCtcGrpDetailList = ecmcContactGroupDetailDao.find(sb.toString(), groupId);
        return baseCtcGrpDetailList.size();
    }

	@Override
	public EcmcContactGroup addContactGroup(EcmcContactGroup ecmcContactGroup) throws AppException {
        BaseEcmcContactGroup baseCtcGrp = new BaseEcmcContactGroup();
        BeanUtils.copyProperties(ecmcContactGroup, baseCtcGrp);
        ecmcContactDao.saveEntity(baseCtcGrp);
        BeanUtils.copyProperties(baseCtcGrp,ecmcContactGroup);
        return ecmcContactGroup;
	}

	@Override
	public boolean checkContactGroupName(String contactGroupId, String contactGroupName) throws AppException {
        StringBuffer sb = new StringBuffer();
        sb.append(" from BaseEcmcContactGroup where name = ? ");
        List<String> param = new ArrayList<String>();
        param.add(contactGroupName);
        if(contactGroupId!=null){
            sb.append(" and id<>?");
            param.add(contactGroupId);
        }
        List<BaseEcmcContactGroup> ctcGrpList = ecmcContactGroupDao.find(sb.toString(), param.toArray());
        if (ctcGrpList.isEmpty()) {
            return true;
        } else {
            return false;
        }
	}

	@Override
	public EcmcContactGroup editContactGroup(EcmcContactGroup ecmcContactGroup) throws AppException {
		BaseEcmcContactGroup baseEcmcCtcGrp = new BaseEcmcContactGroup();
		BeanUtils.copyProperties(ecmcContactGroup, baseEcmcCtcGrp);
		ecmcContactGroupDao.saveOrUpdate(baseEcmcCtcGrp);
		BeanUtils.copyProperties(baseEcmcCtcGrp, ecmcContactGroup);
		return ecmcContactGroup;
	}

	@Override
	public void deleteContactGroup(String contactGroupId) throws AppException {
		log.info("删除联系组");
        BaseEcmcContactGroup baseCtcGrp = ecmcContactGroupDao.findOne(contactGroupId);
        ecmcContactGroupDao.delete(baseCtcGrp);
	}

	@Override
	public Page getPagedContactListInGroup(String groupId, Page page, QueryMap queryMap) throws AppException {
		//查询groupDetail表确定组内联系人ID，然后查询联系人信息。
		StringBuffer sb = new StringBuffer();
		sb.append("SELECT mc_id, mc_name, mc_phone, mc_email, mc_smsnotify, mc_mailnotify "
				+ " FROM ecmc_contact WHERE mc_id IN ("
                + "	SELECT  mgd_contactid  FROM  ecmc_contactgroupdetail  WHERE mgd_groupid='"+groupId+"')"
                + " ORDER BY CONVERT( mc_name USING gbk ) COLLATE gbk_chinese_ci ASC");
		page = ecmcContactDao.pagedNativeQuery(sb.toString(), queryMap);
		List list =  (List) page.getResult();
        for(int i=0; i<list.size(); i++){
            Object[] objs = (Object[]) list.get(i);
            EcmcContact contact = new EcmcContact();
            String id = String.valueOf(objs[0]);
            contact.setId(id);
            contact.setName(String.valueOf(objs[1]));
            contact.setPhone(String.valueOf(objs[2]));
            contact.setEmail(String.valueOf(objs[3]));
            contact.setSmsNotify(String.valueOf(objs[4]));
            contact.setMailNotify(String.valueOf(objs[5]));
            contact.setLinkedToAlarmRule(isContactLinkedToAlarmRule(id));
            contact.setCurrentCtcGrpId(groupId);
            list.set(i,contact);
        }
        return page;
	}

	@Override
	public List<EcmcContact> getContactListOutOfGroup(String groupId) throws AppException {
		StringBuffer sb = new StringBuffer();
		sb.append("SELECT mc_id, mc_name, mc_phone, mc_email, mc_smsnotify, mc_mailnotify "
				+ " FROM ecmc_contact WHERE mc_id NOT IN ("
                + "	SELECT  mgd_contactid  FROM  ecmc_contactgroupdetail  WHERE mgd_groupid='"+groupId+"')"
                + " ORDER BY CONVERT( mc_name USING gbk ) COLLATE gbk_chinese_ci ASC");
		Query query = ecmcContactDao.createSQLNativeQuery(sb.toString());
		List list = query.getResultList();
		List<EcmcContact> ctcList = new ArrayList<EcmcContact>();
        for(int i=0; i<list.size(); i++){
        	Object[] objs = (Object[]) list.get(i);
            EcmcContact contact = new EcmcContact();
            String id = String.valueOf(objs[0]);
            contact.setId(id);
            contact.setName(String.valueOf(objs[1]));
            contact.setPhone(String.valueOf(objs[2]));
            contact.setEmail(String.valueOf(objs[3]));
            contact.setSmsNotify(String.valueOf(objs[4]));
            contact.setMailNotify(String.valueOf(objs[5]));
            contact.setLinkedToAlarmRule(isContactLinkedToAlarmRule(id));
            
            ctcList.add(contact);
        }
        return ctcList;
	}

	@Override
	public void removeContactFromGroup(String contactGroupId, String contactId) throws AppException {
		StringBuffer sb = new StringBuffer();
        sb.append("delete from ecmc_contactgroupdetail where mgd_groupid=? and mgd_contactid=?");
        ecmcContactGroupDetailDao.execSQL(sb.toString(), contactGroupId, contactId);
	}

	@Override
	public void addContactToGroup(String contactGroupId, String contactId) throws AppException {
		BaseEcmcContactGroup ctcGrp = ecmcContactGroupDao.findOne(contactGroupId);
		if(ctcGrp==null){
			throw new AppException("",new String[] {"请选择联系组！"});
		}
        BaseEcmcContactGroupDetail baseCtcGrpDetail = new BaseEcmcContactGroupDetail();
        baseCtcGrpDetail.setGroupId(contactGroupId);
        baseCtcGrpDetail.setContactId(contactId);
        ecmcContactGroupDetailDao.saveOrUpdate(baseCtcGrpDetail);
		
	}
	

}
