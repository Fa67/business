package com.eayun.eayunstack.service;

import java.util.List;

import com.alibaba.fastjson.JSONObject;
import com.eayun.eayunstack.model.SwiftObject;

public interface OpenstackSwiftService extends OpenstackBaseService<SwiftObject>{
	
	/**
	 * <p>获取token用于下载Swift上的日志</p>
	 * ----------------------
	 * @author zhouhaitao
	 * @param dcId				数据中心ID
	 * @param prjId				项目ID
	 * @return
	 * @throws Exception 
	 */
	public String download(String dcId,String prjId) throws Exception;
	
	/**
	 * <p>查询实例的指定类型的RDS的日志文件列表</p>
	 * --------------------------------------
	 * @author zhouhaitao
	 * 
	 * @param dcId				数据中心ID
	 * @param prjId				项目ID
	 * @param rdsInstanceIs		RDS实例ID
	 * @param type				日志类型
	 * @param fileCount			文件个数
	 * @return
	 */
	public List<SwiftObject> list(String dcId,String prjId,String rdsInstanceIs,String type,int fileCount);
	
	/**
	 * <p>修改Container的meta信息</p>
	 * -----------------------
	 * @author zhouhaitao
	 * 
	 * @param dcId				数据中心ID
	 * @param prjId				项目ID
	 * @param container			Container名称
	 * @param data				参数信息
	 */
	public void update(String dcId,String prjId,String container,JSONObject data);
	
	/**
	 * <p>删除Container或者Object</p>
	 * --------------------
	 * @author zhouhaitao
	 * 
	 * @param dcId				数据中心ID
	 * @param prjId				项目ID
	 * @param container			Container名称或者Object名称
	 */
	public void deleteContainer(String dcId,String prjId,String container);
}
