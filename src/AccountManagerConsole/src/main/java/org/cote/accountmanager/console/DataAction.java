package org.cote.accountmanager.console;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;
import org.cote.accountmanager.data.ArgumentException;
import org.cote.accountmanager.data.BulkFactories;
import org.cote.accountmanager.data.DataAccessException;
import org.cote.accountmanager.data.Factories;
import org.cote.accountmanager.data.FactoryException;
import org.cote.accountmanager.exceptions.DataException;
import org.cote.accountmanager.objects.DataType;
import org.cote.accountmanager.objects.DirectoryGroupType;
import org.cote.accountmanager.objects.UserType;
import org.cote.accountmanager.objects.types.FactoryEnumType;
import org.cote.accountmanager.util.CalendarUtil;
import org.cote.accountmanager.util.DataUtil;
import org.cote.accountmanager.util.MimeUtil;
import org.cote.accountmanager.util.StreamUtil;
import org.cote.accountmanager.util.ZipUtil;

public class DataAction {
	public static final Logger logger = Logger.getLogger(DataAction.class.getName());
	private static Pattern limitNames = Pattern.compile("([^A-Za-z0-9\\-_\\.\\s])",Pattern.MULTILINE);
	public static void migrateData(UserType user, long sourceOwnerId){
		try {
		    Class.forName("com.mysql.jdbc.Driver");
		} catch (ClassNotFoundException e) {
		    throw new RuntimeException("Cannot find the driver in the classpath!", e);
		}	
		
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
	public static void importDataPath(UserType user, String localPath, String targetPath){
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
				importFile(user, dir, f);
			}
			else{
				importDirectory(user,dir, f);
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
	private static void importBulkFiles(UserType user, DirectoryGroupType dir, List<File> bulkFiles) throws ArgumentException, FactoryException, DataAccessException, DataException{
		String sessionId = BulkFactories.getBulkFactory().newBulkSession();
		int maxLoad = 50;
		for(int i = 0; i < bulkFiles.size();i++){
			if(i > 0 && (i % maxLoad) == 0){
				BulkFactories.getBulkFactory().write(sessionId);
				BulkFactories.getBulkFactory().close(sessionId);
				///Factories.clearCaches();
				sessionId = BulkFactories.getBulkFactory().newBulkSession();
			}
			importFile(user, dir, bulkFiles.get(i),sessionId);
		}
		BulkFactories.getBulkFactory().write(sessionId);
		BulkFactories.getBulkFactory().close(sessionId);
		//Factories.clearCaches();
	}
	private static void importFile(UserType user, DirectoryGroupType dir, File f) throws ArgumentException, DataException, FactoryException{
		importFile(user, dir, f, null);
	}
	
	private static void importFile(UserType user, DirectoryGroupType dir, File f, String bulkSession) throws ArgumentException, DataException, FactoryException{
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
		DataUtil.setValue(data, StreamUtil.fileHandleToBytes(f));
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
	
	private static void importDirectory(UserType user, DirectoryGroupType dir, File f) throws FactoryException, ArgumentException, DataException, DataAccessException{
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
				importDirectory(user, tdir, fs[i]);
			}
			else{
				bulkList.add(fs[i]);
				//importFile(user, tdir, fs[i]);
			}
		}
		importBulkFiles(user, tdir, bulkList);
		
	}
}
