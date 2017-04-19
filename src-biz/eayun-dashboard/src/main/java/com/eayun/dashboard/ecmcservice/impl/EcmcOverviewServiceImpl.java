package com.eayun.dashboard.ecmcservice.impl;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.Query;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSONObject;
import com.eayun.common.RedisNodeIdConstant;
import com.eayun.common.constant.RedisKey;
import com.eayun.common.constant.ResourceType;
import com.eayun.common.dao.ParamsMap;
import com.eayun.common.dao.QueryMap;
import com.eayun.common.dao.support.Page;
import com.eayun.common.redis.JedisUtil;
import com.eayun.common.tools.DictUtil;
import com.eayun.common.util.BeanUtils;
import com.eayun.common.util.DateUtil;
import com.eayun.common.util.StringUtil;
import com.eayun.customer.ecmcservice.EcmcCustomerService;
import com.eayun.customer.model.BaseCustomer;
import com.eayun.customer.model.Customer;
import com.eayun.customer.serivce.CustomerService;
import com.eayun.dashboard.dao.OverviewIncomeChartDao;
import com.eayun.dashboard.dao.OverviewIncomeDataDao;
import com.eayun.dashboard.ecmcservice.EcmcOverviewService;
import com.eayun.dashboard.model.BaseOverviewIncomeChart;
import com.eayun.dashboard.model.BaseOverviewIncomeData;
import com.eayun.dashboard.model.OverviewIncomeChart;
import com.eayun.dashboard.model.OverviewIncomeData;
import com.eayun.database.instance.service.EcmcCloudRDSInstanceService;
import com.eayun.datacenter.ecmcservice.EcmcDataCenterService;
import com.eayun.datacenter.model.BaseDcDataCenter;
import com.eayun.datacenter.model.DcDataCenter;
import com.eayun.order.ecmcservice.EcmcOrderService;
import com.eayun.project.service.ProjectService;
import com.eayun.sys.model.SysDataTree;
import com.eayun.virtualization.model.CloudProject;
import com.eayun.virtualization.model.CloudProjectType;

@Service
public class EcmcOverviewServiceImpl implements EcmcOverviewService {

	@Autowired
	private ProjectService projectService;
	@Autowired
	private CustomerService customerService;
	@Autowired
	private EcmcDataCenterService ecmcDataCenterService;
	@Autowired
	private JedisUtil jedisUtil;
	@Autowired
	private EcmcCloudRDSInstanceService ecmcCloudRDSInstanceService;
	@Autowired
	private OverviewIncomeDataDao incomeDataDao;
	@Autowired
	private OverviewIncomeChartDao incomeChartDao;
	@Autowired
	private EcmcOrderService ecmcOrderService;
	@Autowired
	private EcmcCustomerService ecmcCustomerService;
	
	/**
	 * 图表类型
	 * @author bo.zeng@eayun.com
	 *
	 */
	protected static class ChartType{
		private final static String INCOME = "0";    //收入金额类型图表
		private final static String CONSUME_AS_NEEDED = "1";    //按需消费类型图表
	}
	
	/**
	 * 统计周期类型
	 * @author bo.zeng@eayun.com
	 *
	 */
	protected static class PeriodType{
		private final static String TOTAL = "0";    //全部
		private final static String YESTERDAY = "1";    //昨日
		private final static String NEARLY_SEVEN_DAYS = "2";    //近7日
		private final static String NEARLY_THIRTY_DAYS = "3";    //近30日
		private final static String NEARLY_NINETY_DAYS = "4";    //近90日
		private final static String YEAR = "5";    //年
		
		public static List<String> getAllTypeValues(){
			List<String> values = new ArrayList<String>();
			try {
				Class clazz = PeriodType.class;
				Field[] fields = clazz.getDeclaredFields();
				if (fields.length > 0) {
					for (Field field : fields) {
						String fieldValue = (String)field.get(null);
						values.add(fieldValue);
					}
				}
			} catch (IllegalArgumentException e) {
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				e.printStackTrace();
			}
			return values;
		}
	}

