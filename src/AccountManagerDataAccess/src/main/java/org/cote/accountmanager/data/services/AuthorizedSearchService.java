package org.cote.accountmanager.data.services;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import org.cote.accountmanager.data.BulkFactories;
import org.cote.accountmanager.data.ConnectionFactory;
import org.cote.accountmanager.data.DBFactory;
import org.cote.accountmanager.data.Factories;
import org.cote.accountmanager.data.DBFactory.CONNECTION_TYPE;
import org.cote.accountmanager.data.factory.FactoryBase;
import org.cote.accountmanager.data.factory.NameIdFactory;
import org.cote.accountmanager.data.query.QueryField;
import org.cote.accountmanager.exceptions.ArgumentException;
import org.cote.accountmanager.exceptions.FactoryException;
import org.cote.accountmanager.factory.FieldMap;
import org.cote.accountmanager.objects.BasePermissionType;
import org.cote.accountmanager.objects.EntitlementType;
import org.cote.accountmanager.objects.FieldMatch;
import org.cote.accountmanager.objects.NameIdType;
import org.cote.accountmanager.objects.ObjectSearchRequestType;
import org.cote.accountmanager.objects.ProcessingInstructionType;
import org.cote.accountmanager.objects.types.AffectEnumType;
import org.cote.accountmanager.objects.types.ComparatorEnumType;
import org.cote.accountmanager.objects.types.FactoryEnumType;
import org.cote.accountmanager.objects.types.NameEnumType;
import org.cote.accountmanager.objects.types.OrderEnumType;
import org.cote.accountmanager.objects.types.SqlDataEnumType;

public class AuthorizedSearchService {
	public static final Logger logger = LogManager.getLogger(AuthorizedSearchService.class);
	
	
	/*
	 * 2021/08/12 - inconsistent table naming convention leads to things like this.  
	 * This all needs to be moved over to FieldMap or another mapping location and a switch statement
	 */
	private static String getTableNameByType(NameEnumType objectType) {
		String outName = null;
		switch(objectType) {
			case ADDRESS:
				outName = "addresses";
				break;
			case GROUP:
				outName = "groups";
				break;
			case ACCOUNT:
				outName = "accounts";
				break;
			case CONTACT:
				outName = "contacts";
				break;
			case PERSON:
				outName = "persons";
				break;
			case ROLE:
				outName = "roles";
				break;
			case TAG:
				outName = "tags";
				break;
			default:
				outName = objectType.toString().toLowerCase();
				break;
		}
		
		return outName;
	}
	
	/// Convenience method for creating the pagination instruction set off the search request
	/// NOTE: The sort fields are extremely limited at the moment and should be replaced with the ColumnEnumType
	///
	private static ProcessingInstructionType getProcessingInstructionFromRequest(ObjectSearchRequestType request) {
		ProcessingInstructionType pi = new ProcessingInstructionType();
		pi.setPaginate(true);
		pi.setSortQuery(request.getSort());
		pi.setStartIndex(request.getStartRecord());
		pi.setRecordCount(request.getRecordCount());
		if(request.getSort() != null) {
			pi.setOrderClause(request.getSort().getSortField().toString().toLowerCase() + " " + (request.getSort().getSortOrder().equals(OrderEnumType.ASCENDING) ? "ASC" : "DESC"));
		}
		return pi;
	}
	private static List<QueryField> getQueryFieldsFromRequest(ObjectSearchRequestType request){
		List<QueryField> fields = new ArrayList<>();
		for(FieldMatch fm : request.getFields()) {
			if(!FieldMap.getColumnMap().containsKey(fm.getFieldName())) {
				logger.error("Invalid column name: " + fm.getFieldName().toString());
				continue;
			}
			QueryField f = new QueryField(fm.getDataType(), FieldMap.getColumnMap().get(fm.getFieldName()), (fm.getComparator().equals(ComparatorEnumType.LIKE) ? fm.getEncodedValue().replaceAll("\\*","%") : fm.getEncodedValue()));
			f.setComparator(fm.getComparator());
			fields.add(f);
		}
		return fields;
	}
	public static boolean isValidSearchActor(NameIdType actor) {
		if(actor == null) return false;
		NameEnumType actorType = actor.getNameType();
		return (actorType != null && (
			actorType.equals(NameEnumType.USER)
			|| actorType.equals(NameEnumType.ACCOUNT)
			|| actorType.equals(NameEnumType.PERSON)
		));
	}
	
	
	
