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
package org.cote.beans;

import javax.xml.bind.annotation.XmlRootElement;

import org.cote.accountmanager.objects.AccessRequestType;
import org.cote.accountmanager.objects.AccountType;
import org.cote.accountmanager.objects.AddressType;
import org.cote.accountmanager.objects.ApplicationProfileType;
import org.cote.accountmanager.objects.ApprovalType;
import org.cote.accountmanager.objects.ApproverType;
import org.cote.accountmanager.objects.AttributeType;
import org.cote.accountmanager.objects.AuthenticationRequestType;
import org.cote.accountmanager.objects.AuthenticationResponseType;
import org.cote.accountmanager.objects.AuthorizationPolicyType;
import org.cote.accountmanager.objects.BaseGroupType;
import org.cote.accountmanager.objects.BasePermissionType;
import org.cote.accountmanager.objects.BaseRoleType;
import org.cote.accountmanager.objects.BaseSearchRequestType;
import org.cote.accountmanager.objects.BaseTagType;
import org.cote.accountmanager.objects.ContactInformationType;
import org.cote.accountmanager.objects.ContactType;
import org.cote.accountmanager.objects.ControlType;
import org.cote.accountmanager.objects.CredentialType;
import org.cote.accountmanager.objects.DataTagSearchRequest;
import org.cote.accountmanager.objects.DataType;
import org.cote.accountmanager.objects.DirectoryGroupType;
import org.cote.accountmanager.objects.FactType;
import org.cote.accountmanager.objects.FieldMatch;
import org.cote.accountmanager.objects.FunctionFactType;
import org.cote.accountmanager.objects.FunctionType;
import org.cote.accountmanager.objects.ObjectSearchRequestType;
import org.cote.accountmanager.objects.OperationType;
import org.cote.accountmanager.objects.OrganizationType;
import org.cote.accountmanager.objects.ParticipationSearchRequest;
import org.cote.accountmanager.objects.PatternType;
import org.cote.accountmanager.objects.PersonType;
import org.cote.accountmanager.objects.PolicyDefinitionType;
import org.cote.accountmanager.objects.PolicyRequestType;
import org.cote.accountmanager.objects.PolicyResponseType;
import org.cote.accountmanager.objects.PolicyType;
import org.cote.accountmanager.objects.RuleType;
import org.cote.accountmanager.objects.SortQueryType;
import org.cote.accountmanager.objects.UserGroupType;
import org.cote.accountmanager.objects.UserType;
import org.cote.propellant.objects.ApplicationRequestType;
import org.cote.propellant.objects.ArtifactType;
import org.cote.propellant.objects.BlueprintType;
import org.cote.propellant.objects.BudgetType;
import org.cote.propellant.objects.CaseType;
import org.cote.propellant.objects.CostType;
import org.cote.propellant.objects.EstimateType;
import org.cote.propellant.objects.EventType;
import org.cote.propellant.objects.ExpenseType;
import org.cote.propellant.objects.FormElementType;
import org.cote.propellant.objects.FormElementValueType;
import org.cote.propellant.objects.FormType;
import org.cote.propellant.objects.GoalType;
import org.cote.propellant.objects.IdentityDataImportType;
import org.cote.propellant.objects.LifecycleType;
import org.cote.propellant.objects.LocationType;
import org.cote.propellant.objects.MethodologyType;
import org.cote.propellant.objects.ModelType;
import org.cote.propellant.objects.ModuleType;
import org.cote.propellant.objects.NoteType;
import org.cote.propellant.objects.ProcessStepType;
import org.cote.propellant.objects.ProcessType;
import org.cote.propellant.objects.ProjectType;
import org.cote.propellant.objects.RequirementType;
import org.cote.propellant.objects.ResourceType;
import org.cote.propellant.objects.ScheduleType;
import org.cote.propellant.objects.StageType;
import org.cote.propellant.objects.TaskType;
import org.cote.propellant.objects.TicketType;
import org.cote.propellant.objects.TimeType;
import org.cote.propellant.objects.TraitType;
import org.cote.propellant.objects.ValidationRuleType;
import org.cote.propellant.objects.WorkType;


