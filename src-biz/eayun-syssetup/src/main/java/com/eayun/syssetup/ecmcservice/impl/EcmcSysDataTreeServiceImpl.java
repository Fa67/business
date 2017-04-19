package com.eayun.syssetup.ecmcservice.impl;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.hibernate.Query;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import com.alibaba.fastjson.JSONObject;
import com.eayun.common.constant.RedisKey;
import com.eayun.common.dao.QueryMap;
import com.eayun.common.dao.support.Page;
import com.eayun.common.redis.JedisUtil;
import com.eayun.common.util.BeanUtils;
import com.eayun.common.util.StringUtil;
import com.eayun.syssetup.dao.EcmcSysDataTreeDao;
import com.eayun.syssetup.ecmcservice.EcmcSysDataTreeService;
import com.eayun.syssetup.model.BaseEcmcSysDataTree;
import com.eayun.syssetup.model.EcmcSysDataTree;

@Service
@Transactional
public class EcmcSysDataTreeServiceImpl implements EcmcSysDataTreeService {

	private static final Logger log = LoggerFactory.getLogger(EcmcSysDataTreeServiceImpl.class);

	@Autowired
	private EcmcSysDataTreeDao ecmcSysDataTreeDao;

	@Autowired
	private JedisUtil jedisUtil;

	@Override
	public EcmcSysDataTree createDataTree(EcmcSysDataTree ecmcDataTree) {
		try {
			if (StringUtil.isEmpty(ecmcDataTree.getParentId())) {
				ecmcDataTree.setParentId("0");
			}
			ecmcDataTree.setFlag("1");
			Long sort = ecmcSysDataTreeDao.findMaxSortByPid(ecmcDataTree.getParentId());
			ecmcDataTree.setSort(sort == null ? 1 : sort + 1);
			ecmcDataTree.setNodeId(this.getIncreasedId(ecmcDataTree.getParentId()));
			BaseEcmcSysDataTree baseDataTree = new BaseEcmcSysDataTree();
			BeanUtils.copyProperties(baseDataTree, ecmcDataTree);
			ecmcSysDataTreeDao.save(baseDataTree);
			//设置缓存
			setDataTreeRedis(ecmcDataTree);
			return ecmcDataTree;
		} catch (Exception e) {
			log.info("添加数据字典时发生异常：{}", e.getMessage());
		}
		return null;
	}

