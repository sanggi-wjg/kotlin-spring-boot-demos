```shell
CREATE USER 'exporter'@'%' IDENTIFIED BY 'passw0rd' WITH MAX_USER_CONNECTIONS 3;
GRANT PROCESS, REPLICATION CLIENT, SELECT ON *.* TO 'exporter'@'%';
FLUSH PRIVILEGES;
```

https://grafana.com/grafana/dashboards/14057-mysql/