#!/bin/sh

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
    "transforms.outbox.type" : "io.debezium.transforms.outbox.EventRouter",
    "transforms.outbox.table.field.event.key": "aggregate_id",
    "transforms.outbox.table.field.event.timestamp": "created_at",
    "transforms.outbox.table.field.event.payload": "payload",
    "transforms.outbox.table.json.payload.null.behavior": "ignore",
    "transforms.outbox.table.fields.additional.error.on.missing": "false",
    "transforms.outbox.route.by.field": "aggregate_type",
    "transforms.outbox.route.topic.replacement" : "${routedByValue}.events",
    "transforms.outbox.table.fields.additional.placement": "event_type:header:eventType",
    "schema.history.internal.kafka.bootstrap.servers": "tob-kafka:29092",
    "schema.history.internal.kafka.topic": "schema-changes.demo"
  }
}'
