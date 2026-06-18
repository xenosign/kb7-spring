USE `kb7spring`;

CREATE TABLE student
(
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    name        VARCHAR(100) NOT NULL,
    role        VARCHAR(20)  NOT NULL,
    specialty   VARCHAR(100),
    status      VARCHAR(100)
);

INSERT INTO student (name, role, specialty, status)
VALUES ('이효석', '강사', '늙음', '늙어서 피곤함'),
       ('순자', '교육생', '순자', '순자');

select *
from student;