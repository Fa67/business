package com.eayun.virtualization.service;

import java.util.List;

import com.alibaba.fastjson.JSONObject;
import com.eayun.common.dao.support.Page;
import com.eayun.virtualization.bean.ResourceTag;
import com.eayun.virtualization.model.Tag;
import com.eayun.virtualization.model.TagGroup;
import com.eayun.virtualization.model.TagResource;

public interface TagService {
    public List<TagGroup> getTagGroupList(String cusId);

    public List<TagGroup> getEnabledTagGroupList(String cusId, String resType);
    
    public List<TagGroup> getEnabledTagGroupListByCusID(String cusId);

    public TagGroup getTagGroupById(String tagGroupId);

    public TagGroup addTagGroup(TagGroup tagGroup);

    public TagGroup updateTagGroup(TagGroup tagGroup);

    public boolean deleteTagGroup(String tagGroupId, String cusId);

    public List<Tag> getTagList(String tagGroupId);

    public List<Tag> getAvailableTagList(String tagGroupId, String resType, String resId);

    public Tag getTagById(String tagId);

    public Tag addTag(Tag tag);

    public Tag updateTag(Tag tag);

    public boolean deleteTag(String tagId, String tagGroupId);

    public boolean checkTagGroupName(String cusId, String tagGroupName);

    public boolean checkTagGroupAbbr(String cusId, String tagGroupAbbr, String tagGroupId);

    public boolean checkTagName(String tagGroupId, String tagName, String tagId);

    public List<TagResource> getTagResourceList(String tagId);

    public TagResource tagResource(String resType, String resId, String tagGroupId, String tagId);

    public boolean untagResource(String tagId, String resType, String resId);

    public List<ResourceTag> getResourceTag(String resType, String resId);

    public String getResourceTagForShowcase(String resType, String resId);

    public List<TagResource> getTagResourceList(String tagId, String prjName);

    public boolean clearTagResByTagGroupId(String tagGroupId);

    public boolean refreshCacheAftDelRes(String resType, String resId);

    public Page getPagedTagList(String tagGroupId, int pageSize, int currentPageNumber);

    public Page getPagedTagResourceList(String tagId, String prjName, int pageSize, int currentPageNumber);
    
    public String synchronizeRedisWithDB();

    public JSONObject getParamsByTagId(String tagId);


}
