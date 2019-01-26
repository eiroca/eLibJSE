select
	A.relname,
	seq_scan,
	seq_tup_read,
	coalesce(idx_scan, 0),
	coalesce(idx_tup_fetch, 0),
	n_tup_ins,
	n_tup_upd,
	n_tup_del,
	n_tup_hot_upd,
	n_live_tup,
	n_dead_tup,
	vacuum_count,
	autovacuum_count,
	analyze_count,
	autoanalyze_count,
	case when coalesce(seq_scan, 0)=0 then 100 else 100 * coalesce(idx_scan, 1) / (coalesce(seq_scan, 0) + coalesce(idx_scan, 1)) end percent_of_times_index_used,
	n_live_tup rows_in_table,
	coalesce(B.heap_blks_read, 0) as heap_blks_read,
	coalesce(B.heap_blks_hit, 0) as heap_blks_hit,
	coalesce(B.idx_blks_read, 0) as idx_blks_read,
	coalesce(B.idx_blks_hit, 0) as idx_blks_hit,
	coalesce(B.toast_blks_read, 0) as toast_blks_read,
	coalesce(B.toast_blks_hit, 0) as toast_blks_hit,
	coalesce(B.tidx_blks_read, 0) as tidx_blks_read,
	coalesce(B.tidx_blks_hit, 0) as tidx_blks_hit
from
	pg_stat_user_tables A left join(
		select
			relname,
			sum( heap_blks_read ) heap_blks_read,
			sum( heap_blks_hit ) heap_blks_hit,
			sum( idx_blks_read ) idx_blks_read,
			sum( idx_blks_hit ) idx_blks_hit,
			sum( toast_blks_read ) toast_blks_read,
			sum( toast_blks_hit ) toast_blks_hit,
			sum( tidx_blks_read ) tidx_blks_read,
			sum( tidx_blks_hit ) tidx_blks_hit
		from
			pg_statio_user_tables
		group by
			relname
	) B on A.relname = B.relname
