package org.cote.accountmanager.data;

import static org.junit.Assert.assertNotNull;

import org.apache.log4j.Logger;
import org.cote.accountmanager.data.services.BshService;
import org.cote.accountmanager.exceptions.DataException;
import org.cote.accountmanager.objects.DataType;
import org.cote.accountmanager.objects.DirectoryGroupType;
import org.cote.accountmanager.objects.FunctionEnumType;
import org.cote.accountmanager.objects.FunctionType;
import org.cote.accountmanager.util.DataUtil;
import org.junit.Test;

public class TestFunctionFactory extends BaseDataAccessTest{
	public static final Logger logger = Logger.getLogger(TestFunctionFactory.class.getName());
	
	@Test
	public void TestCRUD(){
		try{
			Factories.getUserFactory().populate(testUser);
			DirectoryGroupType ddir = Factories.getGroupFactory().getCreateDirectory(testUser, "Data", testUser.getHomeDirectory(), testUser.getOrganization());
			DataType bsh = Factories.getDataFactory().getDataByName("Test.bsh",false,ddir);
			if(bsh != null){
				Factories.getDataFactory().deleteData(bsh);
				bsh = null;
			}
			if(bsh == null){
				bsh = Factories.getDataFactory().newData(testUser, ddir);
				bsh.setName("Test.bsh");
				bsh.setMimeType("text/plain");
				DataUtil.setValueString(bsh, getShellScript());
				Factories.getDataFactory().addData(bsh);
				bsh = Factories.getDataFactory().getDataByName("Test.bsh",false,ddir);
			}
			DirectoryGroupType fdir = Factories.getGroupFactory().getCreateDirectory(testUser, "Functions", testUser.getHomeDirectory(), testUser.getOrganization());
			FunctionType func = Factories.getFunctionFactory().getByName("TestBSH1", fdir);
			if(func != null){
				Factories.getFunctionFactory().deleteFunction(func);
				func = null;
			}
			if(func == null){
				func = Factories.getFunctionFactory().newFunction(testUser, fdir);
				func.setName("TestBSH1");
				func.setFunctionType(FunctionEnumType.JAVA);
				func.setFunctionData(bsh);
				Factories.getFunctionFactory().addFunction(func);
				func = Factories.getFunctionFactory().getByName("TestBSH1", fdir);
			}
			assertNotNull("Function is null",func);
			
			BshService.run(testUser,func);
		}
		catch(FactoryException e){
			logger.error(e.getMessage());
		} catch (ArgumentException e) {
			// TODO Auto-generated catch block
			logger.error(e.getMessage());
		} catch (DataException e) {
			// TODO Auto-generated catch block
			logger.error(e.getMessage());
		}

	}
	
	private static String getShellScript(){
		StringBuffer  buff = new StringBuffer();
		//buff.append("import org.apache.log4j.Logger;\n");
		//buff.append("import org.cote.accountmanager.objects.DirectoryGroupType;\n");
		//buff.append("import org.cote.accountmanager.objects.DirectoryGroupType;\n");
		//buff.append("import org.cote.accountmanager.objects.types.GroupEnumType;\n");
		//buff.append("import org.cote.accountmanager.data.Factories;\n");
		//buff.append("Logger logger = Logger.getLogger(\"BeanShell\");\n");
		
		buff.append("DirectoryGroupType dir = Factories.getGroupFactory().findGroup(null, GroupEnumType.DATA, \"/Home/TestUser1\", Factories.getOrganizationFactory().findOrganization(\"/Accelerant/Rocket\"));");
		buff.append("logger.info(\"BeanShell: \" + dir.getName());");
		buff.append("if(true==false){");
		buff.append("}");
		return buff.toString();
	}
	
}
	