package org.cote.accountmanager.console;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;
import org.cote.accountmanager.data.ArgumentException;
import org.cote.accountmanager.data.BulkFactories;
import org.cote.accountmanager.data.DataAccessException;
import org.cote.accountmanager.data.Factories;
import org.cote.accountmanager.data.FactoryException;
import org.cote.accountmanager.data.query.QueryField;
import org.cote.accountmanager.data.query.QueryFields;
import org.cote.accountmanager.exceptions.DataException;
import org.cote.accountmanager.objects.DataParticipantType;
import org.cote.accountmanager.objects.DataTagType;
import org.cote.accountmanager.objects.DataType;
import org.cote.accountmanager.objects.DirectoryGroupType;
import org.cote.accountmanager.objects.OrganizationType;
import org.cote.accountmanager.objects.UserType;
import org.cote.accountmanager.objects.types.ComparatorEnumType;
import org.cote.accountmanager.objects.types.FactoryEnumType;
import org.cote.accountmanager.objects.types.GroupEnumType;
import org.cote.accountmanager.objects.types.SqlDataEnumType;
import org.cote.accountmanager.util.CalendarUtil;
import org.cote.accountmanager.util.DataUtil;
import org.cote.accountmanager.util.FileUtil;
import org.cote.accountmanager.util.MimeUtil;
import org.cote.accountmanager.util.StreamUtil;
import org.cote.accountmanager.util.ZipUtil;

public class DataAction {
	public static final Logger logger = Logger.getLogger(DataAction.class.getName());
	private static int maxLoad = 50;
	public static void setMaximumLoad(int i){ maxLoad = i;}
	private static Pattern limitNames = Pattern.compile("([^A-Za-z0-9\\-_\\.\\s])",Pattern.MULTILINE);
	private static Pattern limitPath = Pattern.compile("([^A-Za-z0-9\\-_\\.\\s\\/])",Pattern.MULTILINE);
	
