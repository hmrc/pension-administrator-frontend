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

package controllers.register.partnership

import connectors.RegistrationConnector
import connectors.cache.UserAnswersCacheConnector
import controllers.Retrievals
import controllers.actions._
import forms.UKAddressFormProvider
import forms.register.partnership.ConfirmPartnershipDetailsFormProvider
import identifiers.TypedIdentifier
import identifiers.register.partnership.{ConfirmPartnershipDetailsId, PartnershipRegisteredAddressId}
import identifiers.register.{BusinessNameId, BusinessTypeId, BusinessUTRId, RegistrationInfoId}
import models._
import models.requests.DataRequest
import play.api.Logger
import play.api.data.Form
import play.api.i18n.I18nSupport
import play.api.libs.json.{JsResultException, Writes}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents, Result}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import utils.annotations.Partnership
import utils.countryOptions.CountryOptions
import utils.{AddressHelper, Navigator, UserAnswers}
import views.html.register.partnership.confirmPartnershipDetails

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class ConfirmPartnershipDetailsController @Inject()(
                                                     dataCacheConnector: UserAnswersCacheConnector,
                                                     @Partnership navigator: Navigator,
                                                     authenticate: AuthAction,
                                                     allowAccess: AllowAccessActionProvider,
                                                     getData: DataRetrievalAction,
                                                     requireData: DataRequiredAction,
                                                     registrationConnector: RegistrationConnector,
                                                     formProvider: ConfirmPartnershipDetailsFormProvider,
                                                     addressFormProvider: UKAddressFormProvider,
                                                     countryOptions: CountryOptions,
                                                     val controllerComponents: MessagesControllerComponents,
                                                     val view: confirmPartnershipDetails,
                                                     addressHelper: AddressHelper
                                                   )(implicit val executionContext: ExecutionContext)
  extends FrontendBaseController
    with I18nSupport
    with Retrievals {

  private val logger = Logger(classOf[ConfirmPartnershipDetailsController])

  private val form: Form[Boolean] = formProvider()

  def onPageLoad(mode: Mode): Action[AnyContent] =
    (authenticate andThen allowAccess(mode) andThen getData andThen requireData).async {
      implicit request =>
        getPartnershipDetails {
          case registration@(_: OrganizationRegistration) =>
          upsert(request.userAnswers, PartnershipRegisteredAddressId)(registration.response.address) { userAnswers =>
            upsert(userAnswers, BusinessNameId)(registration.response.organisation.organisationName) { userAnswers =>
              upsert(userAnswers, RegistrationInfoId)(registration.info) { userAnswers =>
                dataCacheConnector.upsert(userAnswers.json).map { _ =>
                  Ok(view(
                    form,
                    registration.response.organisation.organisationName,
                    registration.response.address,
                    countryOptions)
                  )
                }
              }
            }
          }
          case _ => Future.successful(Redirect(controllers.register.partnership.routes.PartnershipCompanyNotFoundController.onPageLoad()))
        }
    }

  private def getPartnershipDetails(fn: OrganizationRegistrationStatus => Future[Result])
                                   (implicit request: DataRequest[AnyContent]): Either[Future[Result], Future[Result]] = {
    (BusinessNameId and BusinessUTRId and BusinessTypeId).retrieve.map {
      case name ~ utr ~ businessType =>
        val organisation = Organisation(name, businessType)
        val legalStatus = RegistrationLegalStatus.Partnership
        registrationConnector.registerWithIdOrganisation(utr, organisation, legalStatus).flatMap {
          case registration@(_: OrganizationRegistration) => fn(registration)
          case _ => Future.successful(Redirect(controllers.register.partnership.routes.PartnershipCompanyNotFoundController.onPageLoad()))
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

  def onSubmit(): Action[AnyContent] = (authenticate andThen getData andThen requireData).async {
    implicit request =>
      form.bindFromRequest().fold(
        (formWithErrors: Form[?]) =>
          (BusinessNameId and PartnershipRegisteredAddressId).retrieve.map {
            case name ~ address =>
              Future.successful(BadRequest(view(
                formWithErrors,
                name,
                address,
                countryOptions
              )))
          },
        {
          case true =>
            val invalidAddressFields = PartnershipRegisteredAddressId.retrieve.map { address =>
              val addressFields = addressHelper.mapAddressFields(address)
              val addressForm: Form[Address] = addressFormProvider()
              val bind = addressForm.bind(addressFields)
              bind.fold(
                _ => true,
                _ => false
              )
            }
            invalidAddressFields.map { invalidFields =>
              if (invalidFields) {
                Future.successful(Redirect(routes.AddressController.onPageLoad()))
              } else {
                Future.successful(Redirect(navigator.nextPage(ConfirmPartnershipDetailsId, NormalMode, request.userAnswers)))
              }
            }
          case false =>
            val updatedAnswers = request.userAnswers.removeAllOf(List(
              PartnershipRegisteredAddressId, RegistrationInfoId
            )).asOpt.getOrElse(request.userAnswers)
            dataCacheConnector.upsert(updatedAnswers.json).flatMap { _ =>
              Future.successful(Redirect(controllers.register.company.routes.CompanyUpdateDetailsController.onPageLoad()))
            }
        }
      )
  }
}
