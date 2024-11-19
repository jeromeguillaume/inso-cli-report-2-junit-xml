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
- MacOs: `$?`

## Example
|`ìnso CLI`|`JUnit XML`|
|:-|:-|
|```
Running request: /uuid req_328aa65514994e33a6287942f6dee874
[network] Response succeeded req=req_328aa65514994e33a6287942f6dee874 status=200

Test results:
❌ Check if status is 200
❌ Check if uuid is valid

Total tests: 2
Passed: 0
Failed: 2

ReferenceError: insomnia2 is not defined
AssertionError: expected { Object (uuid) } to have property 'uuidXXX'
Running request: Invalid Hostname /uuid req_50a8af2d27cf4a1db623a5595e5bcfef
[network] Response failed req=req_50a8af2d27cf4a1db623a5595e5bcfef err=Error: Couldn't resolve host name

Test results:
❌ Check if status is 200

Total tests: 1
Passed: 0
Failed: 1

TypeError: Cannot read properties of undefined (reading 'code')
Running request: /image req_93d29e2006d04e1796a15aa456dd6e68
[network] Response succeeded req=req_93d29e2006d04e1796a15aa456dd6e68 status=200

Test results:
✅ Check if status is 200

Total tests: 1
Passed: 1
Failed: 0


Running request: My /anything req_f80d5f794ca54e9e99c558f4f64525fe
[network] Response succeeded req=req_f80d5f794ca54e9e99c558f4f64525fe status=200

Test results:
❌ Check if status is 500
❌ Check content validity with 'json2' key

Total tests: 2
Passed: 0
Failed: 2

AssertionError: expected 200 to deeply equal 500
AssertionError: expected { args: {}, …(8) } to have key 'json2'
```|```xml
<?xml version="1.0" encoding="UTF-8" standalone="no"?>
<testsuites failed="5" tests="6" timestamp="2024-11-19T15:12:23">
    <testsuite failures="2" name="/uuid.req_328aa65514994e33a6287942f6dee874" tests="2">
        <testcase classname="/uuid.req_328aa65514994e33a6287942f6dee874" name="Check if status is 200">
            <failure message="insomnia2 is not defined" type="ReferenceError"/>
        </testcase>
        <testcase classname="/uuid.req_328aa65514994e33a6287942f6dee874" name="Check if uuid is valid">
            <failure message="expected { Object (uuid) } to have property 'uuidXXX'" type="AssertionError"/>
        </testcase>
    </testsuite>
    <testsuite failures="1" name="Invalid Hostname /uuid.req_50a8af2d27cf4a1db623a5595e5bcfef" tests="1">
        <system-err>Error: Couldn't resolve host name</system-err>
        <testcase classname="Invalid Hostname /uuid.req_50a8af2d27cf4a1db623a5595e5bcfef" name="Check if status is 200">
            <failure message="Cannot read properties of undefined (reading 'code')" type="TypeError"/>
        </testcase>
    </testsuite>
    <testsuite failures="0" name="/image.req_93d29e2006d04e1796a15aa456dd6e68" tests="1">
        <testcase classname="/image.req_93d29e2006d04e1796a15aa456dd6e68" name="Check if status is 200"/>
    </testsuite>
    <testsuite failures="2" name="My /anything.req_f80d5f794ca54e9e99c558f4f64525fe" tests="2">
        <testcase classname="My /anything.req_f80d5f794ca54e9e99c558f4f64525fe" name="Check if status is 500">
            <failure message="expected 200 to deeply equal 500" type="AssertionError"/>
        </testcase>
        <testcase classname="My /anything.req_f80d5f794ca54e9e99c558f4f64525fe" name="Check content validity with 'json2' key">
            <failure message="expected { args: {}, …(8) } to have key 'json2'" type="AssertionError"/>
        </testcase>
    </testsuite>
</testsuites>
```|