	public static void tagData(UserType user, String tagFile){
		Map<String,Map<String,List<String>>> tagMap = new HashMap<String,Map<String,List<String>>>();
		Factories.getTagParticipationFactory().setBatchSize(1000);
		Factories.getTagParticipationFactory().setAggressiveKeyFlush(false);

		try{

			OrganizationType org = user.getOrganization();
			Factories.getUserFactory().populate(user);
			DirectoryGroupType tagDir = Factories.getGroupFactory().getCreateDirectory(user, "Tags", user.getHomeDirectory(), org);
			String[] dataFile = FileUtil.getFileAsString(tagFile).split("\n");
			logger.info("Reading " + dataFile.length + " lines");
			String match = "Root/Home/product_user/Media";
			String replace = "/Home/TestUser1/GalleryHome/rd";
			
			Map<String,DataTagType> dataTags = new HashMap<String,DataTagType>();
			
			DataTagType nTag = null;
			long startMap = System.currentTimeMillis();
			
			for(int i = 0; i < dataFile.length;i++){
				String[] pairs = dataFile[i].split("\t");
				if(pairs.length != 4) logger.warn("Unexpected length");
				String tagName = pairs[2];
				String path = pairs[3].replace(match,replace).trim();
				path = limitPath.matcher(path).replaceAll("");
				String name = limitNames.matcher(pairs[1]).replaceAll("0");
				if(tagMap.containsKey(tagName) == false){
					tagMap.put(tagName, new HashMap<String,List<String>>());
				}
				if(tagMap.get(tagName).containsKey(path)==false){
					tagMap.get(tagName).put(path, new ArrayList<String>());
				}
				tagMap.get(tagName).get(path).add(name);

			}
			logger.info("Time to build map: " + (System.currentTimeMillis() - startMap));
			startMap = System.currentTimeMillis();
			String[] tags = tagMap.keySet().toArray(new String[0]);
			for(int i = 0; i < tags.length;i++){
				long startTag = System.currentTimeMillis();
				String sessionId = BulkFactories.getBulkFactory().newBulkSession();
				if(dataTags.containsKey(tags[i])==false){
					DataTagType tag = Factories.getTagFactory().newDataTag(user,tags[i],tagDir);
					BulkFactories.getBulkFactory().createBulkEntry(sessionId, FactoryEnumType.TAG, tag);
					dataTags.put(tags[i], tag);
					nTag = tag;
				}
				else nTag = dataTags.get(tags[i]);
				String[] paths = tagMap.get(tags[i]).keySet().toArray(new String[0]);
				//logger.info("Tag Path Size = " + paths.length);
				for(int g = 0; g < paths.length; g++){
					long startLookup = System.currentTimeMillis();
					DirectoryGroupType dir = (DirectoryGroupType)Factories.getGroupFactory().findGroup(user, GroupEnumType.DATA,paths[g], org);
					//logger.info("Group Lookup: " + (System.currentTimeMillis() - startLookup));
					if(dir == null){
						logger.warn("Failed to find path '" + paths[g] + "'");
						continue;
					}
					
					StringBuffer names = new StringBuffer();
					List<String> dNames = tagMap.get(tags[i]).get(paths[g]);
					//logger.info("Tag Data Size = " + dNames.size());
					for(int n = 0; n < dNames.size();n++){
						if(n > 0) names.append(",");
						names.append("'" + dNames.get(n) + "'");
					}
					List<QueryField> fields = new ArrayList<QueryField>();
					QueryField nameField = new QueryField(SqlDataEnumType.VARCHAR,"name",names.toString());
					nameField.setComparator(ComparatorEnumType.IN);
					fields.add(nameField);
					fields.add(QueryFields.getFieldGroup(dir.getId()));
					
					startLookup = System.currentTimeMillis();
					List<DataType> data = Factories.getDataFactory().getDataList(fields.toArray(new QueryField[0]), null,true,dir.getOrganization());
					//logger.info("Data Lookup: " + (System.currentTimeMillis() - startLookup));
					
					if(data.size() == 0){
						logger.warn("Empty data size for: " + names.toString() + " in group id " + dir.getId());
					}
					startLookup = System.currentTimeMillis();
					for(int d = 0; d < data.size(); d++){
						DataParticipantType dpt = Factories.getTagParticipationFactory().newDataTagParticipation(nTag, data.get(d));
						BulkFactories.getBulkFactory().createBulkEntry(sessionId, FactoryEnumType.TAGPARTICIPATION, dpt);
					}
					//logger.info("Bulk Queue: " + (System.currentTimeMillis() - startLookup));
	
				}
				//logger.info("Prepare Write: " + (System.currentTimeMillis() - startTag)); 
				BulkFactories.getBulkFactory().write(sessionId);
				
				//if(true) break;
			}
			
			logger.info("Time to import: " + (System.currentTimeMillis() - startMap));
				
			}
			catch(FactoryException e){
				logger.error(e.getMessage());
			} catch (ArgumentException e) {
				// TODO Auto-generated catch block
				logger.error(e.getMessage());
			} catch (DataAccessException e) {
				// TODO Auto-generated catch block
				logger.error(e.getMessage());
			}
			
	}
	
