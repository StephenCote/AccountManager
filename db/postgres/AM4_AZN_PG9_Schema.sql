				DROP TABLE IF EXISTS fact CASCADE;
				DROP SEQUENCE IF EXISTS fact_id_seq;
				CREATE SEQUENCE fact_id_seq;
				create table fact (
					Id bigint not null default nextval('fact_id_seq'),
					OwnerId bigint not null default 0,
					Name varchar(255) not null,
					Description varchar(255),
					LogicalOrder int not null default 0,
					Urn varchar(255) not null,
					FactType varchar(64),
					SourceUrn varchar(255),
					SourceUrl varchar(2047),
					SourceDataType varchar(64),
					FactData varchar(255),
					FactoryType varchar(64),
					GroupId bigint not null,
					ObjectId varchar(64),
					OrganizationId bigint not null default 0,
					primary key(Id)
				);
				CREATE UNIQUE INDEX fact_id ON fact(Id);
				CREATE UNIQUE INDEX IdxfactNameGroup on fact(Name,GroupId,OrganizationId);
				CREATE UNIQUE INDEX IdxfactUrnGroup on fact(Urn,OrganizationId);
				CREATE INDEX IdxfactIdGroup on fact(Id,OrganizationId);


				DROP TABLE IF EXISTS functionfact CASCADE;
				DROP SEQUENCE IF EXISTS functionfact_id_seq;
				CREATE SEQUENCE functionfact_id_seq;
				create table functionfact (
					Id bigint not null default nextval('functionfact_id_seq'),
					OwnerId bigint not null default 0,
					Name varchar(255) not null,
					Description varchar(255),
					LogicalOrder int not null default 0,
					Urn varchar(255) not null,
					FunctionUrn varchar(255),
					FactUrn varchar(255),
					GroupId bigint not null,
					ObjectId varchar(64),
					OrganizationId bigint not null default 0,
					primary key(Id)
				);
				CREATE UNIQUE INDEX functionfact_id ON functionfact(Id);
				CREATE UNIQUE INDEX IdxfunctionfactNameGroup on functionfact(Name,GroupId,OrganizationId);
				CREATE UNIQUE INDEX IdxfunctionfactUrnGroup on functionfact(Urn,OrganizationId);
				CREATE INDEX IdxfunctionfactIdGroup on functionfact(Id,OrganizationId);

				DROP TABLE IF EXISTS function CASCADE;
				DROP SEQUENCE IF EXISTS function_id_seq;
				CREATE SEQUENCE function_id_seq;
				create table function (
					Id bigint not null default nextval('function_id_seq'),
					OwnerId bigint not null default 0,
					Name varchar(255) not null,
					Description varchar(255),
					LogicalOrder int not null default 0,
					Urn varchar(255) not null,
					FunctionType varchar(64),
					SourceUrn varchar(255),
					SourceUrl varchar(2047),
					GroupId bigint not null,
					ObjectId varchar(64),
					OrganizationId bigint not null default 0,
					primary key(Id)
				);
				CREATE UNIQUE INDEX function_id ON function(Id);
				CREATE UNIQUE INDEX IdxfunctionNameGroup on function(Name,GroupId,OrganizationId);
				CREATE UNIQUE INDEX IdxfunctionUrnGroup on function(Urn,OrganizationId);
				CREATE INDEX IdxfunctionIdGroup on function(Id,OrganizationId);

				DROP TABLE IF EXISTS functionparticipation CASCADE;
				DROP SEQUENCE IF EXISTS functionparticipation_id_seq;
				CREATE SEQUENCE functionparticipation_id_seq;
				CREATE TABLE functionparticipation (
						Id bigint not null default nextval('functionparticipation_id_seq'),
						OwnerId bigint not null,
						ParticipationId bigint not null default 0,
						ParticipantType varchar(16) not null,
						ParticipantId bigint not null default 0,
						AffectType varchar(16) not null,
						AffectId bigint not null default 0,
						OrganizationId bigint not null default 0,
						primary key(Id)
					);
				CREATE UNIQUE INDEX functionparticipation_id ON functionparticipation(Id);
				CREATE INDEX functionparticipation_parttype ON functionparticipation(ParticipantId,ParticipantType,AffectId);
				CREATE INDEX functionparticipation_pid ON functionparticipation(ParticipationId);
				CREATE UNIQUE INDEX IdxfunctionparticipationCbo on functionparticipation(ParticipationId,ParticipantId,ParticipantType,AffectId,OrganizationId);

				DROP TABLE IF EXISTS operation CASCADE;
				DROP SEQUENCE IF EXISTS operation_id_seq;
				CREATE SEQUENCE operation_id_seq;
				create table operation (
					Id bigint not null default nextval('operation_id_seq'),
					OwnerId bigint not null default 0,
					Name varchar(255) not null,
					Description varchar(255),
					LogicalOrder int not null default 0,
					Urn varchar(255) not null,
					OperationType varchar(64),
					Operation varchar(2047),
					GroupId bigint not null,
					ObjectId varchar(64),
					OrganizationId bigint not null default 0,
					primary key(Id)
				);
				CREATE UNIQUE INDEX operation_id ON operation(Id);
				CREATE UNIQUE INDEX IdxoperationNameGroup on operation(Name,GroupId,OrganizationId);
				CREATE UNIQUE INDEX IdxoperationUrnGroup on operation(Urn,OrganizationId);
				CREATE INDEX IdxoperationIdGroup on operation(Id,OrganizationId);

				DROP TABLE IF EXISTS pattern CASCADE;
				DROP SEQUENCE IF EXISTS pattern_id_seq;
				CREATE SEQUENCE pattern_id_seq;
				create table pattern (
					Id bigint not null default nextval('pattern_id_seq'),
					OwnerId bigint not null default 0,
					Name varchar(255) not null,
					Description varchar(255),
					LogicalOrder int not null default 0,
					Urn varchar(255) not null,
					FactUrn varchar(255),
					Comparator varchar(32),
					PatternType varchar(64),
					MatchUrn varchar(255),
					OperationUrn varchar(255),
					GroupId bigint not null,
					ObjectId varchar(64),
					OrganizationId bigint not null default 0,
					primary key(Id)
				);
				CREATE UNIQUE INDEX pattern_id ON pattern(Id);
				CREATE UNIQUE INDEX IdxpatternNameGroup on pattern(Name,GroupId,OrganizationId);
				CREATE UNIQUE INDEX IdxpatternUrnGroup on pattern(Urn,OrganizationId);
				CREATE INDEX IdxpatternIdGroup on pattern(Id,OrganizationId);

				DROP TABLE IF EXISTS policy CASCADE;
				DROP SEQUENCE IF EXISTS policy_id_seq;
				CREATE SEQUENCE policy_id_seq;
				create table policy (
					Id bigint not null default nextval('policy_id_seq'),
					OwnerId bigint not null default 0,
					Name varchar(255) not null,
					Description varchar(255),
					LogicalOrder int not null default 0,
					Enabled boolean not null default false,
					Urn varchar(255) not null,
					CreatedDate timestamp not null,
					ModifiedDate timestamp not null,
					ExpirationDate timestamp not null,
					DecisionAge bigint not null default 0,
					GroupId bigint not null,
					ObjectId varchar(64),
					OrganizationId bigint not null default 0,
					primary key(Id)
				);
				CREATE UNIQUE INDEX policy_id ON policy(Id);
				CREATE UNIQUE INDEX IdxpolicyNameGroup on policy(Name,GroupId,OrganizationId);
				CREATE UNIQUE INDEX IdxpolicyUrnGroup on policy(Urn,OrganizationId);
				CREATE INDEX IdxpolicyIdGroup on policy(Id,OrganizationId);

				DROP TABLE IF EXISTS policyparticipation CASCADE;
				DROP SEQUENCE IF EXISTS policyparticipation_id_seq;
				CREATE SEQUENCE policyparticipation_id_seq;
				CREATE TABLE policyparticipation (
						Id bigint not null default nextval('policyparticipation_id_seq'),
						OwnerId bigint not null,
						ParticipationId bigint not null default 0,
						ParticipantType varchar(16) not null,
						ParticipantId bigint not null default 0,
						AffectType varchar(16) not null,
						AffectId bigint not null default 0,
						OrganizationId bigint not null default 0,
						primary key(Id)
					);
				CREATE UNIQUE INDEX policyparticipation_id ON policyparticipation(Id);
				CREATE INDEX policyparticipation_parttype ON policyparticipation(ParticipantId,ParticipantType,AffectId);
				CREATE INDEX policyparticipation_pid ON policyparticipation(ParticipationId);
				CREATE UNIQUE INDEX IdxpolicyparticipationCbo on policyparticipation(ParticipationId,ParticipantId,ParticipantType,AffectId,OrganizationId);

				DROP TABLE IF EXISTS rule CASCADE;
				DROP SEQUENCE IF EXISTS rule_id_seq;
				CREATE SEQUENCE rule_id_seq;
				create table rule (
					Id bigint not null default nextval('rule_id_seq'),
					OwnerId bigint not null default 0,
					Name varchar(255) not null,
					Description varchar(255),
					LogicalOrder int not null default 0,
					Urn varchar(255) not null,
					RuleType varchar(64),
					Condition varchar(64),
					GroupId bigint not null,
					ObjectId varchar(64),
					OrganizationId bigint not null default 0,
					primary key(Id)
				);
				CREATE UNIQUE INDEX rule_id ON rule(Id);
				CREATE UNIQUE INDEX IdxruleNameGroup on rule(Name,GroupId,OrganizationId);
				CREATE UNIQUE INDEX IdxruleUrnGroup on rule(Urn,OrganizationId);
				CREATE INDEX IdxruleIdGroup on rule(Id,OrganizationId);

				DROP TABLE IF EXISTS ruleparticipation CASCADE;
				DROP SEQUENCE IF EXISTS ruleparticipation_id_seq;
				CREATE SEQUENCE ruleparticipation_id_seq;
				CREATE TABLE ruleparticipation (
						Id bigint not null default nextval('ruleparticipation_id_seq'),
						OwnerId bigint not null,
						ParticipationId bigint not null default 0,
						ParticipantType varchar(16) not null,
						ParticipantId bigint not null default 0,
						AffectType varchar(16) not null,
						AffectId bigint not null default 0,
						OrganizationId bigint not null default 0,
						primary key(Id)
					);
				CREATE UNIQUE INDEX ruleparticipation_id ON ruleparticipation(Id);
				CREATE INDEX ruleparticipation_parttype ON ruleparticipation(ParticipantId,ParticipantType,AffectId);
				CREATE INDEX ruleparticipation_pid ON ruleparticipation(ParticipationId);
				CREATE UNIQUE INDEX IdxruleparticipationCbo on ruleparticipation(ParticipationId,ParticipantId,ParticipantType,AffectId,OrganizationId);