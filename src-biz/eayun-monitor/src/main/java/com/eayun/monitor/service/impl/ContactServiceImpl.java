package com.eayun.monitor.service.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.Query;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.eayun.common.dao.QueryMap;
import com.eayun.common.dao.support.Page;
import com.eayun.common.exception.AppException;
import com.eayun.common.util.BeanUtils;
import com.eayun.monitor.dao.ContactDao;
import com.eayun.monitor.dao.ContactGroupDao;
import com.eayun.monitor.dao.ContactGroupDetailDao;
import com.eayun.monitor.model.BaseContact;
import com.eayun.monitor.model.BaseContactGroup;
import com.eayun.monitor.model.BaseContactGroupDetail;
import com.eayun.monitor.model.Contact;
import com.eayun.monitor.model.ContactGroup;
import com.eayun.monitor.service.ContactService;

@Service
@Transactional
@SuppressWarnings({"unchecked","rawtypes"})
public class ContactServiceImpl implements ContactService {

    private static final Logger   log = LoggerFactory.getLogger(ContactServiceImpl.class);

    @Autowired
    private ContactDao            contactDao;
    @Autowired
    private ContactGroupDao       contactGroupDao;
    @Autowired
    private ContactGroupDetailDao contactGroupDetailDao;

    @Override
    public List<Contact> getContactList(String cusId) throws AppException {
        StringBuffer strb = new StringBuffer();
        strb.append("from BaseContact where cusId = ? order by name");
        List<BaseContact> baseContactList = contactDao.find(strb.toString(), cusId);

        List<Contact> contactList = new ArrayList<Contact>();
        for (BaseContact baseContact : baseContactList) {
            Contact contact = new Contact();
            BeanUtils.copyPropertiesByModel(contact, baseContact);
            contactList.add(contact);
        }
        return contactList;
    }

    @Override
    public Page getPagedContactList(String cusId, String name, Page page, QueryMap queryMap)
                                                                                            throws AppException {
        log.info("获取联系人列表");
        page = contactDao
            .pagedNativeQuery(
                "select c_id, c_name, c_phone, c_email, c_smsnotify, c_mailnotify, c_isadmin from ecsc_contact where c_cusid='"
                        + cusId + "' and binary c_name like '%" + name + "%' order by CONVERT( c_name USING gbk ) COLLATE gbk_chinese_ci ASC", queryMap);
        List newList = (List) page.getResult();
        for (int i = 0; i < newList.size(); i++) {
            Object[] objs = (Object[]) newList.get(i);
            Contact contact = new Contact();
            String id = String.valueOf(objs[0]);
            contact.setId(id);
            contact.setCusId(cusId);
            contact.setName(String.valueOf(objs[1]));
            contact.setPhone(String.valueOf(objs[2]));
            contact.setEmail(String.valueOf(objs[3]));
            contact.setSmsNotify(String.valueOf(objs[4]));
            contact.setMailNotify(String.valueOf(objs[5]));
            contact.setIsAdmin(String.valueOf(objs[6]));
            contact.setLinkedToAlarmRule(isLinkedToAlarmRule(id));

            newList.set(i, contact);
        }
        return page;
    }

    @Override
    public Contact addContact(Contact contact) throws AppException {
        log.info("添加联系人");
        BaseContact baseContact = new BaseContact();
        BeanUtils.copyPropertiesByModel(baseContact, contact);
        contactDao.saveEntity(baseContact);
        BeanUtils.copyPropertiesByModel(contact, baseContact);
        return contact;
    }

    /**
     * 更新通知方法
     * @param mode sms or email
     * @param isChecked
     * @param contactId
     * @return
     * @throws AppException
     * @see com.eayun.monitor.service.ContactService#updateNotifyMethod(java.lang.String, java.lang.String, java.lang.String)
     */
    @Override
    public boolean updateNotifyMethod(String mode, String isChecked, String contactId)
                                                                                      throws AppException {
        log.info("更新通知方式");
        BaseContact baseContact = contactDao.findOne(contactId);
        if (mode.equals("sms")) {
            baseContact.setSmsNotify(isChecked);
        } else if (mode.equals("email")) {
            baseContact.setMailNotify(isChecked);
        }
        contactDao.saveOrUpdate(baseContact);
        return true;
    }