	public static <T> List<T> searchByEffectiveMemberEntitlement(ObjectSearchRequestType searchRequest, NameIdType member){
		/// ents represent the ids 
		BasePermissionType viewObjPer = null;
		BasePermissionType viewGroupPer = null;
		List<T> outL = new ArrayList<T>();
		NameEnumType objectType = searchRequest.getObjectType();
		try {
			viewObjPer = AuthorizationService.getViewPermissionForMapType(objectType, member.getOrganizationId());
			viewGroupPer = AuthorizationService.getViewPermissionForMapType(NameEnumType.GROUP, member.getOrganizationId());
		} catch (FactoryException | ArgumentException e) {
			logger.error(e.getMessage());
		}
		if(viewObjPer == null || viewGroupPer == null) {
			logger.error("Unable to find object view permissions");
			return outL;
		}
		
		List<EntitlementType> ents = searchForEffectiveMemberEntitlements(searchRequest, member, new Long[] {viewObjPer.getId(), viewGroupPer.getId()});
		Set<Long> idsx = new HashSet<Long>();
		for(EntitlementType ent : ents) idsx.add(ent.getObjectId());
		Long[] ids = idsx.toArray(new Long[0]);
		List<QueryField> fields = getQueryFieldsFromRequest(searchRequest);
		StringBuilder buff = new StringBuilder();
		
		try {
			NameIdFactory fact = Factories.getFactory(FactoryEnumType.valueOf(objectType.toString()));
			
			for (int i = 0; i < ids.length; i++)
			{
				if (buff.length() > 0) buff.append(",");
				buff.append(ids[i]);
				if ((i > 0 || ids.length == 1) && ((i % BulkFactories.bulkQueryLimit == 0) || i == ids.length - 1))
				{
					QueryField match = new QueryField(SqlDataEnumType.BIGINT, "id", buff.toString());
					match.setComparator(ComparatorEnumType.IN);
					QueryField[] subFields = ArrayUtils.addAll(fields.toArray(new QueryField[0]), match);
					List<NameIdType> tmpDataList = fact.getByField(subFields , member.getOrganizationId());
					outL.addAll(FactoryBase.convertList(tmpDataList));
					buff.delete(0,  buff.length());
				}
			}
		}
		catch(FactoryException | ArgumentException e) {
			logger.error(e);
		}
		
		return outL;
	}
	
	/// Convenience method to return a count of the entitlements
	public static int countByEffectiveMemberEntitlement(ObjectSearchRequestType request, NameIdType member, Long[] permissionIds) {
		return searchForEffectiveMemberEntitlements(request, member, permissionIds).size();
	}
	
