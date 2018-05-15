/*******************************************************************************
 * Copyright (C) 2002, 2017 Stephen Cote Enterprises, LLC. All rights reserved.
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

import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.cote.accountmanager.data.ArgumentException;
import org.cote.accountmanager.data.Factories;
import org.cote.accountmanager.data.query.QueryField;
import org.cote.accountmanager.data.query.QueryFields;
import org.cote.accountmanager.data.services.AuthorizationService;
import org.cote.accountmanager.exceptions.FactoryException;
import org.cote.accountmanager.objects.BaseParticipantType;
import org.cote.accountmanager.objects.FactParticipantType;
import org.cote.accountmanager.objects.FactType;
import org.cote.accountmanager.objects.FunctionFactParticipantType;
import org.cote.accountmanager.objects.FunctionFactType;
import org.cote.accountmanager.objects.NameIdType;
import org.cote.accountmanager.objects.PatternParticipantType;
import org.cote.accountmanager.objects.PatternType;
import org.cote.accountmanager.objects.RuleParticipantType;
import org.cote.accountmanager.objects.RuleType;
import org.cote.accountmanager.objects.types.AffectEnumType;
import org.cote.accountmanager.objects.types.FactoryEnumType;
import org.cote.accountmanager.objects.types.ParticipantEnumType;
import org.cote.accountmanager.objects.types.ParticipationEnumType;




public abstract class AuthorizationParticipationFactory extends BaseParticipationFactory {
	public static final Logger logger = LogManager.getLogger(AuthorizationParticipationFactory.class);
	public AuthorizationParticipationFactory(ParticipationEnumType type, String tableName){
		super(type, tableName);
	}
	public PatternParticipantType newPatternParticipation(NameIdType cycle, PatternType sched) throws ArgumentException,FactoryException {
		return newParticipation(cycle, sched, AuthorizationService.getViewObjectPermission(cycle.getOrganizationId()),AffectEnumType.GRANT_PERMISSION,ParticipantEnumType.PATTERN);
	}

	public FunctionFactParticipantType newFunctionFactParticipation(NameIdType cycle, FunctionFactType sched) throws ArgumentException,FactoryException {
		return newParticipation(cycle, sched, AuthorizationService.getViewObjectPermission(cycle.getOrganizationId()),AffectEnumType.GRANT_PERMISSION,ParticipantEnumType.FUNCTIONFACT);
	}
	public RuleParticipantType newRuleParticipation(NameIdType cycle, RuleType sched) throws ArgumentException,FactoryException {
		return newParticipation(cycle, sched, AuthorizationService.getViewObjectPermission(cycle.getOrganizationId()),AffectEnumType.GRANT_PERMISSION,ParticipantEnumType.RULE);
	}

	
	@Override
	protected BaseParticipantType newParticipant(ParticipantEnumType type) throws ArgumentException
	{
		BaseParticipantType new_participant = null;
		switch (type)
		{
			case RULE:
				new_participant = new RuleParticipantType();
				break;
			case FUNCTIONFACT:
				new_participant = new FunctionFactParticipantType();
				break;
			case PATTERN:
				new_participant = new PatternParticipantType();
				break;
			case FACT:
				new_participant = new FactParticipantType();
				break;
			
			default:
				new_participant = super.newParticipant(type);
				break;
		}
		new_participant.setParticipantType(type);
		return new_participant;
	}
	public List<PatternType> getPatternsFromParticipation(NameIdType participation) throws ArgumentException{
		List<PatternType> items = new ArrayList<PatternType>();
		try{
			PatternParticipantType[] parts = getPatternParticipations(participation).toArray(new PatternParticipantType[0]);
			if(parts.length > 0){
				items = getPatternsFromParticipations(parts, 0, 0, participation.getOrganizationId());
			}
		}
		catch(FactoryException fe){
			logger.error(fe.getMessage());
			logger.error(FactoryException.LOGICAL_EXCEPTION,fe);
		}
		return items;
	}
	public List<PatternParticipantType> getPatternParticipations(NameIdType cycle) throws FactoryException, ArgumentException
	{
		return convertList( getParticipations(new NameIdType[]{cycle}, ParticipantEnumType.PATTERN));
	}
	public List<PatternType> getPatternsFromParticipations(PatternParticipantType[] list, long startRecord, int recordCount, long organizationId) throws FactoryException,ArgumentException
	{
		QueryField field = QueryFields.getFieldParticipantIds(list);
		return Factories.getNameIdFactory(FactoryEnumType.PATTERN).paginateList(new QueryField[]{ field }, startRecord, recordCount, organizationId);
	}

	public List<RuleType> getRulesFromParticipation(NameIdType participation) throws ArgumentException{
		List<RuleType> items = new ArrayList<RuleType>();
		try{
			RuleParticipantType[] parts = getRuleParticipations(participation).toArray(new RuleParticipantType[0]);
			if(parts.length > 0){
				items = getRulesFromParticipations(parts, 0, 0, participation.getOrganizationId());
			}
		}
		catch(FactoryException fe){
			logger.error(fe.getMessage());
			logger.error(FactoryException.LOGICAL_EXCEPTION,fe);
		}
		return items;
	}
	public List<RuleParticipantType> getRuleParticipations(NameIdType cycle) throws FactoryException, ArgumentException
	{
		return convertList( getParticipations(new NameIdType[]{cycle}, ParticipantEnumType.RULE));
	}
	public List<RuleType> getRulesFromParticipations(RuleParticipantType[] list, long startRecord, int recordCount, long organizationId) throws FactoryException,ArgumentException
	{
		QueryField field = QueryFields.getFieldParticipantIds(list);
		return Factories.getNameIdFactory(FactoryEnumType.RULE).paginateList(new QueryField[]{ field }, startRecord, recordCount, organizationId);
	}

	
	public List<FactType> getFactsFromParticipation(NameIdType participation) throws ArgumentException{
		List<FactType> items = new ArrayList<FactType>();
		try{
			FactParticipantType[] parts = getFactParticipations(participation).toArray(new FactParticipantType[0]);
			if(parts.length > 0){
				items = getFactsFromParticipations(parts, 0, 0, participation.getOrganizationId());
			}
		}
		catch(FactoryException fe){
			logger.error(fe.getMessage());
			logger.error(FactoryException.LOGICAL_EXCEPTION,fe);
		}
		return items;
	}
	public List<FactParticipantType> getFactParticipations(NameIdType cycle) throws FactoryException, ArgumentException
	{
		return convertList( getParticipations(new NameIdType[]{cycle}, ParticipantEnumType.FACT));
	}
	public List<FactType> getFactsFromParticipations(FactParticipantType[] list, long startRecord, int recordCount, long organizationId) throws FactoryException,ArgumentException
	{
		QueryField field = QueryFields.getFieldParticipantIds(list);
		return Factories.getNameIdFactory(FactoryEnumType.FACT).paginateList(new QueryField[]{ field }, startRecord, recordCount, organizationId);
	}

	
	public List<FunctionFactType> getFunctionFactsFromParticipation(NameIdType participation) throws ArgumentException{
		List<FunctionFactType> items = new ArrayList<FunctionFactType>();
		try{
			FunctionFactParticipantType[] parts = getFunctionFactParticipations(participation).toArray(new FunctionFactParticipantType[0]);
			if(parts.length > 0){
				items = getFunctionFactsFromParticipations(parts, 0, 0, participation.getOrganizationId());
			}
		}
		catch(FactoryException fe){
			logger.error(fe.getMessage());
			logger.error(FactoryException.LOGICAL_EXCEPTION,fe);
		}
		return items;
	}
	public List<FunctionFactParticipantType> getFunctionFactParticipations(NameIdType cycle) throws FactoryException, ArgumentException
	{
		return convertList( getParticipations(new NameIdType[]{cycle}, ParticipantEnumType.FUNCTIONFACT));
	}
	public List<FunctionFactType> getFunctionFactsFromParticipations(FunctionFactParticipantType[] list, long startRecord, int recordCount, long organizationId) throws FactoryException,ArgumentException
	{
		QueryField field = QueryFields.getFieldParticipantIds(list);
		return Factories.getNameIdFactory(FactoryEnumType.FUNCTIONFACT).paginateList(new QueryField[]{ field }, startRecord, recordCount, organizationId);
	}

}
