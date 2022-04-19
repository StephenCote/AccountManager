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
import org.cote.accountmanager.factory.FieldMap;
import org.cote.accountmanager.objects.types.ColumnEnumType;
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
		return getStringField(FieldMap.Columns.get(ColumnEnumType.GOALTYPE), type.toString());
	}
	public static QueryField getFieldCost(CostType type)
	{
		return getBigIntField(FieldMap.Columns.get(ColumnEnumType.COSTID), (type != null ? type.getId() : 0));
	}
	public static QueryField getFieldTime(TimeType type)
	{
		return getBigIntField(FieldMap.Columns.get(ColumnEnumType.TIMEID), (type != null ? type.getId() : 0));
	}
	public static QueryField getFieldBudgetType(BudgetEnumType type)
	{
		return getStringField(FieldMap.Columns.get(ColumnEnumType.BUDGETTYPE), type.toString());
	}
	public static QueryField getFieldValue(double value)
	{
		return getDoubleField(FieldMap.Columns.get(ColumnEnumType.VALUE), value);
	}
	public static QueryField getFieldAssignedResourceId(long value)
	{
		return getBigIntField(FieldMap.Columns.get(ColumnEnumType.ASSIGNEDRESOURCEID), value);
	}
	public static QueryField getFieldPriority(PriorityEnumType value)
	{
		return getStringField(FieldMap.Columns.get(ColumnEnumType.PRIORITY), value.toString());
	}
	public static QueryField getFieldSeverity(SeverityEnumType value)
	{
		return getStringField(FieldMap.Columns.get(ColumnEnumType.SEVERITY), value.toString());
	}
	public static QueryField getFieldTicketStatus(TicketStatusEnumType value)
	{
		return getStringField(FieldMap.Columns.get(ColumnEnumType.TICKETSTATUS), value.toString());
	}
	public static QueryField getFieldActualTimeId(long value)
	{
		return getBigIntField(FieldMap.Columns.get(ColumnEnumType.ACTUALTIMEID), value);
	}
	public static QueryField getFieldActualCostId(long value)
	{
		return getBigIntField(FieldMap.Columns.get(ColumnEnumType.ACTUALCOSTID), value);
	}
	public static QueryField getFieldDueDate(XMLGregorianCalendar val)
	{
		return getTimestampField(FieldMap.Columns.get(ColumnEnumType.DUEDATE), val);
	}
	public static QueryField getFieldClosedDate(XMLGregorianCalendar val)
	{
		return getTimestampField(FieldMap.Columns.get(ColumnEnumType.CLOSEDDATE), val);
	}
	public static QueryField getFieldCompletedDate(XMLGregorianCalendar val)
	{
		return getTimestampField(FieldMap.Columns.get(ColumnEnumType.COMPLETEDDATE), val);
	}

	public static QueryField getFieldReopenedDate(XMLGregorianCalendar val)
	{
		return getTimestampField(FieldMap.Columns.get(ColumnEnumType.REOPENEDDATE), val);
	}
	public static QueryField getFieldCurrencyType(CurrencyEnumType type){
		return getStringField(FieldMap.Columns.get(ColumnEnumType.CURRENCYTYPE), type.toString());
	}
	public static QueryField getFieldBasisType(TimeEnumType type){
		return getStringField(FieldMap.Columns.get(ColumnEnumType.BASISTYPE), type.toString());
	}
	public static QueryField getFieldArtifactType(ArtifactEnumType type){
		return getStringField(FieldMap.Columns.get(ColumnEnumType.ARTIFACTTYPE), type.toString());
	}
	public static QueryField getFieldPreviousTransitionId(long value)
	{
		return getBigIntField(FieldMap.Columns.get(ColumnEnumType.PREVIOUSTRANSITIONID), value);
	}
	public static QueryField getFieldNextTransitionId(long value)
	{
		return getBigIntField(FieldMap.Columns.get(ColumnEnumType.NEXTTRANSITIONID), value);
	}
	public static QueryField getFieldArtifactDataId(long value)
	{
		return getBigIntField(FieldMap.Columns.get(ColumnEnumType.ARTIFACTDATAID), value);
	}
	public static QueryField getFieldRequirementType(RequirementEnumType type){
		return getStringField(FieldMap.Columns.get(ColumnEnumType.REQUIREMENTTYPE), type.toString());
	}
	public static QueryField getFieldRequirementStatusType(RequirementStatusEnumType type){
		return getStringField(FieldMap.Columns.get(ColumnEnumType.REQUIREMENTSTATUS), type.toString());
	}
	public static QueryField getFieldModelType(ModelEnumType type){
		return getStringField(FieldMap.Columns.get(ColumnEnumType.MODELTYPE), type.toString());
	}	
	public static QueryField getFieldEstimateType(EstimateEnumType type){
		return getStringField(FieldMap.Columns.get(ColumnEnumType.ESTIMATETYPE), type.toString());
	}
	public static QueryField getFieldTaskStatus(TaskStatusEnumType type){
		return getStringField(FieldMap.Columns.get(ColumnEnumType.TASKSTATUS), type.toString());
	}
	public static QueryField getFieldResourceType(ResourceEnumType type){
		return getStringField(FieldMap.Columns.get(ColumnEnumType.RESOURCETYPE), type.toString());
	}
	public static QueryField getFieldModuleType(ModuleEnumType type){
		return getStringField(FieldMap.Columns.get(ColumnEnumType.MODULETYPE), type.toString());
	}
	public static QueryField getFieldCaseType(CaseEnumType type){
		return getStringField(FieldMap.Columns.get(ColumnEnumType.CASETYPE), type.toString());
	}
	public static QueryField getFieldTraitType(TraitEnumType type){
		return getStringField(FieldMap.Columns.get(ColumnEnumType.TRAITTYPE), type.toString());
	}
	public static QueryField getFieldAlignmentType(AlignmentEnumType type){
		return getStringField(FieldMap.Columns.get(ColumnEnumType.ALIGNMENT), type.toString());
	}

	public static QueryField getFieldEventType(EventEnumType type){
		return getStringField(FieldMap.Columns.get(ColumnEnumType.EVENTTYPE), type.toString());
	}
	public static QueryField getFieldLocationId(long id){
		return getBigIntField(FieldMap.Columns.get(ColumnEnumType.LOCATIONID), id);
	}

	public static QueryField getFieldGeographyType(GeographyEnumType type){
		return getStringField(FieldMap.Columns.get(ColumnEnumType.GEOGRAPHYTYPE), type.toString());
	}

	public static QueryField getFieldClassification(String cls){
		return getStringField(FieldMap.Columns.get(ColumnEnumType.CLASSIFICATION), cls);
	}

	
	public static QueryField getFieldEstimateId(long val){
		return getBigIntField(FieldMap.Columns.get(ColumnEnumType.ESTIMATEID), val);
	}
	public static QueryField getFieldResourceId(long val){
		return getBigIntField(FieldMap.Columns.get(ColumnEnumType.RESOURCEID), val);
	}
	public static QueryField getFieldScheduleId(long val){
		return getBigIntField(FieldMap.Columns.get(ColumnEnumType.SCHEDULEID), val);
	}
	public static QueryField getFieldWorkId(long val){
		return getBigIntField(FieldMap.Columns.get(ColumnEnumType.WORKID), val);
	}
	public static QueryField getFieldBudgetId(long val){
		return getBigIntField(FieldMap.Columns.get(ColumnEnumType.BUDGETID), val);
	}
	public static QueryField getFieldMethodologyId(long val){
		return getBigIntField(FieldMap.Columns.get(ColumnEnumType.METHODOLOGYID), val);
	}
	public static QueryField getFieldUtilization(double val){
		return getDoubleField(FieldMap.Columns.get(ColumnEnumType.UTILIZATION), val);
	}
	public static QueryField getFieldIterates(boolean val){
		return getBooleanField(FieldMap.Columns.get(ColumnEnumType.ITERATES),val);
	}
	public static QueryField getFieldIsBinary(boolean val){
		return getBooleanField(FieldMap.Columns.get(ColumnEnumType.ISBINARY),val);
	}
	public static QueryField getFieldBinaryValueId(long val){
		return getBigIntField(FieldMap.Columns.get(ColumnEnumType.BINARYVALUEID),val);
	}
	public static QueryField getFieldTextValue(String val){
		return getStringField(FieldMap.Columns.get(ColumnEnumType.TEXTVALUE),val);
	}
	public static QueryField getFieldFormElementId(long val){
		return getBigIntField(FieldMap.Columns.get(ColumnEnumType.FORMELEMENTID),val);
	}
	public static QueryField getFieldElementType(ElementEnumType val){
		return getStringField(FieldMap.Columns.get(ColumnEnumType.ELEMENTTYPE),val.toString());
	}
	public static QueryField getFieldElementName(String val){
		return getStringField(FieldMap.Columns.get(ColumnEnumType.ELEMENTNAME),val);
	}
	public static QueryField getFieldElementLabel(String val){
		return getStringField(FieldMap.Columns.get(ColumnEnumType.ELEMENTLABEL),val);
	}
	public static QueryField getFieldFormId(long val){
		return getBigIntField(FieldMap.Columns.get(ColumnEnumType.FORMID),val);
	}	
	public static QueryField getFieldValidationRuleId(long val){
		return getBigIntField(FieldMap.Columns.get(ColumnEnumType.VALIDATIONRULEID),val);
	}
	public static QueryField getFieldAllowNull(boolean val){
		return getBooleanField(FieldMap.Columns.get(ColumnEnumType.ALLOWNULL),val);
	}
	public static QueryField getFieldTemplateId(long val){
		return getBigIntField(FieldMap.Columns.get(ColumnEnumType.TEMPLATEID),val);
	}
	public static QueryField getFieldViewTemplateId(long val){
		return getBigIntField(FieldMap.Columns.get(ColumnEnumType.VIEWTEMPLATEID),val);
	}
	public static QueryField getFieldElementTemplateId(long val){
		return getBigIntField(FieldMap.Columns.get(ColumnEnumType.ELEMENTTEMPLATEID),val);
	}

	public static QueryField getFieldIsTemplate(boolean val){
		return getBooleanField(FieldMap.Columns.get(ColumnEnumType.ISTEMPLATE),val);
	}
	public static QueryField getFieldIsGrid(boolean val){
		return getBooleanField(FieldMap.Columns.get(ColumnEnumType.ISGRID),val);
	}
	public static QueryField getFieldValidationType(ValidationEnumType val){
		return getStringField(FieldMap.Columns.get(ColumnEnumType.VALIDATIONTYPE),val.toString());
	}
	public static QueryField getFieldIsRuleSet(boolean val){
		return getBooleanField(FieldMap.Columns.get(ColumnEnumType.ISRULESET),val);
	}
	public static QueryField getFieldComparison(boolean val){
		return getBooleanField(FieldMap.Columns.get(ColumnEnumType.COMPARISON),val);
	}
	public static QueryField getFieldIsReplacementRule(boolean val){
		return getBooleanField(FieldMap.Columns.get(ColumnEnumType.ISREPLACEMENTRULE),val);
	}
	public static QueryField getFieldExpression(String val){
		return getStringField(FieldMap.Columns.get(ColumnEnumType.EXPRESSION),val);
	}
	public static QueryField getFieldErrorMessage(String val){
		return getStringField(FieldMap.Columns.get(ColumnEnumType.ERRORMESSAGE),val);
	}
	public static QueryField getFieldReplacementValue(String val){
		return getStringField(FieldMap.Columns.get(ColumnEnumType.REPLACEMENTVALUE),val);
	}
	public static QueryField getFieldRequirementId(String val){
		return getStringField(FieldMap.Columns.get(ColumnEnumType.REQUIREMENTID),val);
	}
	public static QueryField getFieldText(String s){
		return getBytesField(FieldMap.Columns.get(ColumnEnumType.TEXT),s.getBytes(StandardCharsets.UTF_8));
	}
	public static QueryField getFieldLongitude(double l){
		return getDoubleField("longitude", l);
	}
	public static QueryField getFieldLatitude(double l){
		return getDoubleField("latitude", l);
	}
}