	/*
	 * This method searches for object identifiers that match a given set of permissions and query criteria
	 * 
	 * The purpose of this method is to look across varying types of entity tables, joined across the participation tables
	 * along with unrolling group and role memberships, as well as owned objects (which EAS doesn't do) to return a list of entitlements that match the supplied permission list
	 * including optional search query parameters so that large sets may be counted and filtered for paginated search results
	 * 
	 * NOTE: Because this method includes the owner, if the desire is to only find entitlements it's better to use the EAS version which only includes entitlements other than the owner
	 * 
	 * 
	 * 2021/08/12 - This is a variation of the getEffectiveMemberEntitlements method in EffectiveAuthorizationService
	 * The primary differences are this one accepts query parameters, and includes any owned objects, versus the EAS version which only returns entitlements that has been declared
	 */
	public static List<EntitlementType> searchForEffectiveMemberEntitlements(ObjectSearchRequestType request, NameIdType member, Long[] permissionIds){
		List<EntitlementType> out_ents = new ArrayList<>();
		/// TODO: Need to add check that object type has a corresponding participation capability
		///
		NameEnumType objectType = request.getObjectType();
		
		if(!isValidSearchActor(member)) {
			logger.error("Search actor is invalid");
			return out_ents;
		}
		if(objectType == NameEnumType.UNKNOWN){
			logger.error("Invalid object or object name type");
			return out_ents;
		}
	

		String referenceType = (member == null ? "" : member.getNameType().toString());
		long referenceId = (member == null ? 0L : member.getId());
		long orgId = member.getOrganizationId();
		Connection conn = ConnectionFactory.getInstance().getConnection();
		CONNECTION_TYPE connType = DBFactory.getConnectionType(conn);
		String token = DBFactory.getParamToken(connType);
		
		String lowType = objectType.toString().toLowerCase();
		String tblType = getTableNameByType(objectType);

		ProcessingInstructionType pi = getProcessingInstructionFromRequest(request);
		String pagePrefix = DBFactory.getPaginationPrefix(pi, connType);
		String pageSuffix = DBFactory.getPaginationSuffix(pi, connType);
		
		QueryField[] fields = getQueryFieldsFromRequest(request).toArray(new QueryField[0] );
		String queryClause = FactoryBase.getQueryClause(pi, fields, token);
		
		String sqlBaseQuery =
		String.format(
		"SELECT participationid as id, affectid, affecttype, DM.referenceid, referencetype FROM ("
		/// 1 - objtype
		+ "\nSELECT D.id as participationid,  -1 as affectid, 'UNKNOWN' as affecttype, D.ownerId as referenceid, 'USER' as referencetype from %s D"
		/// 2  = token 1 ownerid
		+ " WHERE ownerId = %s"
		+ "\n UNION ALL"
		/// START GROUPID
		+ " SELECT D.id, GP.affectid,GP.affecttype,GP.participantid as referenceid, GP.participanttype as referencetype"
		/// 3 = objtype
		+ " FROM %s D"
		+ " INNER JOIN groups G on G.id = " + (objectType.equals(NameEnumType.GROUP) ? "D.parentId" : "D.groupid")
		/// 4 = token 2 referencetype
		/// 4.5 = token 2.5 referenceid
		+ " INNER JOIN groupparticipation GP on GP.participationid = G.id AND GP.participanttype = %s AND GP.participantid = %s"
		/// 5 = token 3 permissionIds
		/// 6 = token 4 permissionIds
		+ " WHERE (0 = cardinality(%s) OR GP.affectId = ANY(%s))"
		+ (request.getIncludeThumbnail() ? "" : " AND G.name <> '.thumbnail'")
		/// END GROUPID
		+ "\n UNION ALL"
		+ " SELECT PP.participationid,PP.affectid,PP.affecttype,"
		+"	CASE WHEN GM.referenceid > 0 THEN GM.referenceid"
		+"	ELSE PP.participantid END as referenceid,"
		+"	CASE WHEN GM.referencetype <> '' THEN GM.referencetype"
		+"	ELSE PP.participanttype END as referencetype"
		/// 7 = objtype
		+"	FROM %sparticipation PP"
		/// 8 = token 5 referencetype
		/// 9 = token 6 referencetype
		/// 10 = token 7 referenceid
		/// 11 = token 8 referenceid
		+"	LEFT JOIN group_membership(PP.participantid) GM ON (%s = '' OR GM.referencetype = %s) AND (%s = 0 OR GM.referenceid = %s) "
		/// 12 = token 9 permissionIds
		/// 13 = token 10 permissionIds
		+"	WHERE (0 = cardinality(%s) OR PP.affectId = ANY(%s)) AND PP.participanttype = 'GROUP'"
		+"\n	UNION ALL"
		+"	SELECT PP.participationid,PP.affectid,PP.affecttype,"
		+"	CASE WHEN GM.referenceid > 0 THEN GM.referenceid"
		+"	ELSE PP.participantid END as referenceid,"
		+"	CASE WHEN GM.referencetype <> '' THEN GM.referencetype"
		+"	ELSE PP.participanttype END as referencetype"
		/// 14 objtype
		+"	FROM %sparticipation PP"
		/// 15 token 11 referencetype
		///	16 token 12 referencetype
		/// 17 = token 13 referenceid
		/// 18 = token 14 referenceid
		+"	LEFT JOIN role_membership(PP.participantid) GM ON (%s = '' OR GM.referencetype = %s)  AND (%s = 0 OR GM.referenceid = %s)"
		/// 19 = token 15 permissionids
		/// 20 = token 16 permissionids
		+"	WHERE (0 = cardinality(%s) OR PP.affectId = ANY(%s)) AND PP.participanttype = 'ROLE'	"
		+"\n UNION ALL"
		+"	SELECT PP.participationid,PP.affectid,PP.affecttype,"
		+"	PP.participantid as referenceid,PP.participanttype as referencetype"
		/// 21 = objtype
		+"	FROM %sparticipation PP"
		/// 21 = token 17 permissionids
		/// 22 = token 18 permissionids
		+"	WHERE (0 = cardinality(%s) OR PP.affectId = ANY(%s))"
		+"	AND NOT PP.participanttype IN('GROUP','ROLE')"
		/// 23 = token 19 referencetype
		/// 24 = token 20 referencetype
		+"	AND (%s = '' OR PP.participanttype = %s)"
		/// 25 = token 21 referenceid
		/// 26 = token 22 referenceid
		+"	AND (%s = 0 OR PP.participantid =%s)"
		/// 27 = token 23 referencetype
		/// 28 = token 24 referencetype
		/// 29 = token 25 referenceid
		/// 26 = token 26 referenceid
		+ ") DM "
		+ "\n INNER JOIN " + tblType + " on " + tblType + ".id = DM.participationid " 
		+ "\n WHERE (%s = '' OR referencetype = %s) AND (%s = 0 OR DM.referenceid =%s) AND affectid <> 0"
		,tblType,token,tblType,token,token,token,token,lowType,token,token,token,token,token,token,lowType
		,token,token,token,token,token,token,lowType
		,token,token,token,token,token,token,token,token,token,token
		);
		
		String sqlQuery = pagePrefix + sqlBaseQuery + (queryClause != null && queryClause.length() > 0 ? " AND " + queryClause : "")
		+ (pi != null && pi.getGroupClause() != null ? " GROUP BY " + pi.getGroupClause() : "")
		+ (pi != null && pi.getHavingClause() != null ? " HAVING " + pi.getHavingClause() : "")
		+ pageSuffix
		+ ";"
		;
		
		PreparedStatement statement = null;

		ResultSet rset = null;
		try{
			long startQuery = System.currentTimeMillis();
			
			statement = conn.prepareStatement(sqlQuery);

			statement.setLong(1, referenceId);
			statement.setString(2, referenceType);
			statement.setLong(3, referenceId);
			statement.setArray(4, conn.createArrayOf("bigint", permissionIds));
			statement.setArray(5, conn.createArrayOf("bigint", permissionIds));
			statement.setString(6, referenceType);
			statement.setString(7, referenceType);
			statement.setLong(8, referenceId);
			statement.setLong(9, referenceId);
			statement.setArray(10, conn.createArrayOf("bigint", permissionIds));
			statement.setArray(11, conn.createArrayOf("bigint", permissionIds));
			
			statement.setString(12, referenceType);
			statement.setString(13, referenceType);
			statement.setLong(14, referenceId);
			statement.setLong(15, referenceId);
			statement.setArray(16, conn.createArrayOf("bigint", permissionIds));
			statement.setArray(17, conn.createArrayOf("bigint", permissionIds));
			
			statement.setArray(18, conn.createArrayOf("bigint", permissionIds));
			statement.setArray(19, conn.createArrayOf("bigint", permissionIds));
			statement.setString(20, referenceType);
			statement.setString(21, referenceType);
			statement.setLong(22, referenceId);
			statement.setLong(23, referenceId);
			statement.setString(24, referenceType);
			statement.setString(25, referenceType);
			statement.setLong(26, referenceId);
			statement.setLong(27, referenceId);	
	
			DBFactory.setStatementParameters(fields, 28, statement);
			
			rset = statement.executeQuery();
			while(rset.next()){
				EntitlementType ent = new EntitlementType();
				ent.setObjectId(rset.getLong(1));
				ent.setObjectType(objectType);
				ent.setMemberId(rset.getLong(4));
				ent.setMemberType(NameEnumType.valueOf(rset.getString(5)));
				ent.setEntitlementId(rset.getLong(2));
				ent.setEntitlementAffectType(AffectEnumType.valueOf(rset.getString(3)));
				ent.setOrganizationId(orgId);
				out_ents.add(ent);
			}

			long stopQuery = System.currentTimeMillis();
			long diff = (stopQuery - startQuery);

			logger.debug("*** QUERY TIME: " + diff + "ms");
		}
		catch(SQLException | FactoryException sqe){
			logger.error(sqe.getMessage());
			logger.error(FactoryException.LOGICAL_EXCEPTION,sqe);
			logger.error(statement);
		}
		finally{
			try {
				if(rset != null) rset.close();
				if(statement != null) statement.close();
				conn.close();
			} catch (SQLException e) {
				
				logger.error(e.getMessage());
				logger.error(FactoryException.LOGICAL_EXCEPTION,e);
			}
		}
		return out_ents;
	}
	
	
}
