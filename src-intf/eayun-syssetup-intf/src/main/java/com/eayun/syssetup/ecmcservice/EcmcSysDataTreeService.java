package com.eayun.syssetup.ecmcservice;

import java.util.ArrayList;
import java.util.List;

import com.eayun.common.dao.QueryMap;
import com.eayun.common.dao.support.Page;
import com.eayun.syssetup.model.EcmcSysDataTree;

public interface EcmcSysDataTreeService {
	
	public EcmcSysDataTree createDataTree(EcmcSysDataTree ecmcDataTree);
	
	public void delDataTrees(List<String> nodeIds);
	
	public boolean updateDataTree(EcmcSysDataTree ecmcDataTree);
	
	public boolean syncDataTree();
	
	public Page getDataTreeList(String nodeId, String nodeNameZh, String parentId, QueryMap queryMap);
	
	public boolean sortDataTree(ArrayList<String> nodeIds, ArrayList<Integer> nodeSorts);
	
	public EcmcSysDataTree getDataTreeById(String nodeId);

	public EcmcSysDataTree getDataTreeNav();

	public List<EcmcSysDataTree> getDataTreeChildren(String parentId);
	
}