@XmlRootElement(name="EntitySchema")
public class EntitySchema {
	
	private String defaultPackage = "org.cote.objects";
	private AccessRequestType accessRequestType = null;
	private ApproverType approverType = null;
	private ApprovalType approvalType = null;
	private AuthenticationRequestType authenticationRequest = null;
	private NoteType noteType = null;
	private LifecycleType lifecycleType = null;
	private ArtifactType artifactType = null;
	private BlueprintType blueprintType = null;
	private BudgetType budgetType = null;
	private CaseType caseType = null;
	private CostType costType = null;
	private EstimateType estimateType = null;
	private ExpenseType expenseType = null;
	private FormType formType = null;
	private FormElementType formElementType = null;
	private FormElementValueType formElementValueType = null;
	private GoalType goalType = null;
	private MethodologyType methodologyType = null;
	private ModelType modelType = null;
	private ModuleType moduleType = null;
	private ProcessType processType = null;
	private ProcessStepType processStepType = null;
	private ProjectType projectType = null;
	private RequirementType requirementType = null;
	private ResourceType resourceType = null;
	private ScheduleType scheduleType = null;
	private LocationType locationType = null;
	private EventType eventType = null;
	private TraitType traitType = null;
	private ApplicationProfileType applicationProfileType = null;
	private StageType stageType = null;
	private TaskType taskType = null;
	private TicketType ticketType = null;
	private TimeType timeType = null;
	private ValidationRuleType validationRuleType = null;
	private WorkType workType = null;
	private ApplicationRequestType applicationRequestType = null;
	private IdentityDataImportType identityDataImportType = null;
	
	
	private BaseRoleType baseRoleType = null;
	private DataType dataTypeSchema = null;
	private CryptoBean cryptoBeanSchema = null;
	private UserType userType = null;
	private DirectoryGroupType directoryGroupType = null;
	private BaseGroupType baseGroupType = null;
	private UserGroupType userGroupType = null;
	private ControlType controlType = null;
	private ContactInformationType contactInformationType = null;
	private PersonType personType = null;
	private ContactType contactType = null;
	private AddressType addressType = null;
	private OrganizationType organizationType = null;
	private AttributeType attributeType = null;
	private CredentialType credentialType = null;
	private AuthenticationRequestType authenticationRequestType = null;
	private AuthenticationResponseType authenticationResponseType = null;
	private PolicyDefinitionType policyDefinitionType = null;
	private PolicyRequestType policyRequestType = null;
	private PolicyResponseType policyResponseType = null;
	private FactType factType = null;
	private FunctionType functionType = null;
	private FunctionFactType functionFactType = null;
	private PatternType patternType = null;
	private PolicyType policyType = null;
	private OperationType operationType = null;
	private RuleType ruleType = null;
	private BasePermissionType basePermissionType = null;
	private AuthorizationPolicyType authorizationPolicyType = null;
	private AccountType accountType = null;
	private BaseTagType baseTagType = null;
	private DataTagSearchRequest dataTagSearchRequest = null;
	private ParticipationSearchRequest participationSearchRequest = null;
	private BaseSearchRequestType baseSearchRequestType = null;
	private SortQueryType sortQueryType = null;
	private ObjectSearchRequestType objectSearchRequestType = null;
	private FieldMatch fieldMatch = null;
	
