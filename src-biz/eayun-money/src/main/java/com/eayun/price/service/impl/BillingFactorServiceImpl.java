package com.eayun.price.service.impl;

import java.io.InputStream;
import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.ServletOutputStream;

import jxl.Cell;
import jxl.Range;
import jxl.Sheet;
import jxl.Workbook;
import jxl.format.Alignment;
import jxl.format.Border;
import jxl.format.BorderLineStyle;
import jxl.format.Colour;
import jxl.format.ScriptStyle;
import jxl.format.UnderlineStyle;
import jxl.format.VerticalAlignment;
import jxl.write.Label;
import jxl.write.WritableCellFormat;
import jxl.write.WritableFont;
import jxl.write.WritableSheet;
import jxl.write.WritableWorkbook;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.eayun.common.RedisNodeIdConstant;
import com.eayun.common.constant.PayType;
import com.eayun.common.constant.RedisKey;
import com.eayun.common.dao.QueryMap;
import com.eayun.common.dao.support.Page;
import com.eayun.common.exception.AppException;
import com.eayun.common.redis.JedisUtil;
import com.eayun.common.util.BeanUtils;
import com.eayun.common.util.DateUtil;
import com.eayun.common.util.StringUtil;
import com.eayun.datacenter.ecmcservice.EcmcDataCenterService;
import com.eayun.datacenter.model.DcDataCenter;
import com.eayun.price.bean.ParamBean;
import com.eayun.price.bean.PriceDetails;
import com.eayun.price.bean.PriceRedis;
import com.eayun.price.bean.PriceTreeData;
import com.eayun.price.bean.PriceUtil;
import com.eayun.price.bean.UpgradeBean;
import com.eayun.price.dao.BillingFactorDao;
import com.eayun.price.model.BaseBillingFactor;
import com.eayun.price.model.BillingFactor;
import com.eayun.price.service.BillingFactorService;

@Service
@Transactional
public class BillingFactorServiceImpl implements BillingFactorService {
	
	private static final Logger   log = LoggerFactory.getLogger(BillingFactorServiceImpl.class);
	
	@Autowired
	private BillingFactorDao billingFactorDao;
	
	@Autowired
    private JedisUtil jedisUtil;
	
	@Autowired
	private EcmcDataCenterService ecmcDataCenterService;

	@Override
	public Page getFactorsPage(Page page, QueryMap queryMap, String dcId,
			String billingFactor, String resourcesType,String priceType) {
		log.info("查询所有计费因子列表实现...");
		List<BillingFactor> billingFactorList = new ArrayList<BillingFactor>();
		
		List<DcDataCenter> dcList = ecmcDataCenterService.getAllList();
		if(!"".equals(dcId)){
			for(DcDataCenter dc : dcList){
				if(dcId.equals(dc.getId())){
					dcList.removeAll(dcList);
					dcList.add(dc);
					break;
				}
			}
		}
		String nodeId = RedisNodeIdConstant.PRICE_CONFIG;
		if(PriceUtil.priceType.CLOUD.toString().equals(priceType)){
			nodeId = RedisNodeIdConstant.CLOUD_DATA_PRICE_CONFIG;
    	}
		
		List<PriceTreeData> resourcesTypeList = getpriceTreeData(nodeId);
		if(!"".equals(resourcesType)){
			for(PriceTreeData type : resourcesTypeList){
				if(resourcesType.equals(type.getNodeId())){
					resourcesTypeList.removeAll(resourcesTypeList);
					resourcesTypeList.add(type);
					break;
				}
			}
		}
		for(PriceTreeData type : resourcesTypeList){
			List<PriceTreeData> factorList = getpriceTreeData(type.getNodeId());
			for(PriceTreeData factor : factorList){
				if("".equals(billingFactor)){
					List<PriceTreeData> unitList = getpriceTreeData(factor.getNodeId());
					for(PriceTreeData unit : unitList){
						for(DcDataCenter dc : dcList){
							BillingFactor billingFactorModel = new BillingFactor();
							billingFactorModel.setResourcesType(type.getNodeId());
							billingFactorModel.setBillingFactor(factor.getNodeId());
							billingFactorModel.setFactorUnit(unit.getNodeId());
							billingFactorModel.setDcId(dc.getId());
							
							billingFactorModel.setTypeName(type.getName());
							billingFactorModel.setFactorName(factor.getName());
							billingFactorModel.setUnitName(unit.getName());
							billingFactorModel.setMeterName(unit.getParam1());
							billingFactorModel.setPricePay(unit.getParam2());
							billingFactorModel.setDcName(dc.getName());
							
							billingFactorList.add(billingFactorModel);
						}
					}
				}else{
					if(factor.getName().contains(billingFactor)){
						List<PriceTreeData> unitList = getpriceTreeData(factor.getNodeId());
						for(PriceTreeData unit : unitList){
							for(DcDataCenter dc : dcList){
								BillingFactor billingFactorModel = new BillingFactor();
								billingFactorModel.setResourcesType(type.getNodeId());
								billingFactorModel.setBillingFactor(factor.getNodeId());
								billingFactorModel.setFactorUnit(unit.getNodeId());
								billingFactorModel.setDcId(dc.getId());
								
								billingFactorModel.setTypeName(type.getName());
								billingFactorModel.setFactorName(factor.getName());
								billingFactorModel.setUnitName(unit.getName());
								billingFactorModel.setMeterName(unit.getParam1());
								billingFactorModel.setPricePay(unit.getParam2());
								billingFactorModel.setDcName(dc.getName());
								
								billingFactorList.add(billingFactorModel);
							}
						}
					}
				}
				
			}
		}
		Collections.sort(billingFactorList,new Comparator<BillingFactor>(){
            public int compare(BillingFactor arg0, BillingFactor arg1) {
            	String value0 = arg0.getDcId();
            	String value1 = arg1.getDcId();
            	int result = 0;
            	result = value0.compareTo(value1);
                return result;
            }
		});
		List<BillingFactor> resultList = new ArrayList<BillingFactor>();
		int pageSize = queryMap.getCURRENT_ROWS_SIZE();
        int pageNumber = queryMap.getPageNum();
        
        int start = (pageNumber-1)*pageSize;
        if(billingFactorList.size()>0){
            int end = start+pageSize;
            resultList = billingFactorList.subList(start, end < billingFactorList.size()?end:billingFactorList.size());
        }
        page = new Page(start, billingFactorList.size(), pageSize, resultList);
		return page;
	}

