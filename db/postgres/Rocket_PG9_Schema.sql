

DROP TABLE IF EXISTS lifecycle CASCADE;
create table lifecycle (

) inherits (uniquenamegroup,objectdescription);

DROP TABLE IF EXISTS lifecycleparticipation CASCADE;
CREATE TABLE lifecycleparticipation (
) inherits (participation);

DROP TABLE IF EXISTS schedule CASCADE;
create table schedule (
	StartTime timestamp not null,
	EndTime timestamp not null
) inherits (uniquenamegroup,objectdescription,objectorderscore);



DROP TABLE IF EXISTS scheduleparticipation CASCADE;
CREATE TABLE scheduleparticipation (
) inherits (participation);

DROP TABLE IF EXISTS goal CASCADE;
create table goal (
	GoalType varchar(16) not null,
	BudgetId bigint not null default 0,
	ResourceId bigint not null default 0,
	ScheduleId bigint not null default 0,
    Priority varchar(16) not null
) inherits (uniquenamegroup,objectdescription,objectorderscore);

DROP TABLE IF EXISTS goalparticipation CASCADE;
CREATE TABLE goalparticipation (
) inherits (participation);


DROP TABLE IF EXISTS cost CASCADE;
create table cost (
	CurrencyType varchar(16) not null,
	Value double precision not null default 0
) inherits (uniquenamegroup,objectdescription);

DROP TABLE IF EXISTS time CASCADE;
create table time (
	BasisType varchar(16) not null,
	Value double precision not null default 0
) inherits (uniquenamegroup,objectdescription);

DROP TABLE IF EXISTS timeparticipation CASCADE;
CREATE TABLE timeparticipation (
) inherits (participation);

DROP TABLE IF EXISTS budget CASCADE;
create table budget (
	BudgetType varchar(16) not null,
	TimeId bigint not null default 0,
	CostId bigint not null default 0
) inherits (uniquenamegroup,objectdescription);

DROP TABLE IF EXISTS project CASCADE;
create table project (
    ScheduleId bigint not null default 0
) inherits (uniquenamegroup,objectdescription);

DROP TABLE IF EXISTS projectparticipation CASCADE;
CREATE TABLE projectparticipation (
) inherits (participation);

DROP TABLE IF EXISTS artifact CASCADE;
create table artifact (
	ArtifactType varchar(16) not null,
	ArtifactDataId bigint not null default 0,
	PreviousTransitionId bigint not null default 0,
	NextTransitionId bigint not null default 0
) inherits (uniquenamegroup,objectdescription,objectdate);

DROP TABLE IF EXISTS requirement CASCADE;
create table requirement (
	RequirementType varchar(16) not null,
      Priority varchar(16) not null,
      RequirementStatus varchar(16) not null,
	NoteId bigint not null default 0,
	FormId bigint not null default 0,
	RequirementId varchar(255)
) inherits (uniquenamegroup,objectdescription,objectorderscore);

DROP TABLE IF EXISTS usecase CASCADE;
create table usecase (
	CaseType varchar(16) not null
) inherits (uniquenamegroup,objectdescription);

DROP TABLE IF EXISTS usecaseparticipation CASCADE;
CREATE TABLE usecaseparticipation (
) inherits (participation);


DROP TABLE IF EXISTS model CASCADE;
create table model (
	ModelType varchar(16) not null
) inherits (uniquenamegroup,objectdescription);

DROP TABLE IF EXISTS modelparticipation CASCADE;
CREATE TABLE modelparticipation (
) inherits (participation);

DROP TABLE IF EXISTS note CASCADE;
create table note (
	Text bytea
) inherits (uniquenameparentgroup,objectdate);

DROP TABLE IF EXISTS estimate CASCADE;
create table estimate (
	EstimateType varchar(16) not null,
	TimeId bigint not null default 0,
	CostId bigint not null default 0
) inherits (uniquenamegroup,objectdescription);

DROP TABLE IF EXISTS task CASCADE;
create table task (
	EstimateId bigint not null default 0,
	TaskStatus varchar(16) not null,
      StartDate timestamp not null,
      CompletedDate timestamp not null,
      DueDate timestamp not null
) inherits (uniquenameparentgroup,objectdescription,objectorderscore,objectdate);

DROP TABLE IF EXISTS taskparticipation CASCADE;
CREATE TABLE taskparticipation (
) inherits (participation);

DROP TABLE IF EXISTS work CASCADE;
create table work (
) inherits (uniquenamegroup,objectdescription,objectorderscore);


DROP TABLE IF EXISTS workparticipation CASCADE;
CREATE TABLE workparticipation (
) inherits (participation);

DROP TABLE IF EXISTS resource CASCADE;
create table resource (
	ResourceId bigint not null default 0,
	ResourceType varchar(16) not null,
	EstimateId bigint not null default 0,
	ScheduleId bigint not null default 0,
	Utilization double precision not null default 0
) inherits (uniquenamegroup,objectdescription);

DROP TABLE IF EXISTS processstep CASCADE;
create table processstep (
) inherits (uniquenamegroup,objectdescription,objectorderscore);

DROP TABLE IF EXISTS processstepparticipation CASCADE;
CREATE TABLE processstepparticipation (
) inherits (participation);

DROP TABLE IF EXISTS process CASCADE;
create table process (
	Iterates boolean not null default false
) inherits (uniquenamegroup,objectdescription,objectorderscore);

DROP TABLE IF EXISTS processparticipation CASCADE;
CREATE TABLE processparticipation (
) inherits (participation);

DROP TABLE IF EXISTS methodology CASCADE;
create table methodology (
) inherits (uniquenamegroup,objectdescription);

DROP TABLE IF EXISTS methodologyparticipation CASCADE;
CREATE TABLE methodologyparticipation (
) inherits (participation);

DROP TABLE IF EXISTS stage CASCADE;
create table stage (
	MethodologyId bigint not null default 0,
	BudgetId bigint not null default 0,
	WorkId bigint not null default 0,
      ScheduleId bigint not null default 0
) inherits (uniquenamegroup,objectdescription,objectorderscore);


DROP TABLE IF EXISTS module CASCADE;
create table module (
	ModuleType varchar(16) not null,
	TimeId bigint not null default 0,
	CostId bigint not null default 0
) inherits (uniquenamegroup,objectdescription);


DROP TABLE IF EXISTS moduleparticipation CASCADE;
CREATE TABLE moduleparticipation (
) inherits (participation);

DROP TABLE IF EXISTS ticket CASCADE;
create table ticket (
	DueDate timestamp not null,
	ClosedDate timestamp not null,
	ReopenedDate timestamp not null,
      TicketStatus varchar(16) not null,
      Priority varchar(16) not null,
      Severity varchar(16) not null,
	AssignedResourceId bigint not null default 0,
	EstimateId bigint not null default 0,
	ActualTimeId bigint not null default 0,
	ActualCostId bigint not null default 0
) inherits (uniquenamegroup,objectdescription,objectdate);


DROP TABLE IF EXISTS ticketparticipation CASCADE;
CREATE TABLE ticketparticipation (
) inherits (participation);

DROP TABLE IF EXISTS formelementvalue CASCADE;
create table formelementvalue (
	Name varchar(255) not null,
	TextValue varchar(255),
	FormElementId bigint not null default 0,
	FormId bigint not null default 0,
	IsBinary boolean not null default false,
	BinaryValueId bigint not null default 0
) inherits (orgid);

DROP TABLE IF EXISTS formelement CASCADE;
create table formelement (
	ElementType varchar(16) not null,
	ElementName varchar(255),
	ElementLabel varchar(255),
	ElementTemplateId bigint not null default 0,
	ValidationRuleId bigint not null default 0
) inherits (uniquenamegroup,objectdescription);


