package com.eayun.virtualization.service.impl;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.persistence.Query;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.eayun.common.ConstantClazz;
import com.eayun.common.constant.RedisKey;
import com.eayun.common.dao.support.Page;
import com.eayun.common.exception.AppException;
import com.eayun.common.redis.JedisUtil;
import com.eayun.common.util.BeanUtils;
import com.eayun.customer.model.BaseCustomer;
import com.eayun.customer.model.Customer;
import com.eayun.customer.model.User;
import com.eayun.customer.serivce.CustomerService;
import com.eayun.customer.serivce.UserService;
import com.eayun.virtualization.bean.ResourceTag;
import com.eayun.virtualization.dao.TagDao;
import com.eayun.virtualization.dao.TagGroupDao;
import com.eayun.virtualization.dao.TagResourceDao;
import com.eayun.virtualization.model.BaseTag;
import com.eayun.virtualization.model.BaseTagGroup;
import com.eayun.virtualization.model.BaseTagResource;
import com.eayun.virtualization.model.Tag;
import com.eayun.virtualization.model.TagComparator;
import com.eayun.virtualization.model.TagGroup;
import com.eayun.virtualization.model.TagGroupComparator;
import com.eayun.virtualization.model.TagResource;
import com.eayun.virtualization.service.TagService;

@Service
@Transactional
@SuppressWarnings("unchecked")
public class TagServiceImpl implements TagService {
    private static final Logger log     = LoggerFactory.getLogger(TagServiceImpl.class);
    @Autowired
    private TagGroupDao         tagGroupDao;
    @Autowired
    private TagDao              tagDao;
    @Autowired
    private TagResourceDao      tagResourceDao;
    @Autowired
    private JedisUtil           jedisUtil;
    @Autowired
    private UserService         userService;
    @Autowired
    private CustomerService     customerService;

    /*-------------------------标签类别相关 BEGIN----------------------------*/
    /**
     * 查询标签类别的全部内容，用于左侧标签类别列表和标签类别详情的展示
     * @param cusId
     * @return
     * @see com.eayun.virtualization.service.TagService#findTagGroup(java.lang.String)
     */
    @Override
    public List<TagGroup> getTagGroupList(String cusId) {
        List<TagGroup> tagGroupList = new ArrayList<TagGroup>();
        Set<String> tagGroupSet;
        try {
            tagGroupSet = jedisUtil.getSet(RedisKey.CUS_TGRP + cusId);
            for (String tagGroupId : tagGroupSet) {
                TagGroup tgrp = getTagGroupById(tagGroupId);
                tagGroupList.add(tgrp);
            }
            TagGroupComparator tgc = new TagGroupComparator();
            Collections.sort(tagGroupList, tgc);
        } catch (Exception e) {
            log.error("获取标签类别列表异常",e);
            throw new AppException("获取标签类别列表异常");
        }
        return tagGroupList;
    }
    
    @Override
    public List<TagGroup> getEnabledTagGroupListByCusID(String cusId) {
        List<TagGroup> tagGroupList = new ArrayList<TagGroup>();
        Set<String> tagGroupSet;
        try {
            tagGroupSet = jedisUtil.getSet(RedisKey.CUS_TGRP  + cusId);
            for (String tagGroupId : tagGroupSet) {
                TagGroup tgrp = getTagGroupById(tagGroupId);
                if(tgrp.getEnabled().equals("1")){
                    tagGroupList.add(tgrp);
                }
            }
            TagGroupComparator tgc = new TagGroupComparator();
            Collections.sort(tagGroupList, tgc);
        } catch (Exception e) {
            log.error("获取标签类别列表异常",e);
            throw new AppException("获取标签类别列表异常");
        }
        return tagGroupList;
    }

    /**
     * 在资源管理界面的标签页面，可选择的标签类别下拉框中必须是已启用的。
     * @param cusId
     * @return
     * @see com.eayun.virtualization.service.TagService#getEnabledTagGroupList(java.lang.String)
     */
    @Override
    public List<TagGroup> getEnabledTagGroupList(String cusId, String resType) {
        List<TagGroup> tagGroupList = new ArrayList<TagGroup>();

        Set<String> tagGroupSet;
        try {
            tagGroupSet = jedisUtil.getSet(RedisKey.CUS_TGRP + cusId);
            for (String tagGroupId : tagGroupSet) {
                TagGroup tgrp = getTagGroupById(tagGroupId);
                //如果当前标签类别是启用的
                if (tgrp.getEnabled().equals("1")) {
                    //如果当前标签类别不是唯一的，则可添加至list
                    if (tgrp.getUnique().equals("0")) {
                        tagGroupList.add(tgrp);
                    } else {
                        //如果当前标签类别是唯一的，但是可标记资源类型为空或与当前资源类型相等，则可标记
                        if (tgrp.getResType() == null || tgrp.getResType().equals(resType)) {
                            tagGroupList.add(tgrp);
                        }
                    }
                }
            }
            TagGroupComparator tgc = new TagGroupComparator();
            Collections.sort(tagGroupList, tgc);
        } catch (Exception e) {
            log.error("获取可用的标签类别列表异常",e);
            throw new AppException("获取可用的标签类别列表异常");
        }
        return tagGroupList;
    }

    /**
     * 根据tagGroupId获取tagGroup实体对象
     * @param tagGroupId
     * @return TagGroup
     * @see com.eayun.virtualization.service.TagService#getTagGroupById(java.lang.String)
     */
    @Override
    public TagGroup getTagGroupById(String tagGroupId) {
        String tgrpStr;
        TagGroup tgrp = new TagGroup();
        try {
            tgrpStr = jedisUtil.get(RedisKey.TAG_GROUP  + tagGroupId);
            JSONObject tgrpJson = JSONObject.parseObject(tgrpStr);

            tgrp.setId(tagGroupId);
            tgrp.setName(tgrpJson.getString("name"));
            tgrp.setAbbreviation(tgrpJson.getString("abbreviation"));
            tgrp.setDescription(tgrpJson.getString("description"));
            tgrp.setCusId(tgrpJson.getString("cusId"));
            Date date = tgrpJson.getDate("createDate");
            tgrp.setCreateDate(date);
            String dateStr = "";
            if (date != null) {
                SimpleDateFormat sf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                dateStr = sf.format(date);
            }
            tgrp.setTgrpCreateDate(dateStr);

            String enabled = tgrpJson.getString("enabled");
            tgrp.setEnabled(enabled);
            tgrp.setTgrpEnabled(enabled.equals("1") ? "是" : "否");
            String unique = tgrpJson.getString("unique");
            tgrp.setUnique(unique);
            tgrp.setTgrpUnique(unique.equals("1") ? "是" : "否");

            String creatorId = tgrpJson.getString("creatorId");
            tgrp.setCreatorId(creatorId);
            tgrp.setTgrpCreator(getCreatorNameById(creatorId));

            String resTypeCanMark = tgrpJson.getString("resType");
            tgrp.setResType(resTypeCanMark);

            //标签类别下的标签数量
            Set<String> tagSet = jedisUtil.getSet(RedisKey.TGRP_TG + tagGroupId);
            tgrp.setTgrpTagNum(tagSet.size());
            if (tagSet.size() > 0) {
                //标签类别下标签标记的资源数量
                String[] tagIds = tagSet.toArray(new String[0]);
                String[] tgResIds = new String[tagIds.length];
                for (int i = 0; i < tagIds.length; i++) {
                    tgResIds[i] = RedisKey.TAG_RES + tagIds[i];
                }
                Set<String> resSet = jedisUtil.getUnionOfSet(tgResIds);
                tgrp.setTgrpResNum(resSet.size());
            }
        } catch (Exception e) {
            log.error("根据tagGroupId获取tagGroup实体对象异常",e);
            throw new AppException("根据tagGroupId获取tagGroup实体对象异常");
        }

        return tgrp;
    }

