      select ''001_001'' as METRIC_ID, ''heap_read'' as METRIC,  sum(heap_blks_read) as METRIC_VALUE from pg_statio_user_tables
union select ''001_002'' as METRIC_ID, ''heap_hit'' as METRIC,   sum(heap_blks_hit) as METRIC_VALUE from pg_statio_user_tables
union select ''001_003'' as METRIC_ID, ''heap_ratio'' as METRIC, (sum(heap_blks_hit) / (sum(heap_blks_hit) + sum(heap_blks_read))) * 100 as METRIC_VALUE from pg_statio_user_tables
union select ''002_001'' as METRIC_ID, ''idx_read'' as METRIC,   sum(idx_blks_read) as METRIC_VALUE from pg_statio_user_indexes
union select ''002_002'' as METRIC_ID, ''idx_hit'' as METRIC,    sum(idx_blks_hit) as METRIC_VALUE from pg_statio_user_indexes
union select ''002_003'' as METRIC_ID, ''idx_ratio'' as METRIC,  ((sum(idx_blks_hit) - sum(idx_blks_read)) / sum(idx_blks_hit)) * 100 as METRIC_VALUE from pg_statio_user_indexes
union select ''003_001'' as METRIC_ID, ''stat_activity'' as METRIC,  count(1) as METRIC_VALUE from pg_stat_activity where state = ''active'' and datname = ''{0}''
union select ''004_001'' as METRIC_ID, ''numbackends'' as METRIC,    numbackends as METRIC_VALUE from pg_stat_database where datname = ''{0}''
union select ''004_002'' as METRIC_ID, ''xact_commit'' as METRIC,    xact_commit as METRIC_VALUE from pg_stat_database where datname = ''{0}''
union select ''004_003'' as METRIC_ID, ''xact_rollback'' as METRIC,  xact_rollback as METRIC_VALUE from pg_stat_database where datname = ''{0}''
union select ''004_004'' as METRIC_ID, ''blks_read'' as METRIC,      blks_read as METRIC_VALUE from pg_stat_database where datname = ''{0}''
union select ''004_005'' as METRIC_ID, ''blks_hit'' as METRIC,       blks_hit as METRIC_VALUE from pg_stat_database where datname = ''{0}''
union select ''004_006'' as METRIC_ID, ''tup_returned'' as METRIC,   tup_returned as METRIC_VALUE from pg_stat_database where datname = ''{0}''
union select ''004_007'' as METRIC_ID, ''tup_fetched'' as METRIC,    tup_fetched as METRIC_VALUE from pg_stat_database where datname = ''{0}''
union select ''004_008'' as METRIC_ID, ''tup_inserted'' as METRIC,   tup_inserted as METRIC_VALUE from pg_stat_database where datname = ''{0}''
union select ''004_009'' as METRIC_ID, ''tup_updated'' as METRIC,    tup_updated as METRIC_VALUE from pg_stat_database where datname = ''{0}''
union select ''004_010'' as METRIC_ID, ''tup_deleted'' as METRIC,    tup_deleted as METRIC_VALUE from pg_stat_database where datname = ''{0}''
union select ''004_011'' as METRIC_ID, ''conflicts'' as METRIC,      conflicts as METRIC_VALUE from pg_stat_database where datname = ''{0}''
union select ''004_012'' as METRIC_ID, ''temp_bytes'' as METRIC,     temp_bytes as METRIC_VALUE from pg_stat_database where datname = ''{0}''
union select ''004_013'' as METRIC_ID, ''deadlocks'' as METRIC,      deadlocks as METRIC_VALUE from pg_stat_database where datname = ''{0}''
union select ''004_014'' as METRIC_ID, ''blk_read_time'' as METRIC,  blk_read_time as METRIC_VALUE from pg_stat_database where datname = ''{0}''
union select ''004_015'' as METRIC_ID, ''blk_write_time'' as METRIC, blk_write_time as METRIC_VALUE from pg_stat_database where datname = ''{0}''