    @Override
    public boolean updateContact(Contact contact) throws AppException {
        log.info("更新联系人");
        BaseContact baseContact = new BaseContact();
        BeanUtils.copyPropertiesByModel(baseContact, contact);
        contactDao.saveOrUpdate(baseContact);
        BeanUtils.copyPropertiesByModel(contact, baseContact);
        return true;
    }

    @Override
    public boolean deleteContact(String contactId) throws AppException {
        log.info("删除联系人");
        BaseContact baseContact = contactDao.findOne(contactId);
        
        //还要删除ecsc_contactgroupdetail中的该联系人的数据
        StringBuffer sb = new StringBuffer();
        sb.append(" from BaseContactGroupDetail where contactId=?");
        List<BaseContactGroupDetail> detailList = contactGroupDetailDao.find(sb.toString(), contactId);
        for (BaseContactGroupDetail baseContactGroupDetail : detailList) {
            contactGroupDetailDao.delete(baseContactGroupDetail);
        }
        
        contactDao.delete(baseContact);
        return true;
    }

    @Override
    public boolean isLinkedToAlarmRule(String contactId) throws AppException {
        log.info("判断联系人是否关联报警规则");
        boolean isLinkedToAlarmRule = false;
        StringBuffer sb = new StringBuffer();
        sb.append("select * from ecsc_alarmcontact where ac_contactid=?");
        Query query = contactDao.createSQLNativeQuery(sb.toString(), contactId);
        List list = query.getResultList();
        //如果报警联系人中存在该联系人，即联系人关联了报警规则，则不可删除
        if (list.size() > 0) {
            isLinkedToAlarmRule = true;
        }
        return isLinkedToAlarmRule;
    }

    /**
     * 获取联系组列表
     * @param cusId
     * @return
     * @throws AppException
     * @see com.eayun.monitor.service.ContactService#getContactGroupList(java.lang.String)
     */
    @Override
    public List<ContactGroup> getContactGroupList(String cusId) throws AppException {
        StringBuffer strb = new StringBuffer();
        strb.append("select cg_id, cg_cusid, cg_name  from ecsc_contactgroup where cg_cusid='"+cusId+"' ORDER BY CONVERT( cg_name USING gbk ) COLLATE gbk_chinese_ci ASC");
        Query query = contactDao.createSQLNativeQuery(strb.toString());
        List list =  query.getResultList();
        List<ContactGroup> ctcGrpList = new ArrayList<ContactGroup>();
        for(int i=0; i<list.size(); i++){
            Object[] objs = (Object[]) list.get(i);
            BaseContactGroup baseCtg = new BaseContactGroup();
            String id = String.valueOf(objs[0]);
            baseCtg.setId(id);
            baseCtg.setCusId(cusId);
            baseCtg.setName(String.valueOf(objs[2]));
            ContactGroup ctcGrp = new ContactGroup();
            BeanUtils.copyPropertiesByModel(ctcGrp, baseCtg);
            int num = getCtcNumInGroup(baseCtg.getId());
            ctcGrp.setContactNum(num);
            ctcGrpList.add(ctcGrp);
            
        }
        return ctcGrpList;
    }

    private int getCtcNumInGroup(String groupId) {
        StringBuffer sb = new StringBuffer();
        sb.append("from BaseContactGroupDetail where groupId=?");
        List<BaseContactGroupDetail> baseCtcGrpDetailList = contactGroupDetailDao.find(sb.toString(), groupId);
        return baseCtcGrpDetailList.size();
    }

    @Override
    public ContactGroup addContactGroup(ContactGroup contactGroup) throws AppException {
        log.info("添加联系组");
        BaseContactGroup baseCtcGrp = new BaseContactGroup();
        BeanUtils.copyPropertiesByModel(baseCtcGrp, contactGroup);
        contactDao.saveEntity(baseCtcGrp);
        BeanUtils.copyPropertiesByModel(contactGroup, baseCtcGrp);
        return contactGroup;
    }

