package com.eayun.common.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

import ch.ethz.ssh2.Connection;
import ch.ethz.ssh2.Session;

public class SSHUtil {

	private static Map<String, Connection> connMap = new HashMap<String, Connection>();

	/**
	 * 得到SSH链接，若登录失败，则返回null
	 * 
	 * @param hostName
	 * @param user
	 * @param password
	 * @return
	 * @throws Exception
	 */
	public static Connection getConnection(String hostName, String user,
			String password) throws Exception {
		if (StringUtil.isEmpty(hostName) || StringUtil.isEmpty(user)
				|| StringUtil.isEmpty(password)) {
			return null;
		}
		String key = hostName + user + password;
		Connection conn = connMap.get(key);
		if (conn == null) {
			conn = new Connection(hostName);
			try {
				conn.connect();
				connMap.put(key, conn);
			} catch (Exception e) {
				conn = null;
				throw e;
			}
		}
		if (!conn.isAuthenticationComplete()) {
			boolean auth = conn.authenticateWithPassword(user, password);
			if (!auth) {
				connMap.remove(key);
				conn.close();
				return null;
			}
		}
		return conn;
	}

	/**
	 * 执行命令返回结果
	 * 
	 * @param conn
	 * @param command
	 * @return
	 * @throws Exception
	 */
	public static String executeCommand(Connection conn, String command)
			throws Exception {
		Session ssh = conn.openSession();
		ssh.execCommand(command);

		InputStream in = ssh.getStdout();
		BufferedReader br = new BufferedReader(new InputStreamReader(in));
		StringBuffer sbf = new StringBuffer();
		try {
			String temp = null;
			while ((temp = br.readLine()) != null) {
				sbf.append(temp);
			}

		} catch (IOException e) {
			throw e;
		} finally {
			br.close();
			in.close();
			ssh.close();
		}
		return sbf.toString();
	}
}
