#!/bin/bash

# Updates .env files for the provided platform. Note this *will* overwrite existing values.
# Since the archive file contains base64 encoded env variables, they must be decoded first.

echo Expectation is this script is running in guardian-bio-auth root directory

readonly DEV_ENV_FILES_ARCHIVE_NAME="devEnvFiles.contents"

cfile=""
while IFS= read -r line; do
        if [ "$cfile" == "" ]; then
                cfile=$line
        else
                echo -e "$line" | base64 --decode > $cfile
		cfile=""
        fi
done < $DEV_ENV_FILES_ARCHIVE_NAME
