package org.cote.accountmanager.data.factory;

import org.cote.accountmanager.objects.types.FactoryEnumType;
import org.cote.accountmanager.objects.types.ParticipationEnumType;


public class ContactInformationParticipationFactory extends BaseParticipationFactory {
	public ContactInformationParticipationFactory(){
		super(ParticipationEnumType.CONTACTINFORMATION, "contactinformationparticipation");
		this.haveAffect = true;
		factoryType = FactoryEnumType.CONTACTINFORMATIONPARTICIPATION;
	}




}