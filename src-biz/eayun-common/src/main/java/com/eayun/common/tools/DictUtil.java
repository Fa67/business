package com.eayun.common.tools;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.fastjson.JSONObject;
import com.eayun.common.CloudStatusType;
import com.eayun.common.constant.RedisKey;
import com.eayun.common.exception.AppException;
import com.eayun.common.redis.JedisUtil;
import com.eayun.sys.model.SysDataTree;

public class DictUtil {
    private static final Logger log = LoggerFactory.getLogger(DictUtil.class);

    public static SysDataTree getDataTreeByNodeId(String nodeId) {
        String dataTreeStr;
        SysDataTree tree = new SysDataTree();
        try {
            dataTreeStr = JedisUtil.getInstance().get(RedisKey.SYS_DATA_TREE + nodeId);
            
            if(dataTreeStr == null) {
            	return null ;
            }
            
            JSONObject dataTree = JSONObject.parseObject(dataTreeStr);

            tree.setNodeId(nodeId);
            tree.setNodeName(dataTree.getString("nodeName"));
            tree.setParentId(dataTree.getString("parentId"));
            tree.setIsRoot(dataTree.getString("isRoot"));
            tree.setFlag(dataTree.getString("flag"));
            tree.setNodeNameEn(dataTree.getString("nodeNameEn"));
            tree.setIcon(dataTree.getString("icon"));
            tree.setImagePath(dataTree.getString("imagePath"));
            tree.setMemo(dataTree.getString("memo"));
            tree.setPara1(dataTree.getString("para1"));
            tree.setPara2(dataTree.getString("para2"));
            tree.setSort(Long.parseLong(dataTree.getString("sort")));
        } catch (Exception e) {
            log.error(e.getMessage(),e);
            throw new AppException(e.getMessage(), new String[] { "根据nodeId获取SysDataTree实体对象异常" });
        }
        return tree;
    }

    public static List<SysDataTree> getDataTreeByParentId(String parentId) {
        List<SysDataTree> treeList = new ArrayList<SysDataTree>();
        Set<String> dataTreeSet;
        try {
            dataTreeSet = JedisUtil.getInstance().getSet(
                RedisKey.SYS_DATA_TREE_PARENT_NODEID + parentId);
            for (String nodeId : dataTreeSet) {
                SysDataTree tree = getDataTreeByNodeId(nodeId);
                if(tree != null) {
                	treeList.add(tree);
                }
            }
            Collections.sort(treeList, new Comparator<SysDataTree>() {
                @Override
                public int compare(SysDataTree o1, SysDataTree o2) {
                    return (int) (o1.getSort() - o2.getSort());
                }

            });

        } catch (Exception e) {
            throw new AppException(e.getMessage(), new String[] { "获取父节点下数据列表异常" });
        }
        return treeList;
    }

    public static String getStatusByNodeEn(String type, String status) {
        String cloudStatus = "";
        try {
            if (null != type && !"".equals(type) && null != status && !"".equals(status)) {
                type = type.trim();
                status = status.trim();
                if ("vm".equals(type)) {
                    cloudStatus = JedisUtil.getInstance().get(
                        RedisKey.SYS_DATA_TREE_STATUS + (CloudStatusType.vmStatus + status));
                } else if ("volume".equals(type)) {
                    cloudStatus = JedisUtil.getInstance().get(
                        RedisKey.SYS_DATA_TREE_STATUS + (CloudStatusType.volStatus + status));
                } else if ("image".equals(type)) {
                    cloudStatus = JedisUtil.getInstance().get(
                        RedisKey.SYS_DATA_TREE_STATUS + (CloudStatusType.imageStatus + status));
                } else if ("fireWall".equals(type)) {
                    cloudStatus = JedisUtil.getInstance().get(
                        RedisKey.SYS_DATA_TREE_STATUS + (CloudStatusType.fwStatus + status));
                } else if ("snapshot".equals(type)) {
                    cloudStatus = JedisUtil.getInstance().get(
                        RedisKey.SYS_DATA_TREE_STATUS + (CloudStatusType.volSnapStatus + status));
                } else if ("ldPool".equals(type)) {
                    cloudStatus = JedisUtil.getInstance().get(
                        RedisKey.SYS_DATA_TREE_STATUS + (CloudStatusType.ldPoolStatus + status));
                } else if ("ldMember".equals(type)) {
                    cloudStatus = JedisUtil.getInstance().get(
                        RedisKey.SYS_DATA_TREE_STATUS + (CloudStatusType.ldMemberStatus + status));
                } else if ("ldVip".equals(type)) {
                    cloudStatus = JedisUtil.getInstance().get(
                        RedisKey.SYS_DATA_TREE_STATUS + (CloudStatusType.ldVipStatus + status));
                } else if ("netWork".equals(type)) {
                    cloudStatus = JedisUtil.getInstance().get(
                        RedisKey.SYS_DATA_TREE_STATUS + (CloudStatusType.netWorkStatus + status));
                } else if ("route".equals(type)) {
                    cloudStatus = JedisUtil.getInstance().get(
                        RedisKey.SYS_DATA_TREE_STATUS + (CloudStatusType.routeStatus + status));
                } else if ("ldType".equals(type)) {
                    cloudStatus = JedisUtil.getInstance().get(
                        RedisKey.SYS_DATA_TREE_STATUS + (CloudStatusType.ldType + status));
                } else if ("vpn".equals(type)) {
                    cloudStatus = JedisUtil.getInstance().get(
                        RedisKey.SYS_DATA_TREE_STATUS + (CloudStatusType.vpnType + status));
                } else if("rds".equals(type)) {
                	cloudStatus = JedisUtil.getInstance().get(
                            RedisKey.SYS_DATA_TREE_STATUS + (CloudStatusType.rdsStatus + status));
                }
            }
        } catch (Exception e) {
            throw new AppException(e.getMessage(), new String[] { "获取资源状态失败" });
        }
        return cloudStatus;
    }

}