	/// One-off method
	///
	public static void migrateData(UserType user, long sourceOwnerId){
		try {
		    Class.forName("com.mysql.jdbc.Driver");
		} catch (ClassNotFoundException e) {
		    throw new RuntimeException("Cannot find the driver in the classpath!", e);
		}	
		
		/// Obviously not used any more - this was the mysql db for the .NET implementation
		String url = "jdbc:mysql://192.168.1.120:3306/coredb";
		String username = "coreuser";
		String password = "Password1";
		Connection connection = null;
		try {
		    connection = DriverManager.getConnection(url, username, password);
		    Statement statement = connection.createStatement();
		    String sql = "SELECT A.Name as AccountName,A.Id as AccountId,G.Name as GroupName,D.Id as DataId, D.Name as DataName,Description,Size,Dimensions,MimeType,IsCompressed,CompressionType,GroupId,CreatedDate,ModifiedDate,Expiration,IsBlob,DataBlob,DataString FROM Data D INNER JOIN Account A ON A.Id = D.OwnerId INNER JOIN Groups G ON G.Id = D.GroupId WHERE A.Id = " + Long.toString(sourceOwnerId) + ";";
		    logger.info("Migration Query: " + sql);
		    ResultSet rset = statement.executeQuery(sql);
		    String sessionId = BulkFactories.getBulkFactory().newBulkSession();
		    Factories.getUserFactory().populate(user);
		    while(rset.next()){
		    	String name = rset.getString("DataName");
		    	String mimeType = rset.getString("MimeType");
		    	String groupName = rset.getString("GroupName");
		    	if(groupName.equals("Media")) groupName = "GalleryHome";
		    	if(!groupName.equals("Media") && !groupName.equals("Blog")){
		    		logger.info("Skip extraneous data");
		    		continue;
		    	}
		    	DirectoryGroupType group = Factories.getGroupFactory().getCreateDirectory(user, groupName, user.getHomeDirectory(), user.getOrganization());
		    	
		    	
		    	DataType data = Factories.getDataFactory().newData(user, group);
		    	data.setName(name);
		    	data.setMimeType(mimeType);
		    	data.setDescription(rset.getString("Description"));
		    	byte[] dataComp = new byte[0];
		    	if(rset.getBoolean("IsBlob")){
		    		dataComp = rset.getBytes("DataBlob");
		    		if(rset.getBoolean("IsCompressed")){
		    			dataComp = ZipUtil.gunzipBytes(dataComp);
		    			logger.info("Gunzipped " + dataComp.length + " bytes");
		    		}
		    	}
		    	else{
		    		DataUtil.setValueString(data, rset.getString("DataString"));
		    	}
		    	if(groupName.equals("Blog")){
		    		String[] source = (new String(dataComp)).split("\n");
		    		StringBuffer buff = new StringBuffer();
		    		for(int i = 0; i < source.length;i++){
		    			buff.append("[p]" + source[i] + "[/p]");
		    		}
		    		dataComp = buff.toString().getBytes();
		    	}
		       	DataUtil.setValue(data, dataComp);
		    	data.setDimensions(rset.getString("Dimensions"));
		    	data.setCreatedDate(CalendarUtil.getXmlGregorianCalendar(rset.getTimestamp("CreatedDate")));
		    	data.setModifiedDate(CalendarUtil.getXmlGregorianCalendar(rset.getTimestamp("ModifiedDate")));
		    	logger.info("Migrating data '" + name + "' from " + data.getCreatedDate().toString() + " to group " + group.getName());
		    	BulkFactories.getBulkFactory().createBulkEntry(sessionId, FactoryEnumType.DATA,data);
		    }
		    BulkFactories.getBulkFactory().write(sessionId);
		    rset.close();
		} catch (SQLException e) {
			logger.error(e.getMessage());
		    throw new RuntimeException("Cannot connect the database!", e);
		} catch (ArgumentException e) {
			// TODO Auto-generated catch block
			logger.error(e.getMessage());
			e.printStackTrace();
		} catch (FactoryException e) {
			// TODO Auto-generated catch block
			logger.error(e.getMessage());
			e.printStackTrace();
		} catch (DataAccessException e) {
			// TODO Auto-generated catch block
			logger.error(e.getMessage());
			e.printStackTrace();
		} catch (DataException e) {
			// TODO Auto-generated catch block
			logger.error(e.getMessage());
			e.printStackTrace();
		} finally {
		    System.out.println("Closing the connection.");
		    if (connection != null) try { connection.close(); } catch (SQLException ignore) {}
		}
		
	}
	public static void importDataPath(UserType user, String localPath, String targetPath, boolean isPointer){
		try{
			DirectoryGroupType dir = Factories.getGroupFactory().getCreatePath(user, targetPath, user.getOrganization());
			File f = new File(localPath);
			if(f.exists() == false){
				System.out.println("Source directory " + localPath + " not found");
			}
			if(dir == null){
				System.out.println("Directory " + targetPath + " not found");
			}
			if(f.isDirectory() == false){
				importFile(user, dir, f, isPointer);
			}
			else{
				importDirectory(user,dir, f, isPointer);
			}
		}
		catch(FactoryException fe){
			fe.printStackTrace();
		} catch (ArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (DataException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (DataAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	private static void importBulkFiles(UserType user, DirectoryGroupType dir, List<File> bulkFiles, boolean isPointer) throws ArgumentException, FactoryException, DataAccessException, DataException{
		String sessionId = BulkFactories.getBulkFactory().newBulkSession();

		for(int i = 0; i < bulkFiles.size();i++){
			if(i > 0 && (i % maxLoad) == 0){
				BulkFactories.getBulkFactory().write(sessionId);
				BulkFactories.getBulkFactory().close(sessionId);
				///Factories.clearCaches();
				sessionId = BulkFactories.getBulkFactory().newBulkSession();
			}
			importFile(user, dir, bulkFiles.get(i),sessionId,isPointer);
		}
		BulkFactories.getBulkFactory().write(sessionId);
		BulkFactories.getBulkFactory().close(sessionId);
		//Factories.clearCaches();
	}
	private static void importFile(UserType user, DirectoryGroupType dir, File f, boolean isPointer) throws ArgumentException, DataException, FactoryException{
		importFile(user, dir, f, null, isPointer);
	}
	
	private static void importFile(UserType user, DirectoryGroupType dir, File f, String bulkSession, boolean isPointer) throws ArgumentException, DataException, FactoryException{
		DataType data = Factories.getDataFactory().newData(user, dir);
		if(f.getName().startsWith(".")){
			logger.info("Skipping possible system name: " + f.getName());
			return;
		}
		String fName = f.getName();
		fName = limitNames.matcher(fName).replaceAll("0");
		if(fName.indexOf(".") > -1) data.setMimeType(MimeUtil.getType(fName.substring(fName.lastIndexOf("."), fName.length())));
		if(data.getMimeType() == null) data.setMimeType("application/unknown");
		data.setName(fName);
		data.setPointer(isPointer);
		if(isPointer == false){
			DataUtil.setValue(data, StreamUtil.fileHandleToBytes(f));
		}
		else{
			DataUtil.setValue(data, f.getAbsolutePath().getBytes());
		}
		if(bulkSession == null){
			if(Factories.getDataFactory().addData(data)){
				logger.info("Added " + fName + " to " + dir.getName());
			}
			else{
				logger.warn("Failed to add " + fName + " to " + dir.getName());
			}
		}
		else{
			logger.info("Spooled " + fName + " to " + dir.getName());
			BulkFactories.getBulkFactory().createBulkEntry(bulkSession, FactoryEnumType.DATA, data);
		}
	}
	
	private static void importDirectory(UserType user, DirectoryGroupType dir, File f, boolean isPointer) throws FactoryException, ArgumentException, DataException, DataAccessException{
		if(f.getName().startsWith(".")){
			logger.info("Skipping possible system name: " + f.getName());
			return;
		}
		String fName = limitNames.matcher(f.getName()).replaceAll("");
		DirectoryGroupType tdir = Factories.getGroupFactory().getCreateDirectory(user, fName, dir, user.getOrganization());
		if(tdir == null){
			logger.warn("Invalid directory for " + fName);
			return;
		}

		File[] fs = f.listFiles();
		List<File> bulkList = new ArrayList<File>();
		logger.info("Importing Directory " + f.getName());
		for(int i = 0; i < fs.length; i++){
			if(fs[i].isDirectory()){
				importDirectory(user, tdir, fs[i],isPointer);
			}
			else{
				bulkList.add(fs[i]);
				//importFile(user, tdir, fs[i]);
			}
		}
		importBulkFiles(user, tdir, bulkList,isPointer);
		
	}
}
