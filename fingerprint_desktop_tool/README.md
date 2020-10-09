# Fingerprint Desktop Tool

Commandline application for generating templates from the desktop. Templates generated are serialized fingerprint
template in gzip-compressed JSON format that can be stored in a database or sent over network.

## Usage 

### Build Distribution

Build distribution zip that includes all the executable needed to run this as a commandline application. It comes with
start up scripts for both *nix (unix, linux, os) and a windows .bat file.

```bash
./gradlew distZip
```

Zip generated in `build/distributions/` folder.

### Run from Desktop Tool

The generated distribution should be package with the desktop tool. The instructions below are for *nix systems, but
they should translate trivially to windows.

The following commands are relative to the root of the unzipped distribution folder.

* See help
    ```bash
    ./bin/fingerprint_desktop_tool -h
    ```

* Generate a template for an image and store in same folder, but with '.tmpl' extension
    ```bash
    ./bin/fingerprint_desktop_tool <image_path>
    ```

* Generate a template for an image and output to the specified path
    ```bash
    ./bin/fingerprint_desktop_tool <image_path> -o <output_path>
    ```

> **_WARNING:_**  Caller is responsible for deleting the generated template after consumption


## TODO
-[ ] Write tests