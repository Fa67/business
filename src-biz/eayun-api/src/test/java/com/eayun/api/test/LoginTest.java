package com.eayun.api.test;

import org.testng.annotations.Test;

public class LoginTest extends AbstrackApiTests {

	public static String loginResult = "";
	
	@Test(groups="login")
	public void testLogin() {
		loginResult = "user:111, token:abc";
	}
	
}