	public EntitySchema(){
		controlType = new ControlType();
		accessRequestType = new AccessRequestType();
		approverType = new ApproverType();
		approvalType = new ApprovalType();
		applicationProfileType = new ApplicationProfileType();
		sortQueryType = new SortQueryType();
		baseSearchRequestType = new BaseSearchRequestType();
		authenticationRequest = new AuthenticationRequestType();
		credentialType = new CredentialType();
		authenticationRequestType = new AuthenticationRequestType();
		authenticationResponseType = new AuthenticationResponseType();
		dataTagSearchRequest = new DataTagSearchRequest();
		participationSearchRequest = new ParticipationSearchRequest();
		baseTagType = new BaseTagType();
		authorizationPolicyType = new AuthorizationPolicyType();
		addressType = new AddressType();
		basePermissionType = new BasePermissionType();
		policyDefinitionType = new PolicyDefinitionType();
		policyRequestType = new PolicyRequestType();
		policyResponseType = new PolicyResponseType();
		factType = new FactType();
		functionType = new FunctionType();
		functionFactType = new FunctionFactType();
		patternType = new PatternType();
		policyType = new PolicyType();
		operationType = new OperationType();
		ruleType = new RuleType();
		attributeType = new AttributeType();
		attributeType.getValues();
		userGroupType = new UserGroupType();
		baseGroupType = new BaseGroupType();
		contactType = new ContactType();
		personType = new PersonType();
		organizationType = new OrganizationType();
		baseRoleType = new BaseRoleType();
		directoryGroupType = new DirectoryGroupType();
		dataTypeSchema = new DataType();
		cryptoBeanSchema = new CryptoBean();
		userType = new UserType();
		contactInformationType = new ContactInformationType();
		accountType = new AccountType();
		
		eventType = new EventType();
		locationType = new LocationType();
		traitType = new TraitType();
		applicationRequestType = new ApplicationRequestType();
		identityDataImportType = new IdentityDataImportType();
		noteType = new NoteType();
		lifecycleType = new LifecycleType();
		artifactType = new ArtifactType();
		blueprintType = new BlueprintType();
		budgetType = new BudgetType();
		caseType = new CaseType();
		costType = new CostType();
		estimateType = new EstimateType();
		expenseType = new ExpenseType();
		formType = new FormType();
		formElementType = new FormElementType();
		formElementValueType = new FormElementValueType();
		goalType = new GoalType();
		methodologyType = new MethodologyType();
		modelType = new ModelType();
		moduleType = new ModuleType();
		processType = new ProcessType();
		processStepType = new ProcessStepType();
		projectType = new ProjectType();
		requirementType = new RequirementType();
		resourceType = new ResourceType();
		scheduleType = new ScheduleType();
		stageType = new StageType();
		taskType = new TaskType();
		ticketType = new TicketType();
		timeType = new TimeType();
		validationRuleType = new ValidationRuleType();
		workType = new WorkType();
		objectSearchRequestType = new ObjectSearchRequestType();
		fieldMatch = new FieldMatch();
	}

	

	public ParticipationSearchRequest getParticipationSearchRequest() {
		return participationSearchRequest;
	}



	public void setParticipationSearchRequest(ParticipationSearchRequest participationSearchRequest) {
		this.participationSearchRequest = participationSearchRequest;
	}



	public FieldMatch getFieldMatch() {
		return fieldMatch;
	}



	public void setFieldMatch(FieldMatch fieldMatch) {
		this.fieldMatch = fieldMatch;
	}



	public ObjectSearchRequestType getObjectSearchRequestType() {
		return objectSearchRequestType;
	}



	public void setObjectSearchRequestType(ObjectSearchRequestType objectSearchRequestType) {
		this.objectSearchRequestType = objectSearchRequestType;
	}



	public ControlType getControlType() {
		return controlType;
	}



	public void setControlType(ControlType controlType) {
		this.controlType = controlType;
	}



	public AccessRequestType getAccessRequestType() {
		return accessRequestType;
	}



	public void setAccessRequestType(AccessRequestType accessRequestType) {
		this.accessRequestType = accessRequestType;
	}



	public ApproverType getApproverType() {
		return approverType;
	}



	public void setApproverType(ApproverType approverType) {
		this.approverType = approverType;
	}



	public ApprovalType getApprovalType() {
		return approvalType;
	}



	public void setApprovalType(ApprovalType approvalType) {
		this.approvalType = approvalType;
	}



	public ApplicationProfileType getApplicationProfileType() {
		return applicationProfileType;
	}



	public void setApplicationProfileType(ApplicationProfileType applicationProfileType) {
		this.applicationProfileType = applicationProfileType;
	}



	public BaseSearchRequestType getBaseSearchRequestType() {
		return baseSearchRequestType;
	}



