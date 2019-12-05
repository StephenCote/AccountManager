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
package org.cote.rocket.test;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.Properties;
import java.util.UUID;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.cote.accountmanager.data.ArgumentException;
import org.cote.accountmanager.data.BulkFactories;
import org.cote.accountmanager.data.ConnectionFactory;
import org.cote.accountmanager.data.DataAccessException;
import org.cote.accountmanager.data.factory.DataFactory;
import org.cote.accountmanager.data.factory.FactoryDefaults;
import org.cote.accountmanager.data.factory.GroupFactory;
import org.cote.accountmanager.data.factory.INameIdFactory;
import org.cote.accountmanager.data.factory.OrganizationFactory;
import org.cote.accountmanager.data.factory.UserFactory;
import org.cote.accountmanager.data.security.CredentialService;
import org.cote.accountmanager.data.services.AuditService;
import org.cote.accountmanager.data.services.ICommunityProvider;
import org.cote.accountmanager.data.services.ServiceUtil;
import org.cote.accountmanager.data.services.SessionSecurity;
import org.cote.accountmanager.exceptions.DataException;
import org.cote.accountmanager.exceptions.FactoryException;
import org.cote.accountmanager.objects.AuditType;
import org.cote.accountmanager.objects.CredentialEnumType;
import org.cote.accountmanager.objects.DataType;
import org.cote.accountmanager.objects.DirectoryGroupType;
import org.cote.accountmanager.objects.OrganizationType;
import org.cote.accountmanager.objects.UserType;
import org.cote.accountmanager.objects.types.AuditEnumType;
import org.cote.accountmanager.objects.types.FactoryEnumType;
import org.cote.accountmanager.objects.types.GroupEnumType;
import org.cote.accountmanager.objects.types.OrganizationEnumType;
import org.cote.accountmanager.objects.types.UserEnumType;
import org.cote.accountmanager.objects.types.UserStatusEnumType;
import org.cote.accountmanager.service.rest.BaseService;
import org.cote.accountmanager.util.DataUtil;
import org.cote.accountmanager.util.SecurityUtil;
import org.cote.propellant.objects.ArtifactType;
import org.cote.propellant.objects.BudgetType;
import org.cote.propellant.objects.CostType;
import org.cote.propellant.objects.EstimateType;
import org.cote.propellant.objects.FormElementParticipantType;
import org.cote.propellant.objects.FormElementType;
import org.cote.propellant.objects.FormElementValueType;
import org.cote.propellant.objects.FormType;
import org.cote.propellant.objects.GoalType;
import org.cote.propellant.objects.LifecycleType;
import org.cote.propellant.objects.MethodologyType;
import org.cote.propellant.objects.ModelType;
import org.cote.propellant.objects.NoteType;
import org.cote.propellant.objects.ProcessStepType;
import org.cote.propellant.objects.ProcessType;
import org.cote.propellant.objects.ProjectType;
import org.cote.propellant.objects.RequirementType;
import org.cote.propellant.objects.ResourceType;
import org.cote.propellant.objects.StageType;
import org.cote.propellant.objects.TaskType;
import org.cote.propellant.objects.TicketType;
import org.cote.propellant.objects.TimeType;
import org.cote.propellant.objects.ValidationRuleType;
import org.cote.propellant.objects.WorkType;
import org.cote.propellant.objects.types.ArtifactEnumType;
import org.cote.propellant.objects.types.BudgetEnumType;
import org.cote.propellant.objects.types.CurrencyEnumType;
import org.cote.propellant.objects.types.ElementEnumType;
import org.cote.propellant.objects.types.EstimateEnumType;
import org.cote.propellant.objects.types.GoalEnumType;
import org.cote.propellant.objects.types.ModelEnumType;
import org.cote.propellant.objects.types.ResourceEnumType;
import org.cote.propellant.objects.types.TaskStatusEnumType;
import org.cote.propellant.objects.types.TimeEnumType;
import org.cote.rocket.Factories;
import org.cote.rocket.Rocket;
import org.cote.rocket.RocketSecurity;
import org.cote.rocket.factory.ArtifactFactory;
import org.cote.rocket.factory.BudgetFactory;
import org.cote.rocket.factory.CostFactory;
import org.cote.rocket.factory.EstimateFactory;
import org.cote.rocket.factory.EventFactory;
import org.cote.rocket.factory.FormElementFactory;
import org.cote.rocket.factory.FormElementValueFactory;
import org.cote.rocket.factory.FormFactory;
import org.cote.rocket.factory.FormParticipationFactory;
import org.cote.rocket.factory.GoalFactory;
import org.cote.rocket.factory.MethodologyFactory;
import org.cote.rocket.factory.ModelFactory;
import org.cote.rocket.factory.NoteFactory;
import org.cote.rocket.factory.ProcessFactory;
import org.cote.rocket.factory.ProcessStepFactory;
import org.cote.rocket.factory.RequirementFactory;
import org.cote.rocket.factory.ResourceFactory;
import org.cote.rocket.factory.StageFactory;
import org.cote.rocket.factory.TaskFactory;
import org.cote.rocket.factory.TicketFactory;
import org.cote.rocket.factory.TicketParticipationFactory;
import org.cote.rocket.factory.TimeFactory;
import org.cote.rocket.factory.ValidationRuleFactory;
import org.cote.rocket.factory.WorkFactory;
import org.cote.rocket.util.DataGeneratorUtil;
import org.junit.After;
import org.junit.Before;
public class BaseAccelerantTest{
	public static final Logger logger = LogManager.getLogger(BaseAccelerantTest.class);
	private static ICommunityProvider provider = null;
	private static String testUserName = "RocketQAUser";
	protected static UserType testUser = null;
	private static String sessionId = null;
	private static String testUserName2 = "RocketQAUser2";
	protected static UserType testUser2 = null;
	private static String sessionId2 = null;

	private static String testUserName3 = "RocketQAUser3";
	protected static UserType testUser3 = null;
	private static String sessionId3 = null;

	protected static ModelType model1 = null;
	protected static ModelType model2 = null;
	private static String modelName1 = "Model QA #1";
	private static String modelName2 = "Model QA #2";
	
	protected static TaskType task1 = null;
	protected static TaskType task2 = null;
	private static String taskName1 = "Task QA #1";
	private static String taskName2 = "Task QA #2";
	protected static Properties testProperties = null;
	protected static OrganizationType testOrganization = null;
	
