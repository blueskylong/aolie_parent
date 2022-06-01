--热点访问表
monitor.hotTable:
SELECT
  title,num
FROM
  (SELECT
    a.table_id,
    b.title,
    COUNT(1) AS num
  FROM
    aolie_s_log a,
    aolie_dm_table b
  WHERE a.table_id = b.table_id
  GROUP BY a.table_id,
    b.title
  ORDER BY num DESC) c
LIMIT 0, 10;

--热点访问方案
monitor.hotSchema:
SELECT
  title,num
FROM
  (SELECT
    a.schema_id ,
    b.schema_name as title,
    COUNT(1) AS num
  FROM
    aolie_s_log a,
    aolie_dm_schema b
  WHERE a.schema_id = b.schema_id
  GROUP BY a.schema_id,
    b.schema_name
  ORDER BY num DESC) c
LIMIT 0, 10;

--查询累计时间最长的TOP10
monitor.optLastTime.query:
SELECT
  title,num
FROM
  (SELECT
    a.table_id,
    b.title,
    SUM(last_time) AS num
  FROM
    aolie_s_log a,
    aolie_dm_table b
  WHERE a.table_id = b.table_id
  AND a.oper_type ='query'
  GROUP BY a.table_id,
    b.title
  ORDER BY num DESC) c
LIMIT 0, 10;

--操作时间最长的更新TOP10
monitor.optLastTime.update:
SELECT
  title,num
FROM
  (SELECT
    a.table_id,
    b.title,
    SUM(last_time) AS num
  FROM
    aolie_s_log a,
    aolie_dm_table b
  WHERE a.table_id = b.table_id
  AND a.oper_type in ('update','insert','delete')
  GROUP BY a.table_id,
    b.title
  ORDER BY num DESC) c
LIMIT 0, 10;

--操作次数最多的变更TOP10
monitor.optTimes.update:
SELECT
  title,num
FROM
  (SELECT
    a.table_id,
    b.title,
    count(1) AS num
  FROM
    aolie_s_log a,
    aolie_dm_table b
  WHERE a.table_id = b.table_id
  AND a.oper_type in ('update','insert','delete')
  GROUP BY a.table_id,
    b.title
  ORDER BY num DESC) c
LIMIT 0, 10;

--最多的查询TOP10
monitor.optTimes.query:
SELECT
  title,num
FROM
  (SELECT
    a.table_id,
    b.title,
    count(1) AS num
  FROM
    aolie_s_log a,
    aolie_dm_table b
  WHERE a.table_id = b.table_id
  AND a.oper_type  ='query'
  GROUP BY a.table_id,
    b.title
  ORDER BY num DESC) c
LIMIT 0, 10;

--最多的查询TOP10
monitor.opertimebyhour:
SELECT
  title,num
FROM
  (SELECT
   DATE_FORMAT(start_time,'%H') AS title,
    COUNT(1) AS num
  FROM
    aolie_s_log a
  WHERE
  schema_id IS NOT NULL AND user_id  IS NOT NULL
  GROUP BY title
  ORDER BY title ) c;

--最多的URL服务TOP10
monitor.controllerServiceTime:
SELECT
  title,num
FROM
  (SELECT
    a. path AS title,
    COUNT(1) AS num
  FROM
    aolie_s_log a
  WHERE  a.log_type=2
  GROUP BY a.path
  ORDER BY num DESC) c
LIMIT 0, 10;

--在线人员时间分布
monitor.onlineUserSeperate:
SELECT
  title,num
FROM
  (SELECT
    a. path AS title,
    COUNT(1) AS num
  FROM
    aolie_s_log a
  WHERE  a.log_type=2
  GROUP BY a.path
  ORDER BY num DESC) c
LIMIT 0, 10;

monitor.plug.init:
