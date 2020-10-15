#!/bin/bash

if [[ "$PWD" == */guardian-bio-auth ]];
then
  cp bioanalyzer_service/dummy.env bioanalyzer_service/.env
  cp identity_service/dummy.env identity_service/.env
  cp identity_intelligence_db/dummy.env identity_intelligence_db/.env
  cp identity_template_db/dummy.env identity_template_db/.env
  echo "Success"
else
  echo "This script must be run from the top-level guardian-bio-auth directory. You are currently running it from: $PWD"
fi