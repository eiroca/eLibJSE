SELECT
	*
FROM
	(
		SELECT
			SQL_TEXT AS "Query",
			elapsed_time AS "Elapsed Time",
			cpu_time AS "CPU Time",
			disk_reads AS "Disk Reads",
			direct_writes AS "Disk Writes",
			executions AS "Executions",
			parse_calls AS "Parse Calls",
			buffer_gets AS "Buffer Gets",
			rows_processed AS "Rows rocessed"
		FROM
			v$sql
		ORDER BY
			EXECUTIONS DESC
	)
WHERE
	rownum <= 20