	@Override
	public List<BillingFactor> getPricesByPayType(String dcId,
			String billingFactor, String resourcesType, String billingUnit, String payType) {
		log.info("查询 预付费或后付费价格");
		List<BillingFactor> resultList = new ArrayList<BillingFactor>();
		List<Object> list = new ArrayList<Object>();
		StringBuffer hql = new StringBuffer();
		hql.append("from BaseBillingFactor where 1 =1 ");
		hql.append(" and resourcesType = ? ");
		list.add(resourcesType);
		if(!StringUtil.isEmpty(billingFactor)){
			hql.append(" and billingFactor = ? ");
			list.add(billingFactor);
		}
		hql.append(" and factorUnit = ?");
		list.add(billingUnit);
		hql.append(" and dcId = ? ");
		list.add(dcId);
		hql.append(" and payType = ? ");
		list.add(payType);
		hql.append(" order by createTime desc ");
		
		resultList = billingFactorDao.find(hql.toString(), list.toArray());
		return resultList;
	}
	@Override
	public List<PriceTreeData> getAllResourcesType(String priceType) {
		String nodeId = RedisNodeIdConstant.PRICE_CONFIG;
		if(PriceUtil.priceType.CLOUD.toString().equals(priceType)){
			nodeId = RedisNodeIdConstant.CLOUD_DATA_PRICE_CONFIG;
    	}
		return getpriceTreeData(nodeId);
	}
	private List<PriceTreeData> getpriceTreeData(String parentId) {
		List<PriceTreeData> priceTreeList = new ArrayList<PriceTreeData>();
		Set<String> priceDataSet = null;
		List<String> treeDataList = new ArrayList<String>();
		try {
			priceDataSet = jedisUtil.getSet(RedisKey.SYS_DATA_TREE_PARENT_NODEID+parentId);
			for(String mngData:priceDataSet){
				treeDataList.add(mngData);
			}
			Collections.sort(treeDataList);
			for(String mngData:treeDataList){
				String jsonPrice = jedisUtil.get(RedisKey.SYS_DATA_TREE+mngData);
				
				JSONObject priceJsonData = JSONObject.parseObject(jsonPrice);
				PriceTreeData priceData = new PriceTreeData();
				priceData.setName(priceJsonData.getString("nodeName"));
				priceData.setNameEN(priceJsonData.getString("nodeNameEn"));
				priceData.setNodeId(priceJsonData.getString("nodeId"));
				priceData.setParam1(priceJsonData.getString("para1"));
				priceData.setParam2(priceJsonData.getString("para2"));
				priceData.setParentId(priceJsonData.getString("parentId"));
				
				priceTreeList.add(priceData);
			}
		} catch (Exception e) {
		    log.error(e.getMessage(),e);
			throw new AppException("查询价格配置redis数据异常："+parentId);
		}
		return priceTreeList;
	}
	
	@Override
	public BillingFactor addFactorPrice(BillingFactor billingFactor,boolean  isDefault) {
		log.info("添加价格");
		billingFactor.setCreateTime(new Date());
		BaseBillingFactor baseBillingFactor = new BaseBillingFactor();
		BeanUtils.copyPropertiesByModel(baseBillingFactor, billingFactor);
		try {
			if(isDefault){	//第一次有确认类型时默认添加价格
				baseBillingFactor.setPayType("1");
				billingFactorDao.saveEntity(baseBillingFactor);
				updatePriceRedis(baseBillingFactor,"ADD");
				
				BaseBillingFactor baseBF = new BaseBillingFactor();
				BeanUtils.copyPropertiesByModel(baseBF, billingFactor);
				baseBF.setPayType("2");
				billingFactorDao.saveEntity(baseBF);
				updatePriceRedis(baseBF,"ADD");
			}else{
				billingFactorDao.saveEntity(baseBillingFactor);
				updatePriceRedis(baseBillingFactor,"ADD");
			}
			
		} catch (Exception e) {
			log.error(e.toString(),e);
		}
		BeanUtils.copyPropertiesByModel(billingFactor, baseBillingFactor);
		return billingFactor;
	}

	@Override
	public void editFactorPrice(BillingFactor billingFactor) {
		log.info("编辑价格");
		BaseBillingFactor baseBillingFactor = new BaseBillingFactor();
		BeanUtils.copyPropertiesByModel(baseBillingFactor, billingFactor);
		billingFactorDao.merge(baseBillingFactor);
		updatePriceRedis(baseBillingFactor,"EDIT");
	}

