				DROP TABLE IF EXISTS organizations CASCADE;
				DROP SEQUENCE IF EXISTS organizations_id_seq;
				CREATE SEQUENCE organizations_id_seq;
				CREATE TABLE organizations (
						Id bigint not null default nextval('organizations_id_seq'),
						OwnerId bigint not null default 0,
						Name varchar(128) not null,
						OrganizationType varchar(16) not null,
						ParentId bigint not null default 0,
						ReferenceId bigint not null default 0,
						LogicalId bigint not null default 0,
						primary key(Id)
					);
				CREATE UNIQUE INDEX organizations_Id ON organizations(Id);
			  	CREATE UNIQUE INDEX IdxorganizationsName on organizations(Name,ParentId);

				DROP TABLE IF EXISTS asymmetrickeys CASCADE;
				DROP SEQUENCE IF EXISTS asymmetrickeys_id_seq;
				CREATE SEQUENCE asymmetrickeys_id_seq;
				CREATE TABLE asymmetrickeys (
					Id bigint not null default nextval('asymmetrickeys_id_seq'),
					OrganizationId bigint not null default 0,
					PublicKey bytea,
					PrivateKey bytea,
					SymmetricKeyId bigint not null default 0,
					primary key(Id)
				);
				
				CREATE UNIQUE INDEX asymmetrickeys_Id on asymmetrickeys(Id);
				CREATE INDEX asymmetrickeys_OrgId ON asymmetrickeys(OrganizationId);

				DROP TABLE IF EXISTS symmetrickeys CASCADE;
				DROP SEQUENCE IF EXISTS symmetrickeys_id_seq;
				CREATE SEQUENCE symmetrickeys_id_seq;
				CREATE TABLE symmetrickeys (
					Id bigint not null default nextval('symmetrickeys_id_seq'),
					OrganizationId bigint not null,
					CipherKey bytea,
					CipherIV bytea,
					AsymmetricKeyId bigint not null default 0,
					primary key(Id)
				);
				CREATE UNIQUE INDEX symmetrickeys_Id on symmetrickeys(Id);
				CREATE INDEX symmetrickeys_OrgId ON symmetrickeys(OrganizationId);
				
				DROP TABLE IF EXISTS groups CASCADE;
				DROP SEQUENCE IF EXISTS groups_id_seq;
				CREATE SEQUENCE groups_id_seq;
				CREATE TABLE groups (
						Id bigint not null default nextval('groups_id_seq'),
						OwnerId bigint not null default 0,
						Name varchar(255) not null,
						GroupType varchar(16) not null,
						ParentId bigint not null default 0,
						ReferenceId bigint not null default 0,
						OrganizationId bigint not null default 0,
						primary key(Id)
					);

				CREATE UNIQUE INDEX groups_group_id ON groups(Id);
				CREATE INDEX groups_group_name ON groups(Name,OrganizationId);
				CREATE UNIQUE INDEX IdxgroupsNameParent on groups(Name,ParentId,OrganizationId);



				DROP TABLE IF EXISTS groupparticipation CASCADE;
				DROP SEQUENCE IF EXISTS groupparticipation_id_seq;
				CREATE SEQUENCE groupparticipation_id_seq;
				CREATE TABLE groupparticipation (
						Id bigint not null default nextval('groupparticipation_id_seq'),
						OwnerId bigint not null,
						ParticipationId bigint not null default 0,
						ParticipantType varchar(16) not null,
						ParticipantId bigint not null default 0,
						AffectType varchar(16) not null,
						AffectId bigint not null default 0,
						OrganizationId bigint not null default 0,
						primary key(Id)
					);
				CREATE UNIQUE INDEX groupparticipation_id ON groupparticipation(Id);
				--CREATE INDEX groupparticipation_parttype ON groupparticipation(ParticipantId,ParticipantType,AffectId);
				CREATE INDEX groupparticipation_pid ON groupparticipation(ParticipationId);
				CREATE INDEX groupparticipant_pid ON groupparticipation(ParticipantId);
				CREATE INDEX groupptype_pid ON groupparticipation(ParticipantType);
				CREATE UNIQUE INDEX IdxgroupparticipationCbo on groupparticipation(ParticipationId,ParticipantId,ParticipantType,AffectId,OrganizationId);


				DROP TABLE IF EXISTS grouprolecache CASCADE;
				CREATE TABLE grouprolecache (
						GroupId bigint not null default 0,
						AffectType varchar(16) not null,
						AffectId bigint not null default 0,
						EffectiveRoleId bigint not null default 0,
						BaseRoleId bigint not null default 0,
					     OrganizationId bigint not null default 0
					);
		
				CREATE INDEX grouprolecache_id ON grouprolecache(groupId);
				CREATE INDEX grouprolecache_role_id ON grouprolecache(EffectiveRoleId);
				CREATE INDEX grouprolecache_aff_id ON grouprolecache(AffectType,AffectId);
				CREATE INDEX grouprolecache_dorg ON grouprolecache(groupId,OrganizationId);


				DROP TABLE IF EXISTS data CASCADE;
				DROP SEQUENCE IF EXISTS data_id_seq;
				CREATE SEQUENCE data_id_seq;
				create table data (
					Id bigint not null default nextval('data_id_seq'),
					OwnerId bigint not null default 0,
					Name varchar(255) not null,
					MimeType varchar(32) not null,
					VaultId varchar(64),
					KeyId varchar(64),
					IsVaulted boolean not null default false,
					IsEnciphered boolean not null default false,
					IsPasswordProtected boolean not null default false,
					IsCompressed boolean not null default false,
					CompressionType varchar(16) not null,
					Description varchar(255),
					Dimensions varchar(9),
					Size int not null default 0,
					Rating double precision not null default 0,
					IsPointer boolean not null default false,
					Hash varchar(64),
					GroupId bigint not null,
					CreatedDate timestamp not null,
					ModifiedDate timestamp not null,
					ExpirationDate timestamp not null,
					IsBlob boolean not null,
					DataBlob bytea,
					DataString varchar(255),
					OrganizationId bigint not null default 0,
					primary key(Id)
				);
				CREATE UNIQUE INDEX data_id ON data(Id);
				CREATE INDEX data_Name ON data(Name);
				CREATE UNIQUE INDEX IdxdataNameGroup on data(Name,GroupId,OrganizationId);
				CREATE UNIQUE INDEX IdxdataIdGroup on data(Id,GroupId,OrganizationId);

				DROP TABLE IF EXISTS dataparticipation CASCADE;
				DROP SEQUENCE IF EXISTS dataparticipation_id_seq;
				CREATE SEQUENCE dataparticipation_id_seq;
				CREATE TABLE dataparticipation (
						Id bigint not null default nextval('dataparticipation_id_seq'),
						OwnerId bigint not null,
						ParticipationId bigint not null default 0,
						ParticipantType varchar(16) not null,
						ParticipantId bigint not null default 0,
						AffectType varchar(16) not null,
						AffectId bigint not null default 0,
						OrganizationId bigint not null default 0,
						primary key(Id)
					);
				CREATE UNIQUE INDEX dataparticipation_id ON dataparticipation(Id);
				--CREATE INDEX dataparticipation_parttype ON dataparticipation(ParticipantId,ParticipantType,AffectId);
				CREATE INDEX dataptype_pid ON dataparticipation(ParticipantType);				
				CREATE INDEX dataparticipation_pid ON dataparticipation(ParticipationId);
				CREATE INDEX dataparticipant_pid ON dataparticipation(ParticipantId);
				CREATE UNIQUE INDEX IdxdataparticipationCbo on dataparticipation(ParticipationId,ParticipantId,ParticipantType,AffectId,OrganizationId);



				DROP TABLE IF EXISTS datarolecache CASCADE;
				CREATE TABLE datarolecache (
						DataId bigint not null default 0,
						AffectType varchar(16) not null,
						AffectId bigint not null default 0,
						EffectiveRoleId bigint not null default 0,
						BaseRoleId bigint not null default 0,
					     OrganizationId bigint not null default 0
					);
		
				CREATE INDEX datarolecache_id ON datarolecache(DataId);
				CREATE INDEX datarolecache_role_id ON datarolecache(EffectiveRoleId);
				CREATE INDEX datarolecache_aff_id ON datarolecache(AffectType,AffectId);
				CREATE INDEX datarolecache_dorg ON datarolecache(DataId,OrganizationId);

				DROP TABLE IF EXISTS accounts CASCADE;
				DROP SEQUENCE IF EXISTS accounts_id_seq;
				CREATE SEQUENCE accounts_id_seq;
				create table accounts (
					Id bigint not null default nextval('accounts_id_seq'),
					AccountId varchar(64) not null,
					ReferenceId bigint not null default 0,
					OrganizationId bigint not null default 0,
					OwnerId bigint not null default 0,
					ParentId bigint not null default 0,
					Name varchar(128) not null,
					AccountStatus varchar(16) not null,
					AccountType varchar(16) not null,
					primary key(Id)
				);
				CREATE UNIQUE INDEX accounts_acct_id ON accounts(Id);
				CREATE INDEX accounts_org_id ON accounts(OrganizationId);
				CREATE UNIQUE INDEX IdxaccountsName on accounts(Name,ParentId,OrganizationId);


				DROP TABLE IF EXISTS users CASCADE;
				DROP SEQUENCE IF EXISTS users_id_seq;
				CREATE SEQUENCE users_id_seq;
				create table users (
					Id bigint not null default nextval('users_id_seq'),
					OrganizationId bigint not null default 0,
					AccountId bigint not null default 0,
					UserId varchar(64) not null,
					Name varchar(128) not null,
					Password varchar(128),
					UserStatus varchar(16) not null,
					UserType varchar(16) not null,
					primary key(Id)
				);
				CREATE UNIQUE INDEX users_acct_id ON users(Id);
				CREATE INDEX users_org_id ON users(OrganizationId);
				CREATE UNIQUE INDEX IdxusersName on users(Name,OrganizationId);

				DROP TABLE IF EXISTS userrolecache CASCADE;
				CREATE TABLE userrolecache (
						UserId bigint not null default 0,
						EffectiveRoleId bigint not null default 0,
						BaseRoleId bigint not null default 0,
						OrganizationId bigint not null default 0
					);
				CREATE INDEX userrolecache_id ON userrolecache(UserId);
				CREATE INDEX userrolecache_role_id ON userrolecache(EffectiveRoleId);
				CREATE INDEX userrolecache_uorg_id ON userrolecache(UserId,OrganizationId);

				DROP TABLE IF EXISTS statistics CASCADE;
				DROP SEQUENCE IF EXISTS statistics_id_seq;
				CREATE SEQUENCE statistics_id_seq;
				create table statistics (
						Id bigint not null default nextval('statistics_id_seq'),
						ReferenceId bigint not null default 0,
						StatisticsType varchar(16) not null,
						CreatedDate timestamp not null,
						AccessedDate timestamp not null,
						ModifiedDate timestamp not null,
						ExpirationDate timestamp not null,
						OrganizationId bigint not null default 0,
						primary key(Id)
				);
				CREATE UNIQUE INDEX statistics_acct_id ON statistics(Id);
				CREATE UNIQUE INDEX IdxstatisticsRefOrg on statistics(ReferenceId,StatisticsType,OrganizationId);



				DROP TABLE IF EXISTS addresses CASCADE;
				DROP SEQUENCE IF EXISTS addresses_id_seq;
				CREATE SEQUENCE addresses_id_seq;
				create table addresses (
						Id bigint not null default nextval('addresses_id_seq'),
						Name varchar(255) not null,
						GroupId bigint not null,
						Description varchar(255),
						Preferred boolean not null default false,
						LocationType varchar(16) not null,
						AddressLine1 varchar(255),
						AddressLine2 varchar(255),
						City varchar(255),
						State varchar(255),
						Region varchar(255),
						PostalCode varchar(255),
						Country varchar(255),
						OwnerId bigint not null,
						OrganizationId bigint not null default 0,
						primary key(Id)
				);
				CREATE UNIQUE INDEX addresses_acct_id ON addresses(Id);
				CREATE UNIQUE INDEX addresses_reftype ON addresses(Name,LocationType,GroupId,OrganizationId);

				DROP TABLE IF EXISTS contacts CASCADE;
				DROP SEQUENCE IF EXISTS contacts_id_seq;
				CREATE SEQUENCE contacts_id_seq;
				create table contacts (
						Id bigint not null default nextval('contacts_id_seq'),
						Name varchar(255) not null,
						GroupId bigint not null,
						Description varchar(255),
						Preferred boolean not null default false,
						ContactType varchar(16) not null,
						LocationType varchar(16) not null,
						ContactValue varchar(255),
						OwnerId bigint not null,
						OrganizationId bigint not null default 0,
						primary key(Id)
				);
				CREATE UNIQUE INDEX contacts_acct_id ON contacts(Id);
				CREATE UNIQUE INDEX contacts_reftype ON contacts(Name,ContactType,LocationType,GroupId,OrganizationId);




				DROP TABLE IF EXISTS contactinformation CASCADE;
				DROP SEQUENCE IF EXISTS contactinformation_id_seq;
				CREATE SEQUENCE contactinformation_id_seq;
				create table contactinformation (
						Id bigint not null default nextval('contactinformation_id_seq'),
						ReferenceId bigint not null,
						Description varchar(255),
						ContactInformationType varchar(16) not null,
						OwnerId bigint not null,
					      OrganizationId bigint not null default 0,
						primary key(Id)
				);
				CREATE UNIQUE INDEX contactinformation_acct_id ON contactinformation(Id);
				CREATE UNIQUE INDEX contactinformation_reftype ON contactinformation(ReferenceId,ContactInformationType,OrganizationId);

				DROP TABLE IF EXISTS contactinformationparticipation CASCADE;
				DROP SEQUENCE IF EXISTS contactinformationparticipation_id_seq;
				CREATE SEQUENCE contactinformationparticipation_id_seq;
				CREATE TABLE contactinformationparticipation (
						Id bigint not null default nextval('contactinformationparticipation_id_seq'),
						OwnerId bigint not null,
						ParticipationId bigint not null default 0,
						ParticipantType varchar(16) not null default 0,
						ParticipantId bigint not null default 0,
						AffectType varchar(16) not null default 0,
						AffectId bigint not null default 0,
						OrganizationId bigint not null default 0,
						primary key(Id)
					);
				CREATE UNIQUE INDEX contactinformationparticipation_id ON contactinformationparticipation(Id);
				CREATE INDEX contactinformationparticipation_parttype ON contactinformationparticipation(ParticipantId,ParticipantType,AffectId,AffectType);
				CREATE INDEX contactinformationparticipation_pid ON contactinformationparticipation(ParticipationId);
				CREATE INDEX contactinformationparticipant_pid ON contactinformationparticipation(ParticipantId);
				CREATE INDEX contactinformationptype_pid ON contactinformationparticipation(ParticipantType);
				CREATE UNIQUE INDEX IdxcontactinformationparticipationCbo on contactinformationparticipation(ParticipationId,ParticipantId,ParticipantType,AffectId,AffectType,OrganizationId);


				DROP TABLE IF EXISTS persons CASCADE;
				DROP SEQUENCE IF EXISTS persons_id_seq;
				CREATE SEQUENCE persons_id_seq;
				create table persons (
						Id bigint not null default nextval('persons_id_seq'),
						OwnerId bigint not null default 0,
						Name varchar(128) not null,
						ParentId bigint not null default 0,
						GroupId bigint not null default 0,
						OrganizationId bigint not null default 0,
						ContactInformationId bigint not null default 0,
						Title varchar(128),
						Prefix varchar(64),
						Suffix varchar(64),
						Description varchar(255),
						FirstName varchar(128),
						MiddleName varchar(128),
						LastName varchar(128),
						Alias varchar(64),
						BirthDate timestamp not null,
						Gender varchar(16) default 'UNKNOWN',

						primary key(Id)
				);
				CREATE UNIQUE INDEX persons_person_id ON persons(Id);
				CREATE INDEX persons_parent_id ON persons(ParentId);				
				CREATE UNIQUE INDEX persons_name ON persons(Name,ParentId,GroupId,OrganizationId);

				DROP TABLE IF EXISTS personparticipation CASCADE;
				DROP SEQUENCE IF EXISTS personparticipation_id_seq;
				CREATE SEQUENCE personparticipation_id_seq;
				CREATE TABLE personparticipation (
						Id bigint not null default nextval('personparticipation_id_seq'),
						OwnerId bigint not null,
						ParticipationId bigint not null default 0,
						ParticipantType varchar(16) not null default 0,
						ParticipantId bigint not null default 0,
						AffectType varchar(16) not null default 0,
						AffectId bigint not null default 0,
						OrganizationId bigint not null default 0,
						primary key(Id)
					);
				CREATE UNIQUE INDEX personparticipation_id ON personparticipation(Id);
				CREATE INDEX personparticipation_parttype ON personparticipation(ParticipantId,ParticipantType,AffectId,AffectType);
				CREATE INDEX personparticipation_pid ON personparticipation(ParticipationId);
				CREATE INDEX personparticipant_pid ON personparticipation(ParticipantId);
				CREATE INDEX personptype_pid ON personparticipation(ParticipantType);
				CREATE UNIQUE INDEX IdxpersonparticipationCbo on personparticipation(ParticipationId,ParticipantId,ParticipantType,AffectId,AffectType,OrganizationId);


				DROP TABLE IF EXISTS roles CASCADE;
				DROP SEQUENCE IF EXISTS roles_id_seq;
				CREATE SEQUENCE roles_id_seq;
				create table roles (
						Id bigint not null default nextval('roles_id_seq'),
						OwnerId bigint not null default 0,
						Name varchar(128) not null,
						RoleType varchar(16) not null default 0,
						ParentId bigint not null default 0,
						ReferenceId bigint not null default 0,
						OrganizationId bigint not null default 0,
						primary key(Id)
				);
				CREATE UNIQUE INDEX roles_role_id ON roles(Id);
				CREATE INDEX roles_parent_id ON roles(ParentId);				
				CREATE UNIQUE INDEX roles_name ON roles(Name,OwnerId,ParentId,RoleType,OrganizationId);
				
				DROP TABLE IF EXISTS roleparticipation CASCADE;
				DROP SEQUENCE IF EXISTS roleparticipation_id_seq;
				CREATE SEQUENCE roleparticipation_id_seq;
				CREATE TABLE roleparticipation (
						Id bigint not null default nextval('roleparticipation_id_seq'),
						OwnerId bigint not null,
						ParticipationId bigint not null default 0,
						ParticipantType varchar(16) not null default 0,
						ParticipantId bigint not null default 0,
						AffectType varchar(16) not null default 0,
						AffectId bigint not null default 0,
						OrganizationId bigint not null default 0,
						primary key(Id)
					);
				CREATE UNIQUE INDEX roleparticipation_id ON roleparticipation(Id);
				CREATE INDEX roleparticipation_parttype ON roleparticipation(ParticipantId,ParticipantType,AffectId,AffectType);
				CREATE INDEX roleparticipation_pid ON roleparticipation(ParticipationId);
				CREATE INDEX roleparticipant_pid ON roleparticipation(ParticipantId);
				CREATE INDEX roleptype_pid ON roleparticipation(ParticipantType);
				CREATE UNIQUE INDEX IdxroleparticipationCbo on roleparticipation(ParticipationId,ParticipantId,ParticipantType,AffectId,AffectType,OrganizationId);


				DROP TABLE IF EXISTS rolerolecache CASCADE;
				CREATE TABLE rolerolecache (
						roleId bigint not null default 0,
						AffectType varchar(16) not null,
						AffectId bigint not null default 0,
						EffectiveRoleId bigint not null default 0,
						BaseRoleId bigint not null default 0,
					     OrganizationId bigint not null default 0
					);
		
				CREATE INDEX rolerolecache_id ON rolerolecache(roleId);
				CREATE INDEX rolerolecache_role_id ON rolerolecache(EffectiveRoleId);
				CREATE INDEX rolerolecache_aff_id ON rolerolecache(AffectType,AffectId);
				CREATE INDEX rolerolecache_dorg ON rolerolecache(roleId,OrganizationId);

				DROP TABLE IF EXISTS permissions CASCADE;
				DROP SEQUENCE IF EXISTS permissions_id_seq;
				CREATE SEQUENCE permissions_id_seq;
				create table permissions (
						Id bigint not null default nextval('permissions_id_seq'),
						OwnerId bigint not null,
						PermissionType varchar(16) not null,
						Name varchar(128) not null,
						ParentId bigint not null default 0,
						ReferenceId bigint not null default 0,
						OrganizationId bigint not null default 0,
						primary key(Id)
					);
				CREATE UNIQUE INDEX permissions_permission_id ON permissions(Id);
				CREATE UNIQUE INDEX IdxpermissionsName on permissions(Name,OrganizationId);

				DROP TABLE IF EXISTS tags CASCADE;
				DROP SEQUENCE IF EXISTS tags_id_seq;
				CREATE SEQUENCE tags_id_seq;
				CREATE TABLE tags (
						Id bigint not null default nextval('tags_id_seq'),
						OwnerId bigint not null,
						TagType varchar(16) not null default 0,
						Name varchar(255) not null,
						OrganizationId bigint not null default 0,
						primary key(Id)
					);
				CREATE UNIQUE INDEX tags_id ON tags(Id);
				CREATE UNIQUE INDEX IdxtagsName on tags(Name,TagType,OrganizationId);

				DROP TABLE IF EXISTS tagparticipation CASCADE;
				DROP SEQUENCE IF EXISTS tagparticipation_id_seq;
				CREATE SEQUENCE tagparticipation_id_seq;
				CREATE TABLE tagparticipation (
						Id bigint not null default nextval('tagparticipation_id_seq'),
						OwnerId bigint not null,
						ParticipationId bigint not null default 0,
						ParticipantType varchar(16) not null default 0,
						ParticipantId bigint not null default 0,
						OrganizationId bigint not null default 0,
						primary key(Id)
					);
				CREATE UNIQUE INDEX tagparticipation_id ON tagparticipation(Id);
				CREATE INDEX tagparticipation_parttype ON tagparticipation(ParticipantId,ParticipantType);
				CREATE INDEX tagparticipation_pid ON tagparticipation(ParticipationId);
				CREATE UNIQUE INDEX IdxtagparticipationCbo on tagparticipation(ParticipationId,ParticipantId,ParticipantType,OrganizationId);

				DROP TABLE IF EXISTS spool CASCADE;
				CREATE TABLE spool (
					SpoolGuid varchar(42) not null,
					SpoolBucketName varchar(64) not null,
					SpoolBucketType varchar(16) not null default 0,
					SpoolOwnerId bigint not null default 0,
					SpoolCreated timestamp not null,
					SpoolExpiration timestamp not null,
					SpoolExpires boolean not null default false,
					SpoolValueType varchar(16) not null,
					SpoolName varchar(64) not null,
					SpoolData varchar(255),
					SpoolStatus bigint not null default 0,
					GroupId bigint not null default 0,
					OrganizationId bigint not null default 0
				);
				CREATE UNIQUE INDEX spool_spool_guid ON spool(SpoolGuid);
				CREATE INDEX spool_spool_expiry ON spool(SpoolExpires,SpoolExpiration);
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
					AuditSourceData varchar(255),
					AuditTargetData varchar(255),
					AuditResultData varchar(255),
					AuditActionSource varchar(255)
				);
				CREATE UNIQUE INDEX audit_id ON audit(Id);
				CREATE INDEX audit_source ON audit(AuditSourceType,AuditSourceData);
				CREATE INDEX audit_target ON audit(AuditTargetType,AuditTargetData);
				CREATE INDEX audit_retention ON audit(AuditRetentionType);
				CREATE INDEX audit_exptype on audit(AuditExpiresDate,AuditRetentionType);