DROP TABLE IF EXISTS formelementparticipation CASCADE;
CREATE TABLE formelementparticipation (
) inherits (participation);

DROP TABLE IF EXISTS validationrule CASCADE;

create table validationrule (
	Expression varchar(255),
	IsRuleSet boolean not null default false,
	IsReplacementRule boolean not null default false,
	ReplacementValue varchar(255),
	ValidationType varchar(16) not null,
	Comparison boolean not null default false,
      AllowNull boolean not null default false,
	ErrorMessage varchar(255)
) inherits (uniquenamegroup,objectdescription);

DROP TABLE IF EXISTS validationruleparticipation CASCADE;
CREATE TABLE validationruleparticipation (
) inherits (participation);


DROP TABLE IF EXISTS form CASCADE;
DROP SEQUENCE IF EXISTS form_id_seq;
CREATE SEQUENCE form_id_seq;
create table form (
	IsTemplate boolean not null default false,
    IsGrid boolean not null default false,
	TemplateId bigint not null default 0,
    ViewTemplateId bigint not null default 0
) inherits (uniquenamegroup,objectdescription);

DROP TABLE IF EXISTS formparticipation CASCADE;
CREATE TABLE formparticipation (
) inherits (participation);

DROP TABLE IF EXISTS location CASCADE;
create table location (
           GeographyType varchar(32) not null,
           Classification varchar(32)
) inherits (uniquenameparentgroup,objectdescription);

DROP TABLE IF EXISTS locationparticipation CASCADE;
CREATE TABLE locationparticipation (
) inherits (participation);

DROP TABLE IF EXISTS event CASCADE;
create table event (
	StartDate timestamp not null,
	EndDate timestamp not null,
	LocationId bigint not null default 0,
	EventType varchar(32) not null
) inherits (uniquenameparentgroup,objectdescription);

DROP TABLE IF EXISTS eventparticipation CASCADE;
CREATE TABLE eventparticipation (
) inherits (participation);

DROP TABLE IF EXISTS trait CASCADE;
create table trait (
	TraitType varchar(32) not null,
	Alignment varchar(32) not null
) inherits (uniquenamegroup,objectdescription,objectorderscore);



create or replace view orphanRocketAttributes as
select referenceid, referencetype,organizationid from attribute R1
where 
(referencetype = 'RESOURCE' AND referenceid not in (select id from resource R1))
OR
(referencetype = 'TRAIT' AND referenceid not in (select id from trait T1))
OR
(referencetype = 'LOCATION' AND referenceid not in (select id from location l1))
OR
organizationid not in (select id from organizations)
;
create or replace view orphanSchedules as
select id, name, groupid from schedule S1
where (groupid not in (select id from Groups G1)
AND S1.groupid > 0)
OR
organizationid not in (select id from organizations)
;

create or replace view orphanLocations as
select id, name, groupid from location R1
where (groupid not in (select id from Groups G1)
and R1.groupid > 0)
or
organizationid not in (select id from organizations)
;
create or replace view orphanLocationParticipations as
select id from LocationParticipation RP1
where 
(participationid not in (select id from location D1))
or
organizationid not in (select id from organizations)
;


create or replace view orphanEvents as
select id, name, groupid from event R1
where (groupid not in (select id from Groups G1)
and R1.groupid > 0)
or
organizationid not in (select id from organizations)
;

create or replace view orphanEventParticipations as
select id from EventParticipation RP1
where 
(participationid not in (select id from event D1))
or
organizationid not in (select id from organizations)
;


create or replace view orphanTraits as
select id, name, groupid from trait R1
where (groupid not in (select id from Groups G1)
and R1.groupid > 0)
or
organizationid not in (select id from organizations)
;

create or replace view orphanResources as
select id, name, groupid from resource R1
where (groupid not in (select id from Groups G1)
and R1.groupid > 0)
or
organizationid not in (select id from organizations)
;
create or replace view orphanWork as
select id, name, groupid from work S1
where (groupid not in (select id from Groups G1)
AND S1.groupid > 0)
OR
organizationid not in (select id from organizations)
;
create or replace view orphanStages as
select id, name, groupid from stage S1
where (groupid not in (select id from Groups G1)
AND S1.groupid > 0)
OR
organizationid not in (select id from organizations)
;
create or replace view orphanTasks as
select id, name, groupid from task S1
where (groupid not in (select id from Groups G1)
AND S1.groupid > 0)
OR
organizationid not in (select id from organizations)
;
create or replace view orphanTimes as
select id, name, groupid from time S1
where (groupid not in (select id from Groups G1)
AND S1.groupid > 0)
OR
organizationid not in (select id from organizations)
;
create or replace view orphanCosts as
select id, name, groupid from cost S1
where (groupid not in (select id from Groups G1)
AND S1.groupid > 0)
OR
organizationid not in (select id from organizations)
;
create or replace view orphanBudgets as
select id, name, groupid from budget S1
where (groupid not in (select id from Groups G1)
AND S1.groupid > 0)
OR
organizationid not in (select id from organizations)
;
create or replace view orphanEstimates as
select id, name, groupid from estimate S1
where (groupid not in (select id from Groups G1)
AND S1.groupid > 0)
OR
organizationid not in (select id from organizations)
;
create or replace view orphanMethodologies as
select id, name, groupid from methodology S1
where (groupid not in (select id from Groups G1)
AND S1.groupid > 0)
OR
organizationid not in (select id from organizations)
;
create or replace view orphanProcesses as
select id, name, groupid from process S1
where (groupid not in (select id from Groups G1)
AND S1.groupid > 0)
OR
organizationid not in (select id from organizations)
;
create or replace view orphanProcessSteps as
select id, name, groupid from processstep S1
where (groupid not in (select id from Groups G1)
AND S1.groupid > 0)
OR
organizationid not in (select id from organizations)
;
create or replace view orphanForms as
select id, name, groupid from form S1
where (groupid not in (select id from Groups G1)
AND S1.groupid > 0)
OR
organizationid not in (select id from organizations)
;
create or replace view orphanFormElements as
select id, name, groupid from formelement S1
where (groupid not in (select id from Groups G1)
AND S1.groupid > 0)
OR
organizationid not in (select id from organizations)
;
create or replace view orphanArtifacts as
select id, name, groupid from artifact S1
where (groupid not in (select id from Groups G1)
AND S1.groupid > 0)
OR
organizationid not in (select id from organizations)
;
create or replace view orphanModels as
select id, name, groupid from model S1
where (groupid not in (select id from Groups G1)
AND S1.groupid > 0)
OR
organizationid not in (select id from organizations)
;
create or replace view orphanModules as
select id, name, groupid from module S1
where (groupid not in (select id from Groups G1)
AND S1.groupid > 0)
OR
organizationid not in (select id from organizations)
;
create or replace view orphanNotes as
select id, name, groupid from note S1
where (groupid not in (select id from Groups G1)
AND S1.groupid > 0)
OR
organizationid not in (select id from organizations)
;
create or replace view orphanRequirements as
select id, name, groupid from requirement S1
where (groupid not in (select id from Groups G1)
AND S1.groupid > 0)
OR
organizationid not in (select id from organizations)
;
create or replace view orphanProjects as
select id, name, groupid from project S1
where (groupid not in (select id from Groups G1)
AND S1.groupid > 0)
OR
organizationid not in (select id from organizations)
;
create or replace view orphanTickets as
select id, name, groupid from ticket S1
where (groupid not in (select id from Groups G1)
AND S1.groupid > 0)
OR
organizationid not in (select id from organizations)
;
create or replace view orphanValidationRules as
select id, name, groupid from validationrule S1
where (groupid not in (select id from Groups G1)
AND S1.groupid > 0)
OR
organizationid not in (select id from organizations)
;
create or replace view orphanGoals as
select id, name, groupid from goal S1
where (groupid not in (select id from Groups G1)
AND S1.groupid > 0)
OR
organizationid not in (select id from organizations)
;
create or replace view orphanUseCases as
select id, name, groupid from usecase S1
where (groupid not in (select id from Groups G1)
AND S1.groupid > 0)
OR
organizationid not in (select id from organizations)
;

