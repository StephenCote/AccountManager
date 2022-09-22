-- CREATE EXTENSION IF NOT EXISTS "uuid-ossp";
-- CREATE EXTENSION IF NOT EXISTS pg_stat_statements;
-- select uuid_generate_v4();

DROP TABLE IF EXISTS orgid CASCADE;
DROP SEQUENCE IF EXISTS orgid_id_seq;
CREATE SEQUENCE orgid_id_seq;
CREATE TABLE orgid(
	Id bigint not null default nextval('orgid_id_seq'),
	OwnerId bigint not null default 0,
	OrganizationId bigint not null default 0,
	primary key(Id)
);
-- CREATE INDEX idxorgidowner on orgid(OwnerId);

DROP TABLE IF EXISTS objorgid CASCADE;
CREATE TABLE objorgid(
	ObjectId varchar(64) default uuid_generate_v4()
) inherits (orgid);
-- CREATE UNIQUE INDEX idxnobjorgidobjectid on objorgid(ObjectId);

DROP TABLE IF EXISTS nameid CASCADE;
CREATE TABLE nameid (
	Name varchar(512) not null,
	ParentId bigint not null default 0,
	Urn text not null
) inherits (objorgid);
-- CREATE UNIQUE INDEX idxnameidurn on nameid(Urn);

DROP TABLE IF EXISTS uniquenameid CASCADE;
CREATE TABLE uniquenameid(
) inherits (nameid);
--CREATE UNIQUE INDEX idxnameid on uniquenameid(Name,OrganizationId);

DROP TABLE IF EXISTS objectreference;
CREATE TABLE objectreference(
	ReferenceId bigint not null default 0,
	ReferenceType varchar(64)
);
-- CREATE INDEX idxlogicalrefid ON objectreference(ReferenceId,ReferenceType);

DROP TABLE IF EXISTS logicalnameid CASCADE;
CREATE TABLE logicalnameid (
	LogicalId bigint not null default 0
) inherits (nameid,objectreference);
--CREATE INDEX idxlogicalrefid ON logicalnameid(ReferenceId,ReferenceType,OrganizationId);

DROP TABLE IF EXISTS organizations CASCADE;
CREATE TABLE organizations (
	OrganizationType varchar(16) not null
) inherits (logicalnameid);

DROP TABLE IF EXISTS vaultkey CASCADE;
CREATE TABLE vaultkey(
	KeyId varchar(64),
	VaultId text,
	IsVaulted boolean not null default false,
	IsEnciphered boolean not null default false
);

DROP TABLE IF EXISTS attribute CASCADE;
CREATE TABLE attribute (
	Name varchar(512) not null,
	DataType varchar(32) not null,
	ValueIndex int not null default 0,
	Value text not null

	) inherits (orgid,vaultkey,objectreference);

-- CREATE UNIQUE INDEX Idxattributes on attribute(ReferenceId,ReferenceType,Name,ValueIndex,OrganizationId);
-- OPTIONAL value index, for when performing broader queries based on attribute value

DROP TABLE IF EXISTS objectlocation CASCADE;
create table objectlocation(
	LocationType varchar(16)
);

DROP TABLE IF EXISTS objectdescription CASCADE;
create table objectdescription(
	Description varchar(255)
);
DROP TABLE IF EXISTS objectorderscore;
create table objectorderscore(
	LogicalOrder int not null default 0,
	Score int not null default 0
);
DROP TABLE IF EXISTS asymmetrickeys CASCADE;
CREATE TABLE asymmetrickeys (
	OrganizationKey boolean not null default false,
	CipherProvider varchar(32) not null,
	CipherKeySpec varchar(32) not null,
	AsymmetricCipherKeySpec varchar(32) not null,
	HashProvider varchar(32) not null,
	SeedLength bigint not null default 0,
	GlobalKey boolean not null default false,
	PrimaryKey boolean not null default false,
	PreviousKeyId bigint not null default 0,
	CurveName varchar(32),
	KeyAgreementSpec varchar(32),
	PublicKey bytea,
	PrivateKey bytea,
	SymmetricKeyId bigint not null default 0
) inherits (objorgid,vaultkey);


DROP TABLE IF EXISTS symmetrickeys CASCADE;
CREATE TABLE symmetrickeys (
	OrganizationKey boolean not null default false,
	CipherProvider varchar(32) not null,
	CipherKeySpec varchar(32) not null,
	SymmetricCipherKeySpec varchar(32) not null,
	HashProvider varchar(32) not null,
	SeedLength bigint not null default 0,
	GlobalKey boolean not null default false,
	PrimaryKey boolean not null default false,
	EncryptedKey boolean not null default false,
	PreviousKeyId bigint not null default 0,
	CurveName varchar(32),
	CipherKey bytea,
	CipherIV bytea,
	AsymmetricKeyId bigint not null default 0
) inherits (objorgid,vaultkey);

DROP TABLE IF EXISTS uniqueparent CASCADE;
CREATE TABLE uniqueparent (
) inherits (nameid);
-- CREATE UNIQUE INDEX idxuniqueparentname on uniqueparent(Name,ParentId,OrganizationId);

DROP TABLE IF EXISTS groups CASCADE;
CREATE TABLE groups (
	GroupType varchar(16) not null,
	-- TODO: Remove
	ReferenceId bigint not null default 0
	) inherits (uniqueparent);


DROP TABLE IF EXISTS participation CASCADE;
DROP SEQUENCE IF EXISTS participation_id_seq;
CREATE SEQUENCE participation_id_seq;
CREATE TABLE participation (
	ParticipationId bigint not null default 0,
	ParticipantType varchar(16) not null,
	ParticipantId bigint not null default 0,
	AffectType varchar(16) not null default 'UNKNOWN',
	AffectId bigint not null default 0

) inherits (orgid);

-- CREATE INDEX participationtype_pid ON participation(ParticipationId);
-- CREATE INDEX participanttype_pid ON participation(ParticipantId,ParticipantType);

DROP TABLE IF EXISTS groupparticipation CASCADE;
CREATE TABLE groupparticipation (
) inherits (participation);

DROP TABLE IF EXISTS dataparticipation CASCADE;
CREATE TABLE dataparticipation (
) inherits (participation);

DROP TABLE IF EXISTS rolecache CASCADE;
CREATE TABLE rolecache (
	ObjectId bigint not null default 0,
	AffectType varchar(16) not null default 'UNKNOWN',
	AffectId bigint not null default 0,
	EffectiveRoleId bigint not null default 0,
	BaseRoleId bigint not null default 0,
	OrganizationId bigint not null default 0
);
CREATE INDEX rolecache_aff_id ON rolecache(AffectType,AffectId);
CREATE INDEX rolecache_dorg ON rolecache(ObjectId,OrganizationId);

DROP TABLE IF EXISTS grouprolecache CASCADE;
CREATE TABLE grouprolecache (

) inherits (rolecache);

DROP TABLE IF EXISTS namegroup CASCADE;
CREATE TABLE namegroup (
   GroupId bigint not null
) inherits (nameid);

DROP TABLE IF EXISTS uniquenamegroup CASCADE;
CREATE TABLE uniquenamegroup (

) inherits (namegroup);
-- CREATE UNIQUE INDEX idxuniquenamegroup on uniquenamegroup(Name,GroupId,OrganizationId);

DROP TABLE IF EXISTS uniquenameparentgroup CASCADE;
CREATE TABLE uniquenameparentgroup (

) inherits (uniquenamegroup);
-- CREATE UNIQUE INDEX idxuniquenameparentgroup on uniquenameparentgroup(Name,ParentId,GroupId,OrganizationId);

DROP TABLE IF EXISTS objectdate CASCADE;
create table objectdate (
	CreatedDate timestamp not null default now(),
	ModifiedDate timestamp not null default now(),
	ExpirationDate timestamp not null default now()
);

DROP TABLE IF EXISTS data CASCADE;
create table data (
	MimeType varchar(127) not null,
	IsPasswordProtected boolean not null default false,
	IsCompressed boolean not null default false,
	CompressionType varchar(16) not null,
	Dimensions varchar(9),	
	Size int not null default 0,
	Rating double precision not null default 0,
	IsPointer boolean not null default false,
	Hash varchar(128),
	IsBlob boolean not null,
	DataBlob bytea,
	DataString varchar(255)

) inherits (uniquenamegroup,vaultkey,objectdate,objectdescription);

DROP TABLE IF EXISTS datarolecache CASCADE;
CREATE TABLE datarolecache (
) inherits (rolecache);

DROP TABLE IF EXISTS accounts CASCADE;
create table accounts (
	AccountId varchar(64) not null,
	AccountStatus varchar(16) not null,
	AccountType varchar(16) not null,
	-- TODO: Remove
	ReferenceId bigint not null default 0
) inherits (uniquenameparentgroup);


DROP TABLE IF EXISTS accountrolecache CASCADE;
CREATE TABLE accountrolecache (
) inherits (rolecache);


DROP TABLE IF EXISTS users CASCADE;
create table users (
	AccountId bigint not null default 0,
	UserId varchar(64) not null,
	UserStatus varchar(16) not null,
	UserType varchar(16) not null

) inherits (uniquenameid);

DROP TABLE IF EXISTS userrolecache CASCADE;
CREATE TABLE userrolecache (
) inherits (rolecache);

DROP TABLE IF EXISTS statistics CASCADE;
create table statistics (
	ReferenceId bigint not null,
	StatisticsType varchar(16) not null,
	AccessedDate timestamp not null
) inherits (orgid,objectdate);
CREATE UNIQUE INDEX IdxstatisticsRefOrg on statistics(ReferenceId,StatisticsType,OrganizationId);
CREATE INDEX idxstatisticsidorg on statistics(id,organizationid);

DROP TABLE IF EXISTS addresses CASCADE;
create table addresses (
	Preferred boolean not null default false,
	AddressLine1 varchar(255),
	AddressLine2 varchar(255),
	City varchar(255),
	State varchar(255),
	Region varchar(255),
	PostalCode varchar(255),
	Country varchar(255)
) inherits (namegroup,objectlocation,objectdescription);
CREATE UNIQUE INDEX addresses_reftype ON addresses(Name,LocationType,GroupId,OrganizationId);


DROP TABLE IF EXISTS contacts CASCADE;
create table contacts (
	Preferred boolean not null default false,
	ContactType varchar(16) not null,
	ContactValue text
) inherits (namegroup,objectlocation,objectdescription);
CREATE UNIQUE INDEX contacts_reftype ON contacts(Name,ContactType,LocationType,GroupId,OrganizationId);

DROP TABLE IF EXISTS contactinformation CASCADE;
create table contactinformation (
	ReferenceId bigint not null,
	ContactInformationType varchar(16) not null
) inherits (objorgid,objectdescription);
CREATE UNIQUE INDEX contactinformation_reftype ON contactinformation(ReferenceId,ContactInformationType,OrganizationId);

DROP TABLE IF EXISTS contactinformationparticipation CASCADE;
CREATE TABLE contactinformationparticipation (

) inherits (participation);

DROP TABLE IF EXISTS persons CASCADE;
create table persons (
	ContactInformationId bigint not null default 0,
	Title varchar(128),
	Prefix varchar(64),
	Suffix varchar(64),
	FirstName varchar(128),
	MiddleName varchar(128),
	LastName varchar(128),
	Alias varchar(64),
	BirthDate timestamp not null,
	Gender varchar(16) default 'UNKNOWN'
) inherits (uniquenameparentgroup,objectdescription);

DROP TABLE IF EXISTS personrolecache CASCADE;
CREATE TABLE personrolecache (

) inherits (rolecache);


DROP TABLE IF EXISTS personparticipation CASCADE;
CREATE TABLE personparticipation (
) inherits (participation);





DROP TABLE IF EXISTS roles CASCADE;
create table roles (
	RoleType varchar(16) not null,
	ReferenceId bigint not null default 0
) inherits (nameid);
CREATE UNIQUE INDEX roles_name ON roles(Name,RoleType,ParentId,OrganizationId);

DROP TABLE IF EXISTS roleparticipation CASCADE;
CREATE TABLE roleparticipation (
) inherits (participation);

DROP TABLE IF EXISTS rolerolecache CASCADE;
CREATE TABLE rolerolecache (
) inherits (rolecache);

