/*
 * Copyright 2018 HM Revenue & Customs
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

import config.FrontendAppConfig
import connectors.{DataCacheConnector, RegistrationConnector}
import controllers.Retrievals
import controllers.actions._
import forms.register.partnership.ConfirmPartnershipDetailsFormProvider
import identifiers.TypedIdentifier
import identifiers.register.partnership.{ConfirmPartnershipDetailsId, PartnershipDetailsId, PartnershipRegisteredAddressId}
import identifiers.register.{BusinessTypeId, RegistrationInfoId}
import javax.inject.Inject

import controllers.register.company.routes
import models.requests.DataRequest
import models.{BusinessDetails, NormalMode, Organisation, OrganizationRegistration}
import play.api.Logger
import play.api.data.Form
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.libs.json.{JsResultException, Writes}
import play.api.mvc.{Action, AnyContent, Result}
import uk.gov.hmrc.http.NotFoundException
import uk.gov.hmrc.play.bootstrap.controller.FrontendController
import utils.annotations.Partnership
import utils.{Navigator, UserAnswers}
import views.html.register.partnership.confirmPartnershipDetails

import scala.concurrent.Future

class ConfirmPartnershipDetailsController @Inject()(
                                                     appConfig: FrontendAppConfig,
                                                     override val messagesApi: MessagesApi,
                                                     dataCacheConnector: DataCacheConnector,
                                                     @Partnership navigator: Navigator,
                                                     authenticate: AuthAction,
                                                     getData: DataRetrievalAction,
                                                     requireData: DataRequiredAction,
                                                     registrationConnector: RegistrationConnector,
                                                     formProvider: ConfirmPartnershipDetailsFormProvider
                                                   ) extends FrontendController with I18nSupport with Retrievals {

  private val form: Form[Boolean] = formProvider()

  def onPageLoad: Action[AnyContent] = (authenticate andThen getData andThen requireData).async {
    implicit request =>
      getPartnershipDetails { case (_, registration) =>
        dataCacheConnector.remove(request.externalId, ConfirmPartnershipDetailsId).flatMap(_ =>
          dataCacheConnector.remove(request.externalId, PartnershipRegisteredAddressId).map(_ =>
            Ok(confirmPartnershipDetails(appConfig, form, registration.response.organisation.organisationName, registration.response.address))))
      }
  }

  def onSubmit: Action[AnyContent] = (authenticate andThen getData andThen requireData).async {
    implicit request =>
      getPartnershipDetails { case (partnershipDetails, registration) =>
        form.bindFromRequest().fold(
          (formWithErrors: Form[_]) =>
            Future.successful(BadRequest(confirmPartnershipDetails(
              appConfig,
              formWithErrors,
              registration.response.organisation.organisationName,
              registration.response.address
            ))),
          {
            case true =>
              upsert(request.userAnswers, PartnershipRegisteredAddressId)(registration.response.address) { userAnswers =>
                upsert(userAnswers, PartnershipDetailsId)(partnershipDetails.copy(registration.response.organisation.organisationName)) { userAnswers =>
                  upsert(userAnswers, RegistrationInfoId)(registration.info) { userAnswers =>
                    dataCacheConnector.upsert(request.externalId, userAnswers.json).map { _ =>
                      Redirect(navigator.nextPage(PartnershipRegisteredAddressId, NormalMode, userAnswers))
                    }
                  }
                }
              }
            case false => Future.successful(Redirect(controllers.register.company.routes.CompanyUpdateDetailsController.onPageLoad()))
          }
        )
      }
  }


  private def getPartnershipDetails(fn: (BusinessDetails, OrganizationRegistration) => Future[Result])
                                   (implicit request: DataRequest[AnyContent]): Either[Future[Result], Future[Result]] = {

    (PartnershipDetailsId and BusinessTypeId).retrieve.right.map {
      case businessDetails ~ businessType =>
        val organisation = Organisation(businessDetails.companyName, businessType)
        registrationConnector.registerWithIdOrganisation(businessDetails.uniqueTaxReferenceNumber, organisation).flatMap {
          registration =>
            fn(businessDetails, registration)
        } recoverWith {
          case _: NotFoundException =>
            Future.successful(Redirect(controllers.routes.UnauthorisedController.onPageLoad()))
        }
    }
  }

  private def upsert[I <: TypedIdentifier.PathDependent](userAnswers: UserAnswers, id: I)(value: id.Data)
                                                        (fn: UserAnswers => Future[Result])
                                                        (implicit writes: Writes[id.Data]) = {

    userAnswers
      .set(id)(value)
      .fold(
        errors => {
          Logger.error("Unable to set user answer", JsResultException(errors))
          Future.successful(InternalServerError)
        },
        userAnswers => fn(userAnswers)
      )
  }
}
