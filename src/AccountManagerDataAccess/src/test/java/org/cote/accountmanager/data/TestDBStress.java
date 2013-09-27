package org.cote.accountmanager.data;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.cote.accountmanager.beans.SecurityBean;
import org.cote.accountmanager.data.ConnectionFactory;
import org.cote.accountmanager.data.ConnectionFactory.CONNECTION_TYPE;
import org.cote.accountmanager.data.DBFactory;
import org.cote.accountmanager.data.factory.DataFactory;
import org.cote.accountmanager.data.Factories;
import org.cote.accountmanager.data.factory.GroupFactory;
import org.cote.accountmanager.data.factory.OrganizationFactory;
import org.cote.accountmanager.data.security.OrganizationSecurity;
import org.cote.accountmanager.exceptions.DataException;
import org.cote.accountmanager.objects.AuditType;
import org.cote.accountmanager.objects.DataColumnType;
import org.cote.accountmanager.objects.DataTableType;
import org.cote.accountmanager.objects.DataType;
import org.cote.accountmanager.objects.DirectoryGroupType;
import org.cote.accountmanager.objects.OrganizationType;
import org.cote.accountmanager.objects.UserType;
import org.cote.accountmanager.objects.types.ActionEnumType;
import org.cote.accountmanager.objects.types.AuditEnumType;
import org.cote.accountmanager.objects.types.ResponseEnumType;


public class TestDBStress{
	public static final Logger logger = Logger.getLogger(TestDBStress.class.getName());
	@Before
	public void setUp() throws Exception {
		String log4jPropertiesPath = System.getProperty("log4j.configuration");
		if(log4jPropertiesPath != null){
			System.out.println("Properties=" + log4jPropertiesPath);
			PropertyConfigurator.configure(log4jPropertiesPath);
		}
		ConnectionFactory cf = ConnectionFactory.getInstance();
		cf.setConnectionType(CONNECTION_TYPE.SINGLE);
		cf.setDriverClassName("org.postgresql.Driver");
		cf.setUserName("devuser");
		cf.setUserPassword("password");
		cf.setUrl("jdbc:postgresql://127.0.0.1:5432/devdb");

	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testConnectionRoundRobin(){
		Connection connection = null;
		for(int i = 0; i < 50; i++){
			connection = ConnectionFactory.getInstance().getConnection();
			try {
				connection.close();
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	@Test
	public void testFactoryFlood(){
		for(int i = 0; i < 50; i++){
			AuditType audit = Factories.getAuditFactory().newAudit();
			audit.setAuditActionType(ActionEnumType.ADD);
			audit.setAuditSourceType(AuditEnumType.DATA);
			audit.setAuditTargetType(AuditEnumType.DATA);
			audit.setAuditResultType(ResponseEnumType.INFO);
			try {
				Factories.getAuditFactory().addAudit(audit);
			} catch (FactoryException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			Factories.getAuditFactory().flushSpool();
		}
	}
}
