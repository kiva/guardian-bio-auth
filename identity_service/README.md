# Identity Service

aka: Biomatcher, Biotemplate handler, identity escrow service, identity template service

## Quickstart

Can be run in isolation from the rest of the stack with docker compose in this
directory (functionality subject to dependencies).

## Dependencies

Dependencies are components which are required to build and run the service
locally and in production. Dependencies include language, libraries, other
services, data, config, environment, platform.

### Language / Libraries

- Kotlin (JVM)
- Gradle
- Spring Boot
- Reactive programming paradigm

### Service

- bioanalyzer (plaintext HTTP, no authentication required, internal to VPC)

### Data

Identity service requires the identity template database to both store computed
templates and recover them.

### Env / Config

This service reads dynamic config information from environment variables.
Static config is provided in the form of a config file bundled with the service
container.

The following environment variables are set (as of 2020-09-24):
```
BIOANALYZER_ENABLED
BIOANALYZER_QUALITY_THRESHOLD
BIOANALYZER_SERVICE_URL
HASH_PEPPER
IDENTITYDB_TEMPLATE_CITIZEN_TABLE
IDENTITYDB_TEMPLATE_POSTGRES_DB
IDENTITYDB_TEMPLATE_POSTGRES_HOST
IDENTITYDB_TEMPLATE_POSTGRES_PASSWORD
IDENTITYDB_TEMPLATE_POSTGRES_PORT
IDENTITYDB_TEMPLATE_POSTGRES_USER
IDENTITYINTELLIGENCEDB_POSTGRES_DRIVER
IDENTITYINTELLIGENCEDB_POSTGRES_PASSWORD
IDENTITYINTELLIGENCEDB_POSTGRES_URL
IDENTITYINTELLIGENCEDB_POSTGRES_USER
REPLAY_ATTACK_ENABLED
```

TODO: deprecate the environment variables that are no longer used.

## Ownership

Development: Jeff Kennedy (backup from Salton Massally)

Operations: @voutasaurus

## Features / Aspects

The core features of the identity service are:
- Fingerprint image verification (by comparison against a template)
- Fingerprint template generation (from source fingerprint image)

Primary API endpoints:
- `/verify`: checks a caller provided fingerprint against the fingerprint template owned by the ID provided

Secondary API endpoints:
- `/templatizer/bulk/{backend}`: create fingerprint templates in bulk

Informational / vestigial / unknown:
- `/healthz`: always returns HTTP 200 (used to confirm that the HTTP server in the container is up)
- `/positions/{backend}/{filter}`: ?
- `/fingerprint_image/{backend}/{filter}/{position}`: return fingerprint image for a given filter (do not expose at API gateway), only works if connected directly to a fingerprint database - which we no longer do.
- `/backend/{name}`: return information about the data backend named in the endpoint path

Note here the following meanings:
- backend: data source (see Notes section for more details)
- filter: query to identify an individual (e.g. nationalId=12345)
- position: which finger position on the hand is being referred to (e.g. right pinky finger).

### /verify

These are the input fields for a call to `/verify`.

| Attribute | Description                                                                                                                                                    | Required |
|-----------|----------------------------------------------------------------------------------------------------------------------------------------------------------------|----------|
| name      | Name of the backend we want to compare fingerprint against. Should be the same as that in backend definition                                                   | True     |
| image     | Base64 rep of the fingerprint capture                                                                                                                          | True     |
| position  | Finger position, for valid values see Finger Positions                                                                                                         | True     |
| filter    | Key:Value pairs of filters used to select candidates we are matching print against. Only filters declared in backend definition are allowed here               | True     |

## Known Bugs

- Datadog opentracing integration does not record a span for `/verify` for every call to `/verify`. The cause is unknown.

## Notes:

A test fingerprint can be found here: test/resources/images/fingerprint.jpg

Backends are connections to data stores or services from which a list of identity data is retrieved from and matched
against a provided fingerprint capture. Backends are declared in backend.yml. An example is shown below.

```markdown
dummy:
  name: dummy
  driver: org.kiva.identityservice.services.backends.drivers.DummyBackend
  positions:
      - right_thumb
      - left_thumb
  filters:
    nationalId:
      type: java.lang.String
      unique: true
      required: true
      to: national_id
      operator: =
      validators:
        -
    firstName:
      type: java.lang.String
      required: false
      validators:
        -
    lastName:
      type: java.lang.String
      required: false
      validators:
        -
  config:
    connection:
      uri: test
      username: secret
      password: secret
```

| Attribute | Purpose                                                                                                                              |
|-----------|------------------------------------------------------------------------------------------------------------------------------------------|
| name      | Name of the backend. This name will be specified in the query to match/validate identities, telling the service where to look for data   |
| driver    | The driver used to retrieve data from the backend. This is what is called to get candidates to compare the supplied fingerprints against |
| position  | A valid finger positions that queries to this backend has to conform to.for valid values see Finger Positions. At least one is required  |
| filters   | Each backend can specify the name of fields that can be used to filter/search for candidates                                             |
| config    | Configuration needed by the backend to make it work, e.g. the connection attributes of a mysql database                                  |

### Finger positions
The following are valid fingerprint positions:

- left_thumb
- left_index
- left_middle
- left_ring
- left_pinky
- right_thumb
- right_index
- right_middle
- right_ring
- right_pinky


## TODO
- Are there multiple fingerprint image recorded for the same position? If that is the case best have strategy for selecting the most recent (for investigation)
- If no entry in sync table it seems to not return any result
- Fix failing tests
- Sane error message + backend error handling
- Move auth details for backend out of backend to env variables
- Restrict accounts to backends
- Implement filter validators
- Coerce filter value types before sending to backend. Now it simple sends as string
- Benchmarking
- Metering via micrometer
- Should by default pull in templates to compare with image only as a fallback
- Change org.kiva.identityservice.StringToEnumConverterFactory to kotlin
- Hookup Backend healthcheck
- Hookup monitors to fuzzy matching
- Templatizer into a seperate service
    - Do we need a tasks that queues requests coming in as image to the templatizer for templatization? We now generate templates only using a scheduler
    - Once we get more backend we need a better selected backends based on when they are least busy (now we simple utc it)
    - Once we scale out to numerous instances, selected rows for templatizer should be locked to prevent another instances from reselecting it 
    - GZip template so we can get even smaller payloads 
    - Instead of voter_id & type_id & position, we instead just have cbi_id. This reduces the # of columns on the template table from 3 to 1, but then would require a join between biometric and template
- Explore caching templates on the server as this will improve performance

## Usage

### Verify an Identity

```
POST /api/v1/verify
Accept: application/json
Content-Type: application/json

{
	"backend": "dummy",
	"position": "right_thumb",
	"filters": {
		"nationalId": "NIN55555"
	},
	"image": "<base64-encoded string goes here>"
}
```

```
RESPONSE: HTTP 200

{
    "status" : "matched",
    "identity" : "12345"
}

```

A test fingerprint can be found here: test/resources/images/fingerprint.jpg

### Grab a print image

This endpoint allows us to grab jpegs of any citizen's print. Should NOT BE exposed to at the api gateway level.

```
GET /api/v1/fingerprint_image/{backend}/{filter}/{position}
Accept: image/jpeg
```

**Where**
- backend - Backend we are grabbing data from e.g. Dummy Issuer
- filter - What we wish to filter the data by using values from our backend filter definition, e.g. nationalId=12345. This filter has to be a unique field.
- position - finger position using NIST convention. e.g. 1 for Right thumb
