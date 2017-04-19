package com.eayun.eayunstack.service.impl;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.util.Base64Utils;

import com.alibaba.fastjson.JSONObject;
import com.eayun.common.exception.AppException;
import com.eayun.eayunstack.model.KeyPairs;
import com.eayun.eayunstack.model.VmUserData;
import com.eayun.eayunstack.service.OpenstackSecretkeyService;
import com.eayun.eayunstack.util.OpenstackUriConstant;
import com.eayun.eayunstack.util.RestTokenBean;

/**
 * @author jingang.liu@eayun.com
 * @Dadte 2017年2月24日
 */
@Service
public class OpenstackSecretkeyServiceImpl extends OpenstackBaseServiceImpl<KeyPairs> implements OpenstackSecretkeyService {

    
    @Override
    public List<KeyPairs> listAll(String datacenterId) throws AppException {
        // TODO Auto-generated method stub
        return null;
    }

    /**
     * 项目下的所有密钥
     */
    @Override
    public List<KeyPairs> list(String datacenterId, String projectId) throws AppException {
        List<KeyPairs> list = null;

        RestTokenBean restTokenBean = getRestTokenBean(datacenterId,OpenstackUriConstant.COMPUTE_SERVICE_URI);
        restTokenBean.setUrl(OpenstackUriConstant.SSH_SECRETKEY_URL);
        List<JSONObject> result = null;
        result = restService.list(restTokenBean,OpenstackUriConstant.SSH_SECRETKEY_DATA_NAME);
        if (result != null && result.size() > 0) {
            for (JSONObject jsonObject : result) {
                if (list == null) {
                    list = new ArrayList<KeyPairs>();
                }
                KeyPairs data = restService.json2bean(jsonObject, KeyPairs.class);
                list.add(data);
            }
        }

        return list;
    }

    @Override
    public KeyPairs getById(String datacenterId, String projectId, String id) throws AppException {
        // TODO Auto-generated method stub
        return null;
    }
    /**
     * 获取详情
     */
    @Override
    public KeyPairs getByName(String datacenterId, String projectId, String secretkeyName) throws AppException {
        KeyPairs object = null;
        RestTokenBean restTokenBean = getRestTokenBean(datacenterId,OpenstackUriConstant.COMPUTE_SERVICE_URI);
        restTokenBean.setUrl(OpenstackUriConstant.SSH_SECRETKEY_URL + "/"+secretkeyName);
        JSONObject json = restService.get(restTokenBean,OpenstackUriConstant.SSH_SECRETKEY_DETAILS_NAME);
        if (json != null) {
            // 转换成java对象
            object = restService.json2bean(json, KeyPairs.class);
        }
        return object;
    }

    /**
     * 创建密钥
     */
    @Override
    public KeyPairs create(String datacenterId, String projectId, JSONObject data) throws AppException {
        KeyPairs object = null;
        RestTokenBean restTokenBean = getRestTokenBean(datacenterId, projectId,OpenstackUriConstant.COMPUTE_SERVICE_URI);
        restTokenBean.setUrl(OpenstackUriConstant.SSH_SECRETKEY_URL);
        JSONObject result = restService.create(restTokenBean, OpenstackUriConstant.SSH_SECRETKEY_DETAILS_NAME, data);
        object = restService.json2bean(result, KeyPairs.class);

        return object;
    }

    /**
     * 删除密钥
     */
    @Override
    public boolean delete(String datacenterId, String projectId, String secretkeyName) throws AppException {
        RestTokenBean restTokenBean = getRestTokenBean(datacenterId, projectId,OpenstackUriConstant.COMPUTE_SERVICE_URI);
        restTokenBean.setUrl(OpenstackUriConstant.SSH_SECRETKEY_URL + "/" + secretkeyName);
        return restService.delete(restTokenBean);
    }

    /**
     * 修改密钥
     */
    @Override
    public KeyPairs update(String datacenterId, String projectId, JSONObject data, String secretkeyName) throws AppException {
        KeyPairs object = null;
        RestTokenBean restTokenBean = getRestTokenBean(datacenterId,OpenstackUriConstant.COMPUTE_SERVICE_URI);
        restTokenBean.setUrl(OpenstackUriConstant.SSH_SECRETKEY_URL + "/" + secretkeyName);

        JSONObject result = restService.update(restTokenBean,OpenstackUriConstant.SSH_SECRETKEY_DETAILS_NAME, data);
        object = restService.json2bean(result, KeyPairs.class);
    
        return object;
    }
    
    @Override
    public String getVmUserData(String datacenterId, String projectId, String vm_id) throws AppException {
        RestTokenBean restTokenBean = getRestTokenBean(datacenterId,OpenstackUriConstant.COMPUTE_SERVICE_URI);
        restTokenBean.setUrl(OpenstackUriConstant.SSH_SECRETKEY_BIND_URL + "/" + vm_id);
        JSONObject result = restService.get(restTokenBean, null);
        String data = result.getString(OpenstackUriConstant.SSH_SECREKEY_USERDATA_NAME);
        String s = null;
        try {
            byte[] bytes = Base64Utils.decodeFromString(data);
            s = new String(bytes, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            throw new AppException("解码失败："+e.getMessage());
        }
        return s;
    }
    @Override
    public String bindSecretKey(String datacenterId, String projectId, String vm_id, JSONObject data) throws AppException {
        RestTokenBean restTokenBean = getRestTokenBean(datacenterId,OpenstackUriConstant.COMPUTE_SERVICE_URI);
        restTokenBean.setUrl(OpenstackUriConstant.SSH_SECRETKEY_BIND_URL + "/" + vm_id);
        JSONObject result = restService.update(restTokenBean,null, data);
        String resultdata = result.getString(OpenstackUriConstant.SSH_SECREKEY_USERDATA_NAME);
        String s = null;
        try {
            byte[] bytes = Base64Utils.decodeFromString(resultdata);
            s = new String(bytes, "UTF-8");
            JSONObject mdata = new JSONObject();
            mdata.put("reset_sshkeys", System.currentTimeMillis()+"");
            JSONObject resultData = new JSONObject();
            resultData.put("metadata", mdata);
            updateVmMetadata(datacenterId, projectId, vm_id, resultData);
        } catch (UnsupportedEncodingException e) {
            throw new AppException("解码失败："+e.getMessage());
        }
        return s;
    }
    
    @Override
    public String getVmMetadata(String datacenterId, String projectId, String vm_id) throws AppException {
        RestTokenBean restTokenBean = getRestTokenBean(datacenterId,OpenstackUriConstant.COMPUTE_SERVICE_URI);
        restTokenBean.setUrl(OpenstackUriConstant.VM_URI + "/" + vm_id + OpenstackUriConstant.SSH_METADATA_URL);
        JSONObject result = restService.get(restTokenBean, OpenstackUriConstant.SSH_METADATA_DATA_NAME);
        return result.toJSONString();
    }
    
    @Override
    public String updateVmMetadata(String datacenterId, String projectId, String vm_id, JSONObject data)
            throws AppException {
        RestTokenBean restTokenBean = getRestTokenBean(datacenterId,OpenstackUriConstant.COMPUTE_SERVICE_URI);
        restTokenBean.setUrl(OpenstackUriConstant.VM_URI + "/" + vm_id + OpenstackUriConstant.SSH_METADATA_URL );
        JSONObject result = restService.create(restTokenBean, OpenstackUriConstant.SSH_METADATA_DATA_NAME, data);
        return result.toJSONString();
    }
    
}
