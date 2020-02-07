DROP VIEW IF EXISTS tempPermArrMap;
DROP VIEW IF EXISTS tempPermArrView;
DROP TABLE IF EXISTS tempPermArr;
DROP TABLE IF EXISTS tempAcctPerm;
DROP TABLE IF EXISTS tempPermList;
DROP TABLE IF EXISTS tempPermSet;
DROP TABLE IF EXISTS tempPermMap;

CREATE TEMP TABLE tempAcctPerm AS
select row_number() OVER () as rnum,accountname,groupname from identityServiceApplicationAccountsGroups
where applicationname = 'Application 4'
ORDER by groupname ASC;
;
CREATE TEMP TABLE tempPermList AS
select row_number() OVER () as rnum,groupname from tempAcctPerm SAP1 group by groupname order by groupname ASC
;

CREATE TEMP TABLE tempPermSet AS
select row_number() OVER () as rnum,accountname,array_agg(groupname) as permlist from (select accountname,groupname from tempAcctPerm ORDER BY groupname) as SAP group by accountname
;

CREATE TEMP TABLE tempPermMap AS
select TPL.rnum as permlistrow,TPS.rnum as permsetrow,groupname, TPS.permlist from tempPermList TPL
join tempPermSet TPS on groupname = ANY(TPS.permlist)
WHERE groupname = ANY(TPS.permlist)
;

CREATE OR REPLACE TEMP view tempPermArrView as
WITH RECURSIVE xtab AS (
        WITH no_cte AS (
        SELECT
        1::int AS len
        , rnum as idx
        , CAST(ARRAY[groupname] AS varchar(512)[]) as str
        FROM tempPermList
        )
        SELECT t0.len as len
                , t0.idx
                , CAST(t0.str AS varchar(512)[]) as str
        FROM no_cte t0
        UNION SELECT 1+t1.len
                , tc.idx
                , array_cat(t1.str,tc.str)::varchar(512)[] AS str
        FROM xtab t1
        JOIN no_cte tc ON tc.idx > t1.idx
--	  limit recursion to this depth
	  WHERE 
--           array_cat(t1.str,tc.str)::varchar(512)[] <@ 
		array_length(t1.str,1) <= 3
        )
SELECT * FROM xtab
ORDER BY len, str
;

CREATE TEMP TABLE tempPermArr AS
select len as length, idx as rowid, str as permArr from tempPermArrView;

select count(TPAM.accountName),array_length(TPA.permArr,1) len,TPA.permArr from tempPermArr TPA
inner join (select accountname,array_length(permlist,1) len,permlist from tempPermSet WHERE array_length(permlist,1) > 1 limit 20) TPAM on TPA.permArr <@ TPAM.permlist
where array_length(TPA.permArr,1) > 1
GROUP BY tpa.permarr
order by count(TPAM.accountName) DESC,len desc;