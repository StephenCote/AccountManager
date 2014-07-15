package org.cote.accountmanager.data.operation;

import java.util.regex.Pattern;

import org.apache.log4j.Logger;
import org.cote.accountmanager.data.ArgumentException;
import org.cote.accountmanager.data.Factories;
import org.cote.accountmanager.data.FactoryException;
import org.cote.accountmanager.data.factory.DataFactory;
import org.cote.accountmanager.data.factory.GroupFactory;
import org.cote.accountmanager.data.factory.NameIdFactory;
import org.cote.accountmanager.data.factory.NameIdGroupFactory;
import org.cote.accountmanager.data.factory.UserFactory;
import org.cote.accountmanager.objects.DirectoryGroupType;
import org.cote.accountmanager.objects.FactType;
import org.cote.accountmanager.objects.types.GroupEnumType;
;

public class OperationUtil {
	public static final Logger logger = Logger.getLogger(OperationUtil.class.getName());
	private static final Pattern idPattern = Pattern.compile("^\\d+$");
	private static DirectoryGroupType getDirectoryFromFact(FactType sourceFact, FactType referenceFact) throws FactoryException, ArgumentException{
		if(sourceFact.getSourceUrl() == null){
			logger.error("Source URL is null");
			return null;
		}
		DirectoryGroupType dir = (DirectoryGroupType)Factories.getGroupFactory().findGroup(null, GroupEnumType.DATA, sourceFact.getSourceUrl(), referenceFact.getOrganization());
		if(dir == null) throw new ArgumentException("Invalid group path " + sourceFact.getSourceUrl());
		return dir;
	}
	/* 
	 * NOTE: Authorization factories intentionally not included in the lookup by name for rules
	 */
	public static <T> T factoryRead(FactType sourceFact,final FactType referenceFact){
		T out_obj = null;
		if(sourceFact.getSourceUrn() == null){
			logger.error("Source URN is null");
			return out_obj;
		}
		try {
			//out_obj = (T)Factories.getUserFactory().getUserByName(sourceFact.getFactData(), referenceFact.getOrganization());
			NameIdFactory fact = Factories.getFactory(referenceFact.getFactoryType());
			if(idPattern.matcher(sourceFact.getSourceUrn()).matches()){
				out_obj = fact.getById(Long.parseLong(sourceFact.getSourceUrn()), referenceFact.getOrganization());
			}
			else{
				switch(referenceFact.getFactoryType()){
					/// User is one of the only organization level schemas with a unique constraint on just the name
					///
					case USER:
						out_obj = (T)((UserFactory)fact).getUserByName(sourceFact.getSourceUrn(), referenceFact.getOrganization());
						break;
						
					/// NameIdGroupFactory types
					case CONTACT:
					case PERSON:
					case ADDRESS:
						out_obj = (T)((NameIdGroupFactory)fact).getByName(sourceFact.getSourceUrn(), getDirectoryFromFact(sourceFact,referenceFact));
						break;
					/// Data is a predecessor to the NameIdGroupFactory type, but it doesn't inherity from that base class
					case DATA:
						out_obj = (T)((DataFactory)fact).getDataByName(sourceFact.getSourceUrn(), getDirectoryFromFact(sourceFact,referenceFact));
						break;
					case GROUP:
						out_obj = (T)((GroupFactory)fact).getDirectoryByName(sourceFact.getSourceUrn(), getDirectoryFromFact(sourceFact,referenceFact),referenceFact.getOrganization());
						break;
					default:
						throw new ArgumentException("Unhandled factory type " + referenceFact.getFactoryType());
				}
			}
		} catch (FactoryException e) {
			// TODO Auto-generated catch block
			logger.error(e.getMessage());
			e.printStackTrace();
		} catch (ArgumentException e) {
			// TODO Auto-generated catch block
			logger.error(e.getMessage());
			e.printStackTrace();
		}
		return out_obj;
	}

	
	
}
