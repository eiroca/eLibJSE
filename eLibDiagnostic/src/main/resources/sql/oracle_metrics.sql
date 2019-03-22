      SELECT '0000' AS ID, 'Info - System date' AS METRIC, TO_NUMBER(TO_CHAR(SYSDATE,'yyyymmddhhmiss')) AS METRIC_VALUE FROM DUAL
UNION SELECT '0001' AS ID, 'SGA - Free Buffer Waits' AS METRIC, FREE_BUFFER_WAIT AS METRIC_VALUE FROM v$buffer_pool_statistics
UNION SELECT '0002' AS ID, 'SGA - Write Complete Waits' AS METRIC, WRITE_COMPLETE_WAIT AS METRIC_VALUE FROM v$buffer_pool_statistics
UNION SELECT '0003' AS ID, 'SGA - Buffer Busy Waits' AS METRIC, BUFFER_BUSY_WAIT AS METRIC_VALUE FROM v$buffer_pool_statistics
UNION SELECT '0004' AS ID, 'SGA - DB Block Changes' AS METRIC, DB_BLOCK_CHANGE AS METRIC_VALUE FROM v$buffer_pool_statistics
UNION SELECT '0005' AS ID, 'SGA - DB Block Gets' AS METRIC, DB_BLOCK_GETS AS METRIC_VALUE FROM v$buffer_pool_statistics
UNION SELECT '0006' AS ID, 'SGA - Consistent Gets' AS METRIC, CONSISTENT_GETS AS METRIC_VALUE FROM v$buffer_pool_statistics
UNION SELECT '0007' AS ID, 'SGA - Physical Reads' AS METRIC, PHYSICAL_READS AS METRIC_VALUE FROM v$buffer_pool_statistics
UNION SELECT '0008' AS ID, 'SGA - Physical Writes' AS METRIC, PHYSICAL_WRITES AS METRIC_VALUE FROM v$buffer_pool_statistics
UNION SELECT '0009' AS ID, 'SGA - Buffer Cache Hit Ratio' AS METRIC, ROUND ((congets.VALUE + dbgets.VALUE - physreads.VALUE) * 100 / (congets.VALUE + dbgets.VALUE), 2) AS METRIC_VALUE FROM v$sysstat congets, v$sysstat dbgets, v$sysstat physreads WHERE congets.NAME = 'consistent gets' AND dbgets.NAME = 'db block gets' AND physreads.NAME = 'physical reads' 
UNION SELECT '0010' AS ID, 'SGA - Execution Without Parse Ratio' AS METRIC, DECODE (SIGN (ROUND ((ec.VALUE - pc.VALUE) * 100 / DECODE (ec.VALUE, 0, 1, ec.VALUE), 2)), -1, 0, ROUND ((ec.VALUE - pc.VALUE) * 100 / DECODE (ec.VALUE, 0, 1, ec.VALUE), 2)) AS METRIC_VALUE FROM v$sysstat ec, v$sysstat pc WHERE ec.NAME = 'execute count' AND pc.NAME IN ('parse count', 'parse count (total)') 
UNION SELECT '0011' AS ID, 'SGA - Memory Sort Ratio' AS METRIC, ROUND (ms.VALUE / DECODE ((ds.VALUE + ms.VALUE), 0, 1, (ds.VALUE + ms.VALUE)) * 100, 2) AS METRIC_VALUE FROM v$sysstat ds, v$sysstat ms WHERE ms.NAME = 'sorts (memory)' AND ds.NAME = 'sorts (disk)' 
UNION SELECT '0101' AS ID, 'Sessions - Maximum Concurrent User' AS METRIC, SESSIONS_MAX AS METRIC_VALUE FROM v$license
UNION SELECT '0102' AS ID, 'Sessions - Current Concurrent User' AS METRIC, SESSIONS_CURRENT AS METRIC_VALUE FROM v$license
UNION SELECT '0103' AS ID, 'Sessions - Highest Concurrent User' AS METRIC, SESSIONS_HIGHWATER AS METRIC_VALUE FROM v$license
UNION SELECT '0104' AS ID, 'Sessions - Maximum Named Users' AS METRIC, USERS_MAX AS METRIC_VALUE FROM v$license
UNION SELECT '0201' AS ID, 'Hit Ratio - SQL Area Get' AS METRIC, ROUND(GETHITRATIO * 100.0, 2) AS METRIC_VALUE FROM V$LIBRARYCACHE WHERE NAMESPACE = 'SQL AREA'
UNION SELECT '0202' AS ID, 'Hit Ratio - SQL Area Pin' AS METRIC, ROUND(PINHITRATIO * 100.0, 2) AS METRIC_VALUE FROM V$LIBRARYCACHE WHERE NAMESPACE = 'SQL AREA'
UNION SELECT '0203' AS ID, 'Hit Ratio - Table/Procedure Get' AS METRIC, ROUND(GETHITRATIO * 100.0, 2) AS METRIC_VALUE FROM V$LIBRARYCACHE WHERE NAMESPACE = 'TABLE/PROCEDURE'
UNION SELECT '0204' AS ID, 'Hit Ratio - Table/Procedure Pin' AS METRIC, ROUND(PINHITRATIO * 100.0, 2) AS METRIC_VALUE FROM V$LIBRARYCACHE WHERE NAMESPACE = 'TABLE/PROCEDURE'
UNION SELECT '0205' AS ID, 'Hit Ratio - Body Get' AS METRIC, ROUND(GETHITRATIO * 100.0, 2) AS METRIC_VALUE FROM V$LIBRARYCACHE WHERE NAMESPACE = 'BODY'
UNION SELECT '0206' AS ID, 'Hit Ratio - Body Pin' AS METRIC, ROUND(PINHITRATIO * 100.0, 2) AS METRIC_VALUE FROM V$LIBRARYCACHE WHERE NAMESPACE = 'BODY'
UNION SELECT '0207' AS ID, 'Hit Ratio - Trigger Get' AS METRIC, ROUND(GETHITRATIO * 100.0, 2) AS METRIC_VALUE FROM V$LIBRARYCACHE WHERE NAMESPACE = 'TRIGGER'
UNION SELECT '0208' AS ID, 'Hit Ratio - Trigger Pin' AS METRIC, ROUND(PINHITRATIO * 100.0, 2) AS METRIC_VALUE FROM V$LIBRARYCACHE WHERE NAMESPACE = 'TRIGGER'
UNION SELECT '0209' AS ID, 'Hit Ratio - Library Cache Get' AS METRIC, ROUND(SUM(gethits) / SUM(gets) * 100, 2) AS METRIC_VALUE FROM v$librarycache
UNION SELECT '0210' AS ID, 'Hit Ratio - Library Cache Pin' AS METRIC, ROUND(SUM(pinhits) / SUM(pins) * 100, 2) AS METRIC_VALUE FROM v$librarycache
UNION SELECT '0211' AS ID, 'Hit Ratio - Dictionary Cache' AS METRIC, ROUND((SUM(GETS - GETMISSES - FIXED)) / SUM(GETS) * 100, 2) AS METRIC_VALUE FROM V$ROWCACHE
UNION SELECT '0301' AS ID, 'Shared Pool - Free Memory' AS METRIC, ROUND((SUM(DECODE(name, 'free memory', bytes, 0)) / SUM(bytes)) * 100, 2) AS METRIC_VALUE FROM v$sgastat 
UNION SELECT '0302' AS ID, 'Shared Pool - Reloads' AS METRIC, ROUND(SUM(reloads) / SUM(pins) * 100, 2) AS METRIC_VALUE FROM v$librarycache WHERE namespace IN('SQL AREA', 'TABLE/PROCEDURE', 'BODY', 'TRIGGER')
UNION SELECT '0401' AS ID, 'Latches - Wait Latch Gets' AS METRIC, ROUND(((SUM(gets) - SUM(misses)) / SUM(gets)) * 100, 2) AS METRIC_VALUE FROM v$latch 
UNION SELECT '0402' AS ID, 'Latches - Immediate Latch Gets' AS METRIC, ROUND(((SUM(immediate_gets) - SUM(immediate_misses)) / SUM(immediate_gets)) * 100, 2) AS METRIC_VALUE FROM v$latch
UNION SELECT '0501' AS ID, 'Redo - Space Wait Ratio' AS METRIC, ROUND((req.value / wrt.value) * 100, 2) AS METRIC_VALUE FROM v$sysstat req, v$sysstat wrt WHERE req.name = 'redo log space requests' AND wrt.name = 'redo writes' 
UNION SELECT '0502' AS ID, 'Redo - Allocation Latch' AS METRIC, ROUND(GREATEST((SUM(DECODE(ln.name, 'redo allocation', misses, 0)) / GREATEST(SUM(DECODE(ln.name, 'redo allocation', gets, 0)), 1)),(SUM(DECODE(ln.name, 'redo allocation', immediate_misses, 0)) / GREATEST(SUM(DECODE(ln.name, 'redo allocation', immediate_gets, 0)) + SUM(DECODE(ln.name, 'redo allocation', immediate_misses, 0)), 1))) * 100, 2) AS METRIC_VALUE FROM v$latch l, v$latchname LN WHERE l.latch# = ln.latch# 
UNION SELECT '0503' AS ID, 'Redo - Copy Latches' AS METRIC, ROUND(GREATEST((SUM(DECODE(ln.name, 'redo copy', misses, 0)) / GREATEST(SUM(DECODE(ln.name, 'redo copy', gets, 0)), 1)),(SUM(DECODE(ln.name, 'redo copy', immediate_misses, 0)) / GREATEST(SUM(DECODE(ln.name, 'redo copy', immediate_gets, 0)) + SUM(DECODE(ln.name, 'redo copy', immediate_misses, 0)), 1))) * 100, 2) AS METRIC_VALUE FROM v$latch l, v$latchname LN WHERE l.latch# = ln.latch#
UNION SELECT '0601' AS ID, 'Info - Recursive Calls Ratio' AS METRIC, ROUND((rcv.value /(rcv.value + usr.value)) * 100, 2) AS METRIC_VALUE FROM v$sysstat rcv, v$sysstat usr WHERE rcv.name = 'recursive calls' AND usr.name = 'user calls'
UNION SELECT '0602' AS ID, 'Info - Short Table Scans Ratio' AS METRIC, ROUND((shrt.value /(shrt.value + lng.value)) * 100, 2) AS METRIC_VALUE FROM v$sysstat shrt, v$sysstat lng WHERE shrt.name = 'table scans (short tables)' AND lng.name = 'table scans (long tables)'
UNION SELECT '0603' AS ID, 'Info - Rollback Segment Contention' AS METRIC, ROUND(SUM(waits) / SUM(gets) * 100, 2) AS METRIC_VALUE FROM v$rollstat 
UNION SELECT '0604' AS ID, 'Info - CPU Parse Overhead' AS METRIC, ROUND((prs.value /(prs.value + exe.value)) * 100, 2) AS METRIC_VALUE FROM v$sysstat prs, v$sysstat exe WHERE prs.name LIKE 'parse count (hard)' AND exe.name = 'execute count'
UNION SELECT '0701' AS ID, 'Table Contention - Chained Fetch Ratio' AS METRIC, ROUND((cont.value /(scn.value + rid.value)) * 100, 2) AS METRIC_VALUE FROM v$sysstat cont, v$sysstat scn, v$sysstat rid WHERE cont.name = 'table fetch continued row' AND scn.name = 'table scan rows gotten' AND rid.name = 'table fetch by rowid' 
UNION SELECT '0702' AS ID, 'Table Contention - Free List Contention' AS METRIC, ROUND((SUM(DECODE(w.class, 'free list', COUNT, 0)) /(SUM(DECODE(name, 'db block gets', VALUE, 0)) + SUM(DECODE(name, 'consistent gets', VALUE, 0)))) * 100, 2) AS METRIC_VALUE FROM v$waitstat w, v$sysstat
