package org.cote.accountmanager.data.factory;

import java.util.List;

import org.cote.accountmanager.data.ArgumentException;
import org.cote.accountmanager.data.FactoryException;
import org.cote.accountmanager.objects.BaseParticipantType;
import org.cote.accountmanager.objects.BasePermissionType;
import org.cote.accountmanager.objects.NameIdType;
import org.cote.accountmanager.objects.types.AffectEnumType;
import org.cote.accountmanager.objects.types.ParticipantEnumType;
import org.cote.accountmanager.objects.types.PermissionEnumType;


public interface IParticipationFactory extends INameIdFactory {
	public PermissionEnumType getDefaultPermissionType();
	public String getPermissionPrefix();
	public String[] getDefaultPermissions();
	public boolean deleteParticipations(NameIdType source) throws FactoryException;
	public boolean deleteParticipationsByAffects(NameIdType source, long[] permissions) throws FactoryException;
	public boolean deleteParticipationsByAffect(NameIdType source,BasePermissionType permission) throws FactoryException;
	public boolean deleteParticipations(long[] ids, long organizationId) throws FactoryException;
	public boolean deleteParticipantsWithAffect(long[] participationIds, long organizationId) throws FactoryException;
	public boolean deleteParticipantsForParticipation(long[] ids, NameIdType participation, long organizationId) throws FactoryException;
	public boolean deleteParticipants(long[] ids, long organizationId) throws FactoryException;
	public boolean deleteParticipants(BaseParticipantType[] list, long organizationId)  throws FactoryException;
	public List<NameIdType> getParticipants(
		NameIdType participation,
		NameIdType participant,
		ParticipantEnumType participant_type,
		BasePermissionType permission,
		AffectEnumType affect_type
	) throws FactoryException, ArgumentException;
	public List<NameIdType> getParticipations(NameIdType[] maps, ParticipantEnumType participant_type)  throws FactoryException, ArgumentException;
	public <T> T getParticipant(NameIdType participation, NameIdType participant, ParticipantEnumType type) throws FactoryException, ArgumentException;
	public <T> T getParticipant(
		NameIdType participation,
		NameIdType participant,
		ParticipantEnumType participant_type,
		BasePermissionType permission,
		AffectEnumType affect_type
	)  throws FactoryException, ArgumentException;

	public BaseParticipantType newParticipant(
		NameIdType participation,
		NameIdType participant,
		ParticipantEnumType participant_type,
		BasePermissionType permission,
		AffectEnumType affect_type
	) throws ArgumentException;
	
	
}