	@Override
	public void deleteFactorPrice(String id) {
		log.info("删除价格");
		BaseBillingFactor baseBillingFactor = billingFactorDao.findOne(id);
		billingFactorDao.delete(baseBillingFactor);
		updatePriceRedis(baseBillingFactor,"DELETE");
	}
	/**
	 * 更新缓存
	 * @param baseBillingFactor
	 * @param type
	 */
	private void updatePriceRedis(BaseBillingFactor baseBillingFactor , String type){
		PriceRedis priceRedis = new PriceRedis();
		priceRedis.setId(baseBillingFactor.getId());
		priceRedis.setPrice(baseBillingFactor.getPrice());
		priceRedis.setStart(baseBillingFactor.getStartNum());
		priceRedis.setEnd(baseBillingFactor.getEndNum());
		priceRedis.setCreateTime(baseBillingFactor.getCreateTime());
		//计费单位英文名称,表示镜像时为镜像id
		String factorUnitEn = baseBillingFactor.getFactorUnit();
		if(!PriceUtil.IMAGE_PRICE_TYPE.equals(baseBillingFactor.getResourcesType())){
			factorUnitEn = getNodeNameEnByNodeID(baseBillingFactor.getFactorUnit());
		}
		//查询当前计费单位的redis数据
		JSONArray array = getPriceRedisList(baseBillingFactor.getDcId(), 
				baseBillingFactor.getPayType(), factorUnitEn);
		if(type.equals("ADD")){
			array.add(priceRedis);
		}else if(type.equals("EDIT")){
			if(!array.isEmpty()){
				for(int i = 0; i< array.size();i++){
	                JSONObject obj = array.getJSONObject(i);
	                if (null == obj || obj.isEmpty()) {
	                    continue;
	                }
	                if(priceRedis.getId().equals(obj.getString("id"))){
	                	obj.put("price", priceRedis.getPrice());
	                	obj.put("start", priceRedis.getStart());
	                	obj.put("end", priceRedis.getEnd());
	                	break;
	                }
				}
			}
		}else if(type.equals("DELETE")){
			if(!array.isEmpty()){
				for(int i = 0; i< array.size();i++){
	                JSONObject obj = array.getJSONObject(i);
	                if (null == obj || obj.isEmpty()) {
	                    continue;
	                }
	                if(priceRedis.getId().equals(obj.getString("id"))){
	                	array.remove(obj);
	                	break;
	                }
				}
			}
		}
		try {
			jedisUtil.set(RedisKey.PRICE + baseBillingFactor.getDcId()+":" + baseBillingFactor.getPayType()+
					":"+factorUnitEn, array.toJSONString());
		} catch (Exception e) {
			log.error(e.toString(),e);
		}
	}
	/**
	 * 获取一种计费单位的价格缓存
	 * 条件包括数据中心、付费类型、计费单位
	 * @return
	 */
	private JSONArray getPriceRedisList(String dcId , String payType , String factorUnitEn){
		JSONArray array = new JSONArray();
		try {
			String jsonString = jedisUtil.get(RedisKey.PRICE+dcId+":"+payType+":"+factorUnitEn);
			if(null != jsonString){
				array = JSONObject.parseArray(jsonString);
			}
		} catch (Exception e) {
			log.error(e.toString(),e);
		}
		return array;
	}
	/**
	 * 根据nodeId获取英文名称
	 * 获取计费单位的英文名称
	 * @param nodeId
	 * @return
	 */
	private String getNodeNameEnByNodeID(String nodeId) {
        String factorUnitEn = null;
        try {
            String unitStr = jedisUtil.get(RedisKey.SYS_DATA_TREE + nodeId);
            JSONObject unitJSON = JSONObject.parseObject(unitStr);

            factorUnitEn = unitJSON.getString("nodeNameEn");
        } catch (Exception e) {
            throw new AppException(e.getMessage());
        }
        return factorUnitEn;
    }
	/**
	 * 计算一次业务调用所需的价钱，只返回总价
	 * @param paramBean
	 * @return
	 */
	@Override
	public BigDecimal getPriceByFactor(ParamBean paramBean) {
		log.info("计算业务价格参数："+JSONObject.toJSONString(paramBean));
		String dcId = paramBean.getDcId();
		List<DcDataCenter> dcList = ecmcDataCenterService.getAllList();
		String payType = paramBean.getPayType();
		Integer number = paramBean.getNumber();
		Integer cycleCount = paramBean.getCycleCount();
		BigDecimal totalPrice = new BigDecimal(0);
		Field[] fields = paramBean.getClass().getDeclaredFields();
		for (int i = 0; i < fields.length; i++) {
            Field f = fields[i];
            f.setAccessible(true);
            Object val = new Object();
			try {
				val = f.get(paramBean);
				String fieldName = f.getName();
				if(null == val){
				}else{
					if(fieldName.equals(PriceUtil.ImageId)){
						//镜像
						String imageId = paramBean.getImageId();
						JSONArray priceArray = getPriceRedisList(dcId, payType, imageId);
						//镜像计费为镜像单价*主机批量个数*时长
						BigDecimal unitPrice = getValidUnitPrice(priceArray, Long.valueOf(number), imageId);
						
						//按个数收费，只不过默认一次计费镜像的个数为1
						BigDecimal singlePrice = unitPrice.multiply(new BigDecimal(1));
						totalPrice = totalPrice.add(singlePrice);
						
					}else if(this.checkPriceWay(fieldName,PriceUtil.RANGE_PRICE)){
						//区间计价
						Long value = Long.valueOf(val.toString());
						JSONArray priceArray = getPriceRedisList(dcId, payType, fieldName);
						BigDecimal unitPrice = getValidUnitPrice(priceArray, value, fieldName);
						
						totalPrice = totalPrice.add(unitPrice);
						
					}else if(this.checkPriceWay(fieldName,PriceUtil.UNIT_PRICE)){
						//单价计价（镜像计算已不在这里处理）
						Long value = Long.valueOf(val.toString());
						Long valueRange = value;
						//按照个数计费的因子，查找价格区间时，先乘以批量个数
						if(this.checkPriceWay(fieldName,PriceUtil.COUNT_PRICE)){
							valueRange = value*number;
						}
						JSONArray priceArray = getPriceRedisList(dcId, payType, fieldName);
						BigDecimal unitPrice = getValidUnitPrice(priceArray, valueRange, fieldName);
						
						BigDecimal singlePrice = unitPrice.multiply(new BigDecimal(value));
						
						//对单位是10GB的计费因子需要除以10
						if(this.checkPriceWay(fieldName,PriceUtil.UNIT_TEN_TIMES)){
							singlePrice = singlePrice.divide(new BigDecimal(10));
						}

						totalPrice = totalPrice.add(singlePrice);
						
					}else if(this.checkPriceWay(fieldName,PriceUtil.LADDER_PRICE)){
						//传统阶梯计价，指OBS服务的存储空间
						if(null == dcId){
							dcId = dcList.get(0).getId();
						}
						Double value = paramBean.getSpaceCapacity();
						BigDecimal bigValue = new BigDecimal(value);
						JSONArray priceArray = getPriceRedisList(dcId, payType, fieldName);
						
						BigDecimal singlePrice = getLadderPrice(priceArray, bigValue, fieldName);
						totalPrice = totalPrice.add(singlePrice);
						
						dcId = paramBean.getDcId();
						
					}else if(this.checkPriceWay(fieldName,PriceUtil.DIFF_PRICE)){
						//差值阶梯计价，指OBS的下行流量和访问次数
						if(null == dcId){
							dcId = dcList.get(0).getId();
						}
						JSONArray priceArray = getPriceRedisList(dcId, payType, fieldName);
						BigDecimal lastVal = new BigDecimal(0);
						BigDecimal thisVal = new BigDecimal(0);
						if(fieldName.equals(PriceUtil.DIFF_PRICE[0])){			//下行流量
							Double[] values = (Double[])(val);
							lastVal = BigDecimal.valueOf(values[0]);
							thisVal = BigDecimal.valueOf(values[1]);
						}else if(fieldName.equals(PriceUtil.DIFF_PRICE[1])){	//访问次数
							Long[] values = (Long[])(val);
							lastVal = BigDecimal.valueOf(values[0]);
							thisVal = BigDecimal.valueOf(values[1]);
							lastVal = lastVal.divide(new BigDecimal(10000));
							thisVal = thisVal.divide(new BigDecimal(10000));
						}
						BigDecimal lastPrice = getLadderPrice(priceArray, lastVal, fieldName);
						BigDecimal thisPrice = getLadderPrice(priceArray, thisVal, fieldName);
						BigDecimal singlePrice = thisPrice.subtract(lastPrice);
						totalPrice = totalPrice.add(singlePrice);
						
						dcId = paramBean.getDcId();
					}
				}
			} catch (IllegalArgumentException e) {
				log.error(e.toString(),e);
			} catch (IllegalAccessException e) {
				log.error(e.toString(),e);
			}
		}
		totalPrice = totalPrice.multiply(new BigDecimal(cycleCount));
		totalPrice = totalPrice.multiply(new BigDecimal(number));
		log.info("本次业务计算价格为："+totalPrice.setScale(3,BigDecimal.ROUND_HALF_UP));
		return totalPrice.setScale(3,BigDecimal.ROUND_HALF_UP);
	}
	/**
	 * 计算一次业务调用所需的价钱，返回总价和每一种计费单位的价钱（不乘以批量数）,乘以包月数（小时数）
	 * @param paramBean
	 * @return
	 */
	@Override
	public PriceDetails getPriceDetails(ParamBean paramBean) {
		log.info("计算业务价格明细参数："+JSONObject.toJSONString(paramBean));
		PriceDetails priceDetails = new PriceDetails();

		List<DcDataCenter> dcList = ecmcDataCenterService.getAllList();
		String dcId = paramBean.getDcId();
		String payType = paramBean.getPayType();
		Integer number = paramBean.getNumber();
		Integer cycleCount = paramBean.getCycleCount();
		BigDecimal totalPrice = new BigDecimal(0);
		Field[] fields = paramBean.getClass().getDeclaredFields();
		for (int i = 0; i < fields.length; i++) {
            Field f = fields[i];
            f.setAccessible(true);
            Object val = new Object();
			try {
				val = f.get(paramBean);
				String fieldName = f.getName();
				if(null == val){
				}else{
					if(fieldName.equals(PriceUtil.ImageId)){
						//镜像
						String imageId = paramBean.getImageId();
						JSONArray priceArray = getPriceRedisList(dcId, payType, imageId);
						//按照个数计费的因子，查找价格区间时，先乘以批量个数
						//镜像计费为镜像单价*主机批量个数*时长
						BigDecimal unitPrice = getValidUnitPrice(priceArray, Long.valueOf(number), imageId);
						
						//镜像按照个数收费，只不过默认为1
						BigDecimal singlePrice = unitPrice.multiply(new BigDecimal(1));
						singlePrice = singlePrice.multiply(new BigDecimal(cycleCount));
						totalPrice = totalPrice.add(singlePrice);
						
						priceDetails.setImagePrice(singlePrice.setScale(3, BigDecimal.ROUND_HALF_UP));
						
					}else if(this.checkPriceWay(fieldName,PriceUtil.RANGE_PRICE)){
						//区间计价
						Long value = Long.valueOf(val.toString());
						JSONArray priceArray = getPriceRedisList(dcId, payType, fieldName);

						BigDecimal unitPrice = getValidUnitPrice(priceArray, value, fieldName);
						unitPrice = unitPrice.multiply(new BigDecimal(cycleCount));
						totalPrice = totalPrice.add(unitPrice);
						
						if(fieldName.equals(PriceUtil.RANGE_PRICE[0])){
							priceDetails.setCusImagePrice(unitPrice.setScale(3, BigDecimal.ROUND_HALF_UP));
						}else if(fieldName.equals(PriceUtil.RANGE_PRICE[1])){
							priceDetails.setPoolPrice(unitPrice.setScale(3, BigDecimal.ROUND_HALF_UP));
						}
						
					}else if(this.checkPriceWay(fieldName,PriceUtil.UNIT_PRICE)){
						//单价计价（镜像计算已不在这里处理）
                        if(null == dcId){
                            dcId = dcList.get(0).getId();
                        }
						Long value = Long.valueOf(val.toString());
						Long valueRange = value;
						//按照个数计费的因子，查找价格区间时，先乘以批量个数
						if(this.checkPriceWay(fieldName,PriceUtil.COUNT_PRICE)){
							valueRange = value*number;
						}
						JSONArray priceArray = getPriceRedisList(dcId, payType, fieldName);
						BigDecimal unitPrice = getValidUnitPrice(priceArray, valueRange, fieldName);
						
						BigDecimal singlePrice = unitPrice.multiply(new BigDecimal(value));
						
						//对单位是10GB的计费因子需要除以10
						if(this.checkPriceWay(fieldName,PriceUtil.UNIT_TEN_TIMES)){
							singlePrice = singlePrice.divide(new BigDecimal(10));
						}
						
						singlePrice = singlePrice.multiply(new BigDecimal(cycleCount));
                        //针对CDN额外处理一下
                        if(fieldName.equals(PriceUtil.UNIT_PRICE[6])){
                            BigDecimal finalCharge = singlePrice.divide(new BigDecimal(1024*1024*1024));
                            totalPrice = totalPrice.add(finalCharge);
                        }else if(fieldName.equals(PriceUtil.UNIT_PRICE[7])){
                            BigDecimal finalCharge = singlePrice.divide(new BigDecimal(1000));
                            totalPrice = totalPrice.add(finalCharge);
                        }else if(fieldName.equals(PriceUtil.UNIT_PRICE[8])){
                            BigDecimal finalCharge = singlePrice.divide(new BigDecimal(10000));
                            totalPrice = totalPrice.add(finalCharge);
                        }else {
                            totalPrice = totalPrice.add(singlePrice);
                        }

						if(fieldName.equals(PriceUtil.UNIT_PRICE[0]) || fieldName.equals(PriceUtil.UNIT_PRICE[9])){
							priceDetails.setCpuPrice(singlePrice.setScale(3, BigDecimal.ROUND_HALF_UP));
						}else if(fieldName.equals(PriceUtil.UNIT_PRICE[1]) || fieldName.equals(PriceUtil.UNIT_PRICE[10])){
							priceDetails.setRamPrice(singlePrice.setScale(3, BigDecimal.ROUND_HALF_UP));
						}else if(fieldName.equals(PriceUtil.UNIT_PRICE[2])){
							priceDetails.setSnapshotPrice(singlePrice.setScale(3, BigDecimal.ROUND_HALF_UP));
						}else if(fieldName.equals(PriceUtil.UNIT_PRICE[3])){
							priceDetails.setBandWidthPrice(singlePrice.setScale(3, BigDecimal.ROUND_HALF_UP));
						}else if(fieldName.equals(PriceUtil.UNIT_PRICE[4])){
							priceDetails.setVpnPrice(singlePrice.setScale(3, BigDecimal.ROUND_HALF_UP));
						}else if(fieldName.equals(PriceUtil.UNIT_PRICE[5])){
							priceDetails.setIpPrice(singlePrice.setScale(3, BigDecimal.ROUND_HALF_UP));
						}else if(fieldName.equals(PriceUtil.UNIT_PRICE[6])){
                            //cdn下载流量传入的是B，计费单位是GB，所以singlePrice = nB*m元/GB，需要再除以(1024*1204*1024)，才得到GB的费用
                            BigDecimal finalCharge = singlePrice.divide(new BigDecimal(1024*1024*1024));
                            priceDetails.setCdnDownloadPrice(finalCharge.setScale(3, BigDecimal.ROUND_HALF_UP));
                        }else if(fieldName.equals(PriceUtil.UNIT_PRICE[7])){
                            //cdn动态请求数的单位是xx元/千次，而我们传入的次数是次数，所以singlePrice = n次*m元/千次，需要再除以1000算得千次的费用
                            BigDecimal finalCharge = singlePrice.divide(new BigDecimal(1000));
                            priceDetails.setCdnDreqsPrice(finalCharge.setScale(3, BigDecimal.ROUND_HALF_UP));
                        }else if(fieldName.equals(PriceUtil.UNIT_PRICE[8])){
                            //cdn-https请求数的单位是xx元/万次，而我们传入的次数是次数，所以singlePrice = n次*m元/万次，需要再除以10000算得万次的费用
                            BigDecimal finalCharge = singlePrice.divide(new BigDecimal(10000));
                            priceDetails.setCdnHreqsPrice(finalCharge.setScale(3, BigDecimal.ROUND_HALF_UP));
                        }else if(  fieldName.equals(PriceUtil.UNIT_PRICE[11]) || fieldName.equals(PriceUtil.UNIT_PRICE[12])
                        		|| fieldName.equals(PriceUtil.UNIT_PRICE[13])){
                        	priceDetails.setSysDiskPrice(singlePrice.setScale(3, BigDecimal.ROUND_HALF_UP));
                        }else if(  fieldName.equals(PriceUtil.UNIT_PRICE[14]) || fieldName.equals(PriceUtil.UNIT_PRICE[15])
                        		|| fieldName.equals(PriceUtil.UNIT_PRICE[16]) || fieldName.equals(PriceUtil.UNIT_PRICE[17])
                        		|| fieldName.equals(PriceUtil.UNIT_PRICE[18]) || fieldName.equals(PriceUtil.UNIT_PRICE[19])){
                        	priceDetails.setDataDiskPrice(singlePrice.setScale(3, BigDecimal.ROUND_HALF_UP));
                        }
					}else if(this.checkPriceWay(fieldName,PriceUtil.LADDER_PRICE)){
						//传统阶梯计价，指OBS服务的存储空间
						if(null == dcId){
							dcId = dcList.get(0).getId();
						}
						Double value = paramBean.getSpaceCapacity();
						BigDecimal bigValue = new BigDecimal(value);
						JSONArray priceArray = getPriceRedisList(dcId, payType, fieldName);
						
						BigDecimal singlePrice = getLadderPrice(priceArray, bigValue, fieldName);
						singlePrice = singlePrice.multiply(new BigDecimal(cycleCount));
						totalPrice = totalPrice.add(singlePrice);
						
						priceDetails.setSpacePrice(singlePrice.setScale(3, BigDecimal.ROUND_HALF_UP));
						
						dcId = paramBean.getDcId();
						
					}else if(this.checkPriceWay(fieldName,PriceUtil.DIFF_PRICE)){
						//差值阶梯计价，指OBS的下行流量和访问次数
						if(null == dcId){
							dcId = dcList.get(0).getId();
						}
						JSONArray priceArray = getPriceRedisList(dcId, payType, fieldName);
						BigDecimal lastVal = new BigDecimal(0);
						BigDecimal thisVal = new BigDecimal(0);
						if(fieldName.equals(PriceUtil.DIFF_PRICE[1])){	//访问次数
							Long[] values = (Long[])(val);
							lastVal = BigDecimal.valueOf(values[0]);
							thisVal = BigDecimal.valueOf(values[1]);
							lastVal = lastVal.divide(new BigDecimal(10000));
							thisVal = thisVal.divide(new BigDecimal(10000));
						}else{
							Double[] values = (Double[])(val);
							lastVal = BigDecimal.valueOf(values[0]);
							thisVal = BigDecimal.valueOf(values[1]);
						}
						BigDecimal lastPrice = getLadderPrice(priceArray, lastVal, fieldName);
						BigDecimal thisPrice = getLadderPrice(priceArray, thisVal, fieldName);
						BigDecimal singlePrice = thisPrice.subtract(lastPrice);
						if(singlePrice.compareTo(new BigDecimal(0)) < 0){
							log.error("阶梯价格设置有问题，价钱为负，请及时排查！！！:"+priceArray.toString());
							singlePrice = new BigDecimal(0);
						}
						singlePrice = singlePrice.multiply(new BigDecimal(cycleCount));
						totalPrice = totalPrice.add(singlePrice);
						
						if(fieldName.equals(PriceUtil.DIFF_PRICE[0])){			//下行流量
							priceDetails.setDownPrice(singlePrice.setScale(3, BigDecimal.ROUND_HALF_UP));
						}else if(fieldName.equals(PriceUtil.DIFF_PRICE[1])){	//访问次数
							priceDetails.setRequestPrice(singlePrice.setScale(3, BigDecimal.ROUND_HALF_UP));
						}
						
						dcId = paramBean.getDcId();
					}else if(this.checkPriceWay(fieldName,PriceUtil.UNIT_PRICE_TO_DELETE)){
						//RDS1.0之前原数据盘和系统盘的逻辑计算单独抽出，稳定后此部分可删除，不影响上面的计算
						Long value = Long.valueOf(val.toString());
						Long valueRange = value;
						JSONArray priceArray = getPriceRedisList(dcId, payType, fieldName);
						BigDecimal unitPrice = getValidUnitPrice(priceArray, valueRange, fieldName);
						BigDecimal singlePrice = unitPrice.multiply(new BigDecimal(value));
						singlePrice = singlePrice.multiply(new BigDecimal(cycleCount));

						if(fieldName.equals(PriceUtil.UNIT_PRICE_TO_DELETE[0]) && null!=priceDetails.getDataDiskPrice()){
							priceDetails.setDataDiskPrice(singlePrice.setScale(3, BigDecimal.ROUND_HALF_UP));
						}else if(fieldName.equals(PriceUtil.UNIT_PRICE_TO_DELETE[1]) && null!=priceDetails.getSysDiskPrice()){
							priceDetails.setSysDiskPrice(singlePrice.setScale(3, BigDecimal.ROUND_HALF_UP));
						}
					}
				}
			} catch (IllegalArgumentException e) {
				log.error(e.toString(),e);
			} catch (IllegalAccessException e) {
				log.error(e.toString(),e);
			}
		}
		totalPrice = totalPrice.multiply(new BigDecimal(number));
	
		priceDetails.setCycleCount(cycleCount);
		priceDetails.setNumber(number);
		priceDetails.setTotalPrice(totalPrice.setScale(3, BigDecimal.ROUND_HALF_UP));
		log.info("本次业务计算价格明细为："+JSONObject.toJSONString(priceDetails));
		return priceDetails;
	}