    /**
     * 添加标签类别
     * @param tagGroup
     * @return
     * @see com.eayun.virtualization.service.TagService#addTagGroup(com.eayun.virtualization.model.TagGroup)
     */
    @Override
    public TagGroup addTagGroup(TagGroup tagGroup) {
        log.info("添加标签类别");
        BaseTagGroup baseTagGroup = new BaseTagGroup();
        BeanUtils.copyPropertiesByModel(baseTagGroup, tagGroup);
        tagGroupDao.saveEntity(baseTagGroup);
        BeanUtils.copyPropertiesByModel(tagGroup, baseTagGroup);

        //将数据写入标签类别缓存和“客户-标签类别”缓存
        String tgJson = JSONObject.toJSONString(baseTagGroup);
        try {
            jedisUtil.set(RedisKey.TAG_GROUP  + baseTagGroup.getId(), tgJson);
            jedisUtil.addToSet(RedisKey.CUS_TGRP + baseTagGroup.getCusId(), baseTagGroup.getId());
        } catch (Exception e) {
            log.error("添加标签类别异常",e);
            throw new AppException("添加标签类别异常");
        }
        return tagGroup;
    }

    /**
     * 更新标签类别
     * @param tagGroup
     * @return
     * @see com.eayun.virtualization.service.TagService#updateTagGroup(com.eayun.virtualization.model.TagGroup)
     */
    @Override
    public TagGroup updateTagGroup(TagGroup tagGroup) {
        log.info("更新标签类别");
        BaseTagGroup baseTagGroup = new BaseTagGroup();
        BeanUtils.copyPropertiesByModel(baseTagGroup, tagGroup);
        tagGroupDao.saveOrUpdate(baseTagGroup);
        BeanUtils.copyPropertiesByModel(tagGroup, baseTagGroup);

        //更新缓存
        try {
            jedisUtil
                .set(RedisKey.TAG_GROUP  + baseTagGroup.getId(), JSONObject.toJSONString(baseTagGroup));
        } catch (Exception e) {
            log.error("更新标签类别异常",e);
            throw new AppException("更新标签类别异常");
        }
        return tagGroup;
    }

    /**
     * 删除标签类别
     * @param id
     * @param cusId
     * @return
     * @see com.eayun.virtualization.service.TagService#deleteTagGroup(java.lang.String, java.lang.String)
     */
    @Override
    public boolean deleteTagGroup(String id, String cusId) {
        //删数据库表中条目
        BaseTagGroup baseTagGroup = tagGroupDao.findOne(id);
        tagGroupDao.delete(baseTagGroup);

        try {
            return jedisUtil.delete(RedisKey.TAG_GROUP  + id)
                   && jedisUtil.removeFromSet(RedisKey.CUS_TGRP + cusId, id);
        } catch (Exception e) {
            log.error("删除标签类别异常",e);
            throw new AppException("删除标签类别异常");
        }
    }

    /*-------------------------标签类别相关 END----------------------------*/

    @Override
    public boolean checkTagGroupName(String cusId, String tagGroupName) {
        log.info("判断标签类别名称是否重复开始");
        Set<String> tagGroupSet;
        try {
            tagGroupSet = jedisUtil.getSet(RedisKey.CUS_TGRP  + cusId);
            for (String tagGroupId : tagGroupSet) {
                TagGroup tgrp = getTagGroupById(tagGroupId);
                if (tgrp.getName().equals(tagGroupName)) {
                    return false;
                }
            }
        } catch (Exception e) {
            log.error("判断标签类别名称重复异常",e);
            throw new AppException("判断标签类别名称重复异常");
        }
        return true;
    }

    @Override
    public boolean checkTagGroupAbbr(String cusId, String tagGroupAbbr, String tagGroupId) {
        log.info("判断标签类别简称是否重复开始");
        Set<String> tagGroupSet;
        try {
            tagGroupSet = jedisUtil.getSet(RedisKey.CUS_TGRP  + cusId);
            for (String tgrpId : tagGroupSet) {
                if (!tgrpId.equals(tagGroupId)) {
                    TagGroup tgrp = getTagGroupById(tgrpId);
                    if (tgrp.getAbbreviation().equals(tagGroupAbbr)) {
                        return false;
                    }
                }
            }
        } catch (Exception e) {
            log.error("判断标签类别简称重复异常",e);
            throw new AppException("判断标签类别简称重复异常");
        }
        return true;
    }

    @Override
    public boolean checkTagName(String tagGroupId, String tagName, String tagId) {
        log.info("判断标签类别下标签名称是否重复开始");
        Set<String> tagSet;
        try {
            tagSet = jedisUtil.getSet(RedisKey.TGRP_TG + tagGroupId);
            for (String tgId : tagSet) {
                if (!tgId.equals(tagId)) {
                    Tag tg = getTagById(tgId);
                    if (tg.getName().equals(tagName)) {
                        return false;
                    }
                }
            }
        } catch (Exception e) {
            log.error("判断标签名称重复异常",e);
            throw new AppException("判断标签名称重复异常");
        }
        return true;
    }

    //根据创建人ID得到创建人名称
    private String getCreatorNameById(String id) {
        String userName = "";

        User user = userService.findUserById(id);
        if (user != null) {
            userName = user.getUserPerson();
        } 
        return userName;
    }

    /*--------------------------标签相关 BEGIN-----------------------------*/
    /**
     * 通过标签类别ID获取标签类别下所有标签
     * @param tagGroupId
     * @return
     * @see com.eayun.virtualization.service.TagService#getTagList(java.lang.String)
     */
    @Override
    public List<Tag> getTagList(String tagGroupId) {
        List<Tag> tagList = new ArrayList<Tag>();
        Set<String> tagSet;
        try {
            tagSet = jedisUtil.getSet(RedisKey.TGRP_TG + tagGroupId);
            for (String tagId : tagSet) {
                Tag tg = getTagById(tagId);
                tagList.add(tg);
            }
            TagComparator tc = new TagComparator();
            Collections.sort(tagList, tc);
        } catch (Exception e) {
            log.error("获取标签类别下标签列表异常",e);
            throw new AppException("获取标签类别下标签列表异常");
        }
        return tagList;
    }

