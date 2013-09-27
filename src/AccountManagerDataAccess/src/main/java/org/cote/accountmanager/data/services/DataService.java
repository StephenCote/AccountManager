package org.cote.accountmanager.data.services;

import java.util.ArrayList;
import java.util.List;

import org.cote.accountmanager.data.ArgumentException;
import org.cote.accountmanager.data.Factories;
import org.cote.accountmanager.data.FactoryException;
import org.cote.accountmanager.objects.DataParticipantType;
import org.cote.accountmanager.objects.DataTagType;
import org.cote.accountmanager.objects.DataType;
import org.cote.accountmanager.objects.OrganizationType;
import org.cote.accountmanager.objects.types.ParticipantEnumType;

public class DataService {
	public static List<DataType> getDataForTag(DataTagType tag, OrganizationType organization) throws FactoryException, ArgumentException{
		return getDataForTags(new DataTagType[]{tag}, organization);
	}
	public static List<DataType> getDataForTags(DataTagType[] tags, OrganizationType organization) throws FactoryException, ArgumentException{
		List<DataParticipantType> parts = Factories.getTagParticipationFactory().getTagParticipations(tags, ParticipantEnumType.DATA);
		return Factories.getTagParticipationFactory().getDataListFromParticipations(parts.toArray(new DataParticipantType[0]), false, 0, 0, organization);

		//Factories.getTagParticipationFactory().getTagParticipations(tags);
		//List<Core.Tools.AccountManager.Map.DataParticipant> dps = Core.Tools.AccountManager.Factory.TagParticipationFactoryInstance.GetTagParticipations(tags.ToArray());

		//if (dps.Count > 0) active_data_list = Core.Tools.AccountManager.Factory.TagParticipationFactoryInstance.GetDataFromParticipations(dps.ToArray(), true, 0, 0, product.Organization).ToArray();
		//else active_data_list = new Core.Tools.AccountManager.Map.Data[0];
	
	}
}
