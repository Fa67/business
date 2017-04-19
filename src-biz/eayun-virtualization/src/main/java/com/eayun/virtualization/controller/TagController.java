package com.eayun.virtualization.controller;

import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.alibaba.fastjson.JSONObject;
import com.eayun.common.ConstantClazz;
import com.eayun.common.controller.BaseController;
import com.eayun.common.dao.ParamsMap;
import com.eayun.common.dao.support.Page;
import com.eayun.common.filter.SessionUserInfo;
import com.eayun.virtualization.bean.ResourceTag;
import com.eayun.virtualization.model.Tag;
import com.eayun.virtualization.model.TagGroup;
import com.eayun.virtualization.model.TagResource;
import com.eayun.virtualization.service.TagService;

/**
 * 标签操作Controller
 *                       
 * @Filename: TagController.java
 * @Description: 
 * @Version: 1.0
 * @Author: fan.zhang
 * @Email: fan.zhang@eayun.com
 * @History:<br>
 *<li>Date: 2015年11月9日</li>
 *<li>Version: 1.0</li>
 *<li>Content: create</li>
 *
 */
@Controller
@RequestMapping("/tag")
@Scope("prototype")
public class TagController extends BaseController{
    private static final Logger log = LoggerFactory.getLogger(TagController.class);
    @Autowired
    private TagService          tagService;

    //参数的传递——map->拼接的参数， 实体对象->实体对象，分页Page page ParamsMap
    @RequestMapping(value = "/getTagGroupList", method = RequestMethod.POST)
    @ResponseBody
    public String getTagGroupList(HttpServletRequest request) throws Exception {
        log.info("标签类别列表查询开始");
        SessionUserInfo sessionUser = (SessionUserInfo) request.getSession().getAttribute(ConstantClazz.SYS_SESSION_USERINFO);
        String cusId = sessionUser.getCusId();
        List<TagGroup> tagGroupList = tagService.getTagGroupList(cusId);
        return JSONObject.toJSONString(tagGroupList);
    }
    
    /**
     * 查询已启用的且可标记当前资源类型的标签类别列表
     * @param request
     * @return
     * @throws Exception
     */
    @RequestMapping(value = "/getEnabledTagGroupList", method = RequestMethod.POST)
    @ResponseBody
    public String getEnabledTagGroupList(HttpServletRequest request, @RequestBody Map map) throws Exception {
        log.info("已启用的标签类别列表查询开始");
        SessionUserInfo sessionUser = (SessionUserInfo) request.getSession().getAttribute(ConstantClazz.SYS_SESSION_USERINFO);
        String cusId = sessionUser.getCusId();
        String resType = map.get("resType").toString();
        List<TagGroup> tagGroupList = tagService.getEnabledTagGroupList(cusId,resType);
        return JSONObject.toJSONString(tagGroupList);
    }
    /**
     * 查询已启用的且可标记当前资源类型的标签类别列表
     * @param request
     * @return
     * @throws Exception
     */
    @RequestMapping(value = "/getEnabledTagGroupListByCusID", method = RequestMethod.POST)
    @ResponseBody
    public String getEnabledTagGroupListByCusID(HttpServletRequest request, @RequestBody Map map) throws Exception {
        log.info("已启用的标签类别列表查询开始");
        SessionUserInfo sessionUser = (SessionUserInfo) request.getSession().getAttribute(ConstantClazz.SYS_SESSION_USERINFO);
        String cusId = sessionUser.getCusId();
        List<TagGroup> tagGroupList = tagService.getEnabledTagGroupListByCusID(cusId);
        return JSONObject.toJSONString(tagGroupList);
    }

    @RequestMapping(value = "/getAvailableTagList", method = RequestMethod.POST)
    @ResponseBody
    public String getAvailableTagList(HttpServletRequest request, @RequestBody Map map)
            throws Exception {
        log.info("获取当前资源未标记的可用标签查询开始");
        String tagGroupId = map.get("tagGroupId").toString();
        String resType = map.get("resType").toString();
        String resId = map.get("resId").toString();
        List<Tag> tagList = tagService.getAvailableTagList(tagGroupId, resType, resId);
        return JSONObject.toJSONString(tagList);
    }
    
    @RequestMapping(value = "/getTagList", method = RequestMethod.POST)
    @ResponseBody
    public String getTagList(HttpServletRequest request, @RequestBody Map map)
                                                                                     throws Exception {
        log.info("标签列表查询开始");
        String tagGroupId = map.get("tagGroupId").toString();
        List<Tag> tagList = tagService.getTagList(tagGroupId);
        return JSONObject.toJSONString(tagList);
    }
    