	@Before
	public void setUp() throws Exception {
		
		File cacheDir = new File("./cache");
		if(cacheDir.exists() == false) cacheDir.mkdirs();
		//FactoryBase.setEnableSchemaCache(true);
		//FactoryBase.setSchemaCachePath("./cache");
		
		if(testProperties == null){
			testProperties = new Properties();
		
			try {
				InputStream fis = ClassLoader.getSystemResourceAsStream("./resource.properties"); 
						//new FileInputStream("./resource.properties");
				
				testProperties.load(fis);
				fis.close();
			} catch (IOException e) {
				
				logger.error(FactoryException.LOGICAL_EXCEPTION,e);
				return;
			}
		}
		ConnectionFactory.setupConnectionFactory(testProperties);
		
		
		sessionId = UUID.randomUUID().toString();
		sessionId2 = UUID.randomUUID().toString();
		sessionId3 = UUID.randomUUID().toString();
		

		org.cote.rocket.Factories.warmUp();

		org.cote.accountmanager.service.util.ServiceUtil.setUseAccountManagerSession(false);
		
		try{
			testOrganization = Factories.getDevelopmentOrganization();
			assertNotNull("Test organization is null", testOrganization);
			if(Rocket.isApplicationEnvironmentConfigured(testOrganization.getId())==false){
				Rocket.configureApplicationEnvironment(testOrganization.getId(),"password");
			}
			testUser = SessionSecurity.login(sessionId, testUserName, CredentialEnumType.HASHED_PASSWORD,"password",testOrganization.getId());

		}
		catch(FactoryException | ArgumentException | NullPointerException fe){
			logger.error(FactoryException.TRACE_EXCEPTION,fe);
		}


		if(testUser == null){
			UserType new_user = ((UserFactory)Factories.getNameIdFactory(FactoryEnumType.USER)).newUser(testUserName, UserEnumType.NORMAL, UserStatusEnumType.NORMAL,testOrganization.getId());
			if(((UserFactory)Factories.getNameIdFactory(FactoryEnumType.USER)).add(new_user,  true)){
				new_user = Factories.getNameIdFactory(FactoryEnumType.USER).getByName(testUserName, testOrganization.getId());
				CredentialService.newHashedPasswordCredential(new_user, new_user, "password", true);

				testUser = SessionSecurity.login(sessionId, testUserName, CredentialEnumType.HASHED_PASSWORD,"password",testOrganization.getId());
			}
			else{
				logger.error("Failed to add new user: " + testUserName);
			}

		}
		
		try{
			testUser2 = SessionSecurity.login(sessionId2, testUserName2, CredentialEnumType.HASHED_PASSWORD,"password",testOrganization.getId());
	
		}
		catch(FactoryException | ArgumentException | NullPointerException fe){
			logger.error(FactoryException.TRACE_EXCEPTION,fe);
		}

		if(testUser2 == null){
			UserType new_user = ((UserFactory)Factories.getNameIdFactory(FactoryEnumType.USER)).newUser(testUserName2, UserEnumType.NORMAL, UserStatusEnumType.NORMAL,testOrganization.getId());
			if(((UserFactory)Factories.getNameIdFactory(FactoryEnumType.USER)).add(new_user,  true)){
				new_user = Factories.getNameIdFactory(FactoryEnumType.USER).getByName(testUserName2, testOrganization.getId());
				CredentialService.newHashedPasswordCredential(new_user, new_user, "password", true);

				testUser2 = SessionSecurity.login(sessionId2, testUserName2, CredentialEnumType.HASHED_PASSWORD,"password",testOrganization.getId());
			}
			else{
				logger.error("Failed to add new user: " + testUserName2);
			}

		}

		try{
			testUser3 = SessionSecurity.login(sessionId3, testUserName3, CredentialEnumType.HASHED_PASSWORD,"password",testOrganization.getId());
		}
		catch(FactoryException | ArgumentException | NullPointerException fe){
			logger.error(FactoryException.TRACE_EXCEPTION,fe);
		}

		if(testUser3 == null){
			UserType new_user = ((UserFactory)Factories.getNameIdFactory(FactoryEnumType.USER)).newUser(testUserName3, UserEnumType.NORMAL, UserStatusEnumType.NORMAL,testOrganization.getId());
			if(((UserFactory)Factories.getNameIdFactory(FactoryEnumType.USER)).add(new_user,  true)){
				new_user = Factories.getNameIdFactory(FactoryEnumType.USER).getByName(testUserName3, testOrganization.getId());
				CredentialService.newHashedPasswordCredential(new_user, new_user, "password", true);

				testUser3 = SessionSecurity.login(sessionId3, testUserName3, CredentialEnumType.HASHED_PASSWORD,"password",testOrganization.getId());
			}
			else{
				logger.error("Failed to add new user: " + testUserName3);
			}
		}

		
		logger.info("Setting up default models");
		
		try{
			DirectoryGroupType dir = ((GroupFactory)Factories.getFactory(FactoryEnumType.GROUP)).getCreateDirectory(testUser, "Rocket", testUser.getHomeDirectory(), testUser.getOrganizationId());
			model1 = ((ModelFactory)Factories.getFactory(FactoryEnumType.MODEL)).getByNameInGroup(modelName1, dir);
			if(model1 == null){
				model1 = ((ModelFactory)Factories.getFactory(FactoryEnumType.MODEL)).newModel(testUser, dir.getId());
				model1.setName(modelName1);
				model1.setModelType(ModelEnumType.ATOMIC);
				model1.setDescription("QA Model");
				((ModelFactory)Factories.getFactory(FactoryEnumType.MODEL)).add(model1);
				model1 = ((ModelFactory)Factories.getFactory(FactoryEnumType.MODEL)).getByNameInGroup(modelName1, dir);
			}
			model2 = ((ModelFactory)Factories.getFactory(FactoryEnumType.MODEL)).getByNameInGroup(modelName2, dir);
			if(model2 == null){
				model2 = ((ModelFactory)Factories.getFactory(FactoryEnumType.MODEL)).newModel(testUser, dir.getId());
				model2.setName(modelName2);
				model2.setModelType(ModelEnumType.ATOMIC);
				model2.setDescription("QA Model");
				((ModelFactory)Factories.getFactory(FactoryEnumType.MODEL)).add(model2);
				model2 = ((ModelFactory)Factories.getFactory(FactoryEnumType.MODEL)).getByNameInGroup(modelName2, dir);
			}
			task1 = ((TaskFactory)Factories.getFactory(FactoryEnumType.TASK)).getByNameInGroup(taskName1, dir);
			if(task1 == null){
				task1 = ((TaskFactory)Factories.getFactory(FactoryEnumType.TASK)).newTask(testUser, dir.getId());
				task1.setName(taskName1);
				task1.setTaskStatus(TaskStatusEnumType.WASTE);
				task1.setDescription("QA task");
				task1.setEstimate(newEstimate(taskName1,4,0));
				((TaskFactory)Factories.getFactory(FactoryEnumType.TASK)).add(task1);
				task1 = ((TaskFactory)Factories.getFactory(FactoryEnumType.TASK)).getByNameInGroup(taskName1, dir);
				
				
			}
			task2 = ((TaskFactory)Factories.getFactory(FactoryEnumType.TASK)).getByNameInGroup(taskName2, dir);
			if(task2 == null){
				task2 = ((TaskFactory)Factories.getFactory(FactoryEnumType.TASK)).newTask(testUser, dir.getId());
				task2.setName(taskName2);
				task2.setTaskStatus(TaskStatusEnumType.WASTE);
				task2.setDescription("QA task");
				task2.setEstimate(newEstimate(taskName2,4,0));
				((TaskFactory)Factories.getFactory(FactoryEnumType.TASK)).add(task2);
				task2 = ((TaskFactory)Factories.getFactory(FactoryEnumType.TASK)).getByNameInGroup(taskName2, dir);
			}			
		}
		catch(FactoryException fe){
			logger.error(fe.getMessage());
		}

	}
	
	@After
	public void tearDown() throws Exception{
		//logger.info("Cleanup session: " + sessionId);
		SessionSecurity.logout(sessionId, testOrganization.getId());
	}

	public DataGeneratorUtil getGenerator(AuditType audit, UserType user, String communityName, String projectName, String locationPath, String traitPath, String dictionaryPath, String namesPath){
		DataGeneratorUtil dutil = new DataGeneratorUtil(
				user,
				communityName,
				projectName,
				locationPath,
				traitPath,
				dictionaryPath,
				namesPath
			);
		try{
			if(dutil.initialize() == false){
				AuditService.denyResult(audit, "Failed to initialize data generator");
				return null;
			}
			if(dutil.getProject() == null){
				AuditService.denyResult(audit, "Failed to load project via data generator");
				return null;
			}
		}
		catch(ArgumentException | FactoryException e){
			AuditService.denyResult(audit, String.format(FactoryException.LOGICAL_EXCEPTION_MSG, e.getMessage()));
			logger.error(FactoryException.LOGICAL_EXCEPTION,e);
			dutil = null;
		}
		return dutil;
	}
	
