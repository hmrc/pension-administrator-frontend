# Pension Administrator Frontend 

[Identity verification testing](#identity-verification-testing)

## Info

This service allows a user to register as a pension administrator.

This service has a corresponding back-end service, namely pension-administrator which integrates with HOD i.e DES/ETMP.
 
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

## Running the service

Service Manager: PODS_ALL

Port: 8201

Link: http://localhost:8201/register-as-pension-scheme-administrator

## Tests and prototype

[View the prototype here](https://pods-prototype.herokuapp.com)

|Repositories     |Link                                                                   |
|-----------------|-----------------------------------------------------------------------|
|Journey tests    |https://github.com/hmrc/pods-journey-tests                             |
|Prototype        |https://pods-prototype.herokuapp.com                                   |

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