    /**
     * 获取可以标记当前资源的标签（即已标记该资源的标签过滤掉）
     * @param tagGroupId
     * @param resType
     * @param resId
     * @return
     * @see com.eayun.virtualization.service.TagService#getAvailableTagList(java.lang.String, java.lang.String, java.lang.String)
     */
    @Override
    public List<Tag> getAvailableTagList(String tagGroupId, String resType, String resId) {
        List<Tag> tagList = new ArrayList<Tag>();
        //查询当前标签类别下所有的标签ID
        Set<String> tagSet;
        try {
            tagSet = jedisUtil.getSet(RedisKey.TGRP_TG + tagGroupId);
            //查询当前类型的资源已标注的标签ID集合
            Set<String> resTagSet = jedisUtil.getSet(RedisKey.RES_TAG + resType + ":" + resId);
            for (String tagId : tagSet) {
                //如果当前类型的资源已标注的标签ID集合中不存在该标签，则加入可用的标签列表中。
                if (!resTagSet.contains(tagId)) {
                    Tag tg = getTagById(tagId);
                    tagList.add(tg);
                }
            }
            TagComparator tc = new TagComparator();
            Collections.sort(tagList, tc);
        } catch (Exception e) {
            log.error("获取可以标记当前资源的标签列表异常",e);
            throw new AppException("获取可以标记当前资源的标签列表异常");
        }
        return tagList;
    }

    /**
     * 根据tagId获取tag实体对象
     * @param tagId
     * @return
     * @see com.eayun.virtualization.service.TagService#getTagById(java.lang.String)
     */
    @Override
    public Tag getTagById(String tagId) {
        String tgStr;
        Tag tg = new Tag();
        try {
            tgStr = jedisUtil.get(RedisKey.TAG + tagId);
            JSONObject tgJson = JSONObject.parseObject(tgStr);

            tg.setId(tagId);
            tg.setName(tgJson.getString("name"));
            tg.setDescription(tgJson.getString("description"));
            tg.setGroupId(tgJson.getString("groupId"));

            Date date = tgJson.getDate("createDate");
            tg.setCreateDate(date);
            String dateStr = "";
            if (date != null) {
                SimpleDateFormat sf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                dateStr = sf.format(date);
            }
            tg.setTgCreateDate(dateStr);

            String creatorId = tgJson.getString("creatorId");
            tg.setCreatorId(creatorId);
            tg.setTgCreator(getCreatorNameById(creatorId));

            //标签下的资源数量
            long resNum = jedisUtil.getSizeOfSet(RedisKey.TAG_RES + tagId);
            tg.setTgResNum(resNum);
        } catch (Exception e) {
            log.error("根据tagId获取tag实体对象异常",e);
            throw new AppException("根据tagId获取tag实体对象异常");
        }
        return tg;
    }

    /**
     * 添加标签
     * @param tag
     * @return
     * @see com.eayun.virtualization.service.TagService#addTag(com.eayun.virtualization.model.Tag)
     */
    @Override
    public Tag addTag(Tag tag) {
        log.info("添加标签");
        BaseTag baseTag = new BaseTag();
        BeanUtils.copyPropertiesByModel(baseTag, tag);
        tagDao.saveEntity(baseTag);
        BeanUtils.copyPropertiesByModel(tag, baseTag);

        //将数据写入标签缓存和“标签类别-标签”缓存
        String tgJson = JSON.toJSONString(baseTag);
        try {
            jedisUtil.set(RedisKey.TAG  + baseTag.getId(), tgJson);
            jedisUtil.addToSet(RedisKey.TGRP_TG + baseTag.getGroupId(), baseTag.getId());
        } catch (Exception e) {
            log.error("添加标签异常",e);
            throw new AppException("添加标签异常");
        }
        return tag;
    }

    /**
     * 更新标签
     * @param tag
     * @return
     * @see com.eayun.virtualization.service.TagService#updateTag(com.eayun.virtualization.model.Tag)
     */
    @Override
    public Tag updateTag(Tag tag) {
        log.info("更新标签");
        BaseTag baseTag = new BaseTag();
        BeanUtils.copyPropertiesByModel(baseTag, tag);
        tagDao.saveOrUpdate(baseTag);
        BeanUtils.copyPropertiesByModel(tag, baseTag);

        //更新缓存
        try {
            jedisUtil.set(RedisKey.TAG  + baseTag.getId(), JSONObject.toJSONString(baseTag));
        } catch (Exception e) {
            log.error("更新标签异常",e);
            throw new AppException("更新标签异常");
        }
        return tag;
    }

    /**
     * 删除标签
     * @param tagId
     * @param tagGroupId
     * @return
     * @see com.eayun.virtualization.service.TagService#deleteTag(java.lang.String, java.lang.String)
     */
    @Override
    public boolean deleteTag(String tagId, String tagGroupId) {
        //删除数据库条目；删除标签类别-标签缓存。因为标签标记资源之后就不可以删除，能进到这个方法一定是可以删除的
        BaseTag baseTag = tagDao.findOne(tagId);
        tagDao.delete(baseTag);
        boolean isSuccess = false;
        //移除缓存
        try {
            isSuccess = jedisUtil.delete(RedisKey.TAG  + tagId)
                        && jedisUtil.removeFromSet(RedisKey.TGRP_TG + tagGroupId, tagId);
        } catch (Exception e) {
            log.error("删除标签异常",e);
            throw new AppException("删除标签异常");
        }
        return isSuccess;
    }

    /**
     * 获取标签标记资源
     * @param tagId
     * @return
     * @see com.eayun.virtualization.service.TagService#getTagResourceById(java.lang.String)
     */
    @Override
    public List<TagResource> getTagResourceList(String tagId) {
        //在标签-资源缓存中查询该标签对应标记的资源 值是[res_type]:[res_id]
        Set<String> tgResSet;
        Map<String, List<String>> map = new HashMap<String, List<String>>();
        List<TagResource> tagResList = null;
        try {
            tgResSet = jedisUtil.getSet(RedisKey.TAG_RES + tagId);
            for (String tgResStr : tgResSet) {
                String resId = tgResStr.substring(tgResStr.indexOf(":") + 1);
                String resType = tgResStr.substring(0, tgResStr.indexOf(":"));

                List<String> list = map.get(resType);
                if (list == null) {
                    list = new ArrayList<String>();
                }
                list.add(resId);
                map.put(resType, list);
            }
            tagResList = getTagResourceList(map, tagId);
        } catch (Exception e) {
            log.error("获取标签已标注资源列表异常",e);
            throw new AppException("获取标签已标注资源列表异常");
        }
        return tagResList;
    }

