package com.eayun.api.test;

import org.testng.Reporter;
import org.testng.annotations.Test;

public class Api1Test extends AbstrackApiTests {
	@Test(dependsOnGroups={"login"})
	public void api1() {
		Reporter.log("login result:" + LoginTest.loginResult, true);
	}
}