	/**
	 * 查询总览页面的资源类型列表
	 * 
	 * @author zhouhaitao
	 * @version 1.0
	 * @date 2016-03-28
	 */
	public List<SysDataTree> getResourceTypeList() {
		return DictUtil.getDataTreeByParentId(RedisNodeIdConstant.DC_RESOURCE_TYPE_NODEID);
	}

	
	/**
	 * 查询总览页面的数据中心下的资源总览
	 * 
	 * @author zhouhaitao
	 * @version 1.0
	 * @date 2016-03-28
	 * @param resourceType 排序的资源类型
	 * @param sortType 排序规则
	 */
	public List<DcDataCenter> getDcResourceList(String resourceType, String sortType) {
		List<DcDataCenter> list = new ArrayList<DcDataCenter>();
		StringBuffer sql = new StringBuffer();
		String sort = "asc";
		if (StringUtil.isEmpty(resourceType)) {
			resourceType = "dc_name";
		}
		if (null != sortType && sortType.equals("up")) {
			sort = "asc";
		} else if (null != sortType && sortType.equals("down")) {
			sort = "desc";
		}
		sql.append("SELECT                                                       ");
		sql.append("	dcd.id,                                                  ");
		sql.append("	dcd.dc_name,                                             ");
		sql.append("	cp.prjCount,                                             ");
		sql.append("	cp.bandWidthQuato,                                       ");
		sql.append("	cv.vmCountQuato,                                         ");
		sql.append("	cvo.volumeCountQuato,                                    ");
		sql.append("	dcserver.cpuQuato,                                       ");
		sql.append("	dcserver.memoryQuato,                                    ");
		sql.append("	dcserver.dataCapacityQuato,                              ");
		sql.append("	vmcpu.cpuQuatoUsed,                                      ");
		sql.append("	vmcpu.memoryUsed,                                        ");
		sql.append("	route.routeUsed,                                         ");
		sql.append("	cdsp.volSnapshotCountQuato,                              ");
		sql.append("	cdsp.usedVolSnapshotSum,                                 ");
		sql.append("	cn.networkCountQuato,                                    ");
		sql.append("	csn.subnetCountQuato,                                    ");
		sql.append("	csg.safeGroupCountQuato,                                 ");
		sql.append("	cdip.floatIpCountQuato,                                  ");
		sql.append("	cldp.poolCountQuato,                                     ");
		sql.append("	dcserver.serverCount,                                    ");
		sql.append("	cvo.usedVolumeCapacity,                                  ");
		sql.append("	ci.imageCount,                                           ");
		sql.append("	cf2.usedFloatIpCount,                                    ");
		sql.append("	cf1.allotFloatIpCount,                                   ");
		sql.append("	cdip.usedDHCP,                                           ");
		sql.append("	cdip.usedRoute,                                          ");
		sql.append("	clrdsi.rdsInstanceCount                                  ");
		sql.append("FROM                                                         ");
		sql.append("	dc_datacenter dcd                                        ");
		sql.append("LEFT JOIN (                                                  ");
		sql.append("	SELECT                                                   ");
		sql.append("		count(*) AS prjCount,                                ");
		sql.append("		sum(p.count_band) AS bandWidthQuato,                 ");
		sql.append("		p.dc_id                                              ");
		sql.append("	FROM                                                     ");
		sql.append("		cloud_project p                                      ");
		sql.append("	GROUP BY                                                 ");
		sql.append("		p.dc_id                                              ");
		sql.append(") cp ON cp.dc_id = dcd.id                                    ");
		sql.append("LEFT JOIN (                                                  ");
		sql.append("	SELECT                                                   ");
		sql.append("		ds.datacenter_id AS dc_id,                           ");
		sql.append("		count(1) AS serverCount,                             ");
		sql.append("		sum(dsm.cpu) AS cpuQuato,                            ");
		sql.append("		sum(dsm.memory) AS memoryQuato,                      ");
		sql.append("		sum(dsm.disk) AS dataCapacityQuato                   ");
		sql.append("	FROM                                                     ");
		sql.append("		dc_server ds                                         ");
		sql.append("	LEFT JOIN dc_server_model dsm                            ");
		sql.append("		ON ds.server_model_id = dsm.id                       ");
		sql.append("	WHERE    is_computenode = '0'                            ");
		sql.append("	GROUP BY                                                 ");
		sql.append("		datacenter_id                                        ");
		sql.append(") dcserver ON dcserver.dc_id = dcd.id                        ");
		///////////////////
		sql.append("LEFT JOIN (                                                  ");
		sql.append("	SELECT                                                   ");
		sql.append("		vm.dc_id AS dc_id,                	                 ");
		sql.append("		sum(cf.flavor_vcpus) AS cpuQuatoUsed,                ");
		sql.append("		sum(cf.flavor_ram) AS memoryUsed                     ");
		sql.append("	FROM                                                     ");
		sql.append("		cloud_vm vm                                          ");
		sql.append("	LEFT JOIN cloud_flavor cf                                ");
		sql.append("		ON cf.flavor_id = vm.flavor_id                       ");
		sql.append("		AND cf.dc_id = vm.dc_id                              ");
		sql.append("	WHERE    vm.is_deleted <> '1'                            ");
		sql.append("	GROUP BY                                                 ");
		sql.append("		vm.dc_id                                             ");
		sql.append(") vmcpu ON vmcpu.dc_id = dcd.id                              ");
		sql.append("LEFT JOIN (                                                  ");
		sql.append("	SELECT                                                   ");
		sql.append("		cr.dc_id AS dc_id,                	                 ");
		sql.append("		sum(cr.rate) AS routeUsed                            ");
		sql.append("	FROM                                                     ");
		sql.append("		cloud_route cr                                       ");
		sql.append("	GROUP BY                                                 ");
		sql.append("		cr.dc_id                                             ");
		sql.append(") route ON route.dc_id = dcd.id                              ");
		///////////////////
		sql.append("LEFT JOIN (                                                  ");
		sql.append("	SELECT                                                   ");
		sql.append("		cvm.dc_id,                                           ");
		sql.append("		count(1) AS vmCountQuato                             ");
		sql.append("	FROM                                                     ");
		sql.append("		cloud_vm cvm                                         ");
		sql.append("	WHERE                                                    ");
		sql.append("		(cvm.is_deleted = '0' or cvm.is_deleted = '2')       ");
		sql.append("	GROUP BY                                                 ");
		sql.append("		cvm.dc_id                                            ");
		sql.append(") cv ON cv.dc_id = dcd.id                                    ");
		sql.append("LEFT JOIN (                                                  ");
		sql.append("	SELECT                                                   ");
		sql.append("		cv.dc_id,                                            ");
		sql.append("		count(1) AS volumeCountQuato,                        ");
		sql.append("		sum(cv.vol_size) AS usedVolumeCapacity               ");
		sql.append("	FROM                                                     ");
		sql.append("		cloud_volume cv                                      ");
		sql.append("	WHERE                                                    ");
		sql.append("		(cv.is_deleted = '0' or cv.is_deleted = '2')         ");
		sql.append("	GROUP BY                                                 ");
		sql.append("		cv.dc_id                                             ");
		sql.append(") cvo ON cvo.dc_id = dcd.id                                  ");
		sql.append("LEFT JOIN (                                                  ");
		sql.append("	SELECT                                                   "); 
		sql.append("		dc_id,                                               ");
		sql.append("		count(*) AS imageCount                               ");
		sql.append("	FROM                                                     ");
		sql.append("		cloud_image                                          ");
		sql.append("where image_ispublic='2'                                     ");
		sql.append("	GROUP BY                                                 ");
		sql.append("		dc_id                                                ");
		sql.append(") ci ON ci.dc_id = dcd.id                                    ");
		sql.append("LEFT JOIN (                                                  ");
		sql.append("	SELECT                                                   ");
		sql.append("		dc_id,                                               ");
		sql.append("		count(*) AS allotFloatIpCount                        ");
		sql.append("	FROM                                                     ");
		sql.append("		cloud_floatip                                        ");
		sql.append("	where is_deleted ='0'                                    ");
		sql.append("	GROUP BY                                                 ");
		sql.append("		dc_id                                                ");
		sql.append(") cf1 ON cf1.dc_id = dcd.id                                  ");
		sql.append("LEFT JOIN (                                                  ");
		sql.append("	SELECT                                                   ");
		sql.append("		dc_id,                                               ");
		sql.append("		count(*) AS  usedFloatIpCount                        ");
		sql.append("	FROM                                                     ");
		sql.append("		cloud_floatip                                        ");
		sql.append("	where (resource_id is not null or resource_id <> '')     ");
		sql.append("	and is_deleted ='0'                                      ");
		sql.append("	GROUP BY                                                 ");
		sql.append("		dc_id                                                ");
		sql.append(") cf2 ON cf2.dc_id = dcd.id                                  ");
		sql.append("LEFT JOIN (                                                  ");
		sql.append("	SELECT                                                   ");
		sql.append("		dc_id,                                               ");
		sql.append("		count(1) AS networkCountQuato                        ");
		sql.append("	FROM                                                     ");
		sql.append("		cloud_network                                        ");
		sql.append("	where router_external = '0'                              ");
		sql.append("	GROUP BY                                                 ");
		sql.append("		dc_id                                                ");
		sql.append(") cn ON cn.dc_id = dcd.id                                    ");
		sql.append("LEFT JOIN (                                                  ");
		sql.append("	SELECT                                                   ");
		sql.append("		dc_id,                                               ");
		sql.append("		count(1) AS subnetCountQuato                         ");
		sql.append("	FROM                                                     ");
		sql.append("		cloud_subnetwork                                     ");
		sql.append("	where net_id not in                                      ");
		sql.append("	(select net_id from cloud_network where router_external = '1') ");
		sql.append("	GROUP BY                                                 ");
		sql.append("		dc_id                                                ");
		sql.append(") csn ON csn.dc_id = dcd.id                                  ");
		sql.append("LEFT JOIN (                                                  ");
		sql.append("	SELECT                                                   ");
		sql.append("		dc_id,                                               ");
		sql.append("		count(1) AS safeGroupCountQuato                      ");
		sql.append("	FROM                                                     ");
		sql.append("		cloud_securitygroup                                  ");
		sql.append("	GROUP BY                                                 ");
		sql.append("		dc_id                                                ");
		sql.append(") csg ON csg.dc_id = dcd.id                                  ");
		sql.append("LEFT JOIN (                                                  ");
		sql.append("	SELECT                                                   ");
		sql.append("		dc_id,                                               ");
		sql.append("		count(1) AS poolCountQuato                           ");
		sql.append("	FROM                                                     ");
		sql.append("		cloud_ldpool                                         ");
		sql.append("	GROUP BY                                                 ");
		sql.append("		dc_id                                                ");
		sql.append(") cldp ON cldp.dc_id = dcd.id                                ");
		sql.append("LEFT JOIN (                                                  ");
		sql.append("	SELECT                                                   ");
		sql.append("		cld.dc_id,                                           ");
		sql.append("		count(1) AS volSnapshotCountQuato,                   ");
		sql.append("		sum(cld.snap_size) AS usedVolSnapshotSum             ");
		sql.append("	FROM                                                     ");
		sql.append("		cloud_disksnapshot cld                               ");
		sql.append("		where (cld.is_deleted = '0' or cld.is_deleted = '2') ");
		sql.append("	GROUP BY                                                 ");
		sql.append("		dc_id                                                ");
		sql.append(") cdsp ON cdsp.dc_id = dcd.id                                ");
		sql.append("LEFT JOIN (                                                  ");
		sql.append("	SELECT                                                   ");
		sql.append("		outip.dc_id,                                         ");
		sql.append("		count(1) AS floatIpCountQuato,                       ");
		sql.append("		sum(case when outip.used_type<>'' and outip.used_type is not null then 1 else 0 end) AS usedDHCP,        ");
		sql.append("		sum(case when route.route_id<>'' and route.route_id is not null then 1 else 0 end) AS usedRoute        ");
		sql.append("	FROM                                                     ");
		sql.append("		cloud_outip outip                                    ");
		sql.append(" left join cloud_route route on route.gateway_ip=outip.ip_address and route.dc_id=outip.dc_id and route.net_id=outip.net_id ");
		sql.append(" left join cloud_floatip floatip on outip.ip_address = floatip.flo_ip   and outip.dc_id=floatip.dc_id and outip.net_id=floatip.net_id and floatip.is_deleted=0 ");
		sql.append("	GROUP BY                                                 ");
		sql.append("		dc_id                                                ");
		sql.append(") cdip ON cdip.dc_id = dcd.id                                ");
		sql.append("LEFT JOIN (                                                  ");
		sql.append("	SELECT                                                   ");
		sql.append("		dc_id,                                               ");
		sql.append("		count(1) AS rdsInstanceCount                         ");
		sql.append("	FROM                                                     ");
		sql.append("		cloud_rdsinstance                                    ");
		sql.append("	GROUP BY                                                 ");
		sql.append("		dc_id                                                ");
		sql.append(") clrdsi ON clrdsi.dc_id = dcd.id                            ");
		sql.append("order by " + resourceType + " " + sort + "                   ");

		Query query = projectService.createSQLNativeQuery(sql.toString(), new Object[] {});
		@SuppressWarnings({ "rawtypes" })
		List listResult = query.getResultList();
		DcDataCenter allDc = new DcDataCenter();
		allDc.setId("-1");
		allDc.setName("所有数据中心");
		for (int i = 0; i < listResult.size(); i++) {
			Object[] objs = (Object[]) listResult.get(i);
			int index = 0;
			DcDataCenter dc = new DcDataCenter();
			dc.setId(String.valueOf(objs[index++]));
			dc.setName(String.valueOf(objs[index++]));
			dc.setPrjCount(Integer.parseInt(objs[index++] != null ? String.valueOf(objs[index - 1]) : "0"));
			index++;
			dc.setBandWidthQuato(100);//网络带宽为100
			dc.setVmCountQuato(Integer.parseInt(objs[index++] != null ? String.valueOf(objs[index - 1]) : "0"));
			dc.setVolumeCountQuato(Integer.parseInt(objs[index++] != null ? String.valueOf(objs[index - 1]) : "0"));
			dc.setCpuQuato((int)Double.parseDouble(objs[index++] != null ? String.valueOf(objs[index - 1]) : "0"));
			dc.setMemoryQuato((int)Double.parseDouble(objs[index++] != null ? String.valueOf(objs[index - 1]) : "0"));
			dc.setDataCapacityQuato((int)Double.parseDouble(objs[index++] != null ? String.valueOf(objs[index - 1]) : "0"));
			dc.setUsedCpuCount((int)Double.parseDouble(objs[index++] != null ? String.valueOf(objs[index - 1]) : "0"));
			dc.setUsedMemoryCount((int)Double.parseDouble(objs[index++] != null ? String.valueOf(objs[index - 1]) : "0"));
			dc.setUsedRouteCount((int)Double.parseDouble(objs[index++] != null ? String.valueOf(objs[index - 1]) : "0"));
			dc.setVolSnapshotCountQuato(Integer.parseInt(objs[index++] != null ? String.valueOf(objs[index - 1]) : "0"));
			dc.setUsedVolSnapshotSum(Integer.parseInt(objs[index++] != null ? String.valueOf(objs[index - 1]) : "0"));
			dc.setNetworkCountQuato(Integer.parseInt(objs[index++] != null ? String.valueOf(objs[index - 1]) : "0"));
			dc.setSubnetCountQuato(Integer.parseInt(objs[index++] != null ? String.valueOf(objs[index - 1]) : "0"));
			dc.setSafeGroupCountQuato(Integer.parseInt(objs[index++] != null ? String.valueOf(objs[index - 1]) : "0"));
			dc.setFloatIpCountQuato(Integer.parseInt(objs[index++] != null ? String.valueOf(objs[index - 1]) : "0"));
			dc.setPoolCountQuato(Integer.parseInt(objs[index++] != null ? String.valueOf(objs[index - 1]) : "0"));
			dc.setServerCount(Integer.parseInt(objs[index++] != null ? String.valueOf(objs[index - 1]) : "0"));
			dc.setUsedVolumeCapacity(Integer.parseInt(objs[index++] != null ? String.valueOf(objs[index - 1]) : "0"));
			dc.setImageCount(Integer.parseInt(objs[index++] != null ? String.valueOf(objs[index - 1]) : "0"));
			dc.setUsedFloatIpCount(Integer.parseInt(objs[index++] != null ? String.valueOf(objs[index - 1]) : "0"));
			dc.setAllotFloatIpCount(Integer.parseInt(objs[index++] != null ? String.valueOf(objs[index - 1]) : "0"));
			dc.setUsedDHCP(Integer.parseInt(objs[index++] != null ? String.valueOf(objs[index - 1]) : "0"));
			dc.setUsedRoute(Integer.parseInt(objs[index++] != null ? String.valueOf(objs[index - 1]) : "0"));
			int rdsInstanceUsed = 0;
			Map<String,String> map = ecmcCloudRDSInstanceService.getRDSInstanceUsedInfoByDcId(dc.getId());
			if(null != map){
				rdsInstanceUsed = Integer.parseInt(null != map.get("Instances")?map.get("Instances"):"0");
				int rdsCPUUsed = Integer.parseInt(null != map.get("CPU")?map.get("CPU"):"0");
				int rdsRamUsed = Integer.parseInt(null != map.get("Ram")?map.get("Ram"):"0");
				int rdsVolumeUsed = Integer.parseInt(null != map.get("Volumes")?map.get("Volumes"):"0");
				
				dc.setVmCountQuato(dc.getVmCountQuato() + rdsInstanceUsed);
				dc.setVolumeCountQuato(dc.getVolumeCountQuato() + rdsInstanceUsed);
				
				dc.setUsedCpuCount(dc.getUsedCpuCount()+rdsCPUUsed);
				dc.setUsedMemoryCount(dc.getUsedMemoryCount() + rdsRamUsed);
				dc.setUsedVolumeCapacity(dc.getUsedVolumeCapacity() + rdsVolumeUsed);
			}
			dc.setUsedTotalRDSInstance(rdsInstanceUsed);

			allDc.setServerCount(dc.getServerCount() + allDc.getServerCount());
			allDc.setPrjCount(dc.getPrjCount() + allDc.getPrjCount());
			allDc.setImageCount(dc.getImageCount() + allDc.getImageCount());
			allDc.setVmCountQuato(dc.getVmCountQuato() + allDc.getVmCountQuato());
			allDc.setVolumeCountQuato(dc.getVolumeCountQuato() + allDc.getVolumeCountQuato());
			allDc.setDataCapacityQuato(dc.getDataCapacityQuato() + allDc.getDataCapacityQuato());
			allDc.setVolSnapshotCountQuato(dc.getVolSnapshotCountQuato() + allDc.getVolSnapshotCountQuato());
			allDc.setUsedVolSnapshotSum(dc.getUsedVolSnapshotSum() + allDc.getUsedVolSnapshotSum());
			allDc.setNetworkCountQuato(dc.getNetworkCountQuato() + allDc.getNetworkCountQuato());
			allDc.setSubnetCountQuato(dc.getSubnetCountQuato() + allDc.getSubnetCountQuato());
			allDc.setPoolCountQuato(dc.getPoolCountQuato() + allDc.getPoolCountQuato());
			allDc.setSafeGroupCountQuato(dc.getSafeGroupCountQuato() + allDc.getSafeGroupCountQuato());
			allDc.setFloatIpCountQuato(dc.getFloatIpCountQuato() + allDc.getFloatIpCountQuato());
			allDc.setCpuQuato(dc.getCpuQuato() + allDc.getCpuQuato());
			allDc.setMemoryQuato(dc.getMemoryQuato() + allDc.getMemoryQuato());
			allDc.setBandWidthQuato(dc.getBandWidthQuato() + allDc.getBandWidthQuato());
			allDc.setUsedCpuCount(dc.getUsedCpuCount() + allDc.getUsedCpuCount());
			allDc.setUsedMemoryCount(dc.getUsedMemoryCount() + allDc.getUsedMemoryCount());
			allDc.setUsedVolumeCapacity(dc.getUsedVolumeCapacity() + allDc.getUsedVolumeCapacity());
			allDc.setUsedFloatIpCount(dc.getUsedFloatIpCount() + allDc.getUsedFloatIpCount());
			allDc.setAllotFloatIpCount(dc.getAllotFloatIpCount() + allDc.getAllotFloatIpCount());
			allDc.setUsedRouteCount(dc.getUsedRouteCount() + allDc.getUsedRouteCount());
			allDc.setUsedRoute(dc.getUsedRoute() + allDc.getUsedRoute());
			allDc.setUsedDHCP(dc.getUsedDHCP() + allDc.getUsedDHCP());
			allDc.setUsedTotalRDSInstance(dc.getUsedTotalRDSInstance() + allDc.getUsedTotalRDSInstance());

			list.add(dc);
		}
		list.add(0, allDc);
		return list;
	}

