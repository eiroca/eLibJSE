SELECT
	b.owner || '.' || b.object_name AS "LockName",
	a.locked_mode AS "Lock Mode",
	CASE
		b.status
		WHEN 'VALID' THEN 0
		ELSE 1
	END AS "Lock Status",
	COUNT( a.session_id ) AS "Locks"
FROM
	v$locked_object a,
	dba_objects b
WHERE
	a.object_id = b.object_id
GROUP BY
	b.owner || '.' || b.object_name,
	a.locked_mode,
	CASE
		b.status
		WHEN 'VALID' THEN 0
		ELSE 1
	END
