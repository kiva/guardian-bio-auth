#!/bin/bash

# Parse command line parameters, if any
HELP=false
FORCE=false
while [[ $# -gt 0 ]]
do
  if [[ "$1" == "-h" || "$1" == "--help" ]]
  then
    HELP=true
  elif [[ "$1" == "-f" || "$1" == "--force" ]]
  then
    FORCE=true
  fi
  shift
done

# Execute. If help is requested, don't actually execute the script.
if [[ "$HELP" == true ]]
then
  echo "Apply dummy environment variables to each of the repos contained in guardian-bio-auth"
  echo ""
  echo "USAGE:  useDummyEnvFiles.sh [OPTIONS]"
  echo ""
  echo "Options:"
  echo "-h, --help     Display this help message"
  echo "-f, --force    Force execute the useDummyEnvFiles.sh script without checking the current working directory"
elif [[ "$FORCE" == true || "$PWD" == */guardian-bio-auth ]]
then
  cp bioanalyzer_service/dummy.env bioanalyzer_service/.env
  cp identity_template_db/dummy.env identity_template_db/.env
  echo "Success"
  exit 0
else
  echo "This script must be run from the top-level guardian-bio-auth directory. You are currently running it from: $PWD"
  exit 1
fi