	public UserType getAdminUser(long organizationId){
		UserType admin = null;
		 try {
			admin = ((INameIdFactory)Factories.getFactory(FactoryEnumType.USER)).getByName("Admin", testUser.getOrganizationId());
		} catch (FactoryException | ArgumentException e) {
			logger.error(e);
		}
		 return admin;
	}
	public HttpServletRequestMock getRequestMock(UserType user){
		HttpServletRequestMock request = new HttpServletRequestMock(user.getSession().getSessionId());
		request.addCookie("OrganizationId", user.getOrganizationId().toString());
		return request;
	}
	
	public TicketType newTicket(String name){
		TicketType ticket = null;
		DirectoryGroupType dir;
		try {
			dir = ((GroupFactory)Factories.getFactory(FactoryEnumType.GROUP)).getCreateDirectory(testUser, "Rocket", testUser.getHomeDirectory(), testUser.getOrganizationId());
			ticket = ((TicketFactory)Factories.getFactory(FactoryEnumType.TICKET)).newTicket(testUser, dir.getId());
			ticket.setName(name);
			ticket.setActualCost(newCost("Actual" + name,4));
			ticket.setActualTime(newTime("Actual" + name, 4));
			ticket.setAssignedResource(newResource(name,0));
			ticket.setEstimate(newEstimate(name,4,4));
			((TicketFactory)Factories.getFactory(FactoryEnumType.TICKET)).add(ticket);
			ticket = ((TicketFactory)Factories.getFactory(FactoryEnumType.TICKET)).getByNameInGroup(name, dir);
			
			((TicketParticipationFactory)Factories.getFactory(FactoryEnumType.TICKETPARTICIPATION)).add(((TicketParticipationFactory)Factories.getFactory(FactoryEnumType.TICKETPARTICIPATION)).newNoteParticipation(ticket, newNote(name,"Ticket note #1")));
			
		} catch (FactoryException e) {
			
			logger.error(FactoryException.LOGICAL_EXCEPTION,e);
		} catch (ArgumentException e) {
			
			logger.error(FactoryException.LOGICAL_EXCEPTION,e);
		}
		
		return ticket;
	}
	public TicketType getTicket(long id){
		TicketType ticket = null;
		try {
			ticket = ((TicketFactory)Factories.getFactory(FactoryEnumType.TICKET)).getById(id, testUser.getOrganizationId());
		} catch (FactoryException e) {
			
			logger.error(FactoryException.LOGICAL_EXCEPTION,e);
		} catch (ArgumentException e) {
			
			logger.error(FactoryException.LOGICAL_EXCEPTION,e);
		}
		
		return ticket;
	}
	
	public boolean updateTicket(TicketType ticket){
		boolean outBool = false;
		try {
			outBool = ((TicketFactory)Factories.getFactory(FactoryEnumType.TICKET)).update(ticket);
		} catch (FactoryException e) {
			
			logger.error(FactoryException.LOGICAL_EXCEPTION,e);
		}
		
		return outBool;
	}
	public boolean newFormParticipant(FormType form, FormElementType formElement){
		FormElementParticipantType fep;
		boolean update = false;
		
		try {
			fep = ((FormParticipationFactory)Factories.getFactory(FactoryEnumType.FORMPARTICIPATION)).newFormElementParticipation(form,  formElement);
			update = ((FormParticipationFactory)Factories.getFactory(FactoryEnumType.FORMPARTICIPATION)).add(fep);
		} catch (ArgumentException e) {
			
			logger.error(FactoryException.LOGICAL_EXCEPTION,e);
		} catch (FactoryException e) {
			
			logger.error(FactoryException.LOGICAL_EXCEPTION,e);
		} 
		

		return update;
	}
	
	public FormType newForm(String name){
		FormType form = null;
		try{
			DirectoryGroupType dir = ((GroupFactory)Factories.getFactory(FactoryEnumType.GROUP)).getCreateDirectory(testUser, "Rocket", testUser.getHomeDirectory(), testUser.getOrganizationId());
			form = ((FormFactory)Factories.getFactory(FactoryEnumType.FORM)).newForm(testUser,  dir.getId());
			form.setName(name);
			((FormFactory)Factories.getFactory(FactoryEnumType.FORM)).add(form);
			form = ((FormFactory)Factories.getFactory(FactoryEnumType.FORM)).getByNameInGroup(name,dir);
		}
		catch(FactoryException fe){
			logger.error(fe.getMessage());
			logger.error(FactoryException.LOGICAL_EXCEPTION,fe);
		} catch (ArgumentException e) {
			
			logger.error(FactoryException.LOGICAL_EXCEPTION,e);
		}
		return form;
	}
	public FormType getCreateForm(UserType user, DirectoryGroupType group, String name){
		return getCreateForm(user, group, name, null);
	}
	public FormType getCreateForm(UserType user, DirectoryGroupType group, String name, FormType template){
		FormType form = null;
		try{
			form = ((FormFactory)Factories.getFactory(FactoryEnumType.FORM)).getByNameInGroup(name,group);
			if(form == null){
				form = ((FormFactory)Factories.getFactory(FactoryEnumType.FORM)).newForm(user,  group.getId());
				form.setName(name);
				if(template != null){
					form.setTemplate(template);
				}
				else{
					form.setIsTemplate(true);
				}
				((FormFactory)Factories.getFactory(FactoryEnumType.FORM)).add(form);
				form =	((FormFactory)Factories.getFactory(FactoryEnumType.FORM)).getByNameInGroup(name,group);
			}
			if(form != null) ((FormFactory)Factories.getFactory(FactoryEnumType.FORM)).populate(form);
		}
		catch(FactoryException fe){
			logger.error(fe.getMessage());
			logger.error(FactoryException.LOGICAL_EXCEPTION,fe);
		} catch (ArgumentException e) {
			
			logger.error(FactoryException.LOGICAL_EXCEPTION,e);
		}

		return form;
	}
	public boolean updateForm(FormType work){
		boolean updated = false;
		try {
			updated = ((FormFactory)Factories.getFactory(FactoryEnumType.FORM)).update(work);
		} catch (FactoryException e) {
			
			logger.error(FactoryException.LOGICAL_EXCEPTION,e);
		}
		return updated;
	}
	
	public FormType getForm(long id){
		FormType work = null;
		try {
			work = ((FormFactory)Factories.getFactory(FactoryEnumType.FORM)).getById(id, testUser.getOrganizationId());
		} catch (FactoryException e) {
			
			logger.error(FactoryException.LOGICAL_EXCEPTION,e);
		} catch (ArgumentException e) {
			
			logger.error(FactoryException.LOGICAL_EXCEPTION,e);
		}
		return work;
	}
	
	public FormElementType newFormElement(String name){
		FormElementType FormElement = null;
		try{
			DirectoryGroupType dir = ((GroupFactory)Factories.getFactory(FactoryEnumType.GROUP)).getCreateDirectory(testUser, "Rocket", testUser.getHomeDirectory(), testUser.getOrganizationId());
			FormElement = ((FormElementFactory)Factories.getFactory(FactoryEnumType.FORMELEMENT)).newFormElement(testUser, dir.getId());
			FormElement.setName(name);
			((FormElementFactory)Factories.getFactory(FactoryEnumType.FORMELEMENT)).add(FormElement);
			FormElement = ((FormElementFactory)Factories.getFactory(FactoryEnumType.FORMELEMENT)).getByNameInGroup(name,dir);
		}
		catch(FactoryException fe){
			logger.error(fe.getMessage());
			logger.error(FactoryException.LOGICAL_EXCEPTION,fe);
		} catch (ArgumentException e) {
			
			logger.error(FactoryException.LOGICAL_EXCEPTION,e);
		}
		return FormElement;
	}
	