DROP TABLE IF EXISTS permissions CASCADE;
create table permissions (
	PermissionType varchar(16) not null,
	ReferenceId bigint not null default 0
) inherits (nameid);
CREATE UNIQUE INDEX IdxpermissionsName on permissions(Name,PermissionType,ParentId,OrganizationId);

DROP TABLE IF EXISTS permissionparticipation CASCADE;
CREATE TABLE permissionparticipation (
) inherits (participation);

DROP TABLE IF EXISTS permissionrolecache CASCADE;
CREATE TABLE permissionrolecache (
) inherits (rolecache);


DROP TABLE IF EXISTS tags CASCADE;
CREATE TABLE tags (
	TagType varchar(16) not null
) inherits (namegroup);
CREATE UNIQUE INDEX IdxtagsName on tags(Name,TagType,GroupId,OrganizationId);

DROP TABLE IF EXISTS tagparticipation CASCADE;
CREATE TABLE tagparticipation (
) inherits (participation);

DROP TABLE IF EXISTS spool CASCADE;
CREATE TABLE spool (
	ObjectId varchar(64) not null,
	ParentObjectId varchar(64),
	SpoolBucketName varchar(64) not null,
	SpoolBucketType varchar(16) not null,
	SpoolValueType varchar(32) not null,
	SpoolData bytea,
	SpoolStatus varchar(32) not null,
	Expires boolean not null default false,
	Classification varchar(64),
	CurrentLevel int not null default 0,
	EndLevel int not null default 0,
	ReferenceId bigint not null default 0,
	ReferenceType varchar(64) not null,
	RecipientId bigint not null default 0,
	RecipientType varchar(64) not null,
	SenderId bigint not null default 0,
	SenderType varchar(64) not null,
	TransportId bigint not null default 0,
	TransportType varchar(64) not null,
	CredentialId bigint not null default 0,
	Name varchar(511) not null,
	GroupId bigint not null default 0
) inherits (orgid,objectdate);
CREATE UNIQUE INDEX spool_spool_objectid ON spool(ObjectId);
CREATE INDEX spool_spool_expiry ON spool(Expires,ExpirationDate);
CREATE INDEX spool_spool_group ON spool(GroupId,OrganizationId);
CREATE INDEX spool_spool_bucknametype ON spool(SpoolBucketName,SpoolBucketType,OrganizationId);

DROP TABLE IF EXISTS session CASCADE;
CREATE TABLE session (
	UserId bigint not null default 0,
	SessionId varchar(64) not null,
	SecurityId varchar(64),
	SessionCreated timestamp not null,
	SessionExpiration timestamp not null,
	SessionAccessed timestamp not null,
	SessionStatus varchar(32) not null,
	SessionDataSize bigint not null default 0,
	OrganizationId bigint not null default 0
);
CREATE INDEX session_user_id ON session(UserId);
CREATE INDEX session_sesid ON session(SessionId);
CREATE UNIQUE INDEX session_sesorgid ON session(SessionId,OrganizationId);

DROP TABLE IF EXISTS sessiondata CASCADE;
CREATE TABLE sessiondata (
	UserId bigint not null default 0,
	SessionId varchar(64) not null,
	Expiration timestamp not null,
	Name varchar(124) not null,
	Data varchar(255),
	OrganizationId bigint not null default 0
);
CREATE INDEX sessiondata_user_id ON sessiondata(UserId);
CREATE INDEX sessiondata_sess_id ON sessiondata(SessionId);
CREATE INDEX sessiondata_name ON sessiondata(SessionId,Name);

DROP TABLE IF EXISTS audit CASCADE;
DROP SEQUENCE IF EXISTS audit_id_seq;
CREATE SEQUENCE audit_id_seq;
CREATE TABLE audit (
	Id bigint not null default nextval('audit_id_seq'),
	AuditDate timestamp not null,
    AuditResultDate timestamp not null,
	AuditLevelType varchar(32) not null,
	AuditExpiresDate timestamp not null,
	AuditRetentionType varchar(32) not null,
	AuditSourceType varchar(32) not null,
	AuditTargetType varchar(32) not null,
	AuditActionType varchar(32) not null,
	AuditResultType varchar(32) not null,
	AuditSourceData text,
	AuditTargetData text,
	AuditResultData text,
	AuditActionSource text
);

CREATE INDEX audit_source ON audit(AuditSourceType,AuditSourceData);
CREATE INDEX audit_target ON audit(AuditTargetType,AuditTargetData);
CREATE INDEX audit_retention ON audit(AuditRetentionType);
CREATE INDEX audit_exptype on audit(AuditExpiresDate,AuditRetentionType);

DROP TABLE IF EXISTS fact CASCADE;
create table fact (
	FactType varchar(64) not null,
	SourceType varchar(255),
	SourceUrn text,
	SourceUrl varchar(2047),
	SourceDataType varchar(64),
	FactData varchar(255),
	FactoryType varchar(64) not null

) inherits (uniquenamegroup,objectdescription,objectorderscore);


DROP TABLE IF EXISTS functionfact CASCADE;
create table functionfact (
	FunctionUrn text,
	FactUrn text
) inherits (uniquenamegroup,objectdescription,objectorderscore);


DROP TABLE IF EXISTS function CASCADE;
create table function (
	FunctionType varchar(64) not null,
	SourceUrn text,
	SourceUrl text
) inherits (uniquenamegroup,objectdescription,objectorderscore);


DROP TABLE IF EXISTS functionparticipation CASCADE;
CREATE TABLE functionparticipation (
) inherits (participation);


DROP TABLE IF EXISTS operation CASCADE;
create table operation (
	OperationType varchar(64) not null,
	Operation text
) inherits (uniquenamegroup,objectdescription,objectorderscore);


DROP TABLE IF EXISTS pattern CASCADE;
create table pattern (
	FactUrn text,
	Comparator varchar(32),
	PatternType varchar(64) not null,
	MatchUrn text,
	OperationUrn text
) inherits (uniquenamegroup,objectdescription,objectorderscore);


DROP TABLE IF EXISTS policy CASCADE;
create table policy (
	Enabled boolean not null default false,
	DecisionAge bigint not null default 0,
	Condition varchar(64) not null
) inherits (uniquenamegroup,objectdescription,objectorderscore,objectdate);

DROP TABLE IF EXISTS policyparticipation CASCADE;
CREATE TABLE policyparticipation (
) inherits (participation);

DROP TABLE IF EXISTS rule CASCADE;
create table rule (
	RuleType varchar(64) not null,
	Condition varchar(64) not null

) inherits (uniquenamegroup,objectdescription,objectorderscore);

DROP TABLE IF EXISTS ruleparticipation CASCADE;
CREATE TABLE ruleparticipation (
) inherits (participation);



DROP TABLE IF EXISTS credential CASCADE;

create table credential (
	HashProvider varchar(32) not null,
	Credential bytea,
	Salt bytea,
	PrimaryCredential boolean not null default false,
	PreviousCredentialId bigint not null default 0,
	NextCredentialId bigint not null default 0,
	CredentialType varchar(32) not null

) inherits(objorgid,objectdate,vaultkey,objectreference);

DROP TABLE IF EXISTS control CASCADE;

create table control (
	ControlType varchar(64) not null,
	ControlAction varchar(32) not null,
	ControlId bigint not null default 0
) inherits(objorgid,objectdate,objectreference);


create table approver (
	ApproverType varchar(64) not null,
	ApproverId bigint not null default 0,
	ApproverLevel int not null default 0,
	EntitlementType varchar(64) not null,
	EntitlementId bigint not null default 0,
	ApprovalType varchar(64) not null
) inherits(objorgid,objectreference);

--- ParentId included directly versus inheritence since the additional name and urn fields aren't needed here
create table request (
	ParentId bigint not null default 0,
	RequestorType varchar(64) not null,
	RequestorId bigint not null default 0,
	DelegateType varchar(64) not null,
	DelegateId bigint not null default 0,
	EntitlementType varchar(64) not null,
	EntitlementId bigint not null default 0,
	ActionType varchar(64) not null,
	ApprovalStatus varchar(64)
) inherits(objorgid,objectdate,objectdescription,objectreference);

create table approval (
	Response varchar(64) not null,
	ResponseMessage varchar(255),
	SignerId varchar(64),
	ValidationId varchar(64),
	ApprovalId varchar(64),
	RequestId varchar(64),
	Signature bytea,
	ApprovalType varchar(64) not null,
	ApproverType varchar(64) not null,
	ApproverId bigint not null default 0,
	ApproverLevel int not null default 0
) inherits(objorgid,objectdate,objectdescription,objectreference);

-- REWORK

CREATE OR REPLACE VIEW duplicateData as
select D2.hashCount, D.id, D.urn, D.name, D.hash, D.groupid from Data D
inner join (select count(hash) hashCount, hash from Data
group by hash
having count(hash) > 1
order by count(hash)
) D2 on D2.hash = D.hash
order by D.hash, D.name;

create or replace view permissionPersonRights as
select U.id as personid,P.id as permissionid, P.name as PermissionName2, P.ownerid as permissionownerid,P.organizationid,
	P2.name as permissionname,
        RP.affecttype,RP.affectid
	from Permissions P
	join permissionparticipation RP on RP.participationid = P.id
	join Persons U on RP.participantid = U.id
	join permissions P2 on RP.affectid = P2.id
	where
	RP.id > 0
	 AND RP.participanttype = 'PERSON'
	 AND RP.affectid > 0;


create or replace view rolePersonRights as
select U.id as personid,R.id as roleid, R.name as RoleName, R.ownerid as roleownerid,R.organizationid,
	P.name as permissionname,
        RP.affecttype,RP.affectid
	from Roles R
	join roleparticipation RP on RP.participationid = R.id
	join Persons U on RP.participantid = U.id
	join permissions P on RP.affectid = P.id
	where
	RP.id > 0
	 AND RP.participanttype = 'PERSON'
	 AND RP.affectid > 0;

create or replace view groupPersonRights as
select  U.id as personid,G.id as groupid, G.name as GroupName, G.ownerid as groupownerid,G.grouptype,G.parentId,G.organizationid,
	P.name as permissionname,
        GP.affecttype,GP.affectid
	from groups G
	join groupparticipation GP on GP.participationid = G.id
	join Persons U on GP.participantid = U.id
	join permissions P on GP.affectid = P.id
	where
	GP.id > 0
	 AND GP.participanttype = 'PERSON'
	 AND GP.affectid > 0
;

create or replace view dataPersonRights as
select U.id as personid,D.id as dataid, D.name as DataName, D.ownerid as dataownerid,G.organizationid,
	P.name as permissionname,
        DP.affecttype,DP.affectid
	from Data D
	join Groups G on G.id = D.groupid
	join dataparticipation DP on DP.participationid = D.id
	join Persons U on DP.participantid = U.id
	join permissions P on DP.affectid = P.id
	where
	DP.id > 0
	 AND DP.participanttype = 'PERSON'
	 AND DP.affectid > 0;


create or replace view permissionUserRights as
select U.id as userid,P.id as permissionid, P.name as PermissionName2, P.ownerid as permissionownerid,P.organizationid,
	P2.name as permissionname,
        RP.affecttype,RP.affectid
	from Permissions P
	join permissionparticipation RP on RP.participationid = P.id
	join users U on RP.participantid = U.id
	join permissions P2 on RP.affectid = P2.id
	where
	RP.id > 0
	 AND RP.participanttype = 'USER'
	 AND RP.affectid > 0;


create or replace view roleUserRights as
select U.id as userid,R.id as roleid, R.name as RoleName, R.ownerid as roleownerid,R.organizationid,
	P.name as permissionname,
        RP.affecttype,RP.affectid
	from Roles R
	join roleparticipation RP on RP.participationid = R.id
	join users U on RP.participantid = U.id
	join permissions P on RP.affectid = P.id
	where
	RP.id > 0
	 AND RP.participanttype = 'USER'
	 AND RP.affectid > 0;

create or replace view groupUserRights as
select U.id as userid,G.id as groupid, G.name as GroupName, G.ownerid as groupownerid,G.grouptype,G.parentId,G.organizationid,
	P.name as permissionname,
        GP.affecttype,GP.affectid
	from groups G
	join groupparticipation GP on GP.participationid = G.id
	join users U on GP.participantid = U.id
	join permissions P on GP.affectid = P.id
	where
	GP.id > 0
	 AND GP.participanttype = 'USER'
	 AND GP.affectid > 0
;

