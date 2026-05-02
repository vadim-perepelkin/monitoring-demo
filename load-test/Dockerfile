FROM grafana/k6:1.7.1
COPY test.js /scripts/test.js
ENTRYPOINT ["k6", "run", "/scripts/test.js"]