	@Override
	public void delDataTrees(List<String> nodeIds) {
		try {
			// 查询要删除的节点中没有子节点的集合，用于删除缓存
			StringBuffer sqlBuffer = new StringBuffer();
			sqlBuffer.append("SELECT t1.node_id, ");
			sqlBuffer.append("t1.node_name, ");
			sqlBuffer.append("t1.node_name_en, ");
			sqlBuffer.append("t1.parent_id ");
			sqlBuffer.append("FROM sys_data_tree t1 ");
			sqlBuffer.append("LEFT JOIN sys_data_tree t2 ON t1.node_id = t2.parent_id ");
			sqlBuffer.append("WHERE t1.node_id IN (:nodeIds) ");
			sqlBuffer.append("GROUP BY t1.node_id ");
			sqlBuffer.append("HAVING count(t2.node_id) = 0 ");
			List<Object> params = new ArrayList<Object>();
			params.add(nodeIds.toArray());
			List<Object[]> resultList = ecmcSysDataTreeDao.createSQLNativeQuery(sqlBuffer.toString()).unwrap(Query.class).setParameterList("nodeIds", nodeIds).list();
			List<BaseEcmcSysDataTree> deletedDataTrees = new ArrayList<BaseEcmcSysDataTree>();
			if (resultList != null && resultList.size() > 0) {
				for (Object[] objects : resultList) {
					BaseEcmcSysDataTree dataTree = new BaseEcmcSysDataTree();
					dataTree.setNodeId(ObjectUtils.toString(objects[0], null));
					dataTree.setNodeName(ObjectUtils.toString(objects[1], null));
					dataTree.setNodeNameEn(ObjectUtils.toString(objects[2], null));
					dataTree.setParentId(ObjectUtils.toString(objects[3], null));
					deletedDataTrees.add(dataTree);
				}
			}
			// 删除节点（没有子节点的节点）;
			ecmcSysDataTreeDao.delete(deletedDataTrees);
			if(deletedDataTrees.size()>0){
				// 更新缓存信息
				for (BaseEcmcSysDataTree ecmcSysDataTree : deletedDataTrees) {
					String nodeId = ecmcSysDataTree.getNodeId();
					String parentId = ecmcSysDataTree.getParentId();
					String nodeNameEn = ecmcSysDataTree.getNodeNameEn();
					// 先删除Key格式：sys_data_tree:[node_id]，Value：整个该node_id对象的Json格式串。
					jedisUtil.delete(RedisKey.SYS_DATA_TREE + nodeId);
					// 移除该父节点下的该节点Id
					if (!StringUtil.isEmpty(parentId)) {
						jedisUtil.removeFromSet(RedisKey.SYS_DATA_TREE_PARENT_NODEID + parentId, nodeId);
					}
					// 移除该key对应的缓存数据
					if (!StringUtil.isEmpty(nodeNameEn) && !StringUtil.isEmpty(parentId)) {
						jedisUtil.delete(RedisKey.SYS_DATA_TREE_STATUS + (parentId + nodeNameEn));
					}
				}
			}
			
		} catch (Exception e) {
			log.info("删除缓存节点时发生异常：{}", e.getMessage());
		}
	}

	@Override
	public boolean updateDataTree(EcmcSysDataTree ecmcDataTree) {
		try {
			BaseEcmcSysDataTree baseDataTree = new BaseEcmcSysDataTree();
			BeanUtils.copyProperties(baseDataTree, ecmcDataTree);
			ecmcSysDataTreeDao.merge(baseDataTree);
			//设置缓存
			setDataTreeRedis(ecmcDataTree);
			return true;
		} catch (Exception e) {
			log.info("更新缓存节点时发生异常：{}", e.getMessage());
			return false;
		}
	}

	@Override
	public boolean syncDataTree() {
		try {
			//删除全部的节点
			Iterator<String> keys = jedisUtil.keys(RedisKey.SYS_DATA_TREE+"*").iterator();
			String sys_data_tree_key = null;
			while (keys.hasNext()) {
				sys_data_tree_key = (String) keys.next();
				jedisUtil.delete(sys_data_tree_key);
			}
			//删除非根节点
			Iterator<String> parent_node_id_keys = jedisUtil.keys(RedisKey.SYS_DATA_TREE_PARENT_NODEID+"*").iterator();
			String parent_node_id_key = null;
			while (parent_node_id_keys.hasNext()) {
				parent_node_id_key = (String) parent_node_id_keys.next();
				jedisUtil.delete(parent_node_id_key);
			}
			//删除状态节点
			Iterator<String> statues_keys = jedisUtil.keys(RedisKey.SYS_DATA_TREE_STATUS+"*").iterator();
			String statues_key = null;
			while (statues_keys.hasNext()) {
				statues_key = (String) statues_keys.next();
				jedisUtil.delete(statues_key);
			}
			
			// 查询全部的节点，并set到缓存
			Iterator<BaseEcmcSysDataTree> allDataTreeIter = ecmcSysDataTreeDao.findAll().iterator();
			BaseEcmcSysDataTree baseDataTree = null;
			while (allDataTreeIter.hasNext()) {
				baseDataTree = allDataTreeIter.next();
				jedisUtil.set(RedisKey.SYS_DATA_TREE + baseDataTree.getNodeId(), JSONObject.toJSONString(baseDataTree));
			}
			// 查询非根节点，放入缓存
			String sql= "select t1.node_id as nodeId,t2.node_id as parentId from sys_data_tree t1 join sys_data_tree t2 on t1.parent_id=t2.node_id;";
			List<Object[]> result = ecmcSysDataTreeDao.createSQLNativeQuery(sql).getResultList();
			for (Object[] obj : result) {
				jedisUtil.addToSet(RedisKey.SYS_DATA_TREE_PARENT_NODEID + ObjectUtils.toString(obj[1]), ObjectUtils.toString(obj[0]));
			}
			// 查询状态节点，放入缓存
			List<BaseEcmcSysDataTree> statusDataTreeList = ecmcSysDataTreeDao.findStatusDataTree();
			for (BaseEcmcSysDataTree statusDataTree : statusDataTreeList) {
				String nodeId = statusDataTree.getNodeId();
				String parentId = statusDataTree.getParentId();
				String nodeNameEn = statusDataTree.getNodeNameEn();
				String nodeName = statusDataTree.getNodeName();
				if (!StringUtil.isEmpty(parentId)) {
					jedisUtil.set(RedisKey.SYS_DATA_TREE_STATUS + parentId + nodeNameEn, nodeName);
				} else {
					jedisUtil.set(RedisKey.SYS_DATA_TREE_STATUS + nodeId + nodeNameEn, nodeName);
				}
			}
			return true;
		} catch (Exception e) {
			log.info("同步缓存时发生异常：{}", e.getMessage());
			return false;
		}
	}

	@Override
	public Page getDataTreeList(String nodeId, String nodeNameZh, String parentId, QueryMap queryMap) {
		StringBuffer sqlBuff = new StringBuffer();
		List<String> params = new ArrayList<String>();
		sqlBuff.append("SELECT t1.node_id, ");		//节点ID
		sqlBuff.append("t1.node_name, ");				//节点名称
		sqlBuff.append("t1.parent_id, ");					//父节点ID
		sqlBuff.append("t1.sort, ");							//排序
		sqlBuff.append("t1.is_root, ");						//是否为根节点
		sqlBuff.append("t1.memo, ");						//描述
		sqlBuff.append("t1.flag, ");								//可用标识
		sqlBuff.append("t1.para1, ");							//参数1
		sqlBuff.append("t1.para2, ");							//参数2
		sqlBuff.append("t1.image_path, ");				//图片路径
		sqlBuff.append("t1.node_name_en, ");		//节点英文名
		sqlBuff.append("t1.icon, ");							//图标
		sqlBuff.append("count(t2.node_id) ");			//子节点数量
		sqlBuff.append("FROM sys_data_tree t1 ");
		sqlBuff.append("LEFT JOIN sys_data_tree t2 ON t1.node_id = t2.parent_id ");
		sqlBuff.append("WHERE t1.flag = 1 ");
		// 如果nodeId不为空，则根据nodeId精确查询
		if (!StringUtil.isEmpty(nodeId)) {
			sqlBuff.append("AND t1.node_id = ? ");
			params.add(nodeId);
		} else if (!StringUtil.isEmpty(parentId)) { // 如果父节点不为空，则根据父节点查询
			sqlBuff.append("AND t1.parent_id = ? ");
			params.add(parentId);
		}
		if (!StringUtil.isEmpty(nodeNameZh)) {
			sqlBuff.append("AND t1.node_name LIKE ? ESCAPE '/' ");
			params.add("%" + escapeSpecialChar(nodeNameZh) + "%");
		}
		sqlBuff.append("GROUP BY t1.node_id ");
		sqlBuff.append("ORDER BY t1.parent_id, t1.sort ASC ");
		Page page = ecmcSysDataTreeDao.pagedNativeQuery(sqlBuff.toString(), queryMap, params.toArray());
		List<Object[]> result = (List<Object[]>)page.getResult();
		List<EcmcSysDataTree> dataTreeList = new ArrayList<EcmcSysDataTree>();
		if (result != null && result.size() > 0) {
			for (Object[] objects : result) {
				EcmcSysDataTree dataTree = new EcmcSysDataTree();
				dataTree.setNodeId(ObjectUtils.toString(objects[0], null));
				dataTree.setNodeNameZh(ObjectUtils.toString(objects[1], null));
				dataTree.setParentId(ObjectUtils.toString(objects[2], null));
				dataTree.setSort(Long.parseLong(ObjectUtils.toString(objects[3], "")));
				dataTree.setIsRoot(ObjectUtils.toString(objects[4], null));
				dataTree.setMemo(ObjectUtils.toString(objects[5], null));
				dataTree.setFlag(ObjectUtils.toString(objects[6], null));
				dataTree.setPara1(ObjectUtils.toString(objects[7], null));
				dataTree.setPara2(ObjectUtils.toString(objects[8], null));
				dataTree.setImagePath(ObjectUtils.toString(objects[9], null));
				dataTree.setNodeNameEn(ObjectUtils.toString(objects[10], null));
				dataTree.setIcon(ObjectUtils.toString(objects[11], null));
				dataTree.setChildrenSize(Integer.parseInt(ObjectUtils.toString(objects[12], "0")));
				dataTreeList.add(dataTree);
			}
		}
		page.setResult(dataTreeList);
		return page;
	}