create or replace view dataUserRights as
select U.id as userid,D.id as dataid, D.name as DataName, D.ownerid as dataownerid,G.organizationid,
	P.name as permissionname,
        DP.affecttype,DP.affectid
	from Data D
	join Groups G on G.id = D.groupid
	join dataparticipation DP on DP.participationid = D.id
	join users U on DP.participantid = U.id
	join permissions P on DP.affectid = P.id
	where
	DP.id > 0
	 AND DP.participanttype = 'USER'
	 AND DP.affectid > 0;

create or replace view permissionAccountRights as
select U.id as accountid,P.id as permissionid, P.name as PermissionName2, P.ownerid as permissionownerid,P.organizationid,
	P2.name as permissionname,
        RP.affecttype,RP.affectid
	from Permissions P
	join permissionparticipation RP on RP.participationid = P.id
	join accounts U on RP.participantid = U.id
	join permissions P2 on RP.affectid = P2.id
	where
	RP.id > 0
	 AND RP.participanttype = 'ACCOUNT'
	 AND RP.affectid > 0;


create or replace view roleAccountRights as
select U.id as accountid,R.id as roleid, R.name as RoleName, R.ownerid as roleownerid,R.organizationid,
	P.name as permissionname,
        RP.affecttype,RP.affectid
	from Roles R
	join roleparticipation RP on RP.participationid = R.id
	join accounts U on RP.participantid = U.id
	join permissions P on RP.affectid = P.id
	where
	RP.id > 0
	 AND RP.participanttype = 'ACCOUNT'
	 AND RP.affectid > 0;

create or replace view groupAccountRights as
select U.id as accountid,G.id as groupid, G.name as GroupName, G.ownerid as groupownerid,G.grouptype,G.parentId,G.organizationid,
	P.name as permissionname,
        GP.affecttype,GP.affectid
	from groups G
	join groupparticipation GP on GP.participationid = G.id
	join accounts U on GP.participantid = U.id
	join permissions P on GP.affectid = P.id
	where
	GP.id > 0
	 AND GP.participanttype = 'ACCOUNT'
	 AND GP.affectid > 0
;

create or replace view dataAccountRights as
select U.id as accountid,D.id as dataid, D.name as DataName, D.ownerid as dataownerid,G.organizationid,
	P.name as permissionname,
        DP.affecttype,DP.affectid
	from Data D
	join Groups G on G.id = D.groupid
	join dataparticipation DP on DP.participationid = D.id
	join accounts U on DP.participantid = U.id
	join permissions P on DP.affectid = P.id
	where
	DP.id > 0
	 AND DP.participanttype = 'ACCOUNT'
	 AND DP.affectid > 0;


--- Create a role path starting from the specified root and moving up
--- Note: The views must reference roles -> roleid and participation -> leafid
---
CREATE OR REPLACE FUNCTION roles_from_leaf(IN root_id bigint)
	RETURNS TABLE(leafid bigint, roleid bigint, parentid bigint, organizationid bigint)
	AS $$
	WITH RECURSIVE role_tree(leafid,roleid, parentid, organizationid) AS (
	   SELECT $1 as leafid, R.id as roleid, R.parentid, R.organizationid
	      FROM roles R WHERE R.id = $1
	   UNION ALL
	   SELECT $1 as leafid, P.id, P.parentid, P.organizationid
	      FROM role_tree RT, roles P
	      WHERE RT.roleid = P.parentid
	)
	select * from role_tree;
	$$ LANGUAGE 'sql';

CREATE OR REPLACE FUNCTION groups_from_branch(IN root_id bigint)
	RETURNS TABLE(branchid bigint, groupid bigint, parentid bigint, organizationid bigint)
	AS $$
	WITH RECURSIVE group_tree(branchid,groupid, parentid, organizationid) AS (
	   SELECT $1 as branchid, R.id as groupid, R.parentid, R.organizationid
	      FROM groups R WHERE R.id = $1
	   UNION ALL
	   SELECT $1 as branchid, P.id, P.parentid, P.organizationid
	      FROM group_tree RT, groups P
	      WHERE RT.groupid = P.parentid
	)
	select * from group_tree;
	$$ LANGUAGE 'sql';

-- similar function, but counting up the levels for use with hierarchical displays
CREATE OR REPLACE FUNCTION leveled_roles_from_leaf(IN root_id bigint)
	RETURNS TABLE(level bigint,leafid bigint, roleid bigint, parentid bigint, organizationid bigint)
	AS $$
	WITH RECURSIVE role_tree(level,leafid,roleid, parentid, organizationid) AS (
	   SELECT CAST(1 AS bigint) as level,$1 as leafid, R.id as roleid, R.parentid, R.organizationid
	      FROM roles R WHERE R.id = $1
	   UNION ALL
	   SELECT CAST((RT.level + 1) AS bigint) as level,$1 as leafid, P.id, P.parentid, P.organizationid
	      FROM role_tree RT, roles P
	      WHERE RT.roleid = P.parentid
	)
	select * from role_tree;
	$$ LANGUAGE 'sql';

-- similar function, but counting up the levels for use with hierarchical displays
CREATE OR REPLACE FUNCTION leveled_roles_from_leaf(root_id BIGINT,organizationid BIGINT) 
        RETURNS TABLE (level bigint,leafid BIGINT,roleid BIGINT, parentid BIGINT, organizationid BIGINT)
        AS $$
	WITH RECURSIVE role_tree(level,leafid,roleid, parentid, organizationid) AS (
	   SELECT CAST(1 as bigint) as level,$1 as leafid, R.id as roleid, R.parentid, R.organizationid
	      FROM roles R WHERE R.id = $1 AND R.organizationid = $2
	   UNION ALL
	   SELECT CAST((RT.level+1) as bigint) as level,$1 as leafid, P.id, P.parentid, P.organizationid
	      FROM role_tree RT, roles P
	      WHERE RT.roleid = P.parentid AND RT.organizationid = $2
	)
	select * from role_tree;
        $$ LANGUAGE 'sql';

--- Create a role tree starting from the specified root and moving down to each leaf
--- Note: The views must reference roles -> roleid and participation -> leafid
CREATE OR REPLACE FUNCTION roles_to_leaf(root_id BIGINT) 
        RETURNS TABLE (leafid BIGINT,roleid BIGINT, parentid BIGINT, organizationid BIGINT)
        AS $$
	WITH RECURSIVE role_tree(leafid,roleid, parentid, organizationid) AS (
	   SELECT $1 as leafid, R.id as roleid, R.parentid, R.organizationid
	      FROM roles R WHERE R.id = $1
	   UNION ALL
	   SELECT $1 as leafid, P.id, P.parentid, P.organizationid
	      FROM role_tree RT, roles P
	      WHERE RT.parentid = P.id
	)
	select * from role_tree;
        $$ LANGUAGE 'sql';



CREATE OR REPLACE FUNCTION role_membership_refid(IN root_id bigint, IN reftype text, in refid bigint)
	RETURNS TABLE(pid bigint,branchid bigint, roleid bigint, parentid bigint, referencetype text,referenceid bigint,organizationid bigint)
	AS $$
	WITH RECURSIVE role_membership(pid,branchid,roleid, parentid, referencetype,referenceid,organizationid) AS MATERIALIZED (
	   SELECT CAST(0 as bigint),$1 as branchid, R.id as roleid, R.parentid, CASE WHEN P.participanttype <> '' THEN P.participanttype ELSE CAST('' as text) END, CASE WHEN P.participantid > 0 THEN P.participantid ELSE CAST(0 as bigint) END,R.organizationid
	      FROM roles R
		  LEFT JOIN roleparticipation P ON P.participationid = R.id AND ($2 = '' OR participanttype = 'ROLE' OR (participanttype = $2 AND participantid = $3))
		  
	      WHERE R.id = $1
	   UNION
	   SELECT P.id as pid,$1 as branchid, P.participationid, RT.roleid, P.participanttype as referencetype,P.participantid as referenceid,P.organizationid
	      FROM role_membership RT, roleparticipation P
	      WHERE RT.parentid = P.participationid  AND ($2 = '' OR participanttype = 'ROLE' OR (participanttype = $2 AND participantid = $3))
	      --and R.participanttype = 'ROLE'
	)
	select * from role_membership;
	$$ LANGUAGE 'sql';


CREATE OR REPLACE FUNCTION group_membership_refid(IN root_id bigint, IN reftype text, in refid bigint)
	RETURNS TABLE(pid bigint,branchid bigint, groupid bigint, parentid bigint, referencetype text,referenceid bigint, organizationid bigint)
	AS $$
	WITH RECURSIVE group_membership(pid,branchid,groupid, parentid, referencetype,referenceid, organizationid) AS MATERIALIZED (
	   SELECT CAST(0 as bigint),$1 as branchid, G.id as groupid, G.parentid, CASE WHEN P.participanttype <> '' THEN P.participanttype ELSE CAST('' as text) END, CASE WHEN P.participantid > 0 THEN P.participantid ELSE CAST(0 as bigint) END,G.organizationid
	      FROM groups G
		LEFT JOIN groupparticipation P on P.participationid = G.id  AND ($2 = '' OR participanttype = 'GROUP' OR (participanttype = $2 AND participantid = $3))
	      WHERE G.id = $1
	   UNION
	   SELECT P.id as pid,$1 as branchid, P.participationid, RT.groupid, P.participanttype as referencetype,P.participantid as referenceid, P.organizationid
	      FROM group_membership RT, groupparticipation P
	      WHERE RT.groupid = P.participationid AND ($2 = ''  OR participanttype = 'GROUP' OR (participanttype = $2 AND participantid = $3))
	)
	select * from group_membership;
	$$ LANGUAGE 'sql';

CREATE OR REPLACE FUNCTION role_membership(IN root_id bigint)
	RETURNS TABLE(pid bigint,branchid bigint, roleid bigint, parentid bigint, referencetype text,referenceid bigint,organizationid bigint)
	AS $$
	WITH RECURSIVE role_membership(pid,branchid,roleid, parentid, referencetype,referenceid,organizationid) AS MATERIALIZED (
	   SELECT CAST(0 as bigint),$1 as branchid, R.id as roleid, R.parentid, CASE WHEN P.participanttype <> '' THEN P.participanttype ELSE CAST('' as text) END, CASE WHEN P.participantid > 0 THEN P.participantid ELSE CAST(0 as bigint) END,R.organizationid
	      FROM roles R
		  LEFT JOIN roleparticipation P ON P.participationid = R.id
		  
	      WHERE R.id = $1
	   UNION
	   SELECT P.id as pid,$1 as branchid, P.participationid, RT.roleid, P.participanttype as referencetype,P.participantid as referenceid,P.organizationid
	      FROM role_membership RT, roleparticipation P
	      WHERE RT.parentid = P.participationid
	      --and R.participanttype = 'ROLE'
	)
	select * from role_membership;
	$$ LANGUAGE 'sql';

CREATE OR REPLACE FUNCTION group_membership(IN root_id bigint)
	RETURNS TABLE(pid bigint,branchid bigint, groupid bigint, parentid bigint, referencetype text,referenceid bigint, organizationid bigint)
	AS $$
	WITH RECURSIVE group_membership(pid,branchid,groupid, parentid, referencetype,referenceid, organizationid) AS MATERIALIZED (
	   SELECT CAST(0 as bigint),$1 as branchid, G.id as groupid, G.parentid, CASE WHEN P.participanttype <> '' THEN P.participanttype ELSE CAST('' as text) END, CASE WHEN P.participantid > 0 THEN P.participantid ELSE CAST(0 as bigint) END,G.organizationid
	      FROM groups G
		LEFT JOIN groupparticipation P on P.participationid = G.id
	      WHERE G.id = $1
	   UNION
	   SELECT P.id as pid,$1 as branchid, P.participationid, RT.groupid, P.participanttype as referencetype,P.participantid as referenceid, P.organizationid
	      FROM group_membership RT, groupparticipation P
	      WHERE RT.groupid = P.participationid
	)
	select * from group_membership;
	$$ LANGUAGE 'sql';