    private List<TagResource> getTagResourceList(Map<String, List<String>> map, String tagId)
                                                                                             throws Exception {
        //遍历map，首先得到当前资源的类型，然后使用getTypeAndTableName(String resType)取得资源类别名称和表明
        //对于一类资源类别，在map中取其list进行遍历，构造SQL的in('',...,'')，遍历完所有的资源类别，构造union语句，进行查询
        //union得到的结果是List list中存放的是数组Object[]
        List<TagResource> tgResList = new ArrayList<TagResource>();
        if (map.size() == 0) {
            return tgResList;
        }
        String tgStr = null;
        try {
            tgStr = jedisUtil.get(RedisKey.TAG  + tagId);
            JSONObject tgJson = JSONObject.parseObject(tgStr);
            String tagName = tgJson.getString("name");

            StringBuffer sb = new StringBuffer();
            sb.append("SELECT res.res_id, res.res_name, prj.prj_name, res.res_typename, res.res_type, res.res_prjid FROM ( \n");
            for (Map.Entry<String, List<String>> entry : map.entrySet()) {
                String resType = entry.getKey();
                String sql = createSQL(resType, entry.getValue());
                sb.append(sql + " UNION ");
            }
            String subStr = sb.substring(0, sb.length() - 6);
            subStr = subStr
                     + " ) AS res LEFT JOIN cloud_project AS prj ON res.res_prjid = prj.prj_id;";
            List<Object[]> result = tagResourceDao.createSQLNativeQuery(subStr).getResultList();

            for (Object[] obj : result) {
                TagResource tgres = new TagResource();
                String resId = obj[0] == null ? "" : obj[0].toString();
                String resName = obj[1] == null ? "" : obj[1].toString();
                String prjName = obj[2] == null ? "" : obj[2].toString();
                String resTypeName = obj[3] == null ? "" : obj[3].toString();
                String resType = obj[4] == null ? "" : obj[4].toString();
                String prjID = obj[5] == null ? "" : obj[5].toString();

                tgres.setResourceId(resId);
                tgres.setResourceName(resName);
                tgres.setProjectName(prjName);
                tgres.setResourceTypeName(resTypeName);
                tgres.setResourceType(resType);
                tgres.setProjectId(prjID);

                tgres.setTagId(tagId);
                tgres.setTagName(tagName);
                tgResList.add(tgres);
            }
        } catch (Exception e) {
            throw e;
        }
        return tgResList;
    }

    private String createSQL(String resType, List<String> resIdList) {
        StringBuffer sb = new StringBuffer();
        String typeName = getTypeName(resType);
        if (resType.equals("vm")) {
            sb.append("select vm_id as res_id, vm_name as res_name, prj_id as res_prjid, '")
                .append(typeName).append("' as res_typename, '").append(resType)
                .append("' as res_type from cloud_vm where vm_id in (");
        } else if (resType.equals("volume")) {
            sb.append("select vol_id as res_id, vol_name as res_name, prj_id as res_prjid ,'")
                .append(typeName).append("' as res_typename, '").append(resType)
                .append("' as res_type from cloud_volume where vol_id in (");
        } else if (resType.equals("diskSnapshot")) {
            sb.append("select snap_id as res_id, snap_name as res_name, prj_id as res_prjid,'")
                .append(typeName).append("' as res_typename, '").append(resType)
                .append("' as res_type from cloud_disksnapshot where snap_id in (");
        } else if (resType.equals("privateImage")) {
            sb.append("select image_id as res_id, image_name as res_name, prj_id as res_prjid,'")
                .append(typeName).append("' as res_typename, '").append(resType)
                .append("' as res_type from cloud_image where image_id in (");
        } else if (resType.equals("securityGroup")) {
            sb.append("select sg_id as res_id, sg_name as res_name, prj_id as res_prjid,'")
                .append(typeName).append("' as res_typename, '").append(resType)
                .append("' as res_type from cloud_securitygroup where sg_id in (");
        } else if (resType.equals("network")) {
            sb.append("select net_id as res_id, net_name as res_name, prj_id as res_prjid,'")
                .append(typeName).append("' as res_typename, '").append(resType)
                .append("' as res_type from cloud_network where net_id in (");
        } else if (resType.equals("subNetwork")) {
            sb.append("select subnet_id as res_id, subnet_name as res_name, prj_id as res_prjid,'")
                .append(typeName).append("' as res_typename, '").append(resType)
                .append("' as res_type from cloud_subnetwork where subnet_id in (");
        } else if (resType.equals("route")) {
            sb.append("select route_id as res_id, route_name as res_name, prj_id as res_prjid,'")
                .append(typeName).append("' as res_typename, '").append(resType)
                .append("' as res_type from cloud_route where route_id in (");
        } else if (resType.equals("firewall")) {
            sb.append("select fw_id as res_id, fw_name as res_name, prj_id as res_prjid,'")
                .append(typeName).append("' as res_typename, '").append(resType)
                .append("' as res_type from cloud_firewall where fw_id in (");
        } else if (resType.equals("firewallPolicy")) {
            sb.append("select fwp_id as res_id, fwp_name as res_name, prj_id as res_prjid,'")
                .append(typeName).append("' as res_typename, '").append(resType)
                .append("' as res_type from cloud_fwpolicy where fwp_id in (");
        } else if (resType.equals("firewallRule")) {
            sb.append("select fwr_id as res_id, fwr_name as res_name, prj_id as res_prjid,'")
                .append(typeName).append("' as res_typename, '").append(resType)
                .append("' as res_type from cloud_fwrule where fwr_id in (");
        } else if (resType.equals("ldPool")) {
            sb.append("select pool_id as res_id, pool_name as res_name, prj_id as res_prjid,'")
                .append(typeName).append("' as res_typename, '").append(resType)
                .append("' as res_type from cloud_ldpool where pool_id in (");
        } else if (resType.equals("ldMember")) {
            sb.append(
                "select member_id as res_id, member_address as res_name, prj_id as res_prjid,'")
                .append(typeName).append("' as res_typename, '").append(resType)
                .append("' as res_type from cloud_ldmember where member_id in (");
        } else if (resType.equals("ldVIP")) {
            sb.append("select vip_id as res_id, vip_name as res_name, prj_id as res_prjid,'")
                .append(typeName).append("' as res_typename, '").append(resType)
                .append("' as res_type from cloud_ldvip where vip_id in (");
        } else if (resType.equals("ldMonitor")) {
            sb.append("select ldm_id as res_id, ldm_id as res_name, prj_id as res_prjid,'")
                .append(typeName).append("' as res_typename, '").append(resType)
                .append("' as res_type from cloud_ldmonitor where ldm_id in (");
            // ldListener表目前没有补充完善，没办法做查询
            //        } else if (resType.equals("ldListener")) {
            //            sb.append("select ldm_id, prj_id from cloud_ldlistener where ldm_id in (");
        } else if (resType.equals("floatIP")) {
            sb.append("select flo_id as res_id, flo_ip as res_name, prj_id as res_prjid,'")
                .append(typeName).append("' as res_typename, '").append(resType)
                .append("' as res_type from cloud_floatip where flo_id in (");
        }

        for (String id : resIdList) {
            sb.append("'" + id + "',");
        }
        String sql = sb.substring(0, sb.length() - 1) + ")";
        return sql;
    }