	public boolean updateFormElement(FormElementType work){
		boolean updated = false;
		try {
			updated = ((FormElementFactory)Factories.getFactory(FactoryEnumType.FORMELEMENT)).update(work);
		} catch (FactoryException e) {
			
			logger.error(FactoryException.LOGICAL_EXCEPTION,e);
		}
		return updated;
	}
	public FormElementType getFormElement(long id){
		FormElementType work = null;
		try {
			work = ((FormElementFactory)Factories.getFactory(FactoryEnumType.FORMELEMENT)).getById(id, testUser.getOrganizationId());
		} catch (FactoryException e) {
			
			logger.error(FactoryException.LOGICAL_EXCEPTION,e);
		} catch (ArgumentException e) {
			
			logger.error(FactoryException.LOGICAL_EXCEPTION,e);
		}
		return work;
	}
	public FormElementType getCreateFormElement(UserType user, DirectoryGroupType group, String name){
		return getCreateFormElement(user, group, name, ElementEnumType.STRING);
	}
	public FormElementType getCreateFormElement(UserType user, DirectoryGroupType group, String name, ElementEnumType elType){
		FormElementType fet = null;
		try{
			fet = ((FormElementFactory)Factories.getFactory(FactoryEnumType.FORMELEMENT)).getByNameInGroup(name, group);
			if(fet == null){
				fet = ((FormElementFactory)Factories.getFactory(FactoryEnumType.FORMELEMENT)).newFormElement(user, group.getId());
				fet.setName(name);
				fet.setElementName(name);
				fet.setElementType(elType);
				((FormElementFactory)Factories.getFactory(FactoryEnumType.FORMELEMENT)).add(fet);
				fet = ((FormElementFactory)Factories.getFactory(FactoryEnumType.FORMELEMENT)).getByNameInGroup(name, group);
			}
		
		} catch (FactoryException e) {
			
			logger.error(FactoryException.LOGICAL_EXCEPTION,e);
		} catch (ArgumentException e) {
			
			logger.error(FactoryException.LOGICAL_EXCEPTION,e);
		}
		return fet;
	}
	public ValidationRuleType getCreateRule(UserType user, DirectoryGroupType group, String name, String expression){
		ValidationRuleType fetRule = null;
		try{
			fetRule = ((ValidationRuleFactory)Factories.getFactory(FactoryEnumType.VALIDATIONRULE)).getByNameInGroup(name, group);
			if(fetRule == null){
				fetRule = ((ValidationRuleFactory)Factories.getFactory(FactoryEnumType.VALIDATIONRULE)).newValidationRule(user, group.getId());
				fetRule.setName(name);
				fetRule.setAllowNull(false);
				fetRule.setComparison(false);
				fetRule.setExpression(expression);
				fetRule.setIsReplacementRule(false);
				fetRule.setIsRuleSet(false);
				((ValidationRuleFactory)Factories.getFactory(FactoryEnumType.VALIDATIONRULE)).add(fetRule);
				fetRule = ((ValidationRuleFactory)Factories.getFactory(FactoryEnumType.VALIDATIONRULE)).getByNameInGroup(name, group);
			}
		
		} catch (FactoryException e) {
			
			logger.error(FactoryException.LOGICAL_EXCEPTION,e);
		} catch (ArgumentException e) {
			
			logger.error(FactoryException.LOGICAL_EXCEPTION,e);
		}
		return fetRule;
	}

	public DataType newTextData(String name, String value, UserType owner, DirectoryGroupType dir){
		DataType data = null;
		try{
			data = ((DataFactory)Factories.getFactory(FactoryEnumType.DATA)).newData(owner, dir.getId());
			data.setName(name);
			data.setMimeType("text/plain");
			DataUtil.setValueString(data, value);
			((DataFactory)Factories.getFactory(FactoryEnumType.DATA)).add(data);
			data = ((DataFactory)Factories.getFactory(FactoryEnumType.DATA)).getDataByName(name, dir);
		}
		catch(FactoryException fe){
			logger.error(fe.getMessage());
			logger.error(FactoryException.LOGICAL_EXCEPTION,fe);
		} catch (ArgumentException e) {
			
			logger.error(FactoryException.LOGICAL_EXCEPTION,e);
		} catch (DataException e) {
			
			logger.error(FactoryException.LOGICAL_EXCEPTION,e);
		}
		return data;
	}
	public FormElementValueType newFormElementValue(String name, String value, boolean isBinary, FormType form, FormElementType formElement){
		FormElementValueType FormElementValue = null;

		try{
			DirectoryGroupType dir = ((GroupFactory)Factories.getFactory(FactoryEnumType.GROUP)).getCreateDirectory(testUser, "Rocket", testUser.getHomeDirectory(), testUser.getOrganizationId());
			FormElementValue = ((FormElementValueFactory)Factories.getFactory(FactoryEnumType.FORMELEMENTVALUE)).newFormElementValue(testUser, form, formElement);
			FormElementValue.setName(name);
			FormElementValue.setIsBinary(isBinary);
			if(isBinary && formElement.getElementType() == ElementEnumType.DATA){
				DataType newData = newTextData(name,value,testUser,dir);
				FormElementValue.setBinaryId(newData.getId());
			}
			else{
				FormElementValue.setTextValue(value);
			}
			((FormElementValueFactory)Factories.getFactory(FactoryEnumType.FORMELEMENTVALUE)).add(FormElementValue);
			FormElementValue = ((FormElementValueFactory)Factories.getFactory(FactoryEnumType.FORMELEMENTVALUE)).getByNameInGroup(name, form, formElement);
			if(FormElementValue != null){
				formElement.getElementValues().clear();
				formElement.setPopulated(false);
			}
		}
		catch(FactoryException fe){
			logger.error(fe.getMessage());
			logger.error(FactoryException.LOGICAL_EXCEPTION,fe);
		} catch (ArgumentException e) {
			
			logger.error(FactoryException.LOGICAL_EXCEPTION,e);
		}
		return FormElementValue;
	}
	
	public boolean updateFormElementValue(FormElementValueType work){
		boolean updated = false;
		try {
			updated = ((FormElementValueFactory)Factories.getFactory(FactoryEnumType.FORMELEMENTVALUE)).update(work);
		} catch (FactoryException e) {
			
			logger.error(FactoryException.LOGICAL_EXCEPTION,e);
		}
		return updated;
	}
	public FormElementValueType getFormElementValue(long id){
		FormElementValueType work = null;
		try {
			work = ((FormElementValueFactory)Factories.getFactory(FactoryEnumType.FORMELEMENTVALUE)).getById(id, testUser.getOrganizationId());
		} catch (FactoryException e) {
			
			logger.error(FactoryException.LOGICAL_EXCEPTION,e);
		} catch (ArgumentException e) {
			
			logger.error(FactoryException.LOGICAL_EXCEPTION,e);
		}
		return work;
	}
	
