# Convert `inso CLI` report to `JUnit XML`
Convert the report of the [`inso CLI`](https://github.com/Kong/insomnia/tree/develop/packages/insomnia-inso) collection execution into a [`JUnit XML`](https://github.com/testmoapp/junitxml) format

## Pre-requisites
1) `inso CLI`: version 10.1.1+
2) `java` and `javac`: version 19

## Clone this repository
```shell
git clone https://github.com/jeromeguillaume/inso-cli-report-2-junit-xml.git
cd ./inso-cli-report-2-junit-xml
```

## How to build `inso-cli-report-2-junit-xml`
```shell
javac -d bin ./src/com/kong/insoclireport2junitxml/*
jar --create --file bin/InsoCliJunit.jar --manifest bin/META-INF/MANIFEST.MF -C bin/ .
```

## How to run `inso-cli-report-2-junit-xml`
```shell
java -jar bin/InsoCliJunit.jar --input ./samples/inso-cli-report.log --output ./samples/inso-cli-junit.xml
```
The exit code of the program is:
- `0`: no error
- `1`: error (and the Exception stack trace is sent to `stdout`)

How to get the exit code for:
- Windows: `%ERRORLEVEL%`
- Linux/MacOs: `$?`

## Example
See full example:
- `inso CLI` input: [inso-cli-report.log](/samples/inso-cli-report.log)
- `JUnit XML` output: [inso-cli-junit.xml](/samples/inso-cli-junit.xml)
### Inso Test passed
|Input: `inso CLI`|Output: `JUnit XML`|
|:---------|:----------|
|Running request: /uuid req_328aa65514994e33a6287942f6dee874|`<testsuite failures="0" name="/uuid.req_328aa65514994e33a6287942f6dee874" tests="1">`|
|✅ Check if status is 200|&ensp;&ensp;&ensp;`<testcase classname="/uuid.req_328aa65514994e33a6287942f6dee874" name="Check if status is 200">`|

### Inso Test failed
|Input: `inso CLI`|Output: `JUnit XML`|
|:---------|:----------|
|My /anything req_f80d5f794ca54e9e99c558f4f64525fe|`<testsuite failures="1" name="My /anything.req_f80d5f794ca54e9e99c558f4f64525fe" tests="1">`|
|❌ Check if status is 200|&ensp;&ensp;&ensp;`<testcase classname="My /anything.req_f80d5f794ca54e9e99c558f4f64525fe" name="Check if status is 200">`|
||&ensp;&ensp;&ensp;&ensp;&ensp;&ensp;`<failure message="expected 200 to deeply equal 500" type="AssertionError"/>`|

### Inso Network Response failed
|Input: `inso CLI`|Output: `JUnit XML`|
|:---------|:----------|
|Running request: /uuid req_50a8af2d27cf4a1db623a5595e5bcfef|`<testsuite failures="1" name="/uuid.req_50a8af2d27cf4a1db623a5595e5bcfef" tests="1">`|
|[network] Response failed req=req_50a8af2d27cf4a1db623a5595e5bcfef err=Error: Couldn't resolve host name|&ensp;&ensp;&ensp;`<system-err>Error: Couldn't resolve host name</system-err>`|