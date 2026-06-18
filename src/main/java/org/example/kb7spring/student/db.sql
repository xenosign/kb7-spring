USE `kb7spring`;

CREATE TABLE student
(
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    name        VARCHAR(100) NOT NULL,
    specialty   VARCHAR(100),
    status      VARCHAR(100),
    role        VARCHAR(20)  NOT NULL
);

INSERT INTO student (name, specialty, status, role)
VALUES ('이효석', '늙음', '늙어서 피곤함', '강사'),
       ('순자', '순자', '순자', '교육생');

select *
from student;