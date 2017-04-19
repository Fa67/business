package com.eayun.virtualization.ecmcservice;

import java.util.List;

import org.springframework.stereotype.Service;

import com.eayun.common.dao.QueryMap;
import com.eayun.common.dao.support.Page;
import com.eayun.common.exception.AppException;
import com.eayun.virtualization.ecmcvo.CloudSecretKeyVoe;
import com.eayun.virtualization.ecmcvo.SecretKeyListVoe;
import com.eayun.virtualization.ecmcvo.SecretKeyVm;
import com.eayun.virtualization.model.BaseSecretkeyVm;

@Service
public interface EcmcSecretKeyService {
	
	public Page getecscsecretkeylist(QueryMap queryMap,String prjid,String dcid,String cusid,String queryName) throws AppException;

	public CloudSecretKeyVoe getSrcretKeyById(String skid)throws AppException;
	
	public Page getSrcretKeyByIdAndVmList(String skid,QueryMap queryMap)throws AppException;
	
}
