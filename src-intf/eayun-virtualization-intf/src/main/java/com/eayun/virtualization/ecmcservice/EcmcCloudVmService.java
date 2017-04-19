package com.eayun.virtualization.ecmcservice;

import java.util.List;
import java.util.Map;

import com.eayun.common.dao.QueryMap;
import com.eayun.common.dao.support.Page;
import com.eayun.common.exception.AppException;
import com.eayun.ecmcuser.model.BaseEcmcSysUser;
import com.eayun.sys.model.SysDataTree;
import com.eayun.virtualization.model.BaseCloudVm;
import com.eayun.virtualization.model.CloudImage;
import com.eayun.virtualization.model.CloudProject;
import com.eayun.virtualization.model.CloudSecurityGroup;
import com.eayun.virtualization.model.CloudSubNetWork;
import com.eayun.virtualization.model.CloudVm;

public interface EcmcCloudVmService {
	/**
	 * 打开云主机控制台
	 * @Author: duanbinbin
	 * @param cloudVm
	 * @return
	 * @throws AppException
	 *<li>Date: 2016年4月27日</li>
	 */
	public String ConsoleUrl(CloudVm cloudVm) throws AppException;

	/**
	 * 升级配置
	 * @Author: duanbinbin
	 * @param cloudVm
	 * @throws AppException
	 *<li>Date: 2016年4月27日</li>
	 */
	public void upgradeVm(CloudVm cloudVm) throws AppException;

	/**
	 * 关闭云主机
	 * @Author: duanbinbin
	 * @param cloudVm
	 * @throws AppException
	 *<li>Date: 2016年4月27日</li>
	 */
	public void shutdownVm(CloudVm cloudVm) throws AppException;

	/**
	 * 启动云主机
	 * @Author: duanbinbin
	 * @param cloudVm
	 * @throws AppException
	 *<li>Date: 2016年4月27日</li>
	 */
	public void startVm(CloudVm cloudVm) throws AppException;

	/**
	 * 重启云主机
	 * @Author: duanbinbin
	 * @param cloudVm
	 * @throws AppException
	 *<li>Date: 2016年4月27日</li>
	 */
	public void restartVm(CloudVm cloudVm) throws AppException;

	/**
	 * 挂起云主机
	 * @Author: duanbinbin
	 * @param cloudVm
	 * @throws AppException
	 *<li>Date: 2016年4月27日</li>
	 */
	public void suspendVm(CloudVm cloudVm) throws AppException;

	/**
	 * 恢复云主机
	 * @Author: duanbinbin
	 * @param cloudVm
	 * @throws AppException
	 *<li>Date: 2016年4月27日</li>
	 */
	public void resumeVm(CloudVm cloudVm) throws AppException;

	/**
	 * 创建自定义镜像
	 * @Author: duanbinbin
	 * @param sysUser
	 * @param cloudVm
	 * @throws AppException
	 *<li>Date: 2016年4月27日</li>
	 */
	public void createSnapshot(BaseEcmcSysUser sysUser, CloudVm cloudVm) throws AppException;

	/**
	 * 删除云主机
	 * @Author: duanbinbin
	 * @param sysUser
	 * @param cloudVm
	 * @throws AppException
	 *<li>Date: 2016年4月27日</li>
	 * @throws Exception 
	 */
	public void deleteVm(BaseEcmcSysUser sysUser, CloudVm cloudVm) throws AppException, Exception;

	/**
	 * 批量挂载云硬盘
	 * @Author: duanbinbin
	 * @param volList
	 * @return
	 * @throws AppException
	 *<li>Date: 2016年4月27日</li>
	 */
	public Map<String ,Object> bindBatchVolume(List<Map<String,String>> volList) throws AppException;

	/**
	 * 查询镜像列表
	 * @Author: duanbinbin
	 * @param cloudVm
	 * @return
	 *<li>Date: 2016年4月27日</li>
	 */
	public List<CloudImage> getImageList(CloudVm cloudVm);

	/**
	 * 获取云主机日志
	 * @Author: duanbinbin
	 * @param cloudVm
	 * @return
	 *<li>Date: 2016年4月27日</li>
	 */
	public String getVmLog(CloudVm cloudVm);

	/**
	 * 查询项目下子网列表
	 * @Author: duanbinbin
	 * @param prjId
	 * @return
	 *<li>Date: 2016年4月27日</li>
	 */
	public List<CloudSubNetWork> getSubNetListByPrjId(String prjId);

	/**
	 * 查看云主机详情
	 * @Author: duanbinbin
	 * @param vmId
	 * @return
	 *<li>Date: 2016年4月27日</li>
	 */
	public CloudVm getVmById(String vmId);

	/**
	 * 查询云主机列表
	 * @Author: duanbinbin
	 * @param page
	 * @param queryMap
	 * @param dcId
	 * @param vmStatus
	 * @param sysType
	 * @param timesort
	 * @param queryType
	 * @param queryName
	 * @return
	 *<li>Date: 2016年4月27日</li>
	 */
	public Page getVmPage(Page page, QueryMap queryMap, String dcId,
			String vmStatus, String sysType, String timesort, String queryType,
			String queryName);
	
	/**
	 * 查询回收站云主机列表
	 * @param page
	 * @param queryMap
	 * @param dcId
	 * @param vmStatus
	 * @param queryType
	 * @param queryName
	 * @return
	 */
	public Page getRecycleVmPage(Page page, QueryMap queryMap, String dcId,
			String vmStatus, String queryType,
			String queryName);

	/**
	 * 编辑云主机
	 * @Author: duanbinbin
	 * @param cloudVm
	 * @throws AppException
	 *<li>Date: 2016年4月27日</li>
	 */
	public void updateVm(CloudVm cloudVm) throws AppException;

