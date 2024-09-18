/*
 * Copyright 2024 HM Revenue & Customs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package controllers.register.company

import connectors.RegistrationConnector
import connectors.cache.UserAnswersCacheConnector
import controllers.Retrievals
import controllers.actions.{AllowAccessActionProvider, AuthAction, DataRequiredAction, DataRetrievalAction}
import forms.AddressFormProvider
import forms.register.company.CompanyAddressFormProvider
import identifiers.TypedIdentifier
import identifiers.register.company.ConfirmCompanyAddressId
import identifiers.register.{BusinessNameId, BusinessTypeId, BusinessUTRId, RegistrationInfoId}
import models._
import models.requests.DataRequest
import play.api.Logger
import play.api.data.Form
import play.api.i18n.I18nSupport
import play.api.libs.json.{JsResultException, Writes}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents, Result}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import utils.annotations.RegisterCompany
import utils.countryOptions.CountryOptions
import utils.{AddressHelper, Navigator, UserAnswers}
import views.html.register.company.confirmCompanyDetails

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class ConfirmCompanyDetailsController @Inject()(
                                                 dataCacheConnector: UserAnswersCacheConnector,
                                                 @RegisterCompany navigator: Navigator,
                                                 authenticate: AuthAction,
                                                 allowAccess: AllowAccessActionProvider,
                                                 getData: DataRetrievalAction,
                                                 requireData: DataRequiredAction,
                                                 registrationConnector: RegistrationConnector,
                                                 formProvider: CompanyAddressFormProvider,
                                                 addressFormProvider: AddressFormProvider,
                                                 countryOptions: CountryOptions,
                                                 val controllerComponents: MessagesControllerComponents,
                                                 val view: confirmCompanyDetails,
                                                 addressHelper: AddressHelper
                                               )(implicit val executionContext: ExecutionContext)
  extends FrontendBaseController
    with I18nSupport
    with Retrievals {

  private val logger = Logger(classOf[ConfirmCompanyDetailsController])

  private val form: Form[Boolean] = formProvider()

  def onPageLoad(mode: Mode): Action[AnyContent] =
    (authenticate andThen allowAccess(mode) andThen getData andThen requireData).async {
      implicit request =>
        getCompanyDetails {
          case registration@(_: OrganizationRegistration) =>
          upsert(request.userAnswers, ConfirmCompanyAddressId)(registration.response.address) { userAnswers =>
            upsert(userAnswers, BusinessNameId)(registration.response.organisation.organisationName) { userAnswers =>
              upsert(userAnswers, RegistrationInfoId)(registration.info) { userAnswers =>
                dataCacheConnector.upsert(request.externalId, userAnswers.json).flatMap { _ =>

                  Future.successful(Ok(view(
                    form, registration.response.address, registration.response.organisation.organisationName, countryOptions
                  )))
                }
              }
            }
          }
          case _ => Future.successful(Redirect(routes.CompanyNotFoundController.onPageLoad()))
        }
    }

  def onSubmit(mode: Mode): Action[AnyContent] =
    (authenticate andThen getData andThen requireData).async {
      implicit request =>
        form.bindFromRequest().fold(
          (formWithErrors: Form[_]) =>
            (BusinessNameId and ConfirmCompanyAddressId).retrieve.map {
              case name ~ address =>
                Future.successful(BadRequest(view(formWithErrors, address, name, countryOptions)))
            },
          {
            case true =>
              val invalidAddressFields = ConfirmCompanyAddressId.retrieve.map { address =>
                val addressFields = addressHelper.mapAddressFields(address)
                val addressForm: Form[Address] = addressFormProvider()
                val bind = addressForm.bind(addressFields)
                bind.fold(
                  _ => true,
                  _ => false
                )
              }
              invalidAddressFields.map {
                invalidFields =>
                  if (invalidFields) {
                    Future.successful(Redirect(routes.AddressController.onPageLoad()))
                  } else {
                    Future.successful(Redirect(routes.CompanyRegistrationTaskListController.onPageLoad()))
                  }
              }
            case false =>
              val updatedAnswers = request.userAnswers.removeAllOf(List(ConfirmCompanyAddressId, RegistrationInfoId)).asOpt.getOrElse(request.userAnswers)
              dataCacheConnector.upsert(request.externalId, updatedAnswers.json).flatMap { _ =>
                Future.successful(Redirect(routes.CompanyUpdateDetailsController.onPageLoad()))
              }
          }
        )
    }

  private def getCompanyDetails(fn: OrganizationRegistrationStatus => Future[Result])
                               (implicit request: DataRequest[AnyContent]): Either[Future[Result], Future[Result]] = {
    (BusinessNameId and BusinessUTRId and BusinessTypeId).retrieve.map {
      case businessName ~ utr ~ businessType =>
        val organisation = Organisation(businessName, businessType)
        val legalStatus = RegistrationLegalStatus.LimitedCompany
        registrationConnector.registerWithIdOrganisation(utr, organisation, legalStatus).flatMap {
          case registration@(_: OrganizationRegistration) => fn(registration)
          case _ =>
            Future.successful(Redirect(routes.CompanyNotFoundController.onPageLoad()))
        }
    }
  }

  private def upsert[I <: TypedIdentifier.PathDependent](userAnswers: UserAnswers, id: I)
                                                        (value: id.Data)
                                                        (fn: UserAnswers => Future[Result])
                                                        (implicit writes: Writes[id.Data]): Future[Result] =
    userAnswers
      .set(id)(value)
      .fold(
        errors => {
          logger.error("Unable to set user answer", JsResultException(errors))
          Future.successful(InternalServerError)
        },
        userAnswers => fn(userAnswers)
      )
}
