# Infinite Technology ∞ Ascend ⏫

|Attribute\Release type|Latest|
|----------------------|------|
|Version|0.0.0-SNAPSHOT|
|Branch|[master](https://github.com/INFINITE-TECHNOLOGY/ASCEND)|
|CI Build status|[![Build Status](https://travis-ci.com/INFINITE-TECHNOLOGY/ASCEND.svg?branch=master)](https://travis-ci.com/INFINITE-TECHNOLOGY/ASCEND)|
|Test coverage|[![codecov](https://codecov.io/gh/INFINITE-TECHNOLOGY/ASCEND/branch/master/graphs/badge.svg)](https://codecov.io/gh/INFINITE-TECHNOLOGY/ASCEND/branch/master/graphs)|
|Library (Maven)|[oss.jfrog.org snapshot](https://oss.jfrog.org/artifactory/webapp/#/artifacts/browse/tree/General/oss-snapshot-local/io/infinite/ascend/0.0.1-SNAPSHOT)|
|Heroku|![Heroku](https://heroku-badge.herokuapp.com/?app=ascend-demo&root=/ascend/unsecured)|

## Purpose

`Ascend` is `Authorization Server` supporting `Step-up Authorization` and many other features.

## In short

'Ascend' allows to create and validate cryptographic credentials (`JWTs`) used to provide a `Client Software` (Apps) the permission
to access secured `Web Services` within the scope of their user authority.

`Ascend` is consists of:
1) Authorization Granting Server - issuing JWTs
2) Authorization Validation Server - validating JWTs and if needed acting as reverse proxy.

## Documentation

* [**Ascend Documentation**](https://github.com/INFINITE-TECHNOLOGY/ASCEND/wiki)

## Technology stack

* Docker
* Spring Boot
* Groovy
* SQL DB (via JPA and Spring Data)
* REST+HATEOAS (via Spring Data Rest repositories)
* Authentication providers extensible using Plugins (Groovy scripts)

## Try me now!

We have deployed a demo [Ascend Demo](https://github.com/INFINITE-TECHNOLOGY/ASCEND_DEMO) repository is as a demo Heroku app (`ascend-demo`).

Just open the below URL in your browser:

https://ascend-demo.herokuapp.com/ascend/secured