	/**
	 * 查询总览页面的项目总览列表(分页)
	 * 
	 * @author zhouhaitao
	 * @version 1.0
	 * @date 2016-03-28
	 * @param prjName 项目名称
	 * @param prjs 多选的项目ID
	 * @param customers 多选的客户ID
	 * @throws Exception 
	 */
	@SuppressWarnings("unchecked")
	public Page getListPrjResource(Page page,ParamsMap map,QueryMap queryMap) throws Exception {
		String prjName = null;
		String prjsStr = null;
		String customersStr = null;
		int index = 0;
		Object[] args = new Object[10];
		
		if(null!=map&&null!=map.getParams()){
			prjName = (String)map.getParams().get("name");
			prjsStr = (String)map.getParams().get("prjs");
			customersStr = (String)map.getParams().get("customers");
		}
		
		StringBuffer sql = new StringBuffer();
		sql.append("SELECT                                                           ");
		sql.append("	cp.customer_id AS customerId,                                ");
		sql.append("	ssc.cus_org,                                                 ");
		sql.append("	cp.prj_id AS prjId,                                          ");
		sql.append("	cp.prj_name AS prjName,                                      ");
		sql.append("	cp.create_date ,                                             ");
		sql.append("	cp.host_count AS hostCount,                                  ");
		sql.append("	cp.memory AS memory,                                         ");
		sql.append("	cp.cpu_count AS cpuCount,                                    ");
		sql.append("	cp.disk_count AS volumeCount,                                ");
		sql.append("	cp.disk_snapshot AS diskSanpshotCount,                       ");
		sql.append("	cp.disk_capacity AS diskCapacity,                            ");
		sql.append("	cp.quota_pool AS poolCount,                                  ");
		sql.append("	cp.count_band AS countBand,                                  ");
		sql.append("	cp.net_work AS networkCount,                                 ");
		sql.append("	cp.subnet_count AS subnetCount,                              ");
		sql.append("	cp.outerip AS ipCount,                                       ");
		sql.append("	cp.safe_group AS safeGroupCount,                             ");
		sql.append("	cp.sms_count AS smsCount,                                    ");
		sql.append("	cp.max_masterinstance AS maxMasterInstance,                  ");
		sql.append("	cp.max_slaveofcluster AS MaxSlaveOfCulster,                  ");
		sql.append("	cv.usedCpu,                                                  ");
		sql.append("	cv.usedHostCount,                                            ");
		sql.append("	cv.usedMemory,                                               ");
		sql.append("	cvo.usedVolumeCount,                                         ");
		sql.append("	cvo.usedVolumeCapcity,                                       ");
		sql.append("	cd.usedSnapshotCount,                                        ");
		sql.append("	cd.usedSnapshotCapacity,                                     ");
		sql.append("	cn.usedNetworkCount,                                         ");
		sql.append("	cr.usedBandWidth,                                            ");
		sql.append("	cs.usedSubnetCount,                                          ");
		sql.append("	clp.usedPoolCount,                                           ");
		sql.append("	cse.usedSafeGroupCount,                                      ");
		sql.append("	cf.usedFloatIpCount                                          ");
		sql.append("FROM                                                             ");
		sql.append("	cloud_project cp                                             ");
		sql.append("LEFT JOIN sys_selfcustomer ssc ON ssc.cus_id = cp.customer_id    ");
		sql.append("LEFT JOIN (                                                      ");
		sql.append("	SELECT                                                       ");
		sql.append("		cv.prj_id,                                               ");
		sql.append("		count(*) AS usedHostCount,                               ");
		sql.append("		sum(cf.flavor_ram) AS usedMemory,                        ");
		sql.append("		sum(cf.flavor_vcpus) AS usedCpu                          ");
		sql.append("	FROM                                                         ");
		sql.append("		cloud_vm cv                                              ");
		sql.append("	LEFT JOIN cloud_flavor cf ON cv.flavor_id = cf.flavor_id     ");
		sql.append("	AND cv.dc_id = cf.dc_id                                      ");
		sql.append("	WHERE                                                        ");
		sql.append("		is_deleted = '0'                                         ");
		sql.append("	GROUP BY                                                     ");
		sql.append("		prj_id                                                   ");
		sql.append(") cv ON cv.prj_id = cp.prj_id                                    ");
		sql.append("LEFT JOIN (                                                      ");
		sql.append("	SELECT                                                       ");
		sql.append("		cv.prj_id,                                               ");
		sql.append("		count(*) AS usedVolumeCount,                             ");
		sql.append("		sum(cv.vol_size) AS usedVolumeCapcity                    ");
		sql.append("	FROM                                                         ");
		sql.append("		cloud_volume cv                                          ");
		sql.append("	WHERE                                                        ");
		sql.append("		(cv.is_deleted = '0' or cv.is_deleted = '2')             ");
		sql.append("	GROUP BY                                                     ");
		sql.append("		cv.prj_id                                                ");
		sql.append(") cvo ON cvo.prj_id = cp.prj_id                                  ");
		sql.append("LEFT JOIN (                                                      ");
		sql.append("	SELECT                                                       ");
		sql.append("		count(*) AS usedSnapshotCount,                           ");
		sql.append("		sum(snap_size) AS usedSnapshotCapacity,                  ");
		sql.append("		prj_id                                                   ");
		sql.append("	FROM                                                         ");
		sql.append("		cloud_disksnapshot                                       ");
		sql.append("	WHERE  is_deleted = '0' or is_deleted = '2'                  ");
		sql.append("	GROUP BY                                                     ");
		sql.append("		prj_id                                                   ");
		sql.append(") cd ON cd.prj_id = cp.prj_id                                    ");
		sql.append("LEFT JOIN (                                                      ");
		sql.append("	SELECT                                                       ");
		sql.append("		sum(rate) AS usedBandWidth,                              ");
		sql.append("		prj_id                                                   ");
		sql.append("	FROM                                                         ");
		sql.append("		cloud_route                                              ");
		sql.append("	GROUP BY                                                     ");
		sql.append("		prj_id                                                   ");
		sql.append(") cr ON cr.prj_id = cp.prj_id                                    ");
		sql.append("LEFT JOIN (                                                      ");
		sql.append("	SELECT                                                       ");
		sql.append("		count(*) AS usedNetworkCount,                            ");
		sql.append("		prj_id                                                   ");
		sql.append("	FROM                                                         ");
		sql.append("		cloud_network                                            ");
		sql.append("	GROUP BY                                                     ");
		sql.append("		prj_id                                                   ");
		sql.append(") cn ON cn.prj_id = cp.prj_id                                    ");
		sql.append("LEFT JOIN (                                                      ");
		sql.append("	SELECT                                                       ");
		sql.append("		count(*) AS usedSubnetCount,                             ");
		sql.append("		prj_id                                                   ");
		sql.append("	FROM                                                         ");
		sql.append("		cloud_subnetwork                                         ");
		sql.append("	GROUP BY                                                     ");
		sql.append("		prj_id                                                   ");
		sql.append(") cs ON cs.prj_id = cp.prj_id                                    ");
		sql.append("LEFT JOIN (                                                      ");
		sql.append("	SELECT                                                       ");
		sql.append("		count(*) AS usedPoolCount,                               ");
		sql.append("		prj_id                                                   ");
		sql.append("	FROM                                                         ");
		sql.append("		cloud_ldpool                                             ");
		sql.append("	GROUP BY                                                     ");
		sql.append("		prj_id                                                   ");
		sql.append(") clp ON clp.prj_id = cp.prj_id                                  ");
		sql.append("LEFT JOIN (                                                      ");
		sql.append("	SELECT                                                       ");
		sql.append("		count(*) AS usedFloatIpCount,                            ");
		sql.append("		prj_id                                                   ");
		sql.append("	FROM                                                         ");
		sql.append("		cloud_floatip                                            ");
		sql.append("	where (resource_id is not null or resource_id <> '' )        ");
		sql.append("	GROUP BY                                                     ");
		sql.append("		prj_id                                                   ");
		sql.append(") cf ON cf.prj_id = cp.prj_id                                    ");
		sql.append("LEFT JOIN (                                                      ");
		sql.append("	SELECT                                                       ");
		sql.append("		count(*) AS usedSafeGroupCount,                          ");
		sql.append("		prj_id                                                   ");
		sql.append("	FROM                                                         ");
		sql.append("		cloud_securitygroup                                      ");
		sql.append("	GROUP BY                                                     ");
		sql.append("		prj_id                                                   ");
		sql.append(" ) cse ON cse.prj_id = cp.prj_id                                 ");
		sql.append(" WHERE  1=1                                                      ");
		if(!StringUtils.isEmpty(prjName)){
			prjName = prjName.replaceAll("\\_", "\\\\_");
			sql.append(" AND	cp.prj_name LIKE ?").append(index+1);
			args[index++] = "%"+prjName+"%";
		}
		if(!StringUtils.isEmpty(prjsStr)){
			sql.append(" AND cp.prj_name IN (?").append(index+1).append(")");
			args[index++] = Arrays.asList(StringUtils.split(prjsStr, ","));
		}
		if(!StringUtils.isEmpty(customersStr)){
			sql.append(" AND ssc.cus_org IN (?").append(index+1).append(")");
			args[index++] = Arrays.asList(StringUtils.split(customersStr, ","));
		}
		sql.append(" ORDER BY                                                         ");
		sql.append("	cp.prj_name DESC                                             ");

		Object[] params = new Object[index];
		System.arraycopy(args, 0, params, 0, index);
		
		page = projectService.pagedNativeQuery(sql.toString(), queryMap, params);
		@SuppressWarnings({ "rawtypes" })
		List listResult = (List)page.getResult();

		for (int i = 0; i < listResult.size(); i++) {
			Object[] objs = (Object[]) listResult.get(i);
			int resIndex = 0;
			CloudProject project = new CloudProject();
			
			project.setCustomerId(String.valueOf(objs[resIndex++]));
			project.setCusOrg(String.valueOf(objs[resIndex++]));
			project.setProjectId(String.valueOf(objs[resIndex++]));
			project.setPrjName(String.valueOf(objs[resIndex++]));
			project.setCreateTime(objs[resIndex++] != null ?((Timestamp)objs[resIndex-1]).getTime()+"":null);
			project.setHostCount(Integer.parseInt(objs[resIndex++] != null ? String.valueOf(objs[resIndex - 1]) : "0"));
			project.setMemory(Integer.parseInt(objs[resIndex++] != null ? String.valueOf(objs[resIndex - 1]) : "0"));
			project.setCpuCount(Integer.parseInt(objs[resIndex++] != null ? String.valueOf(objs[resIndex - 1]) : "0"));
			project.setDiskCount(Integer.parseInt(objs[resIndex++] != null ? String.valueOf(objs[resIndex - 1]) : "0"));
			project.setDiskSnapshot(Integer.parseInt(objs[resIndex++] != null ? String.valueOf(objs[resIndex - 1]) : "0"));
			project.setDiskCapacity(Integer.parseInt(objs[resIndex++] != null ? String.valueOf(objs[resIndex - 1]) : "0"));
			project.setQuotaPool(Integer.parseInt(objs[resIndex++] != null ? String.valueOf(objs[resIndex - 1]) : "0"));
			project.setCountBand(Integer.parseInt(objs[resIndex++] != null ? String.valueOf(objs[resIndex - 1]) : "0"));
			project.setNetWork(Integer.parseInt(objs[resIndex++] != null ? String.valueOf(objs[resIndex - 1]) : "0"));
			project.setSubnetCount(Integer.parseInt(objs[resIndex++] != null ? String.valueOf(objs[resIndex - 1]) : "0"));
			project.setOuterIP(Integer.parseInt(objs[resIndex++] != null ? String.valueOf(objs[resIndex - 1]) : "0"));
			project.setSafeGroup(Integer.parseInt(objs[resIndex++] != null ? String.valueOf(objs[resIndex - 1]) : "0"));
			project.setSmsCount(Integer.parseInt(objs[resIndex++] != null ? String.valueOf(objs[resIndex - 1]) : "0"));
			project.setMaxMasterInstance(Integer.parseInt(objs[resIndex++] != null ? String.valueOf(objs[resIndex - 1]) : "0"));
			project.setMaxSlaveIOfCluster(Integer.parseInt(objs[resIndex++] != null ? String.valueOf(objs[resIndex - 1]) : "0"));
			project.setUsedCpuCount(Integer.parseInt(objs[resIndex++] != null ? String.valueOf(objs[resIndex - 1]) : "0"));
			project.setUsedVmCount(Integer.parseInt(objs[resIndex++] != null ? String.valueOf(objs[resIndex - 1]) : "0"));
			project.setUsedMemory((Float.parseFloat(objs[resIndex++] != null ? String.valueOf(objs[resIndex - 1]) : "0"))/1024);
			project.setDiskCountUse(Integer.parseInt(objs[resIndex++] != null ? String.valueOf(objs[resIndex - 1]) : "0"));
			project.setUsedDataCapacity(Integer.parseInt(objs[resIndex++] != null ? String.valueOf(objs[resIndex - 1]) : "0"));
			project.setDiskSnapshotUse(Integer.parseInt(objs[resIndex++] != null ? String.valueOf(objs[resIndex - 1]) : "0"));
			project.setUsedSnapshotCapacity(Integer.parseInt(objs[resIndex++] != null ? String.valueOf(objs[resIndex - 1]) : "0"));
			project.setNetWorkUse(Integer.parseInt(objs[resIndex++] != null ? String.valueOf(objs[resIndex - 1]) : "0"));
			project.setCountBandUse(Integer.parseInt(objs[resIndex++] != null ? String.valueOf(objs[resIndex - 1]) : "0"));
			project.setSubnetCountUse(Integer.parseInt(objs[resIndex++] != null ? String.valueOf(objs[resIndex - 1]) : "0"));
			project.setUsedPool(Integer.parseInt(objs[resIndex++] != null ? String.valueOf(objs[resIndex - 1]) : "0"));
			project.setSafeGroupUse(Integer.parseInt(objs[resIndex++] != null ? String.valueOf(objs[resIndex - 1]) : "0"));
			project.setOuterIPUse(Integer.parseInt(objs[resIndex++] != null ? String.valueOf(objs[resIndex - 1]) : "0"));
//			project.setUsedDataCapacity(project.getUsedSnapshotCapacity()+project.getDiskCapacityUse());
			int usedSmsCount = 0;
			if(!StringUtils.isEmpty(project.getCustomerId())&&!StringUtils.isEmpty(project.getProjectId())){
				String usedStr = jedisUtil.get(RedisKey.SMS_QUOTA_SENT+project.getCustomerId()+":"+project.getProjectId());
				usedSmsCount = Integer.parseInt(usedStr!=null?usedStr:"0");
			}
			project.setUsedSmsCount(usedSmsCount);
			int totalInstanceCount = ecmcCloudRDSInstanceService.getRdsInstanceCountByPrjId(project.getProjectId());
			
			project.setTotalInstanceUse(totalInstanceCount);
			listResult.set(i,project);
		}
		return page;
	}

