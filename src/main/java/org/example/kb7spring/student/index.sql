CREATE TABLE index_overhead_test (
                                     id   BIGINT AUTO_INCREMENT PRIMARY KEY,
                                     col1 VARCHAR(50),
                                     col2 VARCHAR(50),
                                     col3 VARCHAR(50),
                                     col4 VARCHAR(50),
                                     col5 VARCHAR(50),
                                     col6 VARCHAR(50),
                                     col7 VARCHAR(50),
                                     col8 VARCHAR(50),
                                     col9 VARCHAR(50),
                                     col10 VARCHAR(50)
);

-- ################ --

SET @t1 = NOW(6);

INSERT INTO index_overhead_test
(col1, col2, col3, col4, col5, col6, col7, col8, col9, col10)
SELECT name,
       role,
       specialty,
       status,
       CONCAT(name, '_5'),
       CONCAT(role, '_6'),
       CONCAT(specialty, '_7'),
       CONCAT(name, '_8'),
       CONCAT(role, '_9'),
       CONCAT(name, '_10')
FROM student
WHERE id BETWEEN 1 AND 100000;

SELECT TIMESTAMPDIFF(MICROSECOND, @t1, NOW(6)) / 1000 AS '인덱스 없음 INSERT (ms)';

-- ################ --

ALTER TABLE index_overhead_test
    ADD INDEX idx_c1  (col1),
    ADD INDEX idx_c2  (col2),
    ADD INDEX idx_c3  (col3),
    ADD INDEX idx_c4  (col4),
    ADD INDEX idx_c5  (col5),
    ADD INDEX idx_c6  (col6),
    ADD INDEX idx_c7  (col7),
    ADD INDEX idx_c8  (col8),
    ADD INDEX idx_c9  (col9),
    ADD INDEX idx_c10 (col10);

-- ################ --

SET @t2 = NOW(6);

INSERT INTO index_overhead_test
(col1, col2, col3, col4, col5, col6, col7, col8, col9, col10)
SELECT name,
       role,
       specialty,
       status,
       CONCAT(name, '_5'),
       CONCAT(role, '_6'),
       CONCAT(specialty, '_7'),
       CONCAT(name, '_8'),
       CONCAT(role, '_9'),
       CONCAT(name, '_10')
FROM student
WHERE id BETWEEN 100001 AND 200000;

SELECT TIMESTAMPDIFF(MICROSECOND, @t2, NOW(6)) / 1000 AS '인덱스 10개 INSERT (ms)';

--

SELECT table_name,
       ROUND(data_length / 1024 / 1024, 1)  AS 'data (MB)',
    ROUND(index_length / 1024 / 1024, 1) AS 'index (MB)'
FROM information_schema.TABLES
WHERE table_schema = DATABASE()
  AND table_name = 'index_overhead_test';
