# Infinite Technology ∞ Ascend 🕊

|Attribute\Release type|Latest|
|----------------------|------|
|Version|1.0.0-SNAPSHOT|
|Branch|[master](https://github.com/INFINITE-TECHNOLOGY/ASCEND)|
|CI Build status|[![Build Status](https://travis-ci.com/INFINITE-TECHNOLOGY/ASCEND.svg?branch=master)](https://travis-ci.com/INFINITE-TECHNOLOGY/ASCEND)|
|Test coverage|[![codecov](https://codecov.io/gh/INFINITE-TECHNOLOGY/ASCEND/branch/master/graphs/badge.svg)](https://codecov.io/gh/INFINITE-TECHNOLOGY/ASCEND/branch/master/graphs)|
|Library (Maven)|[oss.jfrog.org snapshot](https://oss.jfrog.org/artifactory/webapp/#/artifacts/browse/tree/General/oss-snapshot-local/io/infinite/ascend/1.0.0-SNAPSHOT)|
|Heroku|![Heroku](https://heroku-badge.herokuapp.com/?app=ascend-demo&root=/ascend/unsecured)|

## Purpose

`Ascend` is a step-up on-demand Cloud Security abstraction layer provider.

With `Ascend` it is no longer needed to hard-wire the business application with security framework.

Even legacy unsecured intranet APIs can now be secured with `Ascend` and published into the cloud.

On top of that `Ascend` is built around `Step-up` authorization - making it an ideal tool for securing 
Web Banking apps.

`Ascend` supports `OAuth2` as well as any possible protocol (including proprietary) encapsulated in its advanced innovative 
Web Token format.

## In short

'Ascend' allows to create and validate cryptographic credentials used to provide a Client Software (Apps) the permission
to access secured Web Services within the scope of their user authority.

`Ascend` fulfils `Authorization Server` role in `OAuth2` terminology.

`Ascend` is consists of:
1) Authorization Granting Server - issuing JWTs
2) Authorization Validation Server - validating JWTs and acting as reverse proxy.

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