	/**
	 * 查询系统所有的客户列表
	 * 
	 * @author zhouhaitao
	 * @version 1.0
	 * @date 2016-03-28
	 */
	public List<BaseCustomer> getAllCustomerList(){
		return customerService.getAllCustomerList();
	}
	
	/**
	 * 查询系统所有的项目列表
	 * 
	 * @author zhouhaitao
	 * @version 1.0
	 * @date 2016-03-28
	 */
	public List<CloudProject> getAllProjectList(){
		return projectService.getAllProjects();
	}


	@Override
	public List<CloudProject> getprojectListByCusId(String cusId) {
		return projectService.getProjectListByCustomer(cusId);
	}


	@Override
	public List<DcDataCenter> getAlldcList() {
		List<DcDataCenter> dcList = ecmcDataCenterService.getAllList();
		return dcList;
	}

	/**
	 * 查询数据中心所有的项目列表
	 * @param dcId
	 * @return
     */
	@Override
	public List<CloudProject> getprojectListByDcId(String dcId) {
		BaseDcDataCenter dc = ecmcDataCenterService.getdatacenterbyid(dcId);
		return projectService.getProjectListByDataCenter(dc.getId());
	}

	/**
	 * 获取系统当前时间
	 * @param json
	 * @throws Exception
     */
	@Override
	public JSONObject getNowTime() throws Exception {
		JSONObject object = new JSONObject();
		try {
			long nowTime = new Date().getTime();
			object.put("nowTime" ,nowTime);
		} catch (Exception e) {
			throw e;
		}
		return object;
	}
	
