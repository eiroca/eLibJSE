select
	relname,
	blks_read,
	blks_hit
from
	pg_statio_user_sequences