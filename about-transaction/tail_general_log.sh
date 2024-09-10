#!/bin/bash

CONTAINER_NAME="29cc58f9b929"
MYSQL_ROOT_PASSWORD="rootroot"

ENABLE_GENERAL_LOG="
SET GLOBAL general_log = 'ON';
SHOW VARIABLES LIKE 'general_log_file';
"

docker exec -i $CONTAINER_NAME mysql -u root -p$MYSQL_ROOT_PASSWORD -e "$ENABLE_GENERAL_LOG"

LOG_FILE=$(docker exec -i $CONTAINER_NAME mysql -u root -p$MYSQL_ROOT_PASSWORD -e "SHOW VARIABLES LIKE 'general_log_file';" | grep general_log_file | awk '{print $2}')
echo "Tailing MySQL General Log at $LOG_FILE"
docker exec -it $CONTAINER_NAME tail -f -n 100 $LOG_FILE
