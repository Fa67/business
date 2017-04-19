package com.eayun.monitor.model;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

import org.hibernate.annotations.GenericGenerator;

/**
 * 运维报警规则-联系人
 *                       
 * @Filename: BaseEcmcAlarmContact.java
 * @Description: 
 * @Version: 1.0
 * @Author: duanbinbin
 * @Email: binbin.duan@eayun.com
 * @History:<br>
 *<li>Date: 2016年3月24日</li>
 *<li>Version: 1.0</li>
 *<li>Content: create</li>
 *
 */
@Entity
@Table(name = "ecmc_alarmcontact")
public class BaseEcmcAlarmContact implements Serializable {

	/**
	 *Comment for <code>serialVersionUID</code>
	 */
	private static final long serialVersionUID = 5151208957459386437L;

	@Id
    @GeneratedValue(generator = "system-uuid")
    @GenericGenerator(name = "system-uuid", strategy = "uuid")
    @Column(name = "ac_id", unique = true, nullable = false, length = 32)
    private String id;
    
	@Column(name = "ac_alarmruleid", length = 32)
    private String alarmRuleId;	//报警规则id
    
	@Column(name = "ac_contactid", length = 32)
    private String contactId;	//联系人id

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getAlarmRuleId() {
        return alarmRuleId;
    }

    public void setAlarmRuleId(String alarmRuleId) {
        this.alarmRuleId = alarmRuleId;
    }

    public String getContactId() {
        return contactId;
    }

    public void setContactId(String contactId) {
        this.contactId = contactId;
    }
}