    @Override
    public boolean checkContactGroupName(String cusId, String contactGroupName, String contactGroupId) throws AppException {
        log.info("验证联系组重复");

        StringBuffer sb = new StringBuffer();
        sb.append(" from BaseContactGroup where cusId = ?  and name = ? ");
        List<String> param = new ArrayList<String>();
        param.add(cusId);
        param.add(contactGroupName);
        if(contactGroupId!=null){
            sb.append(" and id<>?");
            param.add(contactGroupId);
        }

        List<BaseContactGroup> ctcGrpList = contactGroupDao.find(sb.toString(), param.toArray());
        if (ctcGrpList.isEmpty()) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * 获取组内联系人
     * @param cusId
     * @param contactGroupName
     * @return
     * @throws AppException
     * @see com.eayun.monitor.service.ContactService#getContactListInGroup(java.lang.String, java.lang.String)
     */
    @Override
    public List<Contact> getContactListInGroup(String cusId, String contactGroupName) throws AppException {
        List<Contact> contactList = new ArrayList<Contact>();
        if(contactGroupName.equals("default")){
            if(!hasDefaultGroup(cusId)){
                createDefaultGroup(cusId);
            }
        }
        //根据组名，先找组的id，然后根据id去联查BaseContactGroupDetail中的联系人
        StringBuffer sb = new StringBuffer();
        sb.append("select c_id, c_name, c_phone, c_email, c_smsnotify, c_mailnotify from ecsc_contact where c_id in ("
                + "select grpdetail.cgd_contactid from ecsc_contactgroupdetail grpdetail "
                + "left join ecsc_contactgroup grp "
                + "on grp.cg_id = grpdetail.cgd_groupid and grp.cg_name='"+contactGroupName+"' ) ORDER BY c_isadmin DESC , CONVERT( c_name USING gbk ) COLLATE gbk_chinese_ci ASC");

        Query query = contactDao.createSQLNativeQuery(sb.toString());
        List list =  query.getResultList();
        for(int i=0; i<list.size(); i++){
            Object[] objs = (Object[]) list.get(i);
            Contact contact = new Contact();
            String id = String.valueOf(objs[0]);
            contact.setId(id);
            contact.setName(String.valueOf(objs[1]));
            contact.setPhone(String.valueOf(objs[2]));
            contact.setEmail(String.valueOf(objs[3]));
            contact.setSmsNotify(String.valueOf(objs[4]));
            contact.setMailNotify(String.valueOf(objs[5]));
            contact.setLinkedToAlarmRule(isLinkedToAlarmRule(id));
            contactList.add(contact);
        }
        return contactList;
    }

    /**
     *  创建默认分组及填充默认联系人
     * @param cusId 
     */
	private void createDefaultGroup(String cusId) {
        BaseContactGroup baseCtcGrp = new BaseContactGroup();
        baseCtcGrp.setCusId(cusId);
        baseCtcGrp.setName("default");
        contactGroupDao.saveOrUpdate(baseCtcGrp);
        String ctcGrpId = baseCtcGrp.getId();

        StringBuffer sb = new StringBuffer();
        sb.append("select user_account, user_phone, user_email from sys_selfuser where cus_id= ? and is_admin='1'");
        Query query = contactGroupDao.createSQLNativeQuery(sb.toString(), cusId);
        List list = query.getResultList();
        for (int i = 0; i < list.size(); i++) {
            Object[] objs = (Object[]) list.get(i);
            BaseContact baseCtc = new BaseContact();
            baseCtc.setCusId(cusId);
            baseCtc.setMailNotify("1");
            baseCtc.setSmsNotify("1");
            baseCtc.setName(objs[0].toString());
            baseCtc.setPhone(objs[1].toString());
            baseCtc.setEmail(objs[2].toString());
            baseCtc.setIsAdmin("1");

            contactDao.saveOrUpdate(baseCtc);

            String ctcId = baseCtc.getId();
            BaseContactGroupDetail baseCtcGrpDetail = new BaseContactGroupDetail();
            baseCtcGrpDetail.setGroupId(ctcGrpId);
            baseCtcGrpDetail.setContactId(ctcId);

            contactGroupDetailDao.saveOrUpdate(baseCtcGrpDetail);
        }
    }

    /**
     * 判断是否有默认分组，如果没有，则创建
     * @param cusId 
     * @return
     */
    private boolean hasDefaultGroup(String cusId) {
        StringBuffer sb = new StringBuffer();
        sb.append("select * from ecsc_contactgroup where cg_cusid= ? and cg_name = ? ");
        Query query = contactGroupDao.createSQLNativeQuery(sb.toString(), cusId, "default");
        List list = query.getResultList();
        if (list.isEmpty()) {
            return false;
        } else {
            return true;
        }
    }

    @Override
    public Page getPagedContactListInGroup(String cusId, String contactGroupName, Page page, QueryMap queryMap)
                                                                                                   throws AppException {
        if(contactGroupName.equals("default")){
            if(!hasDefaultGroup(cusId)){
                createDefaultGroup(cusId);
            }
        }
        StringBuffer ctcgrpSb = new StringBuffer();
        ctcgrpSb.append("select id from BaseContactGroup where name=? and cusId=?");
        String ctcGrpId = String.valueOf(contactGroupDao.find(ctcgrpSb.toString(), contactGroupName,cusId).get(0));
        //根据组名，先找组的id，然后根据id去联查BaseContactGroupDetail中的联系人
        StringBuffer sb = new StringBuffer();
        sb.append("SELECT c_id, c_name, c_phone, c_email, c_smsnotify, c_mailnotify,c_isadmin FROM ecsc_contact WHERE c_cusid='"+cusId+"' and c_id in ("
                + "SELECT  cgd_contactid  FROM  ecsc_contactgroupdetail  WHERE cgd_groupid IN ("
                + "SELECT cg_id FROM ecsc_contactgroup WHERE cg_name = '"+contactGroupName+"')) ORDER BY c_isadmin DESC , CONVERT( c_name USING gbk ) COLLATE gbk_chinese_ci ASC");
        page = contactDao.pagedNativeQuery(sb.toString(), queryMap);
        List list =  (List) page.getResult();
        for(int i=0; i<list.size(); i++){
            Object[] objs = (Object[]) list.get(i);
            Contact contact = new Contact();
            String id = String.valueOf(objs[0]);
            contact.setId(id);
            contact.setCusId(cusId);
            contact.setName(String.valueOf(objs[1]));
            contact.setPhone(String.valueOf(objs[2]));
            contact.setEmail(String.valueOf(objs[3]));
            contact.setSmsNotify(String.valueOf(objs[4]));
            contact.setMailNotify(String.valueOf(objs[5]));
            contact.setIsAdmin(String.valueOf(objs[6]));
            contact.setLinkedToAlarmRule(isLinkedToAlarmRule(id));
            contact.setCurrentCtcGrpId(ctcGrpId);
            list.set(i,contact);
        }
        return page;
    }

    @Override
    public boolean updateContactGroup(ContactGroup contactGroup) throws AppException {
         log.info("更新联系组");
        BaseContactGroup baseCtcGrp = new BaseContactGroup();
        BeanUtils.copyPropertiesByModel(baseCtcGrp, contactGroup);
        contactGroupDao.saveOrUpdate(baseCtcGrp);
        BeanUtils.copyPropertiesByModel(contactGroup, baseCtcGrp);
        return true;
    }

    @Override
    public boolean deleteContactGroup(String ctcGrpId) throws AppException {
        log.info("删除联系组");
        BaseContactGroup baseCtcGrp = contactGroupDao.findOne(ctcGrpId);
        contactGroupDao.delete(baseCtcGrp);
        return true;
    }

    @Override
    public List<Contact> getContactListOutOfGroup(String cusId, String contactGroupName)
                                                                                        throws AppException {
        List<Contact> ctcList = new ArrayList<Contact>();
        StringBuffer sb = new StringBuffer();
        sb.append("SELECT c_id, c_name, c_phone, c_email, c_smsnotify, c_mailnotify,c_isadmin FROM ecsc_contact WHERE c_cusid = '"+cusId+"' and c_id not in ("
                + "SELECT  cgd_contactid  FROM  ecsc_contactgroupdetail  WHERE cgd_groupid IN ("
                + "SELECT cg_id FROM ecsc_contactgroup WHERE cg_name = '"+contactGroupName+"')) ORDER BY CONVERT( c_name USING gbk ) COLLATE gbk_chinese_ci ASC");
        Query query = contactDao.createSQLNativeQuery(sb.toString());
        List list = query.getResultList();
        for(int i=0; i<list.size(); i++){
            Object[] objs = (Object[]) list.get(i);
            Contact contact = new Contact();
            String id = String.valueOf(objs[0]);
            contact.setId(id);
            contact.setCusId(cusId);
            contact.setName(String.valueOf(objs[1]));
            contact.setPhone(String.valueOf(objs[2]));
            contact.setEmail(String.valueOf(objs[3]));
            contact.setSmsNotify(String.valueOf(objs[4]));
            contact.setMailNotify(String.valueOf(objs[5]));
            contact.setIsAdmin(String.valueOf(objs[6]));
            contact.setLinkedToAlarmRule(isLinkedToAlarmRule(id));
            
            ctcList.add(contact);
        }
        return ctcList;
    }

    @Override
    public boolean removeContactFromGroup(String contactGroupId, String contactId)
                                                                                        throws AppException {
        StringBuffer sb = new StringBuffer();
        sb.append("delete from ecsc_contactgroupdetail where cgd_groupid=? and cgd_contactid=?");
        contactGroupDetailDao.execSQL(sb.toString(), contactGroupId, contactId);
        return true;
    }

    @Override
    public boolean addContact2Group(String cusId, String contactGroupName, String contactId) throws AppException {
        StringBuffer sb = new StringBuffer();
        sb.append(" from BaseContactGroup where cusId=? and name=?");
        org.hibernate.Query query = contactGroupDao.createQuery(sb.toString(), cusId, contactGroupName);
        BaseContactGroup baseCtcGrp = (BaseContactGroup) query.list().get(0);
        BaseContactGroupDetail baseCtcGrpDetail = new BaseContactGroupDetail();
        baseCtcGrpDetail.setGroupId(baseCtcGrp.getId());
        baseCtcGrpDetail.setContactId(contactId);
        contactGroupDetailDao.saveOrUpdate(baseCtcGrpDetail);
        return true;
    }

    @Override
    public boolean checkContactName(String cusId, String contactName, String contactId) throws AppException {
        log.info("验证联系人名称重复");

        StringBuffer sb = new StringBuffer();
        sb.append(" from BaseContact where cusId = ?  and name = ? ");
        List<String> param = new ArrayList<String>();
        param.add(cusId);
        param.add(contactName);
        if(contactId!=null){
            sb.append(" and id<>?");
            param.add(contactId);
        }

        List<BaseContact> ctcList = contactDao.find(sb.toString(), param.toArray());
        if (ctcList.isEmpty()) {
            return true;
        } else {
            return false;
        }
    }

    @Override
    public Map<String, String> getAdminContact(String customerId, String projectId) {
        StringBuffer sb = new StringBuffer();
        sb.append("SELECT ")
                .append("   cus.cus_email, ")
                .append("   cus.cus_phone, ")
                .append("   prj.prj_name ")
                .append(" FROM ")
                .append("   sys_selfcustomer cus ")
                .append(" LEFT JOIN cloud_project prj ON cus.cus_id = prj.customer_id ")
                .append(" WHERE ")
                .append("   cus.cus_id = ? ")
                .append(" AND prj.prj_id = ? ");
        Query query = contactDao.createSQLNativeQuery(sb.toString(), customerId, projectId);
        List<Object[]> list = query.getResultList();
        Object[] objs = list.get(0);
        String email = objs[0] == null ? "" : objs[0].toString();
        String phone = objs[1] == null ? "" : objs[1].toString();
        String projectName = objs[2] == null ? "" : objs[2].toString();

        Map<String,String> map = new HashMap<String, String>();
        map.put("email",email);
        map.put("phone",phone);
        map.put("projectName",projectName);

        return map;
    }
}