create or replace view orphanUseCaseParticipations as
select id from UseCaseParticipation RP1
where 
(participationid not in (select id from usecase D1))
or
organizationid not in (select id from organizations)
;

create or replace view orphanFormElementParticipations as
select id from FormElementParticipation RP1
where 
(participationid not in (select id from formelement D1))
or
organizationid not in (select id from organizations)
;

create or replace view orphanFormParticipations as
select id from FormParticipation RP1
where 
(participationid not in (select id from form D1))
or
organizationid not in (select id from organizations)
;

create or replace view orphanLifecycleParticipations as
select id from LifecycleParticipation RP1
where 
(participationid not in (select id from lifecycle D1))
or
organizationid not in (select id from organizations)
;

create or replace view orphanMethodologyParticipations as
select id from MethodologyParticipation RP1
where 
(participationid not in (select id from methodology D1))
or
organizationid not in (select id from organizations)
;

create or replace view orphanModelParticipations as
select id from ModelParticipation RP1
where 
(participationid not in (select id from model D1))
or
organizationid not in (select id from organizations)
;

create or replace view orphanModuleParticipations as
select id from ModuleParticipation RP1
where 
(participationid not in (select id from module D1))
or
organizationid not in (select id from organizations)
;

create or replace view orphanProcessParticipations as
select id from ProcessParticipation RP1
where 
(participationid not in (select id from process D1))
or
organizationid not in (select id from organizations)
;

create or replace view orphanProcessStepParticipations as
select id from ProcessStepParticipation RP1
where 
(participationid not in (select id from processstep D1))
or
organizationid not in (select id from organizations)
;

create or replace view orphanProjectParticipations as
select id from ProjectParticipation RP1
where 
(participationid not in (select id from project D1))
or
organizationid not in (select id from organizations)
;

create or replace view orphanScheduleParticipations as
select id from ScheduleParticipation RP1
where 
(participationid not in (select id from schedule D1))
or
organizationid not in (select id from organizations)
;

create or replace view orphanTaskParticipations as
select id from TaskParticipation RP1
where 
(participationid not in (select id from task D1))
or
organizationid not in (select id from organizations)
;

create or replace view orphanValidationRuleParticipations as
select id from ValidationRuleParticipation RP1
where 
(participationid not in (select id from validationrule D1))
or
organizationid not in (select id from organizations)
;

create or replace view orphanWorkParticipations as
select id from WorkParticipation RP1
where 
(participationid not in (select id from work D1))
or
organizationid not in (select id from organizations)
;



CREATE OR REPLACE FUNCTION cleanup_rocket_orphans() 
        RETURNS BOOLEAN
        AS $$
	delete from location where id in (select id from orphanLocations);
	delete from event where id in (select id from orphanEvents);
	delete from trait where id in (select id from orphanTraits);
	delete from artifact where id in (select id from orphanArtifacts);
	delete from work where id in (select id from orphanWork);
	delete from schedule where id in (select id from orphanSchedules);
	delete from resource WHERE id in (select id from orphanResources);
	delete from stage WHERE id in (select id from orphanStages);
	delete from task WHERE id in (select id from orphanTasks);
	delete from time WHERE id in (select id from orphanTimes);
	delete from cost WHERE id in (select id from orphanCosts);
	delete from budget WHERE id in (select id from orphanBudgets);
	delete from estimate WHERE id in (select id from orphanEstimates);
	delete from methodology WHERE id in (select id from orphanMethodologies);
	delete from process WHERE id in (select id from orphanProcesses);
	delete from processstep WHERE id in (select id from orphanProcessSteps);
	delete from form WHERE id in (select id from orphanForms);
	delete from formelement WHERE id in (select id from orphanFormElements);
	delete from artifact WHERE id in (select id from orphanArtifacts);
	delete from model WHERE id in (select id from orphanModels);
	delete from module WHERE id in (select id from orphanModules);
	delete from note WHERE id in (select id from orphanNotes);
	delete from requirement WHERE id in (select id from orphanRequirements);
	delete from project WHERE id in (select id from orphanProjects);
	delete from ticket WHERE id in (select id from orphanTickets);
	delete from validationrule WHERE id in (select id from orphanValidationRules);
	delete from goal WHERE id in (select id from orphanGoals);
	delete from usecase WHERE id in (select id from orphanUseCases);
	delete from attribute A1 WHERE (A1.referenceid, A1.referencetype) IN (SELECT referenceid, referencetype FROM orphanRocketAttributes);
	delete from UseCaseParticipation WHERE id in (select id from orphanUseCaseParticipations);
	delete from FormElementParticipation WHERE id in (select id from orphanFormElementParticipations);
	delete from FormParticipation WHERE id in (select id from orphanFormParticipations);
	delete from LifecycleParticipation WHERE id in (select id from orphanLifecycleParticipations);
	delete from MethodologyParticipation WHERE id in (select id from orphanMethodologyParticipations);
	delete from ModelParticipation WHERE id in (select id from orphanModelParticipations);
	delete from ModuleParticipation WHERE id in (select id from orphanModuleParticipations);
	delete from ProcessParticipation WHERE id in (select id from orphanProcessParticipations);
	delete from ProcessStepParticipation WHERE id in (select id from orphanProcessStepParticipations);
	delete from ProjectParticipation WHERE id in (select id from orphanProjectParticipations);
	delete from ScheduleParticipation WHERE id in (select id from orphanScheduleParticipations);
	delete from TaskParticipation WHERE id in (select id from orphanTaskParticipations);
	delete from ValidationRuleParticipation WHERE id in (select id from orphanValidationRuleParticipations);
	delete from WorkParticipation WHERE id in (select id from orphanWorkParticipations);
	delete from LocationParticipation WHERE id in (select id from orphanLocationParticipations);
	delete from EventParticipation WHERE id in (select id from orphanEventParticipations);
	SELECT true;
        $$ LANGUAGE 'sql';


CREATE OR REPLACE FUNCTION tasks_from_leaf(task_id BIGINT,organizationid BIGINT) 
        RETURNS TABLE (leafid BIGINT,taskid BIGINT, parentid BIGINT, organizationid BIGINT)
        AS $$
	WITH RECURSIVE task_tree(leafid,taskid, parentid, organizationid) AS (
	   SELECT $1 as leafid, T.id as taskid, T.parentid, T.organizationid
	      FROM task T WHERE T.id = $1 AND T.organizationid = $2
	   UNION ALL
	   SELECT $1 as leafid, P.id, P.parentid, P.organizationid
	      FROM task_tree RT, task P
	      WHERE RT.taskid = P.parentid AND RT.organizationid = $2
	)
	select * from task_tree;
        $$ LANGUAGE 'sql';


