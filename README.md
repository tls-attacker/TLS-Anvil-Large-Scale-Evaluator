# TLS-Anvil-Large-Scale-Evaluator

The TLS-Anvil-Large-Scale-Evaluator provides a setup to execute the [TLS-Anvil](https://github.com/tls-attacker/TLS-Anvil) against multiple TLS implementations running in Docker containers ([TLS-Docker-Library](https://github.com/tls-attacker/TLS-Docker-Library)) in parallel.
 
It was developed as part of the master's thesis *Development and Evaluation of a TLS-Testsuite* at *Ruhr University Bochum* in cooperation with the *TÜV Informationstechnik GmbH*.
 
## Build
```shell
git clone https://github.com/tls-attacker/TLS-Docker-Library.git
( cd TLS-Docker-Library && mvn install -DskipTests )

git clone https://github.com/RUB-NDS/TLS-Anvil-Large-Scale-Evaluator.git
cd TLS-Anvil-Large-Scale-Evaluator
mvn install -DskipTests

```

## Start the evaluator
```shell
java -jar apps/TLS-Testsuite-Large-Scale-Evaluator.jar
```
