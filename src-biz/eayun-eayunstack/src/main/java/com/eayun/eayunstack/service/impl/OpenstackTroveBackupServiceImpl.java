package com.eayun.eayunstack.service.impl;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.eayun.common.exception.AppException;
import com.eayun.common.util.StringUtil;
import com.eayun.eayunstack.model.RDSBackup;
import com.eayun.eayunstack.service.OpenstackTroveBackupService;
import com.eayun.eayunstack.util.OpenstackUriConstant;
import com.eayun.eayunstack.util.RestTokenBean;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * 云数据库与底层交互的Service实现，除了备份状态同步，其他同步均不涉及
 * @author fan.zhang.
 */
@Service
public class OpenstackTroveBackupServiceImpl extends OpenstackBaseServiceImpl<RDSBackup> implements OpenstackTroveBackupService {
    private static final Logger log = LoggerFactory.getLogger(OpenstackTroveBackupServiceImpl.class);

    @Deprecated
    @Override
    public List<RDSBackup> listAll(String datacenterId) throws AppException {
        //do nothing
        return null;
    }

    @Override
    public List<RDSBackup> list(String datacenterId, String projectId) throws AppException {
        if(StringUtils.isEmpty(projectId)){
            return null;
        }
        RestTokenBean restTokenBean = getRestTokenBean(datacenterId, projectId,OpenstackUriConstant.TROVE_SERVICE_URI);
        restTokenBean.setUrl(OpenstackUriConstant.RDS_BACKUPS_URI);
        JSONObject resp =  restService.getResponse(restTokenBean);
        log.info("response of listing all backups by datacenter and project = "+resp.toJSONString());
        JSONArray array = resp.getJSONArray("backups");
        List<RDSBackup> rdsBackups = new ArrayList<>();
        for(int i=0; i<array.size(); i++){
            JSONObject json = array.getJSONObject(i);
            RDSBackup rdsBackup = restService.json2bean(json, RDSBackup.class);
            rdsBackups.add(rdsBackup);
        }
        return rdsBackups;
    }

    @Override
    public RDSBackup getById(String datacenterId, String projectId, String backupId) throws AppException {
        //根据backupId查备份详情
        RestTokenBean restTokenBean = getRestTokenBean(datacenterId, projectId,OpenstackUriConstant.TROVE_SERVICE_URI);
        restTokenBean.setUrl(OpenstackUriConstant.RDS_BACKUPS_URI+"/"+backupId);
        JSONObject responseBody =  restService.getResponse(restTokenBean);
        JSONObject json = responseBody.getJSONObject("backup");
        RDSBackup rdsBackup = restService.json2bean(json, RDSBackup.class);
        return rdsBackup;
    }

    @Override
    public RDSBackup create(String datacenterId, String projectId, JSONObject requestBody) throws AppException {
        RestTokenBean restTokenBean = getRestTokenBean(datacenterId, projectId,OpenstackUriConstant.TROVE_SERVICE_URI);
        restTokenBean.setUrl(OpenstackUriConstant.RDS_BACKUPS_URI);
        JSONObject responseBody = restService.post(restTokenBean, requestBody);
        JSONObject json = responseBody.getJSONObject("backup");
        RDSBackup rdsBackup = restService.json2bean(json, RDSBackup.class);
        return rdsBackup;
    }

    @Override
    public boolean delete(String datacenterId, String projectId, String backupId) throws AppException {
        RestTokenBean restTokenBean = getRestTokenBean(datacenterId, projectId,OpenstackUriConstant.TROVE_SERVICE_URI);
        restTokenBean.setUrl(OpenstackUriConstant.RDS_BACKUPS_URI+"/"+backupId);

        boolean resp =  restService.delete(restTokenBean);
        return resp;
    }

    public boolean delete(RestTokenBean bean) throws AppException {
        if (StringUtil.isEmpty(bean.getTokenId())) {
            bean = restService.getToken(bean);
        }
        HttpClient httpclient = HttpClients.createDefault();
        HttpDelete delete = new HttpDelete(bean.getEndpoint()+bean.getUrl());
        log.info("The current URL is:" + bean.getEndpoint()+bean.getUrl());
        String resData = null;
        JSONObject resJson = null;
        int statusCode = 0;
        try {
            delete.setHeader("Content-Type", "application/json");
            delete.setHeader("Accept", "application/json");
            if (bean.getTokenId() != null) {
                delete.setHeader("X-Auth-Token", bean.getTokenId());
            }
            HttpResponse res = httpclient.execute(delete);
            log.info("HttpResponse=" + res);
            statusCode = res.getStatusLine().getStatusCode();
            HttpEntity resEntity = res.getEntity();
            if (resEntity != null) {
                resData = EntityUtils.toString(res.getEntity());
                if (resData != null) {
                    resJson = JSONObject.parseObject(resData);
                }
            }
            log.info("The response Json=" + resJson);
            //增加404判断，如果删除底层备份http状态码为404，则不向上抛异常，认为删除成功
            if (statusCode >= 300 && statusCode!=404) {
                StringBuilder message = null;
                if (resJson != null) {
                    message = new StringBuilder();
                    Map map = (Map) resJson;
                    Iterator iterator = map.keySet().iterator();
                    while (iterator.hasNext()) {
                        String key = (String) iterator.next();
                        JSONObject value = (JSONObject) JSONObject.toJSON(map.get(key));
                        message.append(key).append("-->").append(value.getString("message"));
                    }
                }
                String info = message == null ? "操作openstack平台时出错，请检查！" : message.toString();
                throw new AppException("error.openstack.message", new String[] { info });
            }
        } catch (AppException e) {
            log.error(e.getMessage(), e);
            throw e;
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            throw new AppException("error.openstack.message", new String[]{statusCode+","+e.getMessage()});
        }
        return true;
    }

    @Deprecated
    @Override
    public RDSBackup update(String datacenterId, String projectId, JSONObject data, String id) throws AppException {
        //do nothing
        return null;
    }
}
