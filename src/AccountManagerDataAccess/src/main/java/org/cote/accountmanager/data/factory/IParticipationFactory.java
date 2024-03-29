/*******************************************************************************
 * Copyright (C) 2002, 2020 Stephen Cote Enterprises, LLC. All rights reserved.
 * Redistribution without modification is permitted provided the following conditions are met:
 *
 *    1. Redistribution may not deviate from the original distribution,
 *        and must reproduce the above copyright notice, this list of conditions
 *        and the following disclaimer in the documentation and/or other materials
 *        provided with the distribution.
 *    2. Products may be derived from this software.
 *    3. Redistributions of any form whatsoever must retain the following acknowledgment:
 *        "This product includes software developed by Stephen Cote Enterprises, LLC"
 *
 * THIS SOFTWARE IS PROVIDED BY STEPHEN COTE ENTERPRISES, LLC ``AS IS''
 * AND ANY EXPRESSED OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO,
 * THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
 * PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THIS PROJECT OR ITS CONTRIBUTORS
 * BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS
 * OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, 
 * STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY 
 * OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *******************************************************************************/
package org.cote.accountmanager.data.factory;

import java.util.List;

import org.cote.accountmanager.exceptions.ArgumentException;
import org.cote.accountmanager.exceptions.FactoryException;
import org.cote.accountmanager.objects.BaseParticipantType;
import org.cote.accountmanager.objects.BasePermissionType;
import org.cote.accountmanager.objects.NameIdType;
import org.cote.accountmanager.objects.ProcessingInstructionType;
import org.cote.accountmanager.objects.types.AffectEnumType;
import org.cote.accountmanager.objects.types.FactoryEnumType;
import org.cote.accountmanager.objects.types.ParticipantEnumType;
import org.cote.accountmanager.objects.types.ParticipationEnumType;
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
		ParticipantEnumType participantType,
		BasePermissionType permission,
		AffectEnumType affectType
	) throws FactoryException, ArgumentException;
	public List<NameIdType> getParticipations(NameIdType[] maps, ParticipantEnumType participantType)  throws FactoryException, ArgumentException;
	public <T> T getParticipant(NameIdType participation, NameIdType participant, ParticipantEnumType type) throws FactoryException, ArgumentException;
	public <T> T getParticipant(
		NameIdType participation,
		NameIdType participant,
		ParticipantEnumType participantType,
		BasePermissionType permission,
		AffectEnumType affectType
	)  throws FactoryException, ArgumentException;

	public BaseParticipantType newParticipant(
		NameIdType participation,
		NameIdType participant,
		ParticipantEnumType participantType,
		BasePermissionType permission,
		AffectEnumType affectType
	) throws ArgumentException;
	
	public <T> List<T> listParticipants(ParticipationEnumType type, ParticipantEnumType ptype, NameIdType[] objects, long startRecord, int recordCount, long organizationId) throws FactoryException, ArgumentException;
	public <T> List<T> listParticipants(NameIdType[] objects, ProcessingInstructionType instruction, ParticipationEnumType type, ParticipantEnumType ptype) throws FactoryException, ArgumentException;
	public int countParticipants(NameIdType[] objects, ParticipantEnumType ptype) throws FactoryException;

	
	public <T> List<T> listParticipations(ParticipantEnumType type, NameIdType[] objects, long startRecord, int recordCount, long organizationId) throws FactoryException, ArgumentException;
	public <T> List<T> listParticipations(NameIdType[] objects, ProcessingInstructionType instruction, ParticipantEnumType type) throws FactoryException, ArgumentException;
	public int countParticipations(NameIdType[] objects, ParticipantEnumType type) throws FactoryException;
	
}