package com.eayun.syssetup.service.impl;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.eayun.common.Dictionary;
import com.eayun.common.tools.DictUtil;
import com.eayun.sys.model.SysDataTree;
import com.eayun.syssetup.service.SysDataTreeService;

/**
 * Created by Administrator on 2016/8/22.
 */
@Transactional
@Service
public class SysDataTreeServiceImpl implements SysDataTreeService {
    @Override
    public String getBuyCondition() {
        SysDataTree sysDataTree=DictUtil.getDataTreeByNodeId(Dictionary.buyNodeId);
        return sysDataTree.getPara1();
    }

    @Override
    public String getRenewCondition() {
        SysDataTree sysDataTree=DictUtil.getDataTreeByNodeId(Dictionary.renewNodeId);
        return sysDataTree.getPara1();
    }

    @Override
    public String getRecoveryTime() {
        SysDataTree sysDataTree=DictUtil.getDataTreeByNodeId(Dictionary.recoveryNodeId);
        return sysDataTree.getPara1();
    }

    @Override
    public String getRetainTime() {
        SysDataTree sysDataTree=DictUtil.getDataTreeByNodeId(Dictionary.retainNodeId);
        return sysDataTree.getPara1();
    }
}
