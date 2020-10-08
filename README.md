# guardian-bio-auth

This repo contains services and tools for using biometric fingerprint authentication with the aries-key-guardian.
Specifically, the main contents are a Bioanalyzer Service, Identity Service, Fingerprint Desktop Tool, and scripts for
the two databases that are used by Identity Service - `identity_intelligence_db` and `identity_template_db`.

## Bioanalyzer Service

The job of this service is to perform common analyses on biometric images. At the moment, it only supports fingerprint
images. The main job it performs is to determine the quality of the image provided, returning a score from 0 to 100, as
defined by ISO/IEC 29794-1:2016.

For more information, please take a look at [Bioanalyzer Service's README](bioanalyzer_service/README.md). 

## Identity Service

The job of this service is to manage access to generified biometric information. At the moment, that means fingerprint
templates. Given a fingerprint image (or several images), Identity Service will generate templates based on those images
and store them in `identity_template_db`. Later, when presented with a candidate fingerprint template, Identity Service
can verify that it does or does not match some fingerprint template that it has previously stored.

For more information, please take a look at [Identity Service's README](identity_service/README.md).

## Fingerprint Desktop Tool

The job of this tool is to provide a CLI for generating fingerprint templates based on a provided image.

For more information, please take a look at [Fingerprint Desktop Tool's README](fingerprint_desktop_tool/README.md).

## Running Guardian Bio Auth Locally

1. Make sure you have the Gradle installed. The easiest way to do this if you're on mac is to use homebrew:
`brew install gradle`. But if homebrew isn't an option, or you'd just prefer an alternate approach, check out
[their installation page](https://gradle.org/install/).

2. Make sure you have docker-compose installed. If you're running on a Mac, it should already be installed. But for full
installation instructions for all environments, see [Docker's documentation](https://docs.docker.com/compose/install/).

3. Generate `.env` files for each repository. You can execute `./scripts/useDummyEnvFiles.sh` to use the default dummy
values we provide, or manually create `.env` files with custom values. If you choose to take the manual approach, check
out the `dummy.env` files in each sub-project for the list of required environment variables.

4. Run the docker-compose. This will spin up a local network that the services and databases can communicate with each
other on. From the top-level guardian-bio-auth directory, execute: `docker-compose up`.

## Running Guardian Bio Auth Elsewhere

Bioanalyzer Service and Identity Service both have public images available in the kivaprotocol
[Dockerhub account](https://hub.docker.com/orgs/kivaprotocol/repositories). They are called
[bioanalyzer](https://hub.docker.com/repository/docker/kivaprotocol/bioanalyzer) and
[identity](https://hub.docker.com/repository/docker/kivaprotocol/identity), respectively. Feel free to pull them down
and use them in your own deployments as you see fit.

## Running Tests

Assuming you have gradle installed, running the tests is as simple as:

    cd identity_service
    ./gradlew test

and

    cd bioanalyzer_service
    ./gradlew test