create or replace view roleUserRights as
select distinct U.id as userid,R.id as roleid, R.name as RoleName, R.ownerid as roleownerid,R.organizationid,
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
select distinct U.id as userid,G.id as groupid, G.name as GroupName, G.ownerid as groupownerid,G.grouptype,G.parentId,G.organizationid,
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
select distinct U.id as userid,D.id as dataid, D.name as DataName, D.ownerid as dataownerid,G.organizationid,
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


--- Accumulate permissions moving up from the leaf 
--- Note: The views must reference roles -> roleid and participation -> leafed
CREATE OR REPLACE FUNCTION roles_from_leaf(root_id BIGINT,organizationid BIGINT) 
        RETURNS TABLE (leafid BIGINT,roleid BIGINT, parentid BIGINT, organizationid BIGINT)
        AS $$
	WITH RECURSIVE role_tree(leafed,roleid, parentid, organizationid) AS (
	   SELECT $1 as leafid, R.id as roleid, R.parentid, R.organizationid
	      FROM roles R WHERE R.id = $1 AND R.organizationid = $2
	   UNION ALL
	   SELECT $1 as leafid, P.id, P.parentid, P.organizationid
	      FROM role_tree RT, roles P
	      WHERE RT.roleid = P.parentid AND RT.organizationid = $2
	)
	select * from role_tree;
        $$ LANGUAGE 'sql';

--- Accumulate permissions for roles moving down from the leaf/trunk (reverse rbac)
--- Note: The views must reference roles -> roleid and participation -> leafed
CREATE OR REPLACE FUNCTION roles_to_leaf(root_id BIGINT,organizationid BIGINT) 
        RETURNS TABLE (leafid BIGINT,roleid BIGINT, parentid BIGINT, organizationid BIGINT)
        AS $$
	WITH RECURSIVE role_tree(leafed,roleid, parentid, organizationid) AS (
	   SELECT $1 as leafid, R.id as roleid, R.parentid, R.organizationid
	      FROM roles R WHERE R.id = $1 AND R.organizationid = $2
	   UNION ALL
	   SELECT $1 as leafid, P.id, P.parentid, P.organizationid
	      FROM role_tree RT, roles P
	      WHERE RT.parentid = P.id AND RT.organizationid = $2
	)
	select * from role_tree;
        $$ LANGUAGE 'sql';


create or replace view effectiveDataRoles as
WITH result AS(
select R.id,R.parentid,roles_from_leaf(R.id) ats,R.organizationid
FROM roles R  WHERE roletype = 'USER'
)
select DP.participationid as dataid,(R.ats).leafid as effectiveRoleId,(R.ats).roleid as baseRoleId,DP.affectType,DP.affectId,R.organizationid from result R
JOIN dataparticipation DP ON DP.participantid = (R.ats).roleid and DP.participanttype = 'ROLE'
;