CREATE OR REPLACE FUNCTION role_membership_up(IN root_id bigint)
	RETURNS TABLE(pid bigint,branchid bigint, roleid bigint, parentid bigint, referencetype text,referenceid bigint,organizationid bigint)
	AS $$
	WITH RECURSIVE role_membership(pid,branchid,roleid, parentid, referencetype,referenceid,organizationid) AS (
	   SELECT CAST(0 as bigint),$1 as branchid, R.id as roleid, R.parentid, CAST('' as text), CAST(0 as bigint),R.organizationid
	      FROM roles R
	      WHERE R.id = $1
	   UNION ALL
	   SELECT P.id as pid,$1 as branchid, P.participantid, P.participationid, P.participanttype as referencetype,P.participantid as referenceid,P.organizationid
	      FROM role_membership RT, roleparticipation P
	      WHERE RT.roleid = P.participationid
	      --and R.participanttype = 'ROLE'
	)
	select * from role_membership;
	$$ LANGUAGE 'sql';

CREATE OR REPLACE FUNCTION group_membership_up(IN root_id bigint)
	RETURNS TABLE(pid bigint,branchid bigint, groupid bigint, parentid bigint, referencetype text,referenceid bigint, organizationid bigint)
	AS $$
	WITH RECURSIVE group_membership(pid,branchid,groupid, parentid, referencetype,referenceid, organizationid) AS (
	   SELECT CAST(0 as bigint),$1 as branchid, G.id as groupid, G.parentid, CAST('' as text), CAST(0 as bigint), G.organizationid
	      FROM groups G
	      WHERE G.id = $1
	   UNION ALL
	   SELECT P.id as pid,$1 as branchid, P.participantid, P.participationid, P.participanttype as referencetype,P.participantid as referenceid, P.organizationid
	      FROM group_membership RT, groupparticipation P
	      WHERE RT.groupid = P.participationid
	      --and P.participanttype = 'GROUP'
	)
	select * from group_membership;
	$$ LANGUAGE 'sql';

DROP VIEW IF EXISTS effectivePermissionRoles CASCADE;
create or replace view effectivePermissionRoles as
WITH result AS(
select R.id,R.parentid,roles_to_leaf(R.id) ats,R.organizationid
FROM roles R  WHERE roletype = 'USER' OR roletype = 'ACCOUNT' OR roletype = 'PERSON'
)
select PP.participationid as permissionid,(R.ats).leafid as effectiveRoleId,(R.ats).roleid as baseRoleId,PP.affectType,PP.affectId,R.organizationid from result R
JOIN permissionparticipation PP ON PP.participantid = (R.ats).leafid and PP.participanttype = 'ROLE'
;


DROP VIEW IF EXISTS effectiveDataRoles CASCADE;
create or replace view effectiveDataRoles as
WITH result AS(
select R.id,R.parentid,roles_to_leaf(R.id) ats,R.organizationid
FROM roles R  WHERE roletype = 'USER' OR roletype = 'ACCOUNT' OR roletype = 'PERSON'
)
select DP.participationid as dataid,(R.ats).leafid as effectiveRoleId,(R.ats).roleid as baseRoleId,DP.affectType,DP.affectId,R.organizationid from result R
JOIN dataparticipation DP ON DP.participantid = (R.ats).leafid and DP.participanttype = 'ROLE'
;

DROP VIEW IF EXISTS effectiveGroupRoles CASCADE;
create or replace view effectiveGroupRoles as
WITH result AS(
select R.id,R.parentid,roles_to_leaf(R.id) ats,R.organizationid
FROM roles R  WHERE roletype = 'USER' OR roletype = 'ACCOUNT' OR roletype = 'PERSON' 
)
select GP.participationid as groupid,(R.ats).leafid as effectiveRoleId,(R.ats).roleid as baseRoleId,GP.affectType, GP.affectId, R.organizationid from result R
JOIN groupparticipation GP ON GP.participantid = (R.ats).leafid and GP.participanttype = 'ROLE'
;

DROP VIEW IF EXISTS effectiveRoleRoles CASCADE;
create or replace view effectiveRoleRoles as
WITH result AS(
select R.id,R.parentid,roles_to_leaf(R.id) ats,R.organizationid
FROM roles R  WHERE roletype = 'USER' OR roletype = 'ACCOUNT' OR roletype = 'PERSON'
)
select RP.participationid as roleid,(R.ats).leafid as effectiveRoleId,(R.ats).roleid as baseRoleId,RP.affectType, RP.affectId,R.organizationid from result R
JOIN roleparticipation RP ON RP.participantid = (R.ats).leafid and RP.participanttype = 'ROLE'
;

DROP VIEW IF EXISTS effectivePersonRoles CASCADE;
create or replace view effectivePersonRoles as
WITH result AS(
select R.id,R.parentid,roles_to_leaf(R.id) ats,R.organizationid
FROM roles R  WHERE roletype = 'PERSON'
)
select CASE WHEN RP.participanttype = 'PERSON' THEN U1.id WHEN RP.participanttype = 'GROUP' AND U2.id > 0 THEN U2.id ELSE -1 END as personid,(R.ats).leafid as effectiveRoleId,(R.ats).roleid as baseRoleId,R.organizationid from result R
JOIN roleparticipation RP ON RP.participationid = (R.ats).roleid
LEFT JOIN persons U1 on U1.id = RP.participantid and RP.participanttype = 'PERSON'
LEFT JOIN groupparticipation gp2 on gp2.participationid = RP.participantid and RP.participanttype = 'GROUP' and gp2.participanttype = 'PERSON'
LEFT JOIN persons U2 on U2.id = gp2.participantid AND U2.organizationid = gp2.organizationid
;
DROP VIEW IF EXISTS effectiveUserRoles CASCADE;
create or replace view effectiveUserRoles as
WITH result AS(
select R.id,R.parentid,roles_to_leaf(R.id) ats,R.organizationid
FROM roles R  WHERE roletype = 'USER'
)
select CASE WHEN RP.participanttype = 'USER' THEN U1.id WHEN RP.participanttype = 'GROUP' AND U2.id > 0 THEN U2.id ELSE -1 END as userid,(R.ats).leafid as effectiveRoleId,(R.ats).roleid as baseRoleId,R.organizationid from result R
JOIN roleparticipation RP ON RP.participationid = (R.ats).roleid
LEFT JOIN users U1 on U1.id = RP.participantid and RP.participanttype = 'USER'
LEFT JOIN groupparticipation gp2 on gp2.participationid = RP.participantid and RP.participanttype = 'GROUP' and gp2.participanttype = 'USER'
LEFT JOIN users U2 on U2.id = gp2.participantid AND U2.organizationid = gp2.organizationid
;

DROP VIEW IF EXISTS effectiveAccountRoles CASCADE;
create or replace view effectiveAccountRoles as
WITH result AS(
select R.id,R.parentid,roles_to_leaf(R.id) ats,R.organizationid
FROM roles R  WHERE roletype = 'ACCOUNT'
)
select CASE WHEN RP.participanttype = 'ACCOUNT' THEN U1.id WHEN RP.participanttype = 'GROUP' AND U2.id > 0 THEN U2.id ELSE -1 END as accountid,(R.ats).leafid as effectiveRoleId,(R.ats).roleid as baseRoleId,R.organizationid from result R
JOIN roleparticipation RP ON RP.participationid = (R.ats).roleid
LEFT JOIN accounts U1 on U1.id = RP.participantid and RP.participanttype = 'ACCOUNT'
LEFT JOIN groupparticipation gp2 on gp2.participationid = RP.participantid and RP.participanttype = 'GROUP' and gp2.participanttype = 'ACCOUNT'
LEFT JOIN accounts U2 on U2.id = gp2.participantid AND U2.organizationid = gp2.organizationid
;

CREATE OR REPLACE FUNCTION cache_person_roles(person_id BIGINT[],organizationid BIGINT) 
        RETURNS BOOLEAN
        AS $$
	DELETE FROM personrolecache where objectid = ANY($1);
	--AND organizationid = $2;
	INSERT INTO personrolecache (objectid,effectiveroleid,baseroleid,organizationid) select * from effectivepersonRoles where personid = ANY($1) AND personid > 0;
	--and organizationid = $2;
	SELECT true;
        $$ LANGUAGE 'sql';


CREATE OR REPLACE FUNCTION cache_all_person_roles(orgId BIGINT) 
        RETURNS BOOLEAN
        AS $BODY$
        DECLARE ids BIGINT[] = ARRAY(SELECT id FROM persons WHERE organizationid = $1);
        BEGIN
	DELETE FROM personrolecache WHERE objectid = ANY(ids);
	INSERT INTO personrolecache (objectid,effectiveroleid,baseroleid,organizationid) select * from effectivepersonRoles where personid = ANY(ids) AND personid > 0;
	RETURN true;
	END

        $BODY$ LANGUAGE 'plpgsql';

CREATE OR REPLACE FUNCTION cache_user_roles(user_id BIGINT[],organizationid BIGINT) 
        RETURNS BOOLEAN
        AS $$
	DELETE FROM userrolecache where objectid = ANY($1);
	--AND organizationid = $2;
	INSERT INTO userrolecache (objectid,effectiveroleid,baseroleid,organizationid) select * from effectiveUserRoles where userid = ANY($1) AND userid > 0;
	--and organizationid = $2;
	SELECT true;
        $$ LANGUAGE 'sql';


CREATE OR REPLACE FUNCTION cache_all_user_roles(orgId BIGINT) 
        RETURNS BOOLEAN
        AS $BODY$
        DECLARE ids BIGINT[] = ARRAY(SELECT id FROM users WHERE organizationid = $1);
        BEGIN
	DELETE FROM userrolecache WHERE objectid = ANY(ids);
	INSERT INTO userrolecache (objectid,effectiveroleid,baseroleid,organizationid) select * from effectiveUserRoles where userid = ANY(ids) AND userid> 0;
	RETURN true;
	END

        $BODY$ LANGUAGE 'plpgsql';


CREATE OR REPLACE FUNCTION cache_account_roles(account_id BIGINT[],organizationid BIGINT) 
        RETURNS BOOLEAN
        AS $$
	DELETE FROM accountrolecache where objectid = ANY($1);
	--AND organizationid = $2;
	INSERT INTO accountrolecache (objectid,effectiveroleid,baseroleid,organizationid) select * from effectiveaccountRoles where accountid = ANY($1) AND accountid > 0;
	--and organizationid = $2;
	SELECT true;
        $$ LANGUAGE 'sql';


CREATE OR REPLACE FUNCTION cache_all_account_roles(orgId BIGINT) 
        RETURNS BOOLEAN
        AS $BODY$
        DECLARE ids BIGINT[] = ARRAY(SELECT id FROM accounts WHERE organizationid = $1);
        BEGIN
	DELETE FROM accountrolecache WHERE objectid = ANY(ids);
	INSERT INTO accountrolecache (objectid,effectiveroleid,baseroleid,organizationid) select * from effectiveaccountRoles where accountid = ANY(ids) AND accountid > 0;
	RETURN true;
	END

        $BODY$ LANGUAGE 'plpgsql';

CREATE OR REPLACE FUNCTION cache_all_permission_roles(orgId BIGINT) 
        RETURNS BOOLEAN
        AS $BODY$
        DECLARE ids BIGINT[] = ARRAY(SELECT id FROM permissions WHERE organizationid = $1);
        BEGIN
	DELETE FROM permissionrolecache WHERE objectid = ANY(ids);
	INSERT INTO permissionrolecache (objectid,effectiveroleid,baseroleid,affecttype,affectid,organizationid) select * from effectivePermissionRoles where permissionid=ANY(ids);
	RETURN true;
	END

        $BODY$ LANGUAGE 'plpgsql';

CREATE OR REPLACE FUNCTION cache_permission_roles(permission_id BIGINT[],organizationid BIGINT) 
        RETURNS BOOLEAN
        AS $$
        BEGIN
	DELETE FROM permissionrolecache where objectid = ANY($1);
	INSERT INTO permissionrolecache (objectid,effectiveroleid,baseroleid,affecttype,affectid,organizationid) select * from effectivePermissionRoles where permissionid=ANY($1);
	RETURN true;
	END
        $$ LANGUAGE 'plpgsql';


CREATE OR REPLACE FUNCTION cache_all_data_roles(orgId BIGINT) 
        RETURNS BOOLEAN
        AS $BODY$
        DECLARE ids BIGINT[] = ARRAY(SELECT id FROM data WHERE organizationid = $1);
        BEGIN
	DELETE FROM datarolecache WHERE objectid = ANY(ids);
	INSERT INTO datarolecache (objectid,effectiveroleid,baseroleid,affecttype,affectid,organizationid) select * from effectiveDataRoles where dataid=ANY(ids);
	RETURN true;
	END

        $BODY$ LANGUAGE 'plpgsql';

CREATE OR REPLACE FUNCTION cache_data_roles(data_id BIGINT[],organizationid BIGINT) 
        RETURNS BOOLEAN
        AS $$
        BEGIN
	DELETE FROM datarolecache where objectid = ANY($1);
	INSERT INTO datarolecache (objectid,effectiveroleid,baseroleid,affecttype,affectid,organizationid) select * from effectiveDataRoles where dataid=ANY($1);
	RETURN true;
	END
        $$ LANGUAGE 'plpgsql';

CREATE OR REPLACE FUNCTION cache_all_role_roles(orgId BIGINT) 
        RETURNS BOOLEAN
        AS $BODY$
        DECLARE ids BIGINT[] = ARRAY(SELECT id FROM roles WHERE organizationid = $1);
        BEGIN
	DELETE FROM rolerolecache WHERE objectid = ANY(ids);
	INSERT INTO rolerolecache (objectid,effectiveroleid,baseroleid,affecttype,affectid,organizationid) select * from effectiveRoleRoles where roleid=ANY(ids);
	RETURN true;
	END

        $BODY$ LANGUAGE 'plpgsql';

CREATE OR REPLACE FUNCTION cache_role_roles(role_id BIGINT[],organizationid BIGINT) 
        RETURNS BOOLEAN
        AS $$
        BEGIN
	DELETE FROM rolerolecache where objectid = ANY($1);
	INSERT INTO rolerolecache (objectid,effectiveroleid,baseroleid,affecttype,affectid,organizationid) select * from effectiveRoleRoles where roleid=ANY($1);
	RETURN true;
	END
        $$ LANGUAGE 'plpgsql';

CREATE OR REPLACE FUNCTION cache_all_group_roles(orgId BIGINT) 
        RETURNS BOOLEAN
        AS $BODY$
        DECLARE ids BIGINT[] = ARRAY(SELECT id FROM groups WHERE organizationid = $1);
        BEGIN
	DELETE FROM grouprolecache WHERE objectid = ANY(ids);
	INSERT INTO grouprolecache (objectid,effectiveroleid,baseroleid,affecttype,affectid,organizationid) select * from effectiveGroupRoles where groupid=ANY(ids);
	RETURN true;
	END

        $BODY$ LANGUAGE 'plpgsql';

CREATE OR REPLACE FUNCTION cache_group_roles(group_id BIGINT[],organizationid BIGINT) 
        RETURNS BOOLEAN
        AS $$
        BEGIN
	DELETE FROM grouprolecache where objectid = ANY($1);
	INSERT INTO grouprolecache (objectid,effectiveroleid,baseroleid,affecttype,affectid,organizationid) select * from effectiveGroupRoles where groupid=ANY($1);
	RETURN true;
	END
        $$ LANGUAGE 'plpgsql';

--create or replace view effectiveGroupRoleRights as
--select distinct GC.groupid,GC.affectId,GC.affectType,GC.effectiveRoleId as roleid,GC.organizationid from grouprolecache GC
--;

create or replace view effectivePermissionPersonRoleRights as
select distinct PC.objectid as permissionid,PC.affectId,PC.affectType,ER.objectid as personid,ER.effectiveRoleId as roleid,ER.organizationid from personrolecache ER
join permissionRoleCache PC on PC.effectiveRoleId=ER.effectiveRoleId 
;


create or replace view effectiveGroupPersonRoleRights as
select distinct GC.objectid as groupid,GC.affectId,GC.affectType,ER.objectid as personid,ER.effectiveRoleId as roleid,ER.organizationid from personrolecache ER
join groupRoleCache GC on GC.effectiveRoleId=ER.effectiveRoleId 
;

create or replace view effectivePermissionUserRoleRights as
select distinct PC.objectid as permissionid,PC.affectId,PC.affectType,ER.objectid as userid,ER.effectiveRoleId as roleid,ER.organizationid from userrolecache ER
join permissionRoleCache PC on PC.effectiveRoleId=ER.effectiveRoleId 
;


create or replace view effectiveGroupUserRoleRights as
select distinct GC.objectid as groupid,GC.affectId,GC.affectType,ER.objectid as userid,ER.effectiveRoleId as roleid,ER.organizationid from userrolecache ER
join groupRoleCache GC on GC.effectiveRoleId=ER.effectiveRoleId 
;

create or replace view effectivePermissionAccountRoleRights as
select distinct PC.objectid as permissionid,PC.affectId,PC.affectType,ER.objectid as accountid,ER.effectiveRoleId as roleid,ER.organizationid from accountrolecache ER
join permissionRoleCache PC on PC.effectiveRoleId=ER.effectiveRoleId 
;


create or replace view effectiveGroupAccountRoleRights as
select distinct GC.objectid as groupid,GC.affectId,GC.affectType,ER.objectid as accountid,ER.effectiveRoleId as roleid,ER.organizationid from accountrolecache ER
join groupRoleCache GC on GC.effectiveRoleId=ER.effectiveRoleId 
;

create or replace view effectiveDataPersonRoleRights as
select distinct DRC.objectid as dataid,DRC.affectId,DRC.affectType,ER.objectid as personid,ER.effectiveRoleId as roleid,ER.organizationid from personrolecache ER
join dataRoleCache DRC on DRC.effectiveRoleId=ER.effectiveRoleId
;

create or replace view effectiveDataUserRoleRights as
select distinct DRC.objectid as dataid,DRC.affectId,DRC.affectType,ER.objectid as userid,ER.effectiveRoleId as roleid,ER.organizationid from userrolecache ER
join dataRoleCache DRC on DRC.effectiveRoleId=ER.effectiveRoleId
;

create or replace view effectiveDataAccountRoleRights as
select distinct DRC.objectid as dataid,DRC.affectId,DRC.affectType,ER.objectid as accountid,ER.effectiveRoleId as roleid,ER.organizationid from accountrolecache ER
join dataRoleCache DRC on DRC.effectiveRoleId=ER.effectiveRoleId
;

create or replace view effectiveRolePersonRoleRights as
select distinct RRC.objectid as sourceroleid,RRC.affectId,RRC.affectType,ER.objectid as personid,ER.effectiveRoleId as roleid,ER.organizationid from personrolecache ER
join roleRoleCache RRC on RRC.effectiveRoleId=ER.effectiveRoleId
;

create or replace view effectiveRoleUserRoleRights as
select distinct RRC.objectid as sourceroleid,RRC.affectId,RRC.affectType,ER.objectid as userid,ER.effectiveRoleId as roleid,ER.organizationid from userrolecache ER
join roleRoleCache RRC on RRC.effectiveRoleId=ER.effectiveRoleId
;

create or replace view effectiveRoleAccountRoleRights as
select distinct RRC.objectid as sourceroleid,RRC.affectId,RRC.affectType,ER.objectid as accountid,ER.effectiveRoleId as roleid,ER.organizationid from accountrolecache ER
join roleRoleCache RRC on RRC.effectiveRoleId=ER.effectiveRoleId
;




CREATE OR REPLACE FUNCTION cache_roles() 
        RETURNS BOOLEAN
        AS $$
	truncate dataRoleCache;
	insert into datarolecache (objectid,effectiveroleid,baseroleid,affecttype,affectid,organizationid) select * from effectiveDataRoles;
	truncate roleRoleCache;
	insert into rolerolecache (objectid,effectiveroleid,baseroleid,affecttype,affectid,organizationid) select * from effectiveRoleRoles;
	truncate groupRoleCache;
	insert into grouprolecache (objectid,effectiveroleid,baseroleid,affecttype,affectid,organizationid) select * from effectiveGroupRoles;
	truncate permissionRoleCache;
	INSERT INTO permissionrolecache (objectid,effectiveroleid,baseroleid,affecttype,affectid,organizationid) select * from effectivePermissionRoles;
	truncate personRoleCache;
	INSERT INTO personrolecache (objectid,effectiveroleid,baseroleid,organizationid) select * from effectivePersonRoles where personid > 0;
	truncate userRoleCache;
	INSERT INTO userrolecache (objectid,effectiveroleid,baseroleid,organizationid) select * from effectiveUserRoles where userid > 0;
	truncate accountRoleCache;
	INSERT INTO accountrolecache (objectid,effectiveroleid,baseroleid,organizationid) select * from effectiveAccountRoles where accountid > 0;

	SELECT true;
        $$ LANGUAGE 'sql';


create or replace view groupRights as
	select personid as referenceid,'PERSON' as referencetype,groupid,affecttype,affectid,organizationid
	FROM groupPersonRights GPR
	UNION
	select personid as referenceid,'PERSON',groupid,affecttype,affectid,organizationid
	FROM effectiveGroupPersonRoleRights GPR
	UNION
	select userid as referenceid,'USER' as referencetype,groupid,affecttype,affectid,organizationid
	FROM groupUserRights GUR
	UNION
	select userid as referenceid,'USER',groupid,affecttype,affectid,organizationid
	FROM effectiveGroupUserRoleRights GRR
	UNION
	select accountid as referenceid,'ACCOUNT' as referencetype,groupid,affecttype,affectid,organizationid
	FROM groupAccountRights GUR
	UNION
	select accountid as referenceid,'ACCOUNT',groupid,affecttype,affectid,organizationid
	FROM effectiveGroupAccountRoleRights GRR
	UNION
	select AGP.participantid as referenceid,'GROUP',AG.id as groupid,AGP.affecttype,AGP.affectid,AG.organizationid
	FROM groups AG
	inner join groupparticipation AGP on AGP.participantType = 'GROUP' AND AGP.participationId = AG.id
--	Role rights are not included in this view since the role member privileges are also reflected
--	UNION
--	select roleid as referenceid,'ROLE',groupid,affecttype,affectid,organizationid
--	FROM effectiveGroupRoleRights GRR
;

create or replace view dataRights as
	select personid as referenceid,'PERSON' as referencetype,dataid,affecttype,affectid,organizationid
	FROM dataPersonRights GUR
	UNION
	select personid as referenceid,'PERSON' as referencetype,dataid,affecttype,affectid,organizationid
	FROM effectiveDataPersonRoleRights GRR
	UNION
	select userid as referenceid,'USER' as referencetype,dataid,affecttype,affectid,organizationid
	FROM dataUserRights GUR
	UNION
	select userid as referenceid,'USER' as referencetype,dataid,affecttype,affectid,organizationid
	FROM effectiveDataUserRoleRights GRR
	UNION
	select accountid as referenceid,'ACCOUNT' as referencetype,dataid,affecttype,affectid,organizationid
	FROM dataAccountRights GUR
	UNION
	select accountid as referenceid,'ACCOUNT' as referencetype,dataid,affecttype,affectid,organizationid
	FROM effectiveDataAccountRoleRights GRR	
	UNION
	select AGP.participantid as referenceid,'GROUP' as referencetype,AGP.participationid as dataid,AGP.affecttype,AGP.affectid,AG.organizationid
	FROM groups AG
	inner join dataparticipation AGP on AGP.participantType = 'GROUP' AND AGP.participantId = AG.id
;

create or replace view roleRights as
	select personid as referenceid,'PERSON' as referencetype,roleid,affecttype,affectid,organizationid
	FROM rolePersonRights GUR
	UNION
	select personid as referenceid,'PERSON' as referencetype,sourceroleid as roleid,affecttype,affectid,organizationid
	FROM effectiveRolePersonRoleRights GRR
	UNION
	select userid as referenceid,'USER' as referencetype,roleid,affecttype,affectid,organizationid
	FROM roleUserRights GUR
	UNION
	select userid as referenceid,'USER' as referencetype,sourceroleid as roleid,affecttype,affectid,organizationid
	FROM effectiveRoleUserRoleRights GRR
	UNION
	select accountid as referenceid,'ACCOUNT' as referencetype,roleid,affecttype,affectid,organizationid
	FROM roleAccountRights GUR
	UNION
	select accountid as referenceid,'ACCOUNT' as referencetype,sourceroleid as roleid,affecttype,affectid,organizationid
	FROM effectiveRoleAccountRoleRights GRR
	UNION
	select AGP.participantid as referenceid,'GROUP' as referencetype,AGP.participationid as dataid,AGP.affecttype,AGP.affectid,AG.organizationid
	FROM roles AG
	inner join roleparticipation AGP on AGP.participantType = 'GROUP' AND AGP.participantId = AG.id

;