--- Needs to have a schedule attached to the lifecycle
DROP VIEW IF EXISTS lifecycleSchedule CASCADE;
create or replace view lifecycleSchedule as 
select L.organizationid as organizationid,L.id as lifecycleid, L.name as lifecyclename,  L.groupid as lifecyclegroupid,S.name as lifecycleschedule, s.starttime as lifecyclestarttime,s.endtime as lifecycleendtime
from Lifecycle L
inner join LifecycleParticipation LP on LP.participationid = L.id
left join Schedule S on LP.participantid = S.id
WHERE
LP.participanttype = 'SCHEDULE'
;

--- Project needs to be part of a lifecycle
DROP VIEW IF EXISTS lifecycleProject CASCADE;
create or replace view lifecycleProject as 
select L.organizationid as organizationid, L.lifecycleid, L.lifecyclename, L.lifecyclegroupid,L.lifecyclestarttime, L.lifecycleendtime, P.id as projectid,P.name as projectname, P.groupid as projectgroupid,PS.name as projectschedule,PS.starttime as projectstarttime,PS.endtime as projectendtime
from lifecycleschedule L
inner join LifecycleParticipation LP on LP.participationid = L.lifecycleid
left join Project P on LP.participantid = P.id
left join Schedule PS on P.scheduleid = PS.id
where
LP.participanttype = 'PROJECT';

DROP VIEW IF EXISTS lifecycleProjectStage CASCADE;
create or replace view lifecycleProjectStage as
select LP.organizationid, LP.lifecycleid, LP.lifecyclename, LP.lifecyclegroupid,LP.lifecyclestarttime,LP.lifecycleendtime
, LP.projectid, LP.projectname, LP.projectgroupid,LP.projectstarttime,LP.projectendtime
,S.id as stageid, S.name as stagename, S.groupid as stagegroupid,SS.name as stageschedule, SS.starttime as stagestarttime, SS.endtime as stageendtime,S.logicalorder as stageorder,T.basistype stageestimatebasis, T.value as stageestimate, C.currencytype as stagecosttype, C.value as stagecostvalue
, W.id as stageworkid, W.name as stageworkname, W.groupid as stageworkgroupid,W.logicalorder as stageworkorder
from lifecycleProject as LP
inner join ProjectParticipation PP on PP.participationid = LP.projectid
left join Stage S on S.id = PP.participantid
left join Budget B on B.id = S.budgetid
left join Schedule SS on SS.id = S.scheduleid
left join Time T on B.timeid = T.id
left join Cost C on B.costid = C.id
left join Work W on S.workid = W.id
where
participanttype = 'STAGE'
ORDER BY stageorder,stagename;


DROP VIEW IF EXISTS lifecycleWorkTasks CASCADE;
create or replace view lifecycleWorkTasks as
WITH result AS(
   select LPS.organizationid,LPS.lifecycleid,LPS.lifecyclename,LPS.lifecyclegroupid,LPS.lifecyclestarttime,LPS.lifecycleendtime,LPS.projectid,LPS.projectname,LPS.projectgroupid,LPS.projectstarttime,LPS.projectendtime,LPS.stageid,LPS.stagename,LPS.stagegroupid,LPS.stageorder,LPS.stageworkid,LPS.stagestarttime,LPS.stageendtime,LPS.stageworkname,LPS.stageworkorder,LPS.stageworkgroupid,tasks_from_leaf(WP.participantid,WP.organizationid) trl FROM lifecycleProjectStage LPS
   inner join workparticipation WP on WP.participationid = LPS.stageworkid AND WP.participanttype = 'TASK'
)
SELECT 
	R.organizationid,lifecycleid,lifecyclename,lifecyclegroupid,lifecyclestarttime,lifecycleendtime,
	projectid,projectname,projectgroupid,projectstarttime,projectendtime,
	stageid,stagename,stagegroupid,stageorder,stagestarttime,stageendtime,
	stageworkid,stageworkname,stageworkorder,stageworkgroupid,(R.trl).leafid,
	(R.trl).taskid,(R.trl).parentid as taskparentid,T.groupid as taskgroupid,T.name as taskname,T.logicalorder as taskorder,
	T.taskstatus,T.duedate as taskduedate, T.completeddate as taskcompleteddate
FROM result R
INNER JOIN task T on T.id = (R.trl).taskid
ORDER by stageorder,stagename,stageworkorder,stageworkname,taskorder,taskname;


DROP VIEW IF EXISTS lifecycleWorkTaskHours CASCADE;
create or replace view lifecycleWorkTaskHours as
WITH result AS(
   select LPS.organizationid,LPS.lifecycleid,LPS.lifecyclename,LPS.lifecyclegroupid,LPS.lifecyclestarttime,LPS.lifecycleendtime,LPS.projectid,LPS.projectname,LPS.projectgroupid,LPS.projectstarttime,LPS.projectendtime,LPS.stageid,LPS.stagename,LPS.stagegroupid,LPS.stageorder,LPS.stageworkid,LPS.stagestarttime,LPS.stageendtime,LPS.stageworkname,LPS.stageworkorder,LPS.stageworkgroupid,tasks_from_leaf(WP.participantid,WP.organizationid) trl FROM lifecycleProjectStage LPS
   inner join workparticipation WP on WP.participationid = LPS.stageworkid AND WP.participanttype = 'TASK'
)
SELECT 
	R.organizationid,lifecycleid,lifecyclename,lifecyclegroupid,lifecyclestarttime,lifecycleendtime,
	projectid,projectname,projectgroupid,projectstarttime,projectendtime,
	stageid,stagename,stagegroupid,stageorder,stagestarttime,stageendtime,
	stageworkid,stageworkname,stageworkorder,stageworkgroupid,(R.trl).leafid,
	(R.trl).taskid,(R.trl).parentid as taskparentid,T.groupid as taskgroupid,T.name as taskname,T.logicalorder as taskorder,
	T.taskstatus,T.duedate as taskduedate, T.completeddate as taskcompleteddate,TT.value as taskestimate,TT.basistype as taskestimatetype,
	CASE
	     WHEN TT.basistype = 'WEEK' THEN (TT.value * 5 * 8)
	     WHEN TT.basistype = 'DAY' THEN (TT.value * 8)
	     WHEN TT.basistype = 'HOUR' THEN (TT.value)
	     WHEN TT.basistype = 'MONTH' THEN (TT.value * 30 * 8)
	     ELSE 0
	END as taskhours
FROM result R
INNER JOIN task T on T.id = (R.trl).taskid
left join estimate E on E.id = T.estimateid
left join time TT on TT.id = E.timeid
left join cost CT on CT.id = E.costid
ORDER by stageorder,stagename,stageworkorder,stageworkname,taskorder,taskname;
;

DROP VIEW IF EXISTS taskResourceGroup CASCADE;
create or replace view taskResourceGroup as
select LWT.organizationid,taskid,array_to_string(array_agg(R.name),',') as resourcelist,count(R.name) as resourcecount
 from lifecycleWorkTasks as LWT
 inner join taskparticipation as TP on TP.participationid = LWT.taskid
 inner join resource as R on R.id = TP.participantid
WHERE
TP.participanttype = 'RESOURCE'
group by LWT.organizationid,taskid;

DROP VIEW IF EXISTS lifecycleWorkTaskResources CASCADE;
create or replace view lifecycleWorkTaskResources as 
select distinct LWT.organizationid,LWT.lifecycleid,LWT.lifecyclename,LWT.lifecyclegroupid,LWT.lifecyclestarttime,LWT.lifecycleendtime,LWT.projectid,LWT.projectname,LWT.projectgroupid,LWT.projectstarttime,LWT.projectendtime
,LWT.stageorder,LWT.stagename,LWT.stageworkorder,LWT.stageworkname,LWT.stagegroupid,LWT.stageworkgroupid,LWT.stagestarttime,LWT.stageendtime
,LWT.taskorder,LWT.taskid,LWT.taskstatus,LWT.taskname,LWT.taskgroupid,LWT.taskduedate,LWT.taskcompleteddate,LWT.taskestimate,LWT.taskestimatetype,LWT.taskhours
,R.id as resourceid,R.resourcetype,R.name as resourcename,R.groupid as resourcegroupid
 from lifecycleWorkTaskHours as LWT
 inner join taskparticipation as TP on TP.participationid = LWT.taskid and participanttype = 'RESOURCE'
 inner join resource R on R.id = TP.participantid
 ;