	/**
	 * 编辑云主机安全组
	 * @Author: duanbinbin
	 * @param cloudVm
	 * @throws AppException
	 *<li>Date: 2016年4月27日</li>
	 */
	public void editVmSecurityGroup(CloudVm cloudVm) throws AppException;

	/**
	 * 查询没有关联该云主机的安全组
	 * @Author: duanbinbin
	 * @param vmId
	 * @param prjId
	 * @return
	 *<li>Date: 2016年4月27日</li>
	 */
	public List<CloudSecurityGroup> getSecurityByPrjNoVm(String vmId,
			String prjId);

	/**
	 * 查询关联了该云主机的安全组列表
	 * @Author: duanbinbin
	 * @param vmId
	 * @param prjId
	 * @return
	 *<li>Date: 2016年4月27日</li>
	 */
	public List<CloudSecurityGroup> getSecurityGroupByVm(String vmId,
			String prjId);

	/**
	 * 查询所有云主机状态
	 * @Author: duanbinbin
	 * @return
	 *<li>Date: 2016年4月27日</li>
	 */
	public List<SysDataTree> getVmStatusList();

	/**
	 * 查询出所有直接用于创建云主机的操作系统类型列表
	 * @Author: duanbinbin
	 * @return
	 *<li>Date: 2016年4月27日</li>
	 */
	public List<SysDataTree> getAllVmSysList();

	/**
	 * 查询某一系统类型下的所有操作系统列表，用于创建云主机使用
	 * @Author: duanbinbin
	 * @param osId
	 * @return
	 *<li>Date: 2016年4月27日</li>
	 */
	public List<SysDataTree> getSysTypeListByOs(String osId);

	/**
	 * 获取系统类型（Linux、Windows、其他）
	 * @Author: duanbinbin
	 * @return
	 *<li>Date: 2016年4月27日</li>
	 */
	public List<SysDataTree> getOsList();

	/**
	 * 获取CPU配置信息
	 * @Author: duanbinbin
	 * @return
	 *<li>Date: 2016年4月27日</li>
	 */
	public List<SysDataTree> getCpuList();

	/**
	 * 根据CPU配置获取内存配置信息
	 * @Author: duanbinbin
	 * @param cpuId
	 * @return
	 *<li>Date: 2016年4月27日</li>
	 */
	public List<SysDataTree> getRamListByCpu(String cpuId);

	/**
	 * 校验项目下云主机名称重名
	 * @Author: duanbinbin
	 * @param cloudVm
	 * @return
	 *<li>Date: 2016年4月27日</li>
	 */
	public boolean checkVmName(CloudVm cloudVm);

	/**
	 * 查询当前云主机已经挂载的云硬盘数
	 * @Author: duanbinbin
	 * @param vmId
	 * @return
	 * @throws Exception
	 *<li>Date: 2016年4月27日</li>
	 */
	public int getDiskCountByVm(String vmId) throws Exception;

	/**
	 * 云主机解绑弹性公网Ip
	 * @Author: duanbinbin
	 * @param cloudVm
	 *<li>Date: 2016年4月27日</li>
	 */
	public void unBindIpByVmId(CloudVm cloudVm) throws AppException;

	public List<CloudVm> getCanBindCloudVmList(String prjId);

	public List<CloudProject> getproListByDcId(String dcId);

	int getUnDeletedVmCountByProject(String prjId);
	/**
	 * 查询客户当前项目下的所有运行中的主机
	 * @return
	 */
	public List<CloudVm> getVmListByPrjIdAndVmStatus(String prjId,String vmStatus);
	/**
	 * 修改云主机，use by liyanchao（冻结客户时调用保存云主机状态）
	 * @return
	 */
	public BaseCloudVm mergeBaseVm(BaseCloudVm vm);
	/**
	 * 查找云主机，use by liyanchao（根据id找baseVm）
	 * @return
	 */
	public BaseCloudVm findBaseVmByVmId(String vmId);
	 /**
     * 获得状态为未删除且非active的云主机list
     * @author liyanchao
     * @return vmList
     */
    public List<CloudVm> getNoActiveUnDeletedVmByPrjId(String prjId , String vmStatus);
    
    /**
     * 查询网络下的子网
     * @param subnet
     * @return
     */
    public List<CloudSubNetWork> querySubnetByNet(CloudSubNetWork subnet);
    
    /**
	 * 云主机修改子网
	 * 
	 * @author zhouhaitao
	 * @param cloudVm
	 * 				主机信息
	 */
	public void modifySubnet(CloudVm cloudVm);
	
	/**
	 * 校验当前主机的受管子网是否可以切换
	 * <p>1. 没有绑定公网IP</p>
	 * <p>2. 没有关联负载均衡器的成员</p>
	 * <p>3. 没有作为端口映射的对应</p>
	 * <p>满足以上3点，返回false;否则 返回 true</p>
	 * ---------------------------------------
	 * @author zhouhaitao
	 * @param vm
	 * @return
	 */
	public boolean checkVmIpUsed (CloudVm vm);
	/**
	 * 查询回收站云主机的信息
	 * @param vmId
	 * @return
	 */
	public CloudVm getRecycleVmById(String vmId);
	
	/**
	 * 云主机状态同步
	 * @param vmId
	 */
	public void refreshStatus(String vmId)throws Exception;
	
	/**
	 * 查询云主机
	 * @param vmId
	 */
	public CloudVm get(String vmId);
	
	/**
	 * 查询指定镜像创建的云主机个数
	 * @param imageId
	 * @return
	 */
	public int countVmByImageId(String imageId);
}