create or replace view effectiveGroupRoles as
WITH result AS(
select R.id,R.parentid,roles_from_leaf(R.id) ats,R.organizationid
FROM roles R  WHERE roletype = 'USER'
)
select GP.participationid as groupid,(R.ats).leafid as effectiveRoleId,(R.ats).roleid as baseRoleId,GP.affectType, GP.affectId, R.organizationid from result R
JOIN groupparticipation GP ON GP.participantid = (R.ats).roleid and GP.participanttype = 'ROLE'
;

create or replace view effectiveRoleRoles as
WITH result AS(
select R.id,R.parentid,roles_from_leaf(R.id) ats,R.organizationid
FROM roles R  WHERE roletype = 'USER'
)
select RP.participationid as roleid,(R.ats).leafid as effectiveRoleId,(R.ats).roleid as baseRoleId,RP.affectType, RP.affectId,R.organizationid from result R
JOIN roleparticipation RP ON RP.participantid = (R.ats).roleid and RP.participanttype = 'ROLE'
;

create or replace view effectiveUserRoles as
WITH result AS(
select R.id,R.parentid,roles_from_leaf(R.id) ats,R.organizationid
FROM roles R  WHERE roletype = 'USER'
)
select CASE WHEN RP.participanttype = 'USER' THEN U1.id WHEN RP.participanttype = 'GROUP' THEN U2.id ELSE -1 END as userid,(R.ats).leafid as effectiveRoleId,(R.ats).roleid as baseRoleId,R.organizationid from result R
JOIN roleparticipation RP ON RP.participationid = (R.ats).roleid
--JOIN roles R1 ON R1.id=(R.ats).leafid
LEFT JOIN users U1 on U1.id = RP.participantid and RP.participanttype = 'USER'
-- allow for cross-org
--  and U1.organizationid = R.organizationid
LEFT JOIN groupparticipation gp2 on gp2.participationid = RP.participantid and RP.participanttype = 'GROUP' and gp2.participanttype = 'USER'
-- allow for cross-org
-- and gp2.organizationid = RP.organizationid
LEFT JOIN users U2 on U2.id = gp2.participantid AND U2.organizationid = gp2.organizationid
;

