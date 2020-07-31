package org.cote.accountmanager.data.services;

import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.cote.accountmanager.data.Factories;
import org.cote.accountmanager.data.factory.OrganizationFactory;
import org.cote.accountmanager.exceptions.ArgumentException;
import org.cote.accountmanager.exceptions.FactoryException;
import org.cote.accountmanager.objects.AlertEnumType;
import org.cote.accountmanager.objects.MessageSpoolType;
import org.cote.accountmanager.objects.OrganizationType;
import org.cote.accountmanager.objects.types.FactoryEnumType;
import org.cote.accountmanager.objects.types.SpoolStatusEnumType;
import org.cote.accountmanager.services.ThreadService;


/// TODO: Still has hardcoded org references to /FirstContact
public class QueueProcessorJob extends ThreadService {
	
	public static final Logger logger = LogManager.getLogger(QueueProcessorJob.class);
	private int spoolFlushDelay = 30000;
	private OrganizationType org = null;
	public QueueProcessorJob(){
		super();
		this.setThreadDelay(spoolFlushDelay);
		try {
			org = ((OrganizationFactory)Factories.getFactory(FactoryEnumType.ORGANIZATION)).findOrganization("/FirstContact");
		} catch (FactoryException | ArgumentException e) {
			logger.error(e.getMessage());
		}
	}
	
	public void execute(){

		processEvents(SpoolStatusEnumType.QUEUED);
		
		
	}
	private void processEvents(SpoolStatusEnumType spoolStatus){
		AlertEnumType[] aTypes = new AlertEnumType[]{AlertEnumType.DEATH,AlertEnumType.BEREAVEMENT,AlertEnumType.MEDICAL};
		for(int i = 0; i < aTypes.length; i++){
			try {
				List<MessageSpoolType> allEvents = FirstContactMessageService.getEvents(null,aTypes[i],spoolStatus,org.getId());
				logger.debug(aTypes[i].toString() + " " + allEvents.size());
				for(int a = 0; a < allEvents.size();a++){
					MessageSpoolType msg = allEvents.get(a);
					if(FirstContactMessageService.createNotifications(msg)){
						logger.info("Created notifications for event " + msg.getName());
						if(FirstContactMessageService.processNotifications(msg)){
							logger.info("Processed notifications for event " + msg.getName());
						}
						else{
							logger.error("Failed to process notifications for event " + msg.getName());
						}
					}
					else{
						logger.error("Failed to create notifications for event " + msg.getName());
					}
				}
			} catch (FactoryException | ArgumentException e) {
				logger.error(e);
			}
		}

	}

}