	public void setBaseSearchRequestType(BaseSearchRequestType baseSearchRequestType) {
		this.baseSearchRequestType = baseSearchRequestType;
	}



	public SortQueryType getSortQueryType() {
		return sortQueryType;
	}



	public void setSortQueryType(SortQueryType sortQueryType) {
		this.sortQueryType = sortQueryType;
	}



	public AuthenticationRequestType getAuthenticationRequest() {
		return authenticationRequest;
	}



	public void setAuthenticationRequest(AuthenticationRequestType authenticationRequest) {
		this.authenticationRequest = authenticationRequest;
	}



	public LocationType getLocationType() {
		return locationType;
	}



	public void setLocationType(LocationType locationType) {
		this.locationType = locationType;
	}



	public CredentialType getCredentialType() {
		return credentialType;
	}



	public void setCredentialType(CredentialType credentialType) {
		this.credentialType = credentialType;
	}



	public AuthenticationRequestType getAuthenticationRequestType() {
		return authenticationRequestType;
	}



	public void setAuthenticationRequestType(
			AuthenticationRequestType authenticationRequestType) {
		this.authenticationRequestType = authenticationRequestType;
	}



	public AuthenticationResponseType getAuthenticationResponseType() {
		return authenticationResponseType;
	}



	public void setAuthenticationResponseType(
			AuthenticationResponseType authenticationResponseType) {
		this.authenticationResponseType = authenticationResponseType;
	}



	public DataTagSearchRequest getDataTagSearchRequest() {
		return dataTagSearchRequest;
	}



	public void setDataTagSearchRequest(DataTagSearchRequest dataTagSearchRequest) {
		this.dataTagSearchRequest = dataTagSearchRequest;
	}



	public BaseTagType getBaseTagType() {
		return baseTagType;
	}



	public void setBaseTagType(BaseTagType tagType) {
		this.baseTagType = tagType;
	}



	public AuthorizationPolicyType getAuthorizationPolicyType() {
		return authorizationPolicyType;
	}



	public void setAuthorizationPolicyType(
			AuthorizationPolicyType authorizationPolicyType) {
		this.authorizationPolicyType = authorizationPolicyType;
	}



	public AddressType getAddressType() {
		return addressType;
	}



	public void setAddressType(AddressType addressType) {
		this.addressType = addressType;
	}



	public BasePermissionType getBasePermissionType() {
		return basePermissionType;
	}


	public void setBasePermissionType(BasePermissionType basePermissionType) {
		this.basePermissionType = basePermissionType;
	}


	public AccountType getAccountType() {
		return accountType;
	}


	public void setAccountType(AccountType accountType) {
		this.accountType = accountType;
	}


	public PolicyDefinitionType getPolicyDefinitionType() {
		return policyDefinitionType;
	}


	public void setPolicyDefinitionType(PolicyDefinitionType policyDefinitionType) {
		this.policyDefinitionType = policyDefinitionType;
	}


	public PolicyRequestType getPolicyRequestType() {
		return policyRequestType;
	}


	public void setPolicyRequestType(PolicyRequestType policyRequestType) {
		this.policyRequestType = policyRequestType;
	}


	public PolicyResponseType getPolicyResponseType() {
		return policyResponseType;
	}


	public void setPolicyResponseType(PolicyResponseType policyResponseType) {
		this.policyResponseType = policyResponseType;
	}


	public FactType getFactType() {
		return factType;
	}


	public void setFactType(FactType factType) {
		this.factType = factType;
	}


	public FunctionType getFunctionType() {
		return functionType;
	}


	public void setFunctionType(FunctionType functionType) {
		this.functionType = functionType;
	}


	public FunctionFactType getFunctionFactType() {
		return functionFactType;
	}


	public void setFunctionFactType(FunctionFactType functionFactType) {
		this.functionFactType = functionFactType;
	}


	public PatternType getPatternType() {
		return patternType;
	}


	public void setPatternType(PatternType patternType) {
		this.patternType = patternType;
	}


	public PolicyType getPolicyType() {
		return policyType;
	}


