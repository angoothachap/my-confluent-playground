1. The docker-compose.yaml includes splunk enterprise image. Run below commands:
    ```docker-compose up -d```
    ```docker ps -a```
    Copy the container ID
    ```docker exec -it <container_id> bash```
2. Next, from inside the splunk container, enable the HTTP Event Collector token using cURL
    ```curl -k -X "POST" -u admin:changeme https://localhost:8089/servicesNS/admin/splunk_httpinput/data/inputs/http/splunk_hec_token/enable```
4. Enable HTTP Event Collector using cURL
    ```curl -k -X "POST" -u admin:changeme https://localhost:8089/servicesNS/admin/splunk_httpinput/data/inputs/http/http/enable```
5. Create index called logs
6. Post Sample output JSON from the metrics API as described [here](https://api.telemetry.confluent.cloud/docs?_ga=2.268700853.512129039.1635115928-1595393998.1629408101#tag/Version-2/paths/~1v2~1metrics~1{dataset}~1query/post)

        OR 
the prometheus-format [endpoint](https://api.telemetry.confluent.cloud/docs?_ga=2.57405038.512129039.1635115928-1595393998.1629408101#tag/Version-2/paths/~1v2~1metrics~1{dataset}~1export/get

HEC knows how to parse the JSON sent to it as an event so it would work on any JSON that's out by either endpoints of the metrics API

```
    curl -k -X "POST" "https://localhost:8088/services/collector" \
   -H "Authorization: Splunk 00000000-0000-0000-0000-0000000000000" \
   -d '{"index":"logs", "sourcetype": "_json", "event": [{"timestamp":"2019-10-17T20:17:00.000Z","metric.topic":"foo","value":9741},{"timestamp":"2019-10-17T20:18:00.000Z","metric.topic":"foo","value":9246},{"timestamp":"2019-10-17T20:17:00.000Z","metric.topic":"bar","value":844.1},{"timestamp":"2019-10-17T20:18:00.000Z","metric.topic":"bar","value":821.1}]}'

```

This example is based on https://github.com/signalfx/splunk-otel-collector/tree/main/examples/splunk-hec

 
