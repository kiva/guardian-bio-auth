# Bioanalyzer Service

Spring Boot microservice that abstracts common analysis we wish to run on bio images. An example is to quality check an 
image of a fingerprint.


## Getting Started

These instructions will get you a copy of the project up and running on your local machine for development and testing
purposes. See deployment for notes on how to deploy the project on a live system.

### Prerequisites

For building and running the application you need:

- JDK 1.8
- Gradle
- Git
- Docker

### Installing

Should always be built with a docker container given dependency on NFIQ2 which is an absolute pain to set up. Note that
we use a kiva-hosted fork of the main https://github.com/usnistgov/NFIQ2 repo so that our builds are deterministic. If
there's ever any useful updates in that repo we should sync them to ours.

```
./gradlew clean
./gradlew build
docker build -t bioanalyzerservice .
docker run -it --rm -p 8080:8080 bioanalyzerservice
```

Note that building this via docker takes a long time to install prerequisites so please grab coffee. If you're in an
area with a low connectivity, maybe take a nap.


## Usage

### Analyzing Images

```
POST /api/v1/analyze
Accept: application/json
Content-Type: application/json

{
    "test": {
        "type": "fingerprint",
        "image": "{BASE_64_ENCODED_IMAGE}"
    }
}
```

```
RESPONSE: HTTP 200 

[
    "test" : {
        "format": "image/jpeg"
        "quality" : 80,
    }
]

```

Where `test` is a key used to refer to the image allowing you to batch calls

### Supported Analysis

| Analysis  | Description                                                                                                                              |
|-----------|------------------------------------------------------------------------------------------------------------------------------------------|
| format    | Returns the format of the biodata e.g. image/png                                                                                         |
| quality   | The quality score in range of 0-100 according to the international biometric sample quality standard ISO/IEC 29794-1:2016                |


New analyzers can easily be added by implementing org.kiva.bioanalyzerservice.services.analyzers.BioDataAnalyzer. As
long as the implementation is annotated as a spring component `@component` the analysis engine auto detects it.


## Running the tests

## Deployment

Add additional notes about how to deploy this on a live system


## Built With

* Spring Boot


## Authors

* **Jeff Kennedy** - *Maintainer*
* **Salton Massally** - *Initial Work*


## TODO
- Fix error handling
- Benchmark performance

## Wish List
