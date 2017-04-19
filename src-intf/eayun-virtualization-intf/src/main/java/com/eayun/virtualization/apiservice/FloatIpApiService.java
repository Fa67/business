package com.eayun.virtualization.apiservice;

import java.util.List;

import org.springframework.transaction.annotation.Transactional;

import com.eayun.common.exception.AppException;
import com.eayun.virtualization.model.CloudFloatIp;
import com.eayun.virtualization.model.CloudOrderFloatIp;


public interface FloatIpApiService {
	
	 /**
     * 公网IP解绑资源
     * 
     * @author chengxiaodong
     * @param floatIp
     * @return
     */
    public CloudFloatIp unbundingResource(CloudFloatIp floatIp);
    

    /**
     * 解除已删除云主机与弹性公网IP的关系
     * @param vmId
     */
	public void refreshFloatIpByVm(String vmId);
	
	
	/**
	 * 创建公网IP的订单<br>
	 * @desc 暂时只是购买云主机调用<br>
	 * -------------------------
	 * 
	 * @author zhouhaitao
	 * @param cloudOrderFloatIp 		公网IP的订单
	 */
	public void createFloatIpOrder (CloudOrderFloatIp cloudOrderFloatIp);
	
	/**
	 * 根据订单创建弹性公网IP<br>
	 * @desc 暂时 只提供给购买云主机购买公网IP使用<br>
	 * ---------------------------------------
	 * 
	 * @author zhouhaitao
	 * @param orderNo			订单编号
	 * 
	 * @return					返回创建成功的公网IP列表
	 */
	public List<CloudFloatIp> createFloatIpByOrderno(String orderNo);
	
	/**
	 * 根据订单或者批量释放弹性公网IP
	 *
	 * @param orderNo
	 * @return
	 */
	public void releaseFloatIpByOrderNo(String orderNo) throws Exception;
	
	/**
     * 公网IP绑定资源
     *
     * @param floatIp
     * @return
     * @author zhouhaitao
     */
    @Transactional(noRollbackFor=AppException.class)
    public CloudFloatIp bindResource(CloudFloatIp floatIp);
    
    
    public void sendMessage(List<CloudFloatIp> cloudFloatIpList, String messKey, String cusId, String orderNo) throws Exception;
	
}