CREATE OR REPLACE FUNCTION cache_user_roles(user_id BIGINT[],organizationid BIGINT) 
        RETURNS BOOLEAN
        AS $$
	DELETE FROM userrolecache where userid = ANY($1);
	--AND organizationid = $2;
	INSERT INTO userrolecache (userid,effectiveroleid,baseroleid,organizationid) select * from effectiveUserRoles where userid = ANY($1);
	--and organizationid = $2;
	SELECT true;
        $$ LANGUAGE 'sql';

CREATE OR REPLACE FUNCTION cache_user_roles_OLD_REFACTOR(user_id BIGINT,organizationid BIGINT) 
        RETURNS BOOLEAN
        AS $$
	DELETE FROM userrolecache where userid = $1;
	--AND organizationid = $2;
	INSERT INTO userrolecache (userid,effectiveroleid,baseroleid,organizationid) select * from effectiveUserRoles where userid=$1;
	--and organizationid = $2;
	SELECT true;
        $$ LANGUAGE 'sql';

CREATE OR REPLACE FUNCTION cache_all_user_roles(orgId BIGINT) 
        RETURNS BOOLEAN
        AS $BODY$
        DECLARE ids BIGINT[] = ARRAY(SELECT id FROM users WHERE organizationid = $1);
        BEGIN
	DELETE FROM userrolecache WHERE userid = ANY(ids);
	INSERT INTO userrolecache (userid,effectiveroleid,baseroleid,organizationid) select * from effectiveUserRoles where userid = ANY(ids);
	RETURN true;
	END

        $BODY$ LANGUAGE 'plpgsql';