DROP VIEW IF EXISTS lifecycleWorkTaskResourceGroup CASCADE;
create or replace view lifecycleWorkTaskResourceGroup as 
select distinct LWT.organizationid,LWT.lifecycleid,LWT.lifecyclename,LWT.lifecyclegroupid,LWT.lifecyclestarttime,LWT.lifecycleendtime,LWT.projectid,LWT.projectname,LWT.projectgroupid,LWT.projectstarttime,LWT.projectendtime
,LWT.stageorder,LWT.stagename,LWT.stagegroupid,LWT.stagestarttime,LWT.stageendtime,LWT.stageworkorder,LWT.stageworkname,LWT.stageworkgroupid
,LWT.taskorder,LWT.taskid,LWT.taskstatus,LWT.taskname,LWT.taskgroupid,LWT.taskduedate,LWT.taskcompleteddate,LWT.taskestimate,LWT.taskestimatetype,LWT.taskhours
,TRG.resourcelist,TRG.resourcecount,(TRG.resourcecount * LWT.taskhours) as totalresourcehours
 from lifecycleWorkTaskHours as LWT
 inner join (select TRG.taskid,TRG.resourcelist,TRG.resourcecount FROM taskResourceGroup as TRG) as TRG on TRG.taskid = LWT.taskid
 ;

DROP VIEW IF EXISTS taskResources CASCADE;
create or replace view taskResources as
select T.organizationid,T.id,array_to_string(array_agg(R.name),',') as resourcelist,count(R.name) as resourcecount
 from task as T
 inner join taskparticipation as TP on TP.participationid = T.id
 inner join resource as R on R.id = TP.participantid
WHERE
TP.participanttype = 'RESOURCE'
GROUP BY T.organizationid,T.id;

-- SprintView requires the following structure:
-- Project:
--   Stage: (Iteration level - filtered to methodology process to iterate)
--      Work: (Iteration level)
--         Task: (Story level)
--            Work: (Used to group tasks to a 'story' level)
--               Task: (Task level)


DROP VIEW IF EXISTS logicalTaskView CASCADE;
create or replace view logicalTaskView as
select 
CASE
   WHEN lifecycleendtime < current_timestamp THEN 'LATE'
   ELSE 'ONGOING'
END as lifecycleschedulestatus
,CASE
   WHEN projectendtime < current_timestamp THEN 'LATE'
   ELSE 'ONGOING'
END as projectschedulestatus
,CASE
   WHEN stageendtime < current_timestamp THEN 'LATE'
   ELSE 'ONGOING'
END as stageschedulestatus
,CASE
   WHEN taskduedate < current_timestamp AND taskstatus <> 'COMPLETED' THEN 'LATE'
   WHEN taskduedate < (stagestarttime + interval '1H' * (taskhours*3) ) AND taskstatus <> 'COMPLETED' THEN 'UNDERESTIMATED'
   WHEN taskduedate >= (stagestarttime + interval '1H' * (taskhours*3) ) AND taskstatus <> 'COMPLETED' THEN 'IN_PROGRESS'
   WHEN taskstatus = 'COMPLETED' THEN 'FINISHED'
   ELSE 'WARN'
END as taskschedulestatus
-- ,(stagestarttime + interval '1H' * (taskhours*3) ) as warnunderdate
,organizationid,lifecycleid,lifecyclename,lifecyclegroupid,lifecyclestarttime,lifecycleendtime
,projectid,projectname,projectgroupid,projectstarttime,projectendtime
,stageorder,stagename,stagegroupid,stagestarttime,stageendtime,stageworkorder,stageworkname,stageworkgroupid
,taskorder,taskid,taskstatus,taskname,taskgroupid,taskduedate,taskcompleteddate,taskestimate,taskestimatetype,taskhours
,resourceid,resourcegroupid,resourcetype,resourcename
 from lifecycleWorkTaskResources
order by lifecyclename,projectname,stageorder,stagename,stageworkorder,stageworkname,taskorder,taskname,resourcename
;

DROP VIEW IF EXISTS scheduledTaskView CASCADE;
create or replace view scheduledTaskView as
select 
CASE
   WHEN lifecycleendtime < current_timestamp THEN 'LATE'
   ELSE 'ONGOING'
END as lifecycleschedulestatus
,CASE
   WHEN projectendtime < current_timestamp THEN 'LATE'
   ELSE 'ONGOING'
END as projectschedulestatus
,CASE
   WHEN stageendtime < current_timestamp THEN 'LATE'
   ELSE 'ONGOING'
END as stageschedulestatus
,CASE
   WHEN taskduedate < current_timestamp AND taskstatus <> 'COMPLETED' THEN 'LATE'
   WHEN taskduedate < (stagestarttime + interval '1H' * (taskhours*3) ) AND taskstatus <> 'COMPLETED' THEN 'UNDERESTIMATED'
   WHEN taskduedate >= (stagestarttime + interval '1H' * (taskhours*3) ) AND taskstatus <> 'COMPLETED' THEN 'IN_PROGRESS'
   WHEN taskstatus = 'COMPLETED' THEN 'FINISHED'
   ELSE 'WARN'
END as taskschedulestatus
,organizationid,lifecycleid,lifecyclename,lifecyclegroupid,lifecyclestarttime,lifecycleendtime
,projectid,projectname,projectgroupid,projectstarttime,projectendtime
,stageorder,stagename,stagegroupid,stagestarttime,stageendtime,stageworkorder,stageworkname,stageworkgroupid
,taskorder,taskid,taskstatus,taskname,taskgroupid,taskduedate,taskcompleteddate,taskestimate,taskestimatetype,taskhours
,resourceid,resourcegroupid,resourcetype,resourcename
 from lifecycleWorkTaskResources
order by taskduedate,taskname,stageendtime,stagename,projectendtime,projectname,lifecycleendtime,lifecyclename
;


DROP VIEW IF EXISTS sprintView CASCADE;
create or replace view sprintView as
 select L1.organizationid,L1.ownerid,L1.name as lifecycleName,P1.name as projectName,SG.name as Sprint,SC.startTime,SC.endTime,T1.logicalorder as storyorder,T1.id as taskid,T1.id as storyid,T1.name as storyName,T1.taskstatus as storystatus,T2.logicalorder as taskorder,T2.name as taskName,T2.taskstatus,TR.resourcelist
 ,CASE
     WHEN TT.basistype = 'WEEK' THEN (TT.value * 5 * 8)
     WHEN TT.basistype = 'DAY' THEN (TT.value * 8)
     WHEN TT.basistype = 'HOUR' THEN (TT.value)
     WHEN TT.basistype = 'MONTH' THEN (TT.value * 30 * 8)
     ELSE 0
END as taskhours
  from lifecycle L1
