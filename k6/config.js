// 로컬 Tomcat 배포 시 컨텍스트 경로가 다르면 BASE_URL 환경변수로 override
// 예) k6 run -e BASE_URL=http://localhost:8080/kb7-spring-1.0-SNAPSHOT ...
export const BASE_URL = __ENV.BASE_URL || 'http://localhost:8080';

