CREATE DATABASE `kb7spring`;

USE `kb7spring`;

CREATE TABLE member
(
    id    BIGINT AUTO_INCREMENT PRIMARY KEY,
    email VARCHAR(255) NOT NULL UNIQUE,
    birth VARCHAR(100) NOT NULL,
    name  VARCHAR(100) NOT NULL,
    grade VARCHAR(50)  NOT NULL,
    asset BIGINT DEFAULT 0
);

INSERT INTO member (email, birth, name, grade, asset)
VALUES ('ronaldo@example.com', '1985. 02. 05', '호날두', '플래티넘', 300000000000),
       ('songjk@example.com', '1985. 09. 19', '송중기', '골드', 80000000000),
       ('xenosign@example.com', '1985. 11. 18', '이효석', '아이언', 10);

select *
from member;