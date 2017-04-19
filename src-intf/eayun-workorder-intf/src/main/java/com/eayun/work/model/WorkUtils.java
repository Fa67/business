package com.eayun.work.model;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.eayun.common.tools.DictUtil;
import com.eayun.sys.model.SysDataTree;


public class WorkUtils {
    
    private static Object lock = new Object();
	
	private static Calendar c = Calendar.getInstance(); 
	/**
	 * 系统超级管理员角色
	 */
	public static String ROOT = "root";
	/**
	 * 管理员
	 */
	public static String ADMIN = "admin";
	/**
	 * 运维人员
	 */
	public static String CPIS = "1505181604430";
	/**
	 * 客服人员
	 */
	public static String ESQ = "1503251002230";
	/**
	 * 商务
	 */
	public static String SW = "1511251031560";
	
	/**
	 * 工单级别的父类id
	 */
	public static String LEVELPARID = "0007001001"; 
	/**
	 * 普通工单的父类id
	 */
	public static String ORDTYPEPARID = "0007001002"; 
	/**
	 *  特殊工单的父类id
	 */
	public static String SPETYPEPARID = "0007001003"; 
	/**
	 * 注册类id
	 */
	public static String REGISTERTYPE="0007001003002";
	/**
	 * 配额类id
	 */
	public static String QUOTATYPE = "0007001003001"; 
	//ecmc状态List
	private static List<SysDataTree> ecmcFlagList=null;
	private static List<SysDataTree> ecmcDoneFlagList=null;
	private static List<SysDataTree> ecmcNoDoneFlagList=null;
	//ecmc状态Map
	private static Map<String, String> ecmcFlagMap=null;
	//ecsc状态List
	private static List<SysDataTree> ecscFlagList =null;
	//ecsc状态Map
	private static Map<String, String> ecscFlagMap=null;
	
	/**
	 * ecsc---mail---Title
	 * @return
	 */
	public static Map<String,String> getEcscTitleMap(){
		Map<String, String> map = new HashMap<String, String>();
		map.put("0", "创建工单");
		map.put("2", "待反馈工单");
		map.put("3", "待确认工单");
		map.put("4", "待评价工单");
		map.put("5", "删除工单");
		return map;
	}
	/**
	 * ecmc---mail---Title
	 * @return
	 */
	public static Map<String,String> getEcmcTitleMap(){
		Map<String, String> map = new HashMap<String, String>();
		map.put("0", "创建工单");
		map.put("1", "处理工单");
		map.put("2", "解决工单");
		map.put("3", "完成工单");
		map.put("4", "取消工单");
		return map;
	}
	/**
	 * ecmc---log---name
	 * @return
	 */
	public static Map<String,String> getLogNameMap(){
		Map<String, String> map = new HashMap<String, String>();
		map.put("1", "处理工单");
		map.put("2", "解决工单");
		map.put("3", "完成工单");
		map.put("4", "取消工单");
		return map;
	}
	/**
	 * ecmc状态Map
	 */
	public static Map<String, String> getEcmcFlagMap() {
        synchronized (lock) {
            if(ecmcFlagMap==null){
                ecmcFlagMap=new HashMap<String, String>();
                ecmcFlagMap.put("0", "待处理");
                ecmcFlagMap.put("1", "处理中");
                ecmcFlagMap.put("2", "已解决");
                ecmcFlagMap.put("3", "已完成");
                ecmcFlagMap.put("4", "已取消");
            }
        }
    
		
		return ecmcFlagMap;
	}
	/**
	 * ecmc状态List(all)
	 */
	public static List<SysDataTree> getEcmcFlagList() {
        synchronized (lock) {
            if(ecmcFlagMap==null){
                ecmcFlagMap=getEcmcFlagMap();
            }
        }
    
        synchronized (lock) {
            if(ecmcFlagList ==null){
                ecmcFlagList=new ArrayList<SysDataTree>();
                for (int i = 0; i < 6; i++) {
                    SysDataTree sys = new SysDataTree();
                    sys.setNodeId(String.valueOf(i));
                    sys.setNodeName(ecmcFlagMap.get(String.valueOf(i)));
                    ecmcFlagList.add(sys);
                }
            }
        }
    
		return ecmcFlagList;
	}
	/**
	 * ecmc状态List(未完成)
	 */
	public static List<SysDataTree> getNoDoneFlagList(){
        synchronized (lock) {
            if(ecmcFlagMap==null){
                ecmcFlagMap=getEcmcFlagMap();
            }
        }
        synchronized (lock) {
            if(ecmcNoDoneFlagList ==null){
                ecmcNoDoneFlagList=new ArrayList<SysDataTree>();
                for (int i = 0; i < 3; i++) {
                    SysDataTree sys = new SysDataTree();
                    sys.setNodeId(String.valueOf(i));
                    sys.setNodeName(ecmcFlagMap.get(String.valueOf(i)));
                    ecmcNoDoneFlagList.add(sys);
                }
            }
        }
    
		return ecmcNoDoneFlagList;
	}
	/**
	 * ecmc状态List(已完成)
	 */
	public static List<SysDataTree> getDoneFlagList(){
	    synchronized (lock){
	        if(ecmcFlagMap==null){
	            ecmcFlagMap=getEcmcFlagMap();
	        }
	    }
        synchronized (lock) {
            if(ecmcDoneFlagList ==null){
                  ecmcDoneFlagList=new ArrayList<SysDataTree>();
                    for (int i = 3; i < 5; i++) {
                        SysDataTree sys = new SysDataTree();
                        sys.setNodeId(String.valueOf(i));
                        sys.setNodeName(ecmcFlagMap.get(String.valueOf(i)));
                        ecmcDoneFlagList.add(sys);
                    }
            }
        }
    
		return ecmcDoneFlagList;
	}
	/**
	 * ecsc状态Map
	 */
	public static Map<String, String> getEcscFlagMap() {
        synchronized (lock) {
            if(ecscFlagMap==null){
                ecscFlagMap=new HashMap<String, String>();
                ecscFlagMap.put("0", "待受理");
                ecscFlagMap.put("1", "处理中");
                ecscFlagMap.put("2", "待反馈");
                ecscFlagMap.put("3", "待确认");
                ecscFlagMap.put("4", "待评价");
                ecscFlagMap.put("5", "已关闭");
                ecscFlagMap.put("6", "已删除");
                ecscFlagMap.put("7", "已取消");
            }
        }
    
		return ecscFlagMap;
	}
	/**
	 * ecsc状态List
	 */
	public static List<SysDataTree> getEcscFlagList(){
        synchronized (lock) {
            if(ecscFlagMap==null){
                ecscFlagMap=getEcmcFlagMap();
            }
        }
        synchronized (lock) {
            if(ecscFlagList ==null){
                ecscFlagList=new ArrayList<SysDataTree>();
                for (int i = 0; i < 8; i++) {
                    SysDataTree sys = new SysDataTree();
                    sys.setNodeId(String.valueOf(i));
                    sys.setNodeName(ecscFlagMap.get(String.valueOf(i)));
                    ecscFlagList.add(sys);
                }
            }
        }
    
		return ecscFlagList;
	}
	/**
	 * 根据数据父类id获取数据字典Map
	 * @param parentId
	 * @return
	 */
	public static  Map<String, String> getDateTreeMap(String parentId) {
		List<SysDataTree> dataList = getDataTreeList(parentId);
		Map<String, String> map = new HashMap<>();
		if (dataList != null && dataList.size() > 0) {
			for (SysDataTree sysDataTree : dataList) {
				map.put(sysDataTree.getNodeId(), sysDataTree.getNodeName());
			}
		}
		return map;
	}
	/**
	 * 获取所有工单类别Map
	 * @return
	 */
	public static Map<String,String> getAllSysTreeMap(){
		Map<String,String> dataTreeMap =new HashMap<String,String>();
		dataTreeMap.putAll(getDateTreeMap(WorkUtils.ORDTYPEPARID));//普通类型
		dataTreeMap.putAll(getDateTreeMap(WorkUtils.SPETYPEPARID));//特殊类型
		return dataTreeMap;
	}
	
