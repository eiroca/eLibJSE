select
	A.relname || ' [Index: ' || A.indexrelname || ']' idx,
	sum( idx_scan ) idx_scan,
	sum( idx_tup_read ) idx_tup_read,
	sum( idx_tup_fetch ) idx_tup_fetch,
	sum( idx_blks_read ) idx_blks_read,
	sum( idx_blks_hit ) idx_blks_hit
from
	pg_stat_user_indexes A left join pg_statio_user_indexes B on A.relname = B.relname and A.indexrelname = B.indexrelname
group by
	A.relname,
	A.indexrelname