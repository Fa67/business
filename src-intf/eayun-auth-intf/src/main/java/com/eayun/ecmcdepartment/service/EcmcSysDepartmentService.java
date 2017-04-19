package com.eayun.ecmcdepartment.service;

import java.util.List;

import com.eayun.common.exception.AppException;
import com.eayun.ecmcdepartment.model.EcmcSysDepartment;

public interface EcmcSysDepartmentService {

	/**
	 * 添加机构信息
	 * @param 机构对象
	 * @return
	 */
	public EcmcSysDepartment addDepartment(EcmcSysDepartment department) throws AppException;
	
	/**
	 * 删除机构信息
	 * @param 机构id
	 * @return
	 */
	public void delDepartment(String departmentId) throws AppException;
	
	/**
	 * 更新机构信息
	 * @param 机构对象
	 * @return
	 */
	public EcmcSysDepartment updateDepartment(EcmcSysDepartment department) throws AppException;
	
	/**
	 * 查询全部机构信息
	 * @param 
	 * @return
	 */
	public List<EcmcSysDepartment> findAllDepartmentTreeGrid() throws AppException;
	
	
	/**
	 * 查询全部机构的信息，只包含Id、机构名称、parentId、描述信息
	 * @return
	 * @throws AppException
	 */
	public List<EcmcSysDepartment> findAllDepartmentTree() throws AppException;
	
	/**
	 * 验证机构名称
	 * @param 
	 * @return
	 */
	public boolean checkDepartmentName(String departmentName) throws AppException;
	
	/**
	 * 检查机构编号是否重复
	 * @param code
	 * @param id
	 * @return
	 * @throws AppException
	 */
	public boolean checkDepartCode(String code, String id) throws AppException;
	
	/**
	 * 根据机构名称查询
	 * @param 
	 * @return
	 */
	public EcmcSysDepartment findDepartmentById(String departmentId) throws AppException;
	
	/**
	 * 判断节机构下是否有子机构
	 * @return
	 */
	public boolean hasChildren(String departmentId);
	
}