	@Override
	public List<String> getYears() {
		StringBuffer sql = new StringBuffer();
		sql.append("SELECT date_format(cus.creat_time,'%Y') sdate FROM sys_selfcustomer cus  LEFT JOIN sys_selfuser AS ss ON ss.cus_id = cus.cus_id where cus.cus_falg = 1 AND ss.is_admin = 1 and cus.is_blocked is not null group by sdate ORDER BY sdate DESC");
		Query query = projectService.createSQLNativeQuery(sql.toString(), new Object[] {});
		@SuppressWarnings({ "rawtypes" })
		List listResult = query.getResultList();
		int listLength = listResult.size();
		List<String> ptype = new ArrayList<>();
		for (int i = 0; i < listLength; i++) {
			ptype.add(listResult.get(i).toString() != null ? String.valueOf(listResult.get(i).toString()) : "-");
		}
		return ptype;
	}
	
	/**
	 * 根据客户类型查询该类型下的所有项目数量
	 */
	@Override
	public CloudProjectType getAllProjectsType() {
		StringBuffer sql = new StringBuffer();
		sql.append("SELECT                                                                       ");
		sql.append("	count(cprj.prj_id) AS allPrjs,                                           ");
		sql.append("	sum(case when cus.cus_type=0 and cus.is_blocked=0 then 1 else 0 end) AS formalCusPrjs,        ");
		sql.append("	sum(case when cus.cus_type=1 and cus.is_blocked=0 then 1 else 0 end) AS cooperationCusPrjs,   ");
		sql.append("	sum(case when cus.cus_type=2 and cus.is_blocked=0 then 1 else 0 end) AS testCusPrjs,          ");
		sql.append("	sum(case when cus.cus_type=3 and cus.is_blocked=0 then 1 else 0 end) AS oneselfCusPrjs,       ");
		sql.append("	sum(case when cus.cus_type=4 and cus.is_blocked=0 then 1 else 0 end) AS otherCusPrjs,         ");
		sql.append("	sum(case when cus.is_blocked<>0 then 1 else 0 end) AS freezeCusPrjs      ");
		sql.append("FROM    cloud_project cprj                                                   ");
		sql.append("LEFT JOIN sys_selfcustomer cus ON cus.cus_id = cprj.customer_id              ");
		sql.append("   where cprj.customer_id is not null                                        ");
		
		Query query = projectService.createSQLNativeQuery(sql.toString(), new Object[] {});
		@SuppressWarnings({ "rawtypes" })
		List listResult = query.getResultList();
		Object[] objs = null;
		CloudProjectType ptype = null;
		int listLength = listResult.size();
		for (int i = 0; i < listLength; i++) {
			objs = (Object[]) listResult.get(i);
			ptype = new CloudProjectType();
			ptype.setAll_prjs(Integer.parseInt(objs[0] != null ? String.valueOf(objs[0]) : "0"));
			ptype.setFormal_cus_prjs(Integer.parseInt(objs[1] != null ? String.valueOf(objs[1]) : "0"));
			ptype.setCooperation_cus_prjs(Integer.parseInt(objs[2] != null ? String.valueOf(objs[2]) : "0"));
			ptype.setTest_cus_prjs(Integer.parseInt(objs[3] != null ? String.valueOf(objs[3]) : "0"));
			ptype.setOneself_cus_prjs(Integer.parseInt(objs[4] != null ? String.valueOf(objs[4]) : "0"));
			ptype.setOther_cus_prjs(Integer.parseInt(objs[5] != null ? String.valueOf(objs[5]) : "0"));
			ptype.setFreeze_cus_prjs(Integer.parseInt(objs[6] != null ? String.valueOf(objs[6]) : "0"));
		}
		return ptype;
	}
	/**
	 * 查询一年每月注册的用户数量
	 * @throws Exception 
	 */
	@Override
	public CloudProjectType getNowCusToMonths(String type) throws Exception {
		String starttime = null;
		String endtime = null;
		if("now".equals(type)){
			starttime = getLast12Months()[0].toString();
			endtime = getLast12Months()[getLast12Months().length-1].toString();
		}else{
			starttime = type+"-01";
			endtime = type+"-12";
		}
		
		StringBuffer sql = new StringBuffer();
		Object[] objst = new Object[] {starttime,endtime};
		sql.append("SELECT date_format(cus.creat_time,'%Y-%m') sdate,count(cus.cus_id) FROM sys_selfcustomer cus ");
		sql.append(" LEFT JOIN sys_selfuser AS ss ON ss.cus_id = cus.cus_id ");
		sql.append(" where date_format(cus.creat_time,'%Y-%m')>=? and date_format(cus.creat_time,'%Y-%m')<=? and cus.cus_falg = 1 AND ss.is_admin = 1 and cus.is_blocked is not null  ");
		sql.append(" group by sdate ORDER BY sdate ");
		Query query = projectService.createSQLNativeQuery(sql.toString(), objst);
		@SuppressWarnings({ "rawtypes" })
		List listResult = query.getResultList();
		Object[] objs = null;
		CloudProjectType ptype = new CloudProjectType();
		List<String> xData = new ArrayList<>();
		List<String> xTitle = new ArrayList<>();
		int listLength = listResult.size();
		for (int i = 0; i < listLength; i++) {
			objs = (Object[]) listResult.get(i);
			xTitle.add(objs[0] != null ? String.valueOf(objs[0]) : "-");
			xData.add(objs[1] != null ? String.valueOf(objs[1]) : "0");
		}
		List<Date> listdata = findDates(starttime, endtime);
		//为没有数据的月份填充0
		if(xTitle.size()<listdata.size()){
			List<String> nData = new ArrayList<>();
			List<String> nTitle = new ArrayList<>();
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM");  
			int j = 0;
			for(int i=0;i<listdata.size();i++){
				if(listdata.get(i).getTime()==sdf.parse(xTitle.get(j)).getTime()){
					nTitle.add(xTitle.get(j));
					nData.add(xData.get(j));
					if(j<xTitle.size()-1){
						j++;
					}
				}else{
					nTitle.add(sdf.format(listdata.get(i)));
					nData.add("0");
				}
			}
			ptype.setxTitle(nTitle);
			ptype.setxData(nData);
		}else{
			ptype.setxTitle(xTitle);
			ptype.setxData(xData);
		}
		return ptype;
	}
	private static List<Date> findDates(String start, String end) {  
		try{
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM");  
	        Date dBegin = sdf.parse(start);  
	        Date dEnd = sdf.parse(end);  
	        List lDate = new ArrayList();  
	        Calendar calBegin = Calendar.getInstance();  
	        // 使用给定的 Date 设置此 Calendar 的时间    
	        calBegin.setTime(dBegin);  
	        lDate.add(calBegin.getTime());
	        // 测试此日期是否在指定日期之后    
	        while (dEnd.after(calBegin.getTime())) { 
	        	calBegin.set(Calendar.MONTH, calBegin.get(Calendar.MONTH)+1);
	        	lDate.add(calBegin.getTime());  
	        }
	        return lDate;
		}catch(Exception e){
			return null;
		}
	}
    /** 
     * 获取最近12个月，经常用于统计图表的X轴 
     */  
    private static String[] getLast12Months(){  
          
        String[] last12Months = new String[12];  
        int m = 0 ;  
        String day = "01";
        Calendar cal = Calendar.getInstance();  
        cal.set(Calendar.MONTH, cal.get(Calendar.MONTH)+1); //要先+1,才能把本月的算进去</span>  
        for(int i=0; i<12; i++){  
            cal.set(Calendar.MONTH, cal.get(Calendar.MONTH)-1); //逐次往前推1个月  
            m = cal.get(Calendar.MONTH)+1;
            if(m<10){
            	day = "0"+m;
            }else{
            	day = m+"";
            }
            last12Months[11-i] = cal.get(Calendar.YEAR)+ "-" + day;  
        }  
          
        return last12Months;  
    }


