SELECT
	t.tablespace AS "Tablespace",
	t.totalspace / (1024*1024) AS "Total",
	ROUND((t.totalspace - fs.freespace), 2) / (1024*1024) AS "Used",
	fs.freespace / (1024*1024) AS "Free",
	ROUND(((t.totalspace - fs.freespace) / t.totalspace) * 100, 2) AS "Used %",
	ROUND((fs.freespace / t.totalspace) * 100, 2) AS "Free %"
FROM
	(
		SELECT
			ROUND(SUM(d.bytes)) AS totalspace,
			d.tablespace_name TABLESPACE
		FROM
			dba_data_files d
		GROUP BY
			d.tablespace_name
	) t,
	(
		SELECT
			ROUND(SUM(f.bytes)) AS freespace,
			f.tablespace_name TABLESPACE
		FROM
			dba_free_space f
		GROUP BY
			f.tablespace_name
	) fs
WHERE
	t.tablespace = fs.tablespace
ORDER BY
	"Free %" 
