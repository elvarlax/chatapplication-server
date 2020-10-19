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
# this will build the project into ./dist/chat-application.jar
$ mvn clean package
```
`Note: bouncy-castle is a signed package that must not be part of the final .jar file, therefore it's defined as external dependency (in lib folder)`

## Run

```bash
# to run as a client
$ java -jar ./dist/chat-application.jar Mode=Client
# to run as a server
$ java -jar ./dist/chat-application.jar Mode=Server
```

## Rest

password for test.jks: 123456
password for ca_key.pem: Scb1kNjPb46KzRESq6Dl
root cert challenge pass: Ercx9rPBtmQWbnp9lqsG