	public BudgetType newBudget(String name){
		BudgetType resource = null;
		try{
			DirectoryGroupType dir = ((GroupFactory)Factories.getFactory(FactoryEnumType.GROUP)).getCreateDirectory(testUser, "Rocket", testUser.getHomeDirectory(), testUser.getOrganizationId());
			resource = ((BudgetFactory)Factories.getFactory(FactoryEnumType.BUDGET)).newBudget(testUser, dir.getId());
			resource.setBudgetType(BudgetEnumType.MULTIFACTOR);
			resource.setCost(newCost(name,4));
			resource.setTime(newTime(name,4));
			resource.setName(name);
			((BudgetFactory)Factories.getFactory(FactoryEnumType.BUDGET)).add(resource);
			resource = ((BudgetFactory)Factories.getFactory(FactoryEnumType.BUDGET)).getByNameInGroup(name,  dir);
		}
		catch(FactoryException fe){
			logger.error(fe.getMessage());
			logger.error(FactoryException.LOGICAL_EXCEPTION,fe);
		} catch (ArgumentException e) {
			
			logger.error(FactoryException.LOGICAL_EXCEPTION,e);
		}
		return resource;
	}
	public boolean updateBudget(BudgetType work){
		boolean updated = false;
		try {
			updated = ((BudgetFactory)Factories.getFactory(FactoryEnumType.BUDGET)).update(work);
		} catch (FactoryException e) {
			
			logger.error(FactoryException.LOGICAL_EXCEPTION,e);
		}
		return updated;
	}
	public BudgetType getBudget(long id){
		BudgetType work = null;
		try {
			work = ((BudgetFactory)Factories.getFactory(FactoryEnumType.BUDGET)).getById(id, testUser.getOrganizationId());
		} catch (FactoryException e) {
			
			logger.error(FactoryException.LOGICAL_EXCEPTION,e);
		} catch (ArgumentException e) {
			
			logger.error(FactoryException.LOGICAL_EXCEPTION,e);
		}
		return work;
	}
	public StageType newStage(String name){
		StageType resource = null;
		try{
			DirectoryGroupType dir = ((GroupFactory)Factories.getFactory(FactoryEnumType.GROUP)).getCreateDirectory(testUser, "Rocket", testUser.getHomeDirectory(), testUser.getOrganizationId());
			resource = ((StageFactory)Factories.getFactory(FactoryEnumType.STAGE)).newStage(testUser, dir.getId());
			
			resource.setName(name);
			((StageFactory)Factories.getFactory(FactoryEnumType.STAGE)).add(resource);
			resource = ((StageFactory)Factories.getFactory(FactoryEnumType.STAGE)).getByNameInGroup(name,  dir);
		}
		catch(FactoryException fe){
			logger.error(fe.getMessage());
			logger.error(FactoryException.LOGICAL_EXCEPTION,fe);
		} catch (ArgumentException e) {
			
			logger.error(FactoryException.LOGICAL_EXCEPTION,e);
		}
		return resource;
	}
	public boolean updateStage(StageType work){
		boolean updated = false;
		try {
			updated = ((StageFactory)Factories.getFactory(FactoryEnumType.STAGE)).update(work);
		} catch (FactoryException e) {
			
			logger.error(FactoryException.LOGICAL_EXCEPTION,e);
		}
		return updated;
	}
	public StageType getStage(long id){
		StageType work = null;
		try {
			work = ((StageFactory)Factories.getFactory(FactoryEnumType.STAGE)).getById(id, testUser.getOrganizationId());
		} catch (FactoryException e) {
			
			logger.error(FactoryException.LOGICAL_EXCEPTION,e);
		} catch (ArgumentException e) {
			
			logger.error(FactoryException.LOGICAL_EXCEPTION,e);
		}
		return work;
	}
	public GoalType newGoal(String name){
		GoalType resource = null;
		try{
			DirectoryGroupType dir = ((GroupFactory)Factories.getFactory(FactoryEnumType.GROUP)).getCreateDirectory(testUser, "Rocket", testUser.getHomeDirectory(), testUser.getOrganizationId());
			resource = ((GoalFactory)Factories.getFactory(FactoryEnumType.GOAL)).newGoal(testUser, dir.getId());
			resource.setName(name);
			resource.setGoalType(GoalEnumType.STAKE);
			((GoalFactory)Factories.getFactory(FactoryEnumType.GOAL)).add(resource);
			resource = ((GoalFactory)Factories.getFactory(FactoryEnumType.GOAL)).getByNameInGroup(name,  dir);
		}
		catch(FactoryException fe){
			logger.error(fe.getMessage());
			logger.error(FactoryException.LOGICAL_EXCEPTION,fe);
		} catch (ArgumentException e) {
			
			logger.error(FactoryException.LOGICAL_EXCEPTION,e);
		}
		return resource;
	}
	public boolean updateGoal(GoalType work){
		boolean updated = false;
		try {
			updated = ((GoalFactory)Factories.getFactory(FactoryEnumType.GOAL)).update(work);
		} catch (FactoryException e) {
			
			logger.error(FactoryException.LOGICAL_EXCEPTION,e);
		}
		return updated;
	}
	public GoalType getGoal(long id){
		GoalType work = null;
		try {
			work = ((GoalFactory)Factories.getFactory(FactoryEnumType.GOAL)).getById(id, testUser.getOrganizationId());
		} catch (FactoryException e) {
			
			logger.error(FactoryException.LOGICAL_EXCEPTION,e);
		} catch (ArgumentException e) {
			
			logger.error(FactoryException.LOGICAL_EXCEPTION,e);
		}
		return work;
	}
	public ProcessStepType newProcessStep(String name){
		ProcessStepType resource = null;
		try{
			DirectoryGroupType dir = ((GroupFactory)Factories.getFactory(FactoryEnumType.GROUP)).getCreateDirectory(testUser, "Rocket", testUser.getHomeDirectory(), testUser.getOrganizationId());
			resource = ((ProcessStepFactory)Factories.getFactory(FactoryEnumType.PROCESSSTEP)).newProcessStep(testUser, dir.getId());
			resource.setName(name);
			((ProcessStepFactory)Factories.getFactory(FactoryEnumType.PROCESSSTEP)).add(resource);
			resource = ((ProcessStepFactory)Factories.getFactory(FactoryEnumType.PROCESSSTEP)).getByNameInGroup(name,  dir);
		}
		catch(FactoryException fe){
			logger.error(fe.getMessage());
			logger.error(FactoryException.LOGICAL_EXCEPTION,fe);
		} catch (ArgumentException e) {
			
			logger.error(FactoryException.LOGICAL_EXCEPTION,e);
		}
		return resource;
	}
	public boolean updateProcessStep(ProcessStepType work){
		boolean updated = false;
		try {
			updated = ((ProcessStepFactory)Factories.getFactory(FactoryEnumType.PROCESSSTEP)).update(work);
		} catch (FactoryException e) {
			
			logger.error(FactoryException.LOGICAL_EXCEPTION,e);
		}
		return updated;
	}
	public ProcessStepType getProcessStep(long id){
		ProcessStepType work = null;
		try {
			work = ((ProcessStepFactory)Factories.getFactory(FactoryEnumType.PROCESSSTEP)).getById(id, testUser.getOrganizationId());
		} catch (FactoryException e) {
			
			logger.error(FactoryException.LOGICAL_EXCEPTION,e);
		} catch (ArgumentException e) {
			
			logger.error(FactoryException.LOGICAL_EXCEPTION,e);
		}
		return work;
	}
	
	public ProcessType newProcess(String name){
		ProcessType resource = null;
		try{
			DirectoryGroupType dir = ((GroupFactory)Factories.getFactory(FactoryEnumType.GROUP)).getCreateDirectory(testUser, "Rocket", testUser.getHomeDirectory(), testUser.getOrganizationId());
			resource = ((ProcessFactory)Factories.getFactory(FactoryEnumType.PROCESS)).newProcess(testUser, dir.getId());

			resource.setName(name);
			((ProcessFactory)Factories.getFactory(FactoryEnumType.PROCESS)).add(resource);
			resource = ((ProcessFactory)Factories.getFactory(FactoryEnumType.PROCESS)).getByNameInGroup(name,  dir);
		}
		catch(FactoryException fe){
			logger.error(fe.getMessage());
			logger.error(FactoryException.LOGICAL_EXCEPTION,fe);
		} catch (ArgumentException e) {
			
			logger.error(FactoryException.LOGICAL_EXCEPTION,e);
		}
		return resource;
	}
	public boolean updateProcess(ProcessType work){
		boolean updated = false;
		try {
			updated = ((ProcessFactory)Factories.getFactory(FactoryEnumType.PROCESS)).update(work);
		} catch (FactoryException e) {
			
			logger.error(FactoryException.LOGICAL_EXCEPTION,e);
		}
		return updated;
	}
	public ProcessType getProcess(long id){
		ProcessType work = null;
		try {
			work = ((ProcessFactory)Factories.getFactory(FactoryEnumType.PROCESS)).getById(id, testUser.getOrganizationId());
		} catch (FactoryException e) {
			
			logger.error(FactoryException.LOGICAL_EXCEPTION,e);
		} catch (ArgumentException e) {
			
			logger.error(FactoryException.LOGICAL_EXCEPTION,e);
		}
		return work;
	}
	