CREATE OR REPLACE FUNCTION cache_all_user_roles_EXAMPLE() 
        RETURNS BOOLEAN
        AS $BODY$
        DECLARE ids BIGINT[] = ARRAY(SELECT id FROM users);
        BEGIN
	TRUNCATE userrolecache;
	INSERT INTO userrolecache (userid,effectiveroleid,baseroleid,organizationid) select * from effectiveUserRoles where userid = ANY(ids);
	RETURN true;
	END

        $BODY$ LANGUAGE 'plpgsql';

CREATE OR REPLACE FUNCTION cache_all_data_roles(orgId BIGINT) 
        RETURNS BOOLEAN
        AS $BODY$
        DECLARE ids BIGINT[] = ARRAY(SELECT id FROM data WHERE organizationid = $1);
        BEGIN
	DELETE FROM datarolecache WHERE dataid = ANY(ids);
	INSERT INTO datarolecache (dataid,effectiveroleid,baseroleid,affecttype,affectid,organizationid) select * from effectiveDataRoles where dataid=ANY(ids);
	RETURN true;
	END

        $BODY$ LANGUAGE 'plpgsql';

CREATE OR REPLACE FUNCTION cache_data_roles(data_id BIGINT[],organizationid BIGINT) 
        RETURNS BOOLEAN
        AS $$
        BEGIN
	DELETE FROM datarolecache where dataid = ANY($1);
	INSERT INTO datarolecache (dataid,effectiveroleid,baseroleid,affecttype,affectid,organizationid) select * from effectiveDataRoles where dataid=ANY($1);
	RETURN true;
	END
        $$ LANGUAGE 'plpgsql';

