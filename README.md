# Pension Administrator Frontend

## Contents
- [Overview](#overview)
- [Requirements](#requirements)
- [Running the Service](#running-the-service)
- [Enrolments](#enrolments)
- [Compile & Test](#compile--test)
- [Identity Verification Testing](#identity-verification-testing)
- [Navigation and Dependent Services](#navigation-and-dependent-services)
- [Service Documentation](#service-documentation)
- [Endpoints](#endpoints)
- [License](#license)

## Overview

This is the repository for Pension Administrator Frontend. This service allows a user to register and perform duties as a pension administrator. The pension administrator is the person or organisation responsible for the overall management of a pension scheme. A user registers to become a pension scheme administrator. As a pension scheme administrator they can invite others to and remove others from a scheme. 

This service has a corresponding back-end microservice, namely pension-administrator which integrates with HOD i.e DES/ETMP and supports the registration, variation, invitation, association and de-registration of a pension administrator.

**Associated Backend Link:** https://github.com/hmrc/pension-administrator

**Stubs:** https://github.com/hmrc/pensions-scheme-stubs


## Requirements 
This service is written in Scala and Play, so needs at least a [JRE] to run.

**Node version:** 16.20.2

**Java version:** 11

**Scala version:** 2.13.14


## Running the Service
**Service Manager Profile:** PODS_ALL

**Port:** 8201

**Link:** http://localhost:8201/register-as-pension-scheme-administrator/registered-psa-details

In order to run the service, ensure Service Manager is installed (see [MDTP guidance](https://docs.tax.service.gov.uk/mdtp-handbook/documentation/developer-set-up/set-up-service-manager.html) if needed) and launch the relevant configuration by typing into the terminal:
`sm2 --start PODS_ALL`

To run the service locally, enter `sm2 --stop PENSION_ADMINISTRATOR_FRONTEND`.

In your terminal, navigate to the relevant directory and enter `sbt run`.

Access the Authority Wizard and login with the relevant enrolment details [here](http://localhost:9949/auth-login-stub/gg-sign-in)


## Enrolments 
There are several different options for enrolling through the auth login stub. In order to enrol as a dummy user to access the platform for local development and testing purposes, the following details must be entered on the auth login page.


For access to the **Pension Administrator dashboard** for local development, enter the following information: 

**Redirect url -** http://localhost:8204/manage-pension-schemes/overview 

**GNAP Token -** NO 

**Affinity Group -** Organisation 

**Enrolment Key -** HMRC-PODS-ORG 

**Identifier Name -** PsaID 

**Identifier Value -** A2100005

---

If you wish to access the **Pension Practitioner dashboard** for local development, enter the following information: 

**Redirect URL -** http://localhost:8204/manage-pension-schemes/dashboard 

**GNAP Token -** NO 

**Affinity Group -** Organisation 

**Enrolment Key -** HMRC-PODSPP-ORG 

**Identifier Name -** PspID 

**Identifier Value -** 21000005

---

**Dual enrolment** as both a Pension Administrator and Practitioner is also possible and can be accessed by entering:

**Redirect url -** http://localhost:8204/manage-pension-schemes/overview 

**GNAP Token -** NO 

**Affinity Group -** Organisation 

**Enrolment Key 1 -** HMRC-PODSPP-ORG Identifier 

**Name 1 -** PspID Identifier 

**Value 1 -** 21000005

**Enrolment Key 2 -** HMRC-PODS-ORG 

**Identifier Name 2 -** PsaID 

**Identifier Value 2 -** A2100005

---

To access the **Scheme Registration journey**, enter the following information:

**Redirect URL -** http://localhost:8204/manage-pension-schemes/you-need-to-register 

**GNAP Token -** NO 

**Affinity Group -** Organisation

---


## Compile & Test 
**To compile:** Run `sbt compile`

**To test:** Use `sbt test`

**To view test results with coverage:** Run `sbt clean coverage test coverageReport`

For further information on the PODS Test Approach and wider testing including acceptance, accessibility, performance, security and E2E testing, visit the PODS Confluence page [here](https://confluence.tools.tax.service.gov.uk/pages/viewpage.action?spaceKey=PODSP&title=PODS+Test+Approach).

For Journey Tests, visit the [Journey Test Repository](| Journey tests(https://github.com/hmrc/pods-journey-tests).

View the prototype [here](https://pods-event-reporting-prototype.herokuapp.com/).


## Identity verification testing

Additional services required to test IV uplift: KEYSTORE, PLATFORM_ANALYTICS, IV_CALLVALIDATE_PROXY, IV_TEST_DATA, IDENTITY_VERIFICATION_FRONTEND  

Relevant application.conf field: urls.iv-uplift-entry  

Manual testing might require disabling CORS on
identity_verification_frontend repository,
this was the case during writing this.  

Add the following to application.conf of identity_verification_frontend:  
```play.filters.disabled += play.filters.csrf.CSRFFilter```

Eventually we might want to move to iv-stubs, but currently
they don't support organisations. identity_verification_stub repository.


## Navigation and Dependent Services 
The Pension Administrator Frontend integrates with the Manage Pension Schemes (MPS) service and uses various stubs available on [GitHub](https://github.com/hmrc/pensions-scheme-stubs). From the Authority Wizard page you will be redirected to the dashboard. Navigate to the appropriate area by accessing items listed within the service-specific tiles on the dashboard. On the Pension Administrator frontend, an administrator can change their details, stop being an administrator and check for invitations, explore Penalties & Charges, manage and migrate pension schemes.


There are numerous APIs implemented throughout the MPS architecture, and the relevant endpoints are illustrated below. For an overview of all PODS APIs, refer to the [PODS API Documentation](https://confluence.tools.tax.service.gov.uk/display/PODSP/PODS+API+Latest+Version).


## Service Documentation 
[To Do]
Include relevant links or details to any additional, service-specific documents (e.g., stubs, testing protocols) when available. 

## Endpoints 

 
### Dependencies

| Service                              | Link                                                          |
|--------------------------------------|---------------------------------------------------------------|
| Pensions Scheme                      | https://github.com/hmrc/pensions-scheme                       |
| Pension Administrator                | https://github.com/hmrc/pension-administrator                 |
| Address Lookup                       | https://github.com/hmrc/address-lookup                        |
| Email                                | https://github.com/hmrc/email                                 |
| Auth                                 | https://github.com/hmrc/auth                                  |
| Personal Details Validation          | https://github.com/hmrc/personal-details-validation           |
| Personal Details Validation Frontend | https://github.com/hmrc/personal-details-validation-frontend  |
| Tax Enrolments                       | https://github.com/hmrc/tax-enrolments                        |
origin	git@github.com:hmrc/personal-details-validation-frontend.git
### Endpoints used   

| Service                              | HTTP Method | Route                                                                           | Purpose                                                                                                   
|--------------------------------------|-------------|---------------------------------------------------------------------------------|-----------------------------------------------------------------------------------------------------------|
| Pension Administrator                | POST        | /pension-administrator/register-with-id/organisation                            | Returns the Business Partner Record for an organisation from ETMP based on the UTR                        |
| Pension Administrator                | POST        | /pension-administrator/register-with-id/individual                              | Returns the Business Partner Record for an individual based on the NINO/UTR from ETMP                     |
| Pension Administrator                | POST        | /pension-administrator/register-with-no-id/organisation                         | Registers an organisation on ETMP who does not have a UTR. Typically this will be a non- UK organisation  |
| Pension Administrator                | POST        | /pension-administrator/register-with-no-id/individual                           | Registers an individual on ETMP who does not have a UTR/NINO. Typically this will be a non- UK individual |
| Pension Administrator                | POST        | /pension-administrator/register-psa                                             | Subscribe a pension scheme administrator                                                                  |
| Pension Administrator                | POST        | /pension-administrator/psa-variation/:id                                        | Update PSA Subscription Details                                                                           |
| Pension Administrator                | GET        | /pension-administrator/can-deregister/:id                                       | Can de register a PSA                                                                                     |
| Pension Administrator                | GET        | /pension-administrator/psa-subscription-details                                 | Returns PSA Subscription Details                                                                          |
| Pension Administrator                | GET        | /pension-administrator/get-minimal-psa                                          | Returns PSA minimal Details                                                                               |
| Address Lookup                       | GET         | /v2/uk/addresses                                                                | Returns a list of addresses that match a given postcode                                                   | 
| Email                                | POST        | /hmrc/email                                                                     | Sends an email to an email address                                                                        |
| Personal Details Validation          | GET        | /personal-details-validation/:validationId                                      | Return the validation results for the given validationId                                                  | 
| Personal Details Validation Frontend | GET        | /personal-details-validation/start?completionUrl=:completionUrl&origin=test     | Start the PDV journey                                                                                     | 
| Tax Enrolments                       | POST        | /tax-enrolments/service/:serviceName/enrolment                                  | Enrols a user synchronously for a given service name                                                      | 

## License 
This code is open source software Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may obtain a copy of the License at:

[http://www.apache.org/licenses/LICENSE-2.0](http://www.apache.org/licenses/LICENSE-2.0)

Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.

[↥ Back to Top](#pension-administrator-frontend)
