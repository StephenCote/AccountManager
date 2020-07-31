package org.cote.accountmanager.client.test;

import java.util.HashMap;
import java.util.Map;

import javax.script.ScriptEngineFactory;
import javax.script.ScriptEngineManager;

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
			Map<String,Object> params = ScriptService.getCommonParameterMap(testUser);
			DataUtil.setValue(scriptData, getDataScript().getBytes());
			scriptData.setUrn("debug");
			ScriptService.run(testUser, params, scriptData);

		} catch (DataException | ArgumentException e) {
			logger.error(e);

		}

	}
	
	private String getDataScript() {
		return "console.log(\"Script Test\"); console.log(\"User: \" + user);";
	}
}