	@Override
	public OverviewIncomeData getIncomeData(String periodType, String searchYear) {
		Date currDate = new Date();
		
		// 根据periodType计算开始时间和结束时间，用于统计收入数据（除图表数据外）
		String startDate = DateUtil.dateToStr(calculateStartOrEndTime(periodType, currDate, false));
		if (!StringUtils.equals(periodType, PeriodType.YEAR)) {
			periodType = PeriodType.YESTERDAY;
		}
		String endDate = DateUtil.dateToStr(calculateStartOrEndTime(periodType, currDate, true));
		
		OverviewIncomeData oid = getOverviewIncomeData(startDate, endDate);
		
		oid = addChartData(oid, periodType, currDate);
		
		return oid;
	}
	
	/**
	 * 查询收入统计中除图表外的数据
	 * @param startDate
	 * @param endDate
	 * @return
	 */
	protected OverviewIncomeData getOverviewIncomeData(String startDate, String endDate) {
		StringBuffer sql = new StringBuffer();
		List<String> sqlParams = new ArrayList<String>();
		sql.append("SELECT SUM(total_income), ");
		sql.append("SUM(total_order), ");
		sql.append("SUM(alipay_recharge), ");
		sql.append("SUM(alipay_buy), ");
		sql.append("SUM(ecmc_recharge), ");
		sql.append("SUM(vm_order), ");
		sql.append("SUM(vdisk_order), ");
		sql.append("SUM(disksnapshot_order), ");
		sql.append("SUM(network_order), ");
		sql.append("SUM(quotapool_order), ");
		sql.append("SUM(floatip_order), ");
		sql.append("SUM(vpn_order), ");
		sql.append("SUM(rds_order), ");
		sql.append("SUM(exceptional_order) ");
		sql.append("FROM overview_income_data WHERE data_time >= ? AND data_time <= ?; ");
		sqlParams.add(startDate);
		sqlParams.add(endDate);
		Object[] obj = (Object[]) incomeDataDao.createSQLNativeQuery(sql.toString(), sqlParams.toArray())
				.getSingleResult();
		OverviewIncomeData oid = new OverviewIncomeData();
		oid.setTotalIncome(new BigDecimal(ObjectUtils.toString(obj[0], "0.000")));
		oid.setTotalOrder(Integer.parseInt(ObjectUtils.toString(obj[1], "0")));
		oid.setAlipayRecharge(new BigDecimal(ObjectUtils.toString(obj[2], "0.000")));
		oid.setAlipayBuy(new BigDecimal(ObjectUtils.toString(obj[3], "0.000")));
		oid.setEcmcRecharge(new BigDecimal(ObjectUtils.toString(obj[4], "0.000")));
		oid.setVmOrder(Integer.parseInt(ObjectUtils.toString(obj[5], "0")));
		oid.setVdiskOrder(Integer.parseInt(ObjectUtils.toString(obj[6], "0")));
		oid.setDisksnapshotOrder(Integer.parseInt(ObjectUtils.toString(obj[7], "0")));
		oid.setNetworkOrder(Integer.parseInt(ObjectUtils.toString(obj[8], "0")));
		oid.setQuotapoolOrder(Integer.parseInt(ObjectUtils.toString(obj[9], "0")));
		oid.setFloatipOrder(Integer.parseInt(ObjectUtils.toString(obj[10], "0")));
		oid.setVpnOrder(Integer.parseInt(ObjectUtils.toString(obj[11], "0")));
		oid.setRdsOrder(Integer.parseInt(ObjectUtils.toString(obj[12], "0")));
		oid.setExceptionalOrder(Integer.parseInt(ObjectUtils.toString(obj[13], "0")));

		return oid;
	}
	
	/**
	 * 查询收入统计中的图表数据
	 * @param oid
	 * @param periodType
	 * @param currDate
	 * @return
	 */
	protected OverviewIncomeData addChartData(OverviewIncomeData oid, String periodType, Date currDate) {
		if (oid == null) {
			return null;
		}
		StringBuffer sql = new StringBuffer();
		List<String> sqlParams = new ArrayList<String>();
		sql.append(
				"SELECT c.id, c.cus_id, s.cus_org, c.money, c.statistic_type FROM overview_income_chart c LEFT JOIN sys_selfcustomer s ON c.cus_id = s.cus_id WHERE ");
		if (!StringUtils.equals(periodType, PeriodType.YEAR)) {
			sql.append("record_time = ? ");
			sqlParams.add(DateUtil.dateToStr(currDate));
		} else {
			sql.append("year = ? ");
			sqlParams.add(DateUtil.getYearString(DateUtil.addDay(currDate, new int[] { -1 })));
		}
		sql.append("ORDER BY c.sort;");
		List<Object[]> resultList = incomeChartDao.createSQLNativeQuery(sql.toString(), sqlParams.toArray())
				.getResultList();
		if (!CollectionUtils.isEmpty(resultList)) {
			for (Object[] object : resultList) {
				OverviewIncomeChart oic = new OverviewIncomeChart();
				oic.setId(ObjectUtils.toString(object[0], null));
				oic.setCusId(ObjectUtils.toString(object[1], null));
				oic.setCusOrg(ObjectUtils.toString(object[2], null));
				oic.setMoney(new BigDecimal(ObjectUtils.toString(object[3], "0.000")));
				oic.setStatisticType(ObjectUtils.toString(object[4], null));
				if (StringUtils.equals(oic.getStatisticType(), ChartType.INCOME)) {
					// 设置收入top数据
					oid.getCusIncomeTops().add(oic);
				} else if (StringUtils.equals(oic.getStatisticType(), ChartType.CONSUME_AS_NEEDED)) {
					// 设置按需消费top数据
					oid.getCusConsumeAsNeededTops().add(oic);
				}
			}
		}
		return oid;
	}