inner join LifecycleParticipation LP1 on LP1.participationid = L1.id
left join Schedule LS1 on LP1.participantid = LS1.id
inner join Project P1 on LP1.participantid = P1.id
inner join ProjectParticipation PP on PP.participationid = P1.id
inner join Stage SG on SG.id = PP.participantid
inner join methodology M on M.id = SG.methodologyid
inner join methodologyparticipation MP on MP.participationid = M.id
inner join process P on MP.participantid = P.id
inner join schedule SC on SC.id = SG.scheduleid
inner join work W1 on W1.id = SG.workid
inner join workparticipation WP1 on WP1.participationid = W1.id
inner join task T1 on T1.id = WP1.participantid
inner join taskparticipation TP1 on TP1.participationid = T1.id
inner join work W2 on W2.id = TP1.participantid
inner join workparticipation WP2 on WP2.participationid = W2.id
inner join task T2 on T2.id = WP2.participantid
left join estimate E on E.id = T2.estimateid
left join time TT on TT.id = E.timeid
left join cost CT on CT.id = E.costid
inner join (select TR.id,TR.resourcelist,TR.resourcecount FROM taskResources as TR) as TR on TR.id = T2.id
WHERE MP.participanttype = 'PROCESS'
AND P.iterates = true
ORDER BY SC.startTime,SG.logicalorder,W1.logicalorder,T1.logicalorder,W2.logicalorder,T2.logicalorder;

DROP VIEW IF EXISTS sprintReport CASCADE;
create view sprintReport as
select S1.lifecyclename,S1.storyid,S1.projectname,S1.sprint,S1.starttime,S1.endtime,S1.storyname
,CASE
     WHEN TS1.completecount > 0 AND TS4.totalcount > 0 THEN (100.0 * (TS1.completecount/TS4.totalcount))
     ELSE 0
END as percentcomplete
,CASE
     WHEN TS4.totalcount > 0 THEN TS4.totalcount
     ELSE 0
END as totalcount
,CASE
     WHEN TS1.completecount > 0 THEN TS1.completecount
     ELSE 0
END as completecount
,CASE
     WHEN TS3.progresscount > 0 THEN TS3.progresscount
     ELSE 0
END as progresscount
,CASE
     WHEN TS2.estimatedcount > 0 THEN TS2.estimatedcount
     ELSE 0
END as estimatedcount
 from sprintView S1
left join (select storyid,count(taskstatus) as completecount from sprintView as TS1 where taskstatus = 'COMPLETED' group by storyid) as TS1 on TS1.storyid = S1.storyid
left join (select storyid,count(taskstatus) as estimatedcount from sprintView as TS1 where taskstatus = 'ESTIMATED' group by storyid) as TS2 on TS2.storyid = S1.storyid
left join (select storyid,count(taskstatus) as progresscount from sprintView as TS1 where taskstatus = 'IN_PROGRESS' group by storyid) as TS3 on TS3.storyid = S1.storyid
left join (select storyid,count(taskstatus) as totalcount from sprintView as TS1 group by storyid) as TS4 on TS4.storyid = S1.storyid

group by S1.lifecyclename,S1.projectname,S1.sprint,S1.starttime,S1.endtime,S1.storyname,S1.storyid,TS4.totalcount,TS3.progresscount,TS2.estimatedcount,TS1.completecount
order by S1.projectname,S1.sprint,S1.storyname;

DROP VIEW IF EXISTS orphanLifecycleParticipations CASCADE;
create or replace view orphanLifecycleParticipations as
select id from lifecycleParticipation where 
participationid not in (select id from lifecycle)
OR (participanttype = 'PROJECT' and participantid not in (select id from project))
OR (participanttype = 'SCHEDULE' and participantid not in (select id from schedule))
;

DROP VIEW IF EXISTS resourceView CASCADE;
create or replace view resourceView as
select R.id, R.name, R.groupid, R.resourceid, R.resourcetype,
CASE
   WHEN R.resourcetype = 'PERSON' THEN P.name
   WHEN R.resourcetype = 'USER' THEN U.name
   WHEN R.resourcetype = 'ACCOUNT' THEN A.name
   ELSE ''
END as resourcename
from resource R
left join Persons P on P.id = R.resourceid AND R.resourcetype = 'PERSON'
left join Users U on U.id = R.resourceid AND R.resourcetype = 'USER'
left join Accounts A on A.id = R.resourceid AND R.resourcetype = 'ACCOUNT'
;

drop view if exists taskResourceView CASCADE;
create or replace view taskResourceView as
select T.id,T.parentId,T.name,T.groupid,T.organizationid,T.taskstatus,T.createddate,T.modifieddate,T.startdate,T.duedate,T.completeddate,T.urn,RW.id as resourceid,RW.resourceType,RW.resourceName from task T
inner join taskparticipation TP on TP.participationid = T.id
inner join resourceView RW on RW.id = TP.participantid AND TP.participanttype = 'RESOURCE'
;


create or replace view identityServiceApplications as
select P.name as projectname, P.id as projectid, G2.name as applicationname,G2.id as applicationid from project P
join Groups G on G.parentid=P.groupid AND G.name = 'Applications'
join Groups G2 on G2.parentid = G.id
;

create or replace view identityServiceApplicationGroups as
select P.name as projectname, P.id as projectid, G2.name as applicationname,G2.id as applicationid,G3.id as applicationgroupid,G3.name as applicationgroupname from project P
join Groups G on G.parentid=P.groupid AND G.name = 'Applications'
join Groups G2 on G2.parentid = G.id
join Groups G3 on G3.parentId = G2.id
;

create or replace view identityServiceApplicationPermissions as
select P.name as projectname, P.id as projectid, G2.name as applicationname,G2.id as applicationid, P2.name as permissionname, P2.id as permissionid from project P
join Groups G on G.parentid=P.groupid AND G.name = 'Applications'
join Groups G2 on G2.parentid = G.id
join Attribute A0 on A0.referenceType = 'PERMISSION' AND A0.name = 'applicationid' AND CAST(A0.value as bigint) = G2.id
join Permissions P2 on P2.id = A0.referenceid
;

create or replace view identityServiceApplicationAccounts as
select P.name as projectname, P.id as projectid, G2.name as applicationname,G2.id as applicationid, A.name as accountname,A.id as accountid from project P
join Groups G on G.parentid=P.groupid AND G.name = 'Applications'
join Groups G2 on G2.parentid = G.id
join Accounts A on A.groupid = G2.id
;

create or replace view identityServicePersons as
select P.name as projectname, P.id as projectid, PE.name as personname,PE.id as personid,PAM.value as manager from project P
join Groups PSG on PSG.parentid=P.groupid AND PSG.name = 'Persons'
join Persons PE on PE.groupid = PSG.id
left join Attribute PAM on PAM.referenceid = PE.id AND PAM.referencetype = 'PERSON' AND PAM.name = 'manager'
;

create or replace view identityServicePersonAccounts as
select P.name as projectname, P.id as projectid, PE.name as personname,PE.id as personid,A.name as accountname,A.id as accountid from project P
join Groups PSG on PSG.parentid=P.groupid AND PSG.name = 'Persons'
join Persons PE on PE.groupid = PSG.id
left join personparticipation PPA on PPA.participationid = PE.id AND PPA.participanttype = 'ACCOUNT'
left join accounts A on A.id = PPA.participantid
;

-- vacuum full analyze verbose