	@Override
	public boolean sortDataTree(ArrayList<String> nodeIds, ArrayList<Integer> nodeSorts) {
		try {
			if (nodeIds != null && nodeSorts != null && nodeIds.size() == nodeSorts.size()) {
				for (int i = 0; i < nodeIds.size(); i++) {
					Integer t = nodeSorts.get(i);
					if (t == null) {
						t = 1;
					}
					ecmcSysDataTreeDao.updateDataTreeSort(nodeIds.get(i), t);
				}
				return true;
			}
		} catch (Exception e) {
		}
		return false;
	}

	@Override
	public EcmcSysDataTree getDataTreeById(String nodeId) {
		try {
			BaseEcmcSysDataTree baseDatatree = ecmcSysDataTreeDao.findOne(nodeId);
			EcmcSysDataTree ecmcDataTree = new EcmcSysDataTree();
			BeanUtils.copyPropertiesByModel(ecmcDataTree, baseDatatree);
			return ecmcDataTree;
		} catch (Exception e) {
			throw e;
		}
	}

	/**
	 * 获取自增长ID
	 *
	 * @param parentId
	 * @return
	 */
	private String getIncreasedId(String parentId) {
		String max = ecmcSysDataTreeDao.findMaxId(parentId);
		String iId = null;
		if (max != null && !"".equals(max)) {
			iId = this.incr(max, false);
		} else {
			iId = this.incr(parentId, true);
		}
		return iId;
	}

	private String incr(String v, boolean newObj) {
		String INITJID = "000";
		if (newObj) {
			v += INITJID;
		}
		String id = v.substring(v.length() - INITJID.length());
		Integer jid = Integer.parseInt(id);
		jid++;
		DecimalFormat df = new DecimalFormat(INITJID);
		StringBuffer sb = new StringBuffer(v.substring(0, v.length() - INITJID.length())).append(df.format(jid));
		return sb.toString();
	}

	@Override
	public EcmcSysDataTree getDataTreeNav() {
		// 拼装根节点
		EcmcSysDataTree rootDataTree = new EcmcSysDataTree();
		rootDataTree.setNodeId("0");
		rootDataTree.setClosed(true);
		rootDataTree.setNodeNameZh("数据字典");
		// 获取子节点
		try {
			Iterator<BaseEcmcSysDataTree> baseDataTreeIter = ecmcSysDataTreeDao.findAll().iterator();
			HashMap<String, EcmcSysDataTree> dataTreeMap = new HashMap<String, EcmcSysDataTree>();
			while (baseDataTreeIter.hasNext()) {
				BaseEcmcSysDataTree baseDataTree = baseDataTreeIter.next();
				EcmcSysDataTree dataTree = new EcmcSysDataTree();
				BeanUtils.copyPropertiesByModel(dataTree, baseDataTree);
				dataTree.setNodeNameZh(dataTree.getNodeName());
				dataTree.setNodeName(null);
				dataTreeMap.put(dataTree.getNodeId(), dataTree);
			}
			rootDataTree.setChildren(embedDataTree(dataTreeMap));
		} catch (Exception e) {
			log.error("获取数据字典导航树发生异常：{}"+e.getMessage(),e);
		}
		return rootDataTree;
	}

