# inso-cli-report-2-junit-xml: inso CLI to JUnit XML
Format the report of the `inso CLI` collection execution into a [`JUnit XML`](https://github.com/testmoapp/junitxml) format

## Clone this repository
```shell
git clone https://github.com/jeromeguillaume/inso-cli-report-2-junit-xml.git
cd ./inso-cli-report-2-junit-xml
```

## How to build JAVA `inso-cli-report-2-junit-xml`
```shell
javac -d bin/ src/com/kong/insoclireport2junitxml/*
```

## How to run JAVA `inso-cli-report-2-junit-xml`
```shell
java -cp bin com.kong.insoclireport2junitxml.InsoCliJunit inso-cli-input.log
```