# 02232 Applied Cryptography

Lab 1 - Establishing Secure Message Exchange

This lab focuses on 2 tasks:
- Applying Symmetric crypto during messaging
- Diffie-Hellman key exchange in P2P communication

## Installation

You should install java sdk (at least 11).
For build, you should install [Maven](https://maven.apache.org/guides/getting-started/maven-in-five-minutes.html)

## Build
```bash
# this will build the project into ./target/ChatApplication.jar
$ mvn clean compile assembly:single
```

## Run

```bash
# to run as a client
$ java -jar ./target/ChatApplication.jar Mode=Client
# to run as a server
$ java -jar ./target/ChatApplication.jar Mode=Server
```