-- *** TABLE INHERITENCE FIX FOR PG9 WHERE UNIQUE CONSTRAINTS DON'T INHERIT
CREATE UNIQUE INDEX lifecycle_urn ON lifecycle(Urn);
CREATE UNIQUE INDEX IdxlifecycleNameGroup on lifecycle(Name,GroupId,OrganizationId);
CREATE UNIQUE INDEX IdxlifecycleparticipationCbo on lifecycleparticipation(ParticipationId,ParticipantId,ParticipantType,AffectId,OrganizationId);
CREATE UNIQUE INDEX schedule_urn ON schedule(Urn);
CREATE UNIQUE INDEX IdxscheduleNameGroup on schedule(Name,GroupId,OrganizationId);
CREATE UNIQUE INDEX IdxscheduleparticipationCbo on scheduleparticipation(ParticipationId,ParticipantId,ParticipantType,AffectId,OrganizationId);
CREATE UNIQUE INDEX goal_urn ON goal(Urn);
CREATE UNIQUE INDEX IdxgoalNameGroup on goal(Name,GroupId,OrganizationId);
CREATE UNIQUE INDEX IdxgoalparticipationCbo on goalparticipation(ParticipationId,ParticipantId,ParticipantType,AffectId,OrganizationId);
CREATE UNIQUE INDEX cost_urn ON cost(Urn);
CREATE UNIQUE INDEX IdxcostNameGroup on cost(Name,GroupId,OrganizationId);
CREATE UNIQUE INDEX time_urn ON time(Urn);
CREATE UNIQUE INDEX IdxtimeNameGroup on time(Name,GroupId,OrganizationId);
CREATE UNIQUE INDEX IdxtimeparticipationCbo on timeparticipation(ParticipationId,ParticipantId,ParticipantType,AffectId,OrganizationId);
CREATE UNIQUE INDEX budget_urn ON budget(Urn);
CREATE UNIQUE INDEX IdxbudgetNameGroup on budget(Name,GroupId,OrganizationId);
CREATE UNIQUE INDEX project_urn ON project(Urn);
CREATE UNIQUE INDEX IdxprojectNameGroup on project(Name,GroupId,OrganizationId);
CREATE UNIQUE INDEX IdxprojectparticipationCbo on projectparticipation(ParticipationId,ParticipantId,ParticipantType,AffectId,OrganizationId);
CREATE UNIQUE INDEX artifact_urn ON artifact(Urn);
CREATE UNIQUE INDEX IdxartifactNameGroup on artifact(Name,GroupId,OrganizationId);
CREATE UNIQUE INDEX requirement_urn ON requirement(Urn);
CREATE UNIQUE INDEX IdxrequirementNameGroup on requirement(Name,GroupId,OrganizationId);
CREATE UNIQUE INDEX usecase_urn ON usecase(Urn);
CREATE UNIQUE INDEX IdxusecaseNameGroup on usecase(Name,GroupId,OrganizationId);
CREATE UNIQUE INDEX IdxusecaseparticipationCbo on usecaseparticipation(ParticipationId,ParticipantId,ParticipantType,AffectId,OrganizationId);
CREATE UNIQUE INDEX model_urn ON model(Urn);
CREATE UNIQUE INDEX IdxmodelNameGroup on model(Name,GroupId,OrganizationId);
CREATE UNIQUE INDEX IdxmodelparticipationCbo on modelparticipation(ParticipationId,ParticipantId,ParticipantType,AffectId,OrganizationId);
CREATE UNIQUE INDEX note_text ON note(Urn);
CREATE UNIQUE INDEX IdxnoteNameGroup on note(Name,ParentId,GroupId,OrganizationId);
CREATE UNIQUE INDEX estimate_urn ON estimate(Urn);
CREATE UNIQUE INDEX IdxestimateNameGroup on estimate(Name,GroupId,OrganizationId);
CREATE UNIQUE INDEX task_urn ON task(Urn);
CREATE UNIQUE INDEX IdxtaskNameGroup on task(Name,ParentId,GroupId,OrganizationId);
CREATE UNIQUE INDEX IdxtaskparticipationCbo on taskparticipation(ParticipationId,ParticipantId,ParticipantType,AffectId,OrganizationId);
CREATE UNIQUE INDEX work_urn ON work(Urn);
CREATE UNIQUE INDEX IdxworkNameGroup on work(Name,GroupId,OrganizationId);
CREATE UNIQUE INDEX IdxworkparticipationCbo on workparticipation(ParticipationId,ParticipantId,ParticipantType,AffectId,OrganizationId);
CREATE UNIQUE INDEX resource_urn ON resource(Urn);
CREATE UNIQUE INDEX IdxresourceNameGroup on resource(Name,GroupId,OrganizationId);
CREATE UNIQUE INDEX processstep_urn ON processstep(Urn);
CREATE UNIQUE INDEX IdxprocessstepNameGroup on processstep(Name,GroupId,OrganizationId);
CREATE UNIQUE INDEX IdxprocessstepparticipationCbo on processstepparticipation(ParticipationId,ParticipantId,ParticipantType,AffectId,OrganizationId);
CREATE UNIQUE INDEX process_urn ON process(Urn);
CREATE UNIQUE INDEX IdxprocessNameGroup on process(Name,GroupId,OrganizationId);
CREATE UNIQUE INDEX IdxprocessparticipationCbo on processparticipation(ParticipationId,ParticipantId,ParticipantType,AffectId,OrganizationId);
CREATE UNIQUE INDEX methodology_urn ON methodology(Urn);
CREATE UNIQUE INDEX IdxmethodologyNameGroup on methodology(Name,GroupId,OrganizationId);
CREATE UNIQUE INDEX IdxmethodologyparticipationCbo on methodologyparticipation(ParticipationId,ParticipantId,ParticipantType,AffectId,OrganizationId);
CREATE UNIQUE INDEX stage_urn ON stage(Urn);
CREATE UNIQUE INDEX IdxstageNameGroup on stage(Name,GroupId,OrganizationId);
CREATE UNIQUE INDEX module_urn ON module(Urn);
CREATE UNIQUE INDEX IdxmoduleNameGroup on module(Name,GroupId,OrganizationId);
CREATE UNIQUE INDEX IdxmoduleIdGroup on module(Id,GroupId,OrganizationId);
CREATE UNIQUE INDEX IdxmoduleparticipationCbo on moduleparticipation(ParticipationId,ParticipantId,ParticipantType,AffectId,OrganizationId);
CREATE UNIQUE INDEX ticket_urn ON ticket(Urn);
CREATE UNIQUE INDEX IdxticketNameGroup on ticket(Name,GroupId,OrganizationId);
CREATE UNIQUE INDEX IdxticketIdGroup on ticket(Id,GroupId,OrganizationId);
CREATE UNIQUE INDEX IdxticketparticipationCbo on ticketparticipation(ParticipationId,ParticipantId,ParticipantType,AffectId,OrganizationId);
CREATE UNIQUE INDEX IdxformelementvalueNameGroup on formelementvalue(Name,FormElementId,FormId, OrganizationId);
CREATE UNIQUE INDEX IdxformelementvalueIdGroup on formelementvalue(Id,FormElementId,FormId, OrganizationId);
CREATE UNIQUE INDEX formelement_urn ON formelement(Urn);
CREATE UNIQUE INDEX IdxformelementNameGroup on formelement(Name,GroupId,OrganizationId);
CREATE UNIQUE INDEX IdxformelementIdGroup on formelement(Id,GroupId,OrganizationId);
CREATE UNIQUE INDEX IdxformelementparticipationCbo on formelementparticipation(ParticipationId,ParticipantId,ParticipantType,AffectId,OrganizationId);
CREATE UNIQUE INDEX validationrule_urn ON validationrule(Urn);
CREATE UNIQUE INDEX IdxvalidationruleNameGroup on validationrule(Name,GroupId,OrganizationId);
CREATE UNIQUE INDEX IdxvalidationruleIdGroup on validationrule(Id,GroupId,OrganizationId);
CREATE UNIQUE INDEX IdxvalidationruleparticipationCbo on validationruleparticipation(ParticipationId,ParticipantId,ParticipantType,AffectId,OrganizationId);
CREATE UNIQUE INDEX form_urn ON form(Urn);
CREATE UNIQUE INDEX IdxformNameGroup on form(Name,GroupId,OrganizationId);
CREATE UNIQUE INDEX IdxformIdGroup on form(Id,GroupId,OrganizationId);
CREATE UNIQUE INDEX IdxformparticipationCbo on formparticipation(ParticipationId,ParticipantId,ParticipantType,AffectId,OrganizationId);
CREATE UNIQUE INDEX location_urn ON location(Urn);
CREATE UNIQUE INDEX IdxlocationNameGroup on location(Name,ParentId,GroupId,OrganizationId);
CREATE UNIQUE INDEX IdxlocationparticipationCbo on locationparticipation(ParticipationId,ParticipantId,ParticipantType,AffectId,OrganizationId);
CREATE UNIQUE INDEX event_urn ON event(Urn);
CREATE UNIQUE INDEX IdxeventNameGroup on event(Name,ParentId,GroupId,OrganizationId);
CREATE UNIQUE INDEX IdxeventparticipationCbo on eventparticipation(ParticipationId,ParticipantId,ParticipantType,AffectId,OrganizationId);
CREATE UNIQUE INDEX trait_urn ON trait(Urn);
CREATE UNIQUE INDEX IdxtraitNameGroup on trait(Name,GroupId,OrganizationId);

