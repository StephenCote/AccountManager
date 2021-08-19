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
package org.cote.rocket.query;

import java.nio.charset.StandardCharsets;

import javax.xml.datatype.XMLGregorianCalendar;

import org.cote.accountmanager.data.query.QueryField;
import org.cote.propellant.objects.CostType;
import org.cote.propellant.objects.TimeType;
import org.cote.propellant.objects.types.AlignmentEnumType;
import org.cote.propellant.objects.types.ArtifactEnumType;
import org.cote.propellant.objects.types.BudgetEnumType;
import org.cote.propellant.objects.types.CaseEnumType;
import org.cote.propellant.objects.types.CurrencyEnumType;
import org.cote.propellant.objects.types.ElementEnumType;
import org.cote.propellant.objects.types.EstimateEnumType;
import org.cote.propellant.objects.types.EventEnumType;
import org.cote.propellant.objects.types.GeographyEnumType;
import org.cote.propellant.objects.types.GoalEnumType;
import org.cote.propellant.objects.types.ModelEnumType;
import org.cote.propellant.objects.types.ModuleEnumType;
import org.cote.propellant.objects.types.PriorityEnumType;
import org.cote.propellant.objects.types.RequirementEnumType;
import org.cote.propellant.objects.types.RequirementStatusEnumType;
import org.cote.propellant.objects.types.ResourceEnumType;
import org.cote.propellant.objects.types.SeverityEnumType;
import org.cote.propellant.objects.types.TaskStatusEnumType;
import org.cote.propellant.objects.types.TicketStatusEnumType;
import org.cote.propellant.objects.types.TimeEnumType;
import org.cote.propellant.objects.types.TraitEnumType;
import org.cote.propellant.objects.types.ValidationEnumType;

public class QueryFields extends org.cote.accountmanager.data.query.QueryFields {
	public static QueryField getFieldGoalType(GoalEnumType type)
	{
		return getStringField("goaltype", type.toString());
	}
	public static QueryField getFieldCost(CostType type)
	{
		return getBigIntField("costid", (type != null ? type.getId() : 0));
	}
	public static QueryField getFieldTime(TimeType type)
	{
		return getBigIntField("timeid", (type != null ? type.getId() : 0));
	}
	public static QueryField getFieldBudgetType(BudgetEnumType type)
	{
		return getStringField("budgettype", type.toString());
	}
	public static QueryField getFieldValue(double value)
	{
		return getDoubleField("value", value);
	}
	public static QueryField getFieldAssignedResourceId(long value)
	{
		return getBigIntField("assignedresourceid", value);
	}
	public static QueryField getFieldPriority(PriorityEnumType value)
	{
		return getStringField("priority", value.toString());
	}
	public static QueryField getFieldSeverity(SeverityEnumType value)
	{
		return getStringField("severity", value.toString());
	}
	public static QueryField getFieldTicketStatus(TicketStatusEnumType value)
	{
		return getStringField("ticketstatus", value.toString());
	}
	public static QueryField getFieldActualTimeId(long value)
	{
		return getBigIntField("actualtimeid", value);
	}
	public static QueryField getFieldActualCostId(long value)
	{
		return getBigIntField("actualcostid", value);
	}
	public static QueryField getFieldDueDate(XMLGregorianCalendar val)
	{
		return getTimestampField("duedate", val);
	}
	public static QueryField getFieldClosedDate(XMLGregorianCalendar val)
	{
		return getTimestampField("closeddate", val);
	}
	public static QueryField getFieldCompletedDate(XMLGregorianCalendar val)
	{
		return getTimestampField("completeddate", val);
	}

	public static QueryField getFieldReopenedDate(XMLGregorianCalendar val)
	{
		return getTimestampField("reopeneddate", val);
	}
	public static QueryField getFieldCurrencyType(CurrencyEnumType type){
		return getStringField("currencytype", type.toString());
	}
	public static QueryField getFieldBasisType(TimeEnumType type){
		return getStringField("basistype", type.toString());
	}
	public static QueryField getFieldArtifactType(ArtifactEnumType type){
		return getStringField("artifacttype", type.toString());
	}
	public static QueryField getFieldPreviousTransitionId(long value)
	{
		return getBigIntField("previoustransitionid", value);
	}
	public static QueryField getFieldNextTransitionId(long value)
	{
		return getBigIntField("nexttransitionid", value);
	}
	public static QueryField getFieldArtifactDataId(long value)
	{
		return getBigIntField("artifactdataid", value);
	}
	public static QueryField getFieldRequirementType(RequirementEnumType type){
		return getStringField("requirementtype", type.toString());
	}
	public static QueryField getFieldRequirementStatusType(RequirementStatusEnumType type){
		return getStringField("requirementstatus", type.toString());
	}
	public static QueryField getFieldModelType(ModelEnumType type){
		return getStringField("modeltype", type.toString());
	}	
	public static QueryField getFieldEstimateType(EstimateEnumType type){
		return getStringField("estimatetype", type.toString());
	}
	public static QueryField getFieldTaskStatus(TaskStatusEnumType type){
		return getStringField("taskstatus", type.toString());
	}
	public static QueryField getFieldResourceType(ResourceEnumType type){
		return getStringField("resourcetype", type.toString());
	}
	public static QueryField getFieldModuleType(ModuleEnumType type){
		return getStringField("moduletype", type.toString());
	}
	public static QueryField getFieldCaseType(CaseEnumType type){
		return getStringField("casetype", type.toString());
	}
	public static QueryField getFieldTraitType(TraitEnumType type){
		return getStringField("traittype", type.toString());
	}
	public static QueryField getFieldAlignmentType(AlignmentEnumType type){
		return getStringField("alignment", type.toString());
	}

