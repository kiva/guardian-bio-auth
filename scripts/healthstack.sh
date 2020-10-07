#!/bin/bash
set -ev

# This script waits for the bio auth stack to be up

# wait for identity-service to be up
./scripts/healthcheck.sh http://localhost:8081/healthz

# wait for bioanalyzer-service to be up
./scripts/healthcheck.sh http://localhost:8089/healthz