	public void setPolicyType(PolicyType policyType) {
		this.policyType = policyType;
	}


	public OperationType getOperationType() {
		return operationType;
	}


	public void setOperationType(OperationType operationType) {
		this.operationType = operationType;
	}


	public RuleType getRuleType() {
		return ruleType;
	}


	public void setRuleType(RuleType ruleType) {
		this.ruleType = ruleType;
	}


	public AttributeType getAttributeType() {
		return attributeType;
	}


	public void setAttributeType(AttributeType attributeType) {
		this.attributeType = attributeType;
	}


	public BaseGroupType getBaseGroupType() {
		return baseGroupType;
	}


	public void setBaseGroupType(BaseGroupType baseGroupType) {
		this.baseGroupType = baseGroupType;
	}


	public UserGroupType getUserGroupType() {
		return userGroupType;
	}


	public void setUserGroupType(UserGroupType userGroupType) {
		this.userGroupType = userGroupType;
	}


	public PersonType getPersonType() {
		return personType;
	}


	public void setPersonType(PersonType personType) {
		this.personType = personType;
	}


	public ContactType getContactType() {
		return contactType;
	}


	public void setContactType(ContactType contactType) {
		this.contactType = contactType;
	}


	public OrganizationType getOrganizationType() {
		return organizationType;
	}


	public void setOrganizationType(OrganizationType organizationType) {
		this.organizationType = organizationType;
	}


	public BaseRoleType getBaseRoleType() {
		return baseRoleType;
	}


	public void setBaseRoleType(BaseRoleType baseRoleType) {
		this.baseRoleType = baseRoleType;
	}

	public DirectoryGroupType getDirectoryGroupType() {
		return directoryGroupType;
	}


	public void setDirectoryGroupType(DirectoryGroupType directoryGroupType) {
		this.directoryGroupType = directoryGroupType;
	}

	public ContactInformationType getContactInformationType() {
		return contactInformationType;
	}



	public void setContactInformationType(
			ContactInformationType contactInformationType) {
		this.contactInformationType = contactInformationType;
	}



	public UserType getUserType() {
		return userType;
	}

	public void setUserType(UserType userType) {
		this.userType = userType;
	}

	public CryptoBean getCryptoBeanSchema() {
		return cryptoBeanSchema;
	}

	public void setCryptoBeanSchema(CryptoBean cryptoBeanSchema) {
		this.cryptoBeanSchema = cryptoBeanSchema;
	}


	public DataType getDataTypeSchema() {
		return dataTypeSchema;
	}

	public void setDataTypeSchema(DataType dataTypeSchema) {
		this.dataTypeSchema = dataTypeSchema;
	}
	
	public EventType getEventType() {
		return eventType;
	}
	
	public void setEventType(EventType eventType) {
		this.eventType = eventType;
	}
	
	public TraitType getTraitType() {
		return traitType;
	}
	
	public void setTraitType(TraitType traitType) {
		this.traitType = traitType;
	}
	
	public ApplicationRequestType getApplicationRequestType() {
		return applicationRequestType;
	}
	
	public void setApplicationRequestType(
			ApplicationRequestType applicationRequestType) {
		this.applicationRequestType = applicationRequestType;
	}
	
	public IdentityDataImportType getIdentityDataImportType() {
		return identityDataImportType;
	}
	
	public void setIdentityDataImportType(
			IdentityDataImportType identityDataImportType) {
		this.identityDataImportType = identityDataImportType;
	}
	
	public String getDefaultPackage() {
		return defaultPackage;
	}
	