    /**
     * 通过资源类型得到资源类型名称，用于在界面展现
     * @param resType
     * @return
     */
    private String getTypeName(String resType) {
        if (resType.equals("vm")) {
            return "云主机";
        } else if (resType.equals("volume")) {
            return "云硬盘";
        } else if (resType.equals("diskSnapshot")) {
            return "云硬盘备份";
        } else if (resType.equals("privateImage")) {
            return "自定义镜像";
        } else if (resType.equals("securityGroup")) {
            return "安全组";
        } else if (resType.equals("network")) {
            return "私有网络";
        } else if (resType.equals("subNetwork")) {
            return "子网";
        } else if (resType.equals("route")) {
            return "路由";
        } else if (resType.equals("firewall")) {
            return "防火墙";
        } else if (resType.equals("firewallPolicy")) {
            return "防火墙策略";
        } else if (resType.equals("firewallRule")) {
            return "防火墙规则";
        } else if (resType.equals("ldPool")) {
            return "负载均衡器";
//        } else if (resType.equals("ldMember")) {
//            return "负载均衡成员";
//        } else if (resType.equals("ldVIP")) {
//            return "负载均衡VIP";
        } else if (resType.equals("ldMonitor")) {
            return "健康检查";
            //        } else if (resType.equals("ldListener")) {
            //            return "负载均衡监听";
        } else if (resType.equals("floatIP")) {
            return "公网IP";
        } else {
            return "";
        }
    }

    /**
     * 为资源打标签
     * @param resType
     * @param resId
     * @param tagGroupId
     * @param tagId
     * @return
     * @see com.eayun.virtualization.service.TagService#tagResource(java.lang.String, java.lang.String, java.lang.String, java.lang.String)
     */
    @Override
    public TagResource tagResource(String resType, String resId, String tagGroupId, String tagId) {
        log.info("用标签标记资源");
        if(tagId.equals("{}")){
            return tagResourceWithTagGroup(resType, resId, tagGroupId);
        }else{
            return tagResourceWithTag(resType, resId, tagGroupId, tagId);
        }
        
    }

    private TagResource tagResourceWithTag(String resType, String resId, String tagGroupId,
                                           String tagId) {
        BaseTagResource baseTagResource = new BaseTagResource();
        baseTagResource.setTagId(tagId);
        baseTagResource.setResourceId(resId);
        baseTagResource.setResourceType(resType);

        String projectId = getProjectIdByResTypeAndId(resType, resId);
        baseTagResource.setProjectId(projectId);
        
        tagResourceDao.saveEntity(baseTagResource);

        TagResource tgRes = new TagResource();
        BeanUtils.copyPropertiesByModel(tgRes, baseTagResource);

        //将数据写入"标签-资源"缓存和“资源-标签”缓存
        try {
            jedisUtil.addToSet(RedisKey.TAG_RES + tagId, resType + ":" + resId);
            jedisUtil.addToSet(RedisKey.RES_TAG + resType + ":" + resId, tagId);
            //如果标记资源结束后，查一下该标签类别的可标注类型是否为空，如果是，则标注为该资源类型
            BaseTagGroup baseTagGroup = tagGroupDao.findOne(tagGroupId);
            if (baseTagGroup.getResType() == null || baseTagGroup.getResType().equals("")) {
                baseTagGroup.setResType(resType);
                tagGroupDao.saveOrUpdate(baseTagGroup);
                //更新缓存
                jedisUtil.set(RedisKey.TAG_GROUP  + tagGroupId, JSONObject.toJSONString(baseTagGroup));
            }
        } catch (Exception e) {
            log.error("标记资源异常",e);
            throw new AppException("标记资源异常");
        }

        return tgRes;
    }

    private TagResource tagResourceWithTagGroup(String resType, String resId, String tagGroupId){
        //获取标签类别下所有的标签，循环去标记
        TagResource tagResource = null;
        try {
            Set<String> tagIdSet = jedisUtil.getSet(RedisKey.TGRP_TG+tagGroupId);
            deleteOddDataBeforeTagging(resType, resId, tagGroupId);//先清掉该标签组下的所有标签标记了该资源的记录，防止出现重复记录
            
            for (String tagId : tagIdSet) {
                tagResource = tagResourceWithTag(resType,resId, tagGroupId, tagId);
            }
        } catch (Exception e) {
            log.error("标记资源异常",e);
            throw new AppException("标记资源异常");
        }
        
        return tagResource;
    }

    private void deleteOddDataBeforeTagging(String resType, String resId, String tagGroupId) {
        StringBuffer sb = new StringBuffer();
        sb.append("delete from sys_tagresource where tgres_tagid in (")
        .append("select tg_id from sys_tag where tg_groupid=?) and tgres_resourcetype =? and tgres_resourceid=?");
        tagDao.execSQL(sb.toString(),tagGroupId, resType, resId);
    }

    /**
     * 通过资源类型和资源ID得到项目ID
     * @param resType
     * @param resId
     * @return
     */
    @SuppressWarnings("rawtypes")
    private String getProjectIdByResTypeAndId(String resType, String resId) {
        String tableName = getTableName(resType);
        String id = getColOfID(resType);
        String sql = "select prj_id from " + tableName + " where " + id + "='" + resId + "'";
        Query query = tagResourceDao.createSQLNativeQuery(sql);
        List resultList = query.getResultList();
        if (resultList.size() == 0) {
            return null;
        }
        String prjId = resultList.get(0).toString();
        return prjId;
    }

    /**
     * 获取不同资源类型的ID字段名称
     * @param resType
     * @return
     */
    private String getColOfID(String resType) {
        if (resType.equals("vm")) {
            return "vm_id";
        } else if (resType.equals("volume")) {
            return "vol_id";
        } else if (resType.equals("diskSnapshot")) {
            return "snap_id";
        } else if (resType.equals("privateImage")) {
            return "image_id";
        } else if (resType.equals("securityGroup")) {
            return "sg_id";
        } else if (resType.equals("network")) {
            return "net_id";
        } else if (resType.equals("subNetwork")) {
            return "subnet_id";
        } else if (resType.equals("route")) {
            return "route_id";
        } else if (resType.equals("firewall")) {
            return "fw_id";
        } else if (resType.equals("firewallPolicy")) {
            return "fwp_id";
        } else if (resType.equals("firewallRule")) {
            return "fwr_id";
        } else if (resType.equals("ldPool")) {
            return "pool_id";
        } else if (resType.equals("ldMember")) {
            return "member_id";
        } else if (resType.equals("ldVIP")) {
            return "vip_id";
        } else if (resType.equals("ldMonitor")) {
            return "ldm_id";
        } else if (resType.equals("floatIP")) {
            return "flo_id";
        } else {
            return "";
        }
    }

