package com.eayun.obs.service;

import java.util.List;

public interface ObsGetAllUsersService {
	/**
	 * 获得obs所有的客户Id
	 * @return
	 * @throws Exception
	 */
	public List<String> getObsAllUsers() throws Exception;

}
