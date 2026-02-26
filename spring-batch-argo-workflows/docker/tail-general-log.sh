#!/bin/bash
docker exec -it 16e7b36a6ea1 tail -f -n 100 /var/lib/mysql/16e7b36a6ea1.log