CREATE INDEX IdxlifecycleIdGroup on lifecycle(Id,OrganizationId);
CREATE INDEX lifecycleparticipation_parttype ON lifecycleparticipation(ParticipantId,ParticipantType,AffectId);
CREATE INDEX lifecycleparticipation_pid ON lifecycleparticipation(ParticipationId);
CREATE INDEX IdxscheduleIdGroup on schedule(Id,OrganizationId);
CREATE INDEX scheduleparticipation_parttype ON scheduleparticipation(ParticipantId,ParticipantType,AffectId);
CREATE INDEX scheduleparticipation_pid ON scheduleparticipation(ParticipationId);
CREATE INDEX IdxgoalIdGroup on goal(Id,OrganizationId);
CREATE INDEX goalparticipation_parttype ON goalparticipation(ParticipantId,ParticipantType,AffectId);
CREATE INDEX goalparticipation_pid ON goalparticipation(ParticipationId);
CREATE INDEX IdxcostIdGroup on cost(Id,OrganizationId);
CREATE INDEX IdxtimeIdGroup on time(Id,OrganizationId);
CREATE INDEX timeparticipation_parttype ON timeparticipation(ParticipantId,ParticipantType,AffectId);
CREATE INDEX timeparticipation_pid ON timeparticipation(ParticipationId);
CREATE INDEX IdxbudgetIdGroup on budget(Id,OrganizationId);
CREATE INDEX IdxprojectIdGroup on project(Id,OrganizationId);
CREATE INDEX projectparticipation_parttype ON projectparticipation(ParticipantId,ParticipantType,AffectId);
CREATE INDEX projectparticipation_pid ON projectparticipation(ParticipationId);
CREATE INDEX IdxartifactIdGroup on artifact(Id,GroupId,OrganizationId);
CREATE INDEX IdxrequirementIdGroup on requirement(Id,GroupId,OrganizationId);
CREATE INDEX IdxusecaseIdGroup on usecase(Id,OrganizationId);
CREATE INDEX usecaseparticipation_parttype ON usecaseparticipation(ParticipantId,ParticipantType,AffectId);
CREATE INDEX usecaseparticipation_pid ON usecaseparticipation(ParticipationId);
CREATE INDEX IdxmodelIdGroup on model(Id,OrganizationId);
CREATE INDEX modelparticipation_parttype ON modelparticipation(ParticipantId,ParticipantType,AffectId);
CREATE INDEX modelparticipation_pid ON modelparticipation(ParticipationId);
CREATE INDEX IdxnoteIdGroup on note(Id,OrganizationId);
CREATE INDEX IdxestimateIdGroup on estimate(Id,OrganizationId);
CREATE INDEX IdxtaskIdGroup on task(Id,OrganizationId);
CREATE INDEX IdxtaskParent on task(ParentId,OrganizationId);
CREATE INDEX taskparticipation_parttype ON taskparticipation(ParticipantId,ParticipantType,AffectId);
CREATE INDEX taskparticipation_pid ON taskparticipation(ParticipationId);
CREATE INDEX IdxworkIdGroup on work(Id,OrganizationId);
CREATE INDEX workparticipation_parttype ON workparticipation(ParticipantId,ParticipantType,AffectId);
CREATE INDEX workparticipation_pid ON workparticipation(ParticipationId);
CREATE INDEX IdxresourceIdGroup on resource(Id,OrganizationId);
CREATE INDEX IdxprocessstepIdGroup on processstep(Id,OrganizationId);
CREATE INDEX processstepparticipation_parttype ON processstepparticipation(ParticipantId,ParticipantType,AffectId);
CREATE INDEX processstepparticipation_pid ON processstepparticipation(ParticipationId);
CREATE INDEX IdxprocessIdGroup on process(Id,OrganizationId);
CREATE INDEX processparticipation_parttype ON processparticipation(ParticipantId,ParticipantType,AffectId);
CREATE INDEX processparticipation_pid ON processparticipation(ParticipationId);
CREATE INDEX IdxmethodologyIdGroup on methodology(Id,OrganizationId);
CREATE INDEX methodologyparticipation_parttype ON methodologyparticipation(ParticipantId,ParticipantType,AffectId);
CREATE INDEX methodologyparticipation_pid ON methodologyparticipation(ParticipationId);
CREATE INDEX IdxstageIdGroup on stage(Id,OrganizationId);
CREATE INDEX module_Name ON module(Name);
CREATE INDEX moduleparticipation_parttype ON moduleparticipation(ParticipantId,ParticipantType,AffectId);
CREATE INDEX moduleparticipation_pid ON moduleparticipation(ParticipationId);
CREATE INDEX ticket_Name ON ticket(Name);
CREATE INDEX ticketparticipation_parttype ON ticketparticipation(ParticipantId,ParticipantType,AffectId);
CREATE INDEX ticketparticipation_pid ON ticketparticipation(ParticipationId);
CREATE INDEX formelementvalue_Name ON formelementvalue(Name);
CREATE INDEX formelement_Name ON formelement(Name);
CREATE INDEX formelementparticipation_parttype ON formelementparticipation(ParticipantId,ParticipantType,AffectId);
CREATE INDEX formelementparticipation_pid ON formelementparticipation(ParticipationId);
CREATE INDEX validationrule_Name ON validationrule(Name);
CREATE INDEX validationruleparticipation_parttype ON validationruleparticipation(ParticipantId,ParticipantType,AffectId);
CREATE INDEX validationruleparticipation_pid ON validationruleparticipation(ParticipationId);
CREATE INDEX form_Name ON form(Name);
CREATE INDEX formparticipation_parttype ON formparticipation(ParticipantId,ParticipantType,AffectId);
CREATE INDEX formparticipation_pid ON formparticipation(ParticipationId);
CREATE INDEX locationparticipation_parttype ON locationparticipation(ParticipantId,ParticipantType,AffectId);
CREATE INDEX locationparticipation_pid ON locationparticipation(ParticipationId);
CREATE INDEX eventparticipation_parttype ON eventparticipation(ParticipantId,ParticipantType,AffectId);
CREATE INDEX eventparticipation_pid ON eventparticipation(ParticipationId);