	/**
	 * 一次升级配置业务计算所需支付价钱
	 * 只针对预付费类型
	 * @param upgradeBean
	 * @return
	 */
	@Override
	public BigDecimal updateConfigPrice(UpgradeBean upgradeBean) {
		log.info("计算升级业务价格参数："+JSONObject.toJSONString(upgradeBean));
		String dcId = upgradeBean.getDcId();
		Integer cycleCount = upgradeBean.getCycleCount();
		BigDecimal totlePrice = new BigDecimal(0);
		Field[] fields = upgradeBean.getClass().getDeclaredFields();
        for (int i = 0; i < fields.length; i++) {
            Field f = fields[i];
            f.setAccessible(true);
            Object val = new Object();
			try {
				val = f.get(upgradeBean);
				String fieldName = f.getName();
				if(null == val){
				}else{
					if(this.checkPriceWay(fieldName,PriceUtil.UPDATE_UNIT)){
						//四种单价计价的升级计费单位
						Long value = Long.valueOf(val.toString());
						JSONArray priceArray = getPriceRedisList(dcId, PayType.PAYBEFORE, fieldName);
						BigDecimal unitPrice = getValidUnitPrice(priceArray, value, fieldName);
						
						BigDecimal singlePrice = unitPrice.multiply(new BigDecimal(value));
						//对单位是10GB的计费因子需要除以10
						if(this.checkPriceWay(fieldName,PriceUtil.UNIT_TEN_TIMES)){
							singlePrice = singlePrice.divide(new BigDecimal(10));
						}
						
						totlePrice = totlePrice.add(singlePrice);
					}else if(this.checkPriceWay(fieldName,PriceUtil.UPDATE_CONN)){
						//负载均衡，区间计价
						JSONArray priceArray = getPriceRedisList(dcId, PayType.PAYBEFORE, PriceUtil.ConnCount);
						Long oldVal = upgradeBean.getOldConnCount();
						Long newVal = upgradeBean.getNewConnCount();
						BigDecimal oldPrice = getValidUnitPrice(priceArray, oldVal, PriceUtil.ConnCount);
						BigDecimal newPrice = getValidUnitPrice(priceArray, newVal, PriceUtil.ConnCount);
						totlePrice = newPrice.subtract(oldPrice);
						break;
					}
				}
			} catch (IllegalArgumentException e) {
				log.error(e.toString(),e);
			} catch (IllegalAccessException e) {
				log.error(e.toString(),e);
			}
        }
        totlePrice = totlePrice.multiply(new BigDecimal(cycleCount));
        totlePrice = totlePrice.divide(new BigDecimal(30), 3, BigDecimal.ROUND_HALF_UP);
        log.info("本次升级业务计算总价为："+totlePrice);
		return totlePrice;
	}
	//检查一种计费单位是否在一种计价方式内
	private boolean checkPriceWay(String unitNameEn,String[] priceWay){
		for(String way : priceWay){
			if(unitNameEn.equals(way)){
				return true;
			}
		}
		return false;
	}
	/**
	 * 根据数值获取符合条件的有效的单价
	 * @param priceArray
	 * @param value
	 * @return
	 */
	private BigDecimal getValidUnitPrice(JSONArray priceArray,Long value,String unitName){
		log.info("查询单价："+unitName+",value:"+value+",price:"+priceArray.toString());
		if(!priceArray.isEmpty()){
			if(value==0){
				return new BigDecimal(0);
			}
			List<PriceRedis> list = new ArrayList<PriceRedis>();
			for(int i = 0; i< priceArray.size();i++){
                JSONObject obj = priceArray.getJSONObject(i);
                if (null == obj || obj.isEmpty()) {
                    continue;
                }
                Long start = obj.getLong("start");
                Long end = obj.getLong("end");
                Date createTime = obj.getDate("createTime");
                BigDecimal price = obj.getBigDecimal("price");
                if((value >= start && value <= end && end != -1) || (value >= start && end == -1)){
                	PriceRedis priceRedis = new PriceRedis();
                	priceRedis.setCreateTime(createTime);
                	priceRedis.setPrice(price);
                	list.add(priceRedis);
                }
			}
			if(!list.isEmpty()){
				Collections.sort(list,new Comparator<PriceRedis>(){
		            public int compare(PriceRedis arg0, PriceRedis arg1) {
		            	Date value0 = arg0.getCreateTime();
		            	Date value1 = arg1.getCreateTime();
		            	int result = 0;
		            	result = value1.compareTo(value0);
		                return result;
		            }
				});
				return list.get(0).getPrice();
			}else{
				log.error(new Date()+unitName+"该计费单位没有设置包含该数据值的区间价格");
				throw new AppException("获取价格失败，请稍后重试");
			}
		}else{
			log.error(new Date()+unitName+"该计费单位没有设置价格");
			throw new AppException("获取价格失败，请稍后重试");
		}
	}
	/**
	 * 计算阶梯计价价钱
	 * 最终结果再保留3位小数
	 * 线上问题解决：对象存储计费相关单位均为原来的100倍
	 * @param priceArray
	 * @param bigValue
	 * @param unitName
	 * @return
	 */
	private BigDecimal getLadderPrice(JSONArray priceArray , BigDecimal bigValue , String unitName){
		log.info("阶梯计算价钱："+unitName+",value:"+bigValue+",price:"+priceArray.toString());
		BigDecimal finalPrice = new BigDecimal(0);
		if(!priceArray.isEmpty()){
			List<PriceRedis> list = new ArrayList<PriceRedis>();
			for(int i = 0; i< priceArray.size();i++){
                JSONObject obj = priceArray.getJSONObject(i);
                if (null == obj || obj.isEmpty()) {
                    continue;
                }
                PriceRedis priceRedis = new PriceRedis();
                priceRedis.setCreateTime(obj.getDate("createTime"));
                priceRedis.setStart(obj.getLong("start"));
                priceRedis.setEnd(obj.getLong("end"));
                priceRedis.setPrice(obj.getBigDecimal("price"));
                list.add(priceRedis);
			}
			if(!list.isEmpty()){
				Collections.sort(list,new Comparator<PriceRedis>(){
		            public int compare(PriceRedis arg0, PriceRedis arg1) {
		            	int result = 0;
		            	result = arg1.getStart().compareTo(arg0.getStart());
		                return result;
		            }
				});
			}else{
				log.error(new Date()+unitName+"该计费单位没有有效的阶梯区间价格");
				throw new AppException("获取价格失败，请稍后重试");
			}
			BigDecimal deduction = new BigDecimal(0);
			BigDecimal dedu = new BigDecimal(0);
			boolean isok = false;
			for(int i = 0;i < list.size() ;i++){
				PriceRedis priceRedis = list.get(i);
				BigDecimal start = new BigDecimal(priceRedis.getStart());
				BigDecimal end = new BigDecimal(priceRedis.getEnd());
				BigDecimal price = priceRedis.getPrice();
				
				if(bigValue.compareTo(start) >= 0 && (priceRedis.getEnd() == -1 || (priceRedis.getEnd() != -1 && bigValue.compareTo(end) <= 0))){
					if(!isok){
						finalPrice = (bigValue.divide(new BigDecimal(100))).multiply(price);
						finalPrice = finalPrice.subtract((start.divide(new BigDecimal(100))).multiply(price));
					}
					isok = true;
                }else{
                	if(isok){
                		dedu = ((end.subtract(start)).divide(new BigDecimal(100))).multiply(price);
        				deduction = deduction.add(dedu);
                	}
                }
			}
			if(!isok){
				log.error("阶梯价格设置有问题，请及时排查！！！:"+unitName+","+priceArray.toString());
			}
			finalPrice = finalPrice.add(deduction);
			return finalPrice;
		}else{
			log.error(new Date()+unitName+"该计费单位没有设置阶梯区间价格");
			throw new AppException("获取价格失败，请稍后重试");
		}
	}

