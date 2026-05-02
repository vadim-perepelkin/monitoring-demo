import http from 'k6/http';

export const options = {
    duration: '87600h',
    vus: 5,
    noConnectionReuse: true
};

export default function () {
  const response = http.get('http://monitorable-application-srv:8080/api/hello');
}