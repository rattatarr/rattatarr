#!/bin/sh
set -e

java \
  -XX:+UseContainerSupport \
  -Xms64m \
  -Xmx1024m \
  -XX:+UseZGC \
  -Dspring.datasource.url="jdbc:sqlite:/data/rattatarr.db?journal_mode=WAL" \
  -jar /app/app.jar &

JAVA_PID=$!

sleep 2

exec caddy run --config /etc/caddy/Caddyfile --adapter caddyfile