	public MethodologyType newMethodology(String name){
		MethodologyType resource = null;
		try{
			DirectoryGroupType dir = ((GroupFactory)Factories.getFactory(FactoryEnumType.GROUP)).getCreateDirectory(testUser, "Rocket", testUser.getHomeDirectory(), testUser.getOrganizationId());
			resource = ((MethodologyFactory)Factories.getFactory(FactoryEnumType.METHODOLOGY)).newMethodology(testUser, dir.getId());

			resource.setName(name);
			((MethodologyFactory)Factories.getFactory(FactoryEnumType.METHODOLOGY)).add(resource);
			resource = ((MethodologyFactory)Factories.getFactory(FactoryEnumType.METHODOLOGY)).getByNameInGroup(name,  dir);
		}
		catch(FactoryException fe){
			logger.error(fe.getMessage());
			logger.error(FactoryException.LOGICAL_EXCEPTION,fe);
		} catch (ArgumentException e) {
			
			logger.error(FactoryException.LOGICAL_EXCEPTION,e);
		}
		return resource;
	}
	public boolean updateMethodology(MethodologyType work){
		boolean updated = false;
		try {
			updated = ((MethodologyFactory)Factories.getFactory(FactoryEnumType.METHODOLOGY)).update(work);
		} catch (FactoryException e) {
			
			logger.error(FactoryException.LOGICAL_EXCEPTION,e);
		}
		return updated;
	}
	public MethodologyType getMethodology(long id){
		MethodologyType work = null;
		try {
			work = ((MethodologyFactory)Factories.getFactory(FactoryEnumType.METHODOLOGY)).getById(id, testUser.getOrganizationId());
		} catch (FactoryException e) {
			
			logger.error(FactoryException.LOGICAL_EXCEPTION,e);
		} catch (ArgumentException e) {
			
			logger.error(FactoryException.LOGICAL_EXCEPTION,e);
		}
		return work;
	}
	public ResourceType newResource(String name, long userId){
		ResourceType resource = null;
		try{
			DirectoryGroupType dir = ((GroupFactory)Factories.getFactory(FactoryEnumType.GROUP)).getCreateDirectory(testUser, "Rocket", testUser.getHomeDirectory(), testUser.getOrganizationId());
			resource = ((ResourceFactory)Factories.getFactory(FactoryEnumType.RESOURCE)).newResource(testUser, dir.getId());
			resource.setResourceDataId(userId);
			resource.setResourceType(ResourceEnumType.USER);
			resource.setName(name);
			((ResourceFactory)Factories.getFactory(FactoryEnumType.RESOURCE)).add(resource);
			resource = ((ResourceFactory)Factories.getFactory(FactoryEnumType.RESOURCE)).getByNameInGroup(name,  dir);
		}
		catch(FactoryException fe){
			logger.error(fe.getMessage());
			logger.error(FactoryException.LOGICAL_EXCEPTION,fe);
		} catch (ArgumentException e) {
			
			logger.error(FactoryException.LOGICAL_EXCEPTION,e);
		}
		return resource;
	}
	public boolean updateWork(WorkType work){
		boolean updated = false;
		try {
			updated = ((WorkFactory)Factories.getFactory(FactoryEnumType.WORK)).update(work);
		} catch (FactoryException e) {
			
			logger.error(FactoryException.LOGICAL_EXCEPTION,e);
		}
		return updated;
	}
	public WorkType getWork(long id){
		WorkType work = null;
		try {
			work = ((WorkFactory)Factories.getFactory(FactoryEnumType.WORK)).getById(id, testUser.getOrganizationId());
		} catch (FactoryException e) {
			
			logger.error(FactoryException.LOGICAL_EXCEPTION,e);
		} catch (ArgumentException e) {
			
			logger.error(FactoryException.LOGICAL_EXCEPTION,e);
		}
		return work;
	}
	public WorkType newWork(String name){
		WorkType Work = null;
		try{
			DirectoryGroupType dir = ((GroupFactory)Factories.getFactory(FactoryEnumType.GROUP)).getCreateDirectory(testUser, "Rocket", testUser.getHomeDirectory(), testUser.getOrganizationId());
			Work = ((WorkFactory)Factories.getFactory(FactoryEnumType.WORK)).newWork(testUser, dir.getId());
			Work.setName(name);
			
			((WorkFactory)Factories.getFactory(FactoryEnumType.WORK)).add(Work);
			Work = ((WorkFactory)Factories.getFactory(FactoryEnumType.WORK)).getByNameInGroup(name,  dir);
		}
		catch(FactoryException fe){
			logger.error(fe.getMessage());
			logger.error(FactoryException.LOGICAL_EXCEPTION,fe);
		} catch (ArgumentException e) {
			
			logger.error(FactoryException.LOGICAL_EXCEPTION,e);
		}
		return Work;
	}
	public NoteType newNote(String name, String text){
		NoteType Note = null;
		try{
			DirectoryGroupType dir = ((GroupFactory)Factories.getFactory(FactoryEnumType.GROUP)).getCreateDirectory(testUser, "Rocket", testUser.getHomeDirectory(), testUser.getOrganizationId());
			Note = ((NoteFactory)Factories.getFactory(FactoryEnumType.NOTE)).newNote(testUser, dir.getId());
			Note.setName(name);
			Note.setText(text);
			((NoteFactory)Factories.getFactory(FactoryEnumType.NOTE)).add(Note);
			Note = ((NoteFactory)Factories.getFactory(FactoryEnumType.NOTE)).getByNameInGroup(name,  dir);
		}
		catch(FactoryException fe){
			logger.error(fe.getMessage());
			logger.error(FactoryException.LOGICAL_EXCEPTION,fe);
		} catch (ArgumentException e) {
			
			logger.error(FactoryException.LOGICAL_EXCEPTION,e);
		}
		return Note;
	}
	public ArtifactType newDependency(String name){
		ArtifactType depends = newArtifact(name);
		depends.setArtifactType(ArtifactEnumType.DEPENDENCY);
		try {
			((ArtifactFactory)Factories.getFactory(FactoryEnumType.ARTIFACT)).update(depends);
		} catch (FactoryException e) {
			
			logger.error(FactoryException.LOGICAL_EXCEPTION,e);
		}
		return depends;
	}
	public ArtifactType newArtifact(String name){
		ArtifactType Artifact = null;
		try{
			DirectoryGroupType dir = ((GroupFactory)Factories.getFactory(FactoryEnumType.GROUP)).getCreateDirectory(testUser, "Rocket", testUser.getHomeDirectory(), testUser.getOrganizationId());
			Artifact = ((ArtifactFactory)Factories.getFactory(FactoryEnumType.ARTIFACT)).newArtifact(testUser, dir.getId());
			Artifact.setName(name);
			Artifact.setArtifactType(ArtifactEnumType.DATA);
			((ArtifactFactory)Factories.getFactory(FactoryEnumType.ARTIFACT)).add(Artifact);
			Artifact = ((ArtifactFactory)Factories.getFactory(FactoryEnumType.ARTIFACT)).getByNameInGroup(name,  dir);
		}
		catch(FactoryException fe){
			logger.error(fe.getMessage());
			logger.error(FactoryException.LOGICAL_EXCEPTION,fe);
		} catch (ArgumentException e) {
			
			logger.error(FactoryException.LOGICAL_EXCEPTION,e);
		}
		return Artifact;
	}
	public RequirementType newRequirement(String name){
		RequirementType Requirement = null;
		try{
			DirectoryGroupType dir = ((GroupFactory)Factories.getFactory(FactoryEnumType.GROUP)).getCreateDirectory(testUser, "Rocket", testUser.getHomeDirectory(), testUser.getOrganizationId());
			Requirement = ((RequirementFactory)Factories.getFactory(FactoryEnumType.REQUIREMENT)).newRequirement(testUser, dir.getId());
			Requirement.setName(name);
			((RequirementFactory)Factories.getFactory(FactoryEnumType.REQUIREMENT)).add(Requirement);
			Requirement = ((RequirementFactory)Factories.getFactory(FactoryEnumType.REQUIREMENT)).getByNameInGroup(name,  dir);
		}
		catch(FactoryException fe){
			logger.error(fe.getMessage());
			logger.error(FactoryException.LOGICAL_EXCEPTION,fe);
		} catch (ArgumentException e) {
			
			logger.error(FactoryException.LOGICAL_EXCEPTION,e);
		}
		return Requirement;
	}
	public CostType newCost(String name, double hours){
		CostType Cost = null;
		try{
			DirectoryGroupType dir = ((GroupFactory)Factories.getFactory(FactoryEnumType.GROUP)).getCreateDirectory(testUser, "Rocket", testUser.getHomeDirectory(), testUser.getOrganizationId());
			Cost = ((CostFactory)Factories.getFactory(FactoryEnumType.COST)).newCost(testUser, dir.getId());
			Cost.setCurrencyType(CurrencyEnumType.USD);
			Cost.setValue(hours);
			Cost.setName(name);
			((CostFactory)Factories.getFactory(FactoryEnumType.COST)).add(Cost);
			Cost = ((CostFactory)Factories.getFactory(FactoryEnumType.COST)).getByNameInGroup(name, dir);
		}
		catch(FactoryException fe){
			logger.error(fe.getMessage());
			logger.error(FactoryException.LOGICAL_EXCEPTION,fe);
		} catch (ArgumentException e) {
			
			logger.error(FactoryException.LOGICAL_EXCEPTION,e);
		}
		return Cost;
	}
	public TimeType newTime(String name, double hours){
		TimeType time = null;
		try{
			DirectoryGroupType dir = ((GroupFactory)Factories.getFactory(FactoryEnumType.GROUP)).getCreateDirectory(testUser, "Rocket", testUser.getHomeDirectory(), testUser.getOrganizationId());
			time = ((TimeFactory)Factories.getFactory(FactoryEnumType.TIME)).newTime(testUser, dir.getId());
			time.setBasisType(TimeEnumType.HOUR);
			time.setValue(hours);
			time.setName(name);
			((TimeFactory)Factories.getFactory(FactoryEnumType.TIME)).add(time);
			time = ((TimeFactory)Factories.getFactory(FactoryEnumType.TIME)).getByNameInGroup(name, dir);
		}
		catch(FactoryException fe){
			logger.error(fe.getMessage());
			logger.error(FactoryException.LOGICAL_EXCEPTION,fe);
		} catch (ArgumentException e) {
			
			logger.error(FactoryException.LOGICAL_EXCEPTION,e);
		}
		return time;
	}
	public EstimateType newEstimate(String name, double hours, double cost){
		EstimateType est = null;
		try{
			DirectoryGroupType dir = ((GroupFactory)Factories.getFactory(FactoryEnumType.GROUP)).getCreateDirectory(testUser, "Rocket", testUser.getHomeDirectory(), testUser.getOrganizationId());
			
			est = ((EstimateFactory)Factories.getFactory(FactoryEnumType.ESTIMATE)).newEstimate(testUser,  dir.getId());
			est.setEstimateType(EstimateEnumType.SWAG);
			est.setName(name);
			est.setTime(newTime(name, hours));
			est.setCost(newCost(name,cost));
			((EstimateFactory)Factories.getFactory(FactoryEnumType.ESTIMATE)).add(est);
			est = ((EstimateFactory)Factories.getFactory(FactoryEnumType.ESTIMATE)).getByNameInGroup(name, dir);

		}
		catch(FactoryException fe){
			logger.error(fe.getMessage());
			logger.error(FactoryException.LOGICAL_EXCEPTION,fe);
		} catch (ArgumentException e) {
			
			logger.error(FactoryException.LOGICAL_EXCEPTION,e);
		}
		return est;
	}
	public TaskType newTask(String name){
		TaskType obj = null;
		try{
			DirectoryGroupType dir = ((GroupFactory)Factories.getFactory(FactoryEnumType.GROUP)).getCreateDirectory(testUser, "Rocket", testUser.getHomeDirectory(), testUser.getOrganizationId());
			obj = ((TaskFactory)Factories.getFactory(FactoryEnumType.TASK)).newTask(testUser, dir.getId());
			obj.setName(name);
			obj.setDescription("Example Task");
			((TaskFactory)Factories.getFactory(FactoryEnumType.TASK)).add(obj);
			obj = ((TaskFactory)Factories.getFactory(FactoryEnumType.TASK)).getByNameInGroup(name, dir);
		}
		catch(FactoryException fe){
			logger.error(fe.getMessage());
			logger.error(FactoryException.LOGICAL_EXCEPTION,fe);
			obj = null;
		} catch (ArgumentException e) {
			
			logger.error(e.getMessage());
			logger.error(FactoryException.LOGICAL_EXCEPTION,e);
			obj = null;
		}
		return obj;
	}
	
	public ModelType newModel(String name){
		ModelType obj = null;
		try{
			DirectoryGroupType dir = ((GroupFactory)Factories.getFactory(FactoryEnumType.GROUP)).getCreateDirectory(testUser, "Rocket", testUser.getHomeDirectory(), testUser.getOrganizationId());
			obj = ((ModelFactory)Factories.getFactory(FactoryEnumType.MODEL)).newModel(testUser, dir.getId());
			obj.setName(name);
			obj.setDescription("Example Model");
			((ModelFactory)Factories.getFactory(FactoryEnumType.MODEL)).add(obj);
			obj = ((ModelFactory)Factories.getFactory(FactoryEnumType.MODEL)).getByNameInGroup(name, dir);
		}
		catch(FactoryException fe){
			logger.error(fe.getMessage());
			obj = null;
		} catch (ArgumentException e) {
			
			logger.error(e.getMessage());
			obj = null;
		}
		return null;
	}
	
	public ProjectType getTestProject(UserType user, LifecycleType lifecycle, String name){
		ProjectType proj = null;
		try {
			proj = Rocket.getProject(name, lifecycle, user.getOrganizationId());
			if(proj == null){
				proj = Rocket.createProject(user, lifecycle, name);
			}
		} catch (FactoryException | ArgumentException | DataAccessException e) {
			
			logger.error(FactoryException.LOGICAL_EXCEPTION,e);
		}
		return proj;
	}
	public LifecycleType getTestLifecycle(UserType user, String name){
		LifecycleType lc = null;
		try {
			lc = Rocket.getLifecycle(name, user.getOrganizationId());
			if(lc == null){
				lc = Rocket.createLifecycle(user, name);
			}
		} catch (FactoryException | ArgumentException | DataAccessException e) {
			
			logger.error(FactoryException.LOGICAL_EXCEPTION,e);
		}

		return lc;
	}
	public UserType addBulkUser(String sessionId, String userName, String password){
		assertTrue("Account Manager Service is not setup correctly",ServiceUtil.isFactorySetup());
		
		UserType user = null;
		try {

			user = ((UserFactory)Factories.getNameIdFactory(FactoryEnumType.USER)).newUser(userName, UserEnumType.NORMAL, UserStatusEnumType.NORMAL,testOrganization.getId());
			BulkFactories.getBulkFactory().createBulkEntry(sessionId, FactoryEnumType.USER, user);
			CredentialService.newCredential(CredentialEnumType.HASHED_PASSWORD, sessionId, user, user, password.getBytes("UTF-8"), true,true);
		} catch (ArgumentException | UnsupportedEncodingException | FactoryException e) {
			
			logger.error(FactoryException.LOGICAL_EXCEPTION,e);
		}
		return user;
	}
	public UserType getUser(String userName, String password){
		return getUser(userName, password, testOrganization.getId());
	}
	public UserType getUser(String userName, String password,long organizationId){
		assertTrue("Account Manager Service is not setup correctly",ServiceUtil.isFactorySetup());
		
		UserType user = null;
		try {
			user = Factories.getNameIdFactory(FactoryEnumType.USER).getByName(userName,organizationId);
			if(user == null){
				user = ((UserFactory)Factories.getNameIdFactory(FactoryEnumType.USER)).newUser(userName, UserEnumType.NORMAL, UserStatusEnumType.NORMAL,organizationId);
				Factories.getNameIdFactory(FactoryEnumType.USER).add(user);
				user = Factories.getNameIdFactory(FactoryEnumType.USER).getByName(userName,organizationId);
				CredentialService.newHashedPasswordCredential(user, user, password, true);

			}
			Factories.getNameIdFactory(FactoryEnumType.USER).populate(user);
		} catch (FactoryException | ArgumentException e) {
			
			logger.error(FactoryException.LOGICAL_EXCEPTION,e);
		}
		return user;
	}
	
