-- Script to fix database lock issues
-- Run this in MySQL Workbench or MySQL CLI

USE fix4home_db;

-- 1. Show current locks and blocking transactions
SELECT 
    r.trx_id waiting_trx_id,
    r.trx_mysql_thread_id waiting_thread,
    r.trx_query waiting_query,
    b.trx_id blocking_trx_id,
    b.trx_mysql_thread_id blocking_thread,
    b.trx_query blocking_query
FROM information_schema.innodb_lock_waits w
INNER JOIN information_schema.innodb_trx b ON b.trx_id = w.blocking_trx_id
INNER JOIN information_schema.innodb_trx r ON r.trx_id = w.requesting_trx_id;

-- 2. Show all running transactions
SELECT 
    trx_id,
    trx_state,
    trx_started,
    trx_requested_lock_id,
    trx_wait_started,
    trx_weight,
    trx_mysql_thread_id,
    trx_query,
    TIME_TO_SEC(TIMEDIFF(NOW(), trx_started)) as seconds_running
FROM information_schema.innodb_trx
ORDER BY trx_started;

-- 3. Show processes
SHOW PROCESSLIST;

-- 4. Kill long-running transactions (BE CAREFUL!)
-- UNCOMMENT ONLY IF YOU WANT TO KILL LONG TRANSACTIONS
-- SET @timeout = 60; -- seconds
-- SELECT CONCAT('KILL ', id, ';') 
-- FROM information_schema.processlist 
-- WHERE time > @timeout 
-- AND command NOT IN ('Sleep', 'Binlog Dump')
-- AND user != 'system user';

-- 5. Optimize notification table to prevent locks
ANALYZE TABLE notifications;
OPTIMIZE TABLE notifications;

-- 6. Check innodb status
SHOW ENGINE INNODB STATUS\G

-- 7. Recommended: Increase lock wait timeout (optional)
-- Current setting
SHOW VARIABLES LIKE 'innodb_lock_wait_timeout';

-- Increase to 120 seconds (default is 50)
-- SET GLOBAL innodb_lock_wait_timeout = 120;

