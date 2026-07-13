import http from 'k6/http';
import { check, group, sleep } from 'k6';
import { Trend, Rate } from 'k6/metrics';
import { BASE_URL } from '../config.js';

// StudentApiControllerV4 - 동시 등록 시 락 전략별(no-lock/optimistic/pessimistic/redis) 비교
// 엔드포인트가 호출 한 번마다 서버 내부에서 requestCount만큼 스레드를 띄워
// classroomId 상태(capacity/studentCount/학생목록)를 초기화하고 경합을 재현하므로,
// 같은 classroomId를 동시에 여러 VU가 두드리면 결과가 서로 오염된다. 반드시 VU=1로 실행할 것.
//
// 사전조건: classroomId로 지정한 반이 DB에 존재해야 함
//   (student/jpa-problem.sql 을 실행해두면 classroom id=1~3 이 생성됨)
//
// 실행 예:
//   k6 run -e BASE_URL=http://localhost:8080/<context-path> \
//          -e CLASSROOM_ID=1 -e REQUEST_COUNT=50 -e CAPACITY=10 -e REPEAT=5 \
//          k6/scenarios/student-v4-concurrency.js

const CLASSROOM_ID = __ENV.CLASSROOM_ID || '1';
const REQUEST_COUNT = __ENV.REQUEST_COUNT || '50';
const CAPACITY = __ENV.CAPACITY || '10';

const STRATEGIES = [
  { key: 'race', path: 'race', mustNotOverCapacity: false },
  { key: 'optimistic', path: 'optimistic', mustNotOverCapacity: true },
  { key: 'pessimistic', path: 'pessimistic', mustNotOverCapacity: true },
  { key: 'redis', path: 'redis', mustNotOverCapacity: true },
];

const durationTrend = {};
const overCapacityRate = {};
STRATEGIES.forEach(({ key }) => {
  durationTrend[key] = new Trend(`duration_${key}`, true);
  overCapacityRate[key] = new Rate(`over_capacity_${key}`);
});

export const options = {
  vus: 1,
  iterations: Number(__ENV.REPEAT) || 5,
  thresholds: {
    // 락이 적용된 세 전략은 정원 초과가 절대 발생하면 안 됨
    'over_capacity_optimistic': ['rate==0'],
    'over_capacity_pessimistic': ['rate==0'],
    'over_capacity_redis': ['rate==0'],
  },
};

export default function () {
  STRATEGIES.forEach(({ key, path, mustNotOverCapacity }) => {
    group(key, () => {
      const url = `${BASE_URL}/api/student/v4/classroom/${CLASSROOM_ID}/${path}` +
        `?requestCount=${REQUEST_COUNT}&capacity=${CAPACITY}`;
      const res = http.get(url, { tags: { strategy: key } });

      check(res, { 'status is 200': (r) => r.status === 200 });

      durationTrend[key].add(res.timings.duration);

      const body = res.json();
      overCapacityRate[key].add(body.overCapacity === true);

      check(body, {
        'actualEnrolledCount <= capacity': (b) => b.actualEnrolledCount <= b.capacity,
      });

      if (mustNotOverCapacity) {
        check(body, { [`${key}: overCapacity is false`]: (b) => b.overCapacity === false });
      }

      console.log(
        `[${key}] duration=${res.timings.duration.toFixed(1)}ms ` +
        `success=${body.successCount} enrolled=${body.actualEnrolledCount} ` +
        `capacity=${body.capacity} overCapacity=${body.overCapacity}`
      );
    });
  });

  sleep(0.5);
}