CREATE OR REPLACE FUNCTION cache_all_role_roles(orgId BIGINT) 
        RETURNS BOOLEAN
        AS $BODY$
        DECLARE ids BIGINT[] = ARRAY(SELECT id FROM roles WHERE organizationid = $1);
        BEGIN
	DELETE FROM rolerolecache WHERE roleid = ANY(ids);
	INSERT INTO rolerolecache (roleid,effectiveroleid,baseroleid,affecttype,affectid,organizationid) select * from effectiveRoleRoles where roleid=ANY(ids);
	RETURN true;
	END

        $BODY$ LANGUAGE 'plpgsql';

CREATE OR REPLACE FUNCTION cache_role_roles(role_id BIGINT[],organizationid BIGINT) 
        RETURNS BOOLEAN
        AS $$
        BEGIN
	DELETE FROM rolerolecache where roleid = ANY($1);
	INSERT INTO rolerolecache (roleid,effectiveroleid,baseroleid,affecttype,affectid,organizationid) select * from effectiveRoleRoles where roleid=ANY($1);
	RETURN true;
	END
        $$ LANGUAGE 'plpgsql';

CREATE OR REPLACE FUNCTION cache_all_group_roles(orgId BIGINT) 
        RETURNS BOOLEAN
        AS $BODY$
        DECLARE ids BIGINT[] = ARRAY(SELECT id FROM groups WHERE organizationid = $1);
        BEGIN
	DELETE FROM grouprolecache WHERE groupid = ANY(ids);
	INSERT INTO grouprolecache (groupid,effectiveroleid,baseroleid,affecttype,affectid,organizationid) select * from effectiveGroupRoles where groupid=ANY(ids);
	RETURN true;
	END

        $BODY$ LANGUAGE 'plpgsql';

CREATE OR REPLACE FUNCTION cache_group_roles(group_id BIGINT[],organizationid BIGINT) 
        RETURNS BOOLEAN
        AS $$
        BEGIN
	DELETE FROM grouprolecache where groupid = ANY($1);
	INSERT INTO grouprolecache (groupid,effectiveroleid,baseroleid,affecttype,affectid,organizationid) select * from effectiveGroupRoles where groupid=ANY($1);
	RETURN true;
	END
        $$ LANGUAGE 'plpgsql';


create or replace view effectiveGroupRoleRights as
--select distinct GP.participationid as groupid,GP.affectId,GP.affectType,ER.userid,ER.effectiveRoleId as roleid,ER.organizationid from userrolecache ER
--join groupparticipation GP on GP.participantid=ER.effectiveRoleId AND GP.participanttype = 'ROLE'
select distinct GC.groupid,GC.affectId,GC.affectType,ER.userid,ER.effectiveRoleId as roleid,ER.organizationid from userrolecache ER
join groupRoleCache GC on GC.effectiveRoleId=ER.effectiveRoleId 
;

create or replace view effectiveDataRoleRights as
--select distinct DP.participationid as dataid,DP.affectId,DP.affectType,ER.userid,ER.effectiveRoleId as roleid,ER.organizationid from userrolecache ER
--join dataparticipation DP on DP.participantid=ER.effectiveRoleId and DP.participanttype = 'ROLE'
select distinct DRC.dataid,DRC.affectId,DRC.affectType,ER.userid,ER.effectiveRoleId as roleid,ER.organizationid from userrolecache ER
join dataRoleCache DRC on DRC.effectiveRoleId=ER.effectiveRoleId
;