create or replace view permissionRights as
	select personid as referenceid,'PERSON' as referencetype,permissionid,affecttype,affectid,organizationid
	FROM permissionPersonRights GUR
	UNION
	select personid as referenceid,'PERSON' as referencetype,permissionid,affecttype,affectid,organizationid
	FROM effectivePermissionPersonRoleRights GRR
	UNION
	select userid as referenceid,'USER' as referencetype,permissionid,affecttype,affectid,organizationid
	FROM permissionUserRights GUR
	UNION
	select userid as referenceid,'USER' as referencetype,permissionid,affecttype,affectid,organizationid
	FROM effectivePermissionUserRoleRights GRR
	UNION
	select accountid as referenceid,'ACCOUNT' as referencetype,permissionid,affecttype,affectid,organizationid
	FROM permissionAccountRights GUR
	UNION
	select accountid as referenceid,'ACCOUNT' as referencetype,permissionid,affecttype,affectid,organizationid
	FROM effectivePermissionAccountRoleRights GRR
	UNION
	select AGP.participantid as referenceid,'GROUP' as referencetype,AGP.participationid as permissionid,AGP.affecttype,AGP.affectid,AG.organizationid
	FROM permissions AG
	inner join permissionparticipation AGP on AGP.participantType = 'GROUP' AND AGP.participantId = AG.id
;


create or replace view orphanCredentials as
select id from credential
where
organizationid NOT IN(select id from organizations)
OR referencetype = 'USER' AND referenceid NOT IN (select id from users)
OR referencetype = 'GROUP' AND referenceid NOT IN (select id from groups)
OR referencetype = 'DATA' AND referenceid NOT IN (select id from data)
;


create or replace view orphanPersonParticipations as
select id from PersonParticipation RP1
where (participanttype = 'USER' and participantid not in (select id from Users U1))
OR ((participanttype = 'PERSON' OR participanttype = 'DEPENDENT') and participantid not in (select id from Persons P1))
OR (participanttype = 'ACCOUNT' and participantid not in (select id from Accounts A1))
OR (participationid not in (select id from persons D1))
or
organizationid not in (select id from organizations)
;

create or replace view orphanDataParticipations as
select id from DataParticipation RP1
where (participanttype = 'USER' and participantid not in (select id from Users U1))
OR (participanttype = 'PERSON' and participantid not in (select id from Persons P1))
OR (participanttype = 'ACCOUNT' and participantid not in (select id from Accounts A1))
OR (participanttype = 'ROLE' and participantid not in (select id from Roles R1))
OR (participanttype = 'GROUP' and participantid not in (select id from Groups G1))
OR (participationid not in (select id from Data D1))
or
organizationid not in (select id from organizations)
;

create or replace view orphanPermissionParticipations as
select id from PermissionParticipation RP1
where (participanttype = 'USER' and participantid not in (select id from Users U1))
OR (participanttype = 'PERSON' and participantid not in (select id from Persons P1))
OR (participanttype = 'ACCOUNT' and participantid not in (select id from Accounts A1))
OR (participanttype = 'ROLE' and participantid not in (select id from Roles R1))
OR (participanttype = 'GROUP' and participantid not in (select id from Groups G1))
OR (participationid not in (select id from Permissions R1))
or
organizationid not in (select id from organizations)
;


create or replace view orphanRoleParticipations as
select id from RoleParticipation RP1
where (participanttype = 'USER' and participantid not in (select id from Users U1))
OR (participanttype = 'PERSON' and participantid not in (select id from Persons P1))
OR (participanttype = 'ACCOUNT' and participantid not in (select id from Accounts A1))
OR (participanttype = 'ROLE' and participantid not in (select id from Roles R1))
OR (participanttype = 'GROUP' and participantid not in (select id from Groups G1))
OR (participationid not in (select id from Roles R1))
or
organizationid not in (select id from organizations)
;

create or replace view orphanGroupParticipations as
select id from GroupParticipation GP1
where (participanttype = 'USER' and participantid not in (select id from Users U1))
OR (participanttype = 'PERSON' and participantid not in (select id from Persons P1))
OR (participanttype = 'ACCOUNT' and participantid not in (select id from Accounts A1))
OR (participanttype = 'ROLE' and participantid not in (select id from Roles R1))
OR (participanttype = 'GROUP' and participantid not in (select id from Groups G1))
OR (participationid not in (select id from Groups))
or
organizationid not in (select id from organizations)
;

create or replace view orphanTagParticipations as
select GP1.id from TagParticipation GP1
left join Data D1 on D1.id = GP1.participantid AND GP1.participanttype = 'DATA'
left join Accounts A1 on A1.id = GP1.participantid AND GP1.participanttype = 'ACCOUNT'
left join Groups G1 on G1.id = GP1.participantid AND GP1.participanttype = 'GROUP'
left join Persons P1 on G1.id = GP1.participantid AND GP1.participanttype = 'PERSON'
left join Roles R1 on R1.id = GP1.participantid AND GP1.participanttype = 'ROLE'
left join Users U1 on U1.id = GP1.participantid AND GP1.participanttype = 'USER'
WHERE
D1.id IS NULL AND A1.id IS NULL AND G1.id IS NULL AND P1.id IS NULL AND R1.id IS NULL AND U1.id IS NULL
;

create or replace view orphanContactInformationParticipations as
select id from ContactInformationParticipation GP1
where (participanttype = 'CONTACT' and participantid not in (select id from Contacts U1))
or
(participanttype = 'ADDRESS' and participantid not in (select id from Addresses A1))
or
ownerId not in(select id from Users U)
or
organizationid not in (select id from organizations)
;

create or replace view orphanGroups as
select id, name, parentid from Groups G1
where (parentid not in (select id from Groups G2)
and G1.parentid > 0)
or
ownerId not in(select id from Users U)
or
organizationid not in (select id from organizations)
;

create or replace view orphanRoles as
select id, name, parentid from Roles R1
where
(parentid not in (select id from Roles R2)
and R1.parentid > 0)
or
ownerId not in(select id from Users U)
or
organizationid not in (select id from organizations)
;

create or replace view orphanData as
select id, name, groupid from Data G1
where groupid not in (select id from Groups G2)
or
ownerId not in(select id from Users U)
or
organizationid not in (select id from organizations)
;

create or replace view orphanAccounts as
select id, name, groupid from Accounts G1
where groupid not in (select id from Groups G2)
or
ownerId not in(select id from Users U)
or
organizationid not in (select id from organizations)
;

create or replace view orphanAttributes as
select referenceid, referencetype,organizationid from attribute R1
where 
(referencetype = 'GROUP' AND referenceid not in (select id from groups G1))
OR
(referencetype = 'ACCOUNT' AND referenceid not in (select id from accounts A1))
OR
(referencetype = 'PERSON' AND referenceid not in (select id from persons A1))
OR
(referencetype = 'DATA' AND referenceid not in (select id from data D1))
OR
organizationid not in (select id from organizations)
;
create or replace view orphanPersons as
select id, name, groupid from persons R1
where (groupid not in (select id from Groups G1)
and R1.groupid > 0)
or
organizationid not in (select id from organizations)
;

create or replace view orphanContacts as
select id, name, groupid from contacts R1
where (groupid not in (select id from Groups G1)
and R1.groupid > 0)
or
ownerId not in(select id from Users U)
or
organizationid not in (select id from organizations)
;

create or replace view orphanAddresses as
select id, name, groupid from addresses R1
where (groupid not in (select id from Groups G1)
and R1.groupid > 0)
or
ownerId not in(select id from Users U)
or
organizationid not in (select id from organizations)
;

create or replace view orphanPermissions as
select id, name, parentId from permissions R1
where (parentid not in (select id from Permissions G1)
and R1.parentid > 0)
or
ownerId not in(select id from Users U)
or
organizationid not in (select id from organizations)
;

create or replace view orphanContactInformation as
select id,contactinformationtype,organizationid from contactinformation RP1
where (contactinformationtype = 'USER' and referenceid not in (select id from Users U1))
or (contactinformationtype = 'PERSON' and referenceid not in (select id from Persons P1))
OR (contactinformationtype = 'ACCOUNT' and referenceid not in (select id from Accounts A1))
or
organizationid not in (select id from organizations)
;

CREATE OR REPLACE FUNCTION cleanup_orphans() 
        RETURNS BOOLEAN
        AS $$

        delete from persons where id in (select id from orphanPersons);
	delete from users where organizationid not in (select id from organizations);
	delete from data where id in (select id from orphanData);
	delete from accounts where id in (select id from orphanAccounts);
	delete from groups where id in (select id from orphanGroups);
	delete from roles where id in (select id from orphanRoles);
	delete from permissions where id in (select id from orphanPermissions);
	delete from contacts where id in (select id from orphanContacts);
	delete from addresses where id in (select id from orphanAddresses);
	delete from contactinformation where id in (select id from orphanContactInformation);
	delete from contactinformationparticipation where id in (select id from orphanContactInformationParticipations);

	delete from dataparticipation where id in (select id from orphanDataParticipations);
	delete from permissionparticipation where id in (select id from orphanPermissionParticipations);
	delete from roleparticipation where id in (select id from orphanRoleParticipations);
	delete from groupparticipation where id in (select id from orphanGroupParticipations);
	delete from personparticipation where id in (select id from orphanPersonParticipations);
	delete from tagparticipation where id in (select id from orphanTagParticipations);
	delete from credential where id in (select id from orphanCredentials);

        delete from attribute where referencetype = 'GROUP' and referenceid not in (select id from groups);
	delete from attribute where referencetype = 'PERMISSION' and referenceid not in (select id from permissions);
        delete from attribute where referencetype = 'PERSON' and referenceid not in (select id from persons);
	delete from attribute where referencetype = 'USER' and referenceid not in (select id from users);
	delete from attribute where referencetype = 'DATA' and referenceid not in (select id from data);
	delete from attribute where referencetype = 'ACCOUNT' and referenceid not in (select id from accounts);

	SELECT true;
        $$ LANGUAGE 'sql';



CREATE OR REPLACE FUNCTION delete_empty_thumbnails() 
        RETURNS BOOLEAN
        AS $$
	delete from data where groupid in (select id from groups where name = '.thumbnail') and size <= 0;
	select * from cleanup_orphans();
	SELECT true;
        $$ LANGUAGE 'sql';


CREATE OR REPLACE FUNCTION delete_thumbnails() 
        RETURNS BOOLEAN
        AS $$
	delete from data where groupid in (select id from groups where name = '.thumbnail');
	-- delete from groups where name = '.thumbnail';
	select * from cleanup_orphans();
	SELECT true;
        $$ LANGUAGE 'sql';

create or replace view orphanHomeGroups as 
  select G1.id,G1.name,G1.organizationid from groups G1
  join groups G2 on G1.parentId=G2.id and G2.name = 'Home'
  join groups G3 on G2.parentId = G3.id and G3.name = 'Root' and G3.parentId = 0
  where G1.name not in (select name from users where organizationid = G1.organizationid)
;

create or replace view personContact as
select P.id,P.ownerid,P.name,P.parentid,P.groupid,P.organizationid,P.title,P.prefix
,P.suffix,P.description,P.firstname,P.middlename,P.lastname,P.alias,P.birthdate,P.gender
,A.name as addressname,A.description as addressdescription,A.preferred as preferredaddress,A.locationtype as addresslocation
,A.addressline1,A.addressline2,A.city,A.state,A.region,A.postalcode,A.country
,CT.name as contactname,CT.description as contactdescription,CT.preferred as preferredcontact,CT.contacttype,CT.locationtype as contactlocation,CT.contactvalue
from Persons P
inner join contactinformation c on C.referenceId = P.id and C.contactinformationtype = 'PERSON'
left join contactinformationparticipation CP on CP.participationid = C.id
left join addresses A on A.id = CP.participantid and CP.participanttype = 'ADDRESS'
left join contacts CT on CT.id = CP.participantid and CP.participanttype = 'CONTACT'
;


create or replace view userContact as
select U.id,U.name,U.accountid,U.usertype,U.organizationid
,A.name as addressname,A.description as addressdescription,A.preferred as preferredaddress,A.locationtype as addresslocation
,A.addressline1,A.addressline2,A.city,A.state,A.region,A.postalcode,A.country
,CT.name as contactname,CT.description as contactdescription,CT.preferred as preferredcontact,CT.contacttype,CT.locationtype as contactlocation,CT.contactvalue
from Users U
inner join contactinformation c on C.referenceId = U.id and C.contactinformationtype = 'USER'
left join contactinformationparticipation CP on CP.participationid = C.id
left join addresses A on A.id = CP.participantid and CP.participanttype = 'ADDRESS'
left join contacts CT on CT.id = CP.participantid and CP.participanttype = 'CONTACT'
;

