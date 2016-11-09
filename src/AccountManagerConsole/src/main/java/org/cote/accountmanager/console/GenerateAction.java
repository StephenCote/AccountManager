package org.cote.accountmanager.console;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.cote.accountmanager.data.ConnectionFactory;
import org.cote.accountmanager.data.Factories;
import org.cote.accountmanager.data.factory.NameIdFactory;
import org.cote.accountmanager.objects.types.FactoryEnumType;
import org.cote.accountmanager.objects.types.NameEnumType;
import org.cote.accountmanager.util.FileUtil;

public class GenerateAction {
	public static final Logger logger = LogManager.getLogger(GenerateAction.class);
	private static boolean execute(String sql){
		/*
		try {
			List<OrganizationType> orgs = ((OrganizationFactory)Factories.getFactory(FactoryEnumType.ORGANIZATION)).list(new QueryField[]{}, 0L);

		} catch (FactoryException | ArgumentException e1) {
			
			logger.error("Error",e1);
		}
		*/
		
		Connection connection = null;
		boolean out_bool = false;
		try{
			connection = ConnectionFactory.getInstance().getConnection();
			Statement statement = connection.createStatement();
			statement.executeUpdate(sql);
			connection.close();
			logger.info("Executed Update");
			out_bool = true;
		}
		catch(SQLException sqe){
			logger.error(sqe.getMessage());
			logger.error("Error",sqe);
		}
		finally{
			if(connection != null){
				try {
					connection.close();
				} catch (SQLException e) {
					
					logger.error("Error",e);
				}
			}
		}
		return true;
	}
	public static String generate(NameEnumType type, boolean exec, boolean export, String path){
		if(type == null || type == NameEnumType.UNKNOWN){
			logger.error("Invalid type: " + type);
			return null;
		}
		FactoryEnumType typeFact = FactoryEnumType.valueOf(type.toString());
		NameIdFactory fact = Factories.getFactory(typeFact);
		if(fact == null){
			logger.error("Null factory for type " + typeFact.toString());
		}
		FactoryEnumType typePart = FactoryEnumType.valueOf(type.toString() + "PARTICIPATION");
		if(typePart == null || typePart == FactoryEnumType.UNKNOWN){
			logger.error("Invalid factory participation type for: " + type);
			return null;
		}
		logger.info("Generating Extension Schema for " + type.toString());
		
		StringBuilder buff = new StringBuilder();
		buff.append(generateCacheTableSchema(type.toString().toLowerCase()));
		buff.append(generateEffectiveRolesView(type.toString().toLowerCase()));
		buff.append(generateEffectiveRoleRightsViews(type.toString().toLowerCase()));
		buff.append(generateObjectRightsViews(type.toString().toLowerCase()));
		buff.append(generateEffectiveRightsView(type.toString().toLowerCase()));
		buff.append(generateCacheFunctionSchema(type.toString().toLowerCase()));
		
		if(exec){
			execute(buff.toString());
		//logger.info("\n" + buff.toString());
		}
		if(export){
			logger.info("Exporting schema (" + buff.length() + " bytes) to '" + path + "'");
			FileUtil.emitFile(path, buff.toString());
		}
		return buff.toString();
	}
	
