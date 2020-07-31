package org.cote.accountmanager.data.services;

import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.cote.accountmanager.data.BulkFactories;
import org.cote.accountmanager.data.DataAccessException;
import org.cote.accountmanager.data.Factories;
import org.cote.accountmanager.data.factory.TagParticipationFactory;
import org.cote.accountmanager.exceptions.ArgumentException;
import org.cote.accountmanager.exceptions.FactoryException;
import org.cote.accountmanager.objects.BaseParticipantType;
import org.cote.accountmanager.objects.BaseTagType;
import org.cote.accountmanager.objects.NameIdType;
import org.cote.accountmanager.objects.UserType;
import org.cote.accountmanager.objects.types.AuditEnumType;
import org.cote.accountmanager.objects.types.FactoryEnumType;
import org.cote.accountmanager.objects.types.ParticipantEnumType;
import org.cote.accountmanager.service.rest.BaseService;

public class TagService {
	public static final Logger logger = LogManager.getLogger(UserService.class);
	public static boolean applyTag(UserType user, BaseTagType tag, NameIdType object, boolean enabled) throws FactoryException, ArgumentException, DataAccessException {
		boolean outBool = false;
		TagParticipationFactory pFact = Factories.getFactory(FactoryEnumType.TAGPARTICIPATION);
		if(tag == null || object == null) {
			logger.warn("Tag or object is null");
			return false;
		}
		if(
			BaseService.canChangeType(AuditEnumType.valueOf(object.getNameType().toString()), user, object)
			&&
			BaseService.canViewType(AuditEnumType.valueOf(tag.getNameType().toString()), user, tag)
		) {
			BaseParticipantType bpt = pFact.getParticipant(tag, object, ParticipantEnumType.valueOf(object.getNameType().toString()));
			if(bpt != null && !enabled) {
				outBool = pFact.delete(bpt);
			}
			else if(bpt == null && enabled) {
				bpt = pFact.newTagParticipation(tag, object);
				outBool = pFact.add(bpt);
			}

		}
		return outBool;
	}
	public static void applyTags(UserType user, BaseTagType[] tags, NameIdType[] objects) throws ArgumentException, FactoryException, DataAccessException{
		List<BaseTagType> authTags = new ArrayList<>();
		List<NameIdType> authObjs = new ArrayList<>();
		TagParticipationFactory pFact = Factories.getFactory(FactoryEnumType.TAGPARTICIPATION);
		for(int i = 0; i < tags.length; i++){
			if(BaseService.canViewType(AuditEnumType.TAG, user,tags[i])){
				authTags.add(tags[i]);
			}
		}
		for(int i = 0; i < objects.length; i++){
			if(BaseService.canViewType(AuditEnumType.valueOf(objects[i].getNameType().toString()), user,objects[i])){
				authObjs.add(objects[i]);
			}
		}
		String sessionId = BulkFactories.getBulkFactory().newBulkSession();
		for(BaseTagType tag : authTags){
			for(NameIdType obj : authObjs){
				if(pFact.getParticipant(tag, obj, ParticipantEnumType.valueOf(obj.getNameType().toString())) != null){
					continue;
				}
				logger.info("Tagging " + obj.getUrn() + " with " + tag.getUrn());
				BaseParticipantType dpt = pFact.newTagParticipation(tag, obj);
				BulkFactories.getBulkFactory().createBulkEntry(sessionId, FactoryEnumType.TAGPARTICIPATION, dpt);
			}
		}
		BulkFactories.getBulkFactory().write(sessionId);
		BulkFactories.getBulkFactory().close(sessionId);
	}
	

}
