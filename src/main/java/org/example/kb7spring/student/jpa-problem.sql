INSERT INTO classroom (room_name) VALUES ('KB7-26');
INSERT INTO classroom (room_name) VALUES ('KB7-27');
INSERT INTO classroom (room_name) VALUES ('KB7-28');

INSERT INTO student (name, role, specialty, status, classroom_id)
VALUES ('홍길동', 'STUDENT', 'JAVA', 'ACTIVE', 1);

INSERT INTO student (name, role, specialty, status, classroom_id)
VALUES ('이순신', 'STUDENT', 'SPRING', 'ACTIVE', 1);

INSERT INTO student (name, role, specialty, status, classroom_id)
VALUES ('강감찬', 'STUDENT', 'DB', 'ACTIVE', 2);

INSERT INTO student (name, role, specialty, status, classroom_id)
VALUES ('유관순', 'STUDENT', 'WEB', 'ACTIVE', 2);

INSERT INTO student (name, role, specialty, status, classroom_id)
VALUES ('김구', 'STUDENT', 'JAVA', 'INACTIVE', 3);