	public void setDefaultPackage(String defaultPackage) {
		this.defaultPackage = defaultPackage;
	}
	
	
	public NoteType getNoteType() {
		return noteType;
	}
	public void setNoteType(NoteType noteType) {
		this.noteType = noteType;
	}
	public LifecycleType getLifecycleType() {
		return lifecycleType;
	}
	public void setLifecycleType(LifecycleType lifecycleType) {
		this.lifecycleType = lifecycleType;
	}
	public ArtifactType getArtifactType() {
		return artifactType;
	}
	public void setArtifactType(ArtifactType artifactType) {
		this.artifactType = artifactType;
	}
	public BlueprintType getBlueprintType() {
		return blueprintType;
	}
	public void setBlueprintType(BlueprintType blueprintType) {
		this.blueprintType = blueprintType;
	}
	public BudgetType getBudgetType() {
		return budgetType;
	}
	public void setBudgetType(BudgetType budgetType) {
		this.budgetType = budgetType;
	}
	public CaseType getCaseType() {
		return caseType;
	}
	public void setCaseType(CaseType caseType) {
		this.caseType = caseType;
	}
	public CostType getCostType() {
		return costType;
	}
	public void setCostType(CostType costType) {
		this.costType = costType;
	}
	public EstimateType getEstimateType() {
		return estimateType;
	}
	public void setEstimateType(EstimateType estimateType) {
		this.estimateType = estimateType;
	}
	public ExpenseType getExpenseType() {
		return expenseType;
	}
	public void setExpenseType(ExpenseType expenseType) {
		this.expenseType = expenseType;
	}
	public FormType getFormType() {
		return formType;
	}
	public void setFormType(FormType formType) {
		this.formType = formType;
	}
	public FormElementType getFormElementType() {
		return formElementType;
	}
	public void setFormElementType(FormElementType formElementType) {
		this.formElementType = formElementType;
	}
	public FormElementValueType getFormElementValueType() {
		return formElementValueType;
	}
	public void setFormElementValueType(FormElementValueType formElementValueType) {
		this.formElementValueType = formElementValueType;
	}
	public GoalType getGoalType() {
		return goalType;
	}
	public void setGoalType(GoalType goalType) {
		this.goalType = goalType;
	}
	public MethodologyType getMethodologyType() {
		return methodologyType;
	}
	public void setMethodologyType(MethodologyType methodologyType) {
		this.methodologyType = methodologyType;
	}
	public ModelType getModelType() {
		return modelType;
	}
	public void setModelType(ModelType modelType) {
		this.modelType = modelType;
	}
	public ModuleType getModuleType() {
		return moduleType;
	}
	public void setModuleType(ModuleType moduleType) {
		this.moduleType = moduleType;
	}
	public ProcessType getProcessType() {
		return processType;
	}
	public void setProcessType(ProcessType processType) {
		this.processType = processType;
	}
	public ProcessStepType getProcessStepType() {
		return processStepType;
	}
	public void setProcessStepType(ProcessStepType processStepType) {
		this.processStepType = processStepType;
	}
	public ProjectType getProjectType() {
		return projectType;
	}
	public void setProjectType(ProjectType projectType) {
		this.projectType = projectType;
	}
	public RequirementType getRequirementType() {
		return requirementType;
	}
	public void setRequirementType(RequirementType requirementType) {
		this.requirementType = requirementType;
	}
	public ResourceType getResourceType() {
		return resourceType;
	}
	public void setResourceType(ResourceType resourceType) {
		this.resourceType = resourceType;
	}
	public ScheduleType getScheduleType() {
		return scheduleType;
	}
	public void setScheduleType(ScheduleType scheduleType) {
		this.scheduleType = scheduleType;
	}
	public StageType getStageType() {
		return stageType;
	}
	public void setStageType(StageType stageType) {
		this.stageType = stageType;
	}
	public TaskType getTaskType() {
		return taskType;
	}
	public void setTaskType(TaskType taskType) {
		this.taskType = taskType;
	}
	public TicketType getTicketType() {
		return ticketType;
	}
	public void setTicketType(TicketType ticketType) {
		this.ticketType = ticketType;
	}
	public TimeType getTimeType() {
		return timeType;
	}
	public void setTimeType(TimeType timeType) {
		this.timeType = timeType;
	}
	public ValidationRuleType getValidationRuleType() {
		return validationRuleType;
	}
	public void setValidationRuleType(ValidationRuleType validationRuleType) {
		this.validationRuleType = validationRuleType;
	}
	public WorkType getWorkType() {
		return workType;
	}
	public void setWorkType(WorkType workType) {
		this.workType = workType;
	}


}
