package com.eayun.generator.service;

import com.eayun.customer.model.Customer;
import com.eayun.virtualization.model.CloudProject;

import java.util.Map;

/**
 * Created by ZH.F on 2016/12/21.
 */
public interface CustomerGeneratorService {
    Map<String,Object> createProject(CloudProject cloudProject, Customer customer, boolean b) throws Exception;

    void bulkCreateOrders() throws Exception;

    void bulkCreateTradeRecords() throws Exception ;

    void bulkCreateAlarmMessage();
}
