#!/bin/sh
set -e
exec bws run \
  ${BWS_PROJECT_ID:+--project-id "$BWS_PROJECT_ID"} \
  -- java -jar /app/app.jar
