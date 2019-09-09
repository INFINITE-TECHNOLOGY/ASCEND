# Infinite Technology ∞ Ascend 🚀

## In short

`Ascend` is a SECaaS (Security as a Service) platform focused on Web Security.

`Ascend` quickly integrates into your ecosystem and allows to:

- Configure user permissions and the needed authorizations - through a centralized `permission management portal`
- Authenticate users on your behalf - using a wide selection of authentication providers such as Facebook OAuth2
- Authorize user access to your resources (REST or any other Web API)

Planned launch: 2020 Q1.

## Pricing

Both rules below apply at the same time.

### Per granted authorization

When user enters their credentials - they are authenticated and the `Access Authorization` is **granted**.

Monthly:
- 0-5000 successfully granted authorizations - free
- Each extra: $0.01 per each successful authorization grant 

### Per validated authorization

When user tries to access your secured resource, `Ascend` will **validate** their `Access Authorization`.

Monthly:
- 0-10000 successful authorization validations - free
- Each extra: $0.001 per each successful authorization validation

## Features

`Ascend` is a revolutionary Web Security framework, built on top of [JWT](https://en.wikipedia.org/wiki/JSON_Web_Token) specifications.

`Ascend` JWT represents an [ACID](https://www.ibm.com/support/knowledgecenter/en/SSGMCP_5.4.0/product-overview/acid.html)-compliant 
Authorization [transaction](https://en.wikipedia.org/wiki/Transaction), containing sufficient information for validation of an HTTP request.

No longer the `Authorization Header` has only `Authentication` or partial Authorization data - with `Ascend` it is what it was intended to be. 

`Ascend` provides:

- Authorization Granting Server
    - App2app authentication
    - User2app authentication
    - Issuing Authorization JSON Web Tokens
- Authorization Validation Server
    - Autonomously validates HTTP Requests authorized using `Ascend Granting Server`
- Client SDK
    - Provides an API for simple communication with `Ascend Granting Server`
- Server SDK
    - Provides an API for simple communication with `Ascend Validation Server`

## Documentation

* [**Ascend Documentation**](https://github.com/INFINITE-TECHNOLOGY/ASCEND/wiki)