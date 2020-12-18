#!/bin/bash
set -ev

# This script waits for the bio auth stack to be up

# wait 5 minutes for identity-service to be up
./scripts/healthcheck.sh http://localhost:8081/healthz 300

# wait 90 seconds for bioanalyzer-service to be up
./scripts/healthcheck.sh http://localhost:8089/healthz