    /**
     * 获取不同资源类型的表名
     * @param resType
     * @return
     */
    private String getTableName(String resType) {
        //得到了资源类型和资源的表名，查询出我们所要的字段。项目表是cloud_project，其他资源表均有prj_id与之关联
        if (resType.equals("vm")) {
            return "cloud_vm";
        } else if (resType.equals("volume")) {
            return "cloud_volume";
        } else if (resType.equals("diskSnapshot")) {
            return "cloud_disksnapshot";
        } else if (resType.equals("privateImage")) {
            return "cloud_image";
        } else if (resType.equals("securityGroup")) {
            return "cloud_securitygroup";
        } else if (resType.equals("network")) {
            return "cloud_network";
        } else if (resType.equals("subNetwork")) {
            return "cloud_subnetwork";
        } else if (resType.equals("route")) {
            return "cloud_route";
        } else if (resType.equals("firewall")) {
            return "cloud_firewall";
        } else if (resType.equals("firewallPolicy")) {
            return "cloud_fwpolicy";
        } else if (resType.equals("firewallRule")) {
            return "cloud_fwrule";
        } else if (resType.equals("ldPool")) {
            return "cloud_ldpool";
        } else if (resType.equals("ldMember")) {
            return "cloud_ldmember";
        } else if (resType.equals("ldVIP")) {
            return "cloud_ldvip";
        } else if (resType.equals("ldMonitor")) {
            return "cloud_ldmonitor";
        } else if (resType.equals("ldListener")) {
            return "cloud_ldlistener";
        } else if (resType.equals("floatIP")) {
            return "cloud_floatip";
        } else {
            return "";
        }
    }

    @Override
    public boolean untagResource(String tagId, String resType, String resId) {
        boolean isSuccess = false;
        String sql = "delete from sys_tagresource where tgres_resourceid='" + resId
                     + "' and tgres_resourcetype='" + resType + "' and tgres_tagid = '" + tagId
                     + "'";
        tagResourceDao.execSQL(sql);

        try {
            isSuccess = jedisUtil.removeFromSet(RedisKey.TAG_RES + tagId, resType + ":" + resId)
                        && jedisUtil.removeFromSet(RedisKey.RES_TAG + resType + ":" + resId, tagId);
        } catch (Exception e) {
            log.error("取消标记异常",e);
            throw new AppException("取消标记异常");
        }
        return isSuccess;
    }

    /**
     * 获取当前资源已标记的标签类别名称、标签名称
     * @param resType
     * @param resId
     * @return
     * @see com.eayun.virtualization.service.TagService#getResourceTag(java.lang.String, java.lang.String)
     */
    @Override
    public List<ResourceTag> getResourceTag(String resType, String resId) {
        List<ResourceTag> list = new ArrayList<ResourceTag>();
        try {
            Set<String> tags = jedisUtil.getSet(RedisKey.RES_TAG + resType + ":" + resId);
            for (String tagId : tags) {
                ResourceTag resTag = new ResourceTag();
                String tagJSONStr = jedisUtil.get(RedisKey.TAG  + tagId);
                resTag.setTagId(tagId);

                if (tagJSONStr != null) {
                    JSONObject tagJSON = JSONObject.parseObject(tagJSONStr);
                    String tagName = tagJSON.getString("name");
                    resTag.setTagName(tagName);

                    String tgrpId = tagJSON.getString("groupId");
                    resTag.setTagGroupId(tgrpId);

                    String tagGroupJSONStr = jedisUtil.get(RedisKey.TAG_GROUP  + tgrpId);
                    if (tagGroupJSONStr != null) {
                        JSONObject tagGroupJSON = JSONObject.parseObject(tagGroupJSONStr);
                        String tagGroupName = tagGroupJSON.getString("abbreviation");
                        resTag.setTagGroupName(tagGroupName);

                        list.add(resTag);
                    }
                }
            }
        } catch (Exception e) {
            log.error("获取当前资源已标记的标签类别名称、标签名称异常",e);
            throw new AppException("获取当前资源已标记的标签类别名称、标签名称异常");
        }
        return list;
    }

    /**
     * 根据所属项目名称获取当前已标记资源列表
     * @param prjName
     * @return
     * @see com.eayun.virtualization.service.TagService#getTagResourceListByPrjName(java.lang.String)
     */
    @Override
    public List<TagResource> getTagResourceList(String tagId, String prjName) {
        //在标签-资源缓存中查询该标签对应标记的资源 值是[res_type]:[res_id]
        Set<String> tgResSet;
        List<TagResource> tagResList = null;
        try {
            tgResSet = jedisUtil.getSet(RedisKey.TAG_RES + tagId);
            //使用这个map的目的是先将资源按照资源类型分组：resType<->List<resId>，用于后面拼接SQL
            Map<String, List<String>> map = new HashMap<String, List<String>>();
            for (String tgResStr : tgResSet) {
                String resId = tgResStr.substring(tgResStr.indexOf(":") + 1);
                String resType = tgResStr.substring(0, tgResStr.indexOf(":"));

                List<String> list = map.get(resType);
                if (list == null) {
                    list = new ArrayList<String>();
                }
                list.add(resId);
                map.put(resType, list);
            }
            tagResList = getTagResourceListByPrjName(map, tagId, prjName);
        } catch (Exception e) {
            log.error("通过项目名称查询标签对应标记的资源异常",e);
            throw new AppException("通过项目名称查询标签对应标记的资源异常");
        }
        return tagResList;
    }

    private List<TagResource> getTagResourceListByPrjName(Map<String, List<String>> map,
                                                          String tagId, String prjName)
                                                                                       throws Exception {
        List<TagResource> tgResList = new ArrayList<TagResource>();
        if (map.size() == 0) {
            return tgResList;
        }
        String tgStr = jedisUtil.get(RedisKey.TAG  + tagId);
        JSONObject tgJson = JSONObject.parseObject(tgStr);
        String tagName = tgJson.getString("name");

        StringBuffer sb = new StringBuffer();
        sb.append("SELECT res.res_id, res.res_name, dc.dc_name, res.res_typename, res.res_type, res.res_prjid FROM ( \n");
        for (Map.Entry<String, List<String>> entry : map.entrySet()) {
            String resType = entry.getKey();
            String sql = createSQL(resType, entry.getValue());
            sb.append(sql + " UNION ");
        }
        String subStr = sb.substring(0, sb.length() - 6);
        subStr = subStr + " ) AS res LEFT JOIN cloud_project AS prj ON res.res_prjid = prj.prj_id"
        		+ " LEFT JOIN dc_datacenter dc on prj.dc_id = dc.id ;";
        List<Object[]> result = tagResourceDao.createSQLNativeQuery(subStr).getResultList();

        for (Object[] obj : result) {
            String resId = obj[0] == null ? "" : obj[0].toString();
            String resName = obj[1] == null ? "" : obj[1].toString();
            String prjNameStr = obj[2] == null ? "" : obj[2].toString();
            String resTypeName = obj[3] == null ? "" : obj[3].toString();
            String resType = obj[4] == null ? "" : obj[4].toString();
            String prjID = obj[5] == null ? "" : obj[5].toString();
            if (prjName.equals("") || prjNameStr.equals(prjName)) {
                TagResource tgres = new TagResource();
                tgres.setResourceId(resId);
                tgres.setResourceName(resName);
                tgres.setProjectName(prjNameStr);
                tgres.setResourceTypeName(resTypeName);
                tgres.setResourceType(resType);
                tgres.setProjectId(prjID);

                tgres.setTagId(tagId);
                tgres.setTagName(tagName);
                tgResList.add(tgres);
            }
        }
        return tgResList;
    }

