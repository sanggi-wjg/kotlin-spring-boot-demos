#!/usr/bin/env bash

while true; do
  echo "$(date +%H:%M:%S) RSS(KB): $(ps -o rss= -p "$1")"
  sleep 1
done