	public static String generateCacheTableSchema(String name){
		String lowName = name.toLowerCase();
		String initUC = lowName.substring(0, 1).toUpperCase() + lowName.substring(1,lowName.length());
		return 
			"DROP TABLE IF EXISTS " + lowName + "rolecache CASCADE;" + System.lineSeparator()
			+ "CREATE TABLE " + lowName + "rolecache (" + System.lineSeparator()
			+ ") inherits (rolecache);" + System.lineSeparator()
			+ System.lineSeparator();
	}
	public static String generateObjectRightsViews(String name){
		String lowName = name.toLowerCase();
		String initUC = lowName.substring(0, 1).toUpperCase() + lowName.substring(1,lowName.length());

		return "create or replace view " + lowName + "PersonRights as" + System.lineSeparator()
		+ "select U.id as personid,D.id as " + lowName + "id, D.name as " + initUC + "Name, D.ownerid as " + lowName + "ownerid,G.organizationid," + System.lineSeparator()
		+ "P.name as permissionname," + System.lineSeparator()
		+ "DP.affecttype,DP.affectid" + System.lineSeparator()
		+ "from " + initUC + " D" + System.lineSeparator()
		+ "join Groups G on G.id = D.groupid" + System.lineSeparator()
		+ "join " + lowName + "participation DP on DP.participationid = D.id" + System.lineSeparator()
		+ "join Persons U on DP.participantid = U.id" + System.lineSeparator()
		+ "join permissions P on DP.affectid = P.id" + System.lineSeparator()
		+ "where" + System.lineSeparator()
		+ "DP.id > 0" + System.lineSeparator()
		+ "AND DP.participanttype = 'PERSON'" + System.lineSeparator()
		+ "AND DP.affectid > 0;" + System.lineSeparator()
		+ "create or replace view " + lowName + "UserRights as" + System.lineSeparator()
		+ "select U.id as userid,D.id as " + lowName + "id, D.name as " + initUC + "Name, D.ownerid as " + lowName + "ownerid,G.organizationid," + System.lineSeparator()
		+ "P.name as permissionname," + System.lineSeparator()
		+ "DP.affecttype,DP.affectid" + System.lineSeparator()
		+ "from " + initUC + " D" + System.lineSeparator()
		+ "join Groups G on G.id = D.groupid" + System.lineSeparator()
		+ "join " + lowName + "participation DP on DP.participationid = D.id" + System.lineSeparator()
		+ "join users U on DP.participantid = U.id" + System.lineSeparator()
		+ "join permissions P on DP.affectid = P.id" + System.lineSeparator()
		+ "where" + System.lineSeparator()
		+ "DP.id > 0" + System.lineSeparator()
		+ "AND DP.participanttype = 'USER'" + System.lineSeparator()
		+ "AND DP.affectid > 0;" + System.lineSeparator()
		+ "create or replace view " + lowName + "AccountRights as" + System.lineSeparator()
		+ "select U.id as accountid,D.id as " + lowName + "id, D.name as " + initUC + "Name, D.ownerid as " + lowName + "ownerid,G.organizationid," + System.lineSeparator()
		+ "P.name as permissionname," + System.lineSeparator()
		+ "DP.affecttype,DP.affectid" + System.lineSeparator()
		+ "from " + initUC + " D" + System.lineSeparator()
		+ "join Groups G on G.id = D.groupid" + System.lineSeparator()
		+ "join " + lowName + "participation DP on DP.participationid = D.id" + System.lineSeparator()
		+ "join accounts U on DP.participantid = U.id" + System.lineSeparator()
		+ "join permissions P on DP.affectid = P.id" + System.lineSeparator()
		+ "where" + System.lineSeparator()
		+ "DP.id > 0" + System.lineSeparator()
		+ "AND DP.participanttype = 'ACCOUNT'" + System.lineSeparator()
		+ "AND DP.affectid > 0;" + System.lineSeparator()
		+ System.lineSeparator()
		;
	}
	public static String generateEffectiveRolesView(String name){
		String lowName = name.toLowerCase();
		String initUC = lowName.substring(0, 1).toUpperCase() + lowName.substring(1,lowName.length());
		return "create or replace view effective" + initUC + "Roles as" + System.lineSeparator()
		+ "WITH result AS(" + System.lineSeparator()
		+ "select R.id,R.parentid,roles_from_leaf(R.id) ats,R.organizationid" + System.lineSeparator()
		+ "FROM roles R  WHERE roletype = 'USER' OR roletype = 'ACCOUNT' OR roletype = 'PERSON'" + System.lineSeparator()
		+ ")" + System.lineSeparator()
		+ "select DP.participationid as " + lowName + "id,(R.ats).leafid as effectiveRoleId,(R.ats).roleid as baseRoleId,DP.affectType,DP.affectId,R.organizationid from result R" + System.lineSeparator()
		+ "JOIN " + lowName + "participation DP ON DP.participantid = (R.ats).leafid and DP.participanttype = 'ROLE';" + System.lineSeparator()
		+ System.lineSeparator();
	}
	