    @Override
    public boolean clearTagResByTagGroupId(String tagGroupId) {
        //1.根据tagGroupId查询类别中所有的标签ID
        //2.根据标签ID，在缓存"tg:res:[tagId]"查到所有的资源[resType]:[resId]
        //3.批量调用untagResource(tagId, resType, resId)方法取消标记。
        Set<String> tagSet;
        boolean isUntagAll = true;
        try {
            tagSet = jedisUtil.getSet(RedisKey.TGRP_TG + tagGroupId);
            for (String tagId : tagSet) {
                Set<String> tgResSet = jedisUtil.getSet(RedisKey.TAG_RES + tagId);
                for (String tgRes : tgResSet) {
                    String resId = tgRes.substring(tgRes.indexOf(":") + 1);
                    String resType = tgRes.substring(0, tgRes.indexOf(":"));
                    isUntagAll &= untagResource(tagId, resType, resId);
                }
            }
        } catch (Exception e) {
            log.error("对标签类别下所有的标签标注的资源，取消标记",e);
            throw new AppException("对标签类别下所有的标签标注的资源，取消标记");
        }
        return isUntagAll;
    }

    /**
     * 删除资源后需要刷新缓存
     * @param resType
     * @param resId
     * @return
     * @see com.eayun.virtualization.service.TagService#refreshCacheAftDelRes(java.lang.String, java.lang.String)
     */
    @Override
    public boolean refreshCacheAftDelRes(String resType, String resId) {
        Set<String> tagIdSet;
        boolean isRemoved = true;
        try {
            tagIdSet = jedisUtil.getSet(RedisKey.RES_TAG + resType + ":" + resId);
            isRemoved &= jedisUtil.delete(RedisKey.RES_TAG + resType + ":" + resId);
            for (String tagId : tagIdSet) {
                isRemoved &= jedisUtil.removeFromSet(RedisKey.TAG_RES + tagId, resType + ":" + resId);
            }
            isRemoved &= removeFromDB(resId);
        } catch (Exception e) {
            log.error("刷新缓存异常",e);
            throw new AppException("刷新缓存异常");
        }
        return isRemoved;
    }

    private boolean removeFromDB(String resId) {
        StringBuffer sb = new StringBuffer();
        sb.append("delete from sys_tagresource where tgres_resourceid=?");
        tagResourceDao.execSQL(sb.toString(), resId);
        return true;
    }

    @Override
    public String getResourceTagForShowcase(String resType, String resId) {
        StringBuffer allTags = new StringBuffer();
        StringBuffer twoTags = new StringBuffer();;
        boolean isMoreThan2 = false;
        JSONObject json = new JSONObject();
        try {
            List<String> tagNameList = getResourceTagNameList(resType,resId);
            if(tagNameList.size()>2){
                isMoreThan2 = true;
                twoTags.append(tagNameList.get(0)).append("&nbsp;&nbsp;").append(tagNameList.get(1));
                for (int i=0; i<tagNameList.size(); i++) {
                    allTags.append(tagNameList.get(i)).append("&nbsp;&nbsp;");
                }
                json.put("twoTagsString", twoTags.toString());
            }else{
                isMoreThan2 = false;
                for (int i=0; i<tagNameList.size(); i++) {
                    allTags.append(tagNameList.get(i)).append("&nbsp;&nbsp;");
                }
                json.put("twoTagsString", allTags.toString());
            }
            json.put("gt2Tags", isMoreThan2);
            json.put("allTagsString", allTags.toString());
        } catch (Exception e) {
            log.error("查询资源标签异常",e);
            throw new AppException("查询资源标签异常");
        }
        return json.toJSONString();
    }

    private List<String> getResourceTagNameList(String resType, String resId) throws Exception {
        List<String> tagNameList = new ArrayList<String>();
        Set<String> tags = jedisUtil.getSet(RedisKey.RES_TAG + resType + ":" + resId);
        for (String tagId : tags) {
            String tagJSONStr = jedisUtil.get(RedisKey.TAG  + tagId);

            if (tagJSONStr != null) {
                JSONObject tagJSON = JSONObject.parseObject(tagJSONStr);
                String tagName = tagJSON.getString("name");
                tagNameList.add(tagName);
            }
        }
        return tagNameList;
    }

    /**
     * 分页的方式查询标签列表
     * @param page
     * @param tagGroupId
     * @param queryMap
     * @return
     * @throws Exception 
     * @see com.eayun.virtualization.service.TagService#getPagedTagList(com.eayun.common.dao.support.Page, java.lang.String, com.eayun.common.dao.QueryMap)
     */
    @Override
    public Page getPagedTagList(String tagGroupId, int pageSize, int currentPageNumber) {
        int startIndex = Page.getStartOfPage(currentPageNumber, pageSize);
        long totalCount = 0;
        List<Tag> allTagsList = getTagList(tagGroupId);
        List<Tag> pagedTagList = null;
        try {
            totalCount = jedisUtil.getSizeOfSet(RedisKey.TGRP_TG + tagGroupId);
            int toIndex = (startIndex + pageSize) > allTagsList.size() ? allTagsList.size()
                : startIndex + pageSize;
            pagedTagList = allTagsList.subList(startIndex, toIndex);
        } catch (Exception e) {
            log.error("查询标签类别-标签缓存异常",e);
            throw new AppException("查询标签类别-标签缓存异常");
        }
        return new Page(startIndex, totalCount, pageSize, pagedTagList);
    }

    @Override
    public Page getPagedTagResourceList(String tagId, String prjName, int pageSize,
                                        int currentPageNumber) {
        int startIndex = Page.getStartOfPage(currentPageNumber, pageSize);
        long totalCount = 0;
        List<TagResource> allTgResList = getTagResourceList(tagId, prjName);
        int listSize = allTgResList.size();
        List<TagResource> pagedTgResList = null;
        try {
            totalCount = jedisUtil.getSizeOfSet(RedisKey.TAG_RES + tagId);
            totalCount = totalCount > listSize ? listSize : totalCount;
            int toIndex = (startIndex + pageSize) > listSize ? listSize : startIndex + pageSize;
            pagedTgResList = allTgResList.subList(startIndex, toIndex);
        } catch (Exception e) {
            log.error("查询标签-资源缓存异常",e);
            throw new AppException("查询标签-资源缓存异常");
        }
        return new Page(startIndex, totalCount, pageSize, pagedTgResList);
    }

