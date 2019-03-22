SELECT
	*
FROM
	(
		SELECT
			event AS "Event",
			total_waits AS "Total Waits",
			total_timeouts AS "Total Timeouts",
			time_waited AS "Time Waited",
			average_wait AS "Average Wait time"
		FROM
			v$system_event
		WHERE
			1=1
			AND event NOT LIKE 'SQL*Net%'
			AND event NOT LIKE '%idle wait%'
			AND event NOT LIKE '%Idle Wait%'
			AND event NOT IN ('pmon timer', 'rdbms ipc message', 'dispatcher timer', 'smon timer', 'VKRM Idle')
		ORDER BY
			"Average Wait time" DESC
	)
WHERE
	rownum <= 20