    @RequestMapping(value = "/getPagedTagList", method = RequestMethod.POST)
    @ResponseBody
    public String getPagedTagList(HttpServletRequest request, Page page,@RequestBody ParamsMap map)
            throws Exception {
        log.info("标签列表分页查询开始");
        String tagGroupId = map.getParams().get("tagGroupId").toString();
        int pageSize = map.getPageSize();
        int pageNumber = map.getPageNumber();
        
        page = tagService.getPagedTagList(tagGroupId, pageSize, pageNumber);
        return JSONObject.toJSONString(page);
    }
    

    @RequestMapping(value = "/getTagGroupById", method = RequestMethod.POST)
    @ResponseBody
    public String getTagGroupById(HttpServletRequest request, @RequestBody Map map)
                                                                                   throws Exception {
        log.info("标签类别查询开始");
        String tagGroupId =  map.get("tagGroupId").toString();
        TagGroup tagGroup = tagService.getTagGroupById(tagGroupId);
        return JSONObject.toJSONString(tagGroup);
    }
    
    @RequestMapping(value = "/getTagById", method = RequestMethod.POST)
    @ResponseBody
    public String getTagById(HttpServletRequest request, @RequestBody Map map)
            throws Exception {
        log.info("标签查询开始");
        String tagId = map.get("tagId").toString();
        Tag tag = tagService.getTagById(tagId);
        return JSONObject.toJSONString(tag);
    }
    
    @RequestMapping(value = "/getTagResourceList", method = RequestMethod.POST)
    @ResponseBody
    public String getTagResourceList(HttpServletRequest request, @RequestBody Map map)
            throws Exception {
        log.info("标签标记资源查询开始");
        String tagId = (map.get("tagId") == null) ? null : map.get("tagId")
            .toString();
        List<TagResource> tagResList = tagService.getTagResourceList(tagId);
        return JSONObject.toJSONString(tagResList);
    }
    
    @RequestMapping(value = "/getPagedTagResourceList", method = RequestMethod.POST)
    @ResponseBody
    public String getPagedTagResourceList(HttpServletRequest request, Page page,@RequestBody ParamsMap map)
            throws Exception {
        log.info("标签标记资源分页查询开始");
        String tagId = map.getParams().get("tagId")==null?null:map.getParams().get("tagId").toString();
        String prjName = map.getParams().get("prjName")==null?null:map.getParams().get("prjName").toString();
        int pageSize = map.getPageSize();
        int pageNumber = map.getPageNumber();
        
        page = tagService.getPagedTagResourceList(tagId, prjName, pageSize, pageNumber);
        return JSONObject.toJSONString(page);
    }
    
    @RequestMapping(value = "/getTagResourceListByPrjName", method = RequestMethod.POST)
    @ResponseBody
    public String getTagResourceListByPrjName(HttpServletRequest request, @RequestBody Map map)
            throws Exception {
        log.info("标签标记资源查询开始");
        String prjName = (map.get("prjName") == null) ? null : map.get("prjName")
            .toString();
        String tagId = (map.get("tagId") == null) ? null : map.get("tagId")
            .toString();
        List<TagResource> tagResList = tagService.getTagResourceList(tagId,prjName);
        return JSONObject.toJSONString(tagResList);
    }
    
    @RequestMapping(value = "/getParamsByTagId", method = RequestMethod.POST)
    @ResponseBody
    public String getParamsByTagId(HttpServletRequest request, @RequestBody Map map)
            throws Exception {
        log.info("获取查看资源的标签名称和所属项目下拉列表");
        String tagId = (map.get("tagId") == null) ? null : map.get("tagId")
            .toString();
        JSONObject json = tagService.getParamsByTagId(tagId);
        return json.toJSONString();
    }
    
    @RequestMapping(value = "/checkTagGroupName" , method = RequestMethod.POST)
    @ResponseBody
    public boolean checkTagGroupName(HttpServletRequest request, @RequestBody Map map) throws Exception {
        log.info("验证标签类别名称重复开始");
        String tagGroupName = map.get("tagGroupName").toString();
        SessionUserInfo sessionUserInfo = (SessionUserInfo) request.getSession().getAttribute(
            ConstantClazz.SYS_SESSION_USERINFO);
        return tagService.checkTagGroupName(sessionUserInfo.getCusId(), tagGroupName);
    }
    
    @RequestMapping(value = "/checkTagGroupAbbr" , method = RequestMethod.POST)
    @ResponseBody
    public boolean checkTagGroupAbbr(HttpServletRequest request, @RequestBody Map map) throws Exception {
        log.info("验证标签类别简称重复开始");
        String tagGroupAbbr = map.get("tagGroupAbbr").toString();
        String tagGroupId = map.get("tagGroupId")==null?null:map.get("tagGroupId").toString();
        SessionUserInfo sessionUserInfo = (SessionUserInfo) request.getSession().getAttribute(
            ConstantClazz.SYS_SESSION_USERINFO);
        return tagService.checkTagGroupAbbr(sessionUserInfo.getCusId(), tagGroupAbbr,tagGroupId);
    }

    @RequestMapping(value = "/checkTagName" , method = RequestMethod.POST)
    @ResponseBody
    public boolean checkTagName(HttpServletRequest request, @RequestBody Map map) throws Exception {
        log.info("验证标签名称重复开始");
        String tagName = map.get("tagName").toString();
        String tagGroupId = map.get("tagGroupId").toString();
        String tagId = map.get("tagId")==null?null:map.get("tagId").toString();
        return tagService.checkTagName(tagGroupId, tagName, tagId);
    }
    
    @RequestMapping(value = "/addTagGroup", method = RequestMethod.POST)
    @ResponseBody
    public String addTagGroup(HttpServletRequest request, @RequestBody TagGroup tagGroup)
                                                                                         throws Exception {
        log.info("添加标签类别开始");
        try {
            SessionUserInfo sessionUserInfo = (SessionUserInfo) request.getSession().getAttribute(
                ConstantClazz.SYS_SESSION_USERINFO);
            String cusId = sessionUserInfo.getCusId();
            String userId = sessionUserInfo.getUserId();
            tagGroup.setCreateDate(new Date());
            tagGroup.setCusId(cusId);
            tagGroup.setCreatorId(userId);
            tagGroup = tagService.addTagGroup(tagGroup);
        } catch (Exception e) {
            throw e;
        }
        return JSONObject.toJSONString(tagGroup);
    }
    
    @RequestMapping(value = "/addTag", method = RequestMethod.POST)
    @ResponseBody
    public String addTag(HttpServletRequest request, @RequestBody Tag tag)
            throws Exception {
        log.info("添加标签开始");
        try {
            SessionUserInfo sessionUserInfo = (SessionUserInfo) request.getSession().getAttribute(
                ConstantClazz.SYS_SESSION_USERINFO);
            String userId = sessionUserInfo.getUserId();
            tag.setCreateDate(new Date());
            tag.setCreatorId(userId);
            tag = tagService.addTag(tag);
        } catch (Exception e) {
            throw e;
        }
        return JSONObject.toJSONString(tag);
    }

    @RequestMapping(value = "/editTagGroup", method = RequestMethod.POST)
    @ResponseBody
    public String editTagGroup(HttpServletRequest request, @RequestBody TagGroup tagGroup)
                                                                                          throws Exception {
        log.info("编辑标签类别");
        try {
            //这里的enabled和unique均为空，如何在ng的情况下在界面上给radio设置默认值——之所以界面没值是因为getTagGroupById时没有把所有的值set到tagGroup中
            tagGroup = tagService.updateTagGroup(tagGroup);
        } catch (Exception e) {
            throw e;
        }
        return JSONObject.toJSONString(tagGroup);
    }
    @RequestMapping(value = "/editTag", method = RequestMethod.POST)
    @ResponseBody
    public String editTag(HttpServletRequest request, @RequestBody Tag tag)
            throws Exception {
        log.info("编辑标签");
        try {
            //这里的enabled和unique均为空，如何在ng的情况下在界面上给radio设置默认值——之所以界面没值是因为getTagGroupById时没有把所有的值set到tagGroup中
            tag = tagService.updateTag(tag);
        } catch (Exception e) {
            throw e;
        }
        return JSONObject.toJSONString(tag);
    }
    
    @RequestMapping(value = "/deleteTagGroup" , method = RequestMethod.POST)
    @ResponseBody
    public String deleteTagGroup(HttpServletRequest request , @RequestBody Map map) throws Exception {
        log.info("删除标签类别开始");
        boolean isDeleted = false;
        try {
            String tagGroupId = map.get("tagGroupId").toString();
            String cusId = map.get("cusId").toString();
            isDeleted = tagService.deleteTagGroup(tagGroupId,cusId);
        } catch (Exception e) {
            throw e;
        }
        return JSONObject.toJSONString(isDeleted);
    }
    
