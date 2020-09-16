# TLS-Testsuite-Large-Scale-Evaluator

The TLS-Testsuite-Large-Scale-Evaluator provides a setup to execute the [TLS-Testsuite](https://github.com/RUB-NDS/TLS-Testsuite) against multiple TLS implementations running in Docker containers ([TLS-Docker-Library](https://github.com/RUB-NDS/TLS-Docker-Library)) in parallel.
 
It was developed as part of the master's thesis *Development and Evaluation of a TLS-Testsuite* at the *Ruhr-University Bochum* in cooperation with the *TÜV Informationstechnik GmbH*.
 
 
## Connected Projects
* [TLS-Test-Framework](https://github.com/RUB-NDS/TLS-Test-Framework)
* [TLS-Testsuite](https://github.com/RUB-NDS/TLS-Testsuite)
* [TLS-Testsuite-Report-Analyzer](https://github.com/RUB-NDS/TLS-Testsuite-Report-Analyzer)

## Build
```shell
git clone git@github.com:RUB-NDS/TLS-Docker-Library.git
( cd TLS-Docker-Library && git checkout 2ac2e1a10efadd0d9f6b766f290c797d7fdbd281 && mvn install -DskipTests )

git clone git@github.com:RUB-NDS/TLS-Testsuite-Large-Scale-Evaluator.git
cd TLS-Testsuite-Large-Scale-Evaluator
mvn install -DskipTests

```

## Start the evaluator
```shell
java -jar apps/TLS-Testsuite-Large-Scale-Evaluator.jar
```