create or replace view personUsers as
select P.id,P.ownerid,P.name,P.parentid,P.groupid,P.organizationid,P.title,P.prefix
,P.suffix,P.description,P.firstname,P.middlename,P.lastname,P.alias,P.birthdate,P.gender,
U.id as userid,U.name as username
from Persons P
inner join personparticipation PT on PT.participationId = P.id AND PT.participantType = 'USER'
inner join users U on U.id = PT.participantId
;

create or replace view personAccounts as
select P.id,P.ownerid,P.name,P.urn,P.parentid,P.groupid,P.organizationid,P.title,P.prefix
,P.suffix,P.description,P.firstname,P.middlename,P.lastname,P.alias,P.birthdate,P.gender,
U.id as accountid,U.name as accountname
from Persons P
inner join personparticipation PT on PT.participationId = P.id AND PT.participantType = 'ACCOUNT'
inner join accounts U on U.id = PT.participantId
;

--- TODO: Is this missing the join against 'USER' groups?
---
create or replace view groupEntitlements as
select
G.id as groupId,G.urn as groupUrn,R.id as roleId, R.urn as roleUrn,
CASE WHEN A2.id > 0 THEN A2.id ELSE A.id END as accountId,
CASE WHEN A2.id > 0 THEN A2.urn ELSE A.urn END as accountUrn,
CASE WHEN P3.id > 0 THEN P3.id ELSE P2.id END as personId,P2.urn as personUrn,
GR.referenceId,GR.referenceType,GR.affectType,
P.id as permissionId,P.urn as permissionUrn,GR.organizationId 
FROM groupRights GR
JOIN permissions P ON P.id = GR.affectId
JOIN groups G on G.id = GR.groupId
LEFT JOIN accounts A ON A.id = GR.referenceId AND GR.referenceType = 'ACCOUNT'
LEFT JOIN personparticipation P3 ON P3.participantId = GR.referenceId AND GR.referenceType = 'ACCOUNT' AND P3.participantType = 'ACCOUNT'
LEFT JOIN persons P2 ON (P2.id = GR.referenceId AND GR.referenceType = 'PERSON') OR (P2.id = P3.participationId AND GR.referenceType = 'ACCOUNT')
LEFT JOIN effectiveGroupAccountRoleRights eGAR ON eGAR.groupId = GR.groupId AND eGAR.accountId = GR.referenceId AND GR.referenceType = 'ACCOUNT' AND eGAR.affectId = P.id
LEFT JOIN effectiveGroupPersonRoleRights eGPR ON eGPR.groupId = GR.groupId AND eGPR.personId = GR.referenceId AND GR.referenceType = 'PERSON' AND eGPR.affectId = P.id
LEFT JOIN roles R ON (GR.referenceType = 'PERSON' AND eGPR.roleId = R.id) OR (GR.referenceType = 'ACCOUNT' AND eGAR.roleId = R.id)
LEFT JOIN groupparticipation aGP on aGP.participationid = GR.referenceid AND GR.referencetype = 'GROUP' AND aGP.participanttype = 'ACCOUNT'
LEFT JOIN accounts A2 ON A2.id = aGP.participantid
;

DROP TABLE IF EXISTS groupEntitlementsCache CASCADE;
CREATE TABLE groupEntitlementsCache (
	GroupUrn text not null,
	PermissionUrn text not null,
	PersonUrn text,
	AccountUrn text,
	RoleUrn text,
	OrganizationId bigint not null default 0
);
CREATE UNIQUE INDEX groupentitlementscache_idx ON groupEntitlementsCache(GroupUrn,PermissionUrn,PersonUrn,AccountUrn,RoleUrn);
--CREATE INDEX groupentitlementscache_groupurn ON groupEntitlementsCache(GroupUrn);
--CREATE INDEX groupentitlementscache_permissionurn ON groupEntitlementsCache(PermissionUrn);
--CREATE INDEX groupentitlementscache_personurn ON groupEntitlementsCache(PersonUrn);
--CREATE INDEX groupentitlementscache_accounturn ON groupEntitlementsCache(AccountUrn);

CREATE OR REPLACE FUNCTION cache_group_entitlements() 
        RETURNS BOOLEAN
        AS $$
	truncate groupEntitlementsCache;
	insert into groupEntitlementsCache (GroupUrn,PermissionUrn,PersonUrn,AccountUrn,RoleUrn,OrganizationId) select distinct GroupUrn,PermissionUrn,PersonUrn,AccountUrn,RoleUrn,OrganizationId from groupEntitlements;

	SELECT true;
        $$ LANGUAGE 'sql';

create or replace view dataEntitlements as
select
G.id as dataId,G.urn as dataUrn,R.id as roleId, R.urn as roleUrn,
CASE WHEN A2.id > 0 THEN A2.id ELSE A.id END as accountId,
CASE WHEN A2.id > 0 THEN A2.urn ELSE A.urn END as accountUrn,
CASE WHEN P3.id > 0 THEN P3.id ELSE P2.id END as personId,P2.urn as personUrn,
GR.referenceId,GR.referenceType,
P.id as permissionId,P.urn as permissionUrn,GR.affectType,GR.organizationId 
FROM dataRights GR
JOIN permissions P ON P.id = GR.affectId
JOIN data G on G.id = GR.dataId
LEFT JOIN accounts A ON A.id = GR.referenceId AND GR.referenceType = 'ACCOUNT'
LEFT JOIN personparticipation P3 ON P3.participantId = GR.referenceId AND GR.referenceType = 'ACCOUNT' AND P3.participantType = 'ACCOUNT'
LEFT JOIN persons P2 ON (P2.id = GR.referenceId AND GR.referenceType = 'PERSON') OR (P2.id = P3.participationId AND GR.referenceType = 'ACCOUNT')
LEFT JOIN effectiveDataAccountRoleRights eGAR ON eGAR.dataId = GR.dataId AND eGAR.accountId = GR.referenceId AND GR.referenceType = 'ACCOUNT' AND eGAR.affectId = P.id
LEFT JOIN effectiveDataPersonRoleRights eGPR ON eGPR.dataId = GR.dataId AND eGPR.personId = GR.referenceId AND GR.referenceType = 'PERSON' AND eGPR.affectId = P.id
LEFT JOIN roles R ON (GR.referenceType = 'PERSON' AND eGPR.roleId = R.id) OR (GR.referenceType = 'ACCOUNT' AND eGAR.roleId = R.id)
LEFT JOIN groupparticipation aGP on aGP.participationid = GR.referenceid AND GR.referencetype = 'GROUP' AND aGP.participanttype = 'ACCOUNT'
LEFT JOIN accounts A2 ON A2.id = aGP.participantid

;

DROP TABLE IF EXISTS dataEntitlementsCache CASCADE;
CREATE TABLE dataEntitlementsCache (
	DataUrn text not null,
	PermissionUrn text not null,
	
	PersonUrn text,
	AccountUrn text,
        RoleUrn text,
	OrganizationId bigint not null default 0
);
CREATE UNIQUE INDEX dataentitlementscache_idx ON dataEntitlementsCache(DataUrn,PermissionUrn,PersonUrn,AccountUrn);
-- CREATE INDEX dataentitlementscache_groupurn ON dataEntitlementsCache(DataUrn);
-- CREATE INDEX dataentitlementscache_permissionurn ON dataEntitlementsCache(PermissionUrn);
-- CREATE INDEX dataentitlementscache_personurn ON dataEntitlementsCache(PersonUrn);
-- CREATE INDEX dataentitlementscache_accounturn ON dataEntitlementsCache(AccountUrn);

CREATE OR REPLACE FUNCTION cache_data_entitlements() 
        RETURNS BOOLEAN
        AS $$
	truncate dataEntitlementsCache;
	insert into dataEntitlementsCache (DataUrn,PermissionUrn,PersonUrn,AccountUrn,RoleUrn,OrganizationId) select distinct DataUrn,PermissionUrn,PersonUrn,AccountUrn,RoleUrn,OrganizationId from dataEntitlements;
	SELECT true;
        $$ LANGUAGE 'sql';



create or replace view effectiveGroupRights as
select roleid,affectid,affecttype,userid as referenceid,'USER' as referencetype,groupid,organizationid from effectiveGroupUserRoleRights
union all
select roleid,affectid,affecttype,accountid as referenceid,'ACCOUNT' as referencetype,groupid,organizationid from effectiveGroupAccountRoleRights
union all
select roleid,affectid,affecttype,personid as referenceid,'PERSON' as referencetype,groupid,organizationid  from effectiveGroupPersonRoleRights;
;

create or replace view effectiveRoleRights as
select sourceroleid,affectid,affecttype,userid as referenceid,'USER' as referencetype,roleid,organizationid from effectiveRoleUserRoleRights
union all
select sourceroleid,affectid,affecttype,accountid as referenceid,'ACCOUNT' as referencetype,roleid,organizationid from effectiveRoleAccountRoleRights
union all
select sourceroleid,affectid,affecttype,personid as referenceid,'PERSON' as referencetype,roleid,organizationid  from effectiveRolePersonRoleRights;
;

create or replace view effectiveDataRights as
select roleid,affectid,affecttype,userid as referenceid,'USER' as referencetype,dataid,organizationid from effectiveDataUserRoleRights
union all
select roleid,affectid,affecttype,accountid as referenceid,'ACCOUNT' as referencetype,dataid,organizationid from effectiveDataAccountRoleRights
union all
select roleid,affectid,affecttype,personid as referenceid,'PERSON' as referencetype,dataid,organizationid  from effectiveDataPersonRoleRights;
;

create or replace view accountEntitlements as
select 'GROUP' as referenceType, groupid as referenceid,roleid,roleurn,accountid,accounturn,permissionid,permissionurn,affectType,organizationid from groupEntitlements where referencetype = 'ACCOUNT' OR (referencetype = 'GROUP' AND accountid > 0)
union all
select 'DATA' as referenceType,dataid as referenceid,roleid,roleurn,accountid,accounturn,permissionid,permissionurn,affectType,organizationid from dataEntitlements where referencetype = 'ACCOUNT'
;

create or replace view personEntitlements as
select text('GROUP') as referenceType, groupid as referenceid,roleid,roleurn,personid,personurn,permissionid,permissionurn,organizationid from groupEntitlements where referencetype = 'PERSON'
union all
select 'DATA' as referenceType,dataid as referenceid,roleid,roleurn,personid,personurn,permissionid,permissionurn,organizationid from dataEntitlements where referencetype = 'PERSON'
;

create or replace view personAccountEntitlements as
select text('ACCOUNT') as referencetype, PA.accountid as referenceid,AE.roleid,AE.roleurn,PA.id as personid, PA.urn as personurn,AE.permissionid,AE.permissionurn,PA.organizationid from personAccounts PA
inner join accountEntitlements AE on AE.accountid = PA.accountid
;

-- select ((EXTRACT(EPOCH FROM AuditResultDate)*1000) - (EXTRACT(EPOCH FROM AuditDate)) * 1000) as PerfInMS from Audit order by AuditResultDate DESC limit 100


-- ***** PG9 INHERITENCE LIMITATION FIX - ADD UNIQUE CONSTRAINTS PER TABLE