    @RequestMapping(value = "/deleteTag" , method = RequestMethod.POST)
    @ResponseBody
    public String deleteTag(HttpServletRequest request , @RequestBody Map map) throws Exception {
        log.info("删除标签开始");
        boolean isDeleted = false;
        try {
            String tagGroupId = map.get("tagGroupId").toString();
            String tagId = map.get("tagId").toString();
            isDeleted = tagService.deleteTag(tagId, tagGroupId);
        } catch (Exception e) {
            throw e;
        }
        return JSONObject.toJSONString(isDeleted);
    }
    @RequestMapping(value = "/afterDeletingResource" , method = RequestMethod.POST)
    @ResponseBody
    public String afterDeletingResource(HttpServletRequest request , @RequestBody Map map) throws Exception {
        log.info("删除资源后刷新缓存操作开始");
        boolean isRefreshed = false;
        try {
            String resType = map.get("resType").toString();
            String resId = map.get("resId").toString();
            isRefreshed = tagService.refreshCacheAftDelRes(resType, resId);
        } catch (Exception e) {
            throw e;
        }
        return JSONObject.toJSONString(isRefreshed);
    }
    
    @RequestMapping(value = "/tagResource" , method = RequestMethod.POST)
    @ResponseBody
    public String tagResource(HttpServletRequest request , @RequestBody Map map) throws Exception {
        log.info("标签标记资源开始");
        JSONObject json = new JSONObject();
        TagResource tgRes;
        try {
            String resType = map.get("resType").toString();
            String resId = map.get("resId").toString();
            String tagGroupId = map.get("tagGroupId").toString();
            //XXX 如果没选择标签，则前台传入的tagId="{}"，这是为何？
            String tagId = map.get("tagId")==null?null:map.get("tagId").toString();
            tgRes = tagService.tagResource(resType, resId, tagGroupId, tagId);
            json.put("tagResource", tgRes);
            json.put("result", true);
        } catch (Exception e) {
            json.put("result", false);
            throw e;
        }
        return json.toJSONString();
    }
    @RequestMapping(value = "/getResourceTag" , method = RequestMethod.POST)
    @ResponseBody
    public String getResourceTag(HttpServletRequest request , @RequestBody Map map) throws Exception {
        log.info("获取资源已标记的标签开始");
        String resType = map.get("resType").toString();
        String resId = map.get("resId").toString();
        List<ResourceTag> resTagList = tagService.getResourceTag(resType, resId);
        return JSONObject.toJSONString(resTagList);
    }
    
    @RequestMapping(value = "/getResourceTagForShowcase" , method = RequestMethod.POST)
    @ResponseBody
    public String getResourceTagForShowcase(HttpServletRequest request , @RequestBody Map map) throws Exception {
        log.info("获取资源已标记的标签开始");
        String resType = map.get("resType").toString();
        String resId = map.get("resId").toString();
        String tags = tagService.getResourceTagForShowcase(resType, resId);
        JSONObject json = new JSONObject();
        json = json.parseObject(tags);
        return JSONObject.toJSONString(json);
    }
    
    @RequestMapping(value = "/untagResource" , method = RequestMethod.POST)
    @ResponseBody
    public String untagResource(HttpServletRequest request , @RequestBody Map map) throws Exception {
        log.info("取消标记开始");
        boolean isUntagged = false;
        try {
            String tagId = map.get("tagId").toString();
            String resType = map.get("resType").toString();
            String resId = map.get("resId").toString();
            isUntagged = tagService.untagResource(tagId,resType, resId);
        } catch (Exception e) {
            throw e;
        }
        return JSONObject.toJSONString(isUntagged);
    }
    @RequestMapping(value = "/clearTagResByTagGroupId" , method = RequestMethod.POST)
    @ResponseBody
    public String clearTagResByTagGroupId(HttpServletRequest request , @RequestBody Map map) throws Exception {
        log.info("取消标签类别下标签标记的所有资源开始");
        boolean isUntagged = false;
        try {
            String tagGroupId = map.get("tagGroupId").toString();
            isUntagged = tagService.clearTagResByTagGroupId(tagGroupId);
        } catch (Exception e) {
            throw e;
        }
        return JSONObject.toJSONString(isUntagged);
    }
    
    @RequestMapping(value = "/syncRedisWithDB.do")
    @ResponseBody
    public String syncRedisWithDB(HttpServletRequest request) throws Exception {
        log.info("同步Redis和DB开始");
        String msg = tagService.synchronizeRedisWithDB();
        return JSONObject.toJSONString(msg);
    }
}
