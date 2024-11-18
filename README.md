# inso-cli-report-2-junit-xml: format `inso CLI` report to `JUnit XML`
Format the report of the [`inso CLI`](https://github.com/Kong/insomnia/tree/develop/packages/insomnia-inso) collection execution into a [`JUnit XML`](https://github.com/testmoapp/junitxml) format

## Clone this repository
```shell
git clone https://github.com/jeromeguillaume/inso-cli-report-2-junit-xml.git
cd ./inso-cli-report-2-junit-xml
```

## How to build `inso-cli-report-2-junit-xml`
```shell
javac -d bin/ src/com/kong/insoclireport2junitxml/*
```

## How to run `inso-cli-report-2-junit-xml`
```shell
java -cp bin com.kong.insoclireport2junitxml.InsoCliJunit ./samples/inso-cli-input.log
```