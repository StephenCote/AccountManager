package org.cote.accountmanager.client.test;

import java.util.Map;

import org.cote.accountmanager.client.services.ScriptService;
import org.cote.accountmanager.exceptions.ArgumentException;
import org.cote.accountmanager.exceptions.DataException;
import org.cote.accountmanager.objects.DataType;
import org.cote.accountmanager.util.DataUtil;
import org.junit.Test;

public class TestScriptEngine extends BaseClientTest {

	@Test
	public void TestScriptSetup() {
		DataType scriptData = new DataType();
		try {
			Map<String,Object> params = ScriptService.getCommonParameterMap(testUserContext);
			DataUtil.setValue(scriptData, getTestScript("testScriptApiBasic.js").getBytes());
			scriptData.setUrn("debug");
			ScriptService.run(params, scriptData);

		} catch (DataException | ArgumentException e) {
			logger.error(e);

		}

	}
	
}
