# Демо по Prometheus и Grafana
## Установка
1. Добавляем репозиторий: \
`helm repo add prometheus-community https://prometheus-community.github.io/helm-charts` \
`helm repo update`
2. Устанавливаем kube-prometheus-stack: \
`helm install prometheus prometheus-community/kube-prometheus-stack \` \
`--set prometheus.prometheusSpec.serviceMonitorSelectorNilUsesHelmValues=false \` \
`--set grafana.enabled=true`

## Запуск приложения
1. В директории monitorable-application собираем образ: `minikube image build -t monitorable-application:1.0 .`
2. `minikube image ls` проверяем, что внутри minikube появился образ `docker.io/library/monitorable-application:1.0`
3. Запускаем приложение: \
`kubectl apply \` \
`-f k8s/deployment.yaml \` \
`-f k8s/service.yaml \` \
`-f k8s/service-monitor.yaml`

## Запуск нагрузочного теста
1. В директории load-test собираем образ: `minikube image build -t load-test:1.0 .`
2. minikube image ls `проверяем, что внутри minikube появился образ ` docker.io/library/load-test:1.0`
3. Запускаем pod с нагрузочным тестом `kubectl apply -f k8s/pod.yaml`

## Prometheus
1. Запускаем port-forward для Prometheus `minikube service prometheus-kube-prometheus-prometheus` и открываем Prometheus
2. Убеждаемся, что в `Target Health` появились эндпоинты для сбора метрик с `minitorable-application`
3. Находим метрики `http_server_requests_seconds_count`
4. Отфильтровываем метрики `http_server_requests_seconds_count` по лейблу `uri=/api/hello`
5. Используем функции `increase` или `rate` для метрики `http_server_requests_seconds_count`, чтобы получить кол-во запросов за 5 минут
6. Используем функцию `sum` для метрики `http_server_requests_seconds_count`, чтобы получить суммарную метрику по двум подам
7. Используем функцию `sum` для метрики `http_server_requests_seconds_count` с агрегацией по лейблу `status`
8. Получаем доступность сервиса через деление кол-ва успешных запросов (`status=200`) на общее кол-во запросов
9. Получаем среднее время выполнение запросов `/api/hello` через деления суммарного времени выполнения запросов `http_server_requests_seconds_sum` на кол-во запросов `http_server_requests_seconds_count`
10. Получаем 95 персентиль времени выполнение запросов `/api/hello` с использованием функции `histogram_quantile`

## Grafana
1. Получаем пароль пользователя admin в Grafana `kubectl --namespace default get secrets prometheus-grafana -o jsonpath="{.data.admin-password}" | base64 -d ; echo`
2. Запускаем port-forward для Grafana `minikube service prometheus-grafana` и открываем Grafana
3. Смотрим дашборды `Node Exporter / Nodes`, `Kubernetes / Kubelete`
4. Импортируем дашборд [JVM (Micrometer)](https://grafana.com/grafana/dashboards/4701-jvm-micrometer/) в Grafana
5. Создаем новый дашборд. 
6. Добавляем панель Bar Chart с запросом `sum by (le) (increase(http_server_requests_seconds_bucket{uri="/api/hello", status=~"2.."}[1m]))` и выбираем в опциях тип `Instant` и формат `Heatmap`
7. Добавляем панель Heatmap с запросом `sum by (le) (increase(http_server_requests_seconds_bucket{uri="/api/hello", status=~"2.."}[1m]))` и выбираем в опциях формат `Heatmap`
8. Для monitorable-application в deployment.yaml изменяем переменные окружения delay-mean на 600 и delay-deviation на 100, передеплоиваем приложение `kubectl apply -f k8s/deployment.yaml`
9. Смотрим, как изменилась Heatmap

## Очистка
1. Удаляем приложение: \
`kubectl delete deployment monitorable-application-deployment && \` \
`kubectl delete service monitorable-application-srv && \` \
`kubectl delete servicemonitor monitorable-application-monitor && \` \
`kubectl delete pod load-test`
2. Удаляем kube-prometheus-stack: `helm uninstall prometheus prometheus-community/kube-prometheus-stack`