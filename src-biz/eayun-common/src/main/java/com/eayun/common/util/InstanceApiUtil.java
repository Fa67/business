package com.eayun.common.util;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.eayun.common.ConstantClazz;
import com.eayun.common.constant.ApiInstanceConstant;
import com.eayun.common.constant.PayType;
import com.eayun.common.tools.DictUtil;
import com.eayun.sys.model.SysDataTree;

public class InstanceApiUtil {
	
	/**
	 * 校验<code>uuid</code>是否是UUID格式<br>
	 * ---------------------------------
	 * @author zhouhaitao
	 * 
	 * @param uuid			待检验的UUID对象
	 * @param isContains	是否包含'-'
	 * 
	 * @return  是UUID格式 返回 true;否则返回 false
	 */
	public static boolean uuidRegex(String uuid , boolean isContains){
		String uuidRegex = ApiInstanceConstant.REGEX_UUID_CONSTAINS_;
		if(!isContains){
			uuidRegex = ApiInstanceConstant.REGEX_UUID_NOCONSTAINS_;
		}
		
		Pattern pattern = Pattern.compile(uuidRegex); 
		Matcher matcher = pattern.matcher(uuid);
		return matcher.matches();
	}
	
	/**
	 * 转义付费模式
	 * @param payType
	 * @return
	 */
	public static String escapePayType(String payType){
		String escapePayType = null;
		if(PayType.PAYBEFORE.equals(payType)){
			escapePayType = ApiInstanceConstant.PAYTYPE_MONTH;
		}
		else if(PayType.PAYAFTER.equals(payType)){
			escapePayType = ApiInstanceConstant.PAYTYPE_DYNAMIC;
		}
		return escapePayType;
		
	}
	
	public static boolean checkCpuAndMemory(String cpu,String memory){
		boolean flag = false;
		Map<String,SysDataTree> cpuMap = new HashMap<String,SysDataTree>();
		List<SysDataTree> cpuList = DictUtil.getDataTreeByParentId(ConstantClazz.DICT_CLOUD_CPU_TYPE_NODE_ID);
		for(SysDataTree sdt :cpuList){
			String nodeName = sdt.getNodeName();
			String cpuStr = nodeName.substring(0, nodeName.length()-1);
			cpuMap.put(cpuStr, sdt);
		}
		if(cpuMap.containsKey(cpu)){
			SysDataTree ramNode = cpuMap.get(cpu);
			List<SysDataTree> memoryList = DictUtil.getDataTreeByParentId(ramNode.getNodeId());
			Map<String,SysDataTree> memoryMap = new HashMap<String,SysDataTree>();
			for(SysDataTree sdt :memoryList){
				String nodeName = sdt.getNodeName();
				String memoryStr = nodeName.substring(0, nodeName.length()-2);
				memoryMap.put(memoryStr, sdt);
			}
			if(memoryMap.containsKey(memory)){
				flag = true;
			}
		}
		return flag;
	}
	
}
