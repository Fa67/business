package com.eayun.ecmcdepartment.service.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.eayun.common.exception.AppException;
import com.eayun.common.util.BeanUtils;
import com.eayun.ecmcdepartment.dao.EcmcSysDepartmentDao;
import com.eayun.ecmcdepartment.model.BaseEcmcSysDepartment;
import com.eayun.ecmcdepartment.model.EcmcSysDepartment;
import com.eayun.ecmcdepartment.service.EcmcSysDepartmentService;
import com.eayun.ecmcuser.model.BaseEcmcSysUser;
import com.eayun.ecmcuser.util.EcmcSessionUtil;

@Service
@Transactional
public class EcmcSysDepartmentServiceImpl implements EcmcSysDepartmentService {

	private static final Logger log = LoggerFactory.getLogger(EcmcSysDepartmentServiceImpl.class);
	@Autowired
	private EcmcSysDepartmentDao ecmcDepartmentDao;
	
	@Override
	public EcmcSysDepartment addDepartment(EcmcSysDepartment department) throws AppException {
		log.info("添加机构");
		try {
			BaseEcmcSysDepartment baseEcmcSysDepartment = new BaseEcmcSysDepartment();
			BeanUtils.copyProperties(baseEcmcSysDepartment, department);
			baseEcmcSysDepartment.setCreateTime(new Date());
			BaseEcmcSysUser createUser = EcmcSessionUtil.getUser();
			if(createUser!=null){
				baseEcmcSysDepartment.setCreatedBy(createUser.getId());
			}
			baseEcmcSysDepartment = ecmcDepartmentDao.save(baseEcmcSysDepartment);
			BeanUtils.copyProperties(department, baseEcmcSysDepartment);
			return department;
		} catch (Exception e) {
		    log.error(e.getMessage(),e);
		}
		return null;
	}

	@Override
	public void delDepartment(String departmentId) throws AppException {
		log.info("删除机构");
		ecmcDepartmentDao.delete(departmentId);
	}

	@Override
	public EcmcSysDepartment updateDepartment(EcmcSysDepartment department) throws AppException {
		log.info("更新机构");
		BaseEcmcSysDepartment baseEcmcSysDepartment = new BaseEcmcSysDepartment();
		BeanUtils.copyPropertiesByModel(baseEcmcSysDepartment, department);
		ecmcDepartmentDao.saveOrUpdate(baseEcmcSysDepartment);
        return department;
	}

	@Override
	public List<EcmcSysDepartment> findAllDepartmentTreeGrid() throws AppException {
		log.info("查询所有的机构");
		List<BaseEcmcSysDepartment> baseList = ecmcDepartmentDao.findAllOrderByCreateTimeDesc();
		if(CollectionUtils.isEmpty(baseList)){
		    return Collections.<EcmcSysDepartment>emptyList();
		}
		//使用有序map，保持原有排序
		HashMap<String, EcmcSysDepartment> departMap = new LinkedHashMap<String, EcmcSysDepartment>();
		for (BaseEcmcSysDepartment baseEcmcSysDepartment : baseList) {
		    EcmcSysDepartment department = new EcmcSysDepartment();
		    BeanUtils.copyPropertiesByModel(department, baseEcmcSysDepartment);
		    departMap.put(baseEcmcSysDepartment.getId(), department);
        }
		return embedDepartment(departMap);
	}

	@Override
	public boolean checkDepartmentName(String departmentName) throws AppException {
		log.info("查询机构名称是否存在");
		BaseEcmcSysDepartment baseDepartment = ecmcDepartmentDao.findDepartmentByName(departmentName);
		if(baseDepartment==null){
			return true;
		}
		return false;
	}
	
	public boolean checkDepartCode(String code, String id) throws AppException {
	    log.info("检查机构编号是否重复");
	    return ecmcDepartmentDao.countDepartCode(code, id) > 0 ? true : false;
	}

	@Override
	public EcmcSysDepartment findDepartmentById(String departmentId) throws AppException {
		log.info("根据ID查询机构");
		try {
			BaseEcmcSysDepartment baseDepartment = ecmcDepartmentDao.findOne(departmentId);
			EcmcSysDepartment department = new EcmcSysDepartment();
			BeanUtils.copyProperties(department, baseDepartment);
			return department;
		} catch (Exception e) {
		    log.error(e.getMessage(),e);
		}
		return null;
	}

	@Override
	public List<EcmcSysDepartment> findAllDepartmentTree() throws AppException {
		log.info("查询全部机构的信息，只包含Id、机构名称、parentId、描述信息");
		List<BaseEcmcSysDepartment> baseList = ecmcDepartmentDao.findAllBaseInfo();
		//使用有序map，保持原有排序
		HashMap<String, EcmcSysDepartment> departMap = new LinkedHashMap<String, EcmcSysDepartment>();
		
		for (BaseEcmcSysDepartment baseDepartment : baseList) {
			EcmcSysDepartment department = new EcmcSysDepartment();
			try {
				BeanUtils.copyPropertiesByModel(department, baseDepartment);
			} catch (Exception e) {
			    log.error(e.getMessage(),e);
			}
			departMap.put(baseDepartment.getId(), department);
		}
		return embedDepartment(departMap);
	}
	

	/**
	 * 将机构对象嵌入父对象
	 * @param departMap 机构的Map集合，key为机构ID，value为机构对象EcmcSysDepartment
	 * @return
	 */
	private ArrayList<EcmcSysDepartment> embedDepartment(HashMap<String, EcmcSysDepartment> departMap) {
		ArrayList<EcmcSysDepartment> rootDepartment = new ArrayList<EcmcSysDepartment>();
		Set<Entry<String, EcmcSysDepartment>> entrySet = departMap.entrySet();
		// 遍历Map，将机构对象add到父机构的children集合属性中
		for (Iterator<Entry<String, EcmcSysDepartment>> it = entrySet.iterator(); it.hasNext();) {
			EcmcSysDepartment department = (EcmcSysDepartment) ((Map.Entry<String, EcmcSysDepartment>) it.next())
					.getValue();
			if (department.getParentId() == null || department.getParentId().equals("")) {
				rootDepartment.add(department); // 根节点
			} else {
				EcmcSysDepartment deaprtment = (EcmcSysDepartment) departMap.get(department.getParentId());
				if (deaprtment != null) {
					deaprtment.addChild(department);
				}
			}
		}
		return rootDepartment;
	}

	@Override
	public boolean hasChildren(String departmentId) {
		List<BaseEcmcSysDepartment> department = ecmcDepartmentDao.findByParentId(departmentId);
		if (department != null && department.size() > 0) {
			return true;
		}
		return false;
	}
	
}