	/**
	 * 根据数据父类id获取数据字典List
	 * @param parentId
	 * @return
	 */
	public static List<SysDataTree> getDataTreeList(String parentId) {
		List<SysDataTree> dataList = new ArrayList<SysDataTree>();
		String parentIds[]=parentId.split(",");
		if(parentIds.length>1){
			for (int i=0;i< parentIds.length;i++) {
				dataList.addAll(DictUtil.getDataTreeByParentId(parentIds[i]));
			}
		}else{
			dataList = DictUtil.getDataTreeByParentId(parentId);
		}
		return dataList;
	}
	/**
	 * 工单级别对应的时间
	 * @return
	 */
	public static Map<String,Integer> getLevelMap(){
		Map<String,Integer> map = new HashMap<String,Integer>();
		map.put("0007001001005", 5*60*1000);//零级
		map.put("0007001001001", 5*60*1000);//一级
		map.put("0007001001002", 10*60*1000);//二级
		map.put("0007001001003", 15*60*1000);//三级
		map.put("0007001001004", 30*60*1000);//四级
		return map;
	}
	/**
	 * 当前时间的前一周开始时间
	 * @return
	 */
	public static synchronized Date getWeekBegTime(Date date) {
		c.setTime(date);   
		c.add(Calendar.DATE, -7);   
		return c.getTime();
	}
	
	/**
	 * 当前时间的前一个月开始时间
	 * @return
	 */
	public static synchronized Date getMonthBegTime(Date date) {
		c.setTime(date);   
		c.add(Calendar.MONTH, -1);   
		return c.getTime();
	}
	/**
	 * 当前时间的前一年开始时间
	 * @return
	 */
	public static synchronized Date getYearBegTime(Date date) {
		c.setTime(date);   
		c.add(Calendar.YEAR, -1);   
		return c.getTime();
	}
}