CREATE UNIQUE INDEX IdxOrganizationObjId ON organizations(ObjectId);
CREATE UNIQUE INDEX IdxorganizationsName on organizations(Name,ParentId);
CREATE UNIQUE INDEX idxorganizations_urn on organizations(Urn);
CREATE UNIQUE INDEX asymmetrickeys_ObjId ON asymmetrickeys(ObjectId);
CREATE UNIQUE INDEX symmetrickeys_ObjId ON symmetrickeys(ObjectId);
CREATE UNIQUE INDEX idxgroups_urn on groups(Urn);
CREATE UNIQUE INDEX IdxGroupObjId ON groups(ObjectId);
CREATE UNIQUE INDEX IdxgroupsNameParent on groups(Name,ParentId,OrganizationId);
CREATE UNIQUE INDEX idxdata_urn on data(Urn);
CREATE UNIQUE INDEX IdxDataObjId ON data(ObjectId);
CREATE UNIQUE INDEX IdxdataNameGroup on data(Name,GroupId,OrganizationId);
CREATE UNIQUE INDEX IdxdataIdGroup on data(Id,GroupId,OrganizationId);
CREATE UNIQUE INDEX idxaccounts_urn on accounts(Urn);
CREATE UNIQUE INDEX IdxAccountObjId ON accounts(ObjectId);
CREATE UNIQUE INDEX IdxaccountsName on accounts(Name,ParentId,GroupId,OrganizationId);
CREATE UNIQUE INDEX users_urn ON users(Urn);
CREATE UNIQUE INDEX IdxUserObjId ON users(ObjectId);
CREATE UNIQUE INDEX IdxusersName on users(Name,AccountId,OrganizationId);
CREATE UNIQUE INDEX IdxAddressObjId ON addresses(ObjectId);
CREATE UNIQUE INDEX idxaddresses_urn on addresses(Urn);
CREATE UNIQUE INDEX idxcontacts_urn on contacts(Urn);
CREATE UNIQUE INDEX IdxContactsObjId ON contacts(ObjectId);
CREATE UNIQUE INDEX IdxContactInfoObjId ON contactinformation(ObjectId);
CREATE UNIQUE INDEX idxpersons_urn on persons(Urn);
CREATE UNIQUE INDEX IdxPersonObjId ON persons(ObjectId);
CREATE UNIQUE INDEX persons_name ON persons(Name,ParentId,GroupId,OrganizationId);
CREATE UNIQUE INDEX IdxRoleObjId ON roles(ObjectId);
CREATE UNIQUE INDEX idxroles_urn ON roles(Urn);
CREATE UNIQUE INDEX IdxPermissionObjId ON permissions(ObjectId);
CREATE UNIQUE INDEX idxpermissions_urn ON permissions(Urn);
CREATE UNIQUE INDEX idxtags_urn on tags(Urn);
CREATE UNIQUE INDEX IdxTagObjId ON tags(ObjectId);
CREATE UNIQUE INDEX fact_id ON fact(Id);
CREATE UNIQUE INDEX IdxFactObjId ON fact(ObjectId);
CREATE UNIQUE INDEX IdxfactNameGroup on fact(Name,GroupId,OrganizationId);
CREATE UNIQUE INDEX IdxfactUrnGroup on fact(Urn);
CREATE UNIQUE INDEX functionfact_id ON functionfact(Id);
CREATE UNIQUE INDEX IdxFunFactObjId ON functionfact(ObjectId);
CREATE UNIQUE INDEX IdxfunctionfactNameGroup on functionfact(Name,GroupId,OrganizationId);
CREATE UNIQUE INDEX IdxfunctionfactUrnGroup on functionfact(Urn);
CREATE UNIQUE INDEX function_id ON function(Id);
CREATE UNIQUE INDEX IdxFunctionObjId ON function(ObjectId);
CREATE UNIQUE INDEX IdxfunctionNameGroup on function(Name,GroupId,OrganizationId);
CREATE UNIQUE INDEX IdxfunctionUrnGroup on function(Urn);
CREATE UNIQUE INDEX functionparticipation_id ON functionparticipation(Id);
CREATE UNIQUE INDEX IdxfunctionparticipationCbo on functionparticipation(ParticipationId,ParticipantId,ParticipantType,AffectId,OrganizationId);
CREATE UNIQUE INDEX operation_id ON operation(Id);
CREATE UNIQUE INDEX IdxOperationObjId ON operation(ObjectId);
CREATE UNIQUE INDEX IdxoperationNameGroup on operation(Name,GroupId,OrganizationId);
CREATE UNIQUE INDEX IdxoperationUrnGroup on operation(Urn);
CREATE UNIQUE INDEX pattern_id ON pattern(Id);
CREATE UNIQUE INDEX IdxPatternObjId ON pattern(ObjectId);
CREATE UNIQUE INDEX IdxpatternNameGroup on pattern(Name,GroupId,OrganizationId);
CREATE UNIQUE INDEX IdxpatternUrnGroup on pattern(Urn);
CREATE UNIQUE INDEX policy_id ON policy(Id);
CREATE UNIQUE INDEX IdxPolicyObjId ON policy(ObjectId);
CREATE UNIQUE INDEX IdxpolicyNameGroup on policy(Name,GroupId,OrganizationId);
CREATE UNIQUE INDEX IdxpolicyUrnGroup on policy(Urn);
CREATE UNIQUE INDEX policyparticipation_id ON policyparticipation(Id);
CREATE UNIQUE INDEX IdxpolicyparticipationCbo on policyparticipation(ParticipationId,ParticipantId,ParticipantType,AffectId,OrganizationId);
CREATE UNIQUE INDEX rule_id ON rule(Id);
CREATE UNIQUE INDEX IdxRuleObjId ON rule(ObjectId);
CREATE UNIQUE INDEX IdxruleNameGroup on rule(Name,GroupId,OrganizationId);
CREATE UNIQUE INDEX IdxruleUrnGroup on rule(Urn);
CREATE UNIQUE INDEX IdxruleparticipationCbo on ruleparticipation(ParticipationId,ParticipantId,ParticipantType,AffectId,OrganizationId);
CREATE UNIQUE INDEX IdxCredentialObjId ON credential(ObjectId);
CREATE UNIQUE INDEX IdxControlReference on control(ControlType,ControlId,ControlAction,ReferenceId,ReferenceType);
CREATE UNIQUE INDEX IdxControlObjId ON control(ObjectId);
CREATE UNIQUE INDEX IdxApproverEntReference on approver(ApproverId, ApproverType, ApprovalType, ReferenceId,ReferenceType,EntitlementId,EntitlementType);
CREATE UNIQUE INDEX IdxApproverObjId ON approver(ObjectId);

CREATE INDEX IdxRequestReference on request(ParentId,RequestorType, RequestorId);
CREATE UNIQUE INDEX IdxRequestObjId ON request(ObjectId);

CREATE INDEX IdxApprovalReference on approval(ApproverId, ApproverType);
CREATE INDEX IdxApprovalRequest on approval(Response, RequestId);
CREATE UNIQUE INDEX IdxApprovalObjId ON approval(ObjectId);
CREATE UNIQUE INDEX IdxApprovalReqId ON approval(RequestId, ApprovalType, ApproverType, ApproverId, ApproverLevel);

CREATE INDEX idxattributerefid_Id ON attribute(ReferenceId,ReferenceType,OrganizationId);
CREATE INDEX idxattributeval on attribute(value);
CREATE INDEX asymmetrickeys_OwnId ON asymmetrickeys(OwnerId);
CREATE INDEX symmetrickeys_OrgId ON symmetrickeys(OwnerId);
CREATE INDEX groups_group_name ON groups(Name,OrganizationId);
CREATE UNIQUE INDEX groupparticipation_id ON groupparticipation(Id);
CREATE INDEX groupparticipation_pid ON groupparticipation(ParticipationId);
CREATE UNIQUE INDEX IdxgroupparticipationCbo on groupparticipation(ParticipationId,ParticipantId,ParticipantType,AffectId,OrganizationId);
CREATE INDEX grouprolecache_id ON grouprolecache(objectId);
CREATE INDEX grouprolecache_role_id ON grouprolecache(EffectiveRoleId);
CREATE INDEX grouprolecache_aff_id ON grouprolecache(AffectType,AffectId);
CREATE INDEX grouprolecache_dorg ON grouprolecache(objectId,OrganizationId);
CREATE INDEX dataptype_pid ON dataparticipation(ParticipantType);
CREATE INDEX dataparticipation_pid ON dataparticipation(ParticipationId);
CREATE INDEX dataparticipant_pid ON dataparticipation(ParticipantId);
CREATE INDEX datarolecache_id ON datarolecache(objectId);
CREATE INDEX datarolecache_role_id ON datarolecache(EffectiveRoleId);
CREATE INDEX datarolecache_aff_id ON datarolecache(AffectType,AffectId);
CREATE INDEX datarolecache_dorg ON datarolecache(objectId,OrganizationId);
CREATE INDEX permissionrolecache_id ON permissionrolecache(objectId);
CREATE INDEX permissionrolecache_role_id ON permissionrolecache(EffectiveRoleId);
CREATE INDEX permissionrolecache_aff_id ON permissionrolecache(AffectType,AffectId);
CREATE INDEX permissionrolecache_dorg ON permissionrolecache(objectId,OrganizationId);

CREATE INDEX accounts_org_id ON accounts(OrganizationId);
CREATE INDEX accountrolecache_id ON accountrolecache(ObjectId);
CREATE INDEX accountrolecache_role_id ON accountrolecache(EffectiveRoleId);
CREATE INDEX accountrolecache_uorg_id ON accountrolecache(ObjectId,OrganizationId);
CREATE INDEX users_org_id ON users(OrganizationId);
CREATE INDEX userrolecache_id ON userrolecache(objectId);
CREATE INDEX userrolecache_role_id ON userrolecache(EffectiveRoleId);
CREATE INDEX userrolecache_uorg_id ON userrolecache(objectId,OrganizationId);
CREATE INDEX contactinformation_type ON contactinformation(ContactInformationType);
CREATE INDEX contactinformationparticipation_pid ON contactinformationparticipation(ParticipationId);
CREATE INDEX contactinformationparticipant_pid ON contactinformationparticipation(ParticipantId);
CREATE INDEX contactinformationptype_pid ON contactinformationparticipation(ParticipantType);
CREATE INDEX persons_group_id ON persons(groupId);
CREATE INDEX personrolecache_id ON personrolecache(ObjectId);
CREATE INDEX personrolecache_role_id ON personrolecache(EffectiveRoleId);
CREATE INDEX personrolecache_org_id ON personrolecache(ObjectId,OrganizationId);
CREATE INDEX personparticipationtype_pid ON personparticipation(ParticipationId,ParticipantType);
CREATE INDEX personparticipation_pid ON personparticipation(ParticipationId);
CREATE INDEX personparticipant_pid ON personparticipation(ParticipantId);
CREATE INDEX personptype_pid ON personparticipation(ParticipantType);
CREATE INDEX roles_parent_id ON roles(ParentId);
CREATE INDEX roleparticipation_parttype ON roleparticipation(ParticipantId,ParticipantType,AffectId,AffectType);
CREATE INDEX roleparticipation_pid ON roleparticipation(ParticipationId);
CREATE INDEX roleparticipant_pid ON roleparticipation(ParticipantId);
CREATE INDEX roleptype_pid ON roleparticipation(ParticipantType);
CREATE INDEX rolerolecache_id ON rolerolecache(objectId);
CREATE INDEX rolerolecache_role_id ON rolerolecache(EffectiveRoleId);
CREATE INDEX rolerolecache_aff_id ON rolerolecache(AffectType,AffectId);
CREATE INDEX rolerolecache_dorg ON rolerolecache(objectId,OrganizationId);
CREATE INDEX idxpermissionsparent_id ON roles(ParentId);
CREATE INDEX tagparticipation_parttype ON tagparticipation(ParticipantId,ParticipantType);
CREATE INDEX tagparticipation_pid ON tagparticipation(ParticipationId);
CREATE INDEX IdxfactIdGroup on fact(Id,OrganizationId);
CREATE INDEX IdxfunctionfactIdGroup on functionfact(Id,OrganizationId);
CREATE INDEX IdxfunctionIdGroup on function(Id,OrganizationId);
CREATE INDEX functionparticipation_parttype ON functionparticipation(ParticipantId,ParticipantType,AffectId);
CREATE INDEX functionparticipation_pid ON functionparticipation(ParticipationId);
CREATE INDEX IdxoperationIdGroup on operation(Id,OrganizationId);
CREATE INDEX IdxpatternIdGroup on pattern(Id,OrganizationId);
CREATE INDEX IdxpolicyIdGroup on policy(Id,OrganizationId);
CREATE INDEX policyparticipation_parttype ON policyparticipation(ParticipantId,ParticipantType,AffectId);
CREATE INDEX policyparticipation_pid ON policyparticipation(ParticipationId);
CREATE INDEX IdxruleIdGroup on rule(Id,OrganizationId);
CREATE INDEX ruleparticipation_parttype ON ruleparticipation(ParticipantId,ParticipantType,AffectId);
CREATE INDEX ruleparticipation_pid ON ruleparticipation(ParticipationId);
CREATE INDEX IdxCredentialReference on credential(ReferenceId,ReferenceType);




