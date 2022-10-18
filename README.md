# Introduction

This repository is cloned from [JAVA SCORE Example](https://github.com/icon-project/java-score-examples).

# Quick start project

### 1. Build the project

```shell
./gradlew build
```

### 2. Optimize the jar

```shell
./gradlew optimizedJar
```
### 3.1 Deploy using `goloop`


```shell
goloop rpc sendtx deploy ./hello-world/build/libs/hello-world-0.1.0-optimized.jar \
    --uri http://localhost:9082/api/v3 \
    --key_store <your_wallet_json> --key_password <password> \
    --nid 3 --step_limit=1000000 \
    --content_type application/java \
    --param name=Alice
```

### 3.2 Deploy using `deployJar` extension

> DOING, don't use it now

**Deploy to localhost**
```
$ ./gradlew hello-world:deployToLocal -PkeystoreName=<your_wallet_json> -PkeystorePass=<password>

> Task :hello-world:deployToLocal
>>> deploy to http://localhost:9082/api/v3
>>> optimizedJar = ./hello-world/build/libs/hello-world-0.1.0-optimized.jar
>>> keystore = <your_wallet_json>
Succeeded to deploy: 0x699534c9f5277539e1b572420819141c7cf3e52a6904a34b2a2cdb05b95ab0a3
SCORE address: cxd6d044b01db068cded47bde12ed4f15a6da9f1d8
```
**Deploy to SeJong testnet**
```
$ ./gradlew hello-world:deployToLocal -PkeystoreName=<your_wallet_json> -PkeystorePass=<password>

> Task :hello-world:deployToLocal
>>> deploy to http://localhost:9082/api/v3
>>> optimizedJar = ./hello-world/build/libs/hello-world-0.1.0-optimized.jar
>>> keystore = <your_wallet_json>
Succeeded to deploy: 0x699534c9f5277539e1b572420819141c7cf3e52a6904a34b2a2cdb05b95ab0a3
SCORE address: cxd6d044b01db068cded47bde12ed4f15a6da9f1d8
```