	public static String generateEffectiveRoleRightsViews(String name){
		String lowName = name.toLowerCase();
		String initUC = lowName.substring(0, 1).toUpperCase() + lowName.substring(1,lowName.length());

		return "create or replace view effective" + initUC + "PersonRoleRights as" + System.lineSeparator()
		+ "select distinct DRC.objectid as " + lowName + "id,DRC.affectId,DRC.affectType,ER.objectid as personid,ER.effectiveRoleId as roleid,ER.organizationid from personrolecache ER" + System.lineSeparator()
		+ "join " + lowName + "RoleCache DRC on DRC.effectiveRoleId=ER.effectiveRoleId;" + System.lineSeparator()
		+ System.lineSeparator()
		+ "create or replace view effective" + initUC + "UserRoleRights as" + System.lineSeparator()
		+ "select distinct DRC.objectid as " + lowName + "id,DRC.affectId,DRC.affectType,ER.objectid as userid,ER.effectiveRoleId as roleid,ER.organizationid from userrolecache ER" + System.lineSeparator()
		+ "join " + lowName + "RoleCache DRC on DRC.effectiveRoleId=ER.effectiveRoleId;" + System.lineSeparator()
		+ System.lineSeparator()
		+ "create or replace view effective" + initUC + "AccountRoleRights as" + System.lineSeparator()
		+ "select distinct DRC.objectid as " + lowName + "id,DRC.affectId,DRC.affectType,ER.objectid as accountid,ER.effectiveRoleId as roleid,ER.organizationid from accountrolecache ER" + System.lineSeparator()
		+ "join " + lowName + "RoleCache DRC on DRC.effectiveRoleId=ER.effectiveRoleId;" + System.lineSeparator()
		+ System.lineSeparator()
		;

	}
	public static String generateEffectiveRightsView(String name){
		String lowName = name.toLowerCase();
		String initUC = lowName.substring(0, 1).toUpperCase() + lowName.substring(1,lowName.length());

		return "create or replace view " + lowName + "Rights as"+ System.lineSeparator()
				+ "select distinct referenceid,referencetype," + lowName + "id,affecttype,affectid,organizationid from ("+ System.lineSeparator()
				+ "select personid as referenceid,'PERSON' as referencetype," + lowName + "id,affecttype,affectid,organizationid"+ System.lineSeparator()
				+ "FROM " + lowName + "PersonRights GUR"+ System.lineSeparator()
				+ "UNION ALL"+ System.lineSeparator()
				+ "select personid as referenceid,'PERSON' as referencetype," + lowName + "id,affecttype,affectid,organizationid"+ System.lineSeparator()
				+ "FROM effective" + initUC + "PersonRoleRights GRR"+ System.lineSeparator()
				+ "UNION ALL"+ System.lineSeparator()
				+ "select userid as referenceid,'USER' as referencetype," + lowName + "id,affecttype,affectid,organizationid"+ System.lineSeparator()
				+ "FROM " + lowName + "UserRights GUR"+ System.lineSeparator()
				+ "UNION ALL"+ System.lineSeparator()
				+ "select userid as referenceid,'USER' as referencetype," + lowName + "id,affecttype,affectid,organizationid"+ System.lineSeparator()
				+ "FROM effective" + initUC + "UserRoleRights GRR"+ System.lineSeparator()
				+ "UNION ALL"+ System.lineSeparator()
				+ "select accountid as referenceid,'ACCOUNT' as referencetype," + lowName + "id,affecttype,affectid,organizationid"+ System.lineSeparator()
				+ "FROM " + lowName + "AccountRights GUR"+ System.lineSeparator()
				+ "UNION ALL"+ System.lineSeparator()
				+ "select accountid as referenceid,'ACCOUNT' as referencetype," + lowName + "id,affecttype,affectid,organizationid"+ System.lineSeparator()
				+ "FROM effective" + initUC + "AccountRoleRights GRR"+ System.lineSeparator()	
				+ "UNION ALL"+ System.lineSeparator()
				+ "select AGP.participantid as referenceid,'GROUP' as referencetype,AGP.participationid as " + lowName + "id,AGP.affecttype,AGP.affectid,AG.organizationid"+ System.lineSeparator()
				+ "FROM groups AG"+ System.lineSeparator()
				+ "inner join " + lowName + "participation AGP on AGP.participantType = 'GROUP' AND AGP.participantId = AG.id"+ System.lineSeparator()
				+ ") as AM;"+ System.lineSeparator()
				+ System.lineSeparator()
			;
	}
	public static String generateCacheFunctionSchema(String name){
		String lowName = name.toLowerCase();
		String initUC = lowName.substring(0, 1).toUpperCase() + lowName.substring(1,lowName.length());

		return "CREATE OR REPLACE FUNCTION cache_all_" + lowName + "_roles(orgId BIGINT)" + System.lineSeparator() 
			+ "RETURNS BOOLEAN" + System.lineSeparator()
			+ "AS $BODY$" + System.lineSeparator()
			+ "DECLARE ids BIGINT[] = ARRAY(SELECT id FROM " + lowName + " WHERE organizationid = $1);" + System.lineSeparator()
			+ "BEGIN" + System.lineSeparator()
			+ "DELETE FROM " + lowName + "rolecache WHERE objectid = ANY(ids);" + System.lineSeparator()
			+ "INSERT INTO " + lowName + "rolecache (objectid,effectiveroleid,baseroleid,affecttype,affectid,organizationid) select * from effective" + initUC + "Roles where " + lowName + "id=ANY(ids);" + System.lineSeparator()
			+ "RETURN true;" + System.lineSeparator()
			+ "END" + System.lineSeparator()
			+ "$BODY$ LANGUAGE 'plpgsql';" + System.lineSeparator()
			+ System.lineSeparator()
			+ "CREATE OR REPLACE FUNCTION cache_" + lowName + "_roles(" + lowName + "_id BIGINT[],organizationid BIGINT)" + System.lineSeparator() 
			+ "RETURNS BOOLEAN" + System.lineSeparator()
			+ "AS $$" + System.lineSeparator()
			+ "BEGIN" + System.lineSeparator()
			+ "DELETE FROM " + lowName + "rolecache where objectid = ANY($1);" + System.lineSeparator()
			+ "INSERT INTO " + lowName + "rolecache (objectid,effectiveroleid,baseroleid,affecttype,affectid,organizationid) select * from effective" + initUC + "Roles where " + lowName + "id=ANY($1);" + System.lineSeparator()
			+ "RETURN true;" + System.lineSeparator()
			+ "END" + System.lineSeparator()
			+ "$$ LANGUAGE 'plpgsql';" + System.lineSeparator()
			+ System.lineSeparator();
	}
}
