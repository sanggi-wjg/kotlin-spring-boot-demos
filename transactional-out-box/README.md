# Transactional Outbox Pattern

# Install

## Add connector

```shell
curl -i -X POST -H "Accept:application/json" -H "Content-Type:application/json" localhost:8083/connectors/ -d '{
  "name": "demo-mysql-connector",
  "config": {
    "connector.class": "io.debezium.connector.mysql.MySqlConnector",
    "tasks.max": "1",
    "database.hostname": "tob-mysql",
    "database.port": "3306",
    "database.user": "root",
    "database.password": "rootroot",
    "database.server.id": 1,
    "topic.prefix": "mysql-tob-1",
    "database.include.list": "demo",
    "table.include.list": "demo.outbox",
    "tombstones.on.delete" : "false",
    "value.converter": "org.apache.kafka.connect.json.JsonConverter",
    "transforms" : "outbox",
    "transforms.outbox.type" : "io.debezium.transforms.outbox.EventRouter"
  }
}'

# "transforms.outbox.table.expand.json.payload": "true",
#"transforms.outbox.route.topic.replacement" : "${routedByValue}.events",
#"transforms.outbox.table.fields.additional.placement" : "type:header:eventType",
#"schema.history.internal.kafka.bootstrap.servers": "tob-kafka:29092",
#"schema.history.internal.kafka.topic": "schema-changes.demo",
#"include.schema.changes": "false"


> After Request
HTTP/1.1 201 Created
Date: Fri, 16 Aug 2024 11:08:25 GMT
Location: http://localhost:8083/connectors/demo-mysql-connector
Content-Type: application/json
Content-Length: 833
Server: Jetty(9.4.52.v20230823)

{"name":"demo-mysql-connector","config":{"connector.class":"io.debezium.connector.mysql.MySqlConnector","tasks.max":"1","database.hostname":"tob-mysql","database.port":"3306","database.user":"root","database.password":"rootroot","database.server.id":"1","topic.prefix":"mysql-1","database.include.list":"demo","table.include.list":"demo.outbox","tombstones.on.delete":"false","transforms":"outbox","transforms.outbox.type":"io.debezium.transforms.outbox.EventRouter","transforms.outbox.route.topic.replacement":"${routedByValue}.events","transforms.outbox.table.fields.additional.placement":"type:header:eventType","schema.history.internal.kafka.bootstrap.servers":"tob-kafka:29092","schema.history.internal.kafka.topic":"schema-changes.demo","include.schema.changes":"true","name":"demo-mysql-connector"},"tasks":[],"type":"source"}%     
```

## Ref

* https://debezium.io/documentation/reference/stable/transformations/outbox-event-router.html
* https://github.com/debezium/debezium-examples/tree/main/outbox
* https://www.youtube.com/watch?v=uk5fRLUsBfk&list=WL&index=3&t=1986s