	@Override
	public void gatherOverviewIncomeData() {
		Date currDate = new Date();
		// 计算昨日开始时间和结束时间
		Date startTime = DateUtil.getNearlyDateTime(currDate, -1, Calendar.DATE, false);
		Date endTime = DateUtil.getNearlyDateTime(currDate, -1, Calendar.DATE, true);
		
		if (endTime != null && startTime != null) {
			//保存除图表外的其他收入统计数据
			BaseOverviewIncomeData oiData = new BaseOverviewIncomeData();
			// TODO 调用查询总收入接口
			// TODO 调用查询总订单接口
			int totalOrder = 0;
			oiData.setTotalOrder(totalOrder);
			// TODO 调用查询支付宝充值金额接口
			// TODO 调用查询支付宝付款金额接口
			// TODO 调用查询总收入接口
			// TODO 调用查询ECMC实际充值接口
			// TODO 调用查询资源订单量接口
			List<String> resourceTypes = ResourceType.getAllTypeValues();
			Map<String, Integer> resourceOrder = null;
			setResourceTypeOrderCount(oiData, resourceOrder);
			// TODO 调用查询异常订单量接口
			int exceptionalOrder = 0;
			oiData.setExceptionalOrder(exceptionalOrder);
			
			oiData.setDataTime(DateUtil.dateToStr(startTime));
			incomeDataDao.save(oiData);
		}
	}
	
	/**
	 * 计算统计开始时间或结束时间
	 * @param periodType 统计周期类型
	 * @param currDate 当前时间
	 * @param needEndTime 是否获取结束时间
	 */
	protected Date calculateStartOrEndTime(String periodType, Date currDate, boolean needEndTime) {
		if (StringUtils.equals(periodType, PeriodType.TOTAL) && needEndTime) {
			return DateUtil.getNearlyDateTime(currDate, -1, Calendar.DATE, needEndTime);
		} else if (StringUtils.equals(periodType, PeriodType.YESTERDAY)) {
			return DateUtil.getNearlyDateTime(currDate, -1, Calendar.DATE, needEndTime);
		} else if (StringUtils.equals(periodType, PeriodType.NEARLY_SEVEN_DAYS)) {
			return DateUtil.getNearlyDateTime(currDate, -7, Calendar.DATE, needEndTime);
		} else if (StringUtils.equals(periodType, PeriodType.NEARLY_THIRTY_DAYS)) {
			return DateUtil.getNearlyDateTime(currDate, -30, Calendar.DATE, needEndTime);
		} else if (StringUtils.equals(periodType, PeriodType.NEARLY_NINETY_DAYS)) {
			return DateUtil.getNearlyDateTime(currDate, -90, Calendar.DATE, needEndTime);
		} else if (StringUtils.equals(periodType, PeriodType.YEAR)) {
			return DateUtil.getNearlyDateTime(currDate, -1, Calendar.YEAR, needEndTime);
		}
		return null;
	}
	
	/**
	 * 设置各种资源订单量
	 * @param oiData
	 * @param resourceOrder
	 */
	protected void setResourceTypeOrderCount(BaseOverviewIncomeData oiData, Map<String, Integer> resourceOrder){
		oiData.setVmOrder(resourceOrder.get(ResourceType.VM));
		oiData.setVdiskOrder(resourceOrder.get(ResourceType.VDISK));
		oiData.setDisksnapshotOrder(resourceOrder.get(ResourceType.DISKSNAPSHOT));
		oiData.setNetworkOrder(resourceOrder.get(ResourceType.NETWORK));
		oiData.setQuotapoolOrder(resourceOrder.get(ResourceType.QUOTAPOOL));
		oiData.setFloatipOrder(resourceOrder.get(ResourceType.FLOATIP));
		oiData.setVpnOrder(resourceOrder.get(ResourceType.VPN));
		oiData.setRdsOrder(resourceOrder.get(ResourceType.RDS));
	}
	
	@Override
	public void gatherOverviewIncomeChart() {
		Date currDate = new Date();
		String lastYearStr = null;
		List<String> periodTypes = PeriodType.getAllTypeValues();
		if (!CollectionUtils.isEmpty(periodTypes)) {
			for (String periodType : periodTypes) {
				// 计算开始时间和结束时间
				Date startTime = calculateStartOrEndTime(periodType, currDate, false);
				if (!StringUtils.equals(periodType, PeriodType.YEAR)) {
					periodType = PeriodType.YESTERDAY;
				}
				Date endTime = calculateStartOrEndTime(periodType, currDate, true);

				if (StringUtils.equals(periodType, PeriodType.YEAR)) {
					// 如果统计年数据，则先判断数据是否存在，不存在才进行统计
					Date lastYear = DateUtil.addDay(currDate, new int[] { -1 });
					lastYearStr = DateUtil.getYearString(lastYear);
					List<BaseOverviewIncomeChart> baseCharts = incomeChartDao.findByYear(lastYearStr);
					if (!CollectionUtils.isEmpty(baseCharts)) {
						return;
					}
				}

				if (startTime != null && endTime != null) {
					OverviewIncomeChart oiChart = new OverviewIncomeChart();
					oiChart.setStartTime(startTime);
					oiChart.setEndTime(endTime);
					oiChart.setPeriodType(periodType);
					oiChart.setYear(lastYearStr);

					// TODO 查询收入金额前五名客户
					LinkedHashMap<String, BigDecimal> cusIncomeTops = new LinkedHashMap<String, BigDecimal>();
					saveChartData(cusIncomeTops, oiChart, ChartType.INCOME);

					// TODO 查询按需消费金额前五名客户
					LinkedHashMap<String, BigDecimal> cusConsumeAsNeededTops = new LinkedHashMap<String, BigDecimal>();
					saveChartData(cusConsumeAsNeededTops, oiChart, ChartType.CONSUME_AS_NEEDED);

					oiChart = null;
				}
			}
		}
	}
	
	/**
	 * 获取并保存图表数据
	 * @param cusTops
	 * @param oiData
	 * @param chartType
	 */
	protected void saveChartData(Map<String, BigDecimal> cusTops, OverviewIncomeChart oiChart, String chartType) {
		int i = 0;
		for (Iterator<String> it = cusTops.keySet().iterator(); it.hasNext();) {
			BaseOverviewIncomeChart newChart = new BaseOverviewIncomeChart();

			String cusId = (String) it.next();
			// 设置客户ID
			newChart.setCusId(cusId);
			BigDecimal money = cusTops.get(cusId);
			// 设置金额
			newChart.setMoney(money);

			newChart.setEndTime(oiChart.getEndTime());
			newChart.setStartTime(oiChart.getStartTime());
			newChart.setPeriodType(oiChart.getPeriodType());
			newChart.setRecordTime(oiChart.getRecordTime());
			newChart.setSort(i++);
			newChart.setStatisticType(chartType);
			if(StringUtils.equals(oiChart.getPeriodType(), PeriodType.YEAR)){
				newChart.setYear(oiChart.getYear());
			}
			// 保存
			incomeChartDao.save(newChart);
		}
	}
}