	private ArrayList<EcmcSysDataTree> embedDataTree(HashMap<String, EcmcSysDataTree> dataTreeMap) {
		// 数据记录中的根节点
		ArrayList<EcmcSysDataTree> rootDataTree = new ArrayList<EcmcSysDataTree>();
		Set<Entry<String, EcmcSysDataTree>> entrySet = dataTreeMap.entrySet();
		// 遍历Map，将数据字典对象add到父节点的children集合属性中
		for (Iterator<Entry<String, EcmcSysDataTree>> it = entrySet.iterator(); it.hasNext();) {
			EcmcSysDataTree dataTree = (EcmcSysDataTree) ((Map.Entry<String, EcmcSysDataTree>) it.next()).getValue();
			if ("0".equals(dataTree.getParentId())) {
				rootDataTree.add(dataTree); // 根节点
			} else {
				((EcmcSysDataTree) dataTreeMap.get(dataTree.getParentId())).addChild(dataTree);
			}
		}
		return rootDataTree;
	}

	@Override
	public List<EcmcSysDataTree> getDataTreeChildren(String parentId) {
		List<BaseEcmcSysDataTree> baseDataTreeList = ecmcSysDataTreeDao.findDataTreeChildren(parentId);
		List<EcmcSysDataTree> ecmcDataTreeList = new ArrayList<EcmcSysDataTree>();
		if(!CollectionUtils.isEmpty(baseDataTreeList)){
			for (BaseEcmcSysDataTree baseEcmcSysDataTree : baseDataTreeList) {
				EcmcSysDataTree ecmcDataTree = new EcmcSysDataTree();
				BeanUtils.copyPropertiesByModel(ecmcDataTree, baseEcmcSysDataTree);
				ecmcDataTreeList.add(ecmcDataTree);
			}
		}
		return ecmcDataTreeList;
	}
	
	private void setDataTreeRedis(EcmcSysDataTree ecmcDataTree){
		try {
			// 更新缓存节点
			jedisUtil.set(RedisKey.SYS_DATA_TREE + ecmcDataTree.getNodeId(), JSONObject.toJSONString(ecmcDataTree));

			if (null != ecmcDataTree.getParentId() && !"".equals(ecmcDataTree.getParentId())) {
				jedisUtil.addToSet(RedisKey.SYS_DATA_TREE_PARENT_NODEID + ecmcDataTree.getParentId(), ecmcDataTree.getNodeId());
			}
			if (null != ecmcDataTree.getNodeNameEn() && !"".equals(ecmcDataTree.getNodeNameEn())) {
				jedisUtil.set(RedisKey.SYS_DATA_TREE_STATUS + ecmcDataTree.getParentId() + ecmcDataTree.getNodeNameEn(),
						ecmcDataTree.getNodeName());
			}
		} catch (Exception e) {
			log.info("操作缓存发生异常：{}", e.getMessage());
		}
	}

	private String escapeSpecialChar(String str) {
		if (StringUtils.isNotBlank(str)) {
			String[] specialChars = { "\\", "$", "(", ")", "*", "+", ".", "[", "]", "?", "^", "{", "}", "|", "%" };
			for (String key : specialChars) {
				if (str.contains(key)) {
					str = str.replace(key, "/" + key);
				}
			}
		}
		return str;
	}
	
}