create or replace view effectiveRoleRoleRights as
--select distinct RP.participationid as sourceroleid,RP.affectId,RP.affectType,ER.userid,ER.effectiveRoleId as roleid,ER.organizationid from userrolecache ER
--join roleparticipation RP on RP.participantid=ER.effectiveRoleId and RP.participanttype = 'ROLE'
select distinct RRC.roleid as sourceroleid,RRC.affectId,RRC.affectType,ER.userid,ER.effectiveRoleId as roleid,ER.organizationid from userrolecache ER
join roleRoleCache RRC on RRC.effectiveRoleId=ER.effectiveRoleId
;



CREATE OR REPLACE FUNCTION cache_roles() 
        RETURNS BOOLEAN
        AS $$
	truncate dataRoleCache;
	insert into datarolecache (dataid,effectiveroleid,baseroleid,affecttype,affectid,organizationid) select * from effectiveDataRoles;
	truncate roleRoleCache;
	insert into rolerolecache (roleid,effectiveroleid,baseroleid,affecttype,affectid,organizationid) select * from effectiveRoleRoles;
	truncate groupRoleCache;
	insert into grouprolecache (groupid,effectiveroleid,baseroleid,affecttype,affectid,organizationid) select * from effectiveGroupRoles;
	truncate userRoleCache;
	INSERT INTO userrolecache (userid,effectiveroleid,baseroleid,organizationid) select * from effectiveUserRoles;
	SELECT true;
        $$ LANGUAGE 'sql';


create or replace view groupRights as
	select distinct userid,groupid,affecttype,affectid,organizationid from (
		select userid,groupid,affecttype,affectid,organizationid
		FROM groupUserRights GUR
	UNION ALL
		select userid,groupid,affecttype,affectid,organizationid
		FROM effectiveGroupRoleRights GRR
	) as AM;

create or replace view dataRights as
	select distinct userid,dataid,affecttype,affectid,organizationid from (
	select userid,dataid,affecttype,affectid,organizationid
	FROM dataUserRights GUR
	UNION ALL
	select userid,dataid,affecttype,affectid,organizationid
	FROM effectiveDataRoleRights GRR
) as AM;
create or replace view roleRights as
	select distinct userid,roleid,affecttype,affectid,organizationid from (
	select userid,roleid,affecttype,affectid,organizationid
	FROM roleUserRights GUR
	UNION ALL
	select userid,sourceroleid as roleid,affecttype,affectid,organizationid
	FROM effectiveRoleRoleRights GRR
) as AM;



create or replace view orphanDataParticipations as
select id from DataParticipation RP1
where (participanttype = 'USER' and participantid not in (select id from Users U1))
OR (participanttype = 'ACCOUNT' and participantid not in (select id from Accounts A1))
OR (participanttype = 'ROLE' and participantid not in (select id from Roles R1))
OR (participanttype = 'GROUP' and participantid not in (select id from Groups G1))
OR (participationid not in (select id from Data D1))
or
organizationid not in (select id from organizations)
;

create or replace view orphanRoleParticipations as
select id from RoleParticipation RP1
where (participanttype = 'USER' and participantid not in (select id from Users U1))
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
OR (participanttype = 'ACCOUNT' and participantid not in (select id from Accounts A1))
OR (participanttype = 'ROLE' and participantid not in (select id from Roles R1))
OR (participanttype = 'GROUP' and participantid not in (select id from Groups G1))
OR (participationid not in (select id from Groups))
or
organizationid not in (select id from organizations)
;

create or replace view orphanGroups as
select id, name, parentid from Groups G1
where (parentid not in (select id from Groups G2)
and G1.parentid > 0)
or
organizationid not in (select id from organizations)
;

create or replace view orphanRoles as
select id, name, parentid from Roles R1
where
(parentid not in (select id from Roles R2)
and R1.parentid > 0)
or
organizationid not in (select id from organizations)
;

create or replace view orphanData as
select id, name, groupid from Data G1
where groupid not in (select id from Groups G2)
or
organizationid not in (select id from organizations)
;

CREATE OR REPLACE FUNCTION cleanup_orphans() 
        RETURNS BOOLEAN
        AS $$
	delete from users where organizationid not in (select id from organizations);
	delete from data where id in (select id from orphanData);
	delete from dataparticipation where id in (select id from orphanDataParticipations);
	delete from groups where id in (select id from orphanGroups);
	delete from groupparticipation where id in (select id from orphanGroupParticipations);
	delete from roles where id in (select id from orphanRoles);
	delete from roleparticipation where id in (select id from orphanRoleParticipations);
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

-- delete from roles where id in (select id from orphanRoles);
-- delete from groups where id in (select id from orphanGroups);
-- delete from roleparticipation where id in (select id from orphanRoleParticipations);
-- delete from groupparticipation where id in (select id from orphanGroupParticipations);
-- delete from lifecycleparticipation where id in (select id from orphanLifecycleParticipations);

-- select ((EXTRACT(EPOCH FROM AuditResultDate)*1000) - (EXTRACT(EPOCH FROM AuditDate)) * 1000) as PerfInMS from Audit order by AuditResultDate DESC limit 100