    /**
     * 同步Redis和DB
     * @return
     * @see com.eayun.virtualization.service.TagService#synchronizeRedisWithDB()
     */
    @Override
    public String synchronizeRedisWithDB() {
        String msg = "";
        try {
            clearDBDataAboutTagResource();
            clearCacheAboutTagBeforeSync();
            
            syncTagGroup();
            syncTag();
            syncTagResource();
            
            msg=ConstantClazz.SUCCESS_CODE;
        } catch (Exception e) {
            msg=e.getMessage();
        }
        return msg;
    }

    private void clearCacheAboutTagBeforeSync() throws Exception{
        //通过KEYS pattern找到所有的相关key，并del掉
        String[] keys = {RedisKey.CUS_TGRP+"*",RedisKey.TAG_GROUP+"*",
                         RedisKey.TGRP_TG+"*",RedisKey.TAG+"*",
                         RedisKey.TAG_RES+"*",RedisKey.RES_TAG+"*"};
        for (String key : keys) {
            Set<String> idSet = jedisUtil.keys(key);
            for(String id : idSet){
                jedisUtil.delete(id);
            }
            idSet.clear();
        }
    }

    @SuppressWarnings("rawtypes")
    private void clearDBDataAboutTagResource() {
        String[] resTypeArray = {"vm","volume","diskSnapshot","privateImage",
                                 "securityGroup","network","subNetwork","route",
                                 "firewall","firewallPolicy","firewallRule","ldPool",
                                 "ldMember","ldVIP","ldMonitor","floatIP"};
        StringBuffer sb = new StringBuffer();
        for(int i=0; i<resTypeArray.length; i++){
            String resType = resTypeArray[i];
            String idColumn = getColOfID(resType);
            String tableName = getTableName(resType);
            sb.append("select tgres_id from sys_tagresource where tgres_resourcetype='"+resType+"' and tgres_resourceid not in (")
            .append("select "+idColumn+" from "+tableName+") ")
            .append("Union ");
        }
        String sql = sb.substring(0, sb.length() - 6);
        Query query = tagResourceDao.createSQLNativeQuery(sql);
        List list = query.getResultList();
        for(int i=0;i<list.size();i++){
            String id = list.get(i).toString();
            tagResourceDao.delete(id);
        }
    }

    private void syncTagResource() throws Exception {
        Iterator<BaseTagResource> iterator = tagResourceDao.findAll().iterator();
        while(iterator.hasNext()){
            BaseTagResource baseTagRes = iterator.next();
            String tagId = baseTagRes.getTagId();
            String resId = baseTagRes.getResourceId();
            String resType = baseTagRes.getResourceType();
            
            jedisUtil.addToSet(RedisKey.TAG_RES+tagId, resType+":"+resId);
            jedisUtil.addToSet(RedisKey.RES_TAG+resType+":"+resId, tagId);
        }
    }

    private void syncTag() throws Exception {
        Iterator<BaseTag> iterator = tagDao.findAll().iterator();
        while(iterator.hasNext()){
            BaseTag baseTag = iterator.next();
            String id = baseTag.getId();
            String tgrpId = baseTag.getGroupId();
            String tagJSON = JSONObject.toJSONString(baseTag);
            
            jedisUtil.addToSet(RedisKey.TGRP_TG+tgrpId, id);
            jedisUtil.set(RedisKey.TAG +id, tagJSON);
        }
    }

    private void syncTagGroup() throws Exception {
        Iterator<BaseTagGroup> iterator = tagGroupDao.findAll().iterator();
        List<String> tgrpIdList = new ArrayList<String>();
        while(iterator.hasNext()){
            BaseTagGroup baseTagGroup = iterator.next();
            String tagGrpJSON = JSONObject.toJSONString(baseTagGroup);
            String cusId = baseTagGroup.getCusId();
            String id = baseTagGroup.getId();
            tgrpIdList.add(id);
            
            jedisUtil.addToSet(RedisKey.CUS_TGRP + cusId, id);
            jedisUtil.set(RedisKey.TAG_GROUP +id, tagGrpJSON);
        }
    }

    @Override
    public JSONObject getParamsByTagId(String tagId) {
        JSONObject paramJson = new JSONObject();
        try {
            String tagJSONStr = jedisUtil.get(RedisKey.TAG  + tagId);
            String tagName = "";
            if (tagJSONStr != null) {
                JSONObject tagJSON = JSONObject.parseObject(tagJSONStr);
                tagName = tagJSON.getString("name");
            }
            List<String> prjNamesList = getProjectNamesList(tagId);
            
            paramJson.put("tagName", tagName);
            paramJson.put("prjNames", prjNamesList);
        } catch (Exception e) {
            log.error("",e);
            throw new AppException("通过TagId为查看资源页面准备参数异常");
        }
        return paramJson;
    }

    private List<String> getProjectNamesList(String tagId) throws Exception {
        Set<String> tgResSet;
        Map<String, List<String>> map = new HashMap<String, List<String>>();
        List<String> prjNamesList = null;
        try {
            tgResSet = jedisUtil.getSet(RedisKey.TAG_RES + tagId);
            for (String tgResStr : tgResSet) {
                String resId = tgResStr.substring(tgResStr.indexOf(":") + 1);
                String resType = tgResStr.substring(0, tgResStr.indexOf(":"));

                List<String> list = map.get(resType);
                if (list == null) {
                    list = new ArrayList<String>();
                }
                list.add(resId);
                map.put(resType, list);
            }
            
            prjNamesList = getProjectNamesList(map, tagId);
        } catch (Exception e) {
            throw e;
        }
        return prjNamesList;
    }

    private List<String> getProjectNamesList(Map<String, List<String>> map, String tagId) throws Exception {
        List<String> prjNameslist = new ArrayList<String>();
        if (map.size() == 0) {
            return prjNameslist;
        }
        try {
            StringBuffer sb = new StringBuffer();
            sb.append("SELECT dc.dc_name FROM ( \n");
            for (Map.Entry<String, List<String>> entry : map.entrySet()) {
                String resType = entry.getKey();
                String sql = createSQL(resType, entry.getValue());
                sb.append(sql + " UNION ");
            }
            String subStr = sb.substring(0, sb.length() - 6);
            subStr = subStr
                     + " ) AS res LEFT JOIN cloud_project AS prj ON res.res_prjid = prj.prj_id "
                     + " LEFT JOIN dc_datacenter AS dc on prj.dc_id = dc.id GROUP BY dc.dc_name";
            List result = tagResourceDao.createSQLNativeQuery(subStr).getResultList();
            for(int i=0; i<result.size(); i++){
                String prjName = result.get(i)==null?"":result.get(i).toString();
                prjNameslist.add(prjName);
            }
        }catch(Exception e){
            throw e;
        }
        return prjNameslist;
    }

}
