#!/bin/bash
  
# Finds .env and .env.* files on the local system, then encodes their contents in base64 and packages them into a
# .contents text file for easy sharing.

echo Expectation is this script is running in guardian-bio-auth root directory

ENV_FILES=$(find . -name '.env*' ! -name '.env.swp' ! -name '.-.env' ! -path '*/devEnvFiles/*')

readonly DEV_ENV_FILES_ARCHIVE_NAME="devEnvFiles.contents"

#Delete devEnvFile.contents if it exists
rm -f $DEV_ENV_FILES_ARCHIVE_NAME

for f in $ENV_FILES
do
        echo -e $f >> $DEV_ENV_FILES_ARCHIVE_NAME
        cat $f | base64 >> $DEV_ENV_FILES_ARCHIVE_NAME
done

