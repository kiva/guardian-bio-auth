#!/bin/bash

echo "This script must be run from the top-level guardian-bio-auth directory. If you run it from anywhere else, it will fail."

cp bioanalyzer_service/dummy.env bioanalyzer_service/.env
cp identity_service/dummy.env identity_service/.env
cp identity_intelligence_db/dummy.env bioanalyzer_service/.env
cp identity_template_db/dummy.env bioanalyzer_service/.env