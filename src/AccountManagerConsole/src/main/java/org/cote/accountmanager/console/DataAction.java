package org.cote.accountmanager.console;

import java.io.File;

import org.cote.accountmanager.data.ArgumentException;
import org.cote.accountmanager.data.Factories;
import org.cote.accountmanager.data.FactoryException;
import org.cote.accountmanager.exceptions.DataException;
import org.cote.accountmanager.objects.DataType;
import org.cote.accountmanager.objects.DirectoryGroupType;
import org.cote.accountmanager.objects.UserType;
import org.cote.accountmanager.util.DataUtil;
import org.cote.accountmanager.util.MimeUtil;
import org.cote.accountmanager.util.StreamUtil;

public class DataAction {
	public static void importDataPath(UserType user, String localPath, String targetPath){
		try{
			DirectoryGroupType dir = Factories.getGroupFactory().findGroup(user, targetPath, user.getOrganization());
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
		}
		
	}
	private static void importFile(UserType user, DirectoryGroupType dir, File f) throws ArgumentException, DataException, FactoryException{
		DataType data = Factories.getDataFactory().newData(user, dir);
		if(f.getName().indexOf(".") > -1) data.setMimeType(MimeUtil.getType(f.getName().substring(f.getName().lastIndexOf("."), f.getName().length())));
		else data.setMimeType("application/unknown");
		data.setName(f.getName());
		DataUtil.setValue(data, StreamUtil.fileHandleToBytes(f));
		if(Factories.getDataFactory().addData(data)){
			System.out.println("Added " + f.getName());
		}
		else{
			System.out.println("Failed to add " + f.getName());
		}
	}
	private static void importDirectory(UserType user, DirectoryGroupType dir, File f) throws FactoryException, ArgumentException, DataException{
		DirectoryGroupType tdir = Factories.getGroupFactory().getCreateDirectory(user, f.getName(), dir, user.getOrganization());
		if(tdir == null){
			System.out.println("Invalid directory for " + f.getName());
			return;
		}
		File[] fs = f.listFiles();
		for(int i = 0; i < fs.length; i++){
			if(fs[i].isDirectory()) importDirectory(user, tdir, fs[i]);
			else importFile(user, tdir, fs[i]);
		}
		
	}
}
