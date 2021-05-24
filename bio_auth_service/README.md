# Bio Auth Service

aka: Biomatcher, Biotemplate handler, identity escrow service, identity template service

## Quickstart

Can be run in isolation from the rest of the stack with docker-compose.local.yml in this directory. It does not, however, include use of the
Bioanalyzer Service, so make sure to set the environment variable BIOANALYZER_ENABLED to false before running, or alter application.conf
to set bioanalyzer.enabled = false.

## Dependencies

### Language / Libraries

- Kotlin
- JVM 11
- Gradle 6.5
- Ktor
- Logback
- Hikari
- Jdbi
- Flyway

For the specific version used for each of these dependencies, see `gradle.properties`.

### Service

- bioanalyzer (plaintext HTTP, no authentication required, internal to VPC)

### Data

Identity service requires the identity template database to both store computed templates and recover them.

### Env / Config

This service reads dynamic config information from environment variables, but attempts to provide sensible default values in
`src/main/resources/application.conf`. In that same file you can see the expected environment variable names if you wish to override those
defaults.

## Ownership

Maintainer: Jeff Kennedy
Operations: @voutasaurus

## Features / Aspects

The core features of the identity service are:
- Fingerprint image verification (by comparison against a stored template)
- Fingerprint template verification (by comparison against a stored template)
- Fingerprint template generation (from source fingerprint image)
- Fingerprint template storage (from ANSI-378, ISO-19794-2, or SourceAFIS v3)

Primary API endpoints:
- `POST /verify`: Checks a caller provided fingerprint against the fingerprint template owned by the ID provided
- `POST /save`: Create & store fingerprint templates in bulk

Informational:
- `GET /healthz`: Always returns HTTP 200 (used to confirm that the HTTP server in the container is up)
- `POST /positions`: Returns the positions of the best quality fingerprint images for the given filters

DEPRECATED:
- `POST /templatizer/bulk/template`: Create & store fingerprint templates in bulk
- `GET /positions/template/{filter}`: Returns the positions of the best quality fingerprint images for the given filters

Note here the following meanings:
- filter: query to identify an individual (e.g. nationalId=12345, dids=abcd1234,efgh5678)
- position: which finger position on the hand is being referred to (e.g. right pinky finger).

### Finger positions
The following are valid fingerprint positions:

- 1: right_thumb
- 2: right_index
- 3: right_middle
- 4: right_ring
- 5: right_pinky
- 6: left_thumb
- 7: left_index
- 8: left_middle
- 9: left_ring
- 10: left_pinky

## Usage

Test fingerprints can be found here: `test/resources/images/`

### Save an Identity (Using a fingerprint image)

```
POST /api/v1/save
Accept: application/json
Content-Type: application/json

{
    "id": "abcd1234",
    "filters": {
        "national_id": "NIN55555",
        "voter_id": "VID11111"
    },
    "params": {
        "type_id": 1,
        "capture_date": "2011-12-03T10:15:30+01:00",
        "position": 1,
        "image": "<base64-encoded string goes here>"
    }
}
```

### Save an Identity (Using a fingerprint template)

```
POST /api/v1/save
Accept: application/json
Content-Type: application/json

{
    "id": "abcd1234",
    "filters": {
        "national_id": "NIN55555",
        "voter_id": "VID11111"
    },
    "params": {
        "type_id": 1,
        "capture_date": "2011-12-03T10:15:30+01:00",
        "position": 1,
        "template": "<base64-encoded string goes here>"
    }
}
```

### Verify an Identity (Using a fingerprint template)

```
POST /api/v1/verify
Accept: application/json
Content-Type: application/json

{
	"backend": "template",
	"imageType": "TEMPLATE",
	"filters": {
		"dids": "abcd1234"
	},
	"params": {
	    "image": "<base64-encoded string goes here>",
	    "position": 1
	}
}
```

### Verify an Identity (Using a fingerprint image)

```
POST /api/v1/verify
Accept: application/json
Content-Type: application/json

{
	"backend": "template",
	"imageType": "IMAGE",
	"filters": {
		"dids": "abcd1234"
	},
	"params": {
	    "image": "<base64-encoded string goes here>",
	    "position": 1
	}
}
```
