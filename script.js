import http from 'k6/http';
import { sleep, check } from 'k6';

export const options = {
  vus: 10,
  duration: '60s',
};

export default function() {
  let res = http.get('http://localhost:8080/users/search?page=0&size=10&sortBy=name&sortDir=asc');
  check(res, { "status is 200": (res) => res.status === 200 });
  sleep(1);
}