	@Override
	public void syncFactorPrice() {
		Iterator<String> keys;
		try {
			keys = jedisUtil.keys(RedisKey.PRICE+"*").iterator();
			//删除所有价格节点
			String sys_data_tree_key = null;
			while (keys.hasNext()) {
				sys_data_tree_key = (String) keys.next();
				jedisUtil.delete(sys_data_tree_key);
			}
			
			// 查询全部的节点，更新到缓存
			Iterator<BaseBillingFactor> allPriceIter = billingFactorDao.findAll().iterator();
			BaseBillingFactor billingFactor = null;
			while (allPriceIter.hasNext()) {
				billingFactor = allPriceIter.next();
				updatePriceRedis(billingFactor, "ADD");
			}
		} catch (Exception e) {
			log.error(e.toString(),e);
		}
	}

	/**
	 * 删除一个镜像下的所有价格
	 * @param imageId
	 */
	@Override
	public void deleteImagePrice(String imageId ,String dcId) {
		StringBuffer sb = new StringBuffer();
        sb.append("delete BaseBillingFactor where factorUnit = ?");
        billingFactorDao.executeUpdate(sb.toString(), imageId);
        try {
        	jedisUtil.delete(RedisKey.PRICE+dcId+":1:"+imageId);
			jedisUtil.delete(RedisKey.PRICE+dcId+":2:"+imageId);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * 导出数据中心下的价格配置
	 * @Author: duanbinbin
	 * @param outputStream
	 * @param dcId
	 * @param type
	 *<li>Date: 2017年2月20日</li>
	 */
	@Override
	public void exportPriceSheets(ServletOutputStream outputStream, String dcId , String type) {
		
		String nodeId = RedisNodeIdConstant.PRICE_CONFIG;
		if(PriceUtil.priceType.CLOUD.toString().equals(type)){
			nodeId = RedisNodeIdConstant.CLOUD_DATA_PRICE_CONFIG;
    	}
		try {
			
			WritableWorkbook workbook = Workbook.createWorkbook(outputStream);
			String[] title = {"计费类型","计费因子","计费单位","左区间","右区间","价格（元）","创建时间"};
			String[] payTypes = {PayType.PAYAFTER,PayType.PAYBEFORE};
			for(int i = 0; i < payTypes.length;i++){
				List<BillingFactor> resultList = getList(dcId , nodeId, payTypes[i]);
				String sheetName = "预付费";
				if(PayType.PAYAFTER.equals(payTypes[i])){
					sheetName = "后付费";
				}
				WritableSheet ws = workbook.createSheet(sheetName, 0);
				Label label = new Label(0, 0, "价格配置表", setTypeface(16,false,"1"));
				ws.addCell(label);
	            ws.mergeCells(0, 0, 6, 0);		//第一行标题合并列
	            ws.setRowView(0, 800);   		//第一行高度
	            
	            for(int j = 0; j < title.length;j++){
	            	ws.setRowView(1,400);		//第二行高度
	            	Label header = new Label(j, 1, title[j], setTypeface(10,true,"2"));
	            	ws.addCell(header);
	            }
	            
	            String resourcesType = "";
                String billingFactor = "";
                String factorUnit = "";
                int a = 0;
                int b = 0;
                int c = 0;
                for(int j = 0; j < resultList.size();j++){
                	BillingFactor bf = resultList.get(j);
                	for(int k = 0; k< 7;k++){
                		 String cont = "";
                		 switch(k){
                         case 0:
                        	 cont = bf.getTypeName();
                             break;
                         case 1:
                        	 cont = bf.getFactorName();
                             break;
                         case 2:
                        	 cont = bf.getUnitName();
                             break;
                         case 3:
                        	 cont = String.valueOf(bf.getStartNum());
                             break;
                         case 4:
                        	 cont = String.valueOf(bf.getEndNum());
                             break;
                         case 5:
                        	 cont = String.valueOf(bf.getPrice());
                             break;
                         case 6:
                        	 Date createTime = bf.getCreateTime();
                    		 cont = DateUtil.dateToString(createTime);
                             break;
                		}
                		ws.setRowView(j+2,400);
                		Label content = new Label(k,j+2, cont, setTypeface(10,false,"3"));
                		ws.addCell(content);
                	}
                	if(j==0){
                		resourcesType = bf.getResourcesType();
                        billingFactor = bf.getBillingFactor();
                        factorUnit = bf.getFactorUnit();
                	}else if(j!=0 && j < resultList.size()-1){
                		if(resourcesType.equals(bf.getResourcesType())){
                			a++;
                		}else{
                			ws.mergeCells(0, j+2-a-1, 0, j+2-1);
                			resourcesType = bf.getResourcesType();
                			a = 0;
                		}
                		if(billingFactor.equals(bf.getBillingFactor())){
                			b++;
                		}else{
                			ws.mergeCells(1, j+2-b-1, 1, j+2-1);
                			billingFactor = bf.getBillingFactor();
                			b = 0;
                		}
                		if(factorUnit.equals(bf.getFactorUnit())){
                			c++;
                		}else{
                			ws.mergeCells(2, j+2-c-1, 2, j+2-1);
                			factorUnit = bf.getFactorUnit();
                			c = 0;
                		}
                	}else if(j == resultList.size()-1){
                		if(resourcesType.equals(bf.getResourcesType())){
                			ws.mergeCells(0, j+2-a-1, 0, j+2);
                		}
                		if(billingFactor.equals(bf.getBillingFactor())){
                			ws.mergeCells(1, j+2-b-1, 1, j+2);
                		}
                		if(factorUnit.equals(bf.getFactorUnit())){
                			ws.mergeCells(2, j+2-c-1, 2, j+2);
                		}
                	}
                }
                for(int j = 0; j< 7;j++){
                	if(j == 0||j == 1||j == 2||j == 6){
                		ws.setColumnView(j, 22);
                	}else if(j == 5){
                		ws.setColumnView(j, 12);
                	}else{
                		ws.setColumnView(j, 10);
                	}
            	}
			}
            workbook.write();
            workbook.close(); 
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private List<BillingFactor> getList(String dcId ,String type ,String payType) {
		List<BillingFactor> list = new ArrayList<BillingFactor>();
		StringBuffer sb = new StringBuffer();
		List<Object> param = new ArrayList<Object>();
        sb.append("SELECT mbf.resources_type,mbf.billing_factor,mbf.factor_unit,sdt.node_name_en as nameEn,sdt1.node_name_en as nameEn1,"
        		+ "sdt2.node_name_en as nameEn2,mbf.start_num,mbf.end_num,mbf.pay_type,mbf.price,mbf.create_time");
        sb.append(" FROM	money_billing_factor mbf");
        sb.append(" JOIN sys_data_tree sdt ON mbf.resources_type = sdt.node_id");
        sb.append(" JOIN sys_data_tree sdt1 ON mbf.billing_factor = sdt1.node_id");
        sb.append(" JOIN sys_data_tree sdt2 ON mbf.factor_unit = sdt2.node_id");
        sb.append(" WHERE	resources_type <> 'IMAGE' AND billing_factor IS NOT NULL");
        sb.append(" AND dc_id = ? ");
        param.add(dcId);
        sb.append(" AND sdt.parent_id = ?");
        param.add(type);
        sb.append(" AND mbf.pay_type = ?");
        param.add(payType);
        sb.append(" ORDER BY factor_unit ASC, pay_type ASC, create_time DESC");
        javax.persistence.Query query = billingFactorDao.createSQLNativeQuery(sb.toString(), param.toArray());
        List result = query.getResultList();
        if(!result.isEmpty()){
        	for(int i = 0; i < result.size(); i++){
        		Object[] obj = (Object[]) result.get(i);
        		BillingFactor bf = new BillingFactor();
        		bf.setResourcesType(String.valueOf(obj[0]));
        		bf.setBillingFactor(String.valueOf(obj[1]));
        		bf.setFactorUnit(String.valueOf(obj[2]));
        		bf.setTypeName(String.valueOf(obj[3]));
        		bf.setFactorName(String.valueOf(obj[4]));
        		bf.setUnitName(String.valueOf(obj[5]));
        		bf.setStartNum(Long.valueOf(String.valueOf(obj[6])));
        		bf.setEndNum(Long.valueOf(String.valueOf(obj[7])));
        		bf.setPayType(String.valueOf(obj[8]));
        		bf.setPrice(new BigDecimal(String.valueOf(obj[9])));
        		bf.setCreateTime((Date) obj[10]);
        		list.add(bf);
        	}
        }
        return list;
	}
	/**
	 * 设置字体样式
	 * @Author: duanbinbin
	 * @param size		字号
	 * @param level		字体
	 * @param isBold	是否加粗
	 * @return
	 * @throws Exception
	 *<li>Date: 2017年2月20日</li>
	 */
	private WritableCellFormat setTypeface(int size , boolean isBold, String level) throws Exception {
		
        WritableFont font = new WritableFont(
                WritableFont.createFont("微软雅黑"),//字体
                size,                 	// 字号
                isBold?WritableFont.BOLD:WritableFont.NO_BOLD,  //设置粗体
                false,              	//是否斜体
                UnderlineStyle.NO_UNDERLINE, //设置下划线
                Colour.BLACK,       	// 字体颜色
                ScriptStyle.NORMAL_SCRIPT);
        WritableCellFormat wcf = new WritableCellFormat(font);//设置字体样式
        
        wcf.setAlignment(Alignment.CENTRE);   				// 设置对齐方式(水平居中)
        wcf.setVerticalAlignment(VerticalAlignment.CENTRE);	//垂直居中
        
        if("1".equals(level)){
        	wcf.setBorder(Border.NONE, BorderLineStyle.NONE);	//边框
        }else if("2".equals(level)){
        	wcf.setBorder(Border.ALL, BorderLineStyle.THIN);	//边框
        	wcf.setBackground(Colour.GRAY_25);					//设置背景颜色
        }else if("3".equals(level)){
        	wcf.setBorder(Border.ALL, BorderLineStyle.THIN);	//边框
        }
        return wcf;
    }

	/**
	 * 导入价格
	 * @Author: duanbinbin
	 * @param is
	 * @param dcId
	 * @throws Exception
	 *<li>Date: 2017年2月23日</li>
	 */
	@Override
	public void importPriceExcel(InputStream is,String dcId,String priceType) throws Exception {
		String nodeId = RedisNodeIdConstant.PRICE_CONFIG;
		if(PriceUtil.priceType.CLOUD.toString().equals(priceType)){
			nodeId = RedisNodeIdConstant.CLOUD_DATA_PRICE_CONFIG;
    	}
		Map<String , String> nameEnToIds = new HashMap<String , String>();
		List<PriceTreeData> resourcesTypeList = getpriceTreeData(nodeId);
		for(PriceTreeData type : resourcesTypeList){
			nameEnToIds.put(type.getNameEN(), type.getNodeId());
			List<PriceTreeData> factorList = getpriceTreeData(type.getNodeId());
			for(PriceTreeData factor : factorList){
				nameEnToIds.put(factor.getNameEN(), factor.getNodeId());
				List<PriceTreeData> unitList = getpriceTreeData(factor.getNodeId());
				for(PriceTreeData unit : unitList){
					nameEnToIds.put(unit.getNameEN(), unit.getNodeId());
				}
			}
		}
		List<BaseBillingFactor> importList = goToImportList(is,dcId,nameEnToIds);
		
		for(BaseBillingFactor bbf : importList){
			billingFactorDao.saveEntity(bbf);
			updatePriceRedis(bbf,"ADD");
		}
	}
	/**
	 * 读取Excel文件，获取所有需要添加的计费因子价格记录
	 * 删除对应的价格
	 * @Author: duanbinbin
	 * @param is
	 * @param dcId
	 * @param map
	 * @return
	 * @throws Exception
	 *<li>Date: 2017年2月23日</li>
	 */
	private List<BaseBillingFactor> goToImportList(InputStream is,String dcId,Map<String , String> map) throws Exception {
		List<BaseBillingFactor> list = new ArrayList<BaseBillingFactor>();
		HashSet<String> toDelete=new HashSet<String>();
		Workbook rwb = Workbook.getWorkbook(is);
		int num = rwb.getNumberOfSheets();
		for(int x = 0; x < num;x++){
			Sheet sheet = rwb.getSheet(x);
	        int row =sheet.getRows();
	        String resourcesType = "";
	        String billingFactor = "";
	        String factorUnit = "";
	        String payType = "1";
	        
	        Range[] range = sheet.getMergedCells();
	        if(x == 1){
	        	payType = "2";
	        }
	        for(int i = 0;i<row;i++){
	        	if(i < 2) continue;
	        	Cell[] cell  = sheet.getRow(i);
	        	BaseBillingFactor bf = new BaseBillingFactor();
	        	Long startNum = 0L;
	        	Long endNum = 0L;
	        	BigDecimal price = new BigDecimal(0);
	        	Date createTime = new Date();
	        	
	        	Boolean isAllRight= true;			//判断一行表格是否全部有值且正确
	        	for (int j = 0; j < cell.length; j++) {
	        		String cont = cell[j].getContents();
	        		if(StringUtil.isEmpty(cont)){
	        			if(j > 2){
	        				isAllRight = false;
		        			log.error("Excel文件"+(x+1)+"页签，第"+(i+1)+"行，第"+(j+1)+"列数据未设置，此行价格数据不导入，请检查文件！！！");
		        			break;
	        			}else{
	        				if(!this.isInMerge(range, i, j)){
	        					isAllRight = false;
			        			log.error("Excel文件"+(x+1)+"页签，第"+(i+1)+"行，第"+(j+1)+"列数据未设置，此行价格数据不导入，请检查文件！！！");
			        			break;
	        				}
	        			}
	        		}
	        		switch(j){
	                case 0:
	                	if(!StringUtil.isEmpty(cont) && !cont.equals(resourcesType)){
	                		if(StringUtil.isEmpty(map.get(cont))){
	                			isAllRight = false;
	                			log.error("Excel文件"+(x+1)+"页签，第"+(i+1)+"行，第"+(j+1)+"设置的计费类型匹配不到，此行价格数据不导入，请检查文件！！！");
	                    		break;
	                		}
	                		resourcesType = map.get(cont);
	                	}
	                    break;
	                case 1:
	                	if(!StringUtil.isEmpty(cont) && !cont.equals(billingFactor)){
	                		if(StringUtil.isEmpty(map.get(cont))){
	                			isAllRight = false;
	                			log.error("Excel文件"+(x+1)+"页签，第"+(i+1)+"行，第"+(j+1)+"设置的计费因子匹配不到，此行价格数据不导入，请检查文件！！！");
	                    		break;
	                		}
	                		billingFactor = map.get(cont);
	                	}
	                    break;
	                case 2:
	                	if(!StringUtil.isEmpty(cont) && !cont.equals(factorUnit)){
	                		if(StringUtil.isEmpty(map.get(cont))){
	                			isAllRight = false;
	                			log.error("Excel文件"+(x+1)+"页签，第"+(i+1)+"行，第"+(j+1)+"设置的计费单位匹配不到，此行价格数据不导入，请检查文件！！！");
	                    		break;
	                		}
	                		factorUnit = map.get(cont);
	                	}
	                    break;
	                case 3:
	                	if(!this.isNum(cont)){		//左区间必须为纯数字，即0和正整数
	                		isAllRight = false;
	                		log.error("Excel文件"+(x+1)+"页签，第"+(i+1)+"行，第"+(j+1)+"列左区间格式设置不正确，此行价格数据不导入，请检查文件！！！");
	                		break;
	                	}
	                	startNum = Long.valueOf(cont);
	                    break;
	                case 4:
	                	if(!this.isNum(cont) && !"-1".equals(cont)){	//右区间必须为纯数字或“-1”
	                		isAllRight = false;
	                		log.error("Excel文件"+(x+1)+"页签，第"+(i+1)+"行，第"+(j+1)+"列右区间格式设置不正确，此行价格数据不导入，请检查文件！！！");
	                		break;
	                	}
	                	endNum = Long.valueOf(cont);
	                    break;
	                case 5:
	                	if(!this.isRightPrice(cont)){	//价格必须为非负的浮点数
	                		isAllRight = false;
	                		log.error("Excel文件"+(x+1)+"页签，第"+(i+1)+"行，第"+(j+1)+"列价格格式设置不正确，此行价格数据不导入，请检查文件！！！");
	                		break;
	                	}
	                	price = new BigDecimal(cont);
	                    break;
	                case 6:
	                	cont=cont.replaceAll("/", "-");
						createTime = DateUtil.stringToDate(cont);
						if(null == createTime){
							isAllRight = false;
							log.error("Excel文件"+(x+1)+"页签，第"+(i+1)+"行，第"+(j+1)+"列日期格式设置不正确，此行价格数据不导入，请检查文件！！！");
						}
	                    break;
	        		}
	        	}
	        	//如果一行内有一列未赋值，或同一类型下的（分基础资源和云数据库）计费因子单位匹配不到，则此条信息无效
	        	if(!isAllRight || StringUtil.isEmpty(resourcesType) 
	        			|| StringUtil.isEmpty(billingFactor) || StringUtil.isEmpty(factorUnit)){
	        		continue;
	        	}
	        	
	        	bf.setResourcesType(resourcesType);
	    		bf.setBillingFactor(billingFactor);
	    		bf.setFactorUnit(factorUnit);
	    		bf.setStartNum(startNum);
	    		bf.setEndNum(endNum);
	    		bf.setPrice(price);
	    		bf.setCreateTime(createTime);
	    		bf.setPayType(payType);
	    		bf.setDcId(dcId);
	    		
	    		toDelete.add(factorUnit);
	    		list.add(bf);
	        }
		}
		//删除需要删除的所有价格
		deletePriceByUnit(toDelete , dcId);
		return list;
	}
	/**
	 * 判断单元格是否在合并单元格内
	 * @Author: duanbinbin
	 * @param range
	 * @param row
	 * @param col
	 * @return
	 *<li>Date: 2017年3月23日</li>
	 */
	public Boolean isInMerge(Range[] range , int row ,int col){
		for(int a = 0;a < range.length;a++){
        	Cell cellTopLeft = range[a].getTopLeft();
        	Cell cellBottomRight = range[a].getBottomRight();
        	int rowTop = cellTopLeft.getRow();
        	int rowBottom = cellBottomRight.getRow();
        	int colLeft = cellTopLeft.getColumn();
        	int colRight = cellBottomRight.getColumn();
        	if(row >= rowTop && row <= rowBottom && col >= colLeft && col <= colRight){
        		return true;
        	}
        }
		return false;
	}
	/**
	 * 判断字符串是否为纯数字
	 * 用于判断价格区间（-1将单独判断）
	 * @Author: duanbinbin
	 * @param str
	 * @return
	 *<li>Date: 2017年3月22日</li>
	 */
	public Boolean isNum(String str){
		for (int i = 0; i < str.length(); i++){
			if (!Character.isDigit(str.charAt(i))){
				return false;
			}
		}
		return true;
	 }
	/**
	 * 判断字符串是否为非负浮点数，用于校验价格
	 * @Author: duanbinbin
	 * @param str
	 * @return
	 *<li>Date: 2017年3月22日</li>
	 */
	 public Boolean isRightPrice(String str){
	    Pattern pattern = Pattern.compile("^\\d+(\\.\\d+)?$"); 
	    Matcher isNum = pattern.matcher(str);
	    if( !isNum.matches() ){
	        return false; 
	    } 
	    return true; 
	 }
	/**
	 * 删除数据中心下需要删除的计费因子的所有价格
	 * @Author: duanbinbin
	 * @param toDelete
	 * @param dcId
	 *<li>Date: 2017年2月20日</li>
	 */
	private void deletePriceByUnit(HashSet<String> toDelete ,String dcId) {
		
        if(!toDelete.isEmpty()){
        	List<Object> params=new ArrayList<Object>();
        	StringBuffer sb = new StringBuffer();
            sb.append("delete BaseBillingFactor where dcId = ? and ( 1 <> 1 ");
            params.add(dcId);
            for(String factorUnit : toDelete){
            	sb.append(" or factorUnit = ? ");
            	params.add(factorUnit);
            }
            sb.append(" or 2 <> 2)");
            billingFactorDao.executeUpdate(sb.toString(), params.toArray());
            try {
            	for(String factorUnit : toDelete){
            		jedisUtil.delete(RedisKey.PRICE+dcId+":1:"+factorUnit);
        			jedisUtil.delete(RedisKey.PRICE+dcId+":2:"+factorUnit);
                }
    		} catch (Exception e) {
    			e.printStackTrace();
    		}
        }
        
	}

	@Override
	public String getDcNameById(String dcId) {
		return ecmcDataCenterService.getdatacenterName(dcId);
	}
}