	public static QueryField getFieldEventType(EventEnumType type){
		return getStringField("eventtype", type.toString());
	}
	public static QueryField getFieldLocationId(long id){
		return getBigIntField("locationid", id);
	}

	public static QueryField getFieldGeographyType(GeographyEnumType type){
		return getStringField("geographytype", type.toString());
	}

	public static QueryField getFieldClassification(String cls){
		return getStringField("classification", cls);
	}

	
	public static QueryField getFieldEstimateId(long val){
		return getBigIntField("estimateid", val);
	}
	public static QueryField getFieldResourceId(long val){
		return getBigIntField("resourceid", val);
	}
	public static QueryField getFieldScheduleId(long val){
		return getBigIntField("scheduleid", val);
	}
	public static QueryField getFieldWorkId(long val){
		return getBigIntField("workid", val);
	}
	public static QueryField getFieldBudgetId(long val){
		return getBigIntField("budgetid", val);
	}
	public static QueryField getFieldMethodologyId(long val){
		return getBigIntField("methodologyid", val);
	}
	public static QueryField getFieldUtilization(double val){
		return getDoubleField("utilization", val);
	}
	public static QueryField getFieldIterates(boolean val){
		return getBooleanField("iterates",val);
	}
	public static QueryField getFieldIsBinary(boolean val){
		return getBooleanField("isbinary",val);
	}
	public static QueryField getFieldBinaryValueId(long val){
		return getBigIntField("binaryvalueid",val);
	}
	public static QueryField getFieldTextValue(String val){
		return getStringField("textvalue",val);
	}
	public static QueryField getFieldFormElementId(long val){
		return getBigIntField("formelementid",val);
	}
	public static QueryField getFieldElementType(ElementEnumType val){
		return getStringField("elementtype",val.toString());
	}
	public static QueryField getFieldElementName(String val){
		return getStringField("elementname",val);
	}
	public static QueryField getFieldElementLabel(String val){
		return getStringField("elementlabel",val);
	}
	public static QueryField getFieldFormId(long val){
		return getBigIntField("formid",val);
	}	
	public static QueryField getFieldValidationRuleId(long val){
		return getBigIntField("validationruleid",val);
	}
	public static QueryField getFieldAllowNull(boolean val){
		return getBooleanField("allownull",val);
	}
	public static QueryField getFieldTemplateId(long val){
		return getBigIntField("templateid",val);
	}
	public static QueryField getFieldViewTemplateId(long val){
		return getBigIntField("viewtemplateid",val);
	}
	public static QueryField getFieldElementTemplateId(long val){
		return getBigIntField("elementtemplateid",val);
	}

	public static QueryField getFieldIsTemplate(boolean val){
		return getBooleanField("istemplate",val);
	}
	public static QueryField getFieldIsGrid(boolean val){
		return getBooleanField("isgrid",val);
	}
	public static QueryField getFieldValidationType(ValidationEnumType val){
		return getStringField("validationtype",val.toString());
	}
	public static QueryField getFieldIsRuleSet(boolean val){
		return getBooleanField("isruleset",val);
	}
	public static QueryField getFieldComparison(boolean val){
		return getBooleanField("comparison",val);
	}
	public static QueryField getFieldIsReplacementRule(boolean val){
		return getBooleanField("isreplacementrule",val);
	}
	public static QueryField getFieldExpression(String val){
		return getStringField("expression",val);
	}
	public static QueryField getFieldErrorMessage(String val){
		return getStringField("errormessage",val);
	}
	public static QueryField getFieldReplacementValue(String val){
		return getStringField("replacementvalue",val);
	}
	public static QueryField getFieldRequirementId(String val){
		return getStringField("requirementid",val);
	}
	public static QueryField getFieldText(String s){
		return getBytesField("text",s.getBytes(StandardCharsets.UTF_8));
	}
}