	protected OrganizationType getCreateOrganization(String newOrgName, String adminPassword){
		OrganizationType newOrg = null;

		try {
			newOrg = ((OrganizationFactory)Factories.getFactory(FactoryEnumType.ORGANIZATION)).getOrganizationByName(newOrgName, testOrganization);
			if(newOrg == null){
				newOrg = ((OrganizationFactory)Factories.getFactory(FactoryEnumType.ORGANIZATION)).addOrganization(newOrgName, OrganizationEnumType.DEVELOPMENT, testOrganization);
				FactoryDefaults.setupOrganization(newOrg, SecurityUtil.getSaltedDigest(adminPassword));
			}
		} catch (FactoryException | ArgumentException | DataAccessException e) {
			
			logger.error(FactoryException.LOGICAL_EXCEPTION,e);
		}

		return newOrg;
	}
	
	protected ICommunityProvider getProvider(){
		if(provider != null) return provider;
		String pcls =testProperties.getProperty("factories.community");
		try {
			logger.info("Initializing community provider " + pcls);
			Class cls = Class.forName(pcls);
			ICommunityProvider f = (ICommunityProvider)cls.newInstance();
			provider = f;
			provider.setRandomizeSeedPopulation(false);
			provider.setOrganizePersonManagement(true);
		} catch (ClassNotFoundException | InstantiationException | IllegalAccessException e) {
			// TODO Auto-generated catch block
			logger.error(FactoryException.TRACE_EXCEPTION, e);
		}
		
		return provider;
	}
	
	protected ProjectType getProviderCommunityProject(UserType user, LifecycleType community, String projectName,boolean cleanup){
		ProjectType proj = null;
		UserType admin = getAdminUser(user.getOrganizationId());
		proj = getProvider().getCommunityProject(admin, community.getName(), projectName);
		//logger.info(JSONUtil.exportObject(user));
		if(proj != null && cleanup){
			getProvider().deleteCommunityProject(admin, proj.getObjectId());
			proj = null;
		}
		if(proj == null){
			assertTrue("Failed to create community",getProvider().createCommunityProject(admin, community.getObjectId(), projectName));
			proj = getProvider().getCommunityProject(admin, community.getName(), projectName);
			assertNotNull("Community project is null",proj);
		}
		return proj;
	}
	
	protected LifecycleType getProviderCommunity(UserType user, String communityName, boolean cleanup){
		LifecycleType lf = null;
		UserType admin = getAdminUser(user.getOrganizationId());
		lf = getProvider().getCommunity(admin, communityName);
		//logger.info(JSONUtil.exportObject(user));
		if(lf != null && cleanup){
			getProvider().deleteCommunity(admin, lf.getObjectId());
			lf = null;
		}
		if(lf == null){
			assertTrue("Failed to create community",getProvider().createCommunity(admin, communityName));
			lf = getProvider().getCommunity(admin, communityName);
			assertNotNull("Community is null",lf);
			assertTrue("Failed to enroll admin",getProvider().enrollAdminInCommunity(admin,lf.getObjectId(), user.getObjectId()));
		}
		return lf;
	}
	
	protected boolean reloadTraits(UserType user, LifecycleType lf){
		boolean outBool = false;
		UserType admin = getAdminUser(user.getOrganizationId());
		DirectoryGroupType tdir = BaseService.findGroup(admin, GroupEnumType.DATA, lf.getGroupPath() + "/Traits");
		assertNotNull("Directory is null", tdir);
		int ct = BaseService.countByGroup(AuditEnumType.TRAIT, tdir, user);
		if(ct == 0){
			outBool = getProvider().importLocationTraits(admin, AuditEnumType.LIFECYCLE,lf.getObjectId(),testProperties.getProperty("data.generator.location"), "featureCodes_en.txt");
		}
		else{
			outBool = true;
		}
		return outBool;
	}
	protected boolean loadProjectRegion(UserType user, LifecycleType lf, ProjectType proj, int locationCount, int populationSize){

		boolean outBool = false;
		UserType admin = getAdminUser(user.getOrganizationId());
		DirectoryGroupType tdir = BaseService.findGroup(admin, GroupEnumType.DATA, proj.getGroupPath() + "/Persons");
		assertNotNull("Directory is null", tdir);
		int ct = BaseService.countByGroup(AuditEnumType.PERSON, tdir, user);
		if(ct == 0){
			logger.info("Loading project region and person seed data.");
			outBool = (
					getProvider().generateCommunityProjectRegion(admin, lf.getObjectId(), proj.getObjectId(), locationCount, populationSize, testProperties.getProperty("data.generator.dictionary"),testProperties.getProperty("data.generator.names"))
				);
		}
		else{
			outBool = true;
		}
		return outBool;
	}
	protected boolean evolveProjectRegion(UserType user, LifecycleType lf, ProjectType proj, int epochSize, int epochEvolutions){
		
		boolean outBool = false;
		try {
			int count = ((EventFactory)Factories.getFactory(FactoryEnumType.EVENT)).countInGroup(RocketSecurity.getProjectDirectory(user, proj, "Events"));
			logger.info("Evolving project region and person seed data to " + (count + epochSize));
			outBool = getProvider().evolveCommunityProjectRegion(user, lf.getObjectId(), proj.getObjectId(), epochSize + count, epochEvolutions, testProperties.getProperty("data.generator.dictionary"),testProperties.getProperty("data.generator.names"));
		} catch (FactoryException e) {
			logger.error(e);
		}

		return outBool;
	}
	protected boolean reloadCountryInfo(UserType user, LifecycleType lf){

		boolean outBool = false;
		UserType admin = getAdminUser(user.getOrganizationId());
		DirectoryGroupType tdir = BaseService.findGroup(admin, GroupEnumType.DATA, lf.getGroupPath() + "/Locations");
		assertNotNull("Directory is null", tdir);
		int ct = BaseService.countByGroup(AuditEnumType.LOCATION, tdir, user);
		if(ct == 0){
			logger.info("Loading community location data.  Note: This may take a while.");
			outBool = (
				getProvider().importLocationCountryInfo(admin, AuditEnumType.LIFECYCLE,lf.getObjectId(),testProperties.getProperty("data.generator.location"), "countryInfo.txt")
				&& getProvider().importLocationAdmin1Codes(admin, AuditEnumType.LIFECYCLE,lf.getObjectId(),testProperties.getProperty("data.generator.location"), "admin1CodesASCII.txt")
				&& getProvider().importLocationAdmin2Codes(admin, AuditEnumType.LIFECYCLE,lf.getObjectId(),testProperties.getProperty("data.generator.location"),  "admin2Codes.txt")
				&& getProvider().importLocationCountryData(admin, AuditEnumType.LIFECYCLE,lf.getObjectId(),testProperties.getProperty("data.generator.location"), testProperties.getProperty("country.list"),"alternateNames.txt")
			);
		}
		else{
			outBool = true;
		}
		